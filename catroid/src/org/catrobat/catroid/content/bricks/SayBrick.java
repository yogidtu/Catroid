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
import android.view.View;
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

import java.util.List;

public class SayBrick extends BubbleBrickBaseType {
	private static final long serialVersionUID = 1L;
	private static final int SECONDS = 2;

	public SayBrick() {
		super();
	}

	public SayBrick(Sprite sprite, String say, Context context) {
		super(sprite, say, SECONDS, context);
	}

	public SayBrick(Sprite sprite, Formula say, Context context) {
		super(sprite, say, new Formula(SECONDS), context);
	}

	@Override
	public Brick copyBrickForSprite(Sprite sprite, Script script) {
		SayBrick copyBrick = (SayBrick) clone();
		copyBrick.sprite = sprite;
		return copyBrick;
	}

	@Override
	public Brick clone() {
		return new SayBrick(getSprite(), text.clone(), context);
	}

	@Override
	public View getView(Context context, int brickId, BaseAdapter baseAdapter) {
		if (animationState) {
			return view;
		}
		view = View.inflate(context, R.layout.brick_say, null);
		view = getViewWithAlpha(alphaValue);
		setCheckboxView(R.id.brick_say_checkbox);

		final Brick brickInstance = this;
		checkbox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				checked = isChecked;
				adapter.handleCheck(brickInstance, isChecked);
			}
		});

		view.findViewById(R.id.brick_say_prototype_text_view).setVisibility(View.GONE);
		TextView sayEditText = (TextView) view.findViewById(R.id.brick_say_edit_text);
		sayEditText.setVisibility(View.VISIBLE);
		sayEditText.setOnClickListener(this);
		text.setTextFieldId(R.id.brick_say_edit_text);
		text.refreshTextField(view);

		return view;
	}

	@Override
	public View getPrototypeView(Context context) {
		prototypeView = View.inflate(context, R.layout.brick_say, null);
		TextView sayPrototypeTextView = (TextView) prototypeView.findViewById(R.id.brick_say_prototype_text_view);
		sayPrototypeTextView.setText(text.interpretString(sprite));
		return prototypeView;
	}

	@Override
	public View getViewWithAlpha(int alphaValue) {
		if (view != null) {
			View layout = view.findViewById(R.id.brick_say_layout);
			layout.getBackground().setAlpha(alphaValue);

			TextView sayTextView = (TextView) view.findViewById(R.id.brick_say_textview);
			sayTextView.setTextColor(sayTextView.getTextColors().withAlpha(alphaValue));
			TextView sayEditText = (TextView) view.findViewById(R.id.brick_say_edit_text);
			sayEditText.setTextColor(sayEditText.getTextColors().withAlpha(alphaValue));
			sayEditText.getBackground().setAlpha(alphaValue);

			this.alphaValue = (alphaValue);
		}
		return view;
	}

	@Override
	public List<SequenceAction> addActionToSequence(SequenceAction sequence) {
		bubble = View.inflate(this.context, R.layout.bubble_speech_new, null);
		((TextView) bubble.findViewById(R.id.bubble_edit_text)).setText(getNormalizedText());
		updateBubbleByteArrayFromDrawingCache();
		sequence.addAction(ExtendedActions.say(sprite, bubbleByteArray, duration));
		return null;
	}
}
