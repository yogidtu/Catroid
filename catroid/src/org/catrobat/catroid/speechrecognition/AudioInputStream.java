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

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteOrder;

import android.media.AudioFormat;

public class AudioInputStream extends InputStream {

	InputStream internalStream;
	private int sampleSize;
	private int sampleRate;
	private int frameSize;
	private int channels;
	private boolean bigEndian;
	private boolean signed;
	private int encoding; //AudioFormat Encodign PCM_SIGNED

	public AudioInputStream(InputStream stream, int encoding, int channels, int sampleRate, int frameSize,
			ByteOrder endian, boolean signed) throws IllegalArgumentException {
		internalStream = stream;
		this.sampleRate = sampleRate;
		this.channels = channels;
		this.encoding = encoding;

		if (encoding == AudioFormat.ENCODING_PCM_8BIT) {
			this.sampleSize = 8;
		} else if (encoding == AudioFormat.ENCODING_PCM_16BIT) {
			this.sampleSize = 16;
		} else {
			throw new IllegalArgumentException("Unsupported encoding.");
		}

		this.frameSize = frameSize;
		this.bigEndian = (endian == ByteOrder.BIG_ENDIAN);
		this.signed = signed;
	}

	@Override
	public int read() throws IOException {
		return internalStream.read();
	}

	public int getSampleSizeInBits() {
		return sampleSize;
	}

	public int getSampleRate() {
		return sampleRate;
	}

	public int getFrameSize() {
		return frameSize;
	}

	public int getChannels() {
		return channels;
	}

	public int getEncoding() {
		return encoding;
	}

	public boolean isBigEndian() {
		return bigEndian;
	}

	public boolean isSigned() {
		return signed;
	}
}
