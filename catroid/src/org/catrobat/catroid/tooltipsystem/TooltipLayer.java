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
import org.catrobat.catroid.ui.ProgramMenuActivity;
import org.catrobat.catroid.ui.ProjectActivity;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
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
	private NinePatchDrawable bubble;

	private int tooltipPositionX;
	private int tooltipPositionY;
	private String tooltipText;

	private CoordinatesCollector collector;

	public TooltipLayer(Context context) {
		super(context);
		this.context = context;
		this.setBackgroundColor(Color.CYAN);
		this.getBackground().setAlpha(0);
		this.setZOrderOnTop(true); //necessary 
		getHolder().setFormat(PixelFormat.TRANSPARENT);
		getHolder().addCallback(this);
		Activity currentActivity = (Activity) context;
		currentActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);
		paint.setTextSize(15);
		paint.setARGB(255, 0, 0, 0);

		collector = new CoordinatesCollector();
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		boolean returnValue = true;

		if (!collector.isActionBarClicked(ev)) {
			switch (Tooltip.getInstance(context).checkActivity()) {
				case 0:
					mainMenuTooltipClicked(ev);
					break;
				case 1:
					projectTooltipClicked(ev);
					break;
				case 3:
					programMenuTooltipClicked(ev);
					break;
			}
			returnValue = Tooltip.getInstance(context).dispatchTouchEvent(ev);
		} else {
			dispatchMenuButtonActionBarClick(ev);
			returnValue = Tooltip.getInstance(context).dispatchTouchEvent(ev);
		}
		return returnValue;
	}

	private void mainMenuTooltipClicked(MotionEvent ev) {
		if (collector.isOnMainMenuActivityContinueTooltipPosition(ev)) {
			showTooltipBubble(R.string.hint_mainmenu_continue);
		} else if (collector.isOnMainMenuActivityNewTooltipPosition(ev)) {
			showTooltipBubble(R.string.hint_mainmenu_new);
		} else if (collector.isOnMainMenuActivityProgramsTooltipPosition(ev)) {
			showTooltipBubble(R.string.hint_mainmenu_programs);
		} else if (collector.isOnMainMenuActivityForumTooltipPosition(ev)) {
			showTooltipBubble(R.string.hint_mainmenu_community);
		} else if (collector.isOnMainMenuActivityWebTooltipPosition(ev)) {
			showTooltipBubble(R.string.hint_mainmenu_web);
		} else if (collector.isOnMainMenuActivityUploadTooltipPosition(ev)) {
			showTooltipBubble(R.string.hint_mainmenu_upload);
		}
	}

	private void projectTooltipClicked(MotionEvent ev) {
		if (collector.isOnProjectActivityBackgroundTooltipPosition(ev)) {
			showTooltipBubble(R.string.hint_project_background);
		} else if (collector.isOnProjectActivityObjectTooltipPosition(ev)) {
			showTooltipBubble(R.string.hint_project_objects);
		} else if (collector.isOnProjectActivityAddButtonTooltipPosition(ev)) {
			showTooltipBubble(R.string.hint_project_add);
		} else if (collector.isOnProjectActivityPlayButtonTooltipPosition(ev)) {
			showTooltipBubble(R.string.hint_project_play);
		}
	}

	private void programMenuTooltipClicked(MotionEvent ev) {
		if (collector.isOnProgramMenuActivityScriptsTooltipPosition(ev)) {
			showTooltipBubble(R.string.hint_programmenu_scripts);
		} else if (collector.isOnProgramMenuActivityLooksTooltipPosition(ev)) {
			showTooltipBubble(R.string.hint_programmenu_looks);
		} else if (collector.isOnProgramMenuActivitySoundsTooltipPosition(ev)) {
			showTooltipBubble(R.string.hint_programmenu_sounds);
		} else if (collector.isOnProgramMenuActivityPlayButtonTooltipPosition(ev)) {
			showTooltipBubble(R.string.hint_programmenu_play);
		}
	}

	private void showTooltipBubble(int stringId) {
		Tooltip.getInstance(context).stopTooltipSystem();
		TooltipObject tooltipObject = Tooltip.getInstance(context).getTooltipObjectForScreenObject(stringId);
		Tooltip.getInstance(context).startTooltipSystem();
		Tooltip.getInstance(context).setTooltipPosition(tooltipObject.getTextXCoordinate(),
				tooltipObject.getTextYCoordinate(), tooltipObject.getTooltipText());
	}

	private boolean dispatchMenuButtonActionBarClick(MotionEvent ev) {
		if (collector.isMenuButtonActionBarPosition(ev)) {
			Tooltip.getInstance(context).stopTooltipSystem();
			Activity activity = (Activity) context;
			activity.openOptionsMenu();
			return true;
		}
		return false;
	}

	public boolean addTooltipButtonsToMainMenuActivity() {
		MainMenuActivity activity = (MainMenuActivity) context;

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
		ProjectActivity activity = (ProjectActivity) context;

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

	public boolean addTooltipButtonsToProgramMenuActivity() {
		ProgramMenuActivity activity = (ProgramMenuActivity) context;

		Button button = (Button) activity.findViewById(R.id.program_menu_button_scripts);
		button.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_program_menu_scripts, 0,
				R.drawable.icon_tooltip_inactive, 0);
		button = (Button) activity.findViewById(R.id.program_menu_button_looks);
		button.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_program_menu_looks, 0,
				R.drawable.icon_tooltip_inactive, 0);
		button = (Button) activity.findViewById(R.id.program_menu_button_sounds);
		button.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_program_menu_sounds, 0,
				R.drawable.icon_tooltip_inactive, 0);

		LinearLayout currentView = (LinearLayout) activity.findViewById(R.id.button_play);
		ImageView tooltipPlayButton = new ImageView(context);
		tooltipPlayButton.setImageDrawable(context.getResources().getDrawable(R.drawable.icon_tooltip_inactive));
		currentView.addView(tooltipPlayButton);

		ProgramMenuActivity.tooltipActive = true;
		return true;
	}

	public boolean removeProgramMenuActivityTooltipButtons() {
		Activity activity = (Activity) context;

		Button button = (Button) activity.findViewById(R.id.program_menu_button_scripts);
		button.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_program_menu_scripts, 0,
				R.drawable.ic_arrow_right_dark, 0);
		button = (Button) activity.findViewById(R.id.program_menu_button_looks);
		button.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_program_menu_looks, 0,
				R.drawable.ic_arrow_right_dark, 0);
		button = (Button) activity.findViewById(R.id.program_menu_button_sounds);
		button.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_program_menu_sounds, 0,
				R.drawable.ic_arrow_right_dark, 0);

		LinearLayout buttonLayout = (LinearLayout) activity.findViewById(R.id.button_play);
		View v = buttonLayout.getChildAt(1);
		buttonLayout.removeView(v);
		ProgramMenuActivity.tooltipActive = false;

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
		ProjectActivity activity = (ProjectActivity) context;

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
		boolean returnValue = false;
		Tooltip tooltip = Tooltip.getInstance(context);
		if (x >= 0 && x < tooltip.getScreenWidth()) {
			tooltipPositionX = x;
			returnValue = true;
		} else {
			tooltipPositionX = 0;
			returnValue = true;
		}

		if (y >= 0 && y < tooltip.getScreenHeight()) {
			tooltipPositionY = y;
			returnValue = true;
		} else {
			tooltipPositionY = 0;
			returnValue = true;
		}

		if (text != null) {
			tooltipText = text;
			returnValue = true;
		} else {
			tooltipText = "";
			returnValue = true;
		}
		return returnValue;
	}

	public int getTooltipPositionX() {
		return tooltipPositionX;
	}

	public int getTooltipPositionY() {
		return tooltipPositionY;
	}

	public String getTooltipText() {
		return tooltipText;
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
