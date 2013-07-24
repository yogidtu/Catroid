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

import java.io.Serializable;

import org.catrobat.catroid.content.Sprite;
import org.catrobat.catroid.formulaeditor.FormulaElement.ElementType;

import android.graphics.drawable.Drawable;
import android.text.Layout;
import android.view.View;
import android.widget.EditText;

public class Formula implements Serializable {

	private static final long serialVersionUID = 1L;
	private FormulaElement formulaTree;
	private transient Integer formulaTextFieldId = null;
	private transient Drawable originalEditTextDrawable = null;
	private transient InternFormula internFormula = null;

	public Object readResolve() {

		if (formulaTree == null) {
			formulaTree = new FormulaElement(ElementType.NUMBER, "0 ", null);
		}

		internFormula = new InternFormula(formulaTree.getInternTokenList());

		return this;
	}

	public Formula(FormulaElement formulaElement) {
		formulaTree = formulaElement;
		internFormula = new InternFormula(formulaTree.getInternTokenList());
	}

	public Formula(Integer value) {
		if (value < 0) {
			formulaTree = new FormulaElement(ElementType.OPERATOR, Operators.MINUS.toString(), null);
			formulaTree.setRightChild(new FormulaElement(ElementType.NUMBER, Long.toString(Math.abs((long) value)),
					formulaTree));
			internFormula = new InternFormula(formulaTree.getInternTokenList());
		} else {
			formulaTree = new FormulaElement(ElementType.NUMBER, value.toString(), null);
			internFormula = new InternFormula(formulaTree.getInternTokenList());
		}
	}

	public Formula(Float value) {
		this(Double.valueOf(value));
	}

	public Formula(Double value) {
		if (value < 0) {
			formulaTree = new FormulaElement(ElementType.OPERATOR, Operators.MINUS.toString(), null);
			formulaTree.setRightChild(new FormulaElement(ElementType.NUMBER, Double.toString(Math.abs(value)),
					formulaTree));
			internFormula = new InternFormula(formulaTree.getInternTokenList());
		} else {
			formulaTree = new FormulaElement(ElementType.NUMBER, value.toString(), null);
			internFormula = new InternFormula(formulaTree.getInternTokenList());
		}
	}

	public boolean interpretBoolean(Sprite sprite) {
		int result = interpretInteger(sprite);

		return result != 0 ? true : false;

	}

	public int interpretInteger(Sprite sprite) {
		Double interpretedValue = formulaTree.interpretRecursive(sprite);
		return interpretedValue.intValue();
	}

	public double interpretDouble(Sprite sprite) {
		return formulaTree.interpretRecursive(sprite);
	}

	public float interpretFloat(Sprite sprite) {
		return (float) interpretDouble(sprite);
	}

	public void setRoot(FormulaElement formula) {
		formulaTree = formula;
		internFormula = new InternFormula(formula.getInternTokenList());

	}

	public void setTextFieldId(int id) {
		formulaTextFieldId = id;
	}

	public void refreshTextField(View view) {
		if (formulaTextFieldId != null && formulaTree != null && view != null) {
			EditText formulaTextField = (EditText) view.findViewById(formulaTextFieldId);
			if (formulaTextField == null) {
				return;
			}
			internFormula.generateExternFormulaStringAndInternExternMapping(view.getContext());

			formulaTextField.setText(internFormula.getExternFormulaString());
		}

	}

	public void refreshTextField(View view, String formulaString, int position) {
		if (formulaTextFieldId != null && formulaTree != null && view != null) {
			EditText formulaTextField = (EditText) view.findViewById(formulaTextFieldId);
			if (formulaTextField == null) {
				return;
			}
			formulaTextField.setText(formulaString);
			Layout formulaTextFieldLayout = formulaTextField.getLayout();
			if (position < 0 || formulaTextFieldLayout == null) {
				return;
			}
			int char_count = formulaTextFieldLayout.getLineVisibleEnd(0);
			if (formulaString.length() > char_count && char_count > 0) {
				int start = position - (char_count / 2);
				int end = position + (char_count / 2) + 1;
				if (end > formulaString.length() - 1) {
					end = formulaString.length() - 1;
					start = end - char_count;
				}
				if (start < 0) {
					start = 0;
					end = char_count;
				}
				formulaTextField.setText(formulaString.substring(start, end));
			}
		}
	}

	public void prepareToRemove() {
		originalEditTextDrawable = null;
		formulaTextFieldId = null;
	}

	public InternFormulaState getInternFormulaState() {
		return internFormula.getInternFormulaState();
	}

	public boolean containsElement(FormulaElement.ElementType elementType) {
		if (formulaTree.containsElement(elementType)) {
			return true;
		}

		return false;
	}

	public boolean isLogicalFormula() {
		return formulaTree.isLogicalOperator();
	}

	public boolean isSingleNumberFormula() {
		return formulaTree.isSingleNumberFormula();
	}

	@Override
	public Formula clone() {
		if (formulaTree != null) {
			return new Formula(formulaTree.clone());
		}

		return new Formula(0);
	}

}
