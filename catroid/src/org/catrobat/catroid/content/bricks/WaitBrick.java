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
package org.catrobat.catroid.content.bricks;

import com.badlogic.gdx.scenes.scene2d.actions.SequenceAction;

import org.catrobat.catroid.content.Script;
import org.catrobat.catroid.content.Sprite;
import org.catrobat.catroid.content.actions.ExtendedActions;
import org.catrobat.catroid.formulaeditor.Formula;

import java.util.List;

public class WaitBrick extends BrickBaseType implements FormulaBrick {
	private static final long serialVersionUID = 1L;
	private Formula timeToWaitInSeconds;

	public WaitBrick(Sprite sprite, int timeToWaitInMillisecondsValue) {
		this.sprite = sprite;
		timeToWaitInSeconds = new Formula(timeToWaitInMillisecondsValue / 1000.0);
	}

	public WaitBrick(Sprite sprite, Formula timeToWaitInSecondsFormula) {
		this.sprite = sprite;
		this.timeToWaitInSeconds = timeToWaitInSecondsFormula;
	}

	public WaitBrick() {

	}

	@Override
	public Formula getFormula() {
		return timeToWaitInSeconds;
	}

	@Override
	public int getRequiredResources() {
		return NO_RESOURCES;
	}

	public Formula getTimeToWait() {
		return timeToWaitInSeconds;
	}

	public void setTimeToWait(Formula timeToWaitInSeconds) {
		this.timeToWaitInSeconds = timeToWaitInSeconds;
	}

	@Override
	public Brick copyBrickForSprite(Sprite sprite, Script script) {
		WaitBrick copyBrick = (WaitBrick) clone();
		copyBrick.sprite = sprite;
		return copyBrick;
	}

	@Override
	public Brick clone() {
		return new WaitBrick(getSprite(), timeToWaitInSeconds.clone());
	}

	@Override
	public List<SequenceAction> addActionToSequence(SequenceAction sequence) {
		sequence.addAction(ExtendedActions.delay(sprite, timeToWaitInSeconds));
		return null;
	}
}
