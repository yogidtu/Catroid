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

import java.util.Map;

import org.catrobat.catroid.content.Sprite;
import org.catrobat.catroid.physics.PhysicCostume;
import org.catrobat.catroid.physics.PhysicObject;
import org.catrobat.catroid.physics.PhysicWorld;
import org.catrobat.catroid.test.utils.TestUtils;

import android.test.AndroidTestCase;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.GdxNativesLoader;

public class PhysicWorldTest extends AndroidTestCase {
	static {
		GdxNativesLoader.load();
	}

	private PhysicWorld physicWorld;
	private World world;
	private Map<Sprite, PhysicObject> physicObjects;

	@SuppressWarnings("unchecked")
	@Override
	public void setUp() {
		physicWorld = new PhysicWorld();
		world = (World) TestUtils.getPrivateField("world", physicWorld, false);
		physicObjects = (Map<Sprite, PhysicObject>) TestUtils.getPrivateField("physicObjects", physicWorld, false);
	}

	@Override
	public void tearDown() {
		physicWorld = null;
		world = null;
		physicObjects = null;
	}

	public void testDefaultSettings() {
		assertEquals(40.0f, PhysicWorld.RATIO);
		assertEquals(8, PhysicWorld.VELOCITY_ITERATIONS);
		assertEquals(3, PhysicWorld.POSITION_ITERATIONS);

		assertEquals(new Vector2(0, -10), PhysicWorld.DEFAULT_GRAVITY);
		assertEquals(false, PhysicWorld.IGNORE_SLEEPING_OBJECTS);

		assertEquals(6, PhysicWorld.STABILIZING_STEPS);
	}

	public void testWrapper() {
		assertNotNull(world);
	}

	public void testGravity() {
		assertEquals(PhysicWorld.DEFAULT_GRAVITY, world.getGravity());

		Vector2 newGravity = new Vector2(-1.2f, 3.4f);
		physicWorld.setGravity(newGravity);

		assertEquals(newGravity, world.getGravity());
	}

	public void testGetNullPhysicObject() {
		try {
			@SuppressWarnings("unused")
			PhysicObject physicObject = physicWorld.getPhysicObject(null);
			fail();
		} catch (NullPointerException exception) {
			// Expected behavior
		}
	}

	public void testGetPhysicObject() {
		Sprite sprite = new Sprite("TestSprite");
		PhysicObject physicObject = physicWorld.getPhysicObject(sprite);

		assertNotNull(physicObject);
		assertEquals(1, physicObjects.size());
		assertTrue(physicObjects.containsKey(sprite));
		assertTrue(physicObjects.containsValue(physicObject));
	}

	public void testCreatePhysicObject() {
		PhysicObject physicObject = (PhysicObject) TestUtils
				.invokeMethod(physicWorld, "createPhysicObject", null, null);
		Body body = (Body) TestUtils.getPrivateField("body", physicObject, false);

		assertTrue(body.isBullet());
	}

	public void testGetSamePhysicObject() {
		Sprite sprite = new Sprite("TestSprite");
		PhysicObject physicObject = physicWorld.getPhysicObject(sprite);
		PhysicObject samePhysicObject = physicWorld.getPhysicObject(sprite);

		assertEquals(1, physicObjects.size());
		assertEquals(physicObject, samePhysicObject);
	}

	public void testStabilizingSteps() {
		int stepPasses = PhysicWorld.STABILIZING_STEPS + 10;

		int stabilizingStep;
		for (int pass = 0; pass < stepPasses; pass++) {
			physicWorld.step(100.0f);
			stabilizingStep = (Integer) TestUtils.getPrivateField("stabilizingStep", physicWorld, false);
			assertTrue(((stabilizingStep == (pass + 1)) && (stabilizingStep < PhysicWorld.STABILIZING_STEPS))
					|| (stabilizingStep == PhysicWorld.STABILIZING_STEPS));
		}
	}

	public void testSteps() throws SecurityException, IllegalArgumentException, NoSuchFieldException,
			IllegalAccessException {
		Sprite sprite = new Sprite("TestSprite");
		PhysicObject physicObject = physicWorld.getPhysicObject(sprite);
		sprite.costume = new PhysicCostume(sprite, null, physicObject);

		Vector2 velocity = new Vector2(2.3f, 4.5f);
		float rotationSpeed = 45.0f;
		physicWorld.setGravity(new Vector2(0.0f, 0.0f));
		TestUtils.setPrivateField(PhysicWorld.class, physicWorld, "stabilizingStep", PhysicWorld.STABILIZING_STEPS);

		assertEquals(new Vector2(), physicObject.getPosition());

		physicObject.setVelocity(velocity);
		physicObject.setRotationSpeed(rotationSpeed);

		physicWorld.step(1.0f);
		assertEquals(velocity.x, physicObject.getXPosition(), 1e-8);
		assertEquals(velocity.y, physicObject.getYPosition(), 1e-8);
		assertEquals(rotationSpeed, physicObject.getAngle(), 1e-8);

		physicWorld.step(1.0f);
		assertEquals(2 * velocity.x, physicObject.getXPosition(), 1e-8);
		assertEquals(2 * velocity.y, physicObject.getYPosition(), 1e-8);
		assertEquals(2 * rotationSpeed, physicObject.getAngle(), 1e-8);
	}
}
