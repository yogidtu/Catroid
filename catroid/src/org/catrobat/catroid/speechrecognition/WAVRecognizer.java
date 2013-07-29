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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import javaFlacEncoder.FLAC_FileEncoderAndroid;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

public class WAVRecognizer extends Thread {

	private static final String API_URL = "https://www.google.com/speech-api/v1/recognize?client=chromium&lang=de-DE&maxresults=5";

	public static final int ERROR_APICHANGE = -1;
	public static final int ERROR_NONETWORK = -2;
	public static final int ERROR_NO_FILE = -3;
	public static final int ERROR_CONVERTION = -4;

	private String inputFilePath;
	private String outputFilePath;
	private SpeechFileToTextListener listener;
	private boolean onlyConvert = false;

	public WAVRecognizer(String inputFilePath, SpeechFileToTextListener listener) {
		this.inputFilePath = inputFilePath;
		this.listener = listener;
		File tmp = new File(inputFilePath);
		outputFilePath = tmp.getAbsolutePath() + ".flac";
	}

	@Override
	public void run() {
		File convertedFile = convertWavToFlacAndRecognize(inputFilePath, outputFilePath);

		if (convertedFile == null) {
			return;
		}

		if (!onlyConvert) {
			JSONArray jsonResponse = sendToOnlineAPI(convertedFile);

			ArrayList<String> matches = new ArrayList<String>();
			try {
				if (jsonResponse != null) {
					matches.add(jsonResponse.getJSONObject(0).getString("utterance"));
					for (int i = 1; i < jsonResponse.length(); i++) {
						matches.add(jsonResponse.getJSONObject(i).getString("utterance"));
					}
				}
				listener.onFileRecognized(outputFilePath, matches);
			} catch (JSONException e) {
				listener.onFileToTextError(ERROR_APICHANGE, "The response JSON-Object couldn't be parsed correct");
			}
		} else {
			listener.onFileRecognized(outputFilePath, null);
		}
		convertedFile.delete();
	}

	private File convertWavToFlacAndRecognize(String wavFilename, String flacFilename) {
		FLAC_FileEncoderAndroid flacEncoder = new FLAC_FileEncoderAndroid();
		File inputFile = new File(inputFilePath);
		File outputFile = new File(flacFilename);

		FLAC_FileEncoderAndroid.Status convertionState = flacEncoder.encode(inputFile, outputFile);

		if (convertionState == FLAC_FileEncoderAndroid.Status.FULL_ENCODE) {

			return outputFile;

		} else {
			listener.onFileToTextError(ERROR_CONVERTION, "Couldn't convert the File. state: " + convertionState);
			return null;
		}
	}

	public JSONArray sendToOnlineAPI(File flacFileToSend) {
		// Create a new HttpClient and Post Header
		HttpClient httpclient = new DefaultHttpClient();
		HttpPost httppost = new HttpPost(API_URL);
		JSONArray resturnJson = new JSONArray();

		InputStreamEntity reqEntity;
		try {
			reqEntity = new InputStreamEntity(new FileInputStream(flacFileToSend), -1);
		} catch (FileNotFoundException e) {
			listener.onFileToTextError(ERROR_NO_FILE, "Couldn't read converted file.");
			return null;
		}
		reqEntity.setContentType("audio/x-flac");
		reqEntity.setChunked(true);
		httppost.setEntity(reqEntity);
		httppost.setHeader("User-Agent",
				"Mozilla/5.0 (Macintosh; Intel Mac OS X 10_6_8) AppleWebKit/535.7 (KHTML, like Gecko) Chrome/16.0.912.77 Safari/535.7");
		httppost.setHeader("Content-Type", "audio/x-flac; rate=16000;");

		HttpResponse response;
		try {
			response = httpclient.execute(httppost);
		} catch (IOException e) {
			listener.onFileToTextError(ERROR_NONETWORK, "No Networkconnection could be established.");
			return null;
		}

		BufferedReader reader;
		try {
			reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "UTF-8"));
		} catch (IOException e) {
			listener.onFileToTextError(ERROR_NONETWORK, "No Networkconnection could be established.");
			return null;
		}

		StringBuilder builder = new StringBuilder();
		try {
			for (String line = null; (line = reader.readLine()) != null;) {
				builder.append(line).append("\n");
			}
		} catch (IOException e) {
			listener.onFileToTextError(ERROR_NONETWORK, "Networkconnection interrupted.");
			return null;
		}

		String resp = builder.toString();
		if (resp.contains("NO_MATCH")) {
			return null;
		}

		JSONObject object;
		try {
			object = (JSONObject) new JSONTokener(resp).nextValue();
			if (object.getInt("status") == 0) {
				resturnJson = object.getJSONArray("hypotheses");
			}
		} catch (JSONException e) {
			listener.onFileToTextError(ERROR_APICHANGE, "The response JSON-Object couldn't be parsed correct");
			return null;
		}
		return resturnJson;
	}

	public void setConvertOnly(boolean onlyConvert) {
		this.onlyConvert = onlyConvert;
	}

	public interface SpeechFileToTextListener {
		public void onFileRecognized(String speechFilePath, ArrayList<String> matches);

		public void onFileToTextError(int errorCode, String errorMessage);
	}

}
