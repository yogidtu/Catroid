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
package org.catrobat.catroid.test.content.brick;

import org.catrobat.catroid.content.Sprite;
import org.catrobat.catroid.content.bricks.Brick;
import org.catrobat.catroid.content.bricks.SetPhysicObjectTypeBrick;
import org.catrobat.catroid.physics.PhysicObject;
import org.catrobat.catroid.physics.PhysicObject.Type;
import org.catrobat.catroid.test.utils.TestUtils;

import android.test.AndroidTestCase;

public class SetPhysicObjectTypeBrickTest extends AndroidTestCase {
	private PhysicObject.Type type;
	private SetPhysicObjectTypeBrick setPhysicObjectTypeBrick;
	private Sprite sprite;
	private PhysicObjectMock physicObjectMock;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		type = Type.DYNAMIC;
		sprite = new Sprite("testSprite");
		physicObjectMock = new PhysicObjectMock();
		setPhysicObjectTypeBrick = new SetPhysicObjectTypeBrick(sprite, type);
		setPhysicObjectTypeBrick.setPhysicObject(physicObjectMock);
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();

		type = null;
		sprite = null;
		physicObjectMock = null;
		setPhysicObjectTypeBrick = null;
	}

	public void testRequiredResources() {
		assertEquals(setPhysicObjectTypeBrick.getRequiredResources(), Brick.NO_RESOURCES);
	}

	public void testGetSprite() {
		assertEquals(setPhysicObjectTypeBrick.getSprite(), sprite);
	}

	public void testSetPhysicObject() {
		assertEquals(physicObjectMock, TestUtils.getPrivateField("physicObject", setPhysicObjectTypeBrick, false));
	}

	public void testClone() {
		Brick clone = setPhysicObjectTypeBrick.clone();

		assertEquals(setPhysicObjectTypeBrick.getSprite(), clone.getSprite());
		assertEquals(setPhysicObjectTypeBrick.getRequiredResources(), clone.getRequiredResources());
	}

	public void testExecution() {
		assertFalse("Set physic object type has already been executed", physicObjectMock.executed);
		assertNotSame("Physic object types are the same", type, physicObjectMock.executedWithType);

		setPhysicObjectTypeBrick.execute();

		assertTrue("Set physic object type hasn't been executed", physicObjectMock.executed);
		assertEquals("Set physic object type has been called with wrong parameters", type,
				physicObjectMock.executedWithType);
	}

	public void testNullPhysicObject() {
		setPhysicObjectTypeBrick = new SetPhysicObjectTypeBrick(null, type);
		try {
			setPhysicObjectTypeBrick.execute();
			fail("Execution of SetPhysicObjectTypeBrick with null Sprite did not cause a "
					+ "NullPointerException to be thrown");
		} catch (NullPointerException expected) {
			// expected behavior
		}
	}

	private class PhysicObjectMock extends PhysicObject {
		public boolean executed = false;
		public Type executedWithType = null;

		public PhysicObjectMock() {
			super(null);
		}

		@Override
		public void setType(Type type) {
			executed = true;
			executedWithType = type;
		}

		@Override
		protected void setCollisionBits(short categoryBits, short maskBits) {
		}
	}
}
