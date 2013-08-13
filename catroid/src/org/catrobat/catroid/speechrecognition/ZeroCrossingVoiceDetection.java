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

public class ZeroCrossingVoiceDetection extends VoiceDetection {

	private int crossZeroFaktor = 30;

	@Override
	public boolean isFrameWithVoice(double[] frame) {

		int crossings = countZeroCrossings(frame);
		if (crossings >= 5 && crossings <= crossZeroFaktor) {
			return true;
		}

		return false;
	}

	@Override
	public boolean isInitialized() {
		return true;
	}

	@Override
	public void resetState() {
		this.setSensibility(VoiceDetectionSensibility.NORMAL);
	}

	@Override
	public void setSensibility(VoiceDetectionSensibility Sensibility) {
		switch (Sensibility) {
			case HIGH:
				crossZeroFaktor = 40;
				break;
			case NORMAL:
				crossZeroFaktor = 30;
				break;
			case LOW:
				crossZeroFaktor = 20;
				break;
		}
	}

	private int countZeroCrossings(double[] frames) {
		double preFrame = frames[0];
		int crossings = 0;
		for (double value : frames) {
			if ((preFrame > 0 && value < 0) || preFrame < 0 && value > 0) {
				crossings++;
			}
			preFrame = value;
		}
		return crossings;
	}

}
