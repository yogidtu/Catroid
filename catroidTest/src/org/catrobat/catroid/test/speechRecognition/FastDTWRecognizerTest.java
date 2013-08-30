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
package org.catrobat.catroid.test.speechRecognition;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteOrder;
import java.util.ArrayList;

import org.catrobat.catroid.ProjectManager;
import org.catrobat.catroid.common.ScreenValues;
import org.catrobat.catroid.common.StandardProjectHandler;
import org.catrobat.catroid.speechrecognition.AudioInputStream;
import org.catrobat.catroid.speechrecognition.FastDTWSpeechRecognizer;
import org.catrobat.catroid.speechrecognition.RecognizerCallback;
import org.catrobat.catroid.test.R;
import org.catrobat.catroid.test.utils.TestUtils;
import org.catrobat.catroid.utils.Utils;

import android.media.AudioFormat;
import android.os.Bundle;
import android.test.InstrumentationTestCase;
import android.util.Log;

public class FastDTWRecognizerTest extends InstrumentationTestCase implements RecognizerCallback {

	private ArrayList<String> lastMatches = new ArrayList<String>();
	private String testProjectName = "FastDTWProject";

	@Override
	public void tearDown() throws Exception {
		lastMatches.clear();
		super.tearDown();
	}

	@Override
	public void setUp() {
		TestUtils.clearProject(testProjectName);
		lastMatches.clear();
	}

	public void testSameFiles() throws IOException {

		int frameSize = 256;

		ScreenValues.SCREEN_WIDTH = 720;
		ScreenValues.SCREEN_HEIGHT = 1134;
		ProjectManager.getInstance().setProject(
				StandardProjectHandler.createAndSaveStandardProject(testProjectName, getInstrumentation()
						.getTargetContext()));

		FastDTWSpeechRecognizer recognizer = new FastDTWSpeechRecognizer();
		recognizer.setSavingDirectory(Utils.buildProjectPath(testProjectName));
		recognizer.addCallbackListener(this);
		recognizer.prepare();

		int identifier = 10;
		Log.v("SebiTest", "We are analysing ----------------------- right01");
		InputStream realAudioExampleStream = getInstrumentation().getContext().getResources()
				.openRawResource(R.raw.right01);
		AudioInputStream audioFileStream = new AudioInputStream(realAudioExampleStream, AudioFormat.ENCODING_PCM_16BIT,
				1, 16000, frameSize, ByteOrder.LITTLE_ENDIAN, true);

		recognizer.startRecognizeInput(audioFileStream, identifier);

		int i = 10;
		do {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
			}
		} while (recognizer.getRunningTaskCount() > 0 && i-- > 0);
		assertTrue("Timed out", i > 0);

		ArrayList<String> matches = new ArrayList<String>();
		matches.add("rechts");
		Bundle mocketResultBundle = new Bundle();
		mocketResultBundle.putStringArrayList(BUNDLE_RESULT_MATCHES, matches);
		mocketResultBundle.putBoolean(BUNDLE_RESULT_RECOGNIZED, true);
		mocketResultBundle.putInt(BUNDLE_IDENTIFIER, identifier);
		mocketResultBundle.putString(BUNDLE_SENDERCLASS, this.toString());
		recognizer.onRecognizerResult(RESULT_OK, mocketResultBundle);

		Log.v("SebiTest", "We are analysing ----------------------- right02");
		realAudioExampleStream = getInstrumentation().getContext().getResources().openRawResource(R.raw.right02);
		audioFileStream = new AudioInputStream(realAudioExampleStream, AudioFormat.ENCODING_PCM_16BIT, 1, 16000,
				frameSize, ByteOrder.LITTLE_ENDIAN, true);
		recognizer.startRecognizeInput(audioFileStream, ++identifier);

		i = 40;
		do {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
			}
		} while (recognizer.getRunningTaskCount() > 0 && i-- > 0);
		assertTrue("Timed out", i > 0);

		Log.v("SebiTest", "We are analysing ----------------------- left01");
		realAudioExampleStream = getInstrumentation().getContext().getResources().openRawResource(R.raw.links01);
		audioFileStream = new AudioInputStream(realAudioExampleStream, AudioFormat.ENCODING_PCM_16BIT, 1, 16000,
				frameSize, ByteOrder.LITTLE_ENDIAN, true);
		recognizer.startRecognizeInput(audioFileStream, ++identifier);

		i = 40;
		do {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
			}
		} while (recognizer.getRunningTaskCount() > 0 && i-- > 0);
		assertTrue("Timed out", i > 0);

		matches = new ArrayList<String>();
		matches.add("links");
		mocketResultBundle = new Bundle();
		mocketResultBundle.putStringArrayList(BUNDLE_RESULT_MATCHES, matches);
		mocketResultBundle.putBoolean(BUNDLE_RESULT_RECOGNIZED, true);
		mocketResultBundle.putInt(BUNDLE_IDENTIFIER, identifier);
		mocketResultBundle.putString(BUNDLE_SENDERCLASS, this.toString());
		recognizer.onRecognizerResult(RESULT_OK, mocketResultBundle);

		Log.v("SebiTest", "We are analysing ----------------------- right04 (is left)");
		realAudioExampleStream = getInstrumentation().getContext().getResources().openRawResource(R.raw.right04);
		audioFileStream = new AudioInputStream(realAudioExampleStream, AudioFormat.ENCODING_PCM_16BIT, 1, 16000,
				frameSize, ByteOrder.LITTLE_ENDIAN, true);
		recognizer.startRecognizeInput(audioFileStream, ++identifier);

		i = 40;
		do {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
			}
		} while (recognizer.getRunningTaskCount() > 0 && i-- > 0);
		assertTrue("Timed out", i > 0);

		matches = new ArrayList<String>();
		matches.add("links");
		mocketResultBundle = new Bundle();
		mocketResultBundle.putStringArrayList(BUNDLE_RESULT_MATCHES, matches);
		mocketResultBundle.putBoolean(BUNDLE_RESULT_RECOGNIZED, true);
		mocketResultBundle.putInt(BUNDLE_IDENTIFIER, identifier);
		mocketResultBundle.putString(BUNDLE_SENDERCLASS, this.toString());
		recognizer.onRecognizerResult(RESULT_OK, mocketResultBundle);

		Log.v("SebiTest", "We are analysing ----------------------- right03");
		realAudioExampleStream = getInstrumentation().getContext().getResources().openRawResource(R.raw.right03);
		audioFileStream = new AudioInputStream(realAudioExampleStream, AudioFormat.ENCODING_PCM_16BIT, 1, 16000,
				frameSize, ByteOrder.LITTLE_ENDIAN, true);
		recognizer.startRecognizeInput(audioFileStream, ++identifier);

		i = 40;
		do {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
			}
		} while (recognizer.getRunningTaskCount() > 0 && i-- > 0);
		assertTrue("Timed out", i > 0);

		Log.v("SebiTest", "We are analysing ----------------------- links02");
		realAudioExampleStream = getInstrumentation().getContext().getResources().openRawResource(R.raw.links02);
		audioFileStream = new AudioInputStream(realAudioExampleStream, AudioFormat.ENCODING_PCM_16BIT, 1, 16000,
				frameSize, ByteOrder.LITTLE_ENDIAN, true);
		recognizer.startRecognizeInput(audioFileStream, ++identifier);

		i = 40;
		do {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
			}
		} while (recognizer.getRunningTaskCount() > 0 && i-- > 0);
		assertTrue("Timed out", i > 0);

		Log.v("SebiTest", "We are analysing ----------------------- right03");
		realAudioExampleStream = getInstrumentation().getContext().getResources().openRawResource(R.raw.right03);
		audioFileStream = new AudioInputStream(realAudioExampleStream, AudioFormat.ENCODING_PCM_16BIT, 1, 16000,
				frameSize, ByteOrder.LITTLE_ENDIAN, true);
		recognizer.startRecognizeInput(audioFileStream, ++identifier);

		i = 40;
		do {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
			}
		} while (recognizer.getRunningTaskCount() > 0 && i-- > 0);
		assertTrue("Timed out", i > 0);

		Log.v("SebiTest", "We are analysing ----------------------- right04 (is left)");
		realAudioExampleStream = getInstrumentation().getContext().getResources().openRawResource(R.raw.right04);
		audioFileStream = new AudioInputStream(realAudioExampleStream, AudioFormat.ENCODING_PCM_16BIT, 1, 16000,
				frameSize, ByteOrder.LITTLE_ENDIAN, true);
		recognizer.startRecognizeInput(audioFileStream, ++identifier);

		i = 40;
		do {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
			}
		} while (recognizer.getRunningTaskCount() > 0 && i-- > 0);
		assertTrue("Timed out", i > 0);
		//
		//		matches = new ArrayList<String>();
		//		matches.add("links");
		//		mocketResultBundle = new Bundle();
		//		mocketResultBundle.putStringArrayList(BUNDLE_RESULT_MATCHES, matches);
		//		mocketResultBundle.putBoolean(BUNDLE_RESULT_RECOGNIZED, true);
		//		mocketResultBundle.putInt(BUNDLE_IDENTIFIER, identifier);
		//		mocketResultBundle.putString(BUNDLE_SENDERCLASS, this.toString());
		//		recognizer.onRecognizerResult(RESULT_OK, mocketResultBundle);

		Log.v("SebiTest", "We are analysing ----------------------- links01");
		realAudioExampleStream = getInstrumentation().getContext().getResources().openRawResource(R.raw.links01);
		audioFileStream = new AudioInputStream(realAudioExampleStream, AudioFormat.ENCODING_PCM_16BIT, 1, 16000,
				frameSize, ByteOrder.LITTLE_ENDIAN, true);
		recognizer.startRecognizeInput(audioFileStream, ++identifier);

		i = 40;
		do {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
			}
		} while (recognizer.getRunningTaskCount() > 0 && i-- > 0);
		assertTrue("Timed out", i > 0);

		Log.v("SebiTest", "We are analysing ----------------------- links02");
		realAudioExampleStream = getInstrumentation().getContext().getResources().openRawResource(R.raw.links02);
		audioFileStream = new AudioInputStream(realAudioExampleStream, AudioFormat.ENCODING_PCM_16BIT, 1, 16000,
				frameSize, ByteOrder.LITTLE_ENDIAN, true);
		recognizer.startRecognizeInput(audioFileStream, ++identifier);

		i = 40;
		do {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
			}
		} while (recognizer.getRunningTaskCount() > 0 && i-- > 0);
		assertTrue("Timed out", i > 0);

		Log.v("SebiTest", "We are analysing ----------------------- links03");
		realAudioExampleStream = getInstrumentation().getContext().getResources().openRawResource(R.raw.links03);
		audioFileStream = new AudioInputStream(realAudioExampleStream, AudioFormat.ENCODING_PCM_16BIT, 1, 16000,
				frameSize, ByteOrder.LITTLE_ENDIAN, true);
		recognizer.startRecognizeInput(audioFileStream, ++identifier);

		i = 40;
		do {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
			}
		} while (recognizer.getRunningTaskCount() > 0 && i-- > 0);
		assertTrue("Timed out", i > 0);

		Log.v("SebiTest", "We are analysing ----------------------- links04");
		realAudioExampleStream = getInstrumentation().getContext().getResources().openRawResource(R.raw.links04);
		audioFileStream = new AudioInputStream(realAudioExampleStream, AudioFormat.ENCODING_PCM_16BIT, 1, 16000,
				frameSize, ByteOrder.LITTLE_ENDIAN, true);
		recognizer.startRecognizeInput(audioFileStream, ++identifier);

		i = 40;
		do {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
			}
		} while (recognizer.getRunningTaskCount() > 0 && i-- > 0);
		assertTrue("Timed out", i > 0);

		Log.v("SebiTest", "We are analysing ----------------------- links05 (is stop)");
		realAudioExampleStream = getInstrumentation().getContext().getResources().openRawResource(R.raw.links05);
		audioFileStream = new AudioInputStream(realAudioExampleStream, AudioFormat.ENCODING_PCM_16BIT, 1, 16000,
				frameSize, ByteOrder.LITTLE_ENDIAN, true);
		recognizer.startRecognizeInput(audioFileStream, ++identifier);

		i = 40;
		do {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
			}
		} while (recognizer.getRunningTaskCount() > 0 && i-- > 0);
		assertTrue("Timed out", i > 0);

		Log.v("SebiTest", "We are analysing ----------------------- allofthem");
		realAudioExampleStream = getInstrumentation().getContext().getResources()
				.openRawResource(R.raw.speechsample_directions);
		audioFileStream = new AudioInputStream(realAudioExampleStream, AudioFormat.ENCODING_PCM_16BIT, 1, 16000,
				frameSize, ByteOrder.LITTLE_ENDIAN, true);
		recognizer.startRecognizeInput(audioFileStream, ++identifier);

		i = 40;
		do {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
			}
		} while (recognizer.getRunningTaskCount() > 0 && i-- > 0);
		assertTrue("Timed out", i > 0);
	}

	public void onRecognizerResult(int resultCode, Bundle resultBundle) {
		// TODO Auto-generated method stub

	}

	public void onRecognizerError(Bundle errorBundle) {
		// TODO Auto-generated method stub

	}
}
