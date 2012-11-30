/**
 *  Catroid: An on-device visual programming system for Android devices
 *  Copyright (C) 2010-2012 The Catrobat Team
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
import java.util.Stack;

import org.catrobat.catroid.common.CostumeData;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.Shape;

public class PhysicShapeBuilderStrategyA implements PhysicShapeBuilderStrategy {

	private enum Side {
		LEFT, RIGHT, NONE;
	}

	@Override
	public Shape[] build(CostumeData costumeData) {
		Pixmap pixmap = costumeData.getPixmap();
		int width = pixmap.getWidth();
		int height = pixmap.getHeight();

		Stack<Vector2> leftConvexHull = new Stack<Vector2>();
		Stack<Vector2> rightConvexHull = new Stack<Vector2>();
		for (int y = 0; y < height; y++) {
			int x;
			for (x = 0; x < width; x++) {
				if ((pixmap.getPixel(x, y) & 0xff) > 0) {
					addPoint(leftConvexHull, new Vector2(x, y), Side.RIGHT);
					break;
				}
			}

			if (x == width) {
				continue;
			}

			for (x = width - 1; x >= 0; x--) {
				if ((pixmap.getPixel(x, y) & 0xff) > 0) {
					addPoint(rightConvexHull, new Vector2(x, y), Side.LEFT);
					x = width - 1;
					break;
				}
			}
		}

		if (leftConvexHull.empty()) {
			return null;
		}
		Vector2 point;
		while (!rightConvexHull.empty()) {
			point = rightConvexHull.pop();
			if (point.equals(leftConvexHull.firstElement()) || point.equals(leftConvexHull.peek())) {
				continue;
			}

			leftConvexHull.push(point);
		}

		return devideShape(leftConvexHull.toArray(new Vector2[leftConvexHull.size()]), width, height);
	}

	private void addPoint(Stack<Vector2> convexHull, Vector2 point, Side side) {
		removePoints(convexHull, point, side);
		convexHull.push(point);
	}

	private void removePoints(Stack<Vector2> convexHull, Vector2 newConvexPoint, Side side) {
		while (convexHull.size() > 1) {
			Vector2 pointToCheck = convexHull.peek();
			Vector2 from = convexHull.get(convexHull.size() - 2);

			if (side == getSide(from, newConvexPoint, pointToCheck)) {
				break;
			}

			convexHull.pop();
		}
	}

	private Side getSide(Vector2 from, Vector2 to, Vector2 pointToCheck) {
		double det = from.x * to.y + to.x * pointToCheck.y + pointToCheck.x * from.y - pointToCheck.x * to.y - to.x
				* from.y - from.x * pointToCheck.y;

		if (det > 0) {
			return Side.RIGHT;
		} else if (det < 0) {
			return Side.LEFT;
		} else {
			return Side.NONE;
		}
	}

	private Shape[] devideShape(Vector2[] convexpoints, int width, int height) {
		for (int index = 0; index < convexpoints.length; index++) {
			Vector2 point = convexpoints[index];
			point.x -= width / 2;
			point.y = height - point.y - height / 2;
			convexpoints[index] = PhysicWorldConverter.vecCatToBox2d(point);
		}

		if (convexpoints.length < 9) {
			PolygonShape polygon = new PolygonShape();
			polygon.set(convexpoints);
			return new Shape[] { polygon };
		}

		List<Shape> shapes = new ArrayList<Shape>(convexpoints.length / 6 + 1);
		List<Vector2> pointsPerShape = new ArrayList<Vector2>(8);

		Vector2 rome = convexpoints[0];
		int index = 1;
		while (index < convexpoints.length - 1) {
			int k = index + 7;

			int remainingPointsCount = convexpoints.length - index;
			if (remainingPointsCount > 7 && remainingPointsCount < 9) {
				k -= 3;
			}

			pointsPerShape.add(rome);
			for (; index < k && index < convexpoints.length; index++) {
				pointsPerShape.add(convexpoints[index]);
			}

			PolygonShape polygon = new PolygonShape();
			polygon.set(pointsPerShape.toArray(new Vector2[pointsPerShape.size()]));
			shapes.add(polygon);

			pointsPerShape.clear();
			index--;
		}

		return shapes.toArray(new Shape[shapes.size()]);
	}
}
