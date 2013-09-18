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
package org.catrobat.catroid.speechrecognition.recognizer;

import com.dtw.DTW;
import com.timeseries.TimeSeries;
import com.util.DistanceFunction;
import com.util.EuclideanDistance;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map.Entry;

public class LocalTemplateCluster {

	private static int maxClusterSize = 6;
	private static int maxClusterMatches = 5;
	private HashMap<TimeSeries, Double> templates = new HashMap<TimeSeries, Double>();
	private ArrayList<String> clusterLabels = new ArrayList<String>();

	public void addFingerprint(TimeSeries fingerprint) {
		synchronized (templates) {
			final DistanceFunction distanceFunktion = new EuclideanDistance();
			double minDistance = Double.MAX_VALUE;
			double highDistance = Double.MIN_NORMAL;
			double nearestThreshold = 0;
			TimeSeries nearestTemplate = null;
			TimeSeries farTemplate = null;

			Iterator<Entry<TimeSeries, Double>> it = templates.entrySet().iterator();
			while (it.hasNext()) {
				Entry<TimeSeries, Double> template = it.next();
				double distance = DTW.getWarpDistBetween(fingerprint, template.getKey(), distanceFunktion);
				double templateThreshold = template.getValue();
				if (distance < minDistance) {
					minDistance = distance;
					if (templateThreshold <= 1) {
						templateThreshold = distance;
					}
					nearestThreshold = (distance + templateThreshold) / 2;
					nearestTemplate = template.getKey();
				}
				if (distance > highDistance) {
					farTemplate = template.getKey();
				}

			}

			if (templates.size() >= maxClusterSize && farTemplate != null && farTemplate != nearestTemplate) {
				templates.remove(farTemplate);
			}
			if (nearestTemplate != null) {
				templates.put(nearestTemplate, nearestThreshold);
			}
			templates.put(fingerprint, nearestThreshold);
		}
	}

	public boolean belongsToCluster(ArrayList<String> remoteMatches) {
		for (String match : remoteMatches) {
			if (clusterLabels.contains(match.toLowerCase(Locale.getDefault()))) {
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
						copyClusterMatches.add(clusterLabels.get(i).toLowerCase(Locale.getDefault()));
						if (remoteMatches.size() > i) {
							copyClusterMatches.add(remoteMatches.get(i).toLowerCase(Locale.getDefault()));
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
		synchronized (templates) {

			if (!templates.containsKey(fingerprint)) {
				return false;
			}
			templates.remove(fingerprint);
		}
		return true;
	}

	public HashMap<TimeSeries, Double> getClusterFiles() {
		synchronized (templates) {
			return templates;
		}
	}

	public ArrayList<String> getClusterLabels() {
		return new ArrayList<String>(clusterLabels);
	}

	public void setLabel(ArrayList<String> labels) {
		clusterLabels.clear();
		for (String label : labels) {
			label.toLowerCase(Locale.getDefault());
			clusterLabels.add(label);
		}
	}
}
