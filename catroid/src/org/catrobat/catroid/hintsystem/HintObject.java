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
package org.catrobat.catroid.hintsystem;

import android.graphics.Paint;

/**
 * @author amore
 * 
 */
public class HintObject {

	private int pointerXCoordinate;
	private int pointerYCoordinate;
	private int textXCoordinate;
	private int textYCoordinate;
	private String text;
	private int maxWidthObject;

	public HintObject(int[] coordinates, String text) {
		this.pointerXCoordinate = ScreenParameters.getInstance().setCoordinatesToDensity(
				coordinates[0] + ScreenParameters.getInstance().getTextMarginLeft(), true);
		this.pointerYCoordinate = ScreenParameters.getInstance().setCoordinatesToDensity(coordinates[1], false);

		this.maxWidthObject = coordinates[2];
		this.text = addingLineBreaks(text);
		examineTextPositions(coordinates);

	}

	private void examineTextPositions(int[] coordinates) {
		this.textXCoordinate = ScreenParameters.getInstance().setCoordinatesToDensity(
				coordinates[0] - ScreenParameters.getInstance().getTextMarginLeft(), true);

		if ((Hint.getInstance().getScreenHeight() - pointerYCoordinate) < 150) {
			this.textYCoordinate = ScreenParameters.getInstance().setCoordinatesToDensity(
					coordinates[1] - ScreenParameters.getInstance().getTextMarginBottom(), false);
		} else {
			this.textYCoordinate = ScreenParameters.getInstance().setCoordinatesToDensity(
					coordinates[1] + ScreenParameters.getInstance().getTextMarginTop(), false);
		}

	}

	private String addingLineBreaks(String text) {
		int screenWidth = Hint.getInstance().getScreenWidth();
		int marginRight = ScreenParameters.getInstance().getTextMarginRight() * screenWidth / 100;
		int textWidth = maxWidthObject - marginRight;

		Paint paint = new Paint();
		paint.setTextSize(25);

		String formatedText = "";
		if (paint.measureText(text) < textWidth) {
			formatedText = text;
		} else {

			String[] words = text.split(" ");
			String currentLine = "";

			for (int i = 0; i < words.length; i++) {
				if (paint.measureText(currentLine + words[i] + " ") < textWidth) {
					currentLine += words[i] + " ";
					formatedText += words[i] + " ";
				} else {
					currentLine = words[i] + " ";
					formatedText += "\n" + words[i] + " ";
				}
			}
		}

		return formatedText;
	}

	public int getXCoordinate() {
		return pointerXCoordinate;
	}

	public int getYCoordinate() {
		return pointerYCoordinate;
	}

	public String getText() {
		return text;
	}

	public int getTextXCoordinate() {
		return textXCoordinate;
	}

	public int getTextYCoordinate() {
		return textYCoordinate;
	}

}
