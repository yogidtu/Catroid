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
package org.catrobat.catroid.test.speechrecognition;

import android.media.AudioFormat;
import android.os.Bundle;
import android.test.InstrumentationTestCase;
import android.util.Log;

import org.catrobat.catroid.speechrecognition.AudioInputStream;
import org.catrobat.catroid.speechrecognition.RecognitionManager;
import org.catrobat.catroid.speechrecognition.RecognizerCallback;
import org.catrobat.catroid.speechrecognition.SpeechRecognizer;
import org.catrobat.catroid.speechrecognition.VoiceDetection;
import org.catrobat.catroid.speechrecognition.recognizer.GoogleOnlineSpeechRecognizer;
import org.catrobat.catroid.speechrecognition.voicedetection.ZeroCrossingVoiceDetection;

import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.nio.ByteOrder;
import java.util.ArrayList;

public class RecognitionManagerTest extends InstrumentationTestCase implements RecognizerCallback {

	private ArrayList<String> lastMatches = new ArrayList<String>();
	private String lastErrorMessage = "";
	private Bundle lastErrorBundle = null;

	@Override
	public void tearDown() throws Exception {
		lastMatches.clear();
		super.tearDown();
	}

	@Override
	public void setUp() {
		lastMatches.clear();
		lastErrorMessage = "";
		lastErrorBundle = null;
	}

	public void testParalellRecognition() throws IOException {
		VoiceDetection alwaysTrueDetection = new VoiceDetection() {

			@Override
			public void setSensibility(VoiceDetectionSensibility sensibility) {
			}

			@Override
			public void resetState() {
			}

			@Override
			public boolean isFrameWithVoice(double[] frame) {
				return true;
			}
		};

		SpeechRecognizer firstRecognizer = new SpeechRecognizer() {
			@Override
			protected void runRecognitionTask(AudioInputStream inputStream) {
				while (true) {
					try {
						int lastByte = inputStream.read();
						if (lastByte == -1) {
							sendResults(null);
							break;
						}
					} catch (IOException e) {
						sendError(ERROR_IO, "Reading Error.");
						break;
					}
				}
			}

			@Override
			public boolean isAudioFormatSupported(AudioInputStream streamToCheck) {
				return true;
			}
		};
		SpeechRecognizer secondRecognizer = new SpeechRecognizer() {
			@Override
			protected void runRecognitionTask(AudioInputStream inputStream) {
				while (true) {
					try {
						int lastByte = inputStream.read();
						if (lastByte == -1) {
							sendResults(null);
							break;
						}
					} catch (IOException e) {
						sendError(ERROR_IO, "Reading Error.");
						break;
					}
				}
			}

			@Override
			public boolean isAudioFormatSupported(AudioInputStream streamToCheck) {
				return true;
			}
		};

		PipedOutputStream controlableStream = new PipedOutputStream();
		InputStream controlledInputStream = new PipedInputStream(controlableStream);
		AudioInputStream controlledAudioStream = new AudioInputStream(controlledInputStream,
				AudioFormat.ENCODING_PCM_16BIT, 1, 1000, 16, ByteOrder.LITTLE_ENDIAN, true);

		RecognitionManager manager = new RecognitionManager(controlledAudioStream);
		manager.addVoiceDetector(alwaysTrueDetection);
		manager.addSpeechRecognizer(firstRecognizer);
		manager.addSpeechRecognizer(secondRecognizer);
		manager.registerContinuousSpeechListener(this);

		manager.setParalellChunkProcessing(true);
		manager.start();

		controlableStream.write(new byte[512]);
		try {
			Thread.sleep(10L);
		} catch (InterruptedException e) {
		}

		assertTrue("First Recognizer wasn't started.", firstRecognizer.getRunningTaskCount() > 0);
		assertTrue("Second Recognizer wasn't started.", secondRecognizer.getRunningTaskCount() > 0);

		controlableStream.close();
		try {
			Thread.sleep(10L);
		} catch (InterruptedException e) {
		}

		assertTrue("First Recognizer didn't finish.", firstRecognizer.getRunningTaskCount() == 0);
		assertTrue("Second Recognizer didn't finish.", secondRecognizer.getRunningTaskCount() == 0);
		assertFalse("RecognitionManager is still running on closed Stream.", manager.isRecognitionRunning());
		assertTrue("An Error was thrown: " + lastErrorMessage, lastErrorMessage == "");
	}

	public void testOnlyFastestResult() throws IOException {

		final String firstResult = "left";
		final String secondResult = "right";

		VoiceDetection alwaysTrueDetection = new VoiceDetection() {

			@Override
			public void setSensibility(VoiceDetectionSensibility sensibility) {
			}

			@Override
			public void resetState() {
			}

			@Override
			public boolean isFrameWithVoice(double[] frame) {
				return true;
			}
		};

		SpeechRecognizer firstRecognizer = new SpeechRecognizer() {
			@Override
			protected void runRecognitionTask(AudioInputStream inputStream) {
				int targetByteCount = 256;
				ArrayList<String> matches = new ArrayList<String>();
				matches.add(firstResult);
				while (true) {
					try {
						int lastByte = inputStream.read();
						targetByteCount--;
						if (targetByteCount == 0) {
							sendResults(matches);
							break;
						}
						if (lastByte == -1) {
							sendResults(null);
							break;
						}
					} catch (IOException e) {
						sendError(ERROR_IO, "Reading Error.");
						break;
					}
				}
			}

			@Override
			public boolean isAudioFormatSupported(AudioInputStream streamToCheck) {
				return true;
			}
		};
		SpeechRecognizer secondRecognizer = new SpeechRecognizer() {
			@Override
			protected void runRecognitionTask(AudioInputStream inputStream) {
				int targetByteCount = 512;
				ArrayList<String> matches = new ArrayList<String>();
				matches.add(secondResult);
				while (true) {
					try {
						int lastByte = inputStream.read();
						targetByteCount--;
						if (targetByteCount == 0) {
							sendResults(matches);
							break;
						}
						if (lastByte == -1) {
							sendResults(null);
							break;
						}
					} catch (IOException e) {
						sendError(ERROR_IO, "Reading Error.");
						break;
					}
				}
			}

			@Override
			public boolean isAudioFormatSupported(AudioInputStream streamToCheck) {
				return true;
			}
		};

		PipedOutputStream controlableStream = new PipedOutputStream();
		InputStream controlledInputStream = new PipedInputStream(controlableStream);
		AudioInputStream controlledAudioStream = new AudioInputStream(controlledInputStream,
				AudioFormat.ENCODING_PCM_16BIT, 1, 1000, 16, ByteOrder.LITTLE_ENDIAN, true);

		RecognitionManager manager = new RecognitionManager(controlledAudioStream);
		manager.addVoiceDetector(alwaysTrueDetection);
		manager.addSpeechRecognizer(firstRecognizer);
		manager.addSpeechRecognizer(secondRecognizer);
		manager.registerContinuousSpeechListener(this);

		manager.setParalellChunkProcessing(true);
		manager.setProcessChunkOnlyTillFirstSuccessRecognizer(true);
		manager.start();

		controlableStream.write(new byte[1024]);
		try {
			Thread.sleep(100L);
		} catch (InterruptedException e) {
		}

		assertTrue("An Error was thrown: " + lastErrorMessage, lastErrorMessage == "");
		assertTrue("The first result wasn't delivered.", matchesContainString(firstResult));
		assertFalse("Multiple results sent.", matchesContainString(secondResult));

		controlableStream.close();
		try {
			Thread.sleep(100L);
		} catch (InterruptedException e) {
		}

		assertTrue("First Recognizer didn't finish.", firstRecognizer.getRunningTaskCount() == 0);
		assertTrue("Second Recognizer didn't finish.", secondRecognizer.getRunningTaskCount() == 0);
		assertFalse("RecognitionManager is still running on closed Stream.", manager.isRecognitionRunning());
		assertTrue("An Error was thrown: " + lastErrorMessage, lastErrorMessage == "");
	}

	public void testSerialRecognition() throws IOException {
		VoiceDetection alwaysTrueDetection = new VoiceDetection() {

			@Override
			public void setSensibility(VoiceDetectionSensibility sensibility) {
			}

			@Override
			public void resetState() {
			}

			@Override
			public boolean isFrameWithVoice(double[] frame) {
				return true;
			}
		};

		SpeechRecognizer firstRecognizer = new SpeechRecognizer() {
			@Override
			protected void runRecognitionTask(AudioInputStream inputStream) {
				while (true) {
					try {
						int lastByte = inputStream.read();
						if (lastByte == -1) {
							sendResults(null, false);
							break;
						}
					} catch (IOException e) {
						sendError(ERROR_IO, "Reading Error.");
						break;
					}
				}
			}

			@Override
			public boolean isAudioFormatSupported(AudioInputStream streamToCheck) {
				return true;
			}
		};

		final Object busyRecognizer = new Object();
		SpeechRecognizer secondRecognizer = new SpeechRecognizer() {
			@Override
			protected void runRecognitionTask(AudioInputStream inputStream) {
				Log.v("SebiTest", "Second recognizer started...");
				while (true) {
					try {
						int lastByte = inputStream.read();
						if (lastByte == -1) {
							synchronized (busyRecognizer) {
								busyRecognizer.wait();
								sendResults(null);
								break;
							}
						}
					} catch (IOException e) {
						sendError(ERROR_IO, "Reading Error.");
						break;
					} catch (InterruptedException e) {
						sendError(ERROR_IO, "Sleeping Error.");
						break;
					}
				}
			}

			@Override
			public boolean isAudioFormatSupported(AudioInputStream streamToCheck) {
				return true;
			}
		};

		PipedOutputStream controlableStream = new PipedOutputStream();
		InputStream controlledInputStream = new PipedInputStream(controlableStream);
		AudioInputStream controlledAudioStream = new AudioInputStream(controlledInputStream,
				AudioFormat.ENCODING_PCM_16BIT, 1, 1000, 16, ByteOrder.LITTLE_ENDIAN, true);

		RecognitionManager manager = new RecognitionManager(controlledAudioStream);
		manager.addVoiceDetector(alwaysTrueDetection);
		manager.addSpeechRecognizer(firstRecognizer);
		manager.addSpeechRecognizer(secondRecognizer);
		manager.registerContinuousSpeechListener(this);

		manager.setParalellChunkProcessing(false);
		manager.start();

		controlableStream.write(new byte[512]);
		try {
			Thread.sleep(10L);
		} catch (InterruptedException e) {
		}

		assertTrue("First Recognizer wasn't started.", firstRecognizer.getRunningTaskCount() > 0);
		assertTrue("Second Recognizer was already started.", secondRecognizer.getRunningTaskCount() == 0);

		controlableStream.close();
		try {
			Thread.sleep(10L);
		} catch (InterruptedException e) {
		}

		assertTrue("First Recognizer didn't finish.", firstRecognizer.getRunningTaskCount() == 0);
		assertTrue("Second Recognizer didn't start.", secondRecognizer.getRunningTaskCount() > 0);
		assertTrue("RecognitionManager is finished, but one Recognizer still works.", manager.isRecognitionRunning());

		synchronized (busyRecognizer) {
			busyRecognizer.notifyAll();
		}
		try {
			Thread.sleep(100L);
		} catch (InterruptedException e) {
		}
		assertTrue("Second Recognizer didn't finish.", secondRecognizer.getRunningTaskCount() == 0);
		assertFalse("RecognitionManager is still running, but all tasks are done.", manager.isRecognitionRunning());
		assertTrue("An Error was thrown: " + lastErrorMessage, lastErrorMessage == "");
	}

	public void testWrongUsage() {
		RecognitionManager manager = new RecognitionManager(null);
		try {
			manager.start();
			fail("Recognizer didn't test inputstream.");
		} catch (IllegalStateException e) {
		}

		InputStream zeroStream = new InputStream() {
			@Override
			public int read() throws IOException {
				return 0;
			}
		};
		AudioInputStream zeroAudioStream = new AudioInputStream(zeroStream, AudioFormat.ENCODING_PCM_16BIT, 1, 8000,
				256, ByteOrder.LITTLE_ENDIAN, true);

		manager = new RecognitionManager(zeroAudioStream);
		try {
			manager.addSpeechRecognizer(new SpeechRecognizer() {
				@Override
				protected void runRecognitionTask(AudioInputStream inputStream) {
				}

				@Override
				public boolean isAudioFormatSupported(AudioInputStream streamToCheck) {
					return false;
				}
			});
			fail("Manager didn't check compatibility of module and stream.");
		} catch (IllegalArgumentException e) {
			//expected
		}

		zeroAudioStream = new AudioInputStream(zeroStream, AudioFormat.ENCODING_PCM_16BIT, 1, 16000, 256,
				ByteOrder.LITTLE_ENDIAN, true);
		manager = new RecognitionManager(zeroAudioStream);
		try {
			manager.start();
			fail("Manager didn't test if somebody even listens.");
		} catch (IllegalStateException e) {
			//expected
		}
		manager.registerContinuousSpeechListener(this);
		try {
			manager.start();
		} catch (IllegalStateException e) {
			fail("Recognizer couldn't start." + e.getMessage());
		}

		assertTrue("Recognizer isn't running after start.", manager.isRecognitionRunning());
		try {
			manager.addVoiceDetector(new ZeroCrossingVoiceDetection());
			fail("Recognizer added detecor during runtime (not supported yet).");
		} catch (IllegalStateException e) {
			//expected
		}

		try {
			manager.addSpeechRecognizer(new GoogleOnlineSpeechRecognizer());
			fail("Recognizer added detecor during runtime (not supported yet).");
		} catch (IllegalStateException e) {
			//expected
		}

		manager.unregisterContinuousSpeechListener(this);
		assertTrue("Recognizer isn't running after unregister, should just pause recognizing.",
				manager.isRecognitionRunning());

		manager.stop();
		assertTrue("Recognizer still running after stop.", !manager.isRecognitionRunning());
	}

	public void testFaultyRecognizer() throws IOException {

		PipedOutputStream controlableStream = new PipedOutputStream();
		InputStream zeroStream = new PipedInputStream(controlableStream);
		AudioInputStream zeroAudioStream = new AudioInputStream(zeroStream, AudioFormat.ENCODING_PCM_16BIT, 1, 16000,
				256, ByteOrder.LITTLE_ENDIAN, true);

		VoiceDetection alwaysTrueDetection = new VoiceDetection() {
			@Override
			public void setSensibility(VoiceDetectionSensibility sensibility) {
			}

			@Override
			public void resetState() {
			}

			@Override
			public boolean isFrameWithVoice(double[] frame) {
				return true;
			}
		};

		SpeechRecognizer faultyRecognizer = new SpeechRecognizer() {
			@Override
			protected void runRecognitionTask(AudioInputStream inputStream) {
				sendError(ERROR_API_CHANGED, "my API was bad since birth");
			}

			@Override
			public boolean isAudioFormatSupported(AudioInputStream streamToCheck) {
				return true;
			}
		};

		SpeechRecognizer braveRecognizer = new SpeechRecognizer() {
			@Override
			protected void runRecognitionTask(AudioInputStream inputStream) {
				while (true) {
					try {
						if (inputStream.read() < 0) {
							break;
						}
					} catch (IOException e) {
						break;
					}
				}
				sendResults(null);
			}

			@Override
			public boolean isAudioFormatSupported(AudioInputStream streamToCheck) {
				return true;
			}
		};

		RecognitionManager manager = new RecognitionManager(zeroAudioStream);
		manager.addSpeechRecognizer(faultyRecognizer);
		manager.addSpeechRecognizer(braveRecognizer);
		manager.addVoiceDetector(alwaysTrueDetection);
		manager.setRecorderMinVoiceChunkTime(0);
		manager.setRecorderPreSilenceChunkTime(0);
		manager.registerContinuousSpeechListener(this);

		manager.start();

		controlableStream.write(new byte[1024]);
		try {
			Thread.sleep(100L);
		} catch (InterruptedException e) {
		}

		assertNotNull("faulty recognizer should have thrown an error", lastErrorBundle);
		assertEquals("Recognizer forwarded wrong errorcode", ERROR_API_CHANGED,
				lastErrorBundle.getInt(BUNDLE_ERROR_CODE));
		assertFalse("Recognizer should still be working", lastErrorBundle.getBoolean(BUNDLE_ERROR_FATAL_FLAG));
		assertTrue("Recognizer died with a module, but others are still remaining", manager.isRecognitionRunning());

		controlableStream.flush();
		controlableStream.close();
		controlableStream.flush();

		try {
			Thread.sleep(200L);
		} catch (InterruptedException e) {
		}

		assertFalse("Recognizer running after closed Stream", manager.isRecognitionRunning());
	}

	private boolean matchesContainString(String search) {
		for (String match : lastMatches) {
			if (match.contains(search)) {
				return true;
			}
		}
		return false;
	}

	public void onRecognizerResult(int resultCode, Bundle resultBundle) {
		if (resultCode == RESULT_OK) {
			lastMatches.addAll(resultBundle.getStringArrayList(BUNDLE_RESULT_MATCHES));
		}
	}

	public void onRecognizerError(Bundle errorBundle) {
		lastErrorMessage = errorBundle.getString(BUNDLE_ERROR_MESSAGE);
		lastErrorBundle = errorBundle;
	}
}
