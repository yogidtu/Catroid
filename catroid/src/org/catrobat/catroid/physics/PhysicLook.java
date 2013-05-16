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

import org.catrobat.catroid.content.Look;
import org.catrobat.catroid.content.Sprite;
import org.catrobat.catroid.physics.shapebuilder.PhysicShapeBuilder;

import com.badlogic.gdx.physics.box2d.Shape;

public class PhysicLook extends Look {
	private final PhysicObject physicObject;
	private final PhysicShapeBuilder physicShapeBuilder;

	public PhysicLook(Sprite sprite, PhysicShapeBuilder physicShapeBuilder, PhysicObject physicObject) {
		super(sprite);

		this.physicShapeBuilder = physicShapeBuilder;
		this.physicObject = physicObject;
	}

	@Override
	protected boolean checkImageChanged() {
		if (super.checkImageChanged()) {
			Shape[] shapes = physicShapeBuilder.getShape(getLookData(), getSize());
			physicObject.setShape(shapes);
			return true;
		}

		return false;
	}

	public void updatePositionAndRotation() {
		super.setXYInUserInterfaceDimensionUnit(physicObject.getXPosition(), physicObject.getYPosition());
		super.setRotation(physicObject.getAngle());
	}

	@Override
	public void setXInUserInterfaceDimensionUnit(float x) {
		physicObject.setXPosition(x);
	}

	@Override
	public void setYInUserInterfaceDimensionUnit(float y) {
		physicObject.setYPosition(y);
	}

	@Override
	public void setXYInUserInterfaceDimensionUnit(float x, float y) {
		physicObject.setXYPosition(x, y);
	}

	@Override
	public void setRotation(float degrees) {
		physicObject.setAngle(degrees);
	}

	@Override
	public float getRotation() {
		return physicObject.getAngle();
	}

	@Override
	public float getXInUserInterfaceDimensionUnit() {
		return physicObject.getXPosition();
	}

	@Override
	public float getYInUserInterfaceDimensionUnit() {
		return physicObject.getYPosition();
	}

	@Override
	public void setSize(float size) {
		super.setSize(size);

		Shape[] shapes = physicShapeBuilder.getShape(getLookData(), getSize());
		physicObject.setShape(shapes);
	}

}
