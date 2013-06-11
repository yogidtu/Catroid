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

import java.util.List;

import org.catrobat.catroid.R;
import org.catrobat.catroid.content.Script;
import org.catrobat.catroid.content.Sprite;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout;

import com.badlogic.gdx.scenes.scene2d.actions.SequenceAction;

public class UserBrick extends BrickBaseType implements OnClickListener {
	private static final long serialVersionUID = 1L;

	private transient View prototypeView;

	public UserBrick() {

	}

	public UserBrick(Sprite sprite) { //, Formula xMovement) {
		this.sprite = sprite;

		//this.xMovement = xMovement;
	}

	@Override
	public int getRequiredResources() {

		// todo aggregate resources required by children
		return NO_RESOURCES;
	}

	@Override
	public UserBrick copyBrickForSprite(Sprite sprite, Script script) {
		UserBrick copyBrick = (UserBrick) clone();
		copyBrick.sprite = sprite;
		return copyBrick;
	}

	@Override
	public View getView(Context context, int brickId, BaseAdapter baseAdapter) {
		if (animationState) {
			return view;
		}

		view = View.inflate(context, R.layout.brick_user, null);
		view = getViewWithAlpha(alphaValue);

		setCheckboxView(R.id.brick_user_checkbox);
		final Brick brickInstance = this;

		checkbox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				checked = isChecked;
				adapter.handleCheck(brickInstance, isChecked);
			}
		});
		//TextView textX = (TextView) view.findViewById(R.id.brick_change_x_prototype_text_view);
		//EditText editX = (EditText) view.findViewById(R.id.brick_change_x_edit_text);
		//xMovement.setTextFieldId(R.id.brick_change_x_edit_text);
		//xMovement.refreshTextField(view);

		//textX.setVisibility(View.GONE);
		//editX.setVisibility(View.VISIBLE);
		//editX.setOnClickListener(this);
		return view;
	}

	@Override
	public View getPrototypeView(Context context) {
		prototypeView = View.inflate(context, R.layout.brick_user, null);
		//TextView textXMovement = (TextView) prototypeView.findViewById(R.id.brick_change_x_prototype_text_view);
		//textXMovement.setText(String.valueOf(xMovement.interpretInteger(sprite)));
		return prototypeView;
	}

	@Override
	public Brick clone() {
		return new UserBrick(getSprite());
	}

	@Override
	public View getViewWithAlpha(int alphaValue) {
		LinearLayout layout = (LinearLayout) view.findViewById(R.id.brick_user_layout);
		Drawable background = layout.getBackground();
		background.setAlpha(alphaValue);

		//TextView changeXByLabel = (TextView) view.findViewById(R.id.brick_user_label);
		//EditText editChangeSize = (EditText) view.findViewById(R.id.brick_change_x_edit_text);
		//changeXByLabel.setTextColor(changeXByLabel.getTextColors().withAlpha(alphaValue));
		//editChangeSize.setTextColor(editChangeSize.getTextColors().withAlpha(alphaValue));
		//editChangeSize.getBackground().setAlpha(alphaValue);

		this.alphaValue = (alphaValue);
		return view;
	}

	@Override
	public void onClick(View view) {
		if (checkbox.getVisibility() == View.VISIBLE) {
			return;
		}
		//FormulaEditorFragment.showFragment(view, this, xMovement);
	}

	@Override
	public List<SequenceAction> addActionToSequence(SequenceAction sequence) {
		//sequence.addAction(ExtendedActions.actionSequence(sprite, ));
		return null;
	}
}
