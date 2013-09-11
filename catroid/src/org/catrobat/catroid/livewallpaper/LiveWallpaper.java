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

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.speech.tts.TextToSpeech;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.WindowManager;
import android.widget.Toast;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.badlogic.gdx.backends.android.AndroidLiveWallpaperService;

import org.catrobat.catroid.common.Constants;
import org.catrobat.catroid.common.ScreenValues;
import org.catrobat.catroid.formulaeditor.SensorHandler;
import org.catrobat.catroid.io.SoundManager;
import org.catrobat.catroid.stage.PreStageActivity;
import org.catrobat.catroid.stage.StageListener;
import org.catrobat.catroid.utils.Utils;

public class LiveWallpaper extends AndroidLiveWallpaperService {

	private StageListener stageListener;
	private AndroidApplicationConfiguration cfg;
	public static LiveWallpaperEngine liveWallpaperEngine;
	private Context context;

	@Override
	public void onCreate() {
		//		android.os.Debug.waitForDebugger();
		super.onCreate();
		DisplayMetrics displayMetrics = new DisplayMetrics();
		((WindowManager) getSystemService(WINDOW_SERVICE)).getDefaultDisplay().getMetrics(displayMetrics);
		ScreenValues.SCREEN_WIDTH = displayMetrics.widthPixels;
		ScreenValues.SCREEN_HEIGHT = displayMetrics.heightPixels;
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		SoundManager.getInstance().soundDisabledByLwp = sharedPreferences.getBoolean(Constants.PREF_SOUND_DISABLED,
				false);
		context = this;

		if (PreStageActivity.initTextToSpeech(context) != 0) {

			Intent installIntent = new Intent();
			installIntent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
			startActivity(installIntent);
		}

	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		PreStageActivity.shutDownTextToSpeech();
	}

	@Override
	public ApplicationListener createListener(boolean isPreview) {
		return stageListener;
	}

	@Override
	public AndroidApplicationConfiguration createConfig() {
		if (cfg == null) {
			cfg = new AndroidApplicationConfiguration();
			cfg.useGL20 = true;
		}
		return cfg;
	}

	@Override
	public Engine onCreateEngine() {
		Utils.loadProjectIfNeeded(getApplicationContext());
		stageListener = new StageListener(true);
		LiveWallpaper.liveWallpaperEngine = new LiveWallpaperEngine(this.stageListener);
		return LiveWallpaper.liveWallpaperEngine;
	}

	@Override
	public void offsetChange(ApplicationListener listener, float xOffset, float yOffset, float xOffsetStep,
			float yOffsetStep, int xPixelOffset, int yPixelOffset) {
		// TODO Auto-generated method stub

	}

	class LiveWallpaperEngine extends AndroidWallpaperEngine {

		private StageListener localStageListener;

		private boolean mVisible = false;
		private final Handler mHandler = new Handler();
		private final Runnable mUpdateDisplay = new Runnable() {
			@Override
			public void run() {
				if (mVisible) {
					mHandler.postDelayed(mUpdateDisplay, 300);
				}
			}
		};

		public LiveWallpaperEngine(StageListener stageListener) {
			super();
			this.localStageListener = stageListener;
			SensorHandler.startSensorListener(getApplicationContext());
		}

		@Override
		public void onVisibilityChanged(boolean visible) {
			mVisible = visible;
			super.onVisibilityChanged(visible);
		}

		@Override
		public void onResume() {
			if (localStageListener.isFinished()) {
				return;
			}
			Log.d("LWP", "VISIBLE EN-" + hashCode() + " SL-" + localStageListener.hashCode());
			localStageListener.menuResume();
			SensorHandler.startSensorListener(getApplicationContext());
			mHandler.postDelayed(mUpdateDisplay, 300);
			if (!SoundManager.getInstance().soundDisabledByLwp) {
				SoundManager.getInstance().resume();
			}
		}

		@Override
		public void onPause() {
			if (localStageListener.isFinished()) {
				return;
			}
			SensorHandler.stopSensorListeners();
			localStageListener.menuPause();
			Log.d("LWP", "NOT VISIBLE EN-" + hashCode() + " SL-" + localStageListener.hashCode());
			mHandler.removeCallbacks(mUpdateDisplay);
			SoundManager.getInstance().pause();
		}

		@Override
		public void onSurfaceChanged(SurfaceHolder holder, int format, int width, int height) {

			if (width > height) {
				Toast.makeText(context, "This wallpaper doesn't support landscape", Toast.LENGTH_LONG).show();
				localStageListener.finish();
			}

			if (mVisible) {
				mHandler.postDelayed(mUpdateDisplay, 300);
			} else {
				mHandler.removeCallbacks(mUpdateDisplay);
			}
			super.onSurfaceChanged(holder, format, width, height);
		}

		@Override
		public void onSurfaceDestroyed(SurfaceHolder holder) {
			mVisible = false;
			mHandler.removeCallbacks(mUpdateDisplay);
			super.onSurfaceDestroyed(holder);

		}

		@Override
		public void onDestroy() {
			mVisible = false;
			mHandler.removeCallbacks(mUpdateDisplay);
			super.onDestroy();
		}

		public void changeWallpaperProgram() {
			this.localStageListener.reloadProject(getApplicationContext(), null);
		}
	}
}
