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

import java.util.LinkedList;
import java.util.Queue;

public class VoiceActivityDetection {
	/*
	 * Implemented after
	 * Sangwan, A.; Chiranth, M.C.; Jamadagni, H.S.; Sah, R.; Venkatesha Prasad, R.; Gaurav, V.,
	 * "VAD techniques for real-time speech transmission on the Internet"
	 * High Speed Networks and Multimedia Communications 5th IEEE International Conference, pp.46,50, 2002
	 */

	public static final float SENSIBILITY_LOW = 1.8f;
	public static final float SENSIBILITY_NORMAL = 1.5f;
	public static final float SENSIBILITY_HIGH = 1.3f;

	public final int samplesPerFrame = 256;
	private double energyThreshold = 0.0d;
	private int framesForThreshold = 20;
	private float weightFactor = 0.1f;

	private float thresholdFactor = 1.2f;
	private Queue<Double> recentEnergyRingQueqe = new LinkedList<Double>();

	public VoiceActivityDetection() {
		this.resetState();
	}

	public boolean isFrameWithVoice(double[] frame) {
		double frameEnergy = calculateEnergyOfFrame(frame);

		if (recentEnergyRingQueqe.size() < framesForThreshold) {
			recentEnergyRingQueqe.add(frameEnergy);
			return false;
		}

		if (energyThreshold == 0.0) {
			for (double energy : recentEnergyRingQueqe) {
				energyThreshold += energy;
			}
			energyThreshold /= recentEnergyRingQueqe.size();
		}

		//update Threshold
		energyThreshold = (1 - weightFactor) * energyThreshold + weightFactor * frameEnergy;

		if (frameEnergy > thresholdFactor * energyThreshold) {
			return true;
		}

		int crossings = countZeroCrossings(frame);
		if (crossings >= 5 && crossings <= 20) {
			return true;
		}

		//adaption of p
		double var_old = getVarianceOfRecentInactiveFrames();
		recentEnergyRingQueqe.add(frameEnergy);
		double var_new = getVarianceOfRecentInactiveFrames();

		double reltion = var_new / var_old;
		if (reltion >= 1.25) {
			weightFactor = 0.25f;
		} else if (reltion >= 1.10) {
			weightFactor = 0.2f;
		} else if (reltion >= 1) {
			weightFactor = 0.15f;
		} else {
			weightFactor = 0.1f;
		}

		recentEnergyRingQueqe.remove();
		return false;
	}

	private double calculateEnergyOfFrame(double[] frame) {
		double sum = 0.0d;
		for (double sample : frame) {
			sum += (sample * sample);
		}

		return sum / frame.length;
	}

	private double getVarianceOfRecentInactiveFrames() {
		double mean = 0.0d;
		for (double energy : recentEnergyRingQueqe) {
			mean += energy;
		}
		mean /= recentEnergyRingQueqe.size();

		double sum = 0.0d;
		for (double energy : recentEnergyRingQueqe) {
			sum += (energy - mean) * (energy - mean);
		}

		return sum / (recentEnergyRingQueqe.size() - 1);
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

	public void resetState() {
		energyThreshold = 0.0d;
		weightFactor = 0.1f;
		recentEnergyRingQueqe.clear();
		setSensibility(SENSIBILITY_NORMAL);
	}

	public void setSensibility(float faktor) {
		thresholdFactor = faktor;
	}
}
