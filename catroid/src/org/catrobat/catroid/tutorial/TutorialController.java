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

import org.catrobat.catroid.common.Values;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.PixelFormat;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
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
	private LessonCollection lessonCollection;
	private WindowManager windowManager;
	private static WindowManager.LayoutParams dragViewParameters;
	private TutorialThread tutorialThread;
	private boolean activityChanged = false;

	private static final String PREF_TUTORIAL_LESSON = "INITIAL_TUTORIAL_LESSON";

	public void cleanAll() {
		Cloud.getInstance(context).clear();
		cloud = null;
		tutorialOverlay = null;
		lessonCollection = null;
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

	public void initalizeLessonCollection() {
		setupTutorialOverlay();
		lessonCollection = new LessonCollection();
		lessonCollection.setTutorialOverlay(tutorialOverlay);
		lessonCollection = getMandatoryLesson();
	}

	public void initalizeLessons() {
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
		int possibleLesson = preferences.getInt(PREF_TUTORIAL_LESSON, 0);
		lessonCollection.setLastPossibleLessonNumber(possibleLesson);

		Log.i("tutorial", "The lesson out of the Preferences is: " + possibleLesson);

		lessonCollection.setTutorialOverlay(tutorialOverlay);
		lessonCollection.switchToLesson(possibleLesson);
	}

	public LessonCollection getMandatoryLesson() {
		lessonCollection.addLesson("Mandatory");

		return lessonCollection;
	}

	@SuppressLint("ParserError")
	public void startThread() {
		if (tutorialThread == null) {
			tutorialThread = new TutorialThread(this.context);
			tutorialThread.setName("TutorialThread");
			tutorialThread.setLessonCollection(lessonCollection);
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

	public void setSharedPreferences() {
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
		SharedPreferences.Editor sharedPreferencesEditor = preferences.edit();
		sharedPreferencesEditor.putInt(PREF_TUTORIAL_LESSON, lessonCollection.getLastPossibleLessonNumber());
		sharedPreferencesEditor.commit();
	}

	public void setDialog(Dialog dialog) {
		this.dialog = dialog;
	}

	public Dialog getDialog() {
		return this.dialog;
	}

	public void stepBackward() {
		//		tutorialThread.setInterruptRoutine(ACTIONS.REWIND);
		tutorialThread.setInterrupt(true);
		tutorialThread.notifyThread();

		tutorialThread.setInterrupt(false);
		tutorialThread.notifyThread();
	}

	public void stepForward() {
		tutorialThread.setInterrupt(true);
		tutorialThread.notifyThread();

		tutorialThread.setInterrupt(false);
		tutorialThread.notifyThread();
	}

	public void stopButtonTutorial() {
		this.dialog = null;
		cleanAll();
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

	public boolean dispatchTouchEvent(MotionEvent ev) {
		Activity activity = correctActivity((Activity) context);
		boolean retval;
		retval = activity.dispatchTouchEvent(ev);
		return retval;
	}

	public void setActivityChanged(Context newContext) {
		this.context = newContext;
		activityChanged = true;
	}

	public boolean getActivityChanged() {
		return activityChanged;
	}
}
