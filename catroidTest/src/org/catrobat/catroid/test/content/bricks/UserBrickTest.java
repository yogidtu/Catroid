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
package org.catrobat.catroid.test.content.bricks;

import java.util.ArrayList;

import org.catrobat.catroid.content.Script;
import org.catrobat.catroid.content.Sprite;
import org.catrobat.catroid.content.UserScript;
import org.catrobat.catroid.content.bricks.ChangeXByNBrick;
import org.catrobat.catroid.content.bricks.UserBrick;
import org.catrobat.catroid.content.bricks.UserBrickUIComponent;
import org.catrobat.catroid.content.bricks.UserBrickUIDataArray;
import org.catrobat.catroid.content.bricks.UserScriptDefinitionBrick;
import org.catrobat.catroid.test.utils.Reflection;
import org.catrobat.catroid.test.utils.Reflection.ParameterList;

import android.test.AndroidTestCase;

import com.badlogic.gdx.scenes.scene2d.actions.SequenceAction;

public class UserBrickTest extends AndroidTestCase {
	private Sprite sprite;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		sprite = new Sprite("testSprite");
		Reflection.invokeMethod(sprite, "init");
	}

	public void testSpriteInit() {

		ArrayList<Script> array = (ArrayList<Script>) Reflection.getPrivateField(sprite, "userBricks");

		assertTrue("the sprite should have zero user bricks after being created and initialized.", array.size() == 0);

		Reflection.invokeMethod(sprite, "getUserBrickListAtLeastOneBrick", new ParameterList("Example", "Variable 1"));

		array = (ArrayList<Script>) Reflection.getPrivateField(sprite, "userBricks");

		assertTrue("the sprite should have one user brick after getUserBrickList()", array.size() == 1);

	}

	public void testSpriteHasOneUserBrickAfterAddingAUserBrick() {
		UserBrick brick = new UserBrick(sprite, 0);
		brick.addUIText("test0");
		brick.addUIVariable("test1");

		UserScriptDefinitionBrick definitionBrick = (UserScriptDefinitionBrick) Reflection.getPrivateField(brick,
				"definitionBrick");
		Script userScript = definitionBrick.initScript(sprite);

		userScript.addBrick(new ChangeXByNBrick(sprite, 1));

		ArrayList<Script> array = (ArrayList<Script>) Reflection.getPrivateField(sprite, "userBricks");

		assertTrue("the sprite should have one user brick after we added a user brick to it, has " + array.size(),
				array.size() == 1);
	}

	public void testSpriteMovedCorrectly() {
		int moveValue = 6;

		UserBrick brick = new UserBrick(sprite, 0);
		brick.addUIText("test0");
		brick.addUIVariable("test1");

		UserScriptDefinitionBrick definitionBrick = (UserScriptDefinitionBrick) Reflection.getPrivateField(brick,
				"definitionBrick");
		Script userScript = definitionBrick.initScript(sprite);

		userScript.addBrick(new ChangeXByNBrick(sprite, moveValue));

		SequenceAction sequence = new SequenceAction();
		brick.addActionToSequence(sequence);

		float x = sprite.look.getXInUserInterfaceDimensionUnit();
		float y = sprite.look.getYInUserInterfaceDimensionUnit();

		assertEquals("Unexpected initial sprite x position: " + x, 0f, x);
		assertEquals("Unexpected initial sprite y position: " + y, 0f, y);

		sequence.act(1f);

		x = sprite.look.getXInUserInterfaceDimensionUnit();
		y = sprite.look.getYInUserInterfaceDimensionUnit();

		assertEquals("Unexpected initial sprite x position: " + x, (float) moveValue,
				sprite.look.getXInUserInterfaceDimensionUnit());
		assertEquals("Unexpected initial sprite y position: " + y, 0f, sprite.look.getYInUserInterfaceDimensionUnit());
	}

	public void testBrickCloneWithFormula() {
		UserBrick brick = new UserBrick(sprite, 0);
		brick.addUIText("test0");
		brick.addUIVariable("test1");

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
