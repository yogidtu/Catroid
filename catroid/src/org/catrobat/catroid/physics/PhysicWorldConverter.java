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

import com.badlogic.gdx.math.Vector2;

public final class PhysicWorldConverter {

	public static float angleBox2dToCat(float angle) {
		return (float) Math.toDegrees(angle);
	}

	public static float angleCatToBox2d(float angle) {
		return (float) Math.toRadians(angle);
	}

	public static float lengthCatToBox2d(float length) {
		return length / PhysicWorld.RATIO;
	}

	public static float lengthBox2dToCat(float length) {
		return length * PhysicWorld.RATIO;
	}

	public static Vector2 vecCatToBox2d(Vector2 vector) {
		return new Vector2(lengthCatToBox2d(vector.x), lengthCatToBox2d(vector.y));
	}

	public static Vector2 vecBox2dToCat(Vector2 vector) {
		return new Vector2(lengthBox2dToCat(vector.x), lengthBox2dToCat(vector.y));
	}
}
