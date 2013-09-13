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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map.Entry;

import org.catrobat.catroid.speechrecognition.signalprocessing.MelFrequencyFilterBank;

import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;

import com.dtw.DTW;
import com.timeseries.TimeSeries;
import com.util.DistanceFunction;
import com.util.EuclideanDistance;

import edu.emory.mathcs.jtransforms.fft.DoubleFFT_1D;

public class FastDTWSpeechRecognizer extends SpeechRecognizer implements RecognizerCallback {

	private static final String TAG = FastDTWSpeechRecognizer.class.getSimpleName();
	private SparseArray<TimeSeries> unassociatedFingerprints = new SparseArray<TimeSeries>();
	private ArrayList<Cluster> clusterList = new ArrayList<Cluster>();
	private boolean fixedClusters = false;

	private int targetTimePerFrame = 64;
	private int overlappingFrameTime = targetTimePerFrame / 2;
	private int filters = 12;
	//	private double globalThreshold = 8000.0;
	//	private double minClusterDistance = globalThreshold / 2;

	private int samplesPerFrame;
	private int samplesPerOverlap;
	private DoubleFFT_1D fft;

	public void setFixedClusterLabels(ArrayList<String> labels) {
		clusterList.clear();
		for (String label : labels) {
			Cluster fixedCluster = new Cluster();
			ArrayList<String> clusterLabel = new ArrayList<String>();
			clusterLabel.add(label);
			fixedCluster.setLabel(clusterLabel);
			clusterList.add(fixedCluster);
		}
		Cluster emptyCluster = new Cluster();
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

		int frameNumber = 0;
		try {
			while (inputStream.read(frameRawBuffer, frameNumber > 0 ? overlapBuffer.length : 0,
					frameNumber > 0 ? (frameRawBuffer.length - overlapBuffer.length) : frameRawBuffer.length) > 0) {
				if (frameNumber > 0) {
					System.arraycopy(overlapBuffer, 0, frameRawBuffer, 0, overlapBuffer.length);
				}
				double[] frameBuffer = audioByteToDouble(frameRawBuffer, inputStream.getSampleSizeInBits() / 8);
				double frameEnergy = calculateEnergyOfFrame(frameBuffer);
				frameBuffer = hammingWindow(frameBuffer, 0, frameBuffer.length);

				double[] powerSpectrum = new double[frameBuffer.length / 2];
				fftMagnitude(frameBuffer, powerSpectrum);
				MelFrequencyFilterBank filterBank = new MelFrequencyFilterBank(130, 6800, filters);
				frameBuffer = filterBank.process(powerSpectrum, inputStream.getSampleRate(), 1);

				if (frameNumber >= currentLMD.length) {
					double[][] growingBuffer = new double[currentLMD.length * 2][];
					System.arraycopy(currentLMD, 0, growingBuffer, 0, currentLMD.length);
					currentLMD = growingBuffer;
				}
				frameBuffer[frameBuffer.length - 1] = frameEnergy;
				currentLMD[frameNumber] = frameBuffer;
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

		long DTWTime = System.currentTimeMillis();
		final DistanceFunction distanceFunktion = new EuclideanDistance();
		final TimeSeries timeSerieInput = new TimeSeries(currentLMD);

		Cluster successCluster = null;
		ArrayList<String> matches = null;
		ArrayList<Cluster> clusterCopyList = new ArrayList<Cluster>(clusterList);
		for (Cluster cluster : clusterCopyList) {
			int hitCounter = 0;
			Log.v(TAG, "cluster --------------------" + cluster.getClusterLabels().toString());
			Iterator<Entry<TimeSeries, Double>> it = cluster.getClusterFiles().entrySet().iterator();
			int clusterNumber = 0;
			int clusterSize = cluster.getClusterFiles().size();
			while (it.hasNext()) {
				clusterNumber++;
				Entry<TimeSeries, Double> template = it.next();
				Double compareableDistance = DTW
						.getWarpDistBetween(timeSerieInput, template.getKey(), distanceFunktion);
				Log.v(TAG, "Compareable:" + compareableDistance);
				Log.v(TAG, "Threshold:" + template.getValue());

				if (compareableDistance < template.getValue()) {
					Log.v(TAG, "We have a hit!");
					hitCounter++;
				}
				if (clusterNumber > clusterSize / 2 && hitCounter == 0) {
					break;
				}

			}
			if (hitCounter >= clusterSize / 2 && clusterSize >= 2) {
				Log.v(TAG, "HITHITHITHIT");
				if (successCluster != null) {
					//multi-hit
					successCluster = null;
					break;
				}
				matches = cluster.getClusterLabels();
				successCluster = cluster;
			}
		}

		if (successCluster != null) {
			Log.w(TAG, "Sending results");
			sendResults(matches);
		} else {
			unassociatedFingerprints.put(identifier, timeSerieInput);
			sendResults(new ArrayList<String>(), Thread.currentThread(), false);
		}

		Log.v(TAG, "Ended process in " + (System.currentTimeMillis() - DTWTime) + "ms");
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
			for (Cluster cluster : clusterList) {
				ArrayList<String> cachedMatches = cluster.getClusterLabels();
				if (emptySearch && cachedMatches.size() == 0) {
					cluster.addFingerprint(unassociatedFingerprints.get(identifier));
					unassociatedFingerprints.remove(identifier);
					Log.v(TAG, "Results added.");
					added = true;
					break;
				}
				if (cluster.belongsToCluster(resultMatches)) {
					cluster.addFingerprint(unassociatedFingerprints.get(identifier));
					unassociatedFingerprints.remove(identifier);
					Log.v(TAG, "Results added.");
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
				Cluster additionalCluster = new Cluster();
				additionalCluster.addFingerprint(unassociatedFingerprints.get(identifier));
				additionalCluster.setLabel(resultMatches);
				unassociatedFingerprints.remove(identifier);

				clusterList.add(additionalCluster);
				Log.v(TAG, "New Cluster added.");
			}
		} else {
			Log.v(TAG, "Result seems not to be valid.");
		}
	}

	@Override
	public void onRecognizerError(Bundle errorBundle) {
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

	private double[] hammingWindow(double[] signal_in, int pos, int size) {
		for (int i = pos; i < pos + size; i++) {
			int j = i - pos;
			signal_in[i] = signal_in[i] * 0.46 * (1.0 - Math.cos(2.0 * Math.PI * j / size));
		}
		return signal_in;
	}

	private double calculateEnergyOfFrame(double[] frame) {
		double sum = 0.0d;
		for (double sample : frame) {
			sum += (sample * sample);
		}

		return sum / frame.length;
	}

	public void fftMagnitude(double[] x, double[] ac) {
		int n = x.length;
		// Assumes n is even.

		fft = new DoubleFFT_1D(n);
		fft.realForward(x);
		ac[0] = x[0];
		for (int i = 1; i < n / 2 - 1; i++) {
			ac[i] = x[2 * i] * x[2 * i] + x[2 * i + 1] * x[2 * i + 1];
		}
	}
}
