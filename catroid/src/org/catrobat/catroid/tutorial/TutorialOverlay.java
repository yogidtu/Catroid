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

import android.content.Context;
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
	private CloudController co;
	private boolean interrupt = false;
	private long timeToWaitAfterRewind = 0;
	private Paint paint = new Paint();

	@Override
	protected void finalize() throws Throwable {
		getHolder().removeCallback(this);
		surfaceObjects.clear();
		surfaceObjects = null;
		cloud = null;
		co = null;
		super.finalize();

	};

	public void playIntro(SurfaceObject intro) {

	}

	public void clean() {
		getHolder().removeCallback(this);
		surfaceObjects.clear();
		surfaceObjects = null;
		cloud = null;
		co = null;
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
		co = new CloudController();
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
		return true;
	}

}
