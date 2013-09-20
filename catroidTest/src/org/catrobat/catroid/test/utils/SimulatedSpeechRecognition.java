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
package org.catrobat.catroid.test.utils;

import android.os.Bundle;

import org.catrobat.catroid.speechrecognition.RecognizerCallback;
import org.catrobat.catroid.stage.StageActivity;

import java.util.ArrayList;

public class SimulatedSpeechRecognition extends StageActivity {

	private boolean recognitionRequested = false;
	private RecognizerCallback lastCaller;
	private String lastQuestion;

	@Override
	public void askForSpeechInput(String question, RecognizerCallback callback) {
		lastQuestion = question;
		lastCaller = callback;
		recognitionRequested = true;
	}

	public boolean isRecognitionRequested() {
		return recognitionRequested;
	}

	public String getLastQuestion() {
		return lastQuestion;
	}

	public void finishLastRequest(ArrayList<String> answer) {
		Bundle result = new Bundle();
		result.putStringArrayList(RecognizerCallback.BUNDLE_RESULT_MATCHES, answer);
		lastCaller.onRecognizerResult(RecognizerCallback.RESULT_OK, result);
		recognitionRequested = false;
	}
}
