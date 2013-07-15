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

import org.catrobat.catroid.R;
import org.catrobat.catroid.content.Sprite;
import org.catrobat.catroid.content.bricks.UserBrick;
import org.catrobat.catroid.content.bricks.UserBrickUIDataArray;
import org.catrobat.catroid.test.utils.Reflection;

import android.test.AndroidTestCase;

public class UserBrickCloneTest extends AndroidTestCase {

	private static final int BRICK_FORMULA_VALUE = 1;
	private static final String CLONE_BRICK_FORMULA_VALUE = "2";
	private Sprite sprite;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		sprite = new Sprite("testSprite");

	}

	public void testBrickCloneWithFormula() {
		UserBrick brick = new UserBrick(sprite);
		brick.addUILocalizedString(R.string.about);
		brick.addUIText("test0");
		brick.addUIField("test1");
		brickClone(brick);
	}

	private void brickClone(UserBrick brick) {
		UserBrick cloneBrick = brick.clone();
		UserBrickUIDataArray array = (UserBrickUIDataArray) Reflection.getPrivateField(brick, "uiData");
		UserBrickUIDataArray clonedArray = (UserBrickUIDataArray) Reflection.getPrivateField(cloneBrick, "uiData");
		assertTrue("The cloned brick has a different uiDataArray than the original brick", array == clonedArray);
	}

}
