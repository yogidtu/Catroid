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
import org.catrobat.catroid.content.Sprite;
import org.catrobat.catroid.formulaeditor.Formula;
import org.catrobat.catroid.physics.PhysicObject;
import org.catrobat.catroid.physics.PhysicObjectBrick;
import org.catrobat.catroid.ui.dialogs.BrickTextDialog;

import android.content.Context;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.badlogic.gdx.scenes.scene2d.actions.SequenceAction;

public class SetBounceFactorBrick extends BrickBaseType implements PhysicObjectBrick, OnClickListener {
	private static final long serialVersionUID = 1L;

	private PhysicObject physicObject;
	private Formula bounceFactor;

	private transient View prototypeView;

	public SetBounceFactorBrick() {
	}

	public SetBounceFactorBrick(Sprite sprite, float bounceFactor) {
		this.sprite = sprite;
		this.bounceFactor = new Formula(bounceFactor);
	}

	public SetBounceFactorBrick(Sprite sprite, Formula bounceFactor) {
		this.sprite = sprite;
		this.bounceFactor = bounceFactor;
	}

	@Override
	public int getRequiredResources() {
		return NO_RESOURCES;
	}

	@Override
	public void setPhysicObject(PhysicObject physicObject) {
		this.physicObject = physicObject;
	}

	@Override
	public View getView(Context context, int brickId, BaseAdapter adapter) {
		prototypeView = View.inflate(context, R.layout.brick_set_bounce_factor, null);

		TextView text = (TextView) prototypeView.findViewById(R.id.brick_set_bounce_factor_prototype_text_view);
		EditText edit = (EditText) prototypeView.findViewById(R.id.brick_set_bounce_factor_edit_text);

		edit.setText(String.valueOf(bounceFactor));
		text.setVisibility(View.GONE);
		edit.setVisibility(View.VISIBLE);
		edit.setOnClickListener(this);

		return prototypeView;
	}

	@Override
	public View getPrototypeView(Context context) {
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.brick_set_bounce_factor, null);
		return view;
	}

	@Override
	public Brick clone() {
		return new SetBounceFactorBrick(sprite, bounceFactor);
	}

	@Override
	public void onClick(final View view) {
		ScriptTabActivity activity = (ScriptTabActivity) view.getContext();

		BrickTextDialog editDialog = new BrickTextDialog() {
			@Override
			protected void initialize() {
				input.setText(String.valueOf(bounceFactor));
				input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
				input.setSelectAllOnFocus(true);
			}

			@Override
			protected boolean handleOkButton() {
				try {
					bounceFactor = Float.parseFloat(input.getText().toString());
				} catch (NumberFormatException exception) {
					Toast.makeText(getActivity(), R.string.error_no_number_entered, Toast.LENGTH_SHORT).show();
				}

				return true;
			}
		};

		editDialog.show(activity.getSupportFragmentManager(), "dialog_set_bounce_factor_brick");
	}

	@Override
	public List<SequenceAction> addActionToSequence(SequenceAction sequence) {
		// TODO:
		//		sequence.addAction(ExtendedActions.setX(sprite, xPosition));
		return null;
	}
}