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

import java.util.List;

public class NoteBrick extends BrickBaseType {
	private static final long serialVersionUID = 1L;

	private String note = "";

	public NoteBrick(Sprite sprite) {
		this.sprite = sprite;
	}

	public NoteBrick() {

	}

	public NoteBrick(Sprite sprite, String note) {
		this.sprite = sprite;
		this.note = note;
	}

	@Override
	public Brick copyBrickForSprite(Sprite sprite, Script script) {
		NoteBrick copyBrick = (NoteBrick) clone();
		copyBrick.sprite = sprite;
		return copyBrick;
	}

	public String getNote() {
		return this.note;
	}

	@Override
	public Brick clone() {
		return new NoteBrick(this.sprite, this.note);
	}

	@Override
	public List<SequenceAction> addActionToSequence(SequenceAction sequence) {
		return null;
	}
}
