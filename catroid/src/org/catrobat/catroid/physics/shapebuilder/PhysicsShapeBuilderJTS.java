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
package org.catrobat.catroid.physics.shapebuilder;

import java.util.ArrayList;
import java.util.List;

import org.catrobat.catroid.common.LookData;
import org.catrobat.catroid.physics.PhysicsWorldConverter;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.math.EarClippingTriangulator;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.Shape;
import com.vividsolutions.jts.util.GeometricShapeFactory;

public class PhysicsShapeBuilderJTS implements PhysicsShapeBuilderStrategy {

	@Override
	public Shape[] build(LookData lookData) {
		Pixmap pixmap = lookData.getPixmap();
		int width = pixmap.getWidth();
		int height = pixmap.getHeight();
		GeometricShapeFactory gf = new GeometricShapeFactory();

		List<Vector2> s = ImageProcessor.getShape(lookData.getPixmap());
		//		List<Vector2> vec2 = new ArrayList<Vector2>();
		EarClippingTriangulator triangulator = new EarClippingTriangulator();
		//		for (Pixel p : s) {
		//			Vector2 vec = new Vector2(p.x, p.y);
		//			vec2.add(vec);
		//			//			pixmap.drawPixel(p.x, p.y, Color.RED);
		//		}
		List<Vector2> triangles = triangulator.computeTriangles(s);
		List<Vector2> convertedTriangles = new ArrayList<Vector2>();
		List<Shape> shapes = new ArrayList<Shape>();

		for (Vector2 triangle : triangles) {
			triangle.x -= width / 2;
			triangle.y = height - triangle.y - height / 2;
			triangle = PhysicsWorldConverter.vecCatToBox2d(triangle);
			convertedTriangles.add(triangle);
		}

		for (int i = 2; i < convertedTriangles.size(); i += 3) {
			PolygonShape currentShape = new PolygonShape();
			Vector2 a = convertedTriangles.get(i - 2);
			Vector2 b = convertedTriangles.get(i - 1);
			Vector2 c = convertedTriangles.get(i);
			Vector2[] triangle = { a, b, c };
			currentShape.set(triangle);
			shapes.add(currentShape);
		}

		return shapes.toArray(new Shape[shapes.size()]);
	}
}