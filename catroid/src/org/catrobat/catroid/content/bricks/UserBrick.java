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

import java.util.ArrayList;
import java.util.Iterator;
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
	private transient ArrayList<UserBrickUIComponent> uiComponents;

	// belonging to stored brick
	public UserBrickUIDataArray uiData;
	private int lastDataVersion = 0;

	public UserBrick(Sprite sprite) {
		this.sprite = sprite;
		sprite.addUserBrick(this);
		uiData = new UserBrickUIDataArray();
		this.definitionBrick = new UserScriptDefinitionBrick(sprite, this);
		updateUIComponents();
	}

	public UserBrick(Sprite sprite, UserBrickUIDataArray uiData, UserScriptDefinitionBrick definitionBrick) {
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
		UserBrick copyBrick = clone();
		copyBrick.sprite = sprite;
		return copyBrick;
	}

	public int addUILocalizedString(int id) {
		UserBrickUIData comp = new UserBrickUIData();
		comp.isVariable = false;
		comp.hasLocalizedString = true;
		comp.localizedStringId = id;
		uiData.add(comp);
		uiData.version++;
		return uiData.size() - 1;
	}

	public int addUIText(String text) {
		UserBrickUIData comp = new UserBrickUIData();
		comp.isVariable = false;
		comp.hasLocalizedString = false;
		comp.userDefinedName = text;
		uiData.add(comp);
		uiData.version++;
		return uiData.size() - 1;
	}

	public int addUILocalizedVariable(int id) {
		UserBrickUIData comp = new UserBrickUIData();
		comp.isVariable = true;
		comp.hasLocalizedString = true;
		comp.localizedStringId = id;
		uiData.add(comp);
		uiData.version++;
		return uiData.size() - 1;
	}

	public int addUIVariable(String id) {
		UserBrickUIData comp = new UserBrickUIData();
		comp.isVariable = true;
		comp.userDefinedName = id;
		comp.hasLocalizedString = false;
		uiData.add(comp);
		uiData.version++;
		return uiData.size() - 1;
	}

	public void removeDataAt(int id) {
		uiData.remove(id);
		uiData.version++;
	}

	public boolean isInstanceOf(UserBrick b) {
		return (b.uiData == uiData);
	}

	public Iterator<UserBrickUIComponent> getUIComponentIterator() {
		return uiComponents.iterator();
	}

	private void updateUIComponents() {
		uiComponents = new ArrayList<UserBrickUIComponent>();

		for (int i = 0; i < uiData.size(); i++) {
			UserBrickUIComponent c = new UserBrickUIComponent();
			c.dataIndex = i;
			if (uiData.get(i).isVariable) {
				c.variableFormula = new Formula(0);
			}
			uiComponents.add(c);
		}
	}

	/**
	 * Removes element at <b>from</b> and adds it after element at <b>to</b>
	 */
	public void reorderUIData(int from, int to) {

		if (to == -1) {
			UserBrickUIData d = uiData.remove(from);
			uiData.add(0, d);
		} else if (from <= to) {
			UserBrickUIData d = uiData.remove(from);
			uiData.add(to, d);
		} else {
			// from > to
			UserBrickUIData d = uiData.remove(from);
			uiData.add(to + 1, d);
		}
		uiData.version++;
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
		if (lastDataVersion < uiData.version) {
			updateUIComponents();
			onLayoutChanged(view);
		}

		BrickLayout layout = (BrickLayout) view.findViewById(R.id.brick_user_flow_layout);
		Drawable background = layout.getBackground();
		background.setAlpha(alphaValue);

		for (UserBrickUIComponent c : uiComponents) {
			if (c != null && c.textView != null) {
				UserBrickUIData d = uiData.get(c.dataIndex);
				c.textView.setTextColor(c.textView.getTextColors().withAlpha(alphaValue));
				if (d.isVariable) {
					c.textView.getBackground().setAlpha(alphaValue);
				}
			}
		}

		this.alphaValue = (alphaValue);
		return view;
	}

	public void onLayoutChanged(View currentView) {
		if (lastDataVersion < uiData.version) {
			updateUIComponents();
		}

		boolean prototype = (currentView == prototypeView);

		Context context = currentView.getContext();

		BrickLayout layout2 = (BrickLayout) currentView.findViewById(R.id.brick_user_flow_layout);
		if (layout2.getChildCount() > 0) {
			layout2.removeAllViews();
		}

		for (UserBrickUIComponent c : uiComponents) {
			TextView currentTextView = null;
			UserBrickUIData d = uiData.get(c.dataIndex);
			if (d.isVariable) {
				currentTextView = new EditText(context);

				if (prototype) {
					currentTextView.setTextAppearance(context, R.style.BrickPrototypeTextView);
					currentTextView.setText(String.valueOf(c.variableFormula.interpretInteger(sprite)));
				} else {
					currentTextView.setTextAppearance(context, R.style.BrickEditText);
					currentTextView.setText(String.valueOf(c.variableFormula.interpretInteger(sprite)));
					c.variableFormula.setTextFieldId(currentTextView.getId());
					c.variableFormula.refreshTextField(view);

					currentTextView.setOnClickListener(this);
				}
				currentTextView.setVisibility(View.VISIBLE);
			} else {
				currentTextView = new TextView(context);
				currentTextView.setTextAppearance(context, R.style.BrickText_Multiple);

				currentTextView.setText(d.getString(context));
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

	public CharSequence getName(Context context) {
		CharSequence name = "";
		for (UserBrickUIData d : uiData) {
			if (!d.isVariable) {
				name = d.getString(context);
				break;
			}
		}
		return name;
	}

	@Override
	public UserBrick clone() {
		return new UserBrick(getSprite(), uiData, definitionBrick);
	}

	@Override
	public void onClick(View eventOrigin) {
		if (checkbox.getVisibility() == View.VISIBLE) {
			return;
		}

		for (UserBrickUIComponent c : uiComponents) {
			UserBrickUIData d = uiData.get(c.dataIndex);
			if (d.isVariable && c.textView.getId() == eventOrigin.getId()) {
				FormulaEditorFragment.showFragment(view, this, c.variableFormula);
			}
		}
	}

	// this function is called when this brick's action is being placed into a sequence
	@Override
	public List<SequenceAction> addActionToSequence(SequenceAction sequence) {
		SequenceAction userSequence = ExtendedActions.sequence();
		Script userScript = definitionBrick.initScript(sprite); // getScript
		userScript.run(userSequence);

		ArrayList<SequenceAction> returnActionList = new ArrayList<SequenceAction>();
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