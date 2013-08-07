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
package org.catrobat.catroid.livewallpaper;

import org.catrobat.catroid.stage.StageListener;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.badlogic.gdx.backends.android.AndroidLiveWallpaperService;

public class LiveWallpaper extends AndroidLiveWallpaperService {

	private StageListener stageListener;

	public void onCreateApplication() {
		super.getApplication();

	}

	@Override
	public void onCreate() {
		//android.os.Debug.waitForDebugger();
		super.onCreate();
		//		DisplayMetrics displayMetrics = new DisplayMetrics();
		//		((WindowManager) getSystemService(WINDOW_SERVICE)).getDefaultDisplay().getMetrics(displayMetrics);
		//		ScreenValues.SCREEN_WIDTH = displayMetrics.widthPixels;
		//		ScreenValues.SCREEN_HEIGHT = displayMetrics.heightPixels;
		//
		//		Log.d("RGB", "Width: " + ScreenValues.SCREEN_WIDTH);
		//		Log.d("RGB", "Height: " + ScreenValues.SCREEN_HEIGHT);

		//		try {
		//			StandardProjectHandler.createAndSaveStandardProject(getApplicationContext());
		//		} catch (IOException e) {
		//			// TODO Auto-generated catch block
		//			e.printStackTrace();
		//		}
		//		Gdx.input.setInputProcess//();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.badlogic.gdx.backends.android.AndroidLiveWallpaperService#createListener(boolean)
	 */
	@Override
	public ApplicationListener createListener(boolean isPreview) {
		stageListener = new StageListener();
		return stageListener;
	}

	/*
	 * stageListenerc)
	 * 
	 * @see com.badlogic.gdx.backends.android.AndroidLiveWallpaperService#createConfig()
	 */
	@Override
	public AndroidApplicationConfiguration createConfig() {
		AndroidApplicationConfiguration cfg = new AndroidApplicationConfiguration();
		cfg.useGL20 = true;
		return cfg;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.badlogic.gdx.backends.android.AndroidLiveWallpaperService#offsetChange(com.badlogic.gdx.ApplicationListener,
	 * float, float, float, float, int, int)
	 */
	@Override
	public void offsetChange(ApplicationListener listener, float xOffset, float yOffset, float xOffsetStep,
			float yOffsetStep, int xPixelOffset, int yPixelOffset) {
		// TODO Auto-generated method stub

	}
}
