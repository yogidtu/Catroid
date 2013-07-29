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
import java.util.ArrayList;

import org.catrobat.catroid.ProjectManager;
import org.catrobat.catroid.common.ScreenValues;
import org.catrobat.catroid.common.StandardProjectHandler;
import org.catrobat.catroid.speechrecognition.VoiceTriggeredRecorder;
import org.catrobat.catroid.speechrecognition.VoiceTriggeredRecorder.VoiceTriggeredRecorderListener;
import org.catrobat.catroid.test.R;
import org.catrobat.catroid.test.utils.Reflection;
import org.catrobat.catroid.test.utils.SimulatedAudioRecord;
import org.catrobat.catroid.test.utils.TestUtils;
import org.catrobat.catroid.utils.MicrophoneGrabber;

import android.test.InstrumentationTestCase;

public class VoiceTriggeredRecorderTest extends InstrumentationTestCase implements VoiceTriggeredRecorderListener {

	private String testProjectName = "testStandardProjectRecognition";
	private ArrayList<String> savedFiles = new ArrayList<String>();
	private static final int SPEECH_FILE_ID = R.raw.speechsample_directions;

	@Override
	public void tearDown() throws Exception {
		savedFiles.clear();
		super.tearDown();
	}

	@Override
	public void setUp() {
		TestUtils.clearProject(testProjectName);
		savedFiles.clear();
	}

	public void testRandomNoise() throws IOException {
		ScreenValues.SCREEN_WIDTH = 720;
		ScreenValues.SCREEN_HEIGHT = 1134;

		ProjectManager.getInstance().setProject(
				StandardProjectHandler.createAndSaveStandardProject(testProjectName, getInstrumentation()
						.getTargetContext()));

		VoiceTriggeredRecorder recorder = new VoiceTriggeredRecorder(this);

		SimulatedAudioRecord simRecorder = new SimulatedAudioRecord();
		Reflection.setPrivateField(MicrophoneGrabber.getInstance(), "audioRecord", simRecorder);

		recorder.startRecording();

		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		recorder.stopRecording();

		assertTrue("There was some speechfile saved", savedFiles.size() == 0);
		Reflection.setPrivateField(MicrophoneGrabber.getInstance(), "instance", null);
	}

	public void testAutomaticFileSaving() throws IOException {

		ScreenValues.SCREEN_WIDTH = 720;
		ScreenValues.SCREEN_HEIGHT = 1134;
		ProjectManager.getInstance().setProject(
				StandardProjectHandler.createAndSaveStandardProject(testProjectName, getInstrumentation()
						.getTargetContext()));

		VoiceTriggeredRecorder recorder = new VoiceTriggeredRecorder(this);

		SimulatedAudioRecord simRecorder = new SimulatedAudioRecord(SPEECH_FILE_ID, getInstrumentation().getContext());
		Reflection.setPrivateField(MicrophoneGrabber.getInstance(), "audioRecord", simRecorder);

		recorder.startRecording();

		int i = 10;
		do {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		} while ((i--) != 0 && simRecorder.isMockRecording());

		recorder.stopRecording();
		assertTrue("There was no speechfile saved", savedFiles.size() > 0);

		Reflection.setPrivateField(MicrophoneGrabber.getInstance(), "instance", null);
	}

	public void onVoiceTriggeredRecorderError(int errorCode) {
		fail("VoiceTriggeredRecorder sent error: " + errorCode);
	}

	public void onSpeechFileSaved(String speechFilePath) {
		savedFiles.add(speechFilePath);
	}
}
