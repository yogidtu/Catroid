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
import org.catrobat.catroid.ui.ProjectActivity;

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
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * @author amore
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
		boolean returnValue = true;
		ScreenParameters screenParameters = ScreenParameters.getInstance();

		if (ev.getY() > screenParameters.getActionBarMenuHeight()) {
			switch (Hint.getInstance().checkActivity()) {
				case 0:
					mainMenuToolTipClicked(ev);
					returnValue = Hint.getInstance().dispatchTouchEvent(ev);
					break;
				case 1:
					projectToolTipClicked(ev);
					returnValue = Hint.getInstance().dispatchTouchEvent(ev);
					break;
			}
		} else {
			menuButtonActionBarClicked(ev);
			returnValue = Hint.getInstance().dispatchTouchEvent(ev);
		}
		return returnValue;
	}

	private void projectToolTipClicked(MotionEvent ev) {
		ScreenParameters screenParameters = ScreenParameters.getInstance();

		if (ev.getX() > screenParameters.getProjectActivitySpriteBackgroundToolTipXPosition()
				&& ev.getX() < screenParameters.getProjectActivitySpriteBackgroundToolTipXPosition()
						+ screenParameters.getToolTipWidth()) {
			if (ev.getY() > screenParameters.getProjectActivitySpriteBackgroundToolTipYPosition()
					&& ev.getY() < screenParameters.getProjectActivitySpriteBackgroundToolTipYPosition()
							+ screenParameters.getToolTipHeight()) {
				handleSpritesBackgroundToolTip();
			} else if (ev.getY() > screenParameters.getProjectActivitySpriteObjectToolTipYPosition()
					&& ev.getY() < screenParameters.getProjectActivitySpriteObjectToolTipYPosition()
							+ screenParameters.getToolTipHeight()) {
				handleSpritesObjectToolTip();
			}
		}

		if (ev.getY() > screenParameters.getProjectActivityAddButtonToolTipYPosition()
				&& ev.getY() < screenParameters.getProjectActivityAddButtonToolTipYPosition()
						+ screenParameters.getToolTipHeight()) {
			if (ev.getX() > screenParameters.getProjectActivityAddButtonToolTipXPosition()
					&& ev.getX() < screenParameters.getProjectActivityAddButtonToolTipXPosition()
							+ screenParameters.getToolTipWidth()) {
				handleSpritesAddButtonToolTip();
			} else if (ev.getX() > screenParameters.getProjectActivityPlayButtonToolTipXPosition()
					&& ev.getX() < screenParameters.getProjectActivityPlayButtonToolTipXPosition()
							+ screenParameters.getToolTipWidth()) {
				handleSpritesPlayButtonToolTip();
			}
		}

	}

	private void mainMenuToolTipClicked(MotionEvent ev) {
		ScreenParameters screenParameters = ScreenParameters.getInstance();

		if (ev.getX() > screenParameters.getMainMenuToolTipXPosition()
				&& ev.getX() < screenParameters.getMainMenuToolTipXPosition() + screenParameters.getToolTipWidth()) {
			if (ev.getY() > screenParameters.getMainMenuContinueToolTipYPosition()
					&& ev.getY() < screenParameters.getMainMenuContinueToolTipYPosition()
							+ screenParameters.getToolTipHeight()) {
				handleContinueToolTip();
			} else if (ev.getY() > screenParameters.getMainMenuNewToolTipYPosition()
					&& ev.getY() < screenParameters.getMainMenuNewToolTipYPosition()
							+ screenParameters.getToolTipHeight()) {
				handleNewToolTip();
			} else if (ev.getY() > screenParameters.getMainMenuPropgramsToolTipYPosition()
					&& ev.getY() < screenParameters.getMainMenuPropgramsToolTipYPosition()
							+ screenParameters.getToolTipHeight()) {
				handleProgramsToolTip();
			} else if (ev.getY() > screenParameters.getMainMenuForumToolTipYPosition()
					&& ev.getY() < screenParameters.getMainMenuForumToolTipYPosition()
							+ screenParameters.getToolTipHeight()) {
				handleForumToolTip();
			} else if (ev.getY() > screenParameters.getMainMenuCommunityToolTipYPosition()
					&& ev.getY() < screenParameters.getMainMenuCommunityToolTipYPosition()
							+ screenParameters.getToolTipHeight()) {
				handleCommunityToolTip();
			} else if (ev.getY() > screenParameters.getMainMenuUploadToolTipYPosition()
					&& ev.getY() < screenParameters.getMainMenuUploadToolTipYPosition()
							+ screenParameters.getToolTipHeight()) {
				handleUploadToolTip();
			}
		}
	}

	private boolean menuButtonActionBarClicked(MotionEvent ev) {
		ScreenParameters screenParameters = ScreenParameters.getInstance();

		if (ev.getX() > screenParameters.getActionBarMenuXPosition()
				&& ev.getX() < screenParameters.getActionBarMenuXPosition() + screenParameters.getActionBarMenuWidth()) {
			if (ev.getY() > screenParameters.getActionBarMenuYPosition()
					&& ev.getY() < screenParameters.getActionBarMenuYPosition()
							+ screenParameters.getActionBarMenuHeight()) {
				Hint.getInstance().removeHint();
				Activity activity = (Activity) context;
				activity.openOptionsMenu();
				return true;
			}
		}
		return false;
	}

	private void handleContinueToolTip() {
		Hint.getInstance().removeHint();

		if (!MainMenuActivity.hintBubbleDisplayed) {

			allHints = Hint.getHints();
			HintObject continueHint = allHints.get(0);

			Hint hint = Hint.getInstance();
			hint.overlayHint();
			hint.setHintPosition(continueHint.getXCoordinate(), continueHint.getYCoordinate(),
					continueHint.getHintText());
			MainMenuActivity.hintBubbleDisplayed = true;
		} else {
			Hint hint = Hint.getInstance();
			hint.overlayHint();
			MainMenuActivity.hintBubbleDisplayed = false;
		}
	}

	private void handleNewToolTip() {
		Hint.getInstance().removeHint();
		if (!MainMenuActivity.hintBubbleDisplayed) {

			allHints = Hint.getHints();
			HintObject continueHint = allHints.get(1);

			Hint hint = Hint.getInstance();
			hint.overlayHint();
			hint.setHintPosition(continueHint.getXCoordinate(), continueHint.getYCoordinate(),
					continueHint.getHintText());
			MainMenuActivity.hintBubbleDisplayed = true;
		} else {
			Hint hint = Hint.getInstance();
			hint.overlayHint();
			MainMenuActivity.hintBubbleDisplayed = false;
		}
	}

	private void handleProgramsToolTip() {
		Hint.getInstance().removeHint();
		if (!MainMenuActivity.hintBubbleDisplayed) {

			allHints = Hint.getHints();
			HintObject continueHint = allHints.get(2);

			Hint hint = Hint.getInstance();
			hint.overlayHint();
			hint.setHintPosition(continueHint.getXCoordinate(), continueHint.getYCoordinate(),
					continueHint.getHintText());
			MainMenuActivity.hintBubbleDisplayed = true;
		} else {
			Hint hint = Hint.getInstance();
			hint.overlayHint();
			MainMenuActivity.hintBubbleDisplayed = false;
		}
	}

	private void handleForumToolTip() {
		Hint.getInstance().removeHint();
		if (!MainMenuActivity.hintBubbleDisplayed) {

			allHints = Hint.getHints();
			HintObject continueHint = allHints.get(3);

			Hint hint = Hint.getInstance();
			hint.overlayHint();
			hint.setHintPosition(continueHint.getXCoordinate(), continueHint.getYCoordinate(),
					continueHint.getHintText());
			MainMenuActivity.hintBubbleDisplayed = true;
		} else {
			Hint hint = Hint.getInstance();
			hint.overlayHint();
			MainMenuActivity.hintBubbleDisplayed = false;
		}
	}

	private void handleCommunityToolTip() {
		Hint.getInstance().removeHint();
		if (!MainMenuActivity.hintBubbleDisplayed) {

			allHints = Hint.getHints();
			HintObject continueHint = allHints.get(4);

			Hint hint = Hint.getInstance();
			hint.overlayHint();
			hint.setHintPosition(continueHint.getXCoordinate(), continueHint.getYCoordinate(),
					continueHint.getHintText());
			MainMenuActivity.hintBubbleDisplayed = true;
		} else {
			Hint hint = Hint.getInstance();
			hint.overlayHint();
			MainMenuActivity.hintBubbleDisplayed = false;
		}
	}

	private void handleUploadToolTip() {
		Hint.getInstance().removeHint();
		if (!MainMenuActivity.hintBubbleDisplayed) {

			allHints = Hint.getHints();
			HintObject continueHint = allHints.get(5);

			Hint hint = Hint.getInstance();
			hint.overlayHint();
			hint.setHintPosition(continueHint.getXCoordinate(), continueHint.getYCoordinate(),
					continueHint.getHintText());
			MainMenuActivity.hintBubbleDisplayed = true;
		} else {
			Hint hint = Hint.getInstance();
			hint.overlayHint();
			MainMenuActivity.hintBubbleDisplayed = false;
		}
	}

	private void handleSpritesBackgroundToolTip() {
		Hint.getInstance().removeHint();
		if (!ProjectActivity.hintBubbleDisplayed) {

			allHints = Hint.getHints();
			HintObject spritesBackgroundHint = allHints.get(0);

			Hint hint = Hint.getInstance();
			hint.overlayHint();
			hint.setHintPosition(spritesBackgroundHint.getXCoordinate(), spritesBackgroundHint.getYCoordinate(),
					spritesBackgroundHint.getHintText());
			ProjectActivity.hintBubbleDisplayed = true;
		} else {
			Hint hint = Hint.getInstance();
			hint.overlayHint();
			ProjectActivity.hintBubbleDisplayed = false;
		}
	}

	private void handleSpritesObjectToolTip() {
		Hint.getInstance().removeHint();
		if (!ProjectActivity.hintBubbleDisplayed) {

			allHints = Hint.getHints();
			HintObject spritesBackgroundHint = allHints.get(1);

			Hint hint = Hint.getInstance();
			hint.overlayHint();
			hint.setHintPosition(spritesBackgroundHint.getXCoordinate(), spritesBackgroundHint.getYCoordinate(),
					spritesBackgroundHint.getHintText());
			ProjectActivity.hintBubbleDisplayed = true;
		} else {
			Hint hint = Hint.getInstance();
			hint.overlayHint();
			ProjectActivity.hintBubbleDisplayed = false;
		}
	}

	private void handleSpritesAddButtonToolTip() {
		Hint.getInstance().removeHint();
		if (!ProjectActivity.hintBubbleDisplayed) {

			allHints = Hint.getHints();
			HintObject spritesBackgroundHint = allHints.get(2);

			Hint hint = Hint.getInstance();
			hint.overlayHint();
			hint.setHintPosition(spritesBackgroundHint.getXCoordinate(), spritesBackgroundHint.getYCoordinate(),
					spritesBackgroundHint.getHintText());
			ProjectActivity.hintBubbleDisplayed = true;
		} else {
			Hint hint = Hint.getInstance();
			hint.overlayHint();
			ProjectActivity.hintBubbleDisplayed = false;
		}
	}

	private void handleSpritesPlayButtonToolTip() {
		Hint.getInstance().removeHint();
		if (!ProjectActivity.hintBubbleDisplayed) {

			allHints = Hint.getHints();
			HintObject spritesBackgroundHint = allHints.get(3);

			Hint hint = Hint.getInstance();
			hint.overlayHint();
			hint.setHintPosition(spritesBackgroundHint.getXCoordinate(), spritesBackgroundHint.getYCoordinate(),
					spritesBackgroundHint.getHintText());
			ProjectActivity.hintBubbleDisplayed = true;
		} else {
			Hint hint = Hint.getInstance();
			hint.overlayHint();
			ProjectActivity.hintBubbleDisplayed = false;
		}

	}

	public boolean addToolTipButtonsToMainMenuActivity() {
		Activity activity = (Activity) context;

		Button button = (Button) activity.findViewById(R.id.main_menu_button_continue);
		button.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_main_menu_continue, 0,
				R.drawable.icon_tooltip_inactive, 0);
		button = (Button) activity.findViewById(R.id.main_menu_button_new);
		button.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_main_menu_new, 0,
				R.drawable.icon_tooltip_inactive, 0);
		button = (Button) activity.findViewById(R.id.main_menu_button_programs);
		button.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_main_menu_programs, 0,
				R.drawable.icon_tooltip_inactive, 0);
		button = (Button) activity.findViewById(R.id.main_menu_button_forum);
		button.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_main_menu_forum, 0,
				R.drawable.icon_tooltip_inactive, 0);
		button = (Button) activity.findViewById(R.id.main_menu_button_web);
		button.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_main_menu_community, 0,
				R.drawable.icon_tooltip_inactive, 0);
		button = (Button) activity.findViewById(R.id.main_menu_button_upload);
		button.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_main_menu_upload, 0,
				R.drawable.icon_tooltip_inactive, 0);

		MainMenuActivity.hintActive = true;
		return true;
	}

	public boolean addToolTipButtonsToProjectActivity() {
		Activity activity = (Activity) context;

		LinearLayout currentView = (LinearLayout) activity.findViewById(R.id.spritelist_background_headline);
		TextView headlineTextView = (TextView) currentView.getChildAt(0);
		View separationLineView = currentView.getChildAt(1);
		currentView.removeView(headlineTextView);
		currentView.removeView(separationLineView);

		ImageView tooltipSpritesBackground = new ImageView(context);
		tooltipSpritesBackground.setImageDrawable(context.getResources().getDrawable(R.drawable.icon_tooltip_inactive));
		tooltipSpritesBackground.setLayoutParams(new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.WRAP_CONTENT, Gravity.RIGHT));

		RelativeLayout spriteBackgroundToolTipLayout = new RelativeLayout(context);
		spriteBackgroundToolTipLayout.addView(headlineTextView);
		spriteBackgroundToolTipLayout.addView(separationLineView);
		spriteBackgroundToolTipLayout.addView(tooltipSpritesBackground);
		currentView.addView(spriteBackgroundToolTipLayout);

		currentView = (LinearLayout) activity.findViewById(R.id.spritelist_objects_headline);
		TextView headlineObjectTextView = (TextView) currentView.getChildAt(0);
		View separationLineObjectView = currentView.getChildAt(1);
		currentView.removeView(headlineObjectTextView);
		currentView.removeView(separationLineObjectView);

		ImageView tooltipSpritesObject = new ImageView(context);
		tooltipSpritesObject.setImageDrawable(context.getResources().getDrawable(R.drawable.icon_tooltip_inactive));
		tooltipSpritesObject.setLayoutParams(new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.WRAP_CONTENT, Gravity.RIGHT));

		RelativeLayout spriteObjectToolTipLayout = new RelativeLayout(context);
		spriteObjectToolTipLayout.addView(headlineObjectTextView);
		spriteObjectToolTipLayout.addView(separationLineObjectView);
		spriteObjectToolTipLayout.addView(tooltipSpritesObject);
		currentView.addView(spriteObjectToolTipLayout);

		currentView = (LinearLayout) activity.findViewById(R.id.button_add);
		ImageView tooltipAddButton = new ImageView(context);
		tooltipAddButton.setImageDrawable(context.getResources().getDrawable(R.drawable.icon_tooltip_inactive));
		currentView.addView(tooltipAddButton);

		currentView = (LinearLayout) activity.findViewById(R.id.button_play);
		ImageView tooltipPlayButton = new ImageView(context);
		tooltipPlayButton.setImageDrawable(context.getResources().getDrawable(R.drawable.icon_tooltip_inactive));
		currentView.addView(tooltipPlayButton);

		ProjectActivity.hintActive = true;

		return true;
	}

	public boolean removeMainMenuActivityToolTipButtons() {
		Activity activity = (Activity) context;

		Button button = (Button) activity.findViewById(R.id.main_menu_button_continue);
		button.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_main_menu_continue, 0,
				R.drawable.ic_arrow_right_dark, 0);
		button = (Button) activity.findViewById(R.id.main_menu_button_new);
		button.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_main_menu_new, 0, R.drawable.ic_arrow_right_dark,
				0);
		button = (Button) activity.findViewById(R.id.main_menu_button_programs);
		button.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_main_menu_programs, 0,
				R.drawable.ic_arrow_right_dark, 0);
		button = (Button) activity.findViewById(R.id.main_menu_button_forum);
		button.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_main_menu_forum, 0,
				R.drawable.ic_arrow_right_dark, 0);
		button = (Button) activity.findViewById(R.id.main_menu_button_web);
		button.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_main_menu_community, 0,
				R.drawable.ic_arrow_right_dark, 0);
		button = (Button) activity.findViewById(R.id.main_menu_button_upload);
		button.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_main_menu_upload, 0,
				R.drawable.ic_arrow_right_dark, 0);

		MainMenuActivity.hintActive = false;

		return true;
	}

	public boolean removeProjectActivityToolTipButtons() {
		Activity activity = (Activity) context;

		LinearLayout currentView = (LinearLayout) activity.findViewById(R.id.spritelist_background_headline);
		RelativeLayout headlineBackgroundView = (RelativeLayout) currentView.getChildAt(0);
		TextView headlineTextView = (TextView) headlineBackgroundView.getChildAt(0);
		View separationLineView = headlineBackgroundView.getChildAt(1);
		headlineBackgroundView.removeView(headlineTextView);
		headlineBackgroundView.removeView(separationLineView);
		currentView.removeView(headlineBackgroundView);

		currentView.addView(headlineTextView);
		currentView.addView(separationLineView);

		currentView = (LinearLayout) activity.findViewById(R.id.spritelist_objects_headline);
		RelativeLayout headlineObjectView = (RelativeLayout) currentView.getChildAt(0);
		TextView headlineObjectTextView = (TextView) headlineObjectView.getChildAt(0);
		View separationLineObjectView = headlineObjectView.getChildAt(1);
		headlineObjectView.removeView(headlineObjectTextView);
		headlineObjectView.removeView(separationLineObjectView);
		currentView.removeView(headlineObjectView);

		currentView.addView(headlineObjectTextView);
		currentView.addView(separationLineObjectView);

		LinearLayout buttonLayout = (LinearLayout) activity.findViewById(R.id.button_add);
		View v = buttonLayout.getChildAt(1);
		buttonLayout.removeView(v);

		buttonLayout = (LinearLayout) activity.findViewById(R.id.button_play);
		v = buttonLayout.getChildAt(1);
		buttonLayout.removeView(v);
		ProjectActivity.hintActive = false;

		return true;
	}

	public boolean setPostions(int x, int y, String text) {
		hintPositionX = x;
		hintPositionY = y;
		hintText = text;
		return true;
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
