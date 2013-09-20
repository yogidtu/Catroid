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

import android.content.Context;
import android.media.AudioRecord;
import android.media.MediaRecorder;

import org.catrobat.catroid.utils.MicrophoneGrabber;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InvalidObjectException;
import java.util.Random;

public class SimulatedAudioRecord extends AudioRecord {

	private BufferedInputStream dataStream;
	private int alreadyReadBytes = 0;
	private boolean isRecording = false;
	private boolean noiseGenerator = false;
	private static final int EMULATORSAMPLERATE = 8000;

	public SimulatedAudioRecord() {
		super(MediaRecorder.AudioSource.MIC, EMULATORSAMPLERATE, MicrophoneGrabber.CHANNELCONFIGURATION,
				MicrophoneGrabber.AUDIOENCODING, AudioRecord.getMinBufferSize(EMULATORSAMPLERATE,
						MicrophoneGrabber.CHANNELCONFIGURATION, MicrophoneGrabber.AUDIOENCODING));
		super.release();
		noiseGenerator = true;
	}

	public SimulatedAudioRecord(String mockAudioFilePath) throws IOException {
		super(MediaRecorder.AudioSource.MIC, EMULATORSAMPLERATE, MicrophoneGrabber.CHANNELCONFIGURATION,
				MicrophoneGrabber.AUDIOENCODING, AudioRecord.getMinBufferSize(EMULATORSAMPLERATE,
						MicrophoneGrabber.CHANNELCONFIGURATION, MicrophoneGrabber.AUDIOENCODING));
		super.release();

		if (!mockAudioFilePath.endsWith(".wav")) {
			throw new InvalidObjectException("Wrong fileformat.");
		}

		alreadyReadBytes = 44;
		File inputMockFile = new File(mockAudioFilePath);
		InputStream dataInputStream = new FileInputStream(inputMockFile);
		dataStream = new BufferedInputStream(dataInputStream, 8000);
		dataStream.skip(alreadyReadBytes);
	}

	public SimulatedAudioRecord(int resourceFileId, Context context) throws IOException {
		super(MediaRecorder.AudioSource.MIC, EMULATORSAMPLERATE, MicrophoneGrabber.CHANNELCONFIGURATION,
				MicrophoneGrabber.AUDIOENCODING, AudioRecord.getMinBufferSize(EMULATORSAMPLERATE,
						MicrophoneGrabber.CHANNELCONFIGURATION, MicrophoneGrabber.AUDIOENCODING));
		super.release();

		alreadyReadBytes = 44;
		InputStream dataInputStream = context.getResources().openRawResource(resourceFileId);
		dataStream = new BufferedInputStream(dataInputStream, 8000);
		dataStream.skip(alreadyReadBytes);
	}

	public boolean isMockRecording() {
		return this.isRecording;
	}

	@Override
	public void startRecording() {
		alreadyReadBytes = 44;
		isRecording = true;
		return;
	}

	@Override
	public void stop() {
		try {
			if (!noiseGenerator) {
				dataStream.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		isRecording = false;
		return;
	}

	@Override
	public int read(byte[] audioData, int offsetInBytes, int sizeInBytes) {

		int readBytes = 0;
		if ((audioData == null) || (offsetInBytes < 0) || (sizeInBytes < 0)
				|| (offsetInBytes + sizeInBytes > audioData.length)) {
			return ERROR_BAD_VALUE;
		}

		if (noiseGenerator) {
			byte[] noise = new byte[sizeInBytes];
			Random noiseCalculator = new Random();
			noiseCalculator.nextBytes(noise);
			System.arraycopy(noise, 0, audioData, offsetInBytes, sizeInBytes);
			return sizeInBytes;
		}

		try {
			while (readBytes < sizeInBytes) {
				int read = dataStream.read(audioData, offsetInBytes + readBytes, sizeInBytes - readBytes);
				if (read < 0) {
					this.stop();
					return read;
				}
				if (read == 0) {
					break;
				}
				readBytes += read;
			}
		} catch (IOException e) {
			e.printStackTrace();
			this.stop();
			return -1;
		}
		return readBytes;
	}

}
