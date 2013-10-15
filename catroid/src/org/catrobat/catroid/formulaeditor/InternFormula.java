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
package org.catrobat.catroid.formulaeditor;

import android.util.Log;

import java.util.List;

public class InternFormula {

	public static enum CursorTokenPosition {
		LEFT, MIDDLE, RIGHT;
	};

	public static enum CursorTokenPropertiesAfterModification {
		LEFT, RIGHT, SELECT, DO_NOT_MODIFY;
	}

	public static enum TokenSelectionType {
		USER_SELECTION, PARSER_ERROR_SELECTION;
	}

	private ExternInternRepresentationMapping externInternRepresentationMapping;

	private List<InternToken> internTokenFormulaList;
	private String externFormulaString;

	private InternFormulaTokenSelection internFormulaTokenSelection;

	private int externCursorPosition;

	private InternToken cursorPositionInternToken;
	private int cursorPositionInternTokenIndex;
	private CursorTokenPosition cursorTokenPosition;

	private InternFormulaParser internTokenFormulaParser;

	public InternFormula(List<InternToken> internTokenList) {

		this.internTokenFormulaList = internTokenList;
		this.externFormulaString = null;
		this.externInternRepresentationMapping = new ExternInternRepresentationMapping();
		this.internFormulaTokenSelection = null;
		this.externCursorPosition = 0;
		this.cursorPositionInternTokenIndex = 0;
	}

	public InternFormula(List<InternToken> internTokenList, InternFormulaTokenSelection internFormulaTokenSelection,
			int externCursorPosition) {
		this.internTokenFormulaList = internTokenList;
		this.externFormulaString = null;
		externInternRepresentationMapping = new ExternInternRepresentationMapping();
		this.internFormulaTokenSelection = internFormulaTokenSelection;
		this.externCursorPosition = externCursorPosition;

		updateInternCursorPosition();

	}
	public void updateInternCursorPosition() {
		int cursorPositionTokenIndex = externInternRepresentationMapping
				.getInternTokenByExternIndex(externCursorPosition);

		int leftCursorPositionTokenIndex = externInternRepresentationMapping
				.getInternTokenByExternIndex(externCursorPosition - 1);

		int leftleftCursorPositionTokenIndex = externInternRepresentationMapping
				.getInternTokenByExternIndex(externCursorPosition - 2);

		if (cursorPositionTokenIndex != ExternInternRepresentationMapping.MAPPING_NOT_FOUND) {
			if (leftCursorPositionTokenIndex != ExternInternRepresentationMapping.MAPPING_NOT_FOUND
					&& cursorPositionTokenIndex == leftCursorPositionTokenIndex) {
				cursorTokenPosition = CursorTokenPosition.MIDDLE;
			} else {
				cursorTokenPosition = CursorTokenPosition.LEFT;
			}

		} else if (leftCursorPositionTokenIndex != ExternInternRepresentationMapping.MAPPING_NOT_FOUND) {
			cursorTokenPosition = CursorTokenPosition.RIGHT;

		} else if (leftleftCursorPositionTokenIndex != ExternInternRepresentationMapping.MAPPING_NOT_FOUND) {
			cursorTokenPosition = CursorTokenPosition.RIGHT;
			leftCursorPositionTokenIndex = leftleftCursorPositionTokenIndex;
		} else {

			cursorTokenPosition = null;
			this.cursorPositionInternToken = null;
			return;
		}

		switch (cursorTokenPosition) {
			case LEFT:
				this.cursorPositionInternToken = internTokenFormulaList.get(cursorPositionTokenIndex);
				this.cursorPositionInternTokenIndex = cursorPositionTokenIndex;
				Log.i("info", "LEFT of " + cursorPositionInternToken.getTokenStringValue());
				break;
			case MIDDLE:
				this.cursorPositionInternToken = internTokenFormulaList.get(cursorPositionTokenIndex);
				this.cursorPositionInternTokenIndex = cursorPositionTokenIndex;
				Log.i("info", "SELECTED " + cursorPositionInternToken.getTokenStringValue());
				break;
			case RIGHT:
				this.cursorPositionInternToken = internTokenFormulaList.get(leftCursorPositionTokenIndex);
				this.cursorPositionInternTokenIndex = leftCursorPositionTokenIndex;
				Log.i("info", "RIGHT of " + cursorPositionInternToken.getTokenStringValue());
				break;

		}
	}

}
