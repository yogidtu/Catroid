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
package org.catrobat.catroid.tooltipsystem;

import android.graphics.Paint;

/**
 * @author amore
 * 
 */
public class ToolTipObject {

	private int textXCoordinate;
	private int textYCoordinate;
	private String tooltipText;
	private int maxWidthObject;

	public ToolTipObject(int[] coordinates, String text) {
		this.textXCoordinate = coordinates[0];
		this.textYCoordinate = coordinates[1];
		this.maxWidthObject = coordinates[2];
		this.tooltipText = addingLineBreaks(text);

	}

	private String addingLineBreaks(String text) {
		int textWidth = maxWidthObject - 40;

		Paint paint = new Paint();
		paint.setTextSize(25);

		String formatedText = "";
		if (paint.measureText(text) < textWidth) {
			formatedText = text;
		} else {

			formatedText = insertLineBreakInString(paint, textWidth, text);
		}

		return formatedText;
	}

	private String insertLineBreakInString(Paint paint, int textWidth, String text) {
		String[] words = text.split(" ");
		String currentLine = "";
		String formatedText = "";

		for (int i = 0; i < words.length; i++) {
			if (paint.measureText(currentLine + words[i] + " ") < textWidth) {
				currentLine += words[i] + " ";
				formatedText += words[i] + " ";
			} else {
				currentLine = words[i] + " ";
				formatedText += "\n" + words[i] + " ";
			}
		}

		return formatedText;
	}

	public String getTooltipText() {
		return tooltipText;
	}

	public int getTextXCoordinate() {
		return textXCoordinate;
	}

	public int getTextYCoordinate() {
		return textYCoordinate;
	}
}
