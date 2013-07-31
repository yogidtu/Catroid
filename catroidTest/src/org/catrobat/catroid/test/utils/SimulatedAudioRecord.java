package org.catrobat.catroid.test.utils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InvalidObjectException;
import java.util.Random;

import org.catrobat.catroid.utils.MicrophoneGrabber;

import android.content.Context;
import android.media.AudioRecord;
import android.media.MediaRecorder;

public class SimulatedAudioRecord extends AudioRecord {

	private BufferedInputStream dataStream;
	private int alreadyReadBytes = 0;
	private boolean isRecording = false;
	private boolean noiseGenerator = false;

	public SimulatedAudioRecord() throws IOException {
		super(MediaRecorder.AudioSource.VOICE_RECOGNITION, MicrophoneGrabber.sampleRate,
				MicrophoneGrabber.channelConfiguration, MicrophoneGrabber.audioEncoding, AudioRecord.getMinBufferSize(
						MicrophoneGrabber.sampleRate, MicrophoneGrabber.channelConfiguration,
						MicrophoneGrabber.audioEncoding));
		noiseGenerator = true;
	}

	public SimulatedAudioRecord(String mockAudioFilePath) throws IOException {
		super(MediaRecorder.AudioSource.VOICE_RECOGNITION, MicrophoneGrabber.sampleRate,
				MicrophoneGrabber.channelConfiguration, MicrophoneGrabber.audioEncoding, AudioRecord.getMinBufferSize(
						MicrophoneGrabber.sampleRate, MicrophoneGrabber.channelConfiguration,
						MicrophoneGrabber.audioEncoding));

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
		super(MediaRecorder.AudioSource.VOICE_RECOGNITION, MicrophoneGrabber.sampleRate,
				MicrophoneGrabber.channelConfiguration, MicrophoneGrabber.audioEncoding, AudioRecord.getMinBufferSize(
						MicrophoneGrabber.sampleRate, MicrophoneGrabber.channelConfiguration,
						MicrophoneGrabber.audioEncoding));

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
		} catch (IOException e) {
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
