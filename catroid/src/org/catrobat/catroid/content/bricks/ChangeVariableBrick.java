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

import com.badlogic.gdx.scenes.scene2d.actions.SequenceAction;

import org.catrobat.catroid.ProjectManager;
import org.catrobat.catroid.content.Project;
import org.catrobat.catroid.content.Script;
import org.catrobat.catroid.content.Sprite;
import org.catrobat.catroid.content.actions.ExtendedActions;
import org.catrobat.catroid.formulaeditor.Formula;
import org.catrobat.catroid.formulaeditor.UserVariable;

import java.util.List;

public class ChangeVariableBrick extends BrickBaseType implements FormulaBrick {

	private static final long serialVersionUID = 1L;
	private UserVariable userVariable;
	private Formula variableFormula;

	public ChangeVariableBrick(Sprite sprite, Formula variableFormula) {
		this.sprite = sprite;
		this.variableFormula = variableFormula;
	}

	public ChangeVariableBrick(Sprite sprite, Formula variableFormula, UserVariable userVariable) {
		this.sprite = sprite;
		this.variableFormula = variableFormula;
		this.userVariable = userVariable;
	}

	public ChangeVariableBrick(Sprite sprite, double value) {
		this.sprite = sprite;
		this.variableFormula = new Formula(value);
	}

	@Override
	public int getRequiredResources() {
		return NO_RESOURCES;
	}

	public ChangeVariableBrick() {
	}

	@Override
	public Brick clone() {
		ChangeVariableBrick clonedBrick = new ChangeVariableBrick(sprite, variableFormula.clone(), userVariable);
		return clonedBrick;
	}

	@Override
	public List<SequenceAction> addActionToSequence(SequenceAction sequence) {
		sequence.addAction(ExtendedActions.changeVariable(sprite, variableFormula, userVariable));
		return null;
	}

	@Override
	public Brick copyBrickForSprite(Sprite sprite, Script script) {
		Project currentProject = ProjectManager.getInstance().getCurrentProject();
		if (!currentProject.getSpriteList().contains(this.sprite)) {
			throw new RuntimeException("this is not the current project");
		}

		ChangeVariableBrick copyBrick = (ChangeVariableBrick) clone();
		copyBrick.sprite = sprite;
		copyBrick.userVariable = currentProject.getUserVariables().getUserVariable(userVariable.getName(), sprite);
		return copyBrick;
	}

	@Override
	public Formula getFormula() {
		return variableFormula;
	}
}
