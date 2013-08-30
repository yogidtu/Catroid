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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import org.catrobat.catroid.speechrecognition.signalprocessing.FFT;
import org.catrobat.catroid.speechrecognition.signalprocessing.PLPFrequencyFilterBank;

import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;

import com.dtw.DTW;
import com.timeseries.TimeSeries;
import com.util.DistanceFunction;
import com.util.DistanceFunctionFactory;

public class FastDTWSpeechRecognizer extends SpeechRecognizer implements RecognizerCallback {

	private static final String TAG = FastDTWSpeechRecognizer.class.getSimpleName();
	private SparseArray<String> processedFilePaths = new SparseArray<String>();
	private HashMap<ArrayList<String>, ArrayList<Integer>> matchCluster = new HashMap<ArrayList<String>, ArrayList<Integer>>();
	private String workingDirectory = "";

	private int maxClusterSize = 5;
	private int maxClusterMatches = 5;
	private int targetTimePerFrame = 256;

	private double globalThreshold = 2000.0;
	private double minClusterDistance = 1000.0;

	private int samplesPerFrame;
	private FFT preCalculatedFFT;

	public void setSavingDirectory(String dirPath) {
		workingDirectory = dirPath;
	}

	@Override
	public boolean isAudioFormatSupported(AudioInputStream streamToCheck) {
		int tmpSamplesPerFrame = (int) (streamToCheck.getSampleRate() / 1000f * targetTimePerFrame);
		if (tmpSamplesPerFrame != samplesPerFrame) {
			samplesPerFrame = tmpSamplesPerFrame;
			Log.v(TAG, "Samples per Frame: " + samplesPerFrame);
			preCalculatedFFT = new FFT(samplesPerFrame);
		}
		return true;
	}

	@Override
	protected void runRecognitionTask(AudioInputStream inputStream) {

		int identifier = getMyIdentifier();
		int samplesPerFrame = (int) (inputStream.getSampleRate() / 1000f * targetTimePerFrame);
		double[][] currentLMD = new double[50][];
		byte[] frameRawBuffer = new byte[samplesPerFrame * (inputStream.getSampleSizeInBits() / 8)];
		double[] imageArray = new double[samplesPerFrame];

		int frameNumber = 0;
		try {
			while (inputStream.read(frameRawBuffer, 0, frameRawBuffer.length) > 0) {
				//Log.v(TAG, "Start converting to features");
				double[] frameBuffer = audioByteToDouble(frameRawBuffer, inputStream.getSampleSizeInBits() / 8);
				preCalculatedFFT.fft(frameBuffer, imageArray);
				frameBuffer = preCalculatedFFT.getMagnitude(frameBuffer, imageArray);
				//								MelFrequencyFilterBank filterBank = new MelFrequencyFilterBank(130, inputStream.getSampleRate() / 2, 32);
				PLPFrequencyFilterBank filterBank = new PLPFrequencyFilterBank(130, inputStream.getSampleRate() / 2, 32);
				frameBuffer = filterBank.processFrame(frameBuffer, inputStream.getSampleRate());
				if (frameNumber >= currentLMD.length) {
					double[][] growingBuffer = new double[currentLMD.length * 2][];
					System.arraycopy(currentLMD, 0, growingBuffer, 0, currentLMD.length);
					currentLMD = growingBuffer;
				}
				currentLMD[frameNumber] = frameBuffer;
				frameNumber++;
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

		double[] nearestDistances = new double[] { 3 * globalThreshold, 3 * globalThreshold };
		ArrayList<Integer> successCluster = null;
		ArrayList<String> matches = null;
		synchronized (matchCluster) {
			Iterator<Entry<ArrayList<String>, ArrayList<Integer>>> it = matchCluster.entrySet().iterator();
			while (it.hasNext()) {
				Entry<ArrayList<String>, ArrayList<Integer>> cluster = it.next();
				Log.v(TAG, "cluster --------------------" + cluster.getKey().toString());
				double clusterMinDistance = 1000d;
				for (int featureFile : cluster.getValue()) {
					final TimeSeries timeSerieReference = new TimeSeries(processedFilePaths.get(featureFile), false,
							false, ',');
					Double result = DTW.getWarpDistBetween(timeSerieInput, timeSerieReference, distanceFunktion);
					//				result = result / ((Math.max(currentLMD.length, timeSerieReference.numOfPts())) * targetTimePerFrame);
					result = result / (currentLMD.length * targetTimePerFrame);
					int compareableDistance = result.intValue();
					Log.v(TAG, "We get distance for " + featureFile + "FILE of " + compareableDistance + " (id:"
							+ identifier + ")");
					if (clusterMinDistance > compareableDistance) {
						clusterMinDistance = compareableDistance;
					}
				}
				if (clusterMinDistance < nearestDistances[0]) {
					nearestDistances[0] = clusterMinDistance;
					matches = cluster.getKey();
					successCluster = cluster.getValue();
				} else if (clusterMinDistance < nearestDistances[1]) {
					nearestDistances[1] = clusterMinDistance;
				}
			}
		}

		if (nearestDistances[1] - nearestDistances[0] >= minClusterDistance && nearestDistances[0] <= globalThreshold) {
			//			Log.v(TAG, "Result " + identifier);
			sendResults(matches, Thread.currentThread(), true);
			if (successCluster.size() < maxClusterSize) {
				successCluster.add(identifier);
			}
		} else {
			//			Log.v(TAG, "Has no result for " + identifier);
			sendResults(new ArrayList<String>());
		}

		Log.v(TAG, "Ended process in " + (System.currentTimeMillis() - DTWTime) + "ms");

		String newFile = workingDirectory + "/" + System.currentTimeMillis() + ".fastdtw";
		try {
			timeSerieInput.save(new File(newFile));
		} catch (IOException e) {
			e.printStackTrace();
		}
		processedFilePaths.put(identifier, newFile);
		Log.v(TAG, "Saved file " + newFile);

	}

	@Override
	public void onRecognizerResult(int resultCode, Bundle resultBundle) {
		if (resultBundle.getString(BUNDLE_SENDERCLASS).contains(FastDTWSpeechRecognizer.class.getSimpleName())) {
			//			Log.v(TAG, "Discarding.");
			return;
		}
		if (resultBundle.getBoolean(BUNDLE_RESULT_RECOGNIZED)) {
			int identifier = resultBundle.getInt(BUNDLE_IDENTIFIER);
			//			cachedMatches.put(identifier, resultBundle.getStringArrayList(BUNDLE_RESULT_MATCHES));
			ArrayList<String> resultMatches = resultBundle.getStringArrayList(BUNDLE_RESULT_MATCHES);
			boolean emptySearch = false;
			if (resultMatches == null) {
				resultMatches = new ArrayList<String>();
				emptySearch = true;
			}

			boolean added = false;
			synchronized (matchCluster) {
				Iterator<Entry<ArrayList<String>, ArrayList<Integer>>> it = matchCluster.entrySet().iterator();
				while (it.hasNext()) {
					Entry<ArrayList<String>, ArrayList<Integer>> cluster = it.next();
					ArrayList<String> cachedMatches = cluster.getKey();
					if (emptySearch && cachedMatches.size() == 0) {
						if (cluster.getValue().size() >= maxClusterSize) {
							int oldestClusterIndex = cluster.getValue().get(0);
							File tooMuch = new File(processedFilePaths.get(oldestClusterIndex));
							tooMuch.delete();
							cluster.getValue().remove(0);
							cluster.getValue().add(identifier);
							Log.v(TAG, "Deleted specific file, enough for this cluster.");
						} else {
							Log.v(TAG, "Added result to cluster.");
							cluster.getValue().add(identifier);
						}
						added = true;
						break;
					}
					for (String itemMatch : cachedMatches) {
						if (resultMatches.contains(itemMatch) && cachedMatches.indexOf(itemMatch) <= 2) {
							if (cluster.getValue().size() >= maxClusterSize) {
								File tooMuch = new File(processedFilePaths.get(cluster.getValue().get(0)));
								tooMuch.delete();
								cluster.getValue().remove(0);
								cluster.getValue().add(identifier);
								Log.v(TAG, "Deleted specific file, enough for this cluster.");
							} else {
								Log.v(TAG, "Added result to cluster.");
								cluster.getValue().add(identifier);
							}
							mergeClusterMatches(cachedMatches, resultMatches, itemMatch);

							added = true;
							break;
						}
					}
					if (added) {
						break;
					}
				}
			}
			if (!added) {
				ArrayList<Integer> matchIdentifiers = new ArrayList<Integer>();
				matchIdentifiers.add(identifier);
				matchCluster.put(resultMatches, matchIdentifiers);
				Log.v(TAG, "New Cluster added.");
			}
		} else {
			Log.v(TAG, "Result seems not to be valid.");
		}
	}

	private void mergeClusterMatches(ArrayList<String> clusterMatches, ArrayList<String> recievedMatches,
			String duplicateItem) {
		int duplicateIndex = clusterMatches.indexOf(duplicateItem);
		ArrayList<String> copyClusterMatches = new ArrayList<String>(clusterMatches);
		clusterMatches.clear();
		clusterMatches.add(duplicateItem);
		for (int i = 0; i < maxClusterMatches / 2; i++) {
			clusterMatches.add(copyClusterMatches.get(i));
			if (recievedMatches.size() > i) {
				clusterMatches.add(recievedMatches.get(i));
			}
		}

	}

	@Override
	public void prepare() throws IllegalStateException {
		if (workingDirectory == "") {
			throw new IllegalStateException();
		}
		super.prepare();
	}

	@Override
	public void onRecognizerError(Bundle errorBundle) {
		// TODO Auto-generated method stub

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
