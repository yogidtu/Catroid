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

import java.io.IOException;
import java.util.ArrayList;

import org.catrobat.catroid.speechrecognition.AudioInputStream;
import org.catrobat.catroid.speechrecognition.RecognizerCallback;
import org.catrobat.catroid.speechrecognition.SpeechRecognizer;

import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;

import com.musicg.fingerprint.FingerprintManager;
import com.musicg.fingerprint.FingerprintSimilarity;
import com.musicg.fingerprint.FingerprintSimilarityComputer;
import com.musicg.wave.Wave;
import com.musicg.wave.WaveHeader;

/**
 * @author nutnuts
 * 
 */
public class MusicgFingerprintRecognizer extends SpeechRecognizer implements RecognizerCallback {

	private static final String TAG = FastDTWSpeechRecognizer.class.getSimpleName();
	private SparseArray<ArrayList<String>> cachedMatches = new SparseArray<ArrayList<String>>();
	private SparseArray<String> processedFilePaths = new SparseArray<String>();
	private String workingDirectory;

	public void setSavingDirectory(String dirPath) {
		workingDirectory = dirPath;
	}

	@Override
	public boolean isAudioFormatSupported(AudioInputStream streamToCheck) {
		return true;
	}

	@Override
	protected void runRecognitionTask(AudioInputStream inputStream) {

		int identifier = getMyIdentifier();
		boolean hasResult = false;
		WaveHeader header = new WaveHeader();
		header.setBitsPerSample(inputStream.getSampleSizeInBits());
		header.setChannels(inputStream.getChannels());
		header.setSampleRate(inputStream.getSampleRate());
		byte[] data = null;
		data = new byte[8012];
		try {
			int offset = 0;
			int tmp = 0;
			while ((tmp = inputStream.read()) != -1) {
				if (tmp == -1) {
					break;
				}
				offset++;
				if (offset >= data.length) {
					byte[] buffer = new byte[data.length * 2];
					System.arraycopy(data, 0, buffer, 0, data.length);
					data = buffer;
				}
				data[offset] = (byte) tmp;

			}
			Log.v(TAG, "We read whole file: " + offset);
		} catch (IOException e) {
			e.printStackTrace();
		}
		Wave thisWave = new Wave(header, data);
		byte[] currentFingerprint = thisWave.getFingerprint();
		FingerprintManager fpm = new FingerprintManager();
		ArrayList<String> matches = new ArrayList<String>();

		int key = 0;
		for (int i = 0; i < processedFilePaths.size(); i++) {
			key = processedFilePaths.keyAt(i);
			byte[] fileFingerprint = fpm.getFingerprintFromFile(processedFilePaths.get(key));

			FingerprintSimilarityComputer fsc = new FingerprintSimilarityComputer(currentFingerprint, fileFingerprint);
			FingerprintSimilarity fps = fsc.getFingerprintsSimilarity();
			float similarity = fps.getSimilarity();
			float score = fps.getScore();
			Log.v(TAG, "We get distance for " + processedFilePaths.get(key) + " of " + similarity + "with score "
					+ score);
			if (similarity >= 0.075) {
				Log.v(TAG, "Has resut for " + identifier);
				matches = cachedMatches.get(key);
				sendResults(matches, Thread.currentThread(), true);
				hasResult = true;
				break;
			}
		}
		if (!hasResult) {
			Log.v(TAG, "Has no resut for " + identifier);
			sendResults(matches);
		}

		if (processedFilePaths.size() < 10 && (matches == null || matches.size() == 0)) {
			String newFile = workingDirectory + "/" + System.currentTimeMillis() + ".fingerprint";
			fpm.saveFingerprintAsFile(currentFingerprint, newFile);
			processedFilePaths.put(identifier, newFile);
			Log.v(TAG, "Saved file " + newFile);
		}

	}

	@Override
	public void onRecognizerResult(int resultCode, Bundle resultBundle) {
		if (resultBundle.getBoolean(BUNDLE_RESULT_RECOGNIZED)) {
			cachedMatches.put(resultBundle.getInt(BUNDLE_IDENTIFIER),
					resultBundle.getStringArrayList(BUNDLE_RESULT_MATCHES));
			Log.v(TAG, "Chaching result " + resultBundle.getStringArrayList(BUNDLE_RESULT_MATCHES).toString() + " for "
					+ resultBundle.getInt(BUNDLE_IDENTIFIER));
		}
	}

	@Override
	public void onRecognizerError(Bundle errorBundle) {
	}

}
