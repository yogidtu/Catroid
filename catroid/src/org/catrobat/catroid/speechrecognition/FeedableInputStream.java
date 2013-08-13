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

import android.util.Log;

public class FeedableInputStream extends InputStream {
	private final static String TAG = FeedableInputStream.class.getSimpleName();
	private final static int DEFAULT_MAX_BUFFERSIZE_REPEATABLE = 262144;
	private final static int DEFAULT_MAX_BUFFERSIZE_STREAM = 8192;
	private int readBytes = 0;
	private int mark;
	private byte[] audioBuffer;
	private int bufferEnd = 0;
	private boolean closeWhenEndReach = false;
	private boolean repeatable = false;
	private Object streamEndLock = new Object();

	public FeedableInputStream(byte[] preBuffer, boolean repeatable, int maxAudioBufferSpaceInBytes) {
		audioBuffer = new byte[maxAudioBufferSpaceInBytes];
		System.arraycopy(preBuffer, 0, audioBuffer, 0, preBuffer.length);
		bufferEnd = preBuffer.length;
		this.repeatable = repeatable;
	}

	public FeedableInputStream(byte[] preBuffer, boolean repeatable) {
		this(preBuffer, repeatable, repeatable ? DEFAULT_MAX_BUFFERSIZE_REPEATABLE : DEFAULT_MAX_BUFFERSIZE_STREAM);
	}

	public FeedableInputStream(boolean repeatable) {
		this(new byte[0], repeatable, repeatable ? DEFAULT_MAX_BUFFERSIZE_REPEATABLE : DEFAULT_MAX_BUFFERSIZE_STREAM);
	}

	public FeedableInputStream() {
		this(new byte[0], false, DEFAULT_MAX_BUFFERSIZE_STREAM);
	}

	@Override
	public int available() {
		return bufferEnd - readBytes;
	}

	public void resetToBeginning() {
		if (!repeatable) {
			return;
		}
		readBytes = 0;
	}

	@Override
	public int read() throws IOException {
		//Block if Necessary
		while (readBytes >= bufferEnd && !closeWhenEndReach) {
			synchronized (streamEndLock) {
				try {
					streamEndLock.wait();
				} catch (InterruptedException e) {
				}
			}
		}

		int value;
		synchronized (audioBuffer) {
			if (readBytes >= bufferEnd) {
				if (closeWhenEndReach) {
					value = -1;
				} else {
					Log.v(TAG, "WARNING, it is read but not blocked!:" + this);
					value = 0;
				}
			} else {
				value = audioBuffer[readBytes] & 0xFF;
				readBytes++;
			}
		}
		return value;
	}

	public void setNextBufferJunk(byte[] bufferChunk, int length) {
		synchronized (audioBuffer) {

			while (audioBuffer.length - readBytes < length) {
				byte[] temp = new byte[audioBuffer.length * 2];
				System.arraycopy(audioBuffer, 0, temp, 0, audioBuffer.length);
				audioBuffer = temp;
			}

			if (repeatable) {

				System.arraycopy(bufferChunk, 0, audioBuffer, bufferEnd, length);
				bufferEnd += bufferChunk.length;
			} else {
				if (readBytes > audioBuffer.length) {
					System.arraycopy(audioBuffer, readBytes, audioBuffer, 0, bufferEnd - readBytes);
					bufferEnd -= readBytes;
					readBytes = 0;
				}

				System.arraycopy(bufferChunk, 0, audioBuffer, bufferEnd, length);
				bufferEnd += bufferChunk.length;
			}
		}
		synchronized (streamEndLock) {
			streamEndLock.notifyAll();
		}
	}

	public void setNextBufferJunk(byte[] bufferChunk) {
		this.setNextBufferJunk(bufferChunk, bufferChunk.length);
	}

	public void setEndReachingCloses() {
		synchronized (streamEndLock) {
			closeWhenEndReach = true;
			streamEndLock.notifyAll();
		}
	}

	@Override
	public synchronized void reset() {
		readBytes = mark;
	}

	@Override
	public synchronized long skip(long byteCount) {
		if (byteCount <= 0) {
			return 0;
		}
		int prePosition = readBytes;
		readBytes = bufferEnd - readBytes < byteCount ? bufferEnd : (int) (readBytes + byteCount);
		return readBytes - prePosition;
	}

	@Override
	public boolean markSupported() {
		return false;
	}

	@Override
	public synchronized void mark(int readlimit) {
		mark = readBytes;
	}
}
