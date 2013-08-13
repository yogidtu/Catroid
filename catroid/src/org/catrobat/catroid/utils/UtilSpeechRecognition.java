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

import java.io.IOException;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.HashMap;

import org.catrobat.catroid.speechrecognition.AudioInputStream;
import org.catrobat.catroid.speechrecognition.FeedableInputStream;
import org.catrobat.catroid.speechrecognition.RecognizerCallback;
import org.catrobat.catroid.speechrecognition.SpeechRecognizer;
import org.catrobat.catroid.speechrecognition.VoiceDetection;

import android.os.Bundle;
import android.util.Log;

public class UtilSpeechRecognition implements RecognizerCallback {

	private static UtilSpeechRecognition instance = null;
	private static final String TAG = UtilSpeechRecognition.class.getSimpleName();

	private static final boolean DEBUG_OUTPUT = false;
	//	private String lastBestAnswer = "";
	//	private ArrayList<String> lastAnswerSuggenstions = new ArrayList<String>();
	private AudioInputStream inputStream = null;
	private boolean runRecognition = false;
	private Thread worker = null;

	private boolean stopAfterFirstSuccessRecognition = true;
	private boolean parallelRecognition = false;
	private int silenceBeforeVoiceMs = 50;
	private int silenceAfterVoiceInMs = 150;

	protected ArrayList<SpeechRecognizeListener> askerList = new ArrayList<SpeechRecognizeListener>();
	protected ArrayList<RecognizerCallback> selfListenerList = new ArrayList<RecognizerCallback>();
	protected ArrayList<VoiceDetection> detectorList = new ArrayList<VoiceDetection>();
	protected ArrayList<SpeechRecognizer> recognizerList = new ArrayList<SpeechRecognizer>();
	protected HashMap<Long, Integer> recognitionTasks = new HashMap<Long, Integer>();
	protected HashMap<Long, FeedableInputStream> recognitionFeedStream = new HashMap<Long, FeedableInputStream>();
	protected HashMap<Long, ArrayList<String>> recognitionMatches = new HashMap<Long, ArrayList<String>>();

	protected UtilSpeechRecognition() {

	}

	public static UtilSpeechRecognition getInstance() {
		if (instance == null) {
			instance = new UtilSpeechRecognition();
		}
		return instance;
	}

	public void registerContinuousSpeechListener(SpeechRecognizeListener asker) throws IllegalStateException {
		if (inputStream == null || detectorList.size() == 0 || recognizerList.size() == 0) {
			throw new IllegalStateException();
		}

		synchronized (askerList) {

			if (askerList.size() == 0) {
				for (VoiceDetection detecor : detectorList) {
					detecor.resetState();
				}

				for (SpeechRecognizer recognizer : recognizerList) {
					for (RecognizerCallback selfListener : selfListenerList) {
						recognizer.addCallbackListener(selfListener);
					}
					recognizer.addCallbackListener(this);
					recognizer.prepare();
				}
				runRecognition = true;
				worker = new Thread(new Runnable() {
					@Override
					public void run() {
						executeRecognitionChain();
					}
				});
				worker.start();
			}
			askerList.add(asker);
		}
		return;
	}

	private void executeRecognitionChain() {
		int frameSize = inputStream.getFrameByteSize();
		int silentPreFrames = (int) (inputStream.getSampleRate() / frameSize * (silenceBeforeVoiceMs / 1000f));
		int silentPostFrames = (int) (inputStream.getSampleRate() / frameSize * (silenceAfterVoiceInMs / 1000f));
		byte[] frameBuffer = new byte[frameSize];
		byte[] preBuffer = new byte[silentPreFrames * frameSize];
		ArrayList<FeedableInputStream> currentStreamList = new ArrayList<FeedableInputStream>();
		int processedSilentFrames = 0;

		boolean inRecognition = false;

		while (runRecognition) {
			int offset = 0;
			int shortRead = 0;

			while (offset < frameSize) {
				try {
					shortRead = inputStream.read(frameBuffer, offset, frameSize - offset);
				} catch (IOException e) {
					runRecognition = false;
					for (FeedableInputStream feedStream : currentStreamList) {
						feedStream.setNextBufferJunk(frameBuffer, offset);
						feedStream.setEndReachingCloses();
					}
					return;
				}

				offset += shortRead;
				if (shortRead == -1) {
					runRecognition = false;
					for (FeedableInputStream feedStream : currentStreamList) {
						feedStream.setNextBufferJunk(frameBuffer, offset);
						feedStream.setEndReachingCloses();
					}
					return;
				}
			}

			double[] frame = audioByteToDouble(frameBuffer, inputStream.getSampleSizeInBits() / 8);
			boolean voiceFrame = false;

			for (VoiceDetection detector : detectorList) {
				if (detector.isFrameWithVoice(frame)) {
					voiceFrame = true;
					processedSilentFrames = 0;
				}
			}

			if (inRecognition && !voiceFrame && (++processedSilentFrames < silentPostFrames)) {
				voiceFrame = true;
			}

			if (voiceFrame) {
				if (!inRecognition) {
					if (DEBUG_OUTPUT) {
						Log.w(TAG, "Starting recognition...");
					}

					System.arraycopy(preBuffer, frameSize, preBuffer, 0, preBuffer.length - frameSize);
					System.arraycopy(frameBuffer, 0, preBuffer, preBuffer.length - frameSize, frameSize);

					long identifier = System.currentTimeMillis();
					if (parallelRecognition) {
						for (SpeechRecognizer recognizer : recognizerList) {
							FeedableInputStream feedStream = new FeedableInputStream(preBuffer, false, frameSize * 4);
							currentStreamList.add(feedStream);
							AudioInputStream voiceProviderStream = new AudioInputStream(feedStream,
									inputStream.getEncoding(), inputStream.getChannels(), inputStream.getSampleRate(),
									inputStream.getFrameByteSize(), inputStream.isBigEndian() ? ByteOrder.BIG_ENDIAN
											: ByteOrder.LITTLE_ENDIAN, inputStream.isSigned());
							recognizer.startRecognizeInput(voiceProviderStream, identifier);
						}
					} else {
						FeedableInputStream feedStream = new FeedableInputStream(preBuffer, true);
						currentStreamList.add(feedStream);
						AudioInputStream voiceProviderStream = new AudioInputStream(feedStream,
								inputStream.getEncoding(), inputStream.getChannels(), inputStream.getSampleRate(),
								inputStream.getFrameByteSize(), inputStream.isBigEndian() ? ByteOrder.BIG_ENDIAN
										: ByteOrder.LITTLE_ENDIAN, inputStream.isSigned());
						recognizerList.get(0).startRecognizeInput(voiceProviderStream, identifier);
						recognitionFeedStream.put(identifier, feedStream);
					}
					recognitionTasks.put(identifier, recognizerList.size());
					recognitionMatches.put(identifier, new ArrayList<String>());
					inRecognition = true;
				} else {
					for (FeedableInputStream feedStream : currentStreamList) {
						feedStream.setNextBufferJunk(frameBuffer);
					}
				}
			} else {
				if (inRecognition) {
					for (FeedableInputStream feedStream : currentStreamList) {
						feedStream.setNextBufferJunk(frameBuffer);
						feedStream.setEndReachingCloses();
					}
					currentStreamList.clear();
					inRecognition = false;
					preBuffer = new byte[preBuffer.length];
					if (DEBUG_OUTPUT) {
						Log.w(TAG, "Stopped recognition...");
					}
				} else {
					System.arraycopy(preBuffer, frameSize, preBuffer, 0, preBuffer.length - frameSize);
					System.arraycopy(frameBuffer, 0, preBuffer, preBuffer.length - frameSize, frameSize);
				}
			}
		}
	}

	public void unregisterContinuousSpeechListener(SpeechRecognizeListener asker) {
		synchronized (askerList) {
			if (askerList.contains(asker)) {
				askerList.remove(asker);

				if (askerList.size() == 0) {
					runRecognition = false;
				}
			} else {
				if (DEBUG_OUTPUT) {
					Log.w(TAG, "Tried to remove not registered speechListener. " + asker.getClass().getSimpleName());
				}
			}
		}
		return;
	}

	public boolean isRecognitionRunning() {
		return recognitionTasks.size() > 0;
	}

	@Override
	public void onRecognizerResult(int resultCode, Bundle resultBundle) {

		Long identifier = resultBundle.getLong(BUNDLE_IDENTIFIER);

		if (!recognitionTasks.containsKey(identifier)) {
			if (DEBUG_OUTPUT) {
				Log.v(TAG, "dead Recognizer");
			}
			return;
		}

		if (resultCode == RESULT_OK) {
			ArrayList<String> matches = resultBundle.getStringArrayList(RESULT_BUNDLE_MATCHES);
			recognitionMatches.get(identifier).addAll(matches);
		}

		if (stopAfterFirstSuccessRecognition && resultCode == RecognizerCallback.RESULT_OK) {
			recognitionTasks.remove(identifier);
			sendResultsToListener(identifier);
			recognitionFeedStream.remove(identifier);
		}

		int remainingRecognizer = recognitionTasks.get(identifier);
		if ((--remainingRecognizer) == 0) {
			sendResultsToListener(identifier);
			recognitionTasks.remove(identifier);
			return;
		}

		if (!parallelRecognition) {
			FeedableInputStream serialStream = recognitionFeedStream.get(identifier);
			serialStream.resetToBeginning();
			AudioInputStream voiceProviderStream = new AudioInputStream(serialStream, inputStream.getEncoding(),
					inputStream.getChannels(), inputStream.getSampleRate(), inputStream.getFrameByteSize(),
					inputStream.isBigEndian() ? ByteOrder.BIG_ENDIAN : ByteOrder.LITTLE_ENDIAN, inputStream.isSigned());
			recognizerList.get(recognizerList.size() - remainingRecognizer).startRecognizeInput(voiceProviderStream,
					identifier);
			recognitionFeedStream.put(identifier, serialStream);
		}
	}

	private void sendResultsToListener(long identifier) {
		if (!recognitionMatches.containsKey(identifier)) {
			return;
		}
		ArrayList<String> matches = recognitionMatches.get(identifier);
		if (matches.size() == 0) {
			return;
		}
		ArrayList<SpeechRecognizeListener> listenerListCopy = new ArrayList<UtilSpeechRecognition.SpeechRecognizeListener>(
				askerList);

		for (SpeechRecognizeListener listener : listenerListCopy) {
			listener.onRecognizedSpeech(matches);
		}
		recognitionMatches.remove(identifier);
	}

	@Override
	public void onRecognizerError(Bundle errorBundle) {

	}

	public void addVoiceDetector(VoiceDetection detector) {
		if (detector == null) {
			return;
		}
		detectorList.add(detector);
	}

	public void addSpeechRecognizer(SpeechRecognizer recognizer) {
		if (recognizer == null) {
			return;
		}
		if (recognizer instanceof RecognizerCallback) {
			selfListenerList.add((RecognizerCallback) recognizer);
		}
		recognizerList.add(recognizer);
	}

	public void setParalellSpeechRecognizerProcessing(boolean enabled) {
		parallelRecognition = enabled;
	}

	public void setUseAllRecognizer(boolean enabled) {
		stopAfterFirstSuccessRecognition = enabled;
	}

	public void setInputStream(AudioInputStream speechInputStream) {
		this.inputStream = speechInputStream;
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

	public interface SpeechRecognizeListener {
		public void onRecognizedSpeech(ArrayList<String> allAnswerSuggestions);
	}
}
