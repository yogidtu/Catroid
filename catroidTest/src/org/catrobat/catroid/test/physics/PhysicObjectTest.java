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

import java.util.ArrayList;
import java.util.List;

import org.catrobat.catroid.content.Sprite;
import org.catrobat.catroid.physics.PhysicBoundaryBox;
import org.catrobat.catroid.physics.PhysicObject;
import org.catrobat.catroid.physics.PhysicObject.Type;
import org.catrobat.catroid.physics.PhysicWorld;
import org.catrobat.catroid.physics.PhysicWorldConverter;
import org.catrobat.catroid.test.utils.TestUtils;

import android.test.AndroidTestCase;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.Filter;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.Shape;
import com.badlogic.gdx.utils.GdxNativesLoader;

public class PhysicObjectTest extends AndroidTestCase {
	static {
		GdxNativesLoader.load();
	}

	private PhysicWorld physicWorld;

	@Override
	protected void setUp() throws Exception {
		physicWorld = new PhysicWorld();
	}

	@Override
	protected void tearDown() throws Exception {
		physicWorld = null;
	}

	public void testDefaultSettings() {
		assertEquals("Wrong configuration", 1.0f, PhysicObject.DEFAULT_DENSITY);
		assertEquals("Wrong configuration", 0.2f, PhysicObject.DEFAULT_FRICTION);
		assertEquals("Wrong configuration", 0.8f, PhysicObject.DEFAULT_BOUNCE_FACTOR);

		assertEquals("Wrong configuration", 1.0f, PhysicObject.DEFAULT_MASS);
		assertEquals("Wrong configuration", 0.000001f, PhysicObject.MIN_MASS);

		assertEquals("Wrong configuration", 0x0004, PhysicObject.COLLISION_MASK);
	}

	public void testNullBody() {
		try {
			new PhysicObject(null);
			fail("Creating a physic object with no body doesn't cause a NullPointerException");
		} catch (NullPointerException exception) {
			// Expected behavior.
		}
	}

	public void testDefaultProperties() {
		PhysicObject physicObject = createPhysicObject();
		assertEquals("Wrong initialization", Type.NONE, getType(physicObject));
		assertEquals("Wrong initialization", PhysicObject.DEFAULT_MASS, getMass(physicObject));

		Body body = getBody(physicObject);
		assertTrue("Body already contains fixtures", body.getFixtureList().isEmpty());

		FixtureDef fixtureDef = getFixtureDef(physicObject);
		assertEquals("Wrong initialization", PhysicObject.DEFAULT_DENSITY, fixtureDef.density);
		assertEquals("Wrong initialization", PhysicObject.DEFAULT_FRICTION, fixtureDef.friction);
		assertEquals("Wrong initialization", PhysicObject.DEFAULT_BOUNCE_FACTOR, fixtureDef.restitution);

		short collisionBits = 0;
		checkCollisionMask(physicObject, collisionBits, collisionBits);

		assertFalse("Wrong initialization", (Boolean) TestUtils.getPrivateField("ifOnEdgeBounce", physicObject, false));
	}

	public void testSetShape() {
		PhysicObject physicObject = createPhysicObject();
		PolygonShape[] rectangle = new PolygonShape[] { createRectanglePolygonShape(5.0f, 5.0f) };
		physicObject.setShape(rectangle);

		checkIfShapesAreTheSameAsInPhysicObject(rectangle, getBody(physicObject));
	}

	public void testSetNewShape() {
		PhysicObject physicObject = createPhysicObject();
		Shape[] shape = new PolygonShape[] { createRectanglePolygonShape(5.0f, 5.0f) };
		physicObject.setShape(shape);

		Body body = getBody(physicObject);
		PolygonShape[] newShape = new PolygonShape[] { createRectanglePolygonShape(1.0f, 2.0f) };
		physicObject.setShape(newShape);

		assertSame("The new shape hasn't been set", newShape, getShapes(physicObject));
		checkIfShapesAreTheSameAsInPhysicObject(newShape, body);
	}

	public void testSetSameShape() {
		PhysicObject physicObject = createPhysicObject();
		Body body = getBody(physicObject);

		Shape[] rectangle = new Shape[] { createRectanglePolygonShape(5.0f, 5.0f) };
		physicObject.setShape(rectangle);
		assertFalse("No shape has been set", body.getFixtureList().isEmpty());

		List<Fixture> fixturesBeforeReset = new ArrayList<Fixture>(body.getFixtureList());
		physicObject.setShape(rectangle);
		List<Fixture> fixturesAfterReset = new ArrayList<Fixture>(body.getFixtureList());

		assertEquals("Fixture has changed after setiting the same shape again", fixturesBeforeReset, fixturesAfterReset);
	}

	public void testSetNullShapeRemovesAllFixtures() {
		PhysicObject physicObject = createPhysicObject();
		Body body = getBody(physicObject);

		physicObject.setShape(new Shape[] { createRectanglePolygonShape(5.0f, 5.0f) });
		assertFalse("No shape has been set", body.getFixtureList().isEmpty());

		physicObject.setShape(null);
		assertEquals(null, getShapes(physicObject));
		assertTrue("Fixture hasn't been removed", body.getFixtureList().isEmpty());
	}

	public void testSetShapeUpdatesDensityButNotMass() {
		PhysicObject physicObject = createPhysicObject();
		physicObject.setShape(new Shape[] { createRectanglePolygonShape(5.0f, 5.0f) });
		Body body = getBody(physicObject);

		float oldDensity = getFixtureDef(physicObject).density;
		float oldMass = body.getMass();

		physicObject.setShape(new Shape[] { createRectanglePolygonShape(111.0f, 111.0f) });

		assertNotSame("Density hasn't changed", oldDensity, getFixtureDef(physicObject).density);
		assertEquals("Mass has changed", oldMass, body.getMass());
	}

	public void testSetType() {
		PhysicObject physicObject = createPhysicObject();
		Body body = getBody(physicObject);

		physicObject.setType(Type.FIXED);
		assertEquals("Wrong physic object type", Type.FIXED, getType(physicObject));
		assertEquals("Wrong body type", BodyType.KinematicBody, body.getType());

		physicObject.setType(Type.DYNAMIC);
		assertEquals("Wrong physic object type", Type.DYNAMIC, getType(physicObject));
		assertEquals("Wrong body type", BodyType.DynamicBody, body.getType());

		physicObject.setType(Type.NONE);
		assertEquals("Wrong physic object type", Type.NONE, getType(physicObject));
		assertEquals("Wrong body type", BodyType.KinematicBody, body.getType());
	}

	public void testSetCollisionBits() {
		PhysicObject physicObject = createPhysicObject(Type.NONE, 10.0f, 5.0f);
		checkCollisionMask(physicObject, (short) 0, (short) 0);

		physicObject.setType(Type.FIXED);
		checkCollisionMask(physicObject, PhysicObject.COLLISION_MASK, PhysicObject.COLLISION_MASK);

		physicObject.setType(Type.NONE);
		checkCollisionMask(physicObject, (short) 0, (short) 0);

		physicObject.setType(Type.DYNAMIC);
		checkCollisionMask(physicObject, PhysicObject.COLLISION_MASK, PhysicObject.COLLISION_MASK);
	}

	public void testSetTypeToDynamicUpdatesMass() {
		PhysicObject physicObject = createPhysicObject(Type.NONE);
		Body body = getBody(physicObject);

		float rectangeSize = 10.0f;
		physicObject.setShape(new Shape[] { createRectanglePolygonShape(rectangeSize, rectangeSize) });

		float mass = 128.0f;
		physicObject.setMass(mass);
		assertEquals("Wrong mass", 0.0f, body.getMass());

		physicObject.setType(Type.DYNAMIC);
		assertEquals("Mass hasn't been updated", mass, body.getMass());
	}

	public void testAngle() {
		for (Type type : Type.values()) {
			PhysicObject physicObject = createPhysicObject(type);
			assertEquals("Wrong initialization", 0.0f, getBody(physicObject).getAngle());

			float[] degrees = { 1.0f, 131.4f, -10.0f };

			for (float angle : degrees) {
				physicObject.setAngle(angle);

				float physicObjectCatroidAngle = PhysicWorldConverter.angleBox2dToCat(getBody(physicObject).getAngle());
				assertEquals("Wrong catroid angle", angle, physicObjectCatroidAngle);
				assertEquals("Wrong box2d angle", angle, physicObject.getAngle());
			}
		}
	}

	public void testPosition() {
		for (Type type : Type.values()) {
			PhysicObject physicObject = createPhysicObject(type);
			assertEquals("Wrong initialization", new Vector2(), getBody(physicObject).getPosition());

			Vector2[] positions = { new Vector2(12.34f, 56.78f), new Vector2(-87.65f, -43.21f) };
			for (Vector2 position : positions) {
				physicObject.setXYPosition(position.x, position.y);

				Vector2 physicObjectCatroidPosition = PhysicWorldConverter.vecBox2dToCat(getBody(physicObject)
						.getPosition());
				assertEquals("Wrong catroid position", position, physicObjectCatroidPosition);
				assertEquals("Wrong box2d position", position, physicObject.getXPosition());
			}

			for (Vector2 position : positions) {
				physicObject.setXYPosition(position);

				Vector2 physicObjectCatroidPosition = PhysicWorldConverter.vecBox2dToCat(getBody(physicObject)
						.getPosition());
				assertEquals("Wrong catroid position", position, physicObjectCatroidPosition);
				assertEquals("Wrong box2d position", position, physicObject.getXPosition());
			}
		}
	}

	public void testAngleAndPosition() {
		for (Type type : Type.values()) {
			PhysicObject physicObject = createPhysicObject(type);
			assertEquals("Wrong initialization", 0.0f, getBody(physicObject).getAngle());
			assertEquals("initialization", new Vector2(), getBody(physicObject).getPosition());

			float angle = 15.6f;
			Vector2 position = new Vector2(12.34f, 56.78f);
			physicObject.setAngle(angle);
			physicObject.setXYPosition(position.x, position.y);

			float physicObjectCatroidAngle = PhysicWorldConverter.angleBox2dToCat(getBody(physicObject).getAngle());
			Vector2 physicObjectCatroidPosition = PhysicWorldConverter.vecBox2dToCat(getBody(physicObject)
					.getPosition());

			assertEquals("Wrong catroid angle", angle, physicObjectCatroidAngle);
			assertEquals("Wrong catroid position", position, physicObjectCatroidPosition);
		}
	}

	public void testSetDensity() {
		for (Type type : Type.values()) {
			PhysicObject physicObject = createPhysicObject(type);
			physicObject.setShape(new Shape[] { new PolygonShape(), new PolygonShape() });
			float[] densityValues = { 0.123f, -0.765f, 24.32f };

			FixtureProptertyTestTemplate densityTemplate = new FixtureProptertyTestTemplate(physicObject, densityValues) {
				@Override
				protected void setValue(float value) {
					Class<?>[] parameterTypeList = { float.class };
					Object[] values = { value };
					TestUtils.invokeMethod(physicObject, "setDensity", parameterTypeList, values);
				}

				@Override
				protected float getFixtureValue(Fixture fixture) {
					return fixture.getDensity();
				}

				@Override
				protected float getFixtureDefValue() {
					return getFixtureDef(physicObject).density;
				}
			};
			densityTemplate.test();
		}
	}

	public void testSetDensityUpdatesMassData() {
		PhysicObject physicObject = createPhysicObject(Type.DYNAMIC, 5.0f, 5.0f);
		Body body = getBody(physicObject);

		float oldMass = body.getMass();
		float density = 12.0f;
		assertNotSame("Densities are the same", density, getFixtureDef(physicObject).density);

		Class<?>[] parameterTypeList = { float.class };
		Object[] values = { density };
		TestUtils.invokeMethod(physicObject, "setDensity", parameterTypeList, values);

		assertNotSame("Masses are the same", oldMass, body.getMass());
	}

	public void testSetDensityAtMassChange() {
		PhysicObject physicObject = createPhysicObject(Type.DYNAMIC);
		Body body = getBody(physicObject);

		float rectangleSize = 24.0f;
		float[] masses = { PhysicObject.MIN_MASS, 1.0f, 24.0f };

		physicObject.setShape(new Shape[] { createRectanglePolygonShape(rectangleSize, rectangleSize) });
		for (float mass : masses) {
			physicObject.setMass(mass);
			float actualDensity = body.getMass() / (rectangleSize * rectangleSize);
			assertEquals("Wrong density calculation when mass changes", getFixtureDef(physicObject).density,
					actualDensity);
		}
	}

	public void testSetFriction() {
		for (Type type : Type.values()) {
			PhysicObject physicObject = createPhysicObject(type);
			physicObject.setShape(new Shape[] { new PolygonShape(), new PolygonShape() });

			float[] frictionValues = { 0.123f, -0.765f, 24.32f };
			FixtureProptertyTestTemplate frictionTemplate = new FixtureProptertyTestTemplate(physicObject,
					frictionValues) {
				@Override
				protected void setValue(float value) {
					physicObject.setFriction(value);
				}

				@Override
				protected float getFixtureValue(Fixture fixture) {
					return fixture.getFriction();
				}

				@Override
				protected float getFixtureDefValue() {
					return getFixtureDef(physicObject).friction;
				}
			};
			frictionTemplate.test();
		}
	}

	public void testSetBounceFactor() {
		for (Type type : Type.values()) {
			PhysicObject physicObject = createPhysicObject(type);
			physicObject.setShape(new Shape[] { new PolygonShape(), new PolygonShape() });
			float[] bounceFactors = { 0.123f, -0.765f, 24.32f };

			FixtureProptertyTestTemplate restitutionTemplate = new FixtureProptertyTestTemplate(physicObject,
					bounceFactors) {
				@Override
				protected void setValue(float value) {
					physicObject.setBounceFactor(value);
				}

				@Override
				protected float getFixtureValue(Fixture fixture) {
					return fixture.getRestitution();
				}

				@Override
				protected float getFixtureDefValue() {
					return getFixtureDef(physicObject).restitution;
				}
			};
			restitutionTemplate.test();
		}
	}

	public void testMass() {
		for (Type type : Type.values()) {
			PhysicObject physicObject = createPhysicObject(type, 5.0f, 5.0f);
			Body body = getBody(physicObject);

			checkBodyMassDependingOnType(type, body, PhysicObject.DEFAULT_MASS);
			assertEquals("Wrong initialization", PhysicObject.DEFAULT_MASS, getMass(physicObject));

			float[] masses = { PhysicObject.MIN_MASS, 0.01f, 1.0f, 12345.0f };
			for (float mass : masses) {
				physicObject.setMass(mass);
				checkBodyMassDependingOnType(type, body, mass);
				assertEquals("Wrong mass in physic object", mass, getMass(physicObject));
			}

			float[] massesResetedToMinMass = { PhysicObject.MIN_MASS / 10.0f, 0.0f, -1.0f };
			for (float mass : massesResetedToMinMass) {
				physicObject.setMass(mass);
				checkBodyMassDependingOnType(type, body, PhysicObject.MIN_MASS);
				assertEquals("Body mass isn't set to MIN_MASS", PhysicObject.MIN_MASS, getMass(physicObject));
			}
		}
	}

	private void checkBodyMassDependingOnType(Type type, Body body, float expectedBodyMass) {
		if (type != Type.DYNAMIC) {
			expectedBodyMass = 0.0f;
		}
		assertEquals("Wrong mass for " + type.toString().toLowerCase(), expectedBodyMass, body.getMass());
	}

	public void testMassWithNoShapeArea() {
		PhysicObject[] physicObjects = { createPhysicObject(Type.DYNAMIC), createPhysicObject(Type.DYNAMIC, 0.0f, 0.0f) };

		for (PhysicObject physicObject : physicObjects) {
			Body body = getBody(physicObject);

			float oldMass = body.getMass();
			float mass = 1.2f;
			assertNotSame("Masses are the same", oldMass, mass);

			physicObject.setMass(mass);
			assertEquals("Mass changed", oldMass, body.getMass());
			assertEquals("Wrong mass stored", mass, getMass(physicObject));
		}
	}

	public void testSetRotationSpeed() {
		PhysicObject physicObject = createPhysicObject(Type.DYNAMIC);
		Body body = getBody(physicObject);

		assertEquals("Wrong initialization", 0.0f, body.getAngularVelocity());
		float rotationSpeed = 20.0f;
		physicObject.setRotationSpeed(rotationSpeed);

		float physicObjectCatroidRotationSpeed = PhysicWorldConverter.angleBox2dToCat(body.getAngularVelocity());
		assertEquals("Set wrong rotation speed", rotationSpeed, physicObjectCatroidRotationSpeed);
	}

	public void testSetVelocity() {
		PhysicObject physicObject = createPhysicObject(Type.DYNAMIC);
		Body body = getBody(physicObject);

		assertEquals("Wrong initialization", new Vector2(), body.getLinearVelocity());
		Vector2 velocity = new Vector2(12.3f, 45.6f);
		physicObject.setVelocity(velocity);

		Vector2 physicObjectCatVelocity = PhysicWorldConverter.vecBox2dToCat(body.getLinearVelocity());
		assertEquals("Set wrong velocity", velocity, physicObjectCatVelocity);
	}

	public void testIfOnEndgeBounce() {
		PhysicObject physicObject = createPhysicObject(Type.DYNAMIC, 1.0f, 1.0f);
		physicObject.setIfOnEdgeBounce(true);

		assertTrue("If on edge bounce hasn't been set correctly",
				(Boolean) TestUtils.getPrivateField("ifOnEdgeBounce", physicObject, false));
		checkCollisionMask(physicObject, PhysicObject.COLLISION_MASK,
				(short) (PhysicObject.COLLISION_MASK | PhysicBoundaryBox.COLLISION_MASK));

		physicObject.setIfOnEdgeBounce(false);
		assertFalse("If on edge bounce hasn't been set correctly",
				(Boolean) TestUtils.getPrivateField("ifOnEdgeBounce", physicObject, false));
		checkCollisionMask(physicObject, PhysicObject.COLLISION_MASK, PhysicObject.COLLISION_MASK);
	}

	// SANTA'S LITTLE HELPERS :)
	// (TODO: Also need to be tested.)

	// Creating Physic Objects helper methods.
	protected PhysicObject createPhysicObject(PhysicObject.Type type, float width, float height) {
		return createPhysicObject(type, createRectanglePolygonShape(width, height));
	}

	protected PolygonShape createRectanglePolygonShape(float width, float height) {
		PolygonShape rectangle = new PolygonShape();
		rectangle.setAsBox(width / 2.0f, height / 2.0f);
		return rectangle;
	}

	protected PhysicObject createPhysicObject(PhysicObject.Type type, Shape shape) {
		PhysicObject physicObject = physicWorld.getPhysicObject(new Sprite("TestSprite"));

		if (type != null || type == Type.NONE) {
			physicObject.setType(type);
		}

		if (shape != null) {
			physicObject.setShape(new Shape[] { shape });
		}

		return physicObject;
	}

	protected PhysicObject createPhysicObject(PhysicObject.Type type) {
		return createPhysicObject(type, null);
	}

	protected PhysicObject createPhysicObject() {
		return createPhysicObject(null, null);
	}

	// Private member helper methods.
	private Body getBody(PhysicObject physicObject) {
		return (Body) TestUtils.getPrivateField("body", physicObject, false);
	}

	private Type getType(PhysicObject physicObject) {
		return (Type) TestUtils.getPrivateField("type", physicObject, false);
	}

	private float getMass(PhysicObject physicObject) {
		return (Float) TestUtils.getPrivateField("mass", physicObject, false);
	}

	private Shape[] getShapes(PhysicObject physicObject) {
		return (Shape[]) TestUtils.getPrivateField("shapes", physicObject, false);
	}

	private FixtureDef getFixtureDef(PhysicObject physicObject) {
		return (FixtureDef) TestUtils.getPrivateField("fixtureDef", physicObject, false);
	}

	// Fixture property helper.
	private abstract class FixtureProptertyTestTemplate {
		protected final PhysicObject physicObject;
		protected final float[] values;

		public FixtureProptertyTestTemplate(PhysicObject physicObject, float[] values) {
			this.physicObject = physicObject;
			this.values = values;
		}

		public void test() {
			assertTrue("Without any values the correctness won't be tested.", values.length > 0);
			assertFalse("Without any fixtures the correctness won't be tested.", getBody(physicObject).getFixtureList()
					.isEmpty());

			for (float value : values) {
				setValue(value);
				assertEquals("Wrong fixture def value in physic object", value, getFixtureDefValue());
				for (Fixture fixture : getBody(physicObject).getFixtureList()) {
					assertEquals("Wrong fixture def value in bodies fixtures.", value, getFixtureValue(fixture));
				}
			}
		}

		protected abstract float getFixtureValue(Fixture fixture);

		protected abstract float getFixtureDefValue();

		protected abstract void setValue(float value);
	}

	// ... and other helpers
	private void checkCollisionMask(PhysicObject physicObject, short categoryBits, short maskBits) {
		FixtureDef fixtureDef = getFixtureDef(physicObject);
		assertEquals("Different category bits", categoryBits, fixtureDef.filter.categoryBits);
		assertEquals("Different bit mask", maskBits, fixtureDef.filter.maskBits);

		Body body = getBody(physicObject);
		for (Fixture fixture : body.getFixtureList()) {
			Filter filter = fixture.getFilterData();
			assertEquals("Different category bits", categoryBits, filter.categoryBits);
			assertEquals("Different bit mask", maskBits, filter.maskBits);
		}
	}

	private void checkIfShapesAreTheSameAsInPhysicObject(PolygonShape[] shapes, Body body) {
		List<Fixture> fixtures = body.getFixtureList();
		assertEquals("Number of shapes and fixtures are not the same", shapes.length, fixtures.size());

		if (body.getFixtureList().isEmpty()) {
			return;
		}

		PolygonShape currentShape;
		PolygonShape currentPhysicObjectShape;
		for (int shapeIndex = 0; shapeIndex < shapes.length; shapeIndex++) {
			currentShape = shapes[shapeIndex];
			currentPhysicObjectShape = (PolygonShape) fixtures.get(shapeIndex).getShape();
			assertEquals("Different vertex count", currentShape.getVertexCount(),
					currentPhysicObjectShape.getVertexCount());

			Vector2 expectedVertex = new Vector2();
			Vector2 actualVertex = new Vector2();
			for (int vertexIndex = 0; vertexIndex < currentShape.getVertexCount(); vertexIndex++) {
				currentShape.getVertex(vertexIndex, expectedVertex);
				currentPhysicObjectShape.getVertex(vertexIndex, actualVertex);
				assertEquals("Vertex are different", expectedVertex, actualVertex);
			}
		}
	}
}
