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
package org.catrobat.catroid.uitest.web;

import android.media.AudioFormat;
import android.os.Bundle;
import android.test.InstrumentationTestCase;

import org.catrobat.catroid.speechrecognition.AudioInputStream;
import org.catrobat.catroid.speechrecognition.RecognitionManager;
import org.catrobat.catroid.speechrecognition.RecognizerCallback;
import org.catrobat.catroid.uitest.R;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteOrder;
import java.util.ArrayList;

public class SpeechRecognitionWebTest extends InstrumentationTestCase implements RecognizerCallback {

	private ArrayList<String> lastMatches = new ArrayList<String>();
	private String lastErrorMessage = "";

	@Override
	public void tearDown() throws Exception {
		lastMatches.clear();
		super.tearDown();
	}

	@Override
	public void setUp() {
		lastMatches.clear();
		lastErrorMessage = "";
	}

	/*
	 * Default, the manager uses the GoogleOnlineSpeechRecognizer, so it needs an internet connection.
	 */
	public void testSimpleOnlineSpeechRecognition() throws IOException {

		InputStream realAudioExampleStream = getInstrumentation().getContext().getResources()
				.openRawResource(R.raw.speechsample_directions);
		AudioInputStream audioFileStream = new AudioInputStream(realAudioExampleStream, AudioFormat.ENCODING_PCM_16BIT,
				1, 16000, 512, ByteOrder.LITTLE_ENDIAN, true);

		RecognitionManager manager = new RecognitionManager(audioFileStream);
		manager.registerContinuousSpeechListener(this);
		manager.start();

		int i = 15;
		do {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		} while ((i--) > 0 && manager.isRecognitionRunning() && lastErrorMessage == "");

		assertTrue("Error occured:\n" + lastErrorMessage, lastErrorMessage == "");
		assertTrue("Timed out.", i > 0);
		assertTrue("There was no recognition", lastMatches.size() > 0);

		assertTrue("\"links\" was not recognized.", matchesContainString("links"));
		assertTrue("\"rechts\" was not recognized.", matchesContainString("rechts"));
		assertTrue("\"rauf\" was not recognized.", matchesContainString("rauf"));
		assertTrue("\"runter\" was not recognized.", matchesContainString("runter"));
		assertTrue("\"stop\" was not recognized.", matchesContainString("stop"));
		manager.unregisterContinuousSpeechListener(this);
		manager.stop();
	}

	/*
	 * Wordchecking is performed through wikitionary, so an internetconnection is needed
	 */
	public void testWordChecking() {
		ArrayList<String> existingWords = new ArrayList<String>();
		existingWords.add("Katze"); //de
		existingWords.add("Cat"); //en
		existingWords.add("chatte"); //fr
		existingWords.add("gato"); //sp
		existingWords.add("кошка"); //ru
		existingWords.add("猫"); //ja
		ArrayList<String> fantasyWords = new ArrayList<String>();
		fantasyWords.add("asgfddf");
		fantasyWords.add("iliketoeatkittens");
		fantasyWords.add("naitsabes");

		for (String toCheck : existingWords) {
			int validWord = RecognitionManager.isWordRecognizeable(toCheck);
			if (validWord == 0) {
				fail(toCheck + " was marked as no valid word");
			}
			if (validWord < 0) {
				fail("Error occured in wordcheck, maybe no network-connection?");
			}
		}
		for (String toCheck : fantasyWords) {
			int validWord = RecognitionManager.isWordRecognizeable(toCheck);
			if (validWord > 0) {
				fail(toCheck + " was marked as valid word");
			}
			if (validWord < 0) {
				fail("Error occured in wordcheck, maybe no network-connection?");
			}
		}

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
		lastErrorMessage = errorBundle.getString(BUNDLE_ERROR_MESSAGE);
	}

}
