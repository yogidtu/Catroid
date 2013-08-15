/**
 *  Catroid: An on-device visual programming system for Android devices
 *  Copyright (C) 2010-2013 The Catrobat Team
 *  (<http://developer.catrobat.org/credits>)
 *  
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as
 *  published by the Free Software Foundation, either version 3 of the
 *  License, or (at your option) any later version.
 *  
 *  An additional term exception under section 7 of the GNU Affero
 *  General Public License, version 3, is available at
 *  http://developer.catrobat.org/license_additional_term
 *  
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU Affero General Public License for more details.
 *  
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.catrobat.catroid.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.ArrayList;
import java.util.HashMap;

import org.catrobat.catroid.speechrecognition.AdaptiveEnergyVoiceDetection;
import org.catrobat.catroid.speechrecognition.AudioInputStream;
import org.catrobat.catroid.speechrecognition.GoogleOnlineSpeechRecognizer;
import org.catrobat.catroid.speechrecognition.RecognizerCallback;
import org.catrobat.catroid.speechrecognition.SpeechRecognizer;
import org.catrobat.catroid.speechrecognition.VoiceDetection;
import org.catrobat.catroid.speechrecognition.ZeroCrossingVoiceDetection;
import org.catrobat.catroid.stage.StageActivity;

import android.os.Bundle;
import android.util.Log;

public class UtilSpeechRecognition implements RecognizerCallback {
	private static final String TAG = UtilSpeechRecognition.class.getSimpleName();

	private static final int DEFAULT_SERIAL_BUFFER_SIZE = 16384;
	private static final boolean DEBUG_OUTPUT = true;
	private static StageActivity currentRunningStage = null;
	private static String lastAnswer;
	private AudioInputStream inputStream = null;
	private Thread worker = null;
	private boolean runRecognition = false;

	private boolean stopAfterFirstSuccessRecognition = true;
	private boolean parallelRecognition = false;
	private boolean broadcastOnlySuccessResults = true;
	private int silenceBeforeVoiceMs = 400;
	private int minActiveVoiceTimeMr = 100;
	private int silenceAfterVoiceInMs = 550;

	protected ArrayList<RecognizerCallback> askerList = new ArrayList<RecognizerCallback>();
	protected ArrayList<RecognizerCallback> recognizerSelfListenerList = new ArrayList<RecognizerCallback>();
	protected ArrayList<VoiceDetection> detectorList = new ArrayList<VoiceDetection>();
	protected ArrayList<SpeechRecognizer> recognizerList = new ArrayList<SpeechRecognizer>();
	protected HashMap<Long, Integer> recognitionTasks = new HashMap<Long, Integer>();
	protected HashMap<Long, byte[]> recognitionSerialPlayback = new HashMap<Long, byte[]>();
	protected HashMap<Long, ArrayList<String>> recognitionMatches = new HashMap<Long, ArrayList<String>>();

	public UtilSpeechRecognition(AudioInputStream speechInputStream) {
		this.inputStream = speechInputStream;
	}

	public static void setStageActivity(StageActivity currentStage) {
		currentRunningStage = currentStage;
	}

	public static void askUserViaIntent(String question, final RecognizerCallback originCallback) {
		currentRunningStage.askForSpeechInput(question, new RecognizerCallback() {

			@Override
			public void onRecognizerResult(int resultCode, Bundle resultBundle) {
				if (resultCode == RESULT_OK) {
					lastAnswer = resultBundle.getStringArrayList(BUNDLE_RESULT_MATCHES).toString();
				} else {
					lastAnswer = "";
				}
				originCallback.onRecognizerResult(resultCode, resultBundle);
			}

			@Override
			public void onRecognizerError(Bundle errorBundle) {
				lastAnswer = "";
				originCallback.onRecognizerError(errorBundle);
			}
		});
	}

	public static String getLastAnswer() {
		return lastAnswer;
	}

	public void registerContinuousSpeechListener(RecognizerCallback asker) {
		if (asker == null) {
			return;
		}
		synchronized (askerList) {
			askerList.add(asker);
		}
		return;
	}

	public void addVoiceDetector(VoiceDetection detector) throws IllegalStateException {
		if ((worker != null && worker.isAlive()) || isRecognitionRunning()) {
			throw new IllegalStateException();
		}
		if (detector == null) {
			return;
		}
		detectorList.add(detector);
	}

	public void addSpeechRecognizer(SpeechRecognizer recognizer) throws IllegalStateException, IllegalArgumentException {
		if ((worker != null && worker.isAlive()) || isRecognitionRunning()) {
			throw new IllegalStateException();
		}
		if (recognizer == null) {
			return;
		}
		if (!recognizer.isAudioFormatSupported(inputStream)) {
			throw new IllegalArgumentException("SpeechRecognizer doesn't support the AudioInputStream.");
		}

		if (recognizer instanceof RecognizerCallback) {
			recognizerSelfListenerList.add((RecognizerCallback) recognizer);
		}

		recognizerList.add(recognizer);
	}

	public void start() throws IllegalStateException {

		if (inputStream == null) {
			throw new IllegalStateException("No input source set.");
		}
		if (askerList.size() == 0) {
			throw new IllegalStateException("No listener registered. For whom are we playing?");
		}
		if (recognizerList.size() == 0) {
			//Use default
			addSpeechRecognizer(new GoogleOnlineSpeechRecognizer());
		}
		if (detectorList.size() == 0) {
			//Use default
			addVoiceDetector(new AdaptiveEnergyVoiceDetection());
			addVoiceDetector(new ZeroCrossingVoiceDetection());
		}
		for (VoiceDetection detecor : detectorList) {
			detecor.resetState();
		}
		for (SpeechRecognizer recognizer : recognizerList) {
			for (RecognizerCallback selfListener : recognizerSelfListenerList) {
				recognizer.addCallbackListener(selfListener);
			}
			recognizer.addCallbackListener(this);
			recognizer.prepare();
		}
		runRecognition = true;
		worker = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					executeRecognitionChain();
				} catch (IOException e) {
					runRecognition = false;

					Bundle errorBundle = new Bundle();
					errorBundle.putString(RecognizerCallback.BUNDLE_ERROR_MESSAGE,
							"Error when executing recognitionchain. " + e.getMessage());
					errorBundle.putInt(RecognizerCallback.BUNDLE_ERROR_CODE, ERROR_IO);
					UtilSpeechRecognition.this.onRecognizerError(errorBundle);
					inputStream = null;
				}
				synchronized (this) {
					this.notifyAll();
				}
			}
		});
		worker.start();
	}

	private void executeRecognitionChain() throws IOException {
		int frameSize = inputStream.getFrameByteSize();
		int silentPreFrames = (int) (inputStream.getSampleRate() / frameSize * (silenceBeforeVoiceMs / 1000f));
		int silentPostFrames = (int) (inputStream.getSampleRate() / frameSize * (silenceAfterVoiceInMs / 1000f));
		int minActiveFrames = (int) (inputStream.getSampleRate() / frameSize * (minActiveVoiceTimeMr / 1000f));
		byte[] frameBuffer = new byte[frameSize];
		byte[] preBuffer = new byte[silentPreFrames * frameSize];
		byte[] activeBuffer = new byte[minActiveFrames * frameSize];
		ArrayList<OutputStream> currentOpenStreamList = new ArrayList<OutputStream>();
		ByteArrayOutputStream serialPlaybackStream = null;
		long currentIdentifier = 0;
		int processedSilentFrames = 0;
		int processedActiveFrames = 0;
		boolean recognizerAreListening = false;
		ArrayList<OutputStream> copyStreamList = new ArrayList<OutputStream>(currentOpenStreamList);

		while (runRecognition) {
			int offset = 0;
			int shortRead = 0;
			boolean voiceFrame = false;

			while (offset < frameSize) {
				shortRead = inputStream.read(frameBuffer, offset, frameSize - offset);
				offset += shortRead;
				if (shortRead == -1) {
					for (OutputStream feedStream : currentOpenStreamList) {
						feedStream.write(frameBuffer, 0, offset);
						feedStream.flush();
						feedStream.close();
					}
					currentOpenStreamList.clear();
					if (!parallelRecognition) {
						recognitionSerialPlayback.put(currentIdentifier, serialPlaybackStream.toByteArray());
					}
					runRecognition = false;
					return;
				}
			}

			double[] frame = audioByteToDouble(frameBuffer, inputStream.getSampleSizeInBits() / 8);
			for (VoiceDetection detector : detectorList) {
				voiceFrame = detector.isFrameWithVoice(frame);
				if (voiceFrame) {
					processedSilentFrames = 0;
					break;
				}
			}

			//PreProcessing
			if (!recognizerAreListening) {
				if (!voiceFrame) {
					System.arraycopy(preBuffer, frameSize, preBuffer, 0, preBuffer.length - frameSize);
					System.arraycopy(frameBuffer, 0, preBuffer, preBuffer.length - frameSize, frameSize);

					if (DEBUG_OUTPUT && processedActiveFrames != 0) {
						Log.v(TAG, "resetting proccessed active frames");
					}
					processedActiveFrames = 0;
					continue;
				} else if ((++processedActiveFrames < minActiveFrames)) {
					System.arraycopy(activeBuffer, frameSize, activeBuffer, 0, activeBuffer.length - frameSize);
					System.arraycopy(frameBuffer, 0, activeBuffer, activeBuffer.length - frameSize, frameSize);
					continue;
				} else {
					//start streaming
					currentIdentifier = System.currentTimeMillis();
					if (DEBUG_OUTPUT) {
						Log.v(TAG, "Starting partial recognition" + currentIdentifier);
					}
					for (SpeechRecognizer recognizer : recognizerList) {
						PipedOutputStream feedStream = new PipedOutputStream();
						PipedInputStream recieverStream = new PipedInputStream(feedStream, inputStream.getSampleRate()
								/ frameSize * 100);
						currentOpenStreamList.add(feedStream);
						recognizer.startRecognizeInput(new AudioInputStream(recieverStream, inputStream),
								currentIdentifier);
						feedStream.write(preBuffer);
						feedStream.write(activeBuffer);
						if (!parallelRecognition) {
							serialPlaybackStream = new ByteArrayOutputStream(DEFAULT_SERIAL_BUFFER_SIZE);
							currentOpenStreamList.add(serialPlaybackStream);
							serialPlaybackStream.write(preBuffer);
							serialPlaybackStream.write(activeBuffer);
							break;
						}
					}
					recognitionTasks.put(currentIdentifier, recognizerList.size());
					recognitionMatches.put(currentIdentifier, new ArrayList<String>());
					recognizerAreListening = true;
					copyStreamList = new ArrayList<OutputStream>(currentOpenStreamList);
				}
			}

			for (OutputStream feedStream : copyStreamList) {
				try {
					feedStream.write(frameBuffer);
				} catch (IOException e) {
					currentOpenStreamList.remove(feedStream);
				}
			}
			if (currentOpenStreamList.size() == 0) {
				recognizerAreListening = false;
			}

			if (voiceFrame || (++processedSilentFrames < silentPostFrames)) {
				continue;
			}

			if (!voiceFrame) {
				for (OutputStream feedStream : currentOpenStreamList) {
					feedStream.flush();
					feedStream.close();
				}
				if (!parallelRecognition) {
					recognitionSerialPlayback.put(currentIdentifier, serialPlaybackStream.toByteArray());
				}
				currentOpenStreamList.clear();
				recognizerAreListening = false;
				preBuffer = new byte[preBuffer.length];
				activeBuffer = new byte[activeBuffer.length];
				if (DEBUG_OUTPUT) {
					Log.v(TAG, "Stopped partial recognition " + currentIdentifier);
				}
				processedSilentFrames = 0;
				processedActiveFrames = 0;
			}
		}

		//We may be stopped from outside
		for (OutputStream feedStream : currentOpenStreamList) {
			feedStream.write(frameBuffer);
			feedStream.flush();
			feedStream.close();
		}
		if (!parallelRecognition && serialPlaybackStream != null) {
			recognitionSerialPlayback.put(currentIdentifier, serialPlaybackStream.toByteArray());
		}
		currentOpenStreamList.clear();
		inputStream.close();
		inputStream = null;

	}

	public void unregisterContinuousSpeechListener(RecognizerCallback asker) {
		synchronized (askerList) {
			if (askerList.contains(asker)) {
				askerList.remove(asker);

				if (askerList.size() == 0) {
					runRecognition = false;
				}
			} else {
				if (DEBUG_OUTPUT) {
					Log.w(TAG, "Tried to remove not registered speechListener. Caller:"
							+ asker.getClass().getSimpleName());
				}
			}
		}
		return;
	}

	private boolean checkRecognizerCallbackAndMarkUsed(long identifier) {

		int remainingRecognizer = recognitionTasks.get(identifier);
		if ((--remainingRecognizer) <= 0) {
			sendResultsToListener(identifier);
			recognitionTasks.remove(identifier);
			return false;
		} else {
			recognitionTasks.put(identifier, remainingRecognizer);
		}
		return true;
	}

	private void startNextRecognizerInSerie(long identifier) {
		byte[] playbackBuffer = recognitionSerialPlayback.get(identifier);
		if (playbackBuffer == null) {
			return;
		}
		ByteArrayInputStream playbackStream = new ByteArrayInputStream(playbackBuffer);
		AudioInputStream voiceProviderStream = new AudioInputStream(playbackStream, inputStream);
		int nextRecognizer = recognizerList.size() - recognitionTasks.get(identifier);
		recognizerList.get(nextRecognizer).startRecognizeInput(voiceProviderStream, identifier);
	}

	@Override
	public void onRecognizerResult(int resultCode, Bundle resultBundle) {
		Long identifier = resultBundle.getLong(BUNDLE_IDENTIFIER);
		if (!recognitionTasks.containsKey(identifier)) {
			if (DEBUG_OUTPUT) {
				Log.v(TAG, "dead Recognizer called results. identifier:" + identifier);
			}
			return;
		}
		if (resultCode == RESULT_OK) {
			ArrayList<String> matches = resultBundle.getStringArrayList(BUNDLE_RESULT_MATCHES);
			ArrayList<String> allMatches = recognitionMatches.get(identifier);
			allMatches.addAll(matches);
			recognitionMatches.put(identifier, allMatches);
		}
		if (!checkRecognizerCallbackAndMarkUsed(identifier)) {
			return;
		}
		if (stopAfterFirstSuccessRecognition && resultCode == RecognizerCallback.RESULT_OK) {
			recognitionTasks.remove(identifier);
			sendResultsToListener(identifier);
			recognitionSerialPlayback.remove(identifier);
		}
		if (!parallelRecognition) {
			startNextRecognizerInSerie(identifier);
		}
	}

	private void sendResultsToListener(long identifier) {
		if (!recognitionMatches.containsKey(identifier)) {
			return;
		}
		ArrayList<String> matches = recognitionMatches.get(identifier);
		if (matches.size() == 0 && broadcastOnlySuccessResults) {
			return;
		}
		ArrayList<RecognizerCallback> listenerListCopy = new ArrayList<RecognizerCallback>(askerList);
		Bundle resultBundle = new Bundle();
		resultBundle.putStringArrayList(BUNDLE_RESULT_MATCHES, matches);
		resultBundle.putLong(BUNDLE_IDENTIFIER, identifier);
		for (RecognizerCallback listener : listenerListCopy) {
			listener.onRecognizerResult(matches.size() > 0 ? RESULT_OK : RESULT_NOMATCH, resultBundle);
		}
		recognitionMatches.remove(identifier);
	}

	private void sendErrorToListener(Bundle errorBundle) {
		ArrayList<RecognizerCallback> listenerListCopy = new ArrayList<RecognizerCallback>(askerList);
		for (RecognizerCallback listener : listenerListCopy) {
			listener.onRecognizerError(errorBundle);
		}
	}

	@Override
	public void onRecognizerError(Bundle errorBundle) {
		if (DEBUG_OUTPUT) {
			Log.v(TAG,
					"Recieved Error: " + errorBundle.getInt(BUNDLE_ERROR_CODE) + " - "
							+ errorBundle.getString(BUNDLE_ERROR_MESSAGE));
		}
		Long identifier = errorBundle.getLong(BUNDLE_IDENTIFIER);
		if (!recognitionTasks.containsKey(identifier)) {
			if (DEBUG_OUTPUT) {
				Log.v(TAG, "dead Recognizer called results. identifier:" + identifier);
			}
			return;
		}
		checkRecognizerCallbackAndMarkUsed(identifier);
		switch (errorBundle.getInt(BUNDLE_ERROR_CODE)) {
			case ERROR_NONETWORK:
			case ERROR_OTHER:
				//need to stop all, it's fatal?
				stop();
				break;
			case ERROR_API_CHANGED:
			case ERROR_IO:
				for (SpeechRecognizer faultyRecognizer : recognizerList) {
					if (faultyRecognizer.toString() == errorBundle.getString(BUNDLE_ERROR_CALLERCLASS)) {
						stop();
						recognizerList.remove(faultyRecognizer);
						if (recognizerList.size() != 0) {
							start();
						}
						break;
					}
				}
				String errorMessage = "Recognizer " + errorBundle.getString(BUNDLE_ERROR_CALLERCLASS)
						+ " needed to be removed.";
				errorMessage += "Original Error Message: " + errorBundle.getString(BUNDLE_ERROR_MESSAGE);
				errorBundle.putString(BUNDLE_ERROR_MESSAGE, errorMessage);
			default:
				Log.w(TAG, "Unhandled errorcode was thrown.");
				break;
		}
		if (!this.isRecognitionRunning()) {
			errorBundle.putBoolean(BUNDLE_ERROR_FATAL_FLAG, true);
		} else {
			errorBundle.putBoolean(BUNDLE_ERROR_FATAL_FLAG, false);
		}
		sendErrorToListener(errorBundle);
	}

	public void unregisterAllAndStop() {
		synchronized (askerList) {
			askerList.clear();
			stop();
		}
	}

	public void stop() {
		runRecognition = false;
		if (worker != null && worker.isAlive()) {
			synchronized (worker) {
				try {
					worker.wait();
				} catch (InterruptedException e) {
				}
			}
		}
	}

	public boolean isRecognitionRunning() {
		return recognitionTasks.size() > 0;
	}

	public void setParalellSpeechRecognizerProcessing(boolean enabled) throws IllegalStateException {
		if ((worker != null && worker.isAlive()) || isRecognitionRunning()) {
			throw new IllegalStateException();
		}
		parallelRecognition = enabled;
	}

	public void setProcessOnlyTillFirstSuccessRecognizer(boolean enabled) throws IllegalStateException {
		if ((worker != null && worker.isAlive()) || isRecognitionRunning()) {
			throw new IllegalStateException();
		}
		stopAfterFirstSuccessRecognition = enabled;
	}

	public void setBroadcastOnlySuccessResults(boolean enabled) {
		broadcastOnlySuccessResults = enabled;
	}

	private static double[] audioByteToDouble(byte[] samples, int bytesPerSample) {

		double[] micBufferData = new double[samples.length / bytesPerSample];

		final double amplification = 1000.0;
		for (int index = 0, floatIndex = 0; index < samples.length - bytesPerSample + 1; index += bytesPerSample, floatIndex++) {
			double sample = 0;
			for (int b = 0; b < bytesPerSample; b++) {
				int v = samples[index + b];
				if (b < bytesPerSample - 1 || bytesPerSample == 1) {
					v &= 0xFF;
				}
				sample += v << (b * 8);
			}
			double sample32 = amplification * (sample / 32768.0);
			micBufferData[floatIndex] = sample32;
		}
		return micBufferData;
	}
}
