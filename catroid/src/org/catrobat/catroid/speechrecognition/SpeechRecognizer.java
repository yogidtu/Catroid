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
import java.util.HashMap;

import android.os.Bundle;
import android.util.Log;

public abstract class SpeechRecognizer {

	protected static final boolean DEBUG_OUTPUT = false;

	private static final String TAG = SpeechRecognizer.class.getSimpleName();
	public static final int ERROR_NONETWORK = 0x1;
	public static final int ERROR_API_CHANGED = 0x2;
	public static final int ERROR_IO = 0x3;
	public static final int ERROR_UNSUPPORTED_AUDIOFORMAT = 0x4;
	public static final int ERROR_OTHER = 0x5;
	private static final int STATE_INIT = 0x1;
	private static final int STATE_PREPARED = 0x2;
	private ArrayList<RecognizerCallback> resultListeners = new ArrayList<RecognizerCallback>();
	private HashMap<Thread, Long> taskQuqe = new HashMap<Thread, Long>();
	private int currentState = STATE_INIT;

	public abstract boolean isAudioFormatSupported(AudioInputStream streamToCheck);

	protected abstract void runRecognitionTask(AudioInputStream inputStream);

	public SpeechRecognizer() {
	}

	public void addCallbackListener(RecognizerCallback resultListener) {
		synchronized (resultListeners) {
			if (resultListener != null) {
				resultListeners.add(resultListener);
			}
		}
	}

	public void startRecognizeInput(final AudioInputStream inputStream, long identifier)
			throws IllegalArgumentException, IllegalStateException {

		if (currentState != STATE_PREPARED) {
			throw new IllegalStateException();
		}
		if (!isAudioFormatSupported(inputStream)) {
			throw new IllegalArgumentException("Streamformat is not supported");
		}
		Thread task = new Thread(new Runnable() {

			@Override
			public void run() {
				runRecognitionTask(inputStream);
			}
		});
		taskQuqe.put(task, identifier);
		task.start();
	}

	public void startRecognizeInput(AudioInputStream inputStream) throws IllegalArgumentException,
			IllegalStateException {
		this.startRecognizeInput(inputStream, 0);
	}

	public void prepare() throws IllegalStateException {
		if (resultListeners.size() == 0 || taskQuqe.size() != 0) {
			throw new IllegalStateException();
		}
		currentState = STATE_PREPARED;
	}

	protected synchronized void sendResults(ArrayList<String> matches) {
		if (!taskQuqe.containsKey(Thread.currentThread())) {
			Thread.dumpStack();
			if (DEBUG_OUTPUT) {
				Log.v(TAG, "Not registered Task tries to send Results. Twice?");
			}
			return;
		}
		long identifier = taskQuqe.get(Thread.currentThread());
		taskQuqe.remove(Thread.currentThread());
		Bundle resultBundle = new Bundle();
		if (identifier != 0) {
			resultBundle.putLong(RecognizerCallback.BUNDLE_IDENTIFIER, identifier);
		}
		synchronized (resultListeners) {
			if (matches != null && matches.size() > 0) {
				resultBundle.putStringArrayList(RecognizerCallback.RESULT_BUNDLE_MATCHES, matches);
				if (DEBUG_OUTPUT) {
					Log.v(TAG,
							"we are sending Results: " + matches.toString() + " from Thread" + Thread.currentThread());
				}
				for (RecognizerCallback listener : resultListeners) {
					listener.onRecognizerResult(RecognizerCallback.RESULT_OK, resultBundle);
				}
			} else {
				if (DEBUG_OUTPUT) {
					Log.v(TAG, "we are sending Results, but none here :(  from Thread" + Thread.currentThread());
				}
				for (RecognizerCallback listener : resultListeners) {
					listener.onRecognizerResult(RecognizerCallback.RESULT_NOMATCH, resultBundle);
				}
			}
		}
	}

	protected synchronized void sendError(int errorCode, String errorMessage) {
		if (!taskQuqe.containsKey(Thread.currentThread())) {
			Thread.dumpStack();
			if (DEBUG_OUTPUT) {
				Log.v(TAG, "Not registered Task tries to send Error! message:" + errorMessage);
			}
			return;
		}
		long identifier = taskQuqe.get(Thread.currentThread());
		taskQuqe.remove(Thread.currentThread());

		Bundle errorBundle = new Bundle();
		if (identifier != 0) {
			errorBundle.putLong(RecognizerCallback.BUNDLE_IDENTIFIER, identifier);
		}

		synchronized (resultListeners) {
			if (DEBUG_OUTPUT) {
				Log.v(TAG, "Recognition error!" + errorMessage);
			}
			errorBundle.putString(RecognizerCallback.ERROR_BUNDLE_MESSAGE, errorMessage);
			errorBundle.putInt(RecognizerCallback.ERROR_BUNDLE_CODE, errorCode);
			for (RecognizerCallback listener : resultListeners) {
				listener.onRecognizerError(errorBundle);
			}
		}

	}

}
