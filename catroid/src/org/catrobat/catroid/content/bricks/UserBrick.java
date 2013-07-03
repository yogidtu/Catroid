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

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.catrobat.catroid.R;
import org.catrobat.catroid.content.Script;
import org.catrobat.catroid.content.Sprite;
import org.catrobat.catroid.content.actions.ExtendedActions;
import org.catrobat.catroid.formulaeditor.Formula;
import org.catrobat.catroid.ui.BrickLayout;
import org.catrobat.catroid.ui.fragment.FormulaEditorFragment;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.TextView;

import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.actions.SequenceAction;

/**
 * @author forestjohnson
 * 
 */

public class UserBrick extends BrickBaseType implements OnClickListener {
	private static final long serialVersionUID = 1L;

	private UserScriptDefinitionBrick definitionBrick;
	private transient View prototypeView;

	// belonging to brick instance
	private LinkedList<UserBrickUIComponent> uiComponents;

	// belonging to stored brick
	public LinkedList<UserBrickUIData> uiData;

	public UserBrick(Sprite sprite) {
		this.sprite = sprite;
		sprite.addUserBrick(this);
		uiData = new LinkedList<UserBrickUIData>();
		this.definitionBrick = new UserScriptDefinitionBrick(sprite, this);
		updateUIComponents();
	}

	public UserBrick(Sprite sprite, LinkedList<UserBrickUIData> uiData, UserScriptDefinitionBrick definitionBrick) {
		this.sprite = sprite;
		this.uiData = uiData;
		this.definitionBrick = definitionBrick;
		updateUIComponents();
	}

	@Override
	public int getRequiredResources() {

		// @TODO aggregate resources required by children
		return NO_RESOURCES;
	}

	@Override
	public UserBrick copyBrickForSprite(Sprite sprite, Script script) {
		UserBrick copyBrick = (UserBrick) clone();
		copyBrick.sprite = sprite;
		return copyBrick;
	}

	public void addUILocalizedStringByName(String key) {
		UserBrickUIData comp = new UserBrickUIData();
		comp.isField = false;
		comp.hasLocalizedString = true;
		comp.localizedStringKey = key;
		uiData.add(comp);
		updateUIComponents();
	}

	public void addUIText(String text) {
		UserBrickUIData comp = new UserBrickUIData();
		comp.isField = false;
		comp.hasLocalizedString = false;
		comp.userDefinedName = text;
		uiData.add(comp);
		updateUIComponents();
	}

	public void addUIField(String id) {
		UserBrickUIData comp = new UserBrickUIData();
		comp.isField = true;
		comp.userDefinedName = id;
		comp.hasLocalizedString = false;
		uiData.add(comp);
		updateUIComponents();
	}

	public Iterator<UserBrickUIComponent> getUIComponentIterator() {
		return uiComponents.iterator();
	}

	private void updateUIComponents() {
		uiComponents = new LinkedList<UserBrickUIComponent>();
		for (UserBrickUIData d : uiData) {
			UserBrickUIComponent c = new UserBrickUIComponent();
			c.data = d;
			if (c.data.isField) {
				c.fieldFormula = new Formula(0);
			}
			uiComponents.add(c);
		}
	}

	public void appendBrickToScript(Brick brick) {
		definitionBrick.appendBrickToScript(brick);
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

		onLayoutChanged(view);

		return view;
	}

	@Override
	public View getPrototypeView(Context context) {
		prototypeView = View.inflate(context, R.layout.brick_user, null);

		onLayoutChanged(prototypeView);

		return prototypeView;
	}

	@Override
	public View getViewWithAlpha(int alphaValue) {

		boolean needsRefresh = false;
		for (UserBrickUIComponent c : uiComponents) {
			if (c.textView == null) {
				needsRefresh = true;
			}
		}
		if (needsRefresh) {
			onLayoutChanged(view);
		}

		BrickLayout layout = (BrickLayout) view.findViewById(R.id.brick_user_flow_layout);
		Drawable background = layout.getBackground();
		background.setAlpha(alphaValue);

		for (UserBrickUIComponent c : uiComponents) {
			c.textView.setTextColor(c.textView.getTextColors().withAlpha(alphaValue));
			if (c.data.isField) {
				c.textView.getBackground().setAlpha(alphaValue);
			}
		}

		this.alphaValue = (alphaValue);
		return view;
	}

	public void onLayoutChanged(View currentView) {

		boolean prototype = (currentView == prototypeView);

		Context context = currentView.getContext();

		BrickLayout layout2 = (BrickLayout) currentView.findViewById(R.id.brick_user_flow_layout);
		if (layout2.getChildCount() > 0) {
			layout2.removeAllViews();
		}

		//FlowLayout layout2 = (FlowLayout) View.inflate(context, R.layout.brick_user_flow_layout_template, null);

		//((LinearLayout) currentView).addView(layout2);

		for (UserBrickUIComponent c : uiComponents) {
			TextView currentTextView = null;
			if (c.data.isField) {
				currentTextView = new EditText(context);

				if (prototype) {
					currentTextView.setTextAppearance(context, R.style.BrickPrototypeTextView);
					currentTextView.setText(String.valueOf(c.fieldFormula.interpretInteger(sprite)));
				} else {
					currentTextView.setTextAppearance(context, R.style.BrickEditText);
					c.fieldFormula.setTextFieldId(currentTextView.getId());
					c.fieldFormula.refreshTextField(view);

					currentTextView.setOnClickListener(this);
				}
				currentTextView.setVisibility(View.VISIBLE);
			} else {
				currentTextView = new TextView(context);
				currentTextView.setTextAppearance(context, R.style.BrickText_Multiple);

				currentTextView.setText(c.data.getString(context));
			}

			// This stuff isn't being included by the style when I use setTextAppearance.
			if (prototype) {
				currentTextView.setFocusable(false);
				currentTextView.setFocusableInTouchMode(false);
				currentTextView.setClickable(false);
			}

			layout2.addView(currentTextView);

			if (prototype) {
				c.prototypeView = currentTextView;
			} else {
				c.textView = currentTextView;
			}
		}
	}

	public String getName(Context context) {
		String name = "";
		for (UserBrickUIData d : uiData) {
			if (!d.isField) {
				name = d.getString(context);
				break;
			}
		}
		return name;
	}

	@Override
	public Brick clone() {
		return new UserBrick(getSprite(), uiData, definitionBrick);
	}

	@Override
	public void onClick(View eventOrigin) {
		if (checkbox.getVisibility() == View.VISIBLE) {
			return;
		}

		for (UserBrickUIComponent c : uiComponents) {
			if (c.data.isField && c.textView.getId() == eventOrigin.getId()) {
				FormulaEditorFragment.showFragment(view, this, c.fieldFormula);
			}
		}
	}

	@Override
	public List<SequenceAction> addActionToSequence(SequenceAction sequence) {
		SequenceAction userSequence = ExtendedActions.sequence();
		Script userScript = getDefinitionBrick().initScript(sprite); // getScript
		userScript.run(userSequence);

		LinkedList<SequenceAction> returnActionList = new LinkedList<SequenceAction>();
		returnActionList.add(userSequence);

		Action action = ExtendedActions.userBrick(sprite, userSequence);
		sequence.addAction(action);
		return returnActionList;
	}

	public UserScriptDefinitionBrick getDefinitionBrick() {
		return definitionBrick;
	}

	public void setDefinitionBrick(UserScriptDefinitionBrick definitionBrick) {
		this.definitionBrick = definitionBrick;
	}
}