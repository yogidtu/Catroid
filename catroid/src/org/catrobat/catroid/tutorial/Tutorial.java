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

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.util.Log;

/**
 * @author faxxe
 * 
 */
public class Tutorial {
	public static final boolean DEBUG = true;
	private static Tutorial tutorial = new Tutorial();
	private boolean tutorialActive;
	private static Context context;

	TutorialController tutorialController = new TutorialController();

	private Tutorial() {

	}

	public void clear() {
		tutorial = null;
		context = null;
		tutorialController = null;
	}

	public static Tutorial getInstance(Context onlyPassContextWhenActivityChanges) {
		if (tutorial == null) {
			tutorial = new Tutorial();
		}
		if (onlyPassContextWhenActivityChanges != null) {
			tutorial.setContextIfActivityHasChanged(onlyPassContextWhenActivityChanges);
		}
		return tutorial;
	}

	public void setContextIfActivityHasChanged(Context con) {
		if (con != null && context != con) {
			context = con;
			tutorialController.setActivityChanged(context);
		}
	}

	private void setTutorialActive() {
		tutorialActive = true;
		tutorialController.setTutorialActive(true);
	}

	private void setTutorialNotActive() {
		tutorialActive = false;
		tutorialController.setTutorialActive(false);
	}

	public void startTutorial() {
		//ProjectManager.getInstance().initializeDefaultProject(context);
		//initialize tutorial project
		Activity currentActivity = (Activity) context;

		currentActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		ScreenParameters.getInstance().setScreenParameters();

		setTutorialActive();
		tutorialController.setupTutorialOverlay();
		tutorialController.setupTutorialStartView();
		//		tutorialController.initalizeLessonCollection();
		//		tutorialController.initalizeLessons();
		tutorialController.resumeTutorial();
		return;
	}

	public void destroyTutorial() {
		tutorial = null;
	}

	public void stopButtonTutorial() {
		stopTutorial();
		clear();
		System.gc();
		Log.i("tutorial", "Tutorial.java: stopButtonTutorial: calling finalisation");
		System.runFinalization();
	}

	public void stopTutorial() {
		pauseTutorial();
		setTutorialNotActive();
		tutorialController.stopThread();
		Activity currentActivity = (Activity) context;
		currentActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
	}

	public void pauseTutorial() {
		Log.i("tutorial", "pause Tutorial");
		if (!tutorialActive) {
			return;
		}
		tutorialController.setTutorialPaused(true);
		tutorialActive = true;
		tutorialController.removeOverlayFromWindow();
	}

	public void resumeTutorial() {
		if (tutorialActive) {
			tutorialController.resumeTutorial();
		}
	}

	public Context getActualContext() {
		return context;
	}

	public void setNotification(String notification) {
		Log.i("drab", "TutorialS: " + notification);
		tutorialController.notifyThread();
	}

	public boolean isActive() {
		return tutorialActive;
	}

	public float getDensity() {
		return context.getResources().getDisplayMetrics().density;
	}

	public int getScreenHeight() {
		return tutorialController.getScreenHeight();
	}

	public int getScreenWidth() {
		return tutorialController.getScreenWidth();
	}
}