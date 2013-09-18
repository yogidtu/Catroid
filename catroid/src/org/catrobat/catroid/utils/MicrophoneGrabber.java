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

import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.ArrayList;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;

public class MicrophoneGrabber extends Thread {
	private static MicrophoneGrabber instance = null;

	public static final int audioEncoding = AudioFormat.ENCODING_PCM_16BIT;
	public static final int channelConfiguration = AudioFormat.CHANNEL_IN_MONO;
	public static final int sampleRate = 16000;
	public static final int frameByteSize = 512;
	public static final int bytesPerSample = 2;

	private ArrayList<PipedOutputStream> microphoneStreamList = new ArrayList<PipedOutputStream>();
	private boolean isRecording;
	public boolean isPaused = false;
	private AudioRecord audioRecord;
	private byte[] buffer;

	@Override
	public MicrophoneGrabber clone() {
		MicrophoneGrabber newGrabber = new MicrophoneGrabber();
		newGrabber.microphoneStreamList.addAll(this.microphoneStreamList);
		newGrabber.isRecording = false;
		newGrabber.isPaused = this.isPaused;
		newGrabber.audioRecord = this.audioRecord;
		MicrophoneGrabber.instance = newGrabber;
		return newGrabber;
	}

	private MicrophoneGrabber() {
		int recBufSize = AudioRecord.getMinBufferSize(sampleRate, channelConfiguration, audioEncoding); // need to be larger than size of a frame
		audioRecord = new AudioRecord(MediaRecorder.AudioSource.VOICE_RECOGNITION, sampleRate, channelConfiguration,
				audioEncoding, recBufSize);
		buffer = new byte[frameByteSize];
	}

	public static MicrophoneGrabber getInstance() {
		if (instance == null) {
			instance = new MicrophoneGrabber();
		}
		return instance;
	}

	public InputStream getMicrophoneStream() {
		PipedOutputStream outputPipe = new PipedOutputStream();
		PipedInputStream inputPipe;
		try {
			inputPipe = new PipedInputStream(outputPipe, frameByteSize * 10);
		} catch (IOException e) {
			return null;
		}
		synchronized (microphoneStreamList) {
			microphoneStreamList.add(outputPipe);
			if (!isRecording && !isPaused) {
				if (this.isAlive()) {
					isRecording = true;
				} else {
					MicrophoneGrabber newGrabber = this.clone();
					newGrabber.start();
				}
			}
		}
		return inputPipe;
	}

	@Override
	public void run() {

		isRecording = true;
		audioRecord.startRecording();

		while (isRecording) {
			int offset = 0;
			int shortRead = 0;

			while (offset < frameByteSize) {
				shortRead = audioRecord.read(buffer, offset, frameByteSize - offset);
				offset += shortRead;
			}

			for (PipedOutputStream outputPipe : microphoneStreamList) {
				try {
					outputPipe.write(buffer);
				} catch (IOException e) {
					try {
						microphoneStreamList.remove(outputPipe);
						outputPipe.close();
					} catch (IOException e1) {
					}
				}
			}
			if (microphoneStreamList.size() == 0) {
				isRecording = false;
			}
		}

		audioRecord.stop();
	}

	public boolean isRecording() {
		return this.isAlive() && isRecording;
	}

	public static void audioByteToDouble(byte[] samples, double[] result) {
		if (result.length != samples.length / bytesPerSample) {
			return;
		}

		final double amplification = 1000.0;
		for (int index = 0, floatIndex = 0; index < samples.length - bytesPerSample + 1; index += bytesPerSample, floatIndex++) {
			double sample = 0;
			for (int b = 0; b < bytesPerSample; b++) {
				int v = samples[index + b];
				if (b < bytesPerSample - 1 || bytesPerSample == 1) {
					v &= 0xFF;
				}
				sample += v << (b * 8);
			}
			double sample32 = amplification * (sample / 32768.0);
			result[floatIndex] = sample32;
		}
		return;
	}
}
