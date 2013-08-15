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
package org.catrobat.catroid.test.utiltests;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteOrder;
import java.util.ArrayList;

import org.catrobat.catroid.ProjectManager;
import org.catrobat.catroid.common.ScreenValues;
import org.catrobat.catroid.common.StandardProjectHandler;
import org.catrobat.catroid.speechrecognition.AudioInputStream;
import org.catrobat.catroid.speechrecognition.RecognizerCallback;
import org.catrobat.catroid.test.R;
import org.catrobat.catroid.test.utils.TestUtils;
import org.catrobat.catroid.utils.UtilSpeechRecognition;

import android.media.AudioFormat;
import android.os.Bundle;
import android.test.InstrumentationTestCase;

public class UtilSpeechRecognitionTest extends InstrumentationTestCase implements RecognizerCallback {

	private String testProjectName = "testStandardProjectRecognition";
	private ArrayList<String> lastMatches = new ArrayList<String>();
	private static final int SPEECH_FILE_ID = R.raw.speechsample_directions;

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

	public void testChunkedSpeechRecognition() throws IOException {

		ScreenValues.SCREEN_WIDTH = 720;
		ScreenValues.SCREEN_HEIGHT = 1134;

		ProjectManager.getInstance().setProject(
				StandardProjectHandler.createAndSaveStandardProject(testProjectName, getInstrumentation()
						.getTargetContext()));
		File testSpeechFile = TestUtils.saveFileToProject(testProjectName, "directionSpeech.wav", SPEECH_FILE_ID,
				getInstrumentation().getContext(), TestUtils.TYPE_SOUND_FILE);
		FileInputStream speechFileStream = new FileInputStream(testSpeechFile);
		AudioInputStream audioFileStream = new AudioInputStream(speechFileStream, AudioFormat.ENCODING_PCM_16BIT, 1,
				16000, 512, ByteOrder.LITTLE_ENDIAN, true);

		UtilSpeechRecognition speechRecognizer = new UtilSpeechRecognition(audioFileStream);
		speechRecognizer.registerContinuousSpeechListener(this);
		speechRecognizer.start();

		int i = 15;
		do {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		} while ((i--) > 0 && speechRecognizer.isRecognitionRunning());

		assertTrue("Timed out.", i > 0);
		assertTrue("There was no recognition", lastMatches.size() > 0);

		assertTrue("\"links\" was not recognized.", matchesContainString("links"));
		assertTrue("\"rechts\" was not recognized.", matchesContainString("rechts"));
		assertTrue("\"rauf\" was not recognized.", matchesContainString("rauf"));
		assertTrue("\"runter\" was not recognized.", matchesContainString("runter"));
		assertTrue("\"stop\" was not recognized.", matchesContainString("stop"));
		speechRecognizer.unregisterContinuousSpeechListener(this);
		speechRecognizer.stop();
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
		fail(errorBundle.getString(BUNDLE_ERROR_MESSAGE));
	}
}
