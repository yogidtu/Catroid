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
package org.catrobat.catroid.ui;

import org.catrobat.catroid.R;

import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

public class BottomBar {

	public static void setButtonsClickable(Activity activity, boolean clickable) {
		Log.d("FOREST", "BOTTOM BAR setButtonsClickable: " + activity.toString());
		Log.d("FOREST", "BOTTOM BAR: setButtonClickable: " + (clickable ? "true" : "false"));
		LinearLayout bottomBarLayout = (LinearLayout) activity.findViewById(R.id.bottom_bar);

		if (bottomBarLayout != null) {
			bottomBarLayout.findViewById(R.id.button_add).setClickable(clickable);
			bottomBarLayout.findViewById(R.id.button_play).setClickable(clickable);
		} else {
			Log.d("FOREST", "BOTTOM BAR: setButtonClickable: bottomBarLayout == null");
		}
	}

	public static void setButtonsVisible(Activity activity, boolean visible) {
		Log.d("FOREST", "BOTTOM BAR setButtonsVisible: " + activity.toString());
		Log.d("FOREST", "BOTTOM BAR setButtonsVisible: " + (visible ? "true" : "false"));
		LinearLayout bottomBarLayout = (LinearLayout) activity.findViewById(R.id.bottom_bar);

		if (bottomBarLayout != null) {
			int forLinearLayout = visible ? LinearLayout.VISIBLE : LinearLayout.GONE;
			int forView = visible ? View.VISIBLE : View.GONE;
			bottomBarLayout.findViewById(R.id.button_add).setVisibility(forLinearLayout);
			bottomBarLayout.findViewById(R.id.bottom_bar).setVisibility(forView);
			bottomBarLayout.findViewById(R.id.bottom_bar_separator).setVisibility(forView);
			bottomBarLayout.findViewById(R.id.button_play).setVisibility(forView);
		} else {
			Log.d("FOREST", "BOTTOM BAR: setButtonVisible: bottomBarLayout == null");
		}
	}

	public static void disablePlayButton(Activity activity) {
		Log.d("FOREST", "BOTTOM BAR disablePlayButton: " + activity.toString());
		LinearLayout bottomBarLayout = (LinearLayout) activity.findViewById(R.id.bottom_bar);

		if (bottomBarLayout != null) {
			bottomBarLayout.findViewById(R.id.button_add).setVisibility(LinearLayout.VISIBLE);
			bottomBarLayout.findViewById(R.id.bottom_bar).setVisibility(View.VISIBLE);
			bottomBarLayout.findViewById(R.id.bottom_bar_separator).setVisibility(View.GONE);
			bottomBarLayout.findViewById(R.id.button_play).setVisibility(View.GONE);
		} else {
			Log.d("FOREST", "BOTTOM BAR: disablePlayButton: bottomBarLayout == null");
		}
	}

	public static void disableAddButton(Activity activity) {
		Log.d("FOREST", "BOTTOM BAR disableAddButton: " + activity.toString());
		LinearLayout bottomBarLayout = (LinearLayout) activity.findViewById(R.id.bottom_bar);

		if (bottomBarLayout != null) {
			bottomBarLayout.findViewById(R.id.button_add).setVisibility(LinearLayout.GONE);
			bottomBarLayout.findViewById(R.id.button_play).setVisibility(LinearLayout.VISIBLE);
			bottomBarLayout.findViewById(R.id.bottom_bar).setVisibility(View.VISIBLE);
			bottomBarLayout.findViewById(R.id.bottom_bar_separator).setVisibility(View.GONE);
		} else {
			Log.d("FOREST", "BOTTOM BAR: disablePlayButton: bottomBarLayout == null");
		}
	}
}
