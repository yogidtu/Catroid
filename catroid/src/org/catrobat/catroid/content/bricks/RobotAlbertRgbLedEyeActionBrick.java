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
import org.catrobat.catroid.content.actions.ExtendedActions;
import org.catrobat.catroid.formulaeditor.Formula;
import org.catrobat.catroid.ui.fragment.FormulaEditorFragment;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.badlogic.gdx.scenes.scene2d.actions.SequenceAction;

public class RobotAlbertRgbLedEyeActionBrick extends BrickBaseType implements OnClickListener {
	private static final long serialVersionUID = 1L;

	private transient View prototypeView;

	public static enum Eye {
		Left, Right, Both
	}

	private String eye;
	private transient Eye eyeEnum;
	private transient EditText editRedValue;
	private transient EditText editGreenValue;
	private transient EditText editBlueValue;
	private Formula red;
	private Formula green;
	private Formula blue;

	protected Object readResolve() {
		if (eye != null) {
			eyeEnum = Eye.valueOf(eye);
		}
		return this;
	}

	public RobotAlbertRgbLedEyeActionBrick(Sprite sprite, Eye eye, int red, int green, int blue) {
		this.sprite = sprite;
		this.eyeEnum = eye;
		this.eye = eyeEnum.name();

		this.red = new Formula(red);
		this.green = new Formula(green);
		this.blue = new Formula(blue);
	}

	public RobotAlbertRgbLedEyeActionBrick(Sprite sprite, Eye eye, Formula red, Formula green, Formula blue) {
		this.sprite = sprite;
		this.eyeEnum = eye;
		this.eye = eyeEnum.name();

		this.red = red;
		this.green = green;
		this.blue = blue;
	}

	@Override
	public int getRequiredResources() {
		return BLUETOOTH_ROBOT_ALBERT;
	}

	@Override
	public Brick copyBrickForSprite(Sprite sprite, Script script) {
		RobotAlbertRgbLedEyeActionBrick copyBrick = (RobotAlbertRgbLedEyeActionBrick) clone();
		copyBrick.sprite = sprite;
		return copyBrick;
	}

	@Override
	public View getPrototypeView(Context context) {
		prototypeView = View.inflate(context, R.layout.brick_robot_albert_rgb_eye_action, null);
		TextView textred = (TextView) prototypeView.findViewById(R.id.robot_albert_rgb_led_action_red_text_view);
		textred.setText(String.valueOf(red.interpretInteger(sprite)));
		TextView textgreen = (TextView) prototypeView.findViewById(R.id.robot_albert_rgb_led_action_green_text_view);
		textgreen.setText(String.valueOf(green.interpretInteger(sprite)));
		TextView textblue = (TextView) prototypeView.findViewById(R.id.robot_albert_rgb_led_action_blue_text_view);
		textblue.setText(String.valueOf(blue.interpretInteger(sprite)));
		//TextView textColor = (TextView) prototypeView.findViewById(R.id.robot_albert_rgb_led_action_color_text_view);
		return prototypeView;
	}

	@Override
	public Brick clone() {
		return new RobotAlbertRgbLedEyeActionBrick(getSprite(), eyeEnum, red.clone(), green.clone(), blue.clone());
	}

	@Override
	public View getView(Context context, int brickId, BaseAdapter baseAdapter) {
		if (animationState) {
			return view;
		}

		view = View.inflate(context, R.layout.brick_robot_albert_rgb_eye_action, null);
		setCheckboxView(R.id.brick_robot_albert_rgb_led_action_checkbox);

		final Brick brickInstance = this;
		checkbox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				checked = isChecked;
				adapter.handleCheck(brickInstance, isChecked);
			}
		});

		TextView textRed = (TextView) view.findViewById(R.id.robot_albert_rgb_led_action_red_text_view);
		editRedValue = (EditText) view.findViewById(R.id.robot_albert_rgb_led_action_red_edit_text);
		red.setTextFieldId(R.id.robot_albert_rgb_led_action_red_edit_text);
		red.refreshTextField(view);

		textRed.setVisibility(View.GONE);
		editRedValue.setVisibility(View.VISIBLE);

		editRedValue.setOnClickListener(this);

		TextView textGreen = (TextView) view.findViewById(R.id.robot_albert_rgb_led_action_green_text_view);
		editGreenValue = (EditText) view.findViewById(R.id.robot_albert_rgb_led_action_green_edit_text);
		green.setTextFieldId(R.id.robot_albert_rgb_led_action_green_edit_text);
		green.refreshTextField(view);

		textGreen.setVisibility(View.GONE);
		editGreenValue.setVisibility(View.VISIBLE);

		editGreenValue.setOnClickListener(this);

		TextView textBlue = (TextView) view.findViewById(R.id.robot_albert_rgb_led_action_blue_text_view);
		editBlueValue = (EditText) view.findViewById(R.id.robot_albert_rgb_led_action_blue_edit_text);
		blue.setTextFieldId(R.id.robot_albert_rgb_led_action_blue_edit_text);
		blue.refreshTextField(view);

		textBlue.setVisibility(View.GONE);
		editBlueValue.setVisibility(View.VISIBLE);

		editBlueValue.setOnClickListener(this);

		TextView colorView = (TextView) view.findViewById(R.id.robot_albert_rgb_led_action_color_text_view);
		colorView.setVisibility(View.VISIBLE);
		//update color of the current rgb-selection
		int r = red.interpretInteger(sprite);
		int g = green.interpretInteger(sprite);
		int b = blue.interpretInteger(sprite);
		colorView.setBackgroundColor(Color.rgb(r, g, b));

		if (r > 255) {
			editRedValue.setText("" + 255);
		} else if (r < 0) {
			editRedValue.setText("" + 0);
		}
		if (g > 255) {
			editGreenValue.setText("" + 255);
		} else if (g < 0) {
			editGreenValue.setText("" + 0);
		}
		if (b > 255) {
			editBlueValue.setText("" + 255);
		} else if (b < 0) {
			editBlueValue.setText("" + 0);
		}

		ArrayAdapter<CharSequence> eyeAdapter = ArrayAdapter.createFromResource(context,
				R.array.robot_albert_eye_chooser, android.R.layout.simple_spinner_item);
		eyeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		Spinner eyeSpinner = (Spinner) view.findViewById(R.id.robot_albert_eye_spinner);
		eyeSpinner.setClickable(true);
		eyeSpinner.setEnabled(true);
		eyeSpinner.setAdapter(eyeAdapter);
		eyeSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1, int position, long arg3) {
				eyeEnum = Eye.values()[position];
				eye = eyeEnum.name();
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub

			}

		});

		eyeSpinner.setSelection(eyeEnum.ordinal());

		return view;
	}

	@Override
	public void onClick(View view) {

		//setContentView(R.layout.brick_robot_albert_rgb_eye_action);

		if (checkbox.getVisibility() == View.VISIBLE) {
			return;
		}
		switch (view.getId()) {
			case R.id.robot_albert_rgb_led_action_red_edit_text:
				FormulaEditorFragment.showFragment(view, this, red);
				break;

			case R.id.robot_albert_rgb_led_action_green_edit_text:
				FormulaEditorFragment.showFragment(view, this, green);
				break;

			case R.id.robot_albert_rgb_led_action_blue_edit_text:
				FormulaEditorFragment.showFragment(view, this, blue);
				break;
		}
	}

	@Override
	public View getViewWithAlpha(int alphaValue) {
		LinearLayout layout = (LinearLayout) view.findViewById(R.id.brick_robot_albert_rgb_led_action_layout);
		Drawable background = layout.getBackground();
		background.setAlpha(alphaValue);
		this.alphaValue = (alphaValue);
		return view;
	}

	@Override
	public List<SequenceAction> addActionToSequence(SequenceAction sequence) {
		sequence.addAction(ExtendedActions.RobotAlbertRgbLedEyeAction(sprite, eye, eyeEnum, red, green, blue));
		return null;
	}
}