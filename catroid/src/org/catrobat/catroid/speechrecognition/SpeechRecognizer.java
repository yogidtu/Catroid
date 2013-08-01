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

import java.util.ArrayList;

import android.os.Bundle;

public abstract class SpeechRecognizer extends Thread {

	public static final int ERROR_NONETWORK = 0x1;
	public static final int ERROR_API_CHANGED = 0x2;
	public static final int ERROR_IO = 0x3;
	public static final int ERROR_UNSUPPORTED_AUDIOFORMAT = 0x4;
	public static final int ERROR_OTHER = 0x5;

	protected static final int STATE_INIT = 0x1;
	protected static final int STATE_PREPARED = 0x2;
	protected static final int STATE_EXECUTE = 0x3;

	protected AudioInputStream stream = null;
	private RecognizerCallback resultListener = null;
	protected int currentState = STATE_INIT;
	private int identifier;

	public SpeechRecognizer() {
	}

	public void setCallbackListener(RecognizerCallback resultListener) {
		this.resultListener = resultListener;
	}

	public void setAudioInputStream(AudioInputStream inputStream, int identifier) throws IllegalArgumentException {
		setAudioInputStream(inputStream);
		this.identifier = identifier;
	}

	public abstract void setAudioInputStream(AudioInputStream inputStream) throws IllegalArgumentException;

	public void prepare() throws IllegalThreadStateException {
		if (currentState != STATE_INIT || stream == null || resultListener == null) {
			throw new IllegalThreadStateException();
		}
		currentState = STATE_PREPARED;
	}

	@Override
	public void start() throws IllegalThreadStateException {
		if (currentState != STATE_PREPARED) {
			throw new IllegalThreadStateException();
		}
		currentState = STATE_EXECUTE;
		super.start();
	}

	protected void sendResults(ArrayList<String> matches) {
		Bundle resultBundle = new Bundle();
		if (identifier != 0) {
			resultBundle.putInt("IDENTIFIER", identifier);
		}
		if (matches != null && matches.size() > 0) {
			resultBundle.putStringArrayList("RESULT", matches);
			resultListener.onRecognizerResult(RecognizerCallback.RESULT_OK, resultBundle);
		} else {
			resultListener.onRecognizerResult(RecognizerCallback.RESULT_NOMATCH, resultBundle);
		}
	}

	protected void sendError(int errorCode, String errorMessage) {
		resultListener.onRecognizerError(errorCode, errorMessage);
	}

	@Override
	public abstract void run();

}
