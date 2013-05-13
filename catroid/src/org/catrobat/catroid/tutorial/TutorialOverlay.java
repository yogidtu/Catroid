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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.catrobat.catroid.common.Constants;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * @author faxxe
 * 
 */
public class TutorialOverlay extends SurfaceView implements SurfaceHolder.Callback {
	private Context context;
	private List<SurfaceObject> surfaceObjects = Collections.synchronizedList(new ArrayList<SurfaceObject>());
	private Cloud cloud;
	private CloudController cloudController;
	private boolean interrupt = false;
	private long timeToWaitAfterRewind = 0;
	private Paint paint = new Paint();
	private String paintroidIntentApplicationName = "org.catrobat.paintroid";
	private String paintroidIntentActivityName = "org.catrobat.paintroid.MainActivity";

	@Override
	protected void finalize() throws Throwable {
		getHolder().removeCallback(this);
		surfaceObjects.clear();
		surfaceObjects = null;
		cloud = null;
		cloudController = null;
		super.finalize();

	};

	public void clean() {
		getHolder().removeCallback(this);
		surfaceObjects.clear();
		surfaceObjects = null;
		cloud = null;
		cloudController = null;
		context = null;
	}

	public TutorialOverlay(Context context) {
		super(context);
		this.context = context;
		this.setBackgroundColor(Color.TRANSPARENT);
		this.setZOrderOnTop(true); //necessary
		getHolder().setFormat(PixelFormat.TRANSPARENT);
		getHolder().addCallback(this);
		surfaceObjects = new ArrayList<SurfaceObject>();
		cloudController = new CloudController();

		paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
	}

	@Override
	public void onDraw(Canvas canvas) {

		canvas.drawPaint(paint);
		postInvalidate();

		if (cloud == null) {
			cloud = Cloud.getInstance(getContext());
		}

		cloud.draw(canvas);

		long actTime = System.currentTimeMillis();
		if (surfaceObjects != null && !interrupt && actTime > timeToWaitAfterRewind) {
			synchronized (surfaceObjects) {
				for (SurfaceObject tmp : surfaceObjects) {
					if (tmp != null) {
						tmp.update(System.currentTimeMillis());
						tmp.draw(canvas);
					}
				}
			}
		}
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
		getHolder().addCallback(this);
		cloud = Cloud.getInstance(context);
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		getHolder().addCallback(this);
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		getHolder().removeCallback(this);
	}

	public void addSurfaceObject(SurfaceObject surfaceObject) {
		if (!surfaceObjects.contains(surfaceObject)) {
			synchronized (surfaceObjects) {
				surfaceObjects.add(surfaceObject);
			}
		}
	}

	public void removeSurfaceObject(SurfaceObject surfaceViewObject) {
		if (surfaceObjects.contains(surfaceViewObject)) {
			synchronized (surfaceObjects) {
				surfaceObjects.remove(surfaceViewObject);
			}
		}
	}

	public void addCloud(Cloud cloud) {
		this.cloud = cloud;
	}

	public void removeCloud() {
		this.cloud = null;
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		boolean retval = false;

		if (ev.getY() > 0 && ev.getY() < 100) {

			if (isOnCloseButton(ev)) {
				Tutorial.getInstance(null).stopTutorial();
				cloudController.disapear();

			}
		} else if (ev.getY() > 700 && ev.getY() < 750) {

			if (isOnNextButton(ev)) {
				sendPaintroidIntent(Constants.NO_POSITION);

			}
		}
		return retval;
	}

	public void sendPaintroidIntent(int selected_position) {
		Intent intent = new Intent("android.intent.action.MAIN");
		intent.setComponent(new ComponentName(paintroidIntentApplicationName, paintroidIntentActivityName));

		intent.addCategory("android.intent.category.LAUNCHER");
		((Activity) context).startActivityForResult(intent, 1);
		//		loadPaintroidImageIntoCatroid(intent);
	}

	private boolean isOnNextButton(MotionEvent ev) {
		double[] closeButtonPosition = { 300, 400 };
		if (ev.getX() > closeButtonPosition[0] && ev.getX() < closeButtonPosition[1]) {
			return true;
		}
		return false;

	}

	private boolean isOnCloseButton(MotionEvent ev) {
		double[] closeButtonPosition = { 400, 430 };
		if (ev.getX() > closeButtonPosition[0] && ev.getX() < closeButtonPosition[1]) {
			return true;
		}
		return false;
	}
}
