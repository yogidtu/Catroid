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
