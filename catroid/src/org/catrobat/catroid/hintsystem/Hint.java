/**
 *  Catroid: An on-device visual programming system for Android devices
 *  Copyright (C) 2010-2012 The Catrobat Team
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
package org.catrobat.catroid.hintsystem;

import java.util.ArrayList;

import org.catrobat.catroid.common.Values;
import org.catrobat.catroid.stage.StageActivity;

import android.app.Activity;
import android.content.Context;
import android.graphics.PixelFormat;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.WindowManager;

/**
 * @author amore
 * 
 */
public class Hint {

	private static boolean debugMode = true;

	private static Hint hint = new Hint();
	public static boolean welcome = false;
	private static Context context;
	private WindowManager windowManager;
	private HintOverlay hintOverlay;

	static HintController controller = new HintController();

	private Hint() {

	}

	public static Hint getInstance() {

		if (hint == null) {
			hint = new Hint();
		}
		return hint;
	}

	public static void setContext(Context con) {
		context = con;
		controller.setContext(context);
	}

	public void overlayHint() {
		float density = context.getResources().getDisplayMetrics().density;
		ScreenParameters screenparameters = ScreenParameters.getInstance();
		screenparameters.setDensityParameter(density);

		WindowManager.LayoutParams windowParameters = createLayoutParameters();
		windowManager = ((Activity) context).getWindowManager();
		hintOverlay = new HintOverlay(context);
		windowManager.addView(hintOverlay, windowParameters);

	}

	public void removeHint() {
		windowManager = ((Activity) context).getWindowManager();
		windowManager.removeViewImmediate(hintOverlay);
		hintOverlay = null;
		System.gc();
		System.runFinalization();
	}

	public WindowManager.LayoutParams createLayoutParameters() {
		WindowManager.LayoutParams windowParameters = new WindowManager.LayoutParams();
		windowParameters.gravity = Gravity.CENTER_VERTICAL | Gravity.RIGHT;
		windowParameters.height = Values.SCREEN_HEIGHT;
		windowParameters.width = Values.SCREEN_WIDTH;
		windowParameters.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
				| WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;
		windowParameters.format = PixelFormat.TRANSLUCENT;
		return windowParameters;
	}

	public static ArrayList<HintObject> getHints() {
		ArrayList<HintObject> hints = controller.getHints();
		return hints;
	}

	public int getScreenHeight() {
		int screenHeight = 0;
		DisplayMetrics deviceDisplayMetrics = new DisplayMetrics();

		windowManager.getDefaultDisplay().getMetrics(deviceDisplayMetrics);

		screenHeight = deviceDisplayMetrics.heightPixels;
		return screenHeight;
	}

	public int getScreenWidth() {
		int screenWidth = 0;
		DisplayMetrics deviceDisplayMetrics = new DisplayMetrics();

		windowManager.getDefaultDisplay().getMetrics(deviceDisplayMetrics);

		screenWidth = deviceDisplayMetrics.widthPixels;
		return screenWidth;
	}

	public static boolean isActive(Activity activity) {
		if (debugMode) {
			return true;
		}
		String preferenceName = getPreferenceName(activity);
		return controller.getSharedPreferencesIsHintActive(preferenceName);
	}

	private static String getPreferenceName(Activity activity) {
		String preferenceName = "";
		switch (controller.checkActivity()) {
			case 0:
				preferenceName = "PREF_HINT_MAINMENU_ACTIVE";
				break;
			case 1:
				preferenceName = "PREF_HINT_PROJECT_ACTIVE";
				break;
			case 2:
				preferenceName = "PREF_HINT_MYPROJECTS_ACTIVE";
				break;
			case 3:
				preferenceName = "PREF_HINT_PROGRAMMENU_ACTIVE";
				break;
			case 4:
				preferenceName = getFragmentPreferenceName();
				break;
			case 5:
				preferenceName = "PREF_HINT_SETTINGS_ACTIVE";
				break;
			case 6:
				if (((StageActivity) activity).getStageDialog().isShowing()) {
					preferenceName = "PREF_HINT_STAGE_ACTIVE";
					break;
				} else {
					preferenceName = "PREF_HINT_STAGEDIALOG_ACTIVE";
					break;
				}
			case 7:
				preferenceName = "PREF_HINT_SOUNDRECORDER_ACTIVE";
				break;
		}

		return preferenceName;
	}

	public static String getFragmentPreferenceName() {
		String preferenceName = "";

		switch (controller.checkFragment()) {
			case 0:
				preferenceName = "PREF_HINT_BRICKCATEGORY_ACTIVE";
				break;
			case 1:
				preferenceName = "PREF_HINT_ADDBRICK_ACTIVE";
				break;
			case 2:
				preferenceName = "PREF_HINT_FORMULAEDITOR_ACTIVE";
				break;
			case 3:
				preferenceName = "PREF_HINT_SCRIPTS_ACTIVE";
				break;
			case 4:
				preferenceName = "PREF_HINT_LOOKS_ACTIVE";
				break;
			case 5:
				preferenceName = "PREF_HINT_SOUNDS_ACTIVE";
				break;
		}
		return preferenceName;
	}
}
