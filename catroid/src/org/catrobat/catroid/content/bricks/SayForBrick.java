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

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;

import com.badlogic.gdx.scenes.scene2d.actions.SequenceAction;

import org.catrobat.catroid.R;
import org.catrobat.catroid.content.Script;
import org.catrobat.catroid.content.Sprite;
import org.catrobat.catroid.content.actions.ExtendedActions;
import org.catrobat.catroid.formulaeditor.Formula;
import org.catrobat.catroid.ui.fragment.FormulaEditorFragment;

import java.io.ByteArrayOutputStream;
import java.util.List;

public class SayForBrick extends BrickBaseType implements OnClickListener, FormulaBrick {
	private static final long serialVersionUID = 1L;
	private Formula text;
	private Formula duration;
	private transient View prototypeView;

	public SayForBrick() {
	}

	public SayForBrick(Sprite sprite, String say, int seconds) {
		this.sprite = sprite;
		this.text = new Formula(say);
		this.duration = new Formula(seconds);
	}

	public SayForBrick(Sprite sprite, Formula say, Formula seconds) {
		this.sprite = sprite;
		this.text = say;
		this.duration = seconds;
	}

	@Override
	public Formula getFormula() {
		return text;
	}

	//	public void setXPosition(Formula xPosition) {
	//		this.text = xPosition;
	//	}
	//
	//	public void setYPosition(Formula yPosition) {
	//		this.seconds = yPosition;
	//	}

	@Override
	public int getRequiredResources() {
		return NO_RESOURCES;
	}

	@Override
	public Brick copyBrickForSprite(Sprite sprite, Script script) {
		SayForBrick copyBrick = (SayForBrick) clone();
		copyBrick.sprite = sprite;
		return copyBrick;
	}

	@Override
	public View getView(Context context, int brickId, BaseAdapter baseAdapter) {
		if (animationState) {
			return view;
		}

		view = View.inflate(context, R.layout.brick_say_for, null);
		view = getViewWithAlpha(alphaValue);
		setCheckboxView(R.id.brick_say_for_checkbox);

		final Brick brickInstance = this;
		checkbox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				checked = isChecked;
				adapter.handleCheck(brickInstance, isChecked);
			}
		});

		TextView sayPrototypeTextView = (TextView) view.findViewById(R.id.brick_say_prototype_text_view);
		sayPrototypeTextView.setVisibility(View.GONE);
		TextView sayEditText = (TextView) view.findViewById(R.id.brick_say_edit_text);
		sayEditText.setVisibility(View.VISIBLE);
		sayEditText.setOnClickListener(this);
		text.setTextFieldId(R.id.brick_say_edit_text);
		text.refreshTextField(view);

		TextView durationPrototypeTextView = (TextView) view.findViewById(R.id.brick_for_prototype_text_view);
		durationPrototypeTextView.setVisibility(View.GONE);
		TextView durationEditText = (TextView) view.findViewById(R.id.brick_for_edit_text);
		durationEditText.setVisibility(View.VISIBLE);
		durationEditText.setOnClickListener(this);
		duration.setTextFieldId(R.id.brick_for_edit_text);
		duration.refreshTextField(view);

		return view;
	}

	@Override
	public View getPrototypeView(Context context) {
		prototypeView = View.inflate(context, R.layout.brick_say_for, null);
		TextView sayPrototypeTextView = (TextView) prototypeView.findViewById(R.id.brick_say_prototype_text_view);
		sayPrototypeTextView.setText(text.interpretString(sprite));
		TextView durationPrototypeTextview = (TextView) prototypeView.findViewById(R.id.brick_for_prototype_text_view);
		durationPrototypeTextview.setText(String.valueOf(duration.interpretInteger(sprite)));
		return prototypeView;
	}

	@Override
	public Brick clone() {
		return new SayForBrick(getSprite(), text.clone(), duration.clone());
	}

	@Override
	public View getViewWithAlpha(int alphaValue) {
		if (view != null) {
			View layout = view.findViewById(R.id.brick_say_for_layout);
			Drawable background = layout.getBackground();
			background.setAlpha(alphaValue);

			TextView sayTextView = (TextView) view.findViewById(R.id.brick_say_textview);
			sayTextView.setTextColor(sayTextView.getTextColors().withAlpha(alphaValue));
			TextView durationTextView = (TextView) view.findViewById(R.id.brick_for_textview);
			durationTextView.setTextColor(durationTextView.getTextColors().withAlpha(alphaValue));
			TextView sayEditText = (TextView) view.findViewById(R.id.brick_say_edit_text);
			sayEditText.setTextColor(sayEditText.getTextColors().withAlpha(alphaValue));
			sayEditText.getBackground().setAlpha(alphaValue);
			TextView durationEditText = (TextView) view.findViewById(R.id.brick_for_edit_text);
			durationEditText.setTextColor(durationEditText.getTextColors().withAlpha(alphaValue));
			durationEditText.getBackground().setAlpha(alphaValue);

			this.alphaValue = (alphaValue);
		}
		return view;
	}

	@Override
	public void onClick(View view) {
		if (checkbox.getVisibility() == View.VISIBLE) {
			return;
		}
		switch (view.getId()) {
			case R.id.brick_say_edit_text:
				FormulaEditorFragment.showFragment(view, this, text);
				break;

			case R.id.brick_for_edit_text:
				FormulaEditorFragment.showFragment(view, this, duration);
				break;
		}
	}

	@Override
	public List<SequenceAction> addActionToSequence(SequenceAction sequence) {
		Context context = view.getContext();
		View bubble = View.inflate(context, R.layout.bubble_speech, null);
		//		((TextView) bubble.findViewById(R.id.bubble_edit_text)).setText(String.valueOf("easy"));
		bubble.setDrawingCacheEnabled(true);
		bubble.measure(MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
				MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
		bubble.layout(0, 0, bubble.getWidth(), bubble.getHeight());
		bubble.buildDrawingCache();
		Bitmap bitmap = bubble.getDrawingCache();

		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		bitmap.compress(Bitmap.CompressFormat.PNG, 90, stream);
		byte[] speechBubble = stream.toByteArray();
		bubble.setDrawingCacheEnabled(false);
		// TODO at the text!
		sequence.addAction(ExtendedActions.say(sprite, speechBubble, duration));
		return null;
	}
}
