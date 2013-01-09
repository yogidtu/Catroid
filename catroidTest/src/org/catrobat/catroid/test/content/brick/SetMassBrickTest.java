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
import org.catrobat.catroid.content.bricks.SetMassBrick;
import org.catrobat.catroid.physics.PhysicObject;
import org.catrobat.catroid.test.utils.TestUtils;

import android.test.AndroidTestCase;

public class SetMassBrickTest extends AndroidTestCase {
	private float mass = 10.50f;
	private SetMassBrick setMassBrick;
	private Sprite sprite;
	private PhysicObjectMock physicObjectMock;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		sprite = new Sprite("testSprite");
		physicObjectMock = new PhysicObjectMock();
		setMassBrick = new SetMassBrick(sprite, mass);
		setMassBrick.setPhysicObject(physicObjectMock);
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();

		sprite = null;
		physicObjectMock = null;
		setMassBrick = null;
	}

	public void testRequiredResources() {
		assertEquals(setMassBrick.getRequiredResources(), Brick.NO_RESOURCES);
	}

	public void testGetSprite() {
		assertEquals(setMassBrick.getSprite(), sprite);
	}

	public void testSetPhysicObject() {
		assertEquals(physicObjectMock, TestUtils.getPrivateField("physicObject", setMassBrick, false));
	}

	public void testClone() {
		Brick clone = setMassBrick.clone();

		assertEquals(setMassBrick.getSprite(), clone.getSprite());
		assertEquals(setMassBrick.getRequiredResources(), clone.getRequiredResources());
	}

	public void testExecution() {
		assertFalse(physicObjectMock.executed);
		assertNotSame(mass, physicObjectMock.executedWithMass);

		setMassBrick.execute();

		assertTrue(physicObjectMock.executed);
		assertEquals(mass, physicObjectMock.executedWithMass);
	}

	public void testNullPhysicObject() {
		setMassBrick = new SetMassBrick(sprite, mass);
		try {
			setMassBrick.execute();
			fail("Execution of SetMassBrick with null Sprite did not cause a " + "NullPointerException to be thrown");
		} catch (NullPointerException expected) {
			// expected behavior
		}
	}

	public void testMass() {
		float physicObjectMass = (Float) TestUtils.getPrivateField("mass", setMassBrick, false);
		assertEquals(mass, physicObjectMass);
	}

	private class PhysicObjectMock extends PhysicObject {
		public boolean executed = false;
		public float executedWithMass;

		public PhysicObjectMock() {
			super(null);
		}

		@Override
		public void setMass(float mass) {
			executed = true;
			executedWithMass = mass;
		}

		@Override
		public void setType(Type type) {
		}

		@Override
		protected void setCollisionBits(short categoryBits, short maskBits) {
		}
	}
}
