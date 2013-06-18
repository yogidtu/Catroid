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
import org.catrobat.catroid.content.Script;
import org.catrobat.catroid.content.Sprite;
import org.catrobat.catroid.content.UserScript;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;

/**
 * @author forestjohnson
 * 
 */

public class UserScriptDefinitionBrick extends ScriptBrick {
	private UserScript userScript;
	private static final long serialVersionUID = 1L;

	public UserScriptDefinitionBrick(Sprite sprite) {
		this.setUserScript(new UserScript(sprite, this));
		this.sprite = sprite;
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

		return view;
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
	public Brick clone() {
		return new UserScriptDefinitionBrick(getSprite());
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
}
