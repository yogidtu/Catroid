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
package org.catrobat.catroid.tutorial;

import org.catrobat.catroid.R;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;

/**
 * @author amore
 * 
 */
public class SurfaceObjectTutor implements SurfaceObject {

	private Context context;
	private TutorialOverlay tutorialOverlay;
	private Bitmap bitmap;
	private Paint paint;

	private Resources resources;

	private String text;
	private int[] position;

	private long lastUpdateTime = 0;
	private int updateTime = 150;

	private boolean holdTutor = false;
	private int currentStep = 0;

	public SurfaceObjectTutor(TutorialOverlay overlay, int[] position) {
		context = Tutorial.getInstance(null).getActualContext();

		this.position = position;
		this.tutorialOverlay = overlay;
		tutorialOverlay.addSurfaceObject(this);
	}

	@Override
	public void draw(Canvas canvas) {
		paint = new Paint();
		paint.setTextSize(25);
		paint.setARGB(255, 0, 238, 0);
		resources = context.getResources();
		BitmapFactory.Options opts = new BitmapFactory.Options();
		opts.inScaled = false;
		bitmap = BitmapFactory.decodeResource(resources, R.drawable.prof, opts);
		canvas.drawBitmap(bitmap, position[0], position[1], paint);

	}

	@Override
	public void update(long gameTime) {
		if ((lastUpdateTime + updateTime) < gameTime && !holdTutor) {
			lastUpdateTime = gameTime;
			currentStep++;
		}

	}

}
