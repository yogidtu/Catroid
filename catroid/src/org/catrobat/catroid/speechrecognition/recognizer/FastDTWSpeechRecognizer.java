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
package org.catrobat.catroid.speechrecognition.recognizer;

import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;

import com.dtw.DTW;
import com.timeseries.TimeSeries;
import com.util.DistanceFunction;
import com.util.EuclideanDistance;

import edu.emory.mathcs.jtransforms.fft.DoubleFFT_1D;

import org.catrobat.catroid.speechrecognition.AudioInputStream;
import org.catrobat.catroid.speechrecognition.RecognizerCallback;
import org.catrobat.catroid.speechrecognition.SpeechRecognizer;
import org.catrobat.catroid.speechrecognition.signalprocessing.MelFrequencyFilterBank;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map.Entry;

public class FastDTWSpeechRecognizer extends SpeechRecognizer implements RecognizerCallback {

	private static final String TAG = FastDTWSpeechRecognizer.class.getSimpleName();
	private SparseArray<TimeSeries> unassociatedFingerprints = new SparseArray<TimeSeries>();
	private ArrayList<LocalTemplateCluster> clusterList = new ArrayList<LocalTemplateCluster>();
	private boolean fixedClusters = false;

	private int targetTimePerFrame = 64;
	private int overlappingFrameTime = targetTimePerFrame / 2;
	private int filters = 12;

	private int samplesPerFrame;
	private int samplesPerOverlap;
	private DoubleFFT_1D fft;

	public void setFixedClusterLabels(ArrayList<String> labels) {
		clusterList.clear();
		for (String label : labels) {
			LocalTemplateCluster fixedCluster = new LocalTemplateCluster();
			ArrayList<String> clusterLabel = new ArrayList<String>();
			clusterLabel.add(label);
			fixedCluster.setLabel(clusterLabel);
			clusterList.add(fixedCluster);
		}
		LocalTemplateCluster emptyCluster = new LocalTemplateCluster();
		emptyCluster.setLabel(new ArrayList<String>());
		clusterList.add(emptyCluster);
		fixedClusters = true;
	}

	@Override
	public boolean isAudioFormatSupported(AudioInputStream streamToCheck) {
		int tmpSamplesPerFrame = (int) (streamToCheck.getSampleRate() / 1000f * targetTimePerFrame);
		if (tmpSamplesPerFrame != samplesPerFrame) {
			samplesPerFrame = tmpSamplesPerFrame;
			samplesPerOverlap = (int) (streamToCheck.getSampleRate() / 1000f * overlappingFrameTime);
		}
		return true;
	}

	@Override
	protected void runRecognitionTask(AudioInputStream inputStream) {

		int identifier = getMyIdentifier();
		double[][] currentLMD = new double[50][];
		byte[] frameRawBuffer = new byte[samplesPerFrame * (inputStream.getSampleSizeInBits() / 8)];
		byte[] overlapBuffer = new byte[samplesPerOverlap * (inputStream.getSampleSizeInBits() / 8)];
		double[] frameBuffer = new double[samplesPerFrame];
		double[] powerSpectrum = new double[frameBuffer.length / 2];

		int frameNumber = 0;
		try {
			while (inputStream.read(frameRawBuffer, frameNumber > 0 ? overlapBuffer.length : 0,
					frameNumber > 0 ? (frameRawBuffer.length - overlapBuffer.length) : frameRawBuffer.length) > 0) {
				if (frameNumber > 0) {
					System.arraycopy(overlapBuffer, 0, frameRawBuffer, 0, overlapBuffer.length);
				}
				audioByteToDouble(frameRawBuffer, inputStream.getSampleSizeInBits() / 8, frameBuffer);
				strenghtHighFrequencys(frameBuffer);
				double frameEnergy = calculateEnergyOfFrame(frameBuffer);
				frameBuffer = hammingWindow(frameBuffer, 0, frameBuffer.length);

				fftMagnitude(frameBuffer, powerSpectrum);
				MelFrequencyFilterBank filterBank = new MelFrequencyFilterBank(130, 6800, filters);
				double[] melFrequences = filterBank.process(powerSpectrum, inputStream.getSampleRate(), 1);

				if (frameNumber >= currentLMD.length) {
					double[][] growingBuffer = new double[currentLMD.length * 2][];
					System.arraycopy(currentLMD, 0, growingBuffer, 0, currentLMD.length);
					currentLMD = growingBuffer;
				}
				melFrequences[melFrequences.length - 1] = frameEnergy;
				currentLMD[frameNumber] = melFrequences;
				frameNumber++;
				System.arraycopy(frameRawBuffer, frameRawBuffer.length - overlapBuffer.length, overlapBuffer, 0,
						overlapBuffer.length);
			}
			double[][] buffer = new double[frameNumber][];
			System.arraycopy(currentLMD, 0, buffer, 0, frameNumber);
			currentLMD = buffer;
		} catch (IOException e) {
			e.printStackTrace();
		}

		final DistanceFunction distanceFunktion = new EuclideanDistance();
		final TimeSeries timeSerieInput = new TimeSeries(currentLMD);

		LocalTemplateCluster successCluster = null;
		ArrayList<String> matches = null;
		ArrayList<LocalTemplateCluster> clusterCopyList = new ArrayList<LocalTemplateCluster>(clusterList);
		for (LocalTemplateCluster cluster : clusterCopyList) {
			int hitCounter = 0;
			if (DEBUG_OUTPUT) {
				Log.v(TAG, "cluster --------------------" + cluster.getClusterLabels().toString());
			}
			Iterator<Entry<TimeSeries, Double>> it = cluster.getClusterFiles().entrySet().iterator();
			int clusterNumber = 0;
			int clusterSize = cluster.getClusterFiles().size();
			while (it.hasNext()) {
				clusterNumber++;
				Entry<TimeSeries, Double> template = it.next();
				Double compareableDistance = DTW
						.getWarpDistBetween(timeSerieInput, template.getKey(), distanceFunktion);
				if (DEBUG_OUTPUT) {
					Log.v(TAG, "Compareable:" + compareableDistance);
					Log.v(TAG, "Threshold:" + template.getValue());
				}

				if (compareableDistance < template.getValue()) {
					if (DEBUG_OUTPUT) {
						Log.v(TAG, "We have a hit!");
					}
					hitCounter++;
				}
				if (clusterNumber > clusterSize / 2 && hitCounter == 0) {
					break;
				}

			}
			if (hitCounter > clusterSize / 2 && clusterSize >= 2) {
				if (DEBUG_OUTPUT) {
					Log.v(TAG, "HITHITHITHIT");
				}
				if (successCluster != null) {
					successCluster = null;
					break;
				}
				matches = cluster.getClusterLabels();
				successCluster = cluster;
			}
		}

		if (successCluster != null) {
			sendResults(matches);
		} else {
			unassociatedFingerprints.put(identifier, timeSerieInput);
			sendResults(new ArrayList<String>(), Thread.currentThread(), false);
		}
	}

	@Override
	public void onRecognizerResult(int resultCode, Bundle resultBundle) {
		if (resultBundle.getString(BUNDLE_SENDERCLASS).contains(FastDTWSpeechRecognizer.class.getSimpleName())) {
			return;
		}
		if (resultBundle.getBoolean(BUNDLE_RESULT_RECOGNIZED)) {
			int identifier = resultBundle.getInt(BUNDLE_IDENTIFIER);
			if (unassociatedFingerprints.get(identifier) == null) {
				return;
			}
			ArrayList<String> resultMatches = resultBundle.getStringArrayList(BUNDLE_RESULT_MATCHES);
			boolean emptySearch = false;
			if (resultMatches == null) {
				resultMatches = new ArrayList<String>();
			}
			if (resultMatches.size() == 0) {
				emptySearch = true;
			}

			boolean added = false;
			for (LocalTemplateCluster cluster : clusterList) {
				ArrayList<String> cachedMatches = cluster.getClusterLabels();
				if (emptySearch && cachedMatches.size() == 0) {
					cluster.addFingerprint(unassociatedFingerprints.get(identifier));
					unassociatedFingerprints.remove(identifier);
					added = true;
					break;
				}
				if (cluster.belongsToCluster(resultMatches)) {
					cluster.addFingerprint(unassociatedFingerprints.get(identifier));
					unassociatedFingerprints.remove(identifier);
					if (!fixedClusters) {
						cluster.mergeResults(resultMatches);
					}
					added = true;
					break;
				}
				if (added) {
					break;
				}
			}
			if (!added && !fixedClusters) {
				LocalTemplateCluster additionalCluster = new LocalTemplateCluster();
				additionalCluster.addFingerprint(unassociatedFingerprints.get(identifier));
				additionalCluster.setLabel(resultMatches);
				unassociatedFingerprints.remove(identifier);

				clusterList.add(additionalCluster);
			}
		}
	}

	@Override
	public void onRecognizerError(Bundle errorBundle) {
	}

	private static void audioByteToDouble(byte[] samples, int bytesPerSample, double[] micBufferData) {

		final double amplification = 100.0;
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
		return;
	}

	private double[] hammingWindow(double[] signalIn, int pos, int size) {
		for (int i = pos; i < pos + size; i++) {
			int j = i - pos;
			signalIn[i] = signalIn[i] * 0.46 * (1.0 - Math.cos(2.0 * Math.PI * j / size));
		}
		return signalIn;
	}

	private double calculateEnergyOfFrame(double[] frame) {
		double sum = 0.0d;
		for (double sample : frame) {
			sum += (sample * sample);
		}

		return sum / frame.length;
	}

	private void fftMagnitude(double[] x, double[] ac) {
		int n = x.length;
		// Assumes n is even.

		fft = new DoubleFFT_1D(n);
		fft.realForward(x);
		ac[0] = x[0];
		for (int i = 1; i < n / 2 - 1; i++) {
			ac[i] = x[2 * i] * x[2 * i] + x[2 * i + 1] * x[2 * i + 1];
		}
	}

	private void strenghtHighFrequencys(double[] signal) {
		float alpha = 0.95f;
		for (int i = 1; i < signal.length; i++) {
			signal[i] = signal[i] - alpha * signal[i - 1];
		}
	}
}
