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
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.NinePatchDrawable;

/**
 * @author amore
 * 
 */
public class SurfaceObjectBubble implements SurfaceObject {

	private Context context;
	private TutorialOverlay tutorialOverlay;
	private Paint paint;

	private Bitmap closeOverlay;
	private SurfaceObjectText text;

	private NinePatchDrawable bubble;

	public SurfaceObjectBubble(TutorialOverlay overlay, SurfaceObjectText text) {
		context = Tutorial.getInstance(null).getActualContext();
		this.text = text;
		this.tutorialOverlay = overlay;
		bubble = (NinePatchDrawable) Tutorial.getInstance(null).getActualContext().getResources()
				.getDrawable(R.drawable.bubble_catro);
		tutorialOverlay.addSurfaceObject(this);
	}

	@Override
	public void draw(Canvas canvas) {
		paint = new Paint();
		paint.setTextSize(25);

		bubble.setBounds(200, 100, 450, 350);
		bubble.draw(canvas);
		text.draw(canvas);

	}

	@Override
	public void update(long gameTime) {

	}

}
