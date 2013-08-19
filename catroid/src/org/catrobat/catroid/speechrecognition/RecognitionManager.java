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
package org.catrobat.catroid.speechrecognition;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.ArrayList;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.catrobat.catroid.speechrecognition.recognizer.GoogleOnlineSpeechRecognizer;
import org.catrobat.catroid.speechrecognition.voicedetection.AdaptiveEnergyVoiceDetection;
import org.catrobat.catroid.stage.StageActivity;
import org.catrobat.catroid.utils.MicrophoneGrabber;

import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;

public class RecognitionManager implements RecognizerCallback {
	private static final String TAG = RecognitionManager.class.getSimpleName();

	private static final int DEFAULT_SERIAL_BUFFER_SIZE = 16384;
	private static final int MAX_SPEECH_CHUNK_TIME = 30000;
	private static final boolean DEBUG_OUTPUT = true;

	private static StageActivity currentRunningStage = null;
	private static String lastAnswer;
	private AudioInputStream inputStream = null;
	private Thread worker = null;
	private boolean runRecognition = false;

	private boolean stopAfterFirstSuccessRecognition = true;
	private boolean parallelRecognition = false;
	private boolean broadcastOnlySuccessResults = true;
	private boolean paused = false;
	private int targetFrameTime = 20;
	private int silenceBeforeVoiceMs = 200;
	private int minActiveVoiceTimeMs = 40;
	private int silenceAfterVoiceInMs = 270;
	private ArrayList<OutputStream> currentOpenStreamList = new ArrayList<OutputStream>();
	private int currentIdentifier = 0;

	protected ArrayList<RecognizerCallback> askerList = new ArrayList<RecognizerCallback>();
	protected ArrayList<RecognizerCallback> recognizerSelfListenerList = new ArrayList<RecognizerCallback>();
	protected ArrayList<VoiceDetection> detectorList = new ArrayList<VoiceDetection>();
	protected ArrayList<SpeechRecognizer> recognizerList = new ArrayList<SpeechRecognizer>();
	protected SparseArray<ArrayList<SpeechRecognizer>> recognitionTasks = new SparseArray<ArrayList<SpeechRecognizer>>();
	//	protected HashMap<Integer, ArrayList<SpeechRecognizer>> recognitionTasks = new HashMap<Integer, ArrayList<SpeechRecognizer>>();
	protected SparseArray<ByteArrayOutputStream> recognitionPlayback = new SparseArray<ByteArrayOutputStream>();
	//	protected HashMap<Integer, ByteArrayOutputStream> recognitionPlayback = new HashMap<Integer, ByteArrayOutputStream>();
	protected SparseArray<ArrayList<String>> recognitionMatches = new SparseArray<ArrayList<String>>();

	//	protected HashMap<Integer, ArrayList<String>> recognitionMatches = new HashMap<Integer, ArrayList<String>>();

	public RecognitionManager(AudioInputStream speechInputStream) {
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

	public static int isWordRecognizeable(String wordToValidate) {
		int isValidWord = 0;
		final HttpParams httpParammeters = new BasicHttpParams();
		HttpConnectionParams.setConnectionTimeout(httpParammeters, 4000);
		HttpClient httpclient = new DefaultHttpClient(httpParammeters);
		HttpGet httppost = new HttpGet("http://en.wiktionary.org/w/api.php?action=opensearch&search=" + wordToValidate
				+ "");
		try {
			if (DEBUG_OUTPUT) {
				Log.v(TAG, "Query dictionary for \"" + wordToValidate + "\".");
			}
			HttpResponse response = httpclient.execute(httppost);

			BufferedReader reader = new BufferedReader(
					new InputStreamReader(response.getEntity().getContent(), "UTF-8"));
			StringBuilder builder = new StringBuilder();
			for (String line = null; (line = reader.readLine()) != null;) {
				builder.append(line).append("\n");
			}
			String resp = builder.toString();
			if (!resp.contains("[]")) {
				if (DEBUG_OUTPUT) {
					Log.v(TAG, "\"" + wordToValidate + "\" seems valid.");
				}
				isValidWord = 1;
			} else if (DEBUG_OUTPUT) {
				Log.v(TAG, "No dict-entry for \"" + wordToValidate + "\".");
			}

		} catch (Exception e) {
			if (DEBUG_OUTPUT) {
				Log.v(TAG, "Error for dict-entry \"" + wordToValidate + "\"." + e.getMessage());
			}
			isValidWord = -1;
		}
		return isValidWord;
	}

	public void registerContinuousSpeechListener(RecognizerCallback asker) {
		if (asker == null) {
			return;
		}
		synchronized (askerList) {
			askerList.add(asker);
			if (askerList.size() > 0 && paused) {
				paused = false;
			}
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
			try {
				addSpeechRecognizer(new GoogleOnlineSpeechRecognizer());
			} catch (IllegalArgumentException e) {
				throw new IllegalStateException("Default recognizer module coulnd't be used on this stream.");
			}
		}
		if (detectorList.size() == 0) {
			//Use default
			addVoiceDetector(new AdaptiveEnergyVoiceDetection());
			//addVoiceDetector(new ZeroCrossingVoiceDetection());
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
							"Error when executing recognitionchain." + e.getMessage());
					errorBundle.putInt(RecognizerCallback.BUNDLE_ERROR_CODE, ERROR_IO);
					RecognitionManager.this.onRecognizerError(errorBundle);
					inputStream = null;
				}
				synchronized (worker) {
					worker.notifyAll();
				}
			}
		});
		worker.start();
	}

	private void executeRecognitionChain() throws IOException {
		int frameSize = (int) (inputStream.getSampleRate() / 1000f * targetFrameTime * (inputStream
				.getSampleSizeInBits() / 8));
		int silentPreFrames = silenceBeforeVoiceMs / targetFrameTime;
		int silentPostFrames = silenceAfterVoiceInMs / targetFrameTime;
		int minActiveFrames = minActiveVoiceTimeMs / targetFrameTime;
		int maxActiveFrames = MAX_SPEECH_CHUNK_TIME / targetFrameTime;
		if (DEBUG_OUTPUT) {
			Log.v(TAG, "frameSize for " + targetFrameTime + "ms: " + frameSize + "\nsilentPreFrames: "
					+ silentPreFrames + "\nsilentPostFrames: " + silentPostFrames + "\nminActiveFrames:"
					+ minActiveFrames + "\nmaxactiveframe:" + maxActiveFrames);
		}
		byte[] frameBuffer = new byte[frameSize];
		double[] frame = new double[frameSize / (inputStream.getSampleSizeInBits() / 8)];
		byte[] preBuffer = new byte[silentPreFrames * frameSize];
		byte[] activeBuffer = new byte[minActiveFrames * frameSize];

		int processedSilentFrames = 0;
		int processedActiveFrames = 0;
		boolean recognizerAreListening = false;
		ArrayList<OutputStream> copyStreamList = new ArrayList<OutputStream>(currentOpenStreamList);

		while (runRecognition || recognizerAreListening) {
			boolean endOfStream = false;
			int offset = 0;
			int shortRead = 0;
			boolean frameContainsVoice = false;

			while (offset < frameSize) {
				shortRead = inputStream.read(frameBuffer, offset, frameSize - offset);

				if (shortRead == -1) {
					endOfStream = true;
					break;
				}
				offset += shortRead;
			}
			if (endOfStream) {
				for (OutputStream feedStream : currentOpenStreamList) {
					feedStream.write(frameBuffer, 0, offset);
					feedStream.flush();
					feedStream.close();
				}
				currentOpenStreamList.clear();
				recognizerAreListening = false;
				runRecognition = false;
				currentIdentifier = 0;
				continue;
			}
			if (paused && !recognizerAreListening) {
				continue;
			}

			MicrophoneGrabber.audioByteToDouble(frameBuffer, frame);
			for (VoiceDetection detector : detectorList) {
				frameContainsVoice = detector.isFrameWithVoice(frame);
				if (frameContainsVoice) {
					processedSilentFrames = 0;
					break;
				}
			}

			if (frameContainsVoice && processedActiveFrames > maxActiveFrames) {
				frameContainsVoice = false;
			}

			//PreProcessing
			if (!recognizerAreListening) {
				if (!frameContainsVoice) {
					System.arraycopy(preBuffer, frameSize, preBuffer, 0, preBuffer.length - frameSize);
					System.arraycopy(frameBuffer, 0, preBuffer, preBuffer.length - frameSize, frameSize);

					if (DEBUG_OUTPUT && processedActiveFrames != 0) {
					}
					processedSilentFrames++;
					processedActiveFrames = 0;
					continue;
				} else if ((++processedActiveFrames <= minActiveFrames)) {
					System.arraycopy(frameBuffer, 0, activeBuffer, (processedActiveFrames - 1) * frameSize, frameSize);
					continue;
				} else {
					currentIdentifier = (int) System.currentTimeMillis();
					recognitionTasks.put(currentIdentifier, new ArrayList<SpeechRecognizer>(recognizerList));
					recognitionMatches.put(currentIdentifier, new ArrayList<String>());
					if (DEBUG_OUTPUT) {
						Log.v(TAG, "Starting partial recognition" + currentIdentifier);
					}
					for (SpeechRecognizer recognizer : recognizerList) {
						PipedOutputStream feedStream = new PipedOutputStream();
						PipedInputStream recieverStream = new PipedInputStream(feedStream, inputStream.getSampleRate()
								/ frameSize * 500);
						currentOpenStreamList.add(feedStream);
						recognizer.startRecognizeInput(new AudioInputStream(recieverStream, inputStream),
								currentIdentifier);
						if (!parallelRecognition) {
							ByteArrayOutputStream serialPlaybackStream = new ByteArrayOutputStream(
									DEFAULT_SERIAL_BUFFER_SIZE);
							currentOpenStreamList.add(serialPlaybackStream);
							recognitionPlayback.put(currentIdentifier, serialPlaybackStream);
							break;
						}
					}
					copyStreamList = new ArrayList<OutputStream>(currentOpenStreamList);
					for (OutputStream feedStream : copyStreamList) {
						try {
							feedStream.write(preBuffer);
							feedStream.write(activeBuffer);
						} catch (IOException e) {
							currentOpenStreamList.remove(feedStream);
						}
					}
					recognizerAreListening = true;
				}
			}

			copyStreamList = new ArrayList<OutputStream>(currentOpenStreamList);
			for (OutputStream feedStream : copyStreamList) {
				try {
					feedStream.write(frameBuffer);
				} catch (IOException e) {
					currentOpenStreamList.remove(feedStream);
				}
			}
			if (currentOpenStreamList.size() == 0) {
				recognizerAreListening = false;
				frameContainsVoice = false;
			}

			if (frameContainsVoice || (++processedSilentFrames < silentPostFrames)) {
				processedActiveFrames++;
				continue;
			}

			if (!frameContainsVoice) {
				for (OutputStream feedStream : currentOpenStreamList) {
					feedStream.flush();
					feedStream.close();
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
				currentIdentifier = 0;
			}
		}
		inputStream.close();
	}

	public void unregisterContinuousSpeechListener(RecognizerCallback asker) {
		synchronized (askerList) {
			if (askerList.contains(asker)) {
				askerList.remove(asker);

				if (askerList.size() == 0) {
					paused = true;
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

	private boolean markUsedAndCheckRemaining(int identifier, String workerName) throws IllegalArgumentException {
		SpeechRecognizer caller = null;
		ArrayList<SpeechRecognizer> taskWorker = recognitionTasks.get(identifier);
		if (taskWorker == null) {
			if (DEBUG_OUTPUT) {
				Log.w(TAG, "No worker found for id:" + identifier);
			}
			throw new IllegalArgumentException();
		}
		for (SpeechRecognizer recognizer : taskWorker) {
			if (recognizer.toString().compareTo(workerName) == 0) {
				caller = recognizer;
			}
		}
		if (caller == null) {
			if (DEBUG_OUTPUT) {
				Log.w(TAG, "Couldn't figure out which recognizer called.");
			}
			throw new IllegalArgumentException();
		}
		taskWorker.remove(caller);
		if (taskWorker.size() == 0) {
			recognitionTasks.remove(identifier);
			recognitionPlayback.remove(identifier);
			if (DEBUG_OUTPUT) {
				Log.v(TAG, "recieved last result for id:" + identifier);
			}
			return false;
		} else {
			recognitionTasks.put(identifier, taskWorker);
			if (DEBUG_OUTPUT) {
				Log.v(TAG, "recieved result, continuing for id:" + identifier);
			}
		}
		return true;
	}

	private void startNextRecognizerInSerie(int identifier) {
		Log.v(TAG, "Will start next Recognizer in list");
		ByteArrayOutputStream byteOutputStream = recognitionPlayback.get(identifier);
		if (byteOutputStream == null) {
			return;
		}
		PipedOutputStream playbackOutputStream = new PipedOutputStream();
		PipedInputStream playbackInputStream = null;
		try {
			playbackInputStream = new PipedInputStream(playbackOutputStream);
		} catch (IOException e) {
		}
		AudioInputStream voiceProviderStream = new AudioInputStream(playbackInputStream, inputStream);
		recognitionTasks.get(identifier).get(0).startRecognizeInput(voiceProviderStream, identifier);
		try {
			playbackOutputStream.write(byteOutputStream.toByteArray());
			if (currentIdentifier == identifier) {
				currentOpenStreamList.add(playbackOutputStream);
			} else {
				playbackOutputStream.close();
			}
		} catch (IOException e) {
		}
	}

	@Override
	public void onRecognizerResult(int resultCode, Bundle resultBundle) {
		int identifier = resultBundle.getInt(BUNDLE_IDENTIFIER);
		synchronized (recognitionTasks) {
			if (recognitionTasks.get(identifier) == null) {
				if (DEBUG_OUTPUT) {
					Log.v(TAG, "dead Recognizer called results. identifier:" + identifier);
				}
				return;
			}
			boolean remainingRecognizer = false;
			try {
				remainingRecognizer = markUsedAndCheckRemaining(identifier, resultBundle.getString(BUNDLE_SENDERCLASS));
			} catch (IllegalArgumentException e) {
				return;
			}
			Log.v(TAG, "remaining recognizer is " + remainingRecognizer);

			if (resultCode == RESULT_OK) {
				ArrayList<String> matches = resultBundle.getStringArrayList(BUNDLE_RESULT_MATCHES);
				ArrayList<String> allMatches = recognitionMatches.get(identifier);
				allMatches.addAll(matches);
				recognitionMatches.put(identifier, allMatches);

				if (stopAfterFirstSuccessRecognition && resultBundle.getBoolean(BUNDLE_RESULT_RECOGNIZED)) {
					sendResultsToListener(identifier);
					recognitionTasks.remove(identifier);
					recognitionPlayback.remove(identifier);
					return;
				}
			}

			if (!remainingRecognizer) {
				sendResultsToListener(identifier);
				if (DEBUG_OUTPUT) {
					Log.v(TAG, "Recieved Result broadcasted and no remaining recognizer");
				}
				return;
			}
			if (!parallelRecognition) {
				startNextRecognizerInSerie(identifier);
			}
		}
	}

	private void sendResultsToListener(int identifier) {
		if (recognitionMatches.get(identifier) == null) {
			Log.v(TAG, "No Matches found");
			return;
		}
		ArrayList<String> matches = recognitionMatches.get(identifier);
		if (matches.size() == 0 && broadcastOnlySuccessResults) {
			Log.v(TAG, "Matchsize null");
			return;
		}
		ArrayList<RecognizerCallback> listenerListCopy = new ArrayList<RecognizerCallback>(askerList);
		Bundle resultBundle = new Bundle();
		resultBundle.putStringArrayList(BUNDLE_RESULT_MATCHES, matches);
		resultBundle.putLong(BUNDLE_IDENTIFIER, identifier);
		Log.v(TAG, "Sending results out");
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
		int identifier = errorBundle.getInt(BUNDLE_IDENTIFIER);
		synchronized (recognitionTasks) {
			if (recognitionTasks.get(identifier) == null) {
				return;
			}
			markUsedAndCheckRemaining(identifier, errorBundle.getString(BUNDLE_SENDERCLASS));
		}
		switch (errorBundle.getInt(BUNDLE_ERROR_CODE)) {
			case ERROR_NONETWORK:
			case ERROR_OTHER:
			case ERROR_API_CHANGED:
			case ERROR_IO:
				for (SpeechRecognizer faultyRecognizer : recognizerList) {
					if (faultyRecognizer.toString().compareTo(errorBundle.getString(BUNDLE_SENDERCLASS)) == 0) {
						removeRecognizerInRuntime(faultyRecognizer);
						break;
					}
				}
				String errorMessage = "Recognizer " + errorBundle.getString(BUNDLE_SENDERCLASS)
						+ " needed to be removed.\n";
				errorMessage += "Errormessage from module: " + errorBundle.getString(BUNDLE_ERROR_MESSAGE);
				errorBundle.putString(BUNDLE_ERROR_MESSAGE, errorMessage);
				break;
			default:
				Log.w(TAG, "Unhandled errorcode was thrown.");
				break;
		}
		if (recognizerList.size() == 0) {
			stop();
			errorBundle.putBoolean(BUNDLE_ERROR_FATAL_FLAG, true);
		} else {
			errorBundle.putBoolean(BUNDLE_ERROR_FATAL_FLAG, false);
		}
		sendErrorToListener(errorBundle);
		if (!parallelRecognition) {
			startNextRecognizerInSerie(identifier);
		}
	}

	private void removeRecognizerInRuntime(SpeechRecognizer badRecognizer) {
		badRecognizer.stopAllTasks();
		synchronized (recognitionTasks) {
			for (int i = 0; i < recognitionTasks.size(); i++) {
				int identifier = recognitionTasks.keyAt(i);
				recognitionTasks.get(identifier).remove(badRecognizer);
			}
		}
		recognizerList.remove(badRecognizer);
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
		return recognitionTasks.size() > 0 || runRecognition;
	}

	public void setParalellChunkProcessing(boolean enabled) throws IllegalStateException {
		if ((worker != null && worker.isAlive()) || isRecognitionRunning()) {
			throw new IllegalStateException();
		}
		parallelRecognition = enabled;
	}

	public void setProcessChunkOnlyTillFirstSuccessRecognizer(boolean enabled) throws IllegalStateException {
		if ((worker != null && worker.isAlive()) || isRecognitionRunning()) {
			throw new IllegalStateException();
		}
		stopAfterFirstSuccessRecognition = enabled;
	}

	public void setBroadcastOnlySuccessResults(boolean enabled) {
		broadcastOnlySuccessResults = enabled;
	}

	public void setRecorderPreSilenceChunkTime(int timeInMs) {
		silenceBeforeVoiceMs = timeInMs;
	}

	public void setRecorderCutChunkAfterSilenceTime(int timeInMs) {
		silenceAfterVoiceInMs = timeInMs;
	}

	public void setRecorderMinVoiceChunkTime(int timeInMs) {
		minActiveVoiceTimeMs = timeInMs;
	}
}
