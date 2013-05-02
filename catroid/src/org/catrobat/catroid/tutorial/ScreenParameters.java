/**
 *  Catroid: An on-device graphical programming language for Android devices
 *  Copyright (C) 2010  Catroid development team 
 *  (<http://code.google.com/p/catroid/wiki/Credits>)
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.catrobat.catroid.tutorial;

import android.util.Log;

/**
 * @author drab
 * 
 */
public class ScreenParameters {
	private static ScreenParameters screenParameters = new ScreenParameters();

	private enum DENSITY {
		LDPI, MDPI, HDPI, XDPI
	}

	private DENSITY density;

	private ScreenParameters() {

	}

	public static ScreenParameters getInstance() {
		if (screenParameters == null) {
			screenParameters = new ScreenParameters();
		}
		return screenParameters;
	}

	public void setScreenParameters() {
		setDensityParameter(Tutorial.getInstance(null).getDensity());
	}

	private void setDensityParameter(float density) {
		if (density < 1.0f) {
			this.density = DENSITY.LDPI;
		} else if (density == 1.0f) {
			this.density = DENSITY.MDPI;
		} else if (density == 1.5f) {
			this.density = DENSITY.HDPI;
		} else if (density > 1.5f) {
			this.density = DENSITY.XDPI;
		}
	}

	public int setCoordinatesToDensity(int value, boolean width) {
		if (value > 100) {
			value = 100;
		}

		if (width) {
			Log.i("state", "DEN: Old x is: " + value);
			value = (int) ((value / 100.0f) * Tutorial.getInstance(null).getScreenWidth());
			Log.i("state", "DEN: New x is: " + value);
		} else {
			Log.i("state", "DEN: Old y is: " + value);
			value = (int) ((value / 100.0f) * Tutorial.getInstance(null).getScreenHeight());
			Log.i("state", "DEN: Old y is: " + value);
		}

		return value;
	}

	public static ScreenParameters getScreenParameters() {
		return screenParameters;
	}

	public DENSITY getDensity() {
		return density;
	}
}