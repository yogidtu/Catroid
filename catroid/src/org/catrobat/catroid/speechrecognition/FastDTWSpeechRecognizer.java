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

import org.catrobat.catroid.speechrecognition.signalprocessing.FFT;
import org.catrobat.catroid.speechrecognition.signalprocessing.MelFrequencyFilterBank;

import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;

import com.dtw.DTW;
import com.dtw.TimeWarpInfo;
import com.timeseries.TimeSeries;
import com.util.DistanceFunction;
import com.util.DistanceFunctionFactory;

public class FastDTWSpeechRecognizer extends SpeechRecognizer implements RecognizerCallback {

	private static final String TAG = FastDTWSpeechRecognizer.class.getSimpleName();
	private SparseArray<TimeSeries> unassociatedFingerprints = new SparseArray<TimeSeries>();
	private ArrayList<Cluster> clusterList = new ArrayList<Cluster>();
	//	private HashMap<ArrayList<String>, ArrayList<Integer>> matchCluster = new HashMap<ArrayList<String>, ArrayList<Integer>>();
	private String workingDirectory = "";
	private boolean fixedClusters = false;

	private int targetTimePerFrame = 32;
	private int overlappingFrameTime = 0;
	private int filters = 32;
	private double globalThreshold = 15.0;
	private double minClusterDistance = 3.0;

	private int samplesPerFrame;
	private int samplesPerOverlap;
	private FFT preCalculatedFFT;

	//	public void setSavingDirectory(String dirPath) {
	//		workingDirectory = dirPath;
	//	}

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
			Log.v(TAG, "Samples per Frame: " + samplesPerFrame);
			preCalculatedFFT = new FFT(samplesPerFrame);
		}
		return true;
	}

	@Override
	protected void runRecognitionTask(AudioInputStream inputStream) {

		int identifier = getMyIdentifier();
		double[][] currentLMD = new double[50][];
		byte[] frameRawBuffer = new byte[samplesPerFrame * (inputStream.getSampleSizeInBits() / 8)];
		double[] imageArray = new double[samplesPerFrame];
		byte[] overlapBuffer = new byte[samplesPerOverlap * (inputStream.getSampleSizeInBits() / 8)];

		int frameNumber = 0;
		try {
			while (inputStream.read(frameRawBuffer, overlapBuffer.length, frameRawBuffer.length - overlapBuffer.length) > 0) {
				System.arraycopy(overlapBuffer, 0, frameRawBuffer, 0, overlapBuffer.length);
				//Log.v(TAG, "Start converting to features");
				double[] frameBuffer = audioByteToDouble(frameRawBuffer, inputStream.getSampleSizeInBits() / 8);
				preCalculatedFFT.fft(frameBuffer, imageArray);
				frameBuffer = preCalculatedFFT.getMagnitude(frameBuffer, imageArray);
				MelFrequencyFilterBank filterBank = new MelFrequencyFilterBank(130, inputStream.getSampleRate(),
						filters);
				//				PLPFrequencyFilterBank filterBank = new PLPFrequencyFilterBank(130, inputStream.getSampleRate() / 2,
				//						filters);
				frameBuffer = filterBank.process(frameBuffer, inputStream.getSampleRate());
				if (frameNumber >= currentLMD.length) {
					double[][] growingBuffer = new double[currentLMD.length * 2][];
					System.arraycopy(currentLMD, 0, growingBuffer, 0, currentLMD.length);
					currentLMD = growingBuffer;
				}
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
		final DistanceFunction distanceFunktion = DistanceFunctionFactory.getDistFnByName("EuclideanDistance");
		final TimeSeries timeSerieInput = new TimeSeries(currentLMD);

		double[] nearestDistances = new double[] { Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY };
		Cluster successCluster = null;
		ArrayList<String> matches = null;
		ArrayList<Cluster> clusterCopyList = new ArrayList<Cluster>(clusterList);
		for (Cluster cluster : clusterCopyList) {
			double clusterMinDistance = Double.POSITIVE_INFINITY;
			Log.v(TAG, "cluster --------------------" + cluster.getClusterLabels().toString());
			for (TimeSeries timeSerieReference : cluster.getClusterFiles()) {

				TimeWarpInfo twi = DTW.getWarpInfoBetween(timeSerieInput, timeSerieReference, distanceFunktion);
				//				Log.v(TAG, "Warp path size: " + twi.getPath().size());
				//Double result = DTW.getWarpDistBetween(timeSerieInput, timeSerieReference, distanceFunktion);
				//				result = result / ((Math.max(currentLMD.length, timeSerieReference.numOfPts())) * targetTimePerFrame);
				Double result = twi.getDistance();
				result = result
						/ (currentLMD.length * timeSerieReference.numOfPts() * 1000 * targetTimePerFrame * filters);
				int compareableDistance = result.intValue();
				Log.v(TAG, "We get distance of " + compareableDistance);
				if (clusterMinDistance > compareableDistance) {
					clusterMinDistance = compareableDistance;
				}
				if (clusterMinDistance < nearestDistances[0]) {
					nearestDistances[0] = clusterMinDistance;
					matches = cluster.getClusterLabels();
					successCluster = cluster;
				} else if (clusterMinDistance < nearestDistances[1]) {
					nearestDistances[1] = clusterMinDistance;
				}
			}
		}

		if (nearestDistances[1] - nearestDistances[0] >= minClusterDistance && nearestDistances[0] <= globalThreshold) {
			Log.w(TAG, "Sending results");
			successCluster.addFingerprint(timeSerieInput);
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

	//	@Override
	//	public void prepare() throws IllegalStateException {
	//		if (workingDirectory == "") {
	//			throw new IllegalStateException();
	//		}
	//		super.prepare();
	//	}

	@Override
	public void onRecognizerError(Bundle errorBundle) {
	}

	private static double[] audioByteToDouble(byte[] samples, int bytesPerSample) {

		double[] micBufferData = new double[samples.length / bytesPerSample];

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
		return micBufferData;
	}
}
