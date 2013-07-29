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
package org.catrobat.catroid.utils;

import java.util.ArrayList;

import android.util.Log;

public class UtilSpeechRecognition {

	private static UtilSpeechRecognition instance = null;
	private static final String TAG = UtilSpeechRecognition.class.getSimpleName();
	private String lastBestAnswer = "";
	private ArrayList<String> lastAnswerSuggenstions = new ArrayList<String>();
	protected ArrayList<speechListener> askerList = new ArrayList<speechListener>();

	protected UtilSpeechRecognition() {

	}

	public static UtilSpeechRecognition getInstance() {
		if (instance == null) {
			instance = new UtilSpeechRecognition();
		}
		return instance;
	}

	public synchronized void onRecognitionResult(ArrayList<String> matches) {
		lastBestAnswer = "";
		lastAnswerSuggenstions.clear();

		if (matches == null) {
			return;
		}

		lastBestAnswer = matches.get(0);
		lastAnswerSuggenstions.addAll(matches);

		ArrayList<speechListener> askerListCopy = new ArrayList<speechListener>(askerList);
		for (speechListener listener : askerListCopy) {
			listener.onRecognizedSpeech(lastBestAnswer, lastAnswerSuggenstions);
		}
	}

	public void registerListener(speechListener asker) {
		askerList.add(asker);
		return;
	}

	public void unregisterListener(speechListener asker) {
		if (askerList.contains(asker)) {
			askerList.remove(asker);
		} else {
			Log.v(TAG, "Tried to remove not registered speechListener. " + asker.getClass().getSimpleName());
		}
		return;
	}

	public String getLastBestAnswer() {
		return instance.lastBestAnswer;
	}

	public ArrayList<String> getLastAnswerSuggenstions() {
		return instance.lastAnswerSuggenstions;
	}

	public interface speechListener {
		public boolean onRecognizedSpeech(String bestAnswer, ArrayList<String> allAnswerSuggestions);
	}
}
