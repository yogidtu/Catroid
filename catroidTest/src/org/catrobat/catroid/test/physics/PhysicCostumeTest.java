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
package org.catrobat.catroid.test.physics;

import org.catrobat.catroid.common.CostumeData;
import org.catrobat.catroid.content.Sprite;
import org.catrobat.catroid.physics.PhysicCostume;
import org.catrobat.catroid.physics.PhysicObject;
import org.catrobat.catroid.physics.PhysicWorld;
import org.catrobat.catroid.physics.shapebuilder.PhysicShapeBuilder;

import android.test.AndroidTestCase;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Shape;
import com.badlogic.gdx.utils.GdxNativesLoader;

public class PhysicCostumeTest extends AndroidTestCase {
	static {
		GdxNativesLoader.load();
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void testCheckImageChanged() {
		Sprite sprite = new Sprite("TestSprite");
		PhysicShapeBuilder physicShapeBuilder = new PhysicShapeBuilderMock();

		PhysicObjectMock physicObjectMock = new PhysicObjectMock();
		PhysicCostumeMock physicCostume = new PhysicCostumeMock(sprite, physicShapeBuilder, physicObjectMock);

		Shape[] shapes = physicShapeBuilder.getShape(physicCostume.getCostumeData(), physicCostume.getSize());

		assertNotNull("No shapes created", shapes);

		physicCostume.setImageChanged(false);
		assertFalse("Costume image has changed", physicCostume.checkImageChanged());
		assertFalse("Set shape has been executed", physicObjectMock.setShapeExecuted);
		assertNull("Shapes already have been set", physicObjectMock.setShapeExecutedWithShapes);

		physicCostume.setImageChanged(true);
		assertTrue("Costume image hasn't changed", physicCostume.checkImageChanged());
		assertTrue("Set shape hasn't been executed", physicObjectMock.setShapeExecuted);
		assertEquals("Set wrong shapes", shapes, physicObjectMock.setShapeExecutedWithShapes);
	}

	public void testUpdatePositionAndRotation() {
		Sprite sprite = new Sprite("TestSprite");
		PhysicWorld physicWorld = new PhysicWorld();
		PhysicObject physicObject = physicWorld.getPhysicObject(sprite);
		PhysicCostumeUpdateMock physicCostume = new PhysicCostumeUpdateMock(sprite, null, physicObject);

		Vector2 position = new Vector2(1.2f, 3.4f);
		float rotation = 3.14f;

		physicCostume.setXYPosition(position.x, position.y);
		physicCostume.setRotation(rotation);

		assertNotSame("Wrong position", position, physicCostume.getCostumePosition());
		assertNotSame("Wrong rotation", rotation, physicCostume.getCostumeRotation());

		physicCostume.updatePositionAndRotation();

		assertEquals("Position not updated", position, physicCostume.getCostumePosition());
		assertEquals("Rotation not updated", rotation, physicCostume.getCostumeRotation());
	}

	public void testPositionAndAngle() {
		PhysicWorld physicWorld = new PhysicWorld();
		PhysicObject physicObject = physicWorld.getPhysicObject(new Sprite("TestSprite"));
		PhysicCostume physicCostume = new PhysicCostume(null, null, physicObject);

		float x = 1.2f;
		physicCostume.setXPosition(x);
		assertEquals("Wrong x position", x, physicObject.getXPosition());

		float y = -3.4f;
		physicCostume.setYPosition(y);
		assertEquals("Wrong y position", y, physicObject.getYPosition());

		x = 5.6f;
		y = 7.8f;
		physicCostume.setXYPosition(x, y);
		assertEquals("Wrong position", new Vector2(x, y), physicObject.getPosition());

		float rotation = 9.0f;
		physicCostume.setRotation(rotation);
		assertEquals(rotation, physicObject.getAngle());

		assertEquals("X position has changed", x, physicCostume.getXPosition());
		assertEquals("Y position has changed", y, physicCostume.getYPosition());
		assertEquals("Wrong rotation", rotation, physicCostume.getRotation());
	}

	// TODO: Check if this test is correct.
	public void testSize() {
		Sprite sprite = new Sprite("TestSprite");
		PhysicShapeBuilder physicShapeBuilder = new PhysicShapeBuilderMock();
		PhysicObjectMock physicObjectMock = new PhysicObjectMock();
		PhysicCostume physicCostume = new PhysicCostume(sprite, physicShapeBuilder, physicObjectMock);
		float size = 3.14f;

		assertFalse("Set shape has been executed", physicObjectMock.setShapeExecuted);
		assertNull("Shapes already has been set", physicObjectMock.setShapeExecutedWithShapes);

		physicCostume.setSize(size);
		assertEquals("Wrong size", size, physicCostume.getSize());

		Shape[] shapes = physicShapeBuilder.getShape(physicCostume.getCostumeData(), size);
		assertTrue("Set shape hasn't been executed", physicObjectMock.setShapeExecuted);
		assertEquals("Wrong shapes", shapes, physicObjectMock.setShapeExecutedWithShapes);
	}

	private class PhysicCostumeUpdateMock extends PhysicCostume {

		public PhysicCostumeUpdateMock(Sprite sprite, PhysicShapeBuilder physicShapeBuilder, PhysicObject physicObject) {
			super(sprite, physicShapeBuilder, physicObject);
		}

		public Vector2 getCostumePosition() {
			float x = super.getXPosition();
			float y = super.getYPosition();

			return new Vector2(x, y);
		}

		public float getCostumeRotation() {
			return super.getRotation();
		}
	}

	private class PhysicCostumeMock extends PhysicCostume {

		public PhysicCostumeMock(Sprite sprite, PhysicShapeBuilder physicShapeBuilder, PhysicObject physicObject) {
			super(sprite, physicShapeBuilder, physicObject);
		}

		@Override
		protected boolean checkImageChanged() {
			return super.checkImageChanged();
		}

		public void setImageChanged(boolean imageChanged) {
			this.imageChanged = imageChanged;
		}
	}

	private class PhysicObjectMock extends PhysicObject {
		public boolean setShapeExecuted = false;
		public Shape[] setShapeExecutedWithShapes = null;

		public PhysicObjectMock() {
			super(null);
		}

		@Override
		public void setShape(Shape[] shapes) {
			setShapeExecuted = true;
			setShapeExecutedWithShapes = shapes;
		}

		@Override
		protected void setCollisionBits(short categoryBits, short maskBits) {
		}

		@Override
		public void setType(Type type) {
		}
	}

	private class PhysicShapeBuilderMock extends PhysicShapeBuilder {
		private final Shape[] shapes = new Shape[4];

		@Override
		public Shape[] getShape(CostumeData costumeData, float scaleFactor) {
			return shapes;
		}
	}
}
