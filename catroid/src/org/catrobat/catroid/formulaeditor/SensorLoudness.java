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

import java.util.ArrayList;

import org.catrobat.catroid.utils.MicrophoneGrabber;
import org.catrobat.catroid.utils.MicrophoneGrabber.microphoneListener;

public class SensorLoudness implements microphoneListener {

	private static SensorLoudness instance = null;
	private final double SCALE_RANGE = 100d;

	private final double MAX_AMP_VALUE = 1000.0d;
	private ArrayList<SensorCustomEventListener> listenerList = new ArrayList<SensorCustomEventListener>();
	private float lastValue = 0f;
	private float currentValue = 0;

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
				MicrophoneGrabber.getInstance().registerListener(this);
			}
		}
		return true;
	}

	public void unregisterListener(SensorCustomEventListener listener) {
		synchronized (listenerList) {
			if (listenerList.contains(listener)) {

				listenerList.remove(listener);
			}
			if (listenerList.size() == 0) {
				MicrophoneGrabber.getInstance().unregisterListener(this);
			}
		}
	}

	@Override
	public void onMicrophoneData(byte[] recievedBuffer) {

		double[] signal = MicrophoneGrabber.audioByteToDouble(recievedBuffer);
		currentValue = 0;
		for (double sample : signal) {
			if (currentValue < Math.abs(sample)) {
				currentValue = Math.abs((float) sample);
			}
		}

		float[] loudness = new float[1];
		loudness[0] = (float) (SCALE_RANGE / MAX_AMP_VALUE) * currentValue;
		if (lastValue != loudness[0] && loudness[0] != 0f) {
			lastValue = loudness[0];
			SensorCustomEvent event = new SensorCustomEvent(Sensors.LOUDNESS, loudness);
			for (SensorCustomEventListener listener : listenerList) {
				listener.onCustomSensorChanged(event);
			}
		}
	}
}
