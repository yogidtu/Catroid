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
package org.catrobat.catroid.test.userbricks;

import java.util.ArrayList;

import org.catrobat.catroid.content.Sprite;
import org.catrobat.catroid.content.UserScript;
import org.catrobat.catroid.content.bricks.UserBrick;
import org.catrobat.catroid.content.bricks.UserBrickUIComponent;
import org.catrobat.catroid.content.bricks.UserBrickUIDataArray;
import org.catrobat.catroid.content.bricks.UserScriptDefinitionBrick;
import org.catrobat.catroid.test.utils.Reflection;

import android.test.AndroidTestCase;

public class AddNewUserBrickTest extends AndroidTestCase {
	private Sprite sprite;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		sprite = new Sprite("testSprite");

	}

	public void testBrickCloneWithFormula() {

	}

	private void brickClone(UserBrick brick) {
		UserBrick cloneBrick = brick.clone();
		UserBrickUIDataArray array = (UserBrickUIDataArray) Reflection.getPrivateField(brick, "uiData");
		UserBrickUIDataArray clonedArray = (UserBrickUIDataArray) Reflection.getPrivateField(cloneBrick, "uiData");
		assertTrue("The cloned brick has a different uiDataArray than the original brick", array == clonedArray);

		UserScriptDefinitionBrick definition = (UserScriptDefinitionBrick) Reflection.getPrivateField(brick,
				"definitionBrick");
		UserScriptDefinitionBrick clonedDef = (UserScriptDefinitionBrick) Reflection.getPrivateField(cloneBrick,
				"definitionBrick");
		assertTrue("The cloned brick has a different UserScriptDefinitionBrick than the original brick",
				definition == clonedDef);

		UserScript userScript = (UserScript) Reflection.getPrivateField(definition, "userScript");
		UserScript clonedUserScript = (UserScript) Reflection.getPrivateField(clonedDef, "userScript");
		assertTrue("The cloned brick has a different UserScriptDefinitionBrick than the original brick",
				userScript == clonedUserScript);

		ArrayList<UserBrickUIComponent> componentArray = (ArrayList<UserBrickUIComponent>) Reflection.getPrivateField(
				brick, "uiComponents");
		ArrayList<UserBrickUIComponent> clonedComponentArray = (ArrayList<UserBrickUIComponent>) Reflection
				.getPrivateField(cloneBrick, "uiComponents");
		assertTrue("The cloned brick has a different uiDataArray than the original brick",
				componentArray != clonedComponentArray);
	}
}
