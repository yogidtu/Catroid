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

import java.util.Iterator;
import java.util.List;

import org.catrobat.catroid.physics.PhysicBoundaryBox;
import org.catrobat.catroid.physics.PhysicDebugSettings;
import org.catrobat.catroid.physics.PhysicObject;
import org.catrobat.catroid.physics.PhysicWorld;

import android.test.AndroidTestCase;

import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.Filter;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.GdxNativesLoader;

public class PhysicBoundaryBoxTest extends AndroidTestCase {
	static {
		GdxNativesLoader.load();
	}

	private World world;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		world = new World(PhysicWorld.DEFAULT_GRAVITY, PhysicWorld.IGNORE_SLEEPING_OBJECTS);
	}

	public void testDefaultSettings() {
		assertEquals(5, PhysicBoundaryBox.FRAME_SIZE);
		assertEquals(0x0002, PhysicBoundaryBox.COLLISION_MASK);
	}

	public void testProperties() {
		assertEquals(0, world.getBodyCount());
		new PhysicBoundaryBox(world).create();
		assertEquals(4, world.getBodyCount());

		Iterator<Body> bodyIterator = world.getBodies();
		while (bodyIterator.hasNext()) {
			Body body = bodyIterator.next();
			assertEquals(BodyType.StaticBody, body.getType());
			assertFalse(body.isSleepingAllowed());

			List<Fixture> fixtures = body.getFixtureList();
			assertEquals(1, fixtures.size());
			for (Fixture fixture : fixtures) {
				Filter filter = fixture.getFilterData();
				assertEquals(PhysicObject.COLLISION_MASK, filter.maskBits);

				if (PhysicDebugSettings.DEBUGFLAG) {
					assertEquals(PhysicObject.COLLISION_MASK, filter.categoryBits);
				} else {
					assertEquals(PhysicBoundaryBox.COLLISION_MASK, filter.categoryBits);
				}
			}
		}
	}

	/**
	 * TODO: Refactor me!
	 */
	public void testPositionAndSize() {
		//		Values.SCREEN_WIDTH = 800;
		//		Values.SCREEN_HEIGHT = 640;
		//
		//		float halfWidth = Values.SCREEN_WIDTH / 2;
		//		float halfHeight = Values.SCREEN_HEIGHT / 2;
		//		float frameSize = PhysicBoundaryBox.FRAME_SIZE;
		//
		//		List<Float> boundaryXValues = Arrays.asList(new Float[] { -(halfWidth + frameSize), -halfWidth, halfWidth,
		//				halfWidth + frameSize });
		//		List<Float> boundaryYValues = Arrays.asList(new Float[] { -(halfHeight + frameSize), -halfHeight, halfHeight,
		//				halfHeight + frameSize });
		//
		//		assertEquals(0, world.getBodyCount());
		//		new PhysicBoundaryBox(world).create();
		//		assertEquals(4, world.getBodyCount());
		//
		//		Body body;
		//		Iterator<Body> bodyIterator = world.getBodies();
		//
		//		while (bodyIterator.hasNext()) {
		//			body = bodyIterator.next();
		//			List<Fixture> fixtures = body.getFixtureList();
		//			assertEquals(1, fixtures.size());
		//			for (Fixture fixture : fixtures) {
		//				assertEquals(Shape.Type.Polygon, fixture.getType());
		//				PolygonShape shape = (PolygonShape) fixture.getShape();
		//				assertEquals(4, shape.getVertexCount());
		//
		//				Vector2 vertex = new Vector2();
		//				for (int index = 0; index < shape.getVertexCount(); index++) {
		//					shape.getVertex(index, vertex);
		//					vertex = PhysicWorldConverter.vecBox2dToCat(vertex);
		//
		//					assertTrue(boundaryXValues.contains(vertex.x));
		//					assertTrue(boundaryYValues.contains(vertex.y));
		//				}
		//			}
		//		}
	}
}
