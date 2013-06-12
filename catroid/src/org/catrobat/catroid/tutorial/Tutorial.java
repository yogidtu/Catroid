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

import java.io.File;
import java.util.ArrayList;

import org.catrobat.catroid.ProjectManager;
import org.catrobat.catroid.R;
import org.catrobat.catroid.common.Constants;
import org.catrobat.catroid.common.LookData;
import org.catrobat.catroid.ui.fragment.LookFragment;
import org.catrobat.catroid.utils.UtilCamera;

import android.app.Activity;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;

/**
 * @author faxxe
 * 
 */
public class Tutorial {
	public static final boolean DEBUG = true;
	private static Tutorial tutorial = new Tutorial();
	private boolean tutorialActive;
	private static Context context;
	private ArrayList<LookData> lookDataList;
	private LookData selectedLookData;

	private String paintroidIntentApplicationName = "org.catrobat.paintroid";
	private String paintroidIntentActivityName = "org.catrobat.paintroid.MainActivity";

	TutorialController tutorialController = new TutorialController();

	private Tutorial() {

	}

	private void selectImageFromCamera() {
		Object lookFromCameraUri = UtilCamera
				.getDefaultLookFromCameraUri(context.getString(R.string.default_look_name));

		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		//intent.putExtra(MediaStore.EXTRA_OUTPUT, lookFromCameraUri);
		Intent chooser = Intent.createChooser(intent, context.getString(R.string.select_look_from_camera));
		((Activity) context).startActivityForResult(chooser, LookFragment.REQUEST_TAKE_PICTURE);
	}

	private void loadPaintroidImageIntoCatroid(Intent intent) {
		Bundle bundle = intent.getExtras();
		String pathOfPaintroidImage = bundle.getString(Constants.EXTRA_PICTURE_PATH_POCKET_PAINT);

		//int[] imageDimensions = ImageEditing.getImageDimensions(pathOfPaintroidImage);
		//if (imageDimensions[0] < 0 || imageDimensions[1] < 0) {
		//Utils.showErrorDialog(getActivity(), this.getString(R.string.error_load_image));
		//return;
		//}

		//String actualChecksum = Utils.md5Checksum(new File(pathOfPaintroidImage));

		// If look changed --> saving new image with new checksum and changing lookData
		//if (!selectedLookData.getChecksum().equalsIgnoreCase(actualChecksum)) {
		//			String oldFileName = selectedLookData.getLookFileName();
		String newFileName = "newpaintroidpic";

		//HACK for https://github.com/Catrobat/Catroid/issues/81
		if (!newFileName.endsWith(".png")) {
			newFileName = newFileName + ".png";
		}

		//String projectName = ProjectManager.getInstance().getCurrentProject().getName();

		//			try {
		//				File newLookFile = StorageHandler.getInstance().copyImage(projectName, pathOfPaintroidImage,
		//						newFileName);
		File PicFileInPaintroid = new File(pathOfPaintroidImage);
		//				tempPicFileInPaintroid.delete(); //delete temp file in paintroid

		//				StorageHandler.getInstance().deleteFile(selectedLookData.getAbsolutePath()); //reduce usage in container or delete it

		//				selectedLookData.setLookFilename(newLookFile.getName());
		//				selectedLookData.resetThumbnailBitmap();
		//			} catch (IOException e) {
		//				e.printStackTrace();
	}

	//		}
	//	}

	public void sendPaintroidIntent(int selected_position) {
		Intent intent = new Intent("android.intent.action.MAIN");
		intent.setComponent(new ComponentName(paintroidIntentApplicationName, paintroidIntentActivityName));

		intent.addCategory("android.intent.category.LAUNCHER");
		//context.startActivity(intent);//
		((Activity) context).startActivityForResult(intent, 1);
		loadPaintroidImageIntoCatroid(intent);
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
		ProjectManager.getInstance().initializeDefaultProject(context);
		Activity currentActivity = (Activity) context;
		currentActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		ScreenParameters.getInstance().setScreenParameters();

		setTutorialActive();
		tutorialController.initalizeLessonCollection();
		tutorialController.initalizeLessons();
		tutorialController.showLessonDialog();
		return;
	}

	public void destroyTutorial() {
		tutorial = null;
	}

	public void stopButtonTutorial() {
		stopTutorial();
		tutorialController.stopButtonTutorial();
		//sendPaintroidIntent(Constants.NO_POSITION);
		clear();
		System.gc();
		Log.i("tutorial", "Tutorial.java: stopButtonTutorial: calling finalisation");
		System.runFinalization();

	}

	public void playButtonTutorial() {
		tutorialController.playTutorial();
	}

	public void pauseButtonTutorial() {
		//tutorialController.pauseTutorial();
		//sendPaintroidIntent(Constants.NO_POSITION); //start Paintroid
		selectImageFromCamera();
		Log.i("tutorial", "Tutorial.java: started paintdroid");
	}

	public boolean dispatchTouchEvent(MotionEvent ev) {
		return tutorialController.dispatchTouchEvent(ev);
	}

	public void stopTutorial() {
		pauseTutorial();
		setTutorialNotActive();
		tutorialController.stopThread();
		tutorialController.setSharedPreferences();
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
		tutorialController.holdTutorsAndRemoveOverlay();
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

	public void stepBackward() {
		tutorialController.stepBackward();
	}

	public void stepForward() {
		tutorialController.stepForward();
	}

	public void setDialog(Dialog dialog) {
		tutorialController.setDialog(dialog);
	}

	public Dialog getDialog() {
		return tutorialController.getDialog();
	}

	public float getDensity() {
		return context.getResources().getDisplayMetrics().density;
	}

	public int getScreenHeight() {
		Display display = ((Activity) context).getWindowManager().getDefaultDisplay();
		int screenHeight = display.getHeight();
		return screenHeight;
	}

	public int getScreenWidth() {
		Display display = ((Activity) context).getWindowManager().getDefaultDisplay();
		int screenWidth = display.getWidth();
		return screenWidth;
	}
}