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
package org.catrobat.catroid.formulaeditor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.catrobat.catroid.ProjectManager;
import org.catrobat.catroid.content.Sprite;
import org.catrobat.catroid.content.bricks.UserBrick;
import org.catrobat.catroid.content.bricks.UserScriptDefinitionBrick;
import org.catrobat.catroid.ui.adapter.UserVariableAdapter;

import android.content.Context;

import com.thoughtworks.xstream.annotations.XStreamAlias;

public class UserVariablesContainer implements Serializable {
	private static final long serialVersionUID = 1L;

	@XStreamAlias("programVariableList")
	private List<UserVariable> projectVariables;
	@XStreamAlias("objectVariableList")
	private Map<Sprite, List<UserVariable>> spriteVariables;
	@XStreamAlias("userBrickVariableList")
	private Map<UserScriptDefinitionBrick, List<UserVariable>> userBrickVariables;

	public UserVariablesContainer() {
		projectVariables = new ArrayList<UserVariable>();
		spriteVariables = new HashMap<Sprite, List<UserVariable>>();
		userBrickVariables = new HashMap<UserScriptDefinitionBrick, List<UserVariable>>();
	}

	public UserVariableAdapter createUserVariableAdapter(Context context, UserScriptDefinitionBrick brick, Sprite sprite) {
		return new UserVariableAdapter(context, getOrCreateVariableListForUserBrick(brick),
				getOrCreateVariableListForSprite(sprite), projectVariables);
	}

	public UserVariable getUserVariable(String userVariableName, Sprite sprite) {
		UserVariable var;
		var = findUserVariable(userVariableName, getOrCreateVariableListForSprite(sprite));
		if (var == null) {
			var = findUserVariable(userVariableName, projectVariables);
		}
		return var;
	}

	public List<UserVariable> getProjectVariables() {
		return projectVariables;
	}

	public UserVariable addUserBrickUserVariable(String userVariableName) {
		UserScriptDefinitionBrick currentBrick = null;
		currentBrick = ProjectManager.getInstance().getCurrentUserBrick().getDefinitionBrick();
		return addUserBrickUserVariableToUserBrick(currentBrick, userVariableName);
	}

	public UserVariable addUserBrickUserVariableToUserBrick(UserScriptDefinitionBrick brick, String userVariableName) {
		List<UserVariable> varList = getOrCreateVariableListForUserBrick(brick);
		UserVariable userVariableToAdd = new UserVariable(userVariableName, varList);
		userVariableToAdd.setValue(0);
		varList.add(userVariableToAdd);
		return userVariableToAdd;
	}

	public UserVariable addSpriteUserVariable(String userVariableName) {
		Sprite currentSprite = ProjectManager.getInstance().getCurrentSprite();
		return addSpriteUserVariableToSprite(currentSprite, userVariableName);
	}

	public UserVariable addSpriteUserVariableToSprite(Sprite sprite, String userVariableName) {
		List<UserVariable> varList = getOrCreateVariableListForSprite(sprite);
		UserVariable userVariableToAdd = new UserVariable(userVariableName, varList);
		varList.add(userVariableToAdd);
		return userVariableToAdd;
	}

	public UserVariable addProjectUserVariable(String userVariableName) {
		UserVariable userVariableToAdd = new UserVariable(userVariableName, projectVariables);
		projectVariables.add(userVariableToAdd);
		return userVariableToAdd;
	}

	/**
	 * This function deletes the user variable with userVariableName in the current context.
	 * 
	 * The current context consists of all global variables, the sprite variables for the current sprite,
	 * and the user brick variables for the current user brick.
	 */
	public void deleteUserVariableByName(String userVariableName) {
		Sprite currentSprite = ProjectManager.getInstance().getCurrentSprite();
		UserBrick currentUserBrick = ProjectManager.getInstance().getCurrentUserBrick();
		UserVariable variableToDelete = getUserVariable(userVariableName, currentUserBrick, currentSprite);
		if (variableToDelete != null) {
			List<UserVariable> context = variableToDelete.getContext();
			context.remove(variableToDelete);
		}
	}

	public void deleteUserVariableFromUserBrick(UserScriptDefinitionBrick brick, String userVariableName) {
		List<UserVariable> context = userBrickVariables.get(brick);
		UserVariable variableToDelete = findUserVariable(userVariableName, context);
		if (variableToDelete != null) {
			context.remove(variableToDelete);
		}

	}

	public List<UserVariable> getOrCreateVariableListForUserBrick(UserScriptDefinitionBrick definitionBrick) {
		if (definitionBrick == null) {
			return new ArrayList<UserVariable>();
		}
		List<UserVariable> variables = userBrickVariables.get(definitionBrick);
		if (variables == null) {
			variables = new ArrayList<UserVariable>();
			userBrickVariables.put(definitionBrick, variables);
		}
		return variables;
	}

	public void cleanVariableListForUserBrick(UserScriptDefinitionBrick definitionBrick) {
		List<UserVariable> vars = userBrickVariables.get(definitionBrick);
		if (vars != null) {
			vars.clear();
		}
		userBrickVariables.remove(definitionBrick);
	}

	public List<UserVariable> getOrCreateVariableListForSprite(Sprite sprite) {
		List<UserVariable> variables = spriteVariables.get(sprite);
		if (variables == null) {
			variables = new ArrayList<UserVariable>();
			spriteVariables.put(sprite, variables);
		}
		return variables;
	}

	public List<UserVariable> createVariableListForCopySprite(Sprite sprite) {
		return spriteVariables.get(sprite);
	}

	public void cleanVariableListForSprite(Sprite sprite) {
		List<UserVariable> vars = spriteVariables.get(sprite);
		if (vars != null) {
			vars.clear();
		}
		spriteVariables.remove(sprite);
	}

	/**
	 * This function finds the user variable with userVariableName in the current context.
	 * 
	 * The current context consists of all global variables, the sprite variables for the current sprite,
	 * and the user brick variables for the current user brick.
	 */
	public UserVariable getUserVariable(String name, UserBrick currentUserBrick, Sprite currentSprite) {

		UserVariable variableToReturn;
		List<UserVariable> spriteVariables = getOrCreateVariableListForSprite(currentSprite);
		variableToReturn = findUserVariable(name, spriteVariables);
		if (variableToReturn != null) {
			return variableToReturn;
		}

		UserScriptDefinitionBrick definitionBrick = currentUserBrick.getDefinitionBrick();
		List<UserVariable> userBrickVariables = getOrCreateVariableListForUserBrick(definitionBrick);
		variableToReturn = findUserVariable(name, userBrickVariables);
		if (variableToReturn != null) {
			return variableToReturn;
		}

		variableToReturn = findUserVariable(name, projectVariables);
		if (variableToReturn != null) {
			return variableToReturn;
		}
		return null;
	}

	public UserVariable findUserVariable(String name, List<UserVariable> variables) {
		if (variables == null) {
			return null;
		}
		for (UserVariable variable : variables) {
			if (variable.getName().equals(name)) {
				return variable;
			}
		}
		return null;
	}

	public void resetAllUserVariables() {

		resetUserVariables(projectVariables);

		Iterator<Sprite> spriteIterator = spriteVariables.keySet().iterator();
		while (spriteIterator.hasNext()) {
			Sprite currentSprite = spriteIterator.next();
			resetUserVariables(spriteVariables.get(currentSprite));
		}
		Iterator<UserScriptDefinitionBrick> brickIterator = userBrickVariables.keySet().iterator();
		while (spriteIterator.hasNext()) {
			UserScriptDefinitionBrick currentBrick = brickIterator.next();
			resetUserVariables(userBrickVariables.get(currentBrick));
		}
	}

	private void resetUserVariables(List<UserVariable> UserVariableList) {
		for (UserVariable userVariable : UserVariableList) {
			userVariable.setValue(0);
		}
	}
}