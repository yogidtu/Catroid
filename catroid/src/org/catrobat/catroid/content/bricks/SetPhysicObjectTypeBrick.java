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
import org.catrobat.catroid.content.actions.ExtendedActions;
import org.catrobat.catroid.physics.PhysicObject;
import org.catrobat.catroid.physics.PhysicObject.Type;
import org.catrobat.catroid.physics.PhysicObjectBrick;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;

import com.badlogic.gdx.scenes.scene2d.actions.SequenceAction;

public class SetPhysicObjectTypeBrick extends BrickBaseType implements PhysicObjectBrick {
	private static final long serialVersionUID = 1L;

	private PhysicObject physicObject;
	private Type type;

	public SetPhysicObjectTypeBrick() {
	}

	public SetPhysicObjectTypeBrick(Sprite sprite, PhysicObject.Type type) {
		this.sprite = sprite;
		this.type = type;
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
	public Sprite getSprite() {
		return this.sprite;
	}

	@Override
	public Brick clone() {
		return new SetPhysicObjectTypeBrick(sprite, type);
	}

	@Override
	public View getView(Context context, int brickId, BaseAdapter baseAdapter) {
		if (animationState) {
			return view;
		}

		view = View.inflate(context, R.layout.brick_set_physic_object_type, null);
		view = getViewWithAlpha(alphaValue);

		setCheckboxView(R.id.brick_set_physic_object_checkbox);
		final Brick brickInstance = this;
		checkbox.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				checked = isChecked;
				adapter.handleCheck(brickInstance, isChecked);
			}

		});

		final Spinner spinner = (Spinner) view.findViewById(R.id.brick_set_physic_object_type_spinner);
		spinner.setAdapter(createAdapter(context));
		spinner.setSelection(type.ordinal());

		spinner.setClickable(true);
		spinner.setFocusable(true);
		spinner.setOnItemSelectedListener(new OnItemSelectedListener() {

			private boolean start = true;

			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				if (start) {
					start = false;
					return;
				}

				if (position < PhysicObject.Type.values().length) {
					type = Type.values()[position];
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
			}
		});

		return view;
	}

	private ArrayAdapter<String> createAdapter(Context context) {
		ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item);
		arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

		for (PhysicObject.Type spinnerItem : PhysicObject.Type.values()) {
			String spinnerItemText = spinnerItem.toString();
			spinnerItemText = spinnerItemText.toLowerCase();
			spinnerItemText = spinnerItemText.substring(0, 1).toUpperCase() + spinnerItemText.substring(1);
			arrayAdapter.add(spinnerItemText);
		}
		return arrayAdapter;
	}

	@Override
	public View getViewWithAlpha(int alphaValue) {
		LinearLayout layout = (LinearLayout) view.findViewById(R.id.brick_set_physic_object_layout);
		Drawable background = layout.getBackground();
		background.setAlpha(alphaValue);
		this.alphaValue = (alphaValue);
		return view;
	}

	@Override
	public View getPrototypeView(Context context) {
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.brick_set_physic_object_type, null);
		Spinner pointToSpinner = (Spinner) view.findViewById(R.id.brick_set_physic_object_type_spinner);
		pointToSpinner.setFocusableInTouchMode(false);
		pointToSpinner.setFocusable(false);
		SpinnerAdapter pointToSpinnerAdapter = createAdapter(context);
		pointToSpinner.setAdapter(pointToSpinnerAdapter);
		pointToSpinner.setSelection(PhysicObject.Type.DYNAMIC.ordinal());
		return view;
	}

	@Override
	public List<SequenceAction> addActionToSequence(SequenceAction sequence) {
		sequence.addAction(ExtendedActions.setPhysicObjectType(sprite, physicObject, type));
		return null;
	}
}
