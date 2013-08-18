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

import org.catrobat.catroid.speechrecognition.AudioInputStream;
import org.catrobat.catroid.speechrecognition.GoogleOnlineSpeechRecognizer;
import org.catrobat.catroid.speechrecognition.RecognizerCallback;
import org.catrobat.catroid.test.R;

import android.media.AudioFormat;
import android.os.Bundle;
import android.test.InstrumentationTestCase;

public class SpeechRecognizerTest extends InstrumentationTestCase implements RecognizerCallback {

	private ArrayList<String> lastMatches = new ArrayList<String>();
	private String lastErrorMessage = "";

	@Override
	public void tearDown() throws Exception {
		lastMatches.clear();
		super.tearDown();
	}

	@Override
	public void setUp() {
		lastErrorMessage = "";
		lastMatches.clear();
	}

	public void testOnlineRecognition() throws IOException {

		InputStream realAudioExampleStream = getInstrumentation().getContext().getResources()
				.openRawResource(R.raw.speechsample_directions);
		AudioInputStream audioFileStream = new AudioInputStream(realAudioExampleStream, AudioFormat.ENCODING_PCM_16BIT,
				1, 16000, 128, ByteOrder.LITTLE_ENDIAN, true);

		GoogleOnlineSpeechRecognizer converter = new GoogleOnlineSpeechRecognizer();
		converter.addCallbackListener(this);
		converter.prepare();
		converter.startRecognizeInput(audioFileStream);

		int i = 50;
		do {
			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		} while ((i--) != 0 && lastMatches.size() == 0 && lastErrorMessage == "");

		if (lastErrorMessage != "") {
			fail("Conversion brought an error: " + lastErrorMessage);
		}

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

	public void onRecognizerResult(int resultCode, Bundle resultBundle) {

		if (resultCode == RESULT_NOMATCH) {
			return;
		}

		ArrayList<String> matches = resultBundle.getStringArrayList(BUNDLE_RESULT_MATCHES);
		lastMatches.add(matches.toString());
	}

	public void onRecognizerError(Bundle errorBundle) {
		lastErrorMessage = errorBundle.getString(BUNDLE_ERROR_MESSAGE);
	}
}
