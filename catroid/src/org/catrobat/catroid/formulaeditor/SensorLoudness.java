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
package org.catrobat.catroid.formulaeditor;

import org.catrobat.catroid.utils.MicrophoneGrabber;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.ArrayList;

public class SensorLoudness {

	private static SensorLoudness instance = null;
	private static final double SCALERANGE = 100d;
	private static final long UPDATEINTERVALMS = 50;
	private static final double MAXAMP = 1000.0d;

	private ArrayList<SensorCustomEventListener> listenerList = new ArrayList<SensorCustomEventListener>();
	private float lastValue = 0f;
	private float currentValue = 0;
	private int analyseFrameSize = MicrophoneGrabber.FRAMEBYTESIZE * 5;
	private BufferedInputStream microphoneInput = null;

	double[] signal = new double[MicrophoneGrabber.FRAMEBYTESIZE * 5 / MicrophoneGrabber.BYTESPERSAMPLE];

	private SensorLoudness() {
	}

	public static SensorLoudness getSensorLoudness() {
		if (instance == null) {
			instance = new SensorLoudness();
		}
		return instance;
	}

	public boolean registerListener(SensorCustomEventListener listener) {
		synchronized (listenerList) {
			if (listenerList.contains(listener)) {
				return true;
			}
			listenerList.add(listener);
			if (listenerList.size() == 1) {
				microphoneInput = new BufferedInputStream(MicrophoneGrabber.getInstance().getMicrophoneStream());
				new Thread(new Runnable() {
					@Override
					public void run() {
						byte[] recievedBuffer = new byte[analyseFrameSize];
						while (microphoneInput != null) {
							try {
								microphoneInput.read(recievedBuffer, 0, recievedBuffer.length);
								microphoneInput.skip(microphoneInput.available());
							} catch (Exception e) {
								try {
									microphoneInput.close();
								} catch (Exception e1) {
								}
								microphoneInput = new BufferedInputStream(MicrophoneGrabber.getInstance()
										.getMicrophoneStream());
							}

							updateLoudnessValue(recievedBuffer);

							try {
								Thread.sleep(UPDATEINTERVALMS);
							} catch (InterruptedException e) {
							}
						}
					}
				}).start();
			}
		}
		return true;
	}

	public void unregisterListener(SensorCustomEventListener listener) {
		synchronized (listenerList) {
			if (!listenerList.contains(listener)) {
				return;
			}
			listenerList.remove(listener);
			if (listenerList.size() == 0 && microphoneInput != null) {
				try {
					microphoneInput.close();
				} catch (IOException e) {
				}
				microphoneInput = null;
			}
		}
	}

	private void updateLoudnessValue(byte[] audioBuffer) {
		MicrophoneGrabber.audioByteToDouble(audioBuffer, signal);
		currentValue = 0;
		for (double sample : signal) {
			if (currentValue < Math.abs(sample)) {
				currentValue = Math.abs((float) sample);
			}
		}

		float[] loudness = new float[1];
		loudness[0] = (float) (SCALERANGE / MAXAMP) * currentValue;
		if (lastValue != loudness[0] && loudness[0] != 0f) {
			lastValue = loudness[0];
			SensorCustomEvent event = new SensorCustomEvent(Sensors.LOUDNESS, loudness);
			for (SensorCustomEventListener listener : listenerList) {
				listener.onCustomSensorChanged(event);
			}
		}
	}
}
