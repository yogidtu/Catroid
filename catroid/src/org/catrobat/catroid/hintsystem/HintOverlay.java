/**
 *  Catroid: An on-device visual programming system for Android devices
 *  Copyright (C) 2010-2012 The Catrobat Team
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
package org.catrobat.catroid.hintsystem;

import java.util.ArrayList;

import org.catrobat.catroid.R;
import org.catrobat.catroid.ui.MainMenuActivity;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.NinePatchDrawable;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * @author peter
 * 
 */
public class HintOverlay extends SurfaceView implements SurfaceHolder.Callback {

	private Context context;
	private Paint paint = new Paint();
	int alpha = 255;
	private Resources res;
	private ArrayList<HintObject> allHints;
	private NinePatchDrawable bubble;

	private int hintPositionX;
	private int hintPositionY;
	private String hintText;

	public HintOverlay(Context context) {
		super(context);
		this.context = context;
		this.setBackgroundColor(Color.BLACK);
		this.getBackground().setAlpha(0);
		this.setZOrderOnTop(true); //necessary 
		getHolder().setFormat(PixelFormat.TRANSPARENT);
		getHolder().addCallback(this);
		Activity currentActivity = (Activity) context;
		currentActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);
		paint.setTextSize(15);
		paint.setARGB(255, 0, 0, 0);

	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {

		ScreenParameters screenParameters = ScreenParameters.getInstance();
		boolean returnValue = true;

		if (ev.getX() > screenParameters.getActionBarMenuXPosition()
				&& ev.getX() < screenParameters.getActionBarMenuXPosition() + screenParameters.getActionBarMenuWidth()) {
			if (ev.getY() > screenParameters.getActionBarMenuYPosition()
					&& ev.getY() < screenParameters.getActionBarMenuYPosition()
							+ screenParameters.getActionBarMenuHeight()) {
				Hint.getInstance().removeHint();
				MainMenuActivity activity = (MainMenuActivity) context;
				activity.openOptionsMenu();
			}
		}

		if (ev.getX() > screenParameters.getMainMenuToolTipXPosition()
				&& ev.getX() < screenParameters.getMainMenuToolTipXPosition()
						+ screenParameters.getMainMenuToolTipWidth()) {
			if (ev.getY() > screenParameters.getMainMenuContinueToolTipYPosition()
					&& ev.getY() < screenParameters.getMainMenuContinueToolTipYPosition()
							+ screenParameters.getMainMenuToolTipHeight()) {
				handleContinueToolTip();
			} else if (ev.getY() > screenParameters.getMainMenuNewToolTipYPosition()
					&& ev.getY() < screenParameters.getMainMenuNewToolTipYPosition()
							+ screenParameters.getMainMenuToolTipHeight()) {
				handleNewToolTip();
			} else if (ev.getY() > screenParameters.getMainMenuPropgramsToolTipYPosition()
					&& ev.getY() < screenParameters.getMainMenuPropgramsToolTipYPosition()
							+ screenParameters.getMainMenuToolTipHeight()) {
				handleProgramsToolTip();
			} else if (ev.getY() > screenParameters.getMainMenuForumToolTipYPosition()
					&& ev.getY() < screenParameters.getMainMenuForumToolTipYPosition()
							+ screenParameters.getMainMenuToolTipHeight()) {
				handleForumToolTip();
			} else if (ev.getY() > screenParameters.getMainMenuCommunityToolTipYPosition()
					&& ev.getY() < screenParameters.getMainMenuCommunityToolTipYPosition()
							+ screenParameters.getMainMenuToolTipHeight()) {
				handleCommunityToolTip();
			} else if (ev.getY() > screenParameters.getMainMenuUploadToolTipYPosition()
					&& ev.getY() < screenParameters.getMainMenuUploadToolTipYPosition()
							+ screenParameters.getMainMenuToolTipHeight()) {
				handleUploadToolTip();
			}
		} else {
			if (MainMenuActivity.hintDisplayed) {
				Hint.getInstance().removeHint();
				Hint hint = Hint.getInstance();
				hint.overlayHint();
				MainMenuActivity.hintDisplayed = false;
			}

			Hint.getInstance().removeHint();
			returnValue = Hint.getInstance().dispatchTouchEvent(ev);

		}
		return returnValue;
	}

	public void handleContinueToolTip() {
		Hint.getInstance().removeHint();

		if (!MainMenuActivity.hintDisplayed) {

			allHints = Hint.getHints();
			HintObject continueHint = allHints.get(0);

			Hint hint = Hint.getInstance();
			hint.overlayHint();
			hint.setHintPosition(continueHint.getXCoordinate(), continueHint.getYCoordinate(),
					continueHint.getHintText());
			MainMenuActivity.hintDisplayed = true;
		} else {
			Hint hint = Hint.getInstance();
			hint.overlayHint();
			MainMenuActivity.hintDisplayed = false;
		}
	}

	public void handleNewToolTip() {
		Hint.getInstance().removeHint();
		if (!MainMenuActivity.hintDisplayed) {

			allHints = Hint.getHints();
			HintObject continueHint = allHints.get(1);

			Hint hint = Hint.getInstance();
			hint.overlayHint();
			hint.setHintPosition(continueHint.getXCoordinate(), continueHint.getYCoordinate(),
					continueHint.getHintText());
			MainMenuActivity.hintDisplayed = true;
		} else {
			Hint hint = Hint.getInstance();
			hint.overlayHint();
			MainMenuActivity.hintDisplayed = false;
		}
	}

	public void handleProgramsToolTip() {
		Hint.getInstance().removeHint();
		if (!MainMenuActivity.hintDisplayed) {

			allHints = Hint.getHints();
			HintObject continueHint = allHints.get(2);

			Hint hint = Hint.getInstance();
			hint.overlayHint();
			hint.setHintPosition(continueHint.getXCoordinate(), continueHint.getYCoordinate(),
					continueHint.getHintText());
			MainMenuActivity.hintDisplayed = true;
		} else {
			Hint hint = Hint.getInstance();
			hint.overlayHint();
			MainMenuActivity.hintDisplayed = false;
		}
	}

	public void handleForumToolTip() {
		Hint.getInstance().removeHint();
		if (!MainMenuActivity.hintDisplayed) {

			allHints = Hint.getHints();
			HintObject continueHint = allHints.get(3);

			Hint hint = Hint.getInstance();
			hint.overlayHint();
			hint.setHintPosition(continueHint.getXCoordinate(), continueHint.getYCoordinate(),
					continueHint.getHintText());
			MainMenuActivity.hintDisplayed = true;
		} else {
			Hint hint = Hint.getInstance();
			hint.overlayHint();
			MainMenuActivity.hintDisplayed = false;
		}
	}

	public void handleCommunityToolTip() {
		Hint.getInstance().removeHint();
		if (!MainMenuActivity.hintDisplayed) {

			allHints = Hint.getHints();
			HintObject continueHint = allHints.get(4);

			Hint hint = Hint.getInstance();
			hint.overlayHint();
			hint.setHintPosition(continueHint.getXCoordinate(), continueHint.getYCoordinate(),
					continueHint.getHintText());
			MainMenuActivity.hintDisplayed = true;
		} else {
			Hint hint = Hint.getInstance();
			hint.overlayHint();
			MainMenuActivity.hintDisplayed = false;
		}
	}

	public void handleUploadToolTip() {
		Hint.getInstance().removeHint();
		if (!MainMenuActivity.hintDisplayed) {

			allHints = Hint.getHints();
			HintObject continueHint = allHints.get(5);

			Hint hint = Hint.getInstance();
			hint.overlayHint();
			hint.setHintPosition(continueHint.getXCoordinate(), continueHint.getYCoordinate(),
					continueHint.getHintText());
			MainMenuActivity.hintDisplayed = true;
		} else {
			Hint hint = Hint.getInstance();
			hint.overlayHint();
			MainMenuActivity.hintDisplayed = false;
		}
	}

	public void setPostions(int x, int y, String text) {
		hintPositionX = x;
		hintPositionY = y;
		hintText = text;
	}

	@Override
	public void onDraw(Canvas canvas) {

		if (hintText != null) {
			bubble = (NinePatchDrawable) context.getResources().getDrawable(R.drawable.bubble);
			bubble.setBounds(hintPositionX - 20, hintPositionY - 30, hintPositionX + 250, hintPositionY + 50);
			bubble.draw(canvas);
			drawMultilineText(hintText, hintPositionX, hintPositionY, paint, canvas);
		}
	}

	private void drawMultilineText(String str, int x, int y, Paint paint, Canvas canvas) {
		int lineHeight = 0;
		int yoffset = 0;
		String[] lines = str.split("\n");
		Rect bounds = new Rect();

		paint.getTextBounds(str, 0, 2, bounds);
		lineHeight = (int) (bounds.height() * 1.2);

		for (int i = 0; i < lines.length; ++i) {
			canvas.drawText(lines[i], x, y + yoffset, paint);
			yoffset = yoffset + lineHeight;
		}
	}

	public Bitmap createBitmap(int id) {
		res = context.getResources();
		BitmapFactory.Options opts = new BitmapFactory.Options();
		opts.inScaled = false;
		Bitmap bitmap = BitmapFactory.decodeResource(res, id, opts);
		return bitmap;
	}

	@Override
	public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {
		getHolder().addCallback(this);

	}

	@Override
	public void surfaceCreated(SurfaceHolder arg0) {
		getHolder().addCallback(this);

	}

	@Override
	public void surfaceDestroyed(SurfaceHolder arg0) {
		getHolder().removeCallback(this);

	}

}
