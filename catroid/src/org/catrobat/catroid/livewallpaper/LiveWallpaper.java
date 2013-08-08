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

import org.catrobat.catroid.common.ScreenValues;
import org.catrobat.catroid.io.SoundManager;
import org.catrobat.catroid.stage.StageListener;

import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.SurfaceHolder;
import android.view.WindowManager;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.badlogic.gdx.backends.android.AndroidLiveWallpaperService;

public class LiveWallpaper extends AndroidLiveWallpaperService {

	public void onCreateApplication() {
		super.getApplication();
	}

	@Override
	public void onCreate() {
		//android.os.Debug.waitForDebugger();
		super.onCreate();
		DisplayMetrics displayMetrics = new DisplayMetrics();
		((WindowManager) getSystemService(WINDOW_SERVICE)).getDefaultDisplay().getMetrics(displayMetrics);
		ScreenValues.SCREEN_WIDTH = displayMetrics.widthPixels;
		ScreenValues.SCREEN_HEIGHT = displayMetrics.heightPixels;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.badlogic.gdx.backends.android.AndroidLiveWallpaperService#createListener(boolean)
	 */
	@Override
	public ApplicationListener createListener(boolean isPreview) {
		return new StageListener();
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

	@Override
	public Engine onCreateEngine() {
		LiveWallpaperEngine liveWallpaperEngine = new LiveWallpaperEngine();
		return liveWallpaperEngine;
	}

	class LiveWallpaperEngine extends AndroidWallpaperEngine {

		private boolean mVisible = false;
		private final Handler mHandler = new Handler();
		private final Runnable mUpdateDisplay = new Runnable() {
			@Override
			public void run() {
				if (mVisible) {
					mHandler.postDelayed(mUpdateDisplay, 100);
				}
			}
		};

		@Override
		public void onVisibilityChanged(boolean visible) {
			mVisible = visible;
			if (visible) {
				mHandler.postDelayed(mUpdateDisplay, 100);
				SoundManager.getInstance().resume();
			} else {
				mHandler.removeCallbacks(mUpdateDisplay);
				SoundManager.getInstance().pause();
			}
		}

		@Override
		public void onSurfaceChanged(SurfaceHolder holder, int format, int width, int height) {
			if (mVisible) {
				mHandler.postDelayed(mUpdateDisplay, 100);
			}
		}

		@Override
		public void onSurfaceDestroyed(SurfaceHolder holder) {
			super.onSurfaceDestroyed(holder);
			mVisible = false;
			mHandler.removeCallbacks(mUpdateDisplay);
		}

		@Override
		public void onDestroy() {
			super.onDestroy();
			mVisible = false;
			mHandler.removeCallbacks(mUpdateDisplay);
		}
	}
}
