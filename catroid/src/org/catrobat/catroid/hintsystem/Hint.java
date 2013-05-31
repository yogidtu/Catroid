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

import android.app.Activity;
import android.content.Context;
import android.graphics.PixelFormat;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.WindowManager;

/**
 * @author amore
 * 
 */
public class Hint {

	private static Hint hint = new Hint();
	private static Context context;
	private WindowManager windowManager;
	private HintOverlay hintOverlay;

	static HintController controller = new HintController();

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
		addToolTipButtons();
	}

	private void addToolTipButtons() {
		switch (controller.checkActivity()) {
			case 0:
				hintOverlay.addToolTipButtonsToMainMenuActivity();
				break;
			case 1:
				hintOverlay.addToolTipButtonsToProjectActivity();
				break;

		}

	}

	public void removeHint() {

		windowManager = ((Activity) context).getWindowManager();
		windowManager.removeViewImmediate(hintOverlay);
		removeToolTipButtons();
		hintOverlay = null;
		System.gc();
		System.runFinalization();

	}

	public void removeToolTipButtons() {
		switch (controller.checkActivity()) {
			case 0:
				hintOverlay.removeMainMenuActivityToolTipButtons();
				break;
			case 1:
				hintOverlay.removeProjectActivityToolTipButtons();
				break;
		}

	}

	public boolean setHintPosition(int x, int y, String text) {
		return hintOverlay.setPostions(x, y, text);

	}

	public WindowManager.LayoutParams createLayoutParameters() {
		WindowManager.LayoutParams windowParameters = new WindowManager.LayoutParams();
		windowParameters.gravity = Gravity.BOTTOM | Gravity.RIGHT;
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

	public boolean dispatchTouchEvent(MotionEvent ev) {
		return controller.dispatchTouchEvent(ev);

	}

	public HintOverlay getHintOverlay() {
		return hintOverlay;
	}

}
