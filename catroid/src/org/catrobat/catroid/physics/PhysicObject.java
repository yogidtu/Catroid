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
package org.catrobat.catroid.physics;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.Filter;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.Shape;

public class PhysicObject {
	public enum Type {
		DYNAMIC, FIXED, NONE;
	}

	public final static float DEFAULT_DENSITY = 1.0f;
	public final static float DEFAULT_FRICTION = 0.2f;
	public final static float DEFAULT_BOUNCE_FACTOR = 0.8f;
	public final static float DEFAULT_MASS = 1.0f;
	public final static float MIN_MASS = 0.000001f;
	public final static short COLLISION_MASK = 0x0004;

	private final Body body;
	private final FixtureDef fixtureDef = new FixtureDef();
	private Shape[] shapes;
	private Type type;
	private float mass;
	private boolean ifOnEdgeBounce = false;

	public PhysicObject(Body body) {
		this.body = body;

		mass = PhysicObject.DEFAULT_MASS;
		fixtureDef.density = PhysicObject.DEFAULT_DENSITY;
		fixtureDef.friction = PhysicObject.DEFAULT_FRICTION;
		fixtureDef.restitution = PhysicObject.DEFAULT_BOUNCE_FACTOR;

		short collisionBits = 0;
		setCollisionBits(collisionBits, collisionBits);
		setType(Type.DYNAMIC);
	}

	public void setShape(Shape[] shapes) {
		if (this.shapes == shapes) {
			return;
		}
		this.shapes = shapes;

		List<Fixture> fixturesOld = new ArrayList<Fixture>(body.getFixtureList());

		if (shapes != null) {
			for (Shape tempShape : shapes) {
				fixtureDef.shape = tempShape;
				body.createFixture(fixtureDef);
			}
		}

		for (Fixture fixture : fixturesOld) {
			body.destroyFixture(fixture);
		}

		setMass(mass);
	}

	public void setIfOnEdgeBounce(boolean bounce) {
		if (ifOnEdgeBounce == bounce) {
			return;
		}
		ifOnEdgeBounce = bounce;

		short maskBits;
		if (bounce) {
			maskBits = PhysicObject.COLLISION_MASK | PhysicBoundaryBox.COLLISION_MASK;
		} else {
			maskBits = PhysicObject.COLLISION_MASK;
		}

		setCollisionBits(fixtureDef.filter.categoryBits, maskBits);
	}

	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		if (this.type == type) {
			return;
		}
		this.type = type;

		short collisionMask = 0;
		switch (type) {
			case DYNAMIC:
				body.setType(BodyType.DynamicBody);
				body.setGravityScale(1.0f);
				setMass(mass);
				collisionMask = PhysicObject.COLLISION_MASK;
				break;
			case FIXED:
				body.setType(BodyType.KinematicBody);
				collisionMask = PhysicObject.COLLISION_MASK;
				break;
			case NONE:
				body.setType(BodyType.KinematicBody);
				break;
		}
		setCollisionBits(collisionMask, collisionMask);
	}

	/**
	 * if ((categoryBitsB & maskBitsA) != 0) {
	 * - A collides with B.
	 * }
	 */
	protected void setCollisionBits(short categoryBits, short maskBits) {
		fixtureDef.filter.categoryBits = categoryBits;
		fixtureDef.filter.maskBits = maskBits;

		for (Fixture fixture : body.getFixtureList()) {
			Filter filter = fixture.getFilterData();
			filter.categoryBits = categoryBits;
			filter.maskBits = maskBits;
			fixture.setFilterData(filter);
		}
	}

	public float getDirection() {
		return PhysicWorldConverter.angleBox2dToCat(body.getAngle());
	}

	public void setDirection(float degrees) {
		body.setTransform(body.getPosition(), PhysicWorldConverter.angleCatToBox2d(degrees));
	}

	public float getX() {
		return PhysicWorldConverter.lengthBox2dToCat(body.getPosition().x);
	}

	public float getY() {
		return PhysicWorldConverter.lengthBox2dToCat(body.getPosition().y);
	}

	public Vector2 getPosition() {
		return PhysicWorldConverter.vecBox2dToCat(body.getPosition());
	}

	public void setX(float x) {
		body.setTransform(PhysicWorldConverter.lengthCatToBox2d(x), body.getPosition().y, body.getAngle());
	}

	public void setY(float y) {
		body.setTransform(body.getPosition().x, PhysicWorldConverter.lengthCatToBox2d(y), body.getAngle());
	}

	public void setPosition(float x, float y) {
		x = PhysicWorldConverter.lengthCatToBox2d(x);
		y = PhysicWorldConverter.lengthCatToBox2d(y);
		body.setTransform(x, y, body.getAngle());
	}

	public void setPosition(Vector2 position) {
		setPosition(position.x, position.y);
	}

	public float getRotationSpeed() {
		return (float) Math.toDegrees(body.getAngularVelocity());
	}

	public void setRotationSpeed(float degreesPerSecond) {
		body.setAngularVelocity((float) Math.toRadians(degreesPerSecond));
	}

	public Vector2 getVelocity() {
		return PhysicWorldConverter.vecBox2dToCat(body.getLinearVelocity());
	}

	public void setVelocity(float x, float y) {
		body.setLinearVelocity(PhysicWorldConverter.lengthCatToBox2d(x), PhysicWorldConverter.lengthCatToBox2d(y));
	}

	public float getMass() {
		return body.getMass();
	}

	public void setMass(float mass) {
		if (mass < PhysicObject.MIN_MASS) {
			mass = PhysicObject.MIN_MASS;
		}

		//		if (mass != Integer.MAX_VALUE) {
		this.mass = mass;
		//		}

		float bodyMass = body.getMass();
		if (bodyMass == 0.0f) {
			return;
		}

		float area = bodyMass / fixtureDef.density;
		float density = mass / area;
		setDensity(density);
	}

	private void setDensity(float density) {
		fixtureDef.density = density;
		for (Fixture fixture : body.getFixtureList()) {
			fixture.setDensity(density);
		}
		body.resetMassData();
	}

	public void setFriction(float friction) {
		fixtureDef.friction = friction;
		for (Fixture fixture : body.getFixtureList()) {
			fixture.setFriction(friction);
		}
	}

	public void setBounceFactor(float bounceFactor) {
		fixtureDef.restitution = bounceFactor;
		for (Fixture fixture : body.getFixtureList()) {
			fixture.setRestitution(bounceFactor);
		}
	}
}
