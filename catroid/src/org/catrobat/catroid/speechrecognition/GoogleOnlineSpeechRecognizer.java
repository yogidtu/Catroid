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
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.ArrayList;
import javaFlacEncoder.FLACEncoder;
import javaFlacEncoder.FLACStreamOutputStream;
import javaFlacEncoder.StreamConfiguration;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.util.Log;

public class GoogleOnlineSpeechRecognizer extends SpeechRecognizer {

	private static final String API_URL = "http://www.google.com/speech-api/v1/recognize?client=chromium&lang=de-DE&maxresults=5";
	private static final String USER_AGENT = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_6_8) AppleWebKit/535.7 (KHTML, like Gecko) Chrome/16.0.912.77 Safari/535.7";

	public GoogleOnlineSpeechRecognizer() {
		super();
	}

	@Override
	protected void runRecognitionTask(AudioInputStream inputStream) {
		InputStream flacInputStream = startEncoding(inputStream);
		if (flacInputStream == null) {
			return;
		}
		JSONArray jsonResponse = pipeToOnlineAPI(flacInputStream);

		ArrayList<String> matches = new ArrayList<String>();
		try {
			if (jsonResponse != null) {
				matches.add(jsonResponse.getJSONObject(0).getString("utterance"));
				for (int i = 1; i < jsonResponse.length(); i++) {
					matches.add(jsonResponse.getJSONObject(i).getString("utterance"));
				}
				sendResults(matches);
			}
		} catch (JSONException e) {
			sendError(RecognizerCallback.ERROR_API_CHANGED, "The response JSON-Object couldn't be parsed correct");
		}
	}

	private InputStream startEncoding(final AudioInputStream inputStream) {

		final FLACEncoder flac = new FLACEncoder();
		StreamConfiguration streamConfiguration = new StreamConfiguration();
		streamConfiguration.setBitsPerSample(inputStream.getSampleSizeInBits());
		streamConfiguration.setChannelCount(inputStream.getChannels());
		streamConfiguration.setSampleRate(inputStream.getSampleRate());

		flac.setStreamConfiguration(streamConfiguration);

		PipedInputStream pipedInputStream;
		final PipedOutputStream pipedOutputStream;
		FLACStreamOutputStream flacOutputStream;

		try {
			pipedOutputStream = new PipedOutputStream();
			flacOutputStream = new FLACStreamOutputStream(pipedOutputStream);
			flac.setOutputStream(flacOutputStream);
			pipedInputStream = new PipedInputStream(pipedOutputStream);
			flac.openFLACStream();
		} catch (IOException e1) {
			sendError(RecognizerCallback.ERROR_OTHER, "Pipes couldn't be generated. Try filebased execution.");
			return null;
		}

		final Thread caller = Thread.currentThread();
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					encodeAudioInputStream(inputStream, inputStream.getFrameByteSize(), flac, false);
				} catch (Exception e) {
					sendError(RecognizerCallback.ERROR_OTHER, "There was a problem when converting into FLAC-Format. :"
							+ e.getMessage(), caller);
				}
				try {
					pipedOutputStream.flush();
					pipedOutputStream.close();
				} catch (IOException e) {

				}
			}
		}).start();

		return pipedInputStream;
	}

	private JSONArray pipeToOnlineAPI(InputStream speechInput) {

		HttpClient httpclient = new DefaultHttpClient();
		HttpPost httppost = new HttpPost(API_URL);
		JSONArray resturnJson = new JSONArray();

		BasicHttpEntity entity = new BasicHttpEntity();
		entity.setChunked(true);
		entity.setContentLength(-1);
		entity.setContent(speechInput);

		httppost.setEntity(entity);
		httppost.setHeader("User-Agent", USER_AGENT);
		httppost.setHeader("Content-Type", "audio/x-flac; rate=16000;");

		HttpResponse response;
		try {
			if (DEBUG_OUTPUT) {
				Log.w("GoogleSpeechRecog", "Starting request" + Thread.currentThread() + " ...");
			}
			response = httpclient.execute(httppost);
			if (DEBUG_OUTPUT) {
				Log.w("GoogleSpeechRecog", "Finished request" + Thread.currentThread() + "...");
			}
		} catch (ClientProtocolException cpe) {
			sendError(RecognizerCallback.ERROR_NONETWORK, "Executing the postrequest failed.");
			return null;
		} catch (IOException e) {
			sendError(RecognizerCallback.ERROR_NONETWORK, e.getMessage());
			return null;
		}

		BufferedReader reader;
		try {
			reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "UTF-8"));
		} catch (IOException e) {
			sendError(RecognizerCallback.ERROR_NONETWORK, e.getMessage());
			return null;
		}

		StringBuilder builder = new StringBuilder();
		try {
			for (String line = null; (line = reader.readLine()) != null;) {
				builder.append(line).append("\n");
			}
		} catch (IOException e) {
			sendError(RecognizerCallback.ERROR_NONETWORK, e.getMessage());
			return null;
		}

		String resp = builder.toString();
		if (resp.contains("NO_MATCH")) {
			sendResults(new ArrayList<String>());
			return null;
		}

		if (response.getStatusLine().getStatusCode() != 200) {
			sendError(RecognizerCallback.ERROR_API_CHANGED, "Statuscode was "
					+ response.getStatusLine().getStatusCode());
			return null;
		}

		JSONObject object;
		try {
			object = (JSONObject) new JSONTokener(resp).nextValue();
			if (object.getInt("status") == 0) {
				resturnJson = object.getJSONArray("hypotheses");
			}
		} catch (JSONException e) {
			sendError(RecognizerCallback.ERROR_API_CHANGED, "The response JSON-Object couldn't be parsed correct");
			return null;
		}
		return resturnJson;
	}

	private int encodeAudioInputStream(AudioInputStream sin, int maxRead, FLACEncoder flac, boolean useThreads)
			throws IOException, IllegalArgumentException {
		int frameSize = 2;
		int sampleSize = sin.getSampleSizeInBits();
		int bytesPerSample = sampleSize / 8;
		if (sampleSize % 8 != 0) {
			//end processing now
			throw new IllegalArgumentException("Unsupported Sample Size: size = " + sampleSize);
		}
		int channels = sin.getChannels();
		boolean bigEndian = sin.isBigEndian();
		boolean isSigned = sin.isSigned();
		byte[] samplesIn = new byte[maxRead];
		int samplesRead;
		int framesRead;
		int[] sampleData = new int[maxRead * channels / frameSize];
		int unencodedSamples = 0;
		int totalSamples = 0;
		while ((samplesRead = sin.read(samplesIn, 0, maxRead)) > 0) {
			framesRead = samplesRead / (frameSize);
			if (bigEndian) {
				for (int i = 0; i < framesRead * channels; i++) {
					int lower8Mask = 255;
					int temp = 0;
					int totalTemp = 0;
					for (int x = bytesPerSample - 1; x >= 0; x++) {
						int upShift = 8 * x;
						if (x == 0) {
							temp = ((samplesIn[bytesPerSample * i + x]) << upShift);
						} else {
							temp = ((samplesIn[bytesPerSample * i + x] & lower8Mask) << upShift);
						}
						totalTemp = totalTemp | temp;
					}
					if (!isSigned) {
						int reducer = 1 << (bytesPerSample * 8 - 1);
						totalTemp -= reducer;
					}
					sampleData[i] = totalTemp;
				}
			} else {
				for (int i = 0; i < framesRead * channels; i++) {
					int lower8Mask = 255;
					int temp = 0;
					int totalTemp = 0;
					for (int x = 0; x < bytesPerSample; x++) {
						int upShift = 8 * x;
						if (x == bytesPerSample - 1 && isSigned) {
							temp = ((samplesIn[bytesPerSample * i + x]) << upShift);
						} else {
							temp = ((samplesIn[bytesPerSample * i + x] & lower8Mask) << upShift);
						}
						totalTemp = totalTemp | temp;
					}
					if (!isSigned) {
						int reducer = 1 << (bytesPerSample * 8 - 1);
						totalTemp -= reducer;
					}
					sampleData[i] = totalTemp;
				}
			}
			if (framesRead > 0) {
				flac.addSamples(sampleData, framesRead);
				unencodedSamples += framesRead;
			}

			if (useThreads) {
				unencodedSamples -= flac.t_encodeSamples(unencodedSamples, false, 5);
			} else {
				unencodedSamples -= flac.encodeSamples(unencodedSamples, false);
			}
			totalSamples += unencodedSamples;
		}
		totalSamples += unencodedSamples;
		if (useThreads) {
			unencodedSamples -= flac.t_encodeSamples(unencodedSamples, true, 5);
		} else {
			unencodedSamples -= flac.encodeSamples(unencodedSamples, true);
		}
		return totalSamples;
	}

	@Override
	public boolean isAudioFormatSupported(AudioInputStream streamToCheck) {
		if (streamToCheck.getSampleRate() != 16000) {
			return false;
		}
		return true;
	}
}
