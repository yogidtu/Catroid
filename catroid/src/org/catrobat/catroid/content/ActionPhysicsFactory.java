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
package org.catrobat.catroid.content;

import org.catrobat.catroid.content.actions.physics.SetBounceFactorAction;
import org.catrobat.catroid.content.actions.physics.SetFrictionAction;
import org.catrobat.catroid.content.actions.physics.SetGravityAction;
import org.catrobat.catroid.content.actions.physics.SetMassAction;
import org.catrobat.catroid.content.actions.physics.SetPhysicObjectTypeAction;
import org.catrobat.catroid.content.actions.physics.SetVelocityAction;
import org.catrobat.catroid.content.actions.physics.TurnLeftSpeedAction;
import org.catrobat.catroid.content.actions.physics.TurnRightSpeedAction;
import org.catrobat.catroid.formulaeditor.Formula;
import org.catrobat.catroid.physics.PhysicObject;
import org.catrobat.catroid.physics.PhysicObject.Type;
import org.catrobat.catroid.physics.PhysicWorld;

import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;

public class ActionPhysicsFactory extends ActionFactory {

	public Action createSetBounceFactorAction(Sprite sprite, PhysicObject physicObject, Formula bounceFactor) {
		SetBounceFactorAction action = Actions.action(SetBounceFactorAction.class);
		action.setSprite(sprite);
		action.setPhysicObject(physicObject);
		action.setBounceFactor(bounceFactor);
		return action;
	}

	public Action createSetFrictionAction(Sprite sprite, PhysicObject physicObject, Formula friction) {
		SetFrictionAction action = Actions.action(SetFrictionAction.class);
		action.setSprite(sprite);
		action.setPhysicObject(physicObject);
		action.setFriction(friction);
		return action;
	}

	public Action createSetGravityAction(Sprite sprite, PhysicWorld physicWorld, Formula gravityX, Formula gravityY) {
		SetGravityAction action = Actions.action(SetGravityAction.class);
		action.setSprite(sprite);
		action.setPhysicWorld(physicWorld);
		action.setGravity(gravityX, gravityY);
		return action;
	}

	public Action createSetMassAction(Sprite sprite, PhysicObject physicObject, Formula mass) {
		SetMassAction action = Actions.action(SetMassAction.class);
		action.setSprite(sprite);
		action.setPhysicObject(physicObject);
		action.setMass(mass);
		return action;
	}

	public Action createSetPhysicObjectTypeAction(Sprite sprite, PhysicObject physicObject, Type type) {
		SetPhysicObjectTypeAction action = Actions.action(SetPhysicObjectTypeAction.class);
		action.setPhysicObject(physicObject);
		action.setType(type);
		return action;
	}

	public Action createSetVelocityAction(Sprite sprite, PhysicObject physicObject, Formula velocityX, Formula velocityY) {
		SetVelocityAction action = Actions.action(SetVelocityAction.class);
		action.setSprite(sprite);
		action.setPhysicObject(physicObject);
		action.setVelocity(velocityX, velocityY);
		return action;
	}

	public Action createTurnLeftSpeedAction(Sprite sprite, PhysicObject physicObject, Formula speed) {
		TurnLeftSpeedAction action = Actions.action(TurnLeftSpeedAction.class);
		action.setSprite(sprite);
		action.setPhysicObject(physicObject);
		action.setSpeed(speed);
		return action;
	}

	public Action createTurnRightSpeedAction(Sprite sprite, PhysicObject physicObject, Formula speed) {
		TurnRightSpeedAction action = Actions.action(TurnRightSpeedAction.class);
		action.setSprite(sprite);
		action.setPhysicObject(physicObject);
		action.setSpeed(speed);
		return action;
	}
}
