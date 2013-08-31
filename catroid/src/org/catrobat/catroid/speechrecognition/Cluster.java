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

import java.util.ArrayList;

import android.util.Log;

import com.timeseries.TimeSeries;

public class Cluster {

	private static final String TAG = Cluster.class.getSimpleName();
	private static int maxClusterSize = 5;
	private static int maxClusterMatches = 5;

	private ArrayList<TimeSeries> fingerprintFiles = new ArrayList<TimeSeries>();
	private ArrayList<String> clusterLabels = new ArrayList<String>();

	public void addFingerprint(TimeSeries fingerprint) {
		fingerprintFiles.add(fingerprint);
		if (fingerprintFiles.size() > maxClusterSize) {
			fingerprintFiles.remove(0);
		}
	}

	public boolean belongsToCluster(ArrayList<String> remoteMatches) {
		for (String match : remoteMatches) {
			if (clusterLabels.contains(match.toLowerCase())) {
				return true;
			}
		}
		return false;
	}

	public boolean mergeResults(ArrayList<String> remoteMatches) {
		for (String itemSearch : clusterLabels) {
			for (String remoteMatch : remoteMatches) {
				if (itemSearch.contains(remoteMatch)) {
					int duplicateIndex = clusterLabels.indexOf(itemSearch);
					ArrayList<String> copyClusterMatches = new ArrayList<String>();
					copyClusterMatches.add(itemSearch);
					clusterLabels.remove(duplicateIndex);
					remoteMatches.remove(remoteMatch);
					for (int i = 0; i < maxClusterMatches / 2; i++) {
						copyClusterMatches.add(clusterLabels.get(i).toLowerCase());
						if (remoteMatches.size() > i) {
							copyClusterMatches.add(remoteMatches.get(i).toLowerCase());
						}
					}
					clusterLabels = copyClusterMatches;
					return true;
				}
			}
		}
		return false;
	}

	public boolean removeFingerprint(TimeSeries fingerprint) {
		if (!fingerprintFiles.contains(fingerprint)) {
			return false;
		}
		fingerprintFiles.remove(fingerprint);
		return true;
	}

	public ArrayList<TimeSeries> getClusterFiles() {
		return new ArrayList<TimeSeries>(fingerprintFiles);
	}

	public ArrayList<String> getClusterLabels() {
		return new ArrayList<String>(clusterLabels);
	}

	public void setLabel(ArrayList<String> labels) {
		clusterLabels.clear();
		for (String label : labels) {
			label.toLowerCase();
			clusterLabels.add(label);
		}
		Log.v(TAG, "Set labels to " + clusterLabels);
	}
}
