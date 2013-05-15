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

import org.catrobat.catroid.R;
import org.catrobat.catroid.common.Values;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.PixelFormat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.WindowManager;

/**
 * @author faxxe
 * 
 */
public class TutorialController {
	private boolean tutorialActive = false;
	private boolean tutorialPaused;
	Context context = null;
	Dialog dialog = null;
	private Cloud cloud;
	private TutorialOverlay tutorialOverlay;
	private WindowManager windowManager;
	private static WindowManager.LayoutParams dragViewParameters;
	private TutorialThread tutorialThread;
	private boolean activityChanged = false;

	private static final String PREF_TUTORIAL_LESSON = "INITIAL_TUTORIAL_LESSON";

	public void cleanAll() {
		Cloud.getInstance(context).clear();
		cloud = null;
		tutorialOverlay = null;
		windowManager = null;
		tutorialThread = null;
		context = null;
		dialog = null;
		Tutorial.getInstance(null).clear();
	}

	public TutorialController() {
		tutorialPaused = true;
	}

	public void setTutorialActive(boolean val) {
		this.tutorialActive = val;
	}

	public void notifyThread() {
		if (tutorialThread.isAlive()) {
			tutorialThread.notifyThread();
		}
	}

	public void stopThread() {
		if (tutorialThread.isAlive()) {
			tutorialThread.stopThread();
		}
	}

	public void removeOverlayFromWindow() {
		synchronized (tutorialThread) {
			windowManager.removeView(tutorialOverlay);
		}
	}

	public void setTutorialPaused(boolean val) {
		tutorialPaused = val;
	}

	public Activity correctActivity(Activity currentActivity) {
		if (currentActivity.getLocalClassName().compareTo("ui.ScriptActivity") == 0) {
			currentActivity = currentActivity.getParent();
		}
		if (currentActivity.getLocalClassName().compareTo("ui.CostumeActivity") == 0) {
			currentActivity = currentActivity.getParent();
		}
		if (currentActivity.getLocalClassName().compareTo("ui.SoundActivity") == 0) {
			currentActivity = currentActivity.getParent();
		}
		return currentActivity;
	}

	public void setupTutorialStartView() {
		Context context = Tutorial.getInstance(null).getActualContext();
		Resources resources = context.getResources();
		SurfaceObjectText tutorialHeadline = new SurfaceObjectText(tutorialOverlay);
		tutorialHeadline.setText("Tutorial");
		tutorialHeadline.setTextSize(40);
		tutorialHeadline.addToSurfaceOverlay();
		SurfaceObjectText welcomeText = new SurfaceObjectText(tutorialOverlay);
		welcomeText.setText(resources.getString(R.string.tutorial_mandatory_welcome));
		welcomeText.setPositionX(150);
		welcomeText.setPositionY(200);
		welcomeText.addToSurfaceOverlay();
		SurfaceObjecButton next = new SurfaceObjecButton(tutorialOverlay);
	}

	@SuppressLint("ParserError")
	public void startThread() {
		if (tutorialThread == null) {
			tutorialThread = new TutorialThread(this.context);
			tutorialThread.setName("TutorialThread");
			tutorialThread.startThread();
		} else {
			synchronized (tutorialThread) {
				if (activityChanged) {
					activityChanged = false;
					tutorialThread.notify();
				}
			}
		}
	}

	public void resumeTutorial() {
		if (!tutorialActive || !tutorialPaused) {
			return;
		}
		tutorialPaused = false;
		startThread();
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

	public void setupTutorialOverlay() {
		if (tutorialOverlay == null) {
			dragViewParameters = createLayoutParameters();
			windowManager = ((Activity) context).getWindowManager();
			tutorialOverlay = new TutorialOverlay(context);
			windowManager.addView(tutorialOverlay, dragViewParameters);
		} else {
			Log.i("tutorial", Thread.currentThread().getName() + ": Tutorial: Adding Overlay again!");
			dragViewParameters = createLayoutParameters();
			windowManager = ((Activity) context).getWindowManager();
			windowManager.addView(tutorialOverlay, dragViewParameters);
		}
	}

	public void setActivityChanged(Context newContext) {
		this.context = newContext;
		activityChanged = true;
	}

	public boolean getActivityChanged() {
		return activityChanged;
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
}
