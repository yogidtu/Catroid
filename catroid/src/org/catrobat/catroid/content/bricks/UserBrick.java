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
import java.util.LinkedList;
import java.util.List;

import org.catrobat.catroid.ProjectManager;
import org.catrobat.catroid.R;
import org.catrobat.catroid.content.Script;
import org.catrobat.catroid.content.Sprite;
import org.catrobat.catroid.content.UserBrickStageToken;
import org.catrobat.catroid.content.actions.ExtendedActions;
import org.catrobat.catroid.formulaeditor.Formula;
import org.catrobat.catroid.formulaeditor.UserVariable;
import org.catrobat.catroid.formulaeditor.UserVariablesContainer;
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

public class UserBrick extends BrickBaseType implements OnClickListener, MultiFormulaBrick {
	private static final long serialVersionUID = 1L;

	private UserScriptDefinitionBrick definitionBrick;
	private transient View prototypeView;

	// belonging to brick instance
	private ArrayList<UserBrickUIComponent> uiComponents;

	// belonging to stored brick
	public UserBrickUIDataArray uiDataArray;
	private int lastDataVersion = 0;
	private int userBrickId;

	public UserBrick(Sprite sprite, int userBrickId) {
		this.userBrickId = userBrickId;
		this.sprite = sprite;
		sprite.addUserBrick(this);
		uiDataArray = new UserBrickUIDataArray();
		this.definitionBrick = new UserScriptDefinitionBrick(sprite, this, userBrickId);

		updateUIComponents(null);
	}

	public UserBrick(Sprite sprite, UserBrickUIDataArray uiData, UserScriptDefinitionBrick definitionBrick) {
		this.userBrickId = definitionBrick.getUserBrickId();
		this.sprite = sprite;
		this.uiDataArray = uiData;
		this.definitionBrick = definitionBrick;
		updateUIComponents(null);
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

	public int addUILocalizedString(Context context, int id) {
		UserBrickUIData data = new UserBrickUIData();
		data.isVariable = false;
		data.name = context.getResources().getString(id);
		uiDataArray.add(data);
		uiDataArray.version++;
		return uiDataArray.size() - 1;
	}

	public int addUIText(String text) {
		UserBrickUIData data = new UserBrickUIData();
		data.isVariable = false;
		data.name = text;
		uiDataArray.add(data);
		uiDataArray.version++;
		return uiDataArray.size() - 1;
	}

	public int addUILocalizedVariable(Context context, int id) {
		UserBrickUIData data = new UserBrickUIData();
		data.isVariable = true;
		data.name = context.getResources().getString(id);

		if (ProjectManager.getInstance().getCurrentProject() != null) {
			UserVariablesContainer variablesContainer = null;
			variablesContainer = ProjectManager.getInstance().getCurrentProject().getUserVariables();
			variablesContainer.addUserBrickUserVariableToUserBrick(userBrickId, data.name);
		}

		uiDataArray.add(data);
		uiDataArray.version++;
		return uiDataArray.size() - 1;
	}

	public int addUIVariable(String id) {
		UserBrickUIData comp = new UserBrickUIData();
		comp.isVariable = true;
		comp.name = id;

		if (ProjectManager.getInstance().getCurrentProject() != null) {
			UserVariablesContainer variablesContainer = null;
			variablesContainer = ProjectManager.getInstance().getCurrentProject().getUserVariables();
			variablesContainer.addUserBrickUserVariableToUserBrick(userBrickId, comp.name);
		}

		uiDataArray.add(comp);
		uiDataArray.version++;
		return uiDataArray.size() - 1;
	}

	public void renameUIVariable(String oldName, String newName, Context context) {
		UserBrickUIData variable = null;
		for (UserBrickUIData data : uiDataArray) {
			if (data.name.equals(oldName)) {
				variable = data;
			}
		}

		definitionBrick.renameVariablesInFormulas(oldName, newName, context);

		variable.name = newName;

		if (ProjectManager.getInstance().getCurrentProject() != null) {
			UserVariablesContainer variablesContainer = null;
			variablesContainer = ProjectManager.getInstance().getCurrentProject().getUserVariables();
			variablesContainer.deleteUserVariableFromUserBrick(userBrickId, oldName);
			variablesContainer.addUserBrickUserVariableToUserBrick(userBrickId, newName);
		}
	}

	public void removeDataAt(int id) {
		uiDataArray.remove(id);
		uiDataArray.version++;
	}

	public boolean isInstanceOf(UserBrick b) {
		return (b.uiDataArray == uiDataArray);
	}

	public Iterator<UserBrickUIComponent> getUIComponentIterator() {
		return uiComponents.iterator();
	}

	private void updateUIComponents(Context context) {
		//Log.d("FOREST", "UB.updateUIComponents");
		ArrayList<UserBrickUIComponent> newUIComponents = new ArrayList<UserBrickUIComponent>();

		for (int i = 0; i < uiDataArray.size(); i++) {
			UserBrickUIComponent c = new UserBrickUIComponent();
			c.dataIndex = i;
			if (uiDataArray.get(i).isVariable) {
				c.variableFormula = new Formula(0);
				c.variableName = uiDataArray.get(i).name;
			}
			newUIComponents.add(c);
		}

		if (context != null && uiComponents != null) {
			copyFormulasMatchingNames(uiComponents, newUIComponents, context);
		}

		uiComponents = newUIComponents;
		lastDataVersion = uiDataArray.version;
	}

	@Override
	public List<Formula> getFormulas() {
		List<Formula> list = new LinkedList<Formula>();
		for (UserBrickUIComponent uiComponent : uiComponents) {
			if (uiComponent.variableFormula != null && uiComponent.variableName != null) {
				list.add(uiComponent.variableFormula);
			}
		}
		return list;
	}

	private void copyFormulasMatchingNames(ArrayList<UserBrickUIComponent> from, ArrayList<UserBrickUIComponent> to,
			Context context) {
		//Log.d("FOREST", "UB.copyFormulasMatchingNames");

		for (UserBrickUIComponent fromElement : from) {
			if (fromElement.dataIndex < uiDataArray.size()) {
				UserBrickUIData fromData = uiDataArray.get(fromElement.dataIndex);
				if (fromData.isVariable) {
					for (UserBrickUIComponent toElement : to) {
						if (toElement.dataIndex < uiDataArray.size()) {
							UserBrickUIData toData = uiDataArray.get(toElement.dataIndex);
							if (fromData.name.equals(toData.name)) {
								toElement.variableFormula = fromElement.variableFormula;
								toElement.variableName = toData.name;
							}
						}
					}
				}
			}
		}
	}

	/**
	 * Removes element at <b>from</b> and adds it after element at <b>to</b>
	 */
	public void reorderUIData(int from, int to) {

		if (to == -1) {
			UserBrickUIData uiData = uiDataArray.remove(from);
			uiDataArray.add(0, uiData);
		} else if (from <= to) {
			UserBrickUIData uiData = uiDataArray.remove(from);
			uiDataArray.add(to, uiData);
		} else {
			// from > to
			UserBrickUIData uiData = uiDataArray.remove(from);
			uiDataArray.add(to + 1, uiData);
		}
		uiDataArray.version++;
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
		if (lastDataVersion < uiDataArray.version || uiComponents == null) {
			updateUIComponents(view.getContext());
			onLayoutChanged(view);
		}

		BrickLayout layout = (BrickLayout) view.findViewById(R.id.brick_user_flow_layout);
		Drawable background = layout.getBackground();
		background.setAlpha(alphaValue);

		for (UserBrickUIComponent c : uiComponents) {
			if (c != null && c.textView != null) {
				c.textView.setTextColor(c.textView.getTextColors().withAlpha(alphaValue));
				if (c.textView.getBackground() != null) {
					c.textView.getBackground().setAlpha(alphaValue);
				}
			}
		}

		this.alphaValue = (alphaValue);
		return view;
	}

	public void onLayoutChanged(View currentView) {
		if (lastDataVersion < uiDataArray.version || uiComponents == null) {
			updateUIComponents(currentView.getContext());
		}

		boolean prototype = (currentView == prototypeView);

		Context context = currentView.getContext();

		BrickLayout layout2 = (BrickLayout) currentView.findViewById(R.id.brick_user_flow_layout);
		if (layout2.getChildCount() > 0) {
			layout2.removeAllViews();
		}

		int id = 0;
		for (UserBrickUIComponent c : uiComponents) {
			TextView currentTextView = null;
			UserBrickUIData uiData = uiDataArray.get(c.dataIndex);
			if (uiData.isVariable) {
				currentTextView = new EditText(context);

				if (prototype) {
					currentTextView.setTextAppearance(context, R.style.BrickPrototypeTextView);
					currentTextView.setText(String.valueOf(c.variableFormula.interpretInteger(sprite)));
				} else {
					currentTextView.setId(id);
					currentTextView.setTextAppearance(context, R.style.BrickEditText);

					c.variableFormula.setTextFieldId(currentTextView.getId());
					String formulaString = c.variableFormula.getFormulaString(currentTextView.getContext());
					c.variableFormula.refreshTextField(currentTextView, formulaString);
					//Log.d("FOREST", "UB.onLayoutChanged: " + currentTextView.getText().toString());
					// This stuff isn't being included by the style when I use setTextAppearance.
					currentTextView.setFocusable(false);
					currentTextView.setFocusableInTouchMode(false);

					currentTextView.setOnClickListener(this);
				}
				currentTextView.setVisibility(View.VISIBLE);
			} else {
				currentTextView = new TextView(context);
				currentTextView.setTextAppearance(context, R.style.BrickText_Multiple);

				currentTextView.setText(uiData.name);
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
			id++;
		}
	}

	public CharSequence getName(Context context) {
		CharSequence name = "";
		for (UserBrickUIData uiData : uiDataArray) {
			if (!uiData.isVariable) {
				name = uiData.name;
				break;
			}
		}
		return name;
	}

	@Override
	public UserBrick clone() {
		return new UserBrick(getSprite(), uiDataArray, definitionBrick);
	}

	@Override
	public void onClick(View eventOrigin) {
		if (checkbox.getVisibility() == View.VISIBLE) {
			return;
		}

		for (UserBrickUIComponent c : uiComponents) {
			UserBrickUIData uiData = uiDataArray.get(c.dataIndex);

			if (uiData.isVariable && c.textView.getId() == eventOrigin.getId()) {
				FormulaEditorFragment.showFragment(view, this, c.variableFormula);
			}
		}
	}

	// this function is called when this brick's action is being placed into a sequence
	@Override
	public List<SequenceAction> addActionToSequence(SequenceAction sequence) {

		UserBrickStageToken stageToken = getStageToken();

		SequenceAction userSequence = ExtendedActions.sequence();
		Script userScript = definitionBrick.initScript(sprite); // getScript
		userScript.run(userSequence);

		ArrayList<SequenceAction> returnActionList = new ArrayList<SequenceAction>();
		returnActionList.add(userSequence);

		Action action = ExtendedActions.userBrick(sprite, userSequence, stageToken);
		sequence.addAction(action);
		return returnActionList;
	}

	private UserBrickStageToken getStageToken() {
		if (ProjectManager.getInstance() == null || ProjectManager.getInstance().getCurrentProject() == null) {
			return null;
		}

		LinkedList<UserBrickVariable> theList = new LinkedList<UserBrickVariable>();

		UserVariablesContainer variablesContainer = null;
		variablesContainer = ProjectManager.getInstance().getCurrentProject().getUserVariables();

		for (UserBrickUIComponent uiComponent : uiComponents) {
			if (uiComponent.variableFormula != null && uiComponent.variableName != null) {
				List<UserVariable> variables = variablesContainer.getOrCreateVariableListForUserBrick(userBrickId);
				UserVariable variable = variablesContainer.findUserVariable(uiComponent.variableName, variables);

				theList.add(new UserBrickVariable(variable, uiComponent.variableFormula));
			}
		}
		return new UserBrickStageToken(theList, userBrickId);
	}

	public UserScriptDefinitionBrick getDefinitionBrick() {
		return definitionBrick;
	}

	public void setDefinitionBrick(UserScriptDefinitionBrick definitionBrick) {
		this.definitionBrick = definitionBrick;
	}
}