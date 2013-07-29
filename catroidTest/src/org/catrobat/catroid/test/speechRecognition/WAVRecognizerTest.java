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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.catrobat.catroid.ProjectManager;
import org.catrobat.catroid.common.ScreenValues;
import org.catrobat.catroid.common.StandardProjectHandler;
import org.catrobat.catroid.speechrecognition.WAVRecognizer;
import org.catrobat.catroid.speechrecognition.WAVRecognizer.SpeechFileToTextListener;
import org.catrobat.catroid.test.R;
import org.catrobat.catroid.test.utils.TestUtils;

import android.test.InstrumentationTestCase;

public class WAVRecognizerTest extends InstrumentationTestCase implements SpeechFileToTextListener {

	private String testProjectName = "testStandardProjectBuilding";
	private ArrayList<String> savedFiles = new ArrayList<String>();
	private ArrayList<String> lastMatches = new ArrayList<String>();
	private String lastErrorMessage = "";
	private static final int SPEECH_FILE_ID = R.raw.speechsample_directions;

	@Override
	public void tearDown() throws Exception {
		savedFiles.clear();
		lastMatches.clear();
		super.tearDown();
		TestUtils.clearProject(testProjectName);
	}

	@Override
	public void setUp() {
		TestUtils.clearProject(testProjectName);
		lastErrorMessage = "";
		savedFiles.clear();
		lastMatches.clear();
	}

	public void testConverting() throws IOException {

		ScreenValues.SCREEN_WIDTH = 720;
		ScreenValues.SCREEN_HEIGHT = 1134;
		ProjectManager.getInstance().setProject(
				StandardProjectHandler.createAndSaveStandardProject(testProjectName, getInstrumentation()
						.getTargetContext()));

		File testSpeechFile = TestUtils.saveFileToProject(testProjectName, "directionSpeech.wav", SPEECH_FILE_ID,
				getInstrumentation().getContext(), TestUtils.TYPE_SOUND_FILE);

		WAVRecognizer converter = new WAVRecognizer(testSpeechFile.getAbsolutePath(), this);
		converter.setConvertOnly(true);

		converter.start();

		int i = 100;
		do {
			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		} while ((i--) != 0 && savedFiles.size() == 0 && lastErrorMessage == "");

		if (lastErrorMessage != "") {
			fail("Conversion brought an error: " + lastErrorMessage);
		}

		assertTrue("There was no flac speechfile saved.", savedFiles.size() > 0);
		assertTrue("Converted File has wrong Format", savedFiles.get(0).endsWith(".flac"));
	}

	public void testOnlineRecognition() throws IOException {

		ScreenValues.SCREEN_WIDTH = 720;
		ScreenValues.SCREEN_HEIGHT = 1134;
		ProjectManager.getInstance().setProject(
				StandardProjectHandler.createAndSaveStandardProject(testProjectName, getInstrumentation()
						.getTargetContext()));

		File testSpeechFile = TestUtils.saveFileToProject(testProjectName, "directionSpeech.wav", SPEECH_FILE_ID,
				getInstrumentation().getContext(), TestUtils.TYPE_SOUND_FILE);

		WAVRecognizer converter = new WAVRecognizer(testSpeechFile.getAbsolutePath(), this);

		converter.start();

		int i = 100;
		do {
			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		} while ((i--) != 0 && savedFiles.size() == 0 && lastErrorMessage == "");

		if (lastErrorMessage != "") {
			fail("Conversion brought an error: " + lastErrorMessage);
		}

		assertTrue("There was no flac speechfile saved. ConnectionTimeout?", savedFiles.size() > 0);
		assertTrue("There where no results.", lastMatches.size() > 0);
		assertTrue("\"links\" was not recognized.", matchesContainString("links"));
		assertTrue("\"rechts\" was not recognized.", matchesContainString("rechts"));
		assertTrue("\"rauf\" was not recognized.", matchesContainString("rauf"));
		assertTrue("\"runter\" was not recognized.", matchesContainString("runter"));
		assertTrue("\"stop\" was not recognized.", matchesContainString("stop"));

	}

	private boolean matchesContainString(String search) {
		for (String match : lastMatches) {
			if (match.contains(search)) {
				return true;
			}
		}
		return false;
	}

	public void onFileRecognized(String speechFilePath, ArrayList<String> matches) {
		savedFiles.add(speechFilePath);
		if (matches != null) {
			lastMatches = matches;
		}
	}

	public void onFileToTextError(int errorCode, String errorMessage) {
		lastErrorMessage = errorMessage;
	}
}
