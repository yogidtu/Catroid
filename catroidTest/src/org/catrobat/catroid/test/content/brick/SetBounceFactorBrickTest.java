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

import junit.framework.TestCase;

import org.catrobat.catroid.content.Sprite;
import org.catrobat.catroid.content.bricks.Brick;
import org.catrobat.catroid.content.bricks.SetBounceFactorBrick;
import org.catrobat.catroid.physics.PhysicObject;
import org.catrobat.catroid.test.utils.TestUtils;

public class SetBounceFactorBrickTest extends TestCase {
	private float bounceFactor = 35.0f;
	private SetBounceFactorBrick setBounceFactorBrick;
	private Sprite sprite;
	private PhysicObjectMock physicObjectMock;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		sprite = new Sprite("testSprite");
		physicObjectMock = new PhysicObjectMock();
		setBounceFactorBrick = new SetBounceFactorBrick(sprite, bounceFactor);
		setBounceFactorBrick.setPhysicObject(physicObjectMock);
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();

		sprite = null;
		physicObjectMock = null;
		setBounceFactorBrick = null;
	}

	public void testRequiredResources() {
		assertEquals(setBounceFactorBrick.getRequiredResources(), Brick.NO_RESOURCES);
	}

	public void testGetSprite() {
		assertEquals(setBounceFactorBrick.getSprite(), sprite);
	}

	public void testSetPhysicObject() {
		assertEquals(physicObjectMock, TestUtils.getPrivateField("physicObject", setBounceFactorBrick, false));
	}

	public void testClone() {
		Brick clone = setBounceFactorBrick.clone();

		assertEquals(setBounceFactorBrick.getSprite(), clone.getSprite());
		assertEquals(setBounceFactorBrick.getRequiredResources(), clone.getRequiredResources());
		assertEquals(TestUtils.getPrivateField("bounceFactor", setBounceFactorBrick, false),
				TestUtils.getPrivateField("bounceFactor", clone, false));
	}

	public void testExecution() {
		assertFalse("Set bounce factor has already been executed", physicObjectMock.executed);
		assertNotSame("Factors are the same", bounceFactor / 100.0f, physicObjectMock.executedWithBounceFactor);

		setBounceFactorBrick.execute();

		assertTrue("Set bounce factor hasn't been executed", physicObjectMock.executed);
		assertEquals("Set bounce factor has been called with wrong parameters", bounceFactor / 100.0f,
				physicObjectMock.executedWithBounceFactor);
	}

	public void testNullPhysicObject() {
		setBounceFactorBrick = new SetBounceFactorBrick(sprite, bounceFactor);
		try {
			setBounceFactorBrick.execute();
			fail("Execution of SetBounceFactorBrick with null Sprite did not cause a NullPointerException");
		} catch (NullPointerException expected) {
			// expected behavior
		}
	}

	private class PhysicObjectMock extends PhysicObject {
		public boolean executed = false;
		public float executedWithBounceFactor;

		public PhysicObjectMock() {
			super(null);
		}

		@Override
		public void setBounceFactor(float bounceFactor) {
			executed = true;
			executedWithBounceFactor = bounceFactor;
		}

		@Override
		public void setType(Type type) {
		}

		@Override
		protected void setCollisionBits(short categoryBits, short maskBits) {
		}
	}
}
