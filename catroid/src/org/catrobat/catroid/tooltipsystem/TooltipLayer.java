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
package org.catrobat.catroid.tooltipsystem;

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
public class TooltipLayer extends SurfaceView implements SurfaceHolder.Callback {

	private Context context;
	private Paint paint = new Paint();
	int alpha = 255;
	private Resources res;
	private NinePatchDrawable bubble;

	private int tooltipPositionX;
	private int tooltipPositionY;
	private String tooltipText;

	public TooltipLayer(Context context) {
		super(context);
		this.context = context;
		this.setBackgroundColor(Color.BLACK);
		this.getBackground().setAlpha(100);
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
			switch (Tooltip.getInstance().checkActivity()) {
				case 0:
					mainMenuTooltipClicked(ev);
					returnValue = Tooltip.getInstance().dispatchTouchEvent(ev);
					break;
				case 1:
					projectTooltipClicked(ev);
					returnValue = Tooltip.getInstance().dispatchTouchEvent(ev);
					break;
			}
		} else {
			menuButtonActionBarClicked(ev);
			returnValue = Tooltip.getInstance().dispatchTouchEvent(ev);
		}
		return returnValue;
	}

	private void projectTooltipClicked(MotionEvent ev) {
		ScreenParameters screenParameters = ScreenParameters.getInstance();

		if (ev.getX() > screenParameters.getProjectActivitySpriteBackgroundTooltipXPosition()
				&& ev.getX() < screenParameters.getProjectActivitySpriteBackgroundTooltipXPosition()
						+ screenParameters.getTooltipWidth()) {
			if (ev.getY() > screenParameters.getProjectActivitySpriteBackgroundTooltipYPosition()
					&& ev.getY() < screenParameters.getProjectActivitySpriteBackgroundTooltipYPosition()
							+ screenParameters.getTooltipHeight()) {
				handleSpritesBackgroundTooltip();
			} else if (ev.getY() > screenParameters.getProjectActivitySpriteObjectTooltipYPosition()
					&& ev.getY() < screenParameters.getProjectActivitySpriteObjectTooltipYPosition()
							+ screenParameters.getTooltipHeight()) {
				handleSpritesObjectTooltip();
			}
		}

		if (ev.getY() > screenParameters.getProjectActivityAddButtonTooltipYPosition()
				&& ev.getY() < screenParameters.getProjectActivityAddButtonTooltipYPosition()
						+ screenParameters.getTooltipHeight()) {
			if (ev.getX() > screenParameters.getProjectActivityAddButtonTooltipXPosition()
					&& ev.getX() < screenParameters.getProjectActivityAddButtonTooltipXPosition()
							+ screenParameters.getTooltipWidth()) {
				handleSpritesAddButtonTooltip();
			} else if (ev.getX() > screenParameters.getProjectActivityPlayButtonTooltipXPosition()
					&& ev.getX() < screenParameters.getProjectActivityPlayButtonTooltipXPosition()
							+ screenParameters.getTooltipWidth()) {
				handleSpritesPlayButtonTooltip();
			}
		}

	}

	private void mainMenuTooltipClicked(MotionEvent ev) {
		ScreenParameters screenParameters = ScreenParameters.getInstance();

		if (ev.getX() > screenParameters.getMainMenuTooltipXPosition()
				&& ev.getX() < screenParameters.getMainMenuTooltipXPosition() + screenParameters.getTooltipWidth()) {
			if (ev.getY() > screenParameters.getMainMenuContinueTooltipYPosition()
					&& ev.getY() < screenParameters.getMainMenuContinueTooltipYPosition()
							+ screenParameters.getTooltipHeight()) {
				handleContinueTooltip();
			} else if (ev.getY() > screenParameters.getMainMenuNewTooltipYPosition()
					&& ev.getY() < screenParameters.getMainMenuNewTooltipYPosition()
							+ screenParameters.getTooltipHeight()) {
				handleNewTooltip();
			} else if (ev.getY() > screenParameters.getMainMenuProgramsTooltipYPosition()
					&& ev.getY() < screenParameters.getMainMenuProgramsTooltipYPosition()
							+ screenParameters.getTooltipHeight()) {
				handleProgramsTooltip();
			} else if (ev.getY() > screenParameters.getMainMenuForumTooltipYPosition()
					&& ev.getY() < screenParameters.getMainMenuForumTooltipYPosition()
							+ screenParameters.getTooltipHeight()) {
				handleForumTooltip();
			} else if (ev.getY() > screenParameters.getMainMenuCommunityTooltipYPosition()
					&& ev.getY() < screenParameters.getMainMenuCommunityTooltipYPosition()
							+ screenParameters.getTooltipHeight()) {
				handleWebTooltip();
			} else if (ev.getY() > screenParameters.getMainMenuUploadTooltipYPosition()
					&& ev.getY() < screenParameters.getMainMenuUploadTooltipYPosition()
							+ screenParameters.getTooltipHeight()) {
				handleUploadTooltip();
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
				Tooltip.getInstance().stopTooltipSystem();
				Activity activity = (Activity) context;
				activity.openOptionsMenu();
				return true;
			}
		}
		return false;
	}

	private void handleContinueTooltip() {
		Tooltip.getInstance().stopTooltipSystem();
		Tooltip tooltip = Tooltip.getInstance();

		if (!MainMenuActivity.tooltipBubbleDisplayed) {
			ToolTipObject continueTooltip = Tooltip.getTooltip(R.string.hint_mainmenu_continue);
			tooltip.startTooltipSystem();
			tooltip.setTooltipPosition(continueTooltip.getTextXCoordinate(), continueTooltip.getTextYCoordinate(),
					continueTooltip.getTooltipText());
			MainMenuActivity.tooltipBubbleDisplayed = true;
		} else {
			tooltip.startTooltipSystem();
			MainMenuActivity.tooltipBubbleDisplayed = false;
		}
	}

	private void handleNewTooltip() {
		Tooltip.getInstance().stopTooltipSystem();
		Tooltip tooltip = Tooltip.getInstance();
		if (!MainMenuActivity.tooltipBubbleDisplayed) {

			ToolTipObject newTooltip = Tooltip.getTooltip(R.string.hint_mainmenu_new);

			tooltip.startTooltipSystem();
			tooltip.setTooltipPosition(newTooltip.getTextXCoordinate(), newTooltip.getTextYCoordinate(),
					newTooltip.getTooltipText());
			MainMenuActivity.tooltipBubbleDisplayed = true;
		} else {
			tooltip.startTooltipSystem();
			MainMenuActivity.tooltipBubbleDisplayed = false;
		}
	}

	private void handleProgramsTooltip() {
		Tooltip.getInstance().stopTooltipSystem();
		Tooltip tooltip = Tooltip.getInstance();

		if (!MainMenuActivity.tooltipBubbleDisplayed) {

			ToolTipObject programsTooltip = Tooltip.getTooltip(R.string.hint_mainmenu_programs);
			tooltip.startTooltipSystem();
			tooltip.setTooltipPosition(programsTooltip.getTextXCoordinate(), programsTooltip.getTextYCoordinate(),
					programsTooltip.getTooltipText());
			MainMenuActivity.tooltipBubbleDisplayed = true;
		} else {
			tooltip.startTooltipSystem();
			MainMenuActivity.tooltipBubbleDisplayed = false;
		}
	}

	private void handleForumTooltip() {
		Tooltip.getInstance().stopTooltipSystem();
		Tooltip tooltip = Tooltip.getInstance();

		if (!MainMenuActivity.tooltipBubbleDisplayed) {

			ToolTipObject forumTooltip = Tooltip.getTooltip(R.string.hint_mainmenu_community);

			tooltip.startTooltipSystem();
			tooltip.setTooltipPosition(forumTooltip.getTextXCoordinate(), forumTooltip.getTextYCoordinate(),
					forumTooltip.getTooltipText());
			MainMenuActivity.tooltipBubbleDisplayed = true;
		} else {
			tooltip.startTooltipSystem();
			MainMenuActivity.tooltipBubbleDisplayed = false;
		}
	}

	private void handleWebTooltip() {
		Tooltip.getInstance().stopTooltipSystem();
		Tooltip tooltip = Tooltip.getInstance();

		if (!MainMenuActivity.tooltipBubbleDisplayed) {

			ToolTipObject webTooltip = Tooltip.getTooltip(R.string.hint_mainmenu_web);

			tooltip.startTooltipSystem();
			tooltip.setTooltipPosition(webTooltip.getTextXCoordinate(), webTooltip.getTextYCoordinate(),
					webTooltip.getTooltipText());
			MainMenuActivity.tooltipBubbleDisplayed = true;
		} else {
			tooltip.startTooltipSystem();
			MainMenuActivity.tooltipBubbleDisplayed = false;
		}
	}

	private void handleUploadTooltip() {
		Tooltip.getInstance().stopTooltipSystem();
		Tooltip tooltip = Tooltip.getInstance();

		if (!MainMenuActivity.tooltipBubbleDisplayed) {
			ToolTipObject uploadTooltip = Tooltip.getTooltip(R.string.hint_mainmenu_upload);

			tooltip.startTooltipSystem();
			tooltip.setTooltipPosition(uploadTooltip.getTextXCoordinate(), uploadTooltip.getTextYCoordinate(),
					uploadTooltip.getTooltipText());
			MainMenuActivity.tooltipBubbleDisplayed = true;
		} else {
			tooltip.startTooltipSystem();
			MainMenuActivity.tooltipBubbleDisplayed = false;
		}
	}

	private void handleSpritesBackgroundTooltip() {
		Tooltip.getInstance().stopTooltipSystem();
		Tooltip tooltip = Tooltip.getInstance();

		if (!ProjectActivity.tooltipBubbleDisplayed) {

			ToolTipObject spritesBackgroundTooltip = Tooltip.getTooltip(R.string.hint_project_background);

			tooltip.startTooltipSystem();
			tooltip.setTooltipPosition(spritesBackgroundTooltip.getTextXCoordinate(),
					spritesBackgroundTooltip.getTextYCoordinate(), spritesBackgroundTooltip.getTooltipText());
			ProjectActivity.tooltipBubbleDisplayed = true;
		} else {
			tooltip.startTooltipSystem();
			ProjectActivity.tooltipBubbleDisplayed = false;
		}
	}

	private void handleSpritesObjectTooltip() {
		Tooltip.getInstance().stopTooltipSystem();
		Tooltip tooltip = Tooltip.getInstance();

		if (!ProjectActivity.tooltipBubbleDisplayed) {
			ToolTipObject spritesObjectTooltip = Tooltip.getTooltip(R.string.hint_project_objects);

			tooltip.startTooltipSystem();
			tooltip.setTooltipPosition(spritesObjectTooltip.getTextXCoordinate(),
					spritesObjectTooltip.getTextYCoordinate(), spritesObjectTooltip.getTooltipText());
			ProjectActivity.tooltipBubbleDisplayed = true;
		} else {
			tooltip.startTooltipSystem();
			ProjectActivity.tooltipBubbleDisplayed = false;
		}
	}

	private void handleSpritesAddButtonTooltip() {
		Tooltip.getInstance().stopTooltipSystem();
		Tooltip tooltip = Tooltip.getInstance();

		if (!ProjectActivity.tooltipBubbleDisplayed) {
			ToolTipObject spritesAddButtonTooltip = Tooltip.getTooltip(R.string.hint_project_add);

			tooltip.startTooltipSystem();
			tooltip.setTooltipPosition(spritesAddButtonTooltip.getTextXCoordinate(),
					spritesAddButtonTooltip.getTextYCoordinate(), spritesAddButtonTooltip.getTooltipText());
			ProjectActivity.tooltipBubbleDisplayed = true;
		} else {
			tooltip.startTooltipSystem();
			ProjectActivity.tooltipBubbleDisplayed = false;
		}
	}

	private void handleSpritesPlayButtonTooltip() {
		Tooltip.getInstance().stopTooltipSystem();
		Tooltip tooltip = Tooltip.getInstance();

		if (!ProjectActivity.tooltipBubbleDisplayed) {
			ToolTipObject spritesPlayTooltip = Tooltip.getTooltip(R.string.hint_project_play);

			tooltip.startTooltipSystem();
			tooltip.setTooltipPosition(spritesPlayTooltip.getTextXCoordinate(),
					spritesPlayTooltip.getTextYCoordinate(), spritesPlayTooltip.getTooltipText());
			ProjectActivity.tooltipBubbleDisplayed = true;
		} else {
			tooltip.startTooltipSystem();
			ProjectActivity.tooltipBubbleDisplayed = false;
		}

	}

	public boolean addTooltipButtonsToMainMenuActivity() {
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

		MainMenuActivity.tooltipActive = true;
		return true;
	}

	public boolean addTooltipButtonsToProjectActivity() {
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

		RelativeLayout spriteBackgroundTooltipLayout = new RelativeLayout(context);
		spriteBackgroundTooltipLayout.addView(headlineTextView);
		spriteBackgroundTooltipLayout.addView(separationLineView);
		spriteBackgroundTooltipLayout.addView(tooltipSpritesBackground);
		currentView.addView(spriteBackgroundTooltipLayout);

		currentView = (LinearLayout) activity.findViewById(R.id.spritelist_objects_headline);
		TextView headlineObjectTextView = (TextView) currentView.getChildAt(0);
		View separationLineObjectView = currentView.getChildAt(1);
		currentView.removeView(headlineObjectTextView);
		currentView.removeView(separationLineObjectView);

		ImageView tooltipSpritesObject = new ImageView(context);
		tooltipSpritesObject.setImageDrawable(context.getResources().getDrawable(R.drawable.icon_tooltip_inactive));
		tooltipSpritesObject.setLayoutParams(new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.WRAP_CONTENT, Gravity.RIGHT));

		RelativeLayout spriteObjectTooltipLayout = new RelativeLayout(context);
		spriteObjectTooltipLayout.addView(headlineObjectTextView);
		spriteObjectTooltipLayout.addView(separationLineObjectView);
		spriteObjectTooltipLayout.addView(tooltipSpritesObject);
		currentView.addView(spriteObjectTooltipLayout);

		currentView = (LinearLayout) activity.findViewById(R.id.button_add);
		ImageView tooltipAddButton = new ImageView(context);
		tooltipAddButton.setImageDrawable(context.getResources().getDrawable(R.drawable.icon_tooltip_inactive));
		currentView.addView(tooltipAddButton);

		currentView = (LinearLayout) activity.findViewById(R.id.button_play);
		ImageView tooltipPlayButton = new ImageView(context);
		tooltipPlayButton.setImageDrawable(context.getResources().getDrawable(R.drawable.icon_tooltip_inactive));
		currentView.addView(tooltipPlayButton);

		ProjectActivity.tooltipActive = true;

		return true;
	}

	public boolean removeMainMenuActivityTooltipButtons() {
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

		MainMenuActivity.tooltipActive = false;

		return true;
	}

	public boolean removeProjectActivityTooltipButtons() {
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
		ProjectActivity.tooltipActive = false;

		return true;
	}

	public boolean setPosition(int x, int y, String text) {
		tooltipPositionX = x;
		tooltipPositionY = y;
		tooltipText = text;
		return true;
	}

	@Override
	public void onDraw(Canvas canvas) {

		if (tooltipText != null) {
			bubble = (NinePatchDrawable) context.getResources().getDrawable(R.drawable.bubble);
			bubble.setBounds(tooltipPositionX - 20, tooltipPositionY - 30, tooltipPositionX + 250,
					tooltipPositionY + 50);
			bubble.draw(canvas);
			drawMultilineText(tooltipText, tooltipPositionX, tooltipPositionY, paint, canvas);
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
