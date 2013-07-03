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
package org.catrobat.catroid.content.bricks;

import org.catrobat.catroid.R;
import org.catrobat.catroid.common.ScreenValues;
import org.catrobat.catroid.content.Script;
import org.catrobat.catroid.content.Sprite;
import org.catrobat.catroid.content.UserScript;
import org.catrobat.catroid.ui.fragment.UserBrickDataEditorFragment;
import org.catrobat.catroid.utils.Utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * @author forestjohnson
 * 
 */

public class UserScriptDefinitionBrick extends ScriptBrick implements OnClickListener {
	private UserScript userScript;
	private UserBrick brick;
	private static final long serialVersionUID = 1L;

	public UserScriptDefinitionBrick(Sprite sprite, UserBrick brick) {
		this.setUserScript(new UserScript(sprite, this));
		this.sprite = sprite;
		this.brick = brick;
	}

	@Override
	public int getRequiredResources() {
		return NO_RESOURCES;
	}

	public void appendBrickToScript(Brick brick) {
		userScript.addBrick(brick);
	}

	@Override
	public Brick copyBrickForSprite(Sprite sprite, Script script) {
		UserScriptDefinitionBrick copyBrick = (UserScriptDefinitionBrick) clone();
		copyBrick.sprite = sprite;
		copyBrick.setUserScript((UserScript) script);
		return copyBrick;
	}

	@Override
	public View getView(final Context context, int brickId, final BaseAdapter baseAdapter) {
		if (animationState) {
			return view;
		}

		view = View.inflate(context, R.layout.brick_user_definition, null);

		setCheckboxView(R.id.brick_user_definition_checkbox);

		onLayoutChanged(view);

		return view;
	}

	public void onLayoutChanged(View currentView) {
		Context context = currentView.getContext();

		LinearLayout layout = (LinearLayout) currentView.findViewById(R.id.brick_user_definition_layout);
		if (layout.getChildCount() > 0) {
			layout.removeAllViews();
		}

		View prototype = brick.getPrototypeView(context);
		Bitmap brickImage = getBrickImage(prototype);

		ImageView preview = getBorderedPreview(brickImage);

		TextView define = new TextView(context);
		define.setTextAppearance(context, R.style.BrickText_Multiple);
		define.setText("\ndefine  ");

		define.setGravity(Gravity.CENTER_HORIZONTAL);

		// This stuff isn't being included by the style when I use setTextAppearance.
		define.setFocusable(false);
		define.setFocusableInTouchMode(false);
		define.setClickable(false);

		layout.addView(define);

		layout.addView(preview);
	}

	private Bitmap getBrickImage(View view) {

		boolean drawingCacheEnabled = view.isDrawingCacheEnabled();

		view.setDrawingCacheEnabled(true);

		view.measure(MeasureSpec.makeMeasureSpec(ScreenValues.SCREEN_WIDTH, MeasureSpec.EXACTLY),
				MeasureSpec.makeMeasureSpec(Utils.getPhysicalPixels(400, view.getContext()), MeasureSpec.AT_MOST));
		view.layout(0, 0, ScreenValues.SCREEN_WIDTH, view.getMeasuredHeight());

		view.setDrawingCacheBackgroundColor(Color.TRANSPARENT);
		view.buildDrawingCache(true);

		if (view.getDrawingCache() == null) {
			view.setDrawingCacheEnabled(drawingCacheEnabled);
			return null;
		}

		Bitmap bitmap = Bitmap.createBitmap(view.getDrawingCache());
		view.setDrawingCacheEnabled(drawingCacheEnabled);

		return bitmap;
	}

	public ImageView getBorderedPreview(Bitmap bitmap) {
		ImageView imageView = new ImageView(view.getContext());
		imageView.setBackgroundColor(Color.TRANSPARENT);

		int radius = 7;

		Bitmap result = getWithBorder(radius, bitmap, Color.argb(Math.round(0.25f * 255), 0, 0, Math.round(0.1f * 255)));

		imageView.setImageBitmap(result);

		return imageView;
	}

	@Override
	public View getViewWithAlpha(int alphaValue) {
		LinearLayout layout = (LinearLayout) view.findViewById(R.id.brick_user_definition_layout);
		Drawable background = layout.getBackground();
		background.setAlpha(alphaValue);
		this.alphaValue = (alphaValue);
		return view;
	}

	@Override
	public View getPrototypeView(Context context) {
		return getView(context, 0, null);
	}

	@Override
	public void onClick(View eventOrigin) {
		if (checkbox.getVisibility() == View.VISIBLE) {
			return;
		}

		UserBrickDataEditorFragment.showFragment(view, brick);
	}

	@Override
	public Brick clone() {
		return new UserScriptDefinitionBrick(getSprite(), brick);
	}

	@Override
	public Script initScript(Sprite sprite) {
		if (getUserScript() == null) {
			setUserScript(new UserScript(sprite, this));
		}

		return getUserScript();
	}

	public UserScript getUserScript() {
		return userScript;
	}

	public void setUserScript(UserScript userScript) {
		this.userScript = userScript;
	}

	public Bitmap getWithBorder(int radius, Bitmap bitmap, int color) {

		int w = bitmap.getWidth();
		int h = bitmap.getHeight();
		int w2 = w + radius * 2;
		int h2 = h + radius * 2;

		Bitmap toReturn = Bitmap.createBitmap(w2, h2, Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(toReturn);

		Bitmap border = Bitmap.createBitmap(w2, h2, Bitmap.Config.ARGB_8888);
		Canvas borderCanvas = new Canvas(border);

		Bitmap alpha = bitmap.extractAlpha();

		Paint paintBorder = new Paint();
		paintBorder.setColor(Color.WHITE);
		Paint paintBorder2 = new Paint();
		paintBorder2.setColor(color);
		Paint paint = new Paint();

		borderCanvas.drawBitmap(alpha, 0, 0, paintBorder);
		borderCanvas.drawBitmap(alpha, radius * 2, 0, paintBorder);
		borderCanvas.drawBitmap(alpha, 0, radius * 2, paintBorder);
		borderCanvas.drawBitmap(alpha, radius * 2, radius * 2, paintBorder);

		alpha = border.extractAlpha();

		canvas.drawBitmap(alpha, 0, 0, paintBorder2);
		canvas.drawBitmap(bitmap, radius, radius, paint);

		return toReturn;
	}
}
