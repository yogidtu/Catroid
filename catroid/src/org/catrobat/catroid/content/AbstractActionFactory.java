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

import org.catrobat.catroid.common.LookData;
import org.catrobat.catroid.common.SoundInfo;
import org.catrobat.catroid.content.actions.BroadcastAction;
import org.catrobat.catroid.content.actions.BroadcastNotifyAction;
import org.catrobat.catroid.content.actions.ChangeBrightnessByNAction;
import org.catrobat.catroid.content.actions.ChangeGhostEffectByNAction;
import org.catrobat.catroid.content.actions.ChangeSizeByNAction;
import org.catrobat.catroid.content.actions.ChangeVolumeByNAction;
import org.catrobat.catroid.content.actions.ChangeXByNAction;
import org.catrobat.catroid.content.actions.ChangeYByNAction;
import org.catrobat.catroid.content.actions.ClearGraphicEffectAction;
import org.catrobat.catroid.content.actions.ComeToFrontAction;
import org.catrobat.catroid.content.actions.GlideToAction;
import org.catrobat.catroid.content.actions.GoNStepsBackAction;
import org.catrobat.catroid.content.actions.HideAction;
import org.catrobat.catroid.content.actions.IfLogicAction;
import org.catrobat.catroid.content.actions.IfOnEdgeBounceAction;
import org.catrobat.catroid.content.actions.LegoNxtMotorActionAction;
import org.catrobat.catroid.content.actions.LegoNxtMotorStopAction;
import org.catrobat.catroid.content.actions.LegoNxtMotorTurnAngleAction;
import org.catrobat.catroid.content.actions.LegoNxtPlayToneAction;
import org.catrobat.catroid.content.actions.MoveNStepsAction;
import org.catrobat.catroid.content.actions.NextLookAction;
import org.catrobat.catroid.content.actions.PlaySoundAction;
import org.catrobat.catroid.content.actions.PointInDirectionAction;
import org.catrobat.catroid.content.actions.PointToAction;
import org.catrobat.catroid.content.actions.RepeatAction;
import org.catrobat.catroid.content.actions.SetBrightnessAction;
import org.catrobat.catroid.content.actions.SetGhostEffectAction;
import org.catrobat.catroid.content.actions.SetLookAction;
import org.catrobat.catroid.content.actions.SetSizeToAction;
import org.catrobat.catroid.content.actions.SetVolumeToAction;
import org.catrobat.catroid.content.actions.SetXAction;
import org.catrobat.catroid.content.actions.SetYAction;
import org.catrobat.catroid.content.actions.ShowAction;
import org.catrobat.catroid.content.actions.SpeakAction;
import org.catrobat.catroid.content.actions.StopAllSoundsAction;
import org.catrobat.catroid.content.actions.TurnLeftAction;
import org.catrobat.catroid.content.actions.TurnRightAction;
import org.catrobat.catroid.content.actions.WaitAction;
import org.catrobat.catroid.content.bricks.SpeakBrick;
import org.catrobat.catroid.formulaeditor.Formula;
import org.catrobat.catroid.formulaeditor.UserVariable;
import org.catrobat.catroid.physics.PhysicObject;
import org.catrobat.catroid.physics.PhysicObject.Type;
import org.catrobat.catroid.physics.PhysicWorld;

import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.actions.SequenceAction;

public abstract class AbstractActionFactory {

	public abstract BroadcastAction createBroadcastAction(Sprite sprite, String broadcastMessage);

	public abstract BroadcastAction createBroadcastActionFromWaiter(Sprite sprite, String broadcastMessage);

	public abstract BroadcastNotifyAction createBroadcastNotifyAction(BroadcastEvent event);

	public abstract ChangeBrightnessByNAction createChangeBrightnessByNAction(Sprite sprite, Formula changeBrightness);

	public abstract ChangeGhostEffectByNAction createChangeGhostEffectByNAction(Sprite sprite, Formula ghostEffect);

	public abstract ChangeSizeByNAction createChangeSizeByNAction(Sprite sprite, Formula size);

	public abstract ChangeVolumeByNAction createChangeVolumeByNAction(Sprite sprite, Formula volume);

	public abstract ChangeXByNAction createChangeXByNAction(Sprite sprite, Formula xMovement);

	public abstract ChangeYByNAction createChangeYByNAction(Sprite sprite, Formula yMovement);

	public abstract ClearGraphicEffectAction createClearGraphicEffect(Sprite sprite);

	public abstract ComeToFrontAction createComeToFrontAction(Sprite sprite);

	public abstract GlideToAction createGlideToAction(Sprite sprite, Formula x, Formula y, Formula duration);

	public abstract GlideToAction createGlideToAction(Sprite sprite, Formula x, Formula y, Formula duration,
			Interpolation interpolation);

	public abstract GlideToAction createPlaceAtAction(Sprite sprite, Formula x, Formula y);

	public abstract GoNStepsBackAction createGoNStepsBackAction(Sprite sprite, Formula steps);

	public abstract HideAction createHideAction(Sprite sprite);

	public abstract IfOnEdgeBounceAction createIfOnEdgeBounceAction(Sprite sprite);

	public abstract LegoNxtMotorActionAction createLegoNxtMotorActionAction(Sprite sprite, String motor,
			org.catrobat.catroid.content.bricks.LegoNxtMotorActionBrick.Motor motorEnum, Formula speed);

	public abstract LegoNxtMotorStopAction createLegoNxtMotorStopAction(
			org.catrobat.catroid.content.bricks.LegoNxtMotorStopBrick.Motor motorEnum);

	public abstract LegoNxtMotorTurnAngleAction createLegoNxtMotorTurnAngleAction(Sprite sprite,
			org.catrobat.catroid.content.bricks.LegoNxtMotorTurnAngleBrick.Motor motorEnum, Formula degrees);

	public abstract LegoNxtPlayToneAction createLegoNxtPlayToneAction(Sprite sprite, Formula hertz,
			Formula durationInSeconds);

	public abstract MoveNStepsAction createMoveNStepsAction(Sprite sprite, Formula steps);

	public abstract NextLookAction createNextLookAction(Sprite sprite);

	public abstract PlaySoundAction createPlaySoundAction(Sprite sprite, SoundInfo sound);

	public abstract PointInDirectionAction createPointInDirectionAction(Sprite sprite, Formula degrees);

	public abstract PointToAction createPointToAction(Sprite sprite, Sprite pointedSprite);

	public abstract SetBrightnessAction createSetBrightnessAction(Sprite sprite, Formula brightness);

	public abstract SetGhostEffectAction createSetGhostEffectAction(Sprite sprite, Formula transparency);

	public abstract SetLookAction createSetLookAction(Sprite sprite, LookData lookData);

	public abstract SetSizeToAction createSetSizeToAction(Sprite sprite, Formula size);

	public abstract SetVolumeToAction createSetVolumeToAction(Sprite sprite, Formula volume);

	public abstract SetXAction createSetXAction(Sprite sprite, Formula x);

	public abstract SetYAction createSetYAction(Sprite sprite, Formula y);

	public abstract ShowAction createShowAction(Sprite sprite);

	public abstract SpeakAction createSpeakAction(String text, SpeakBrick speakBrick);

	public abstract StopAllSoundsAction createStopAllSoundsAction();

	public abstract TurnLeftAction createTurnLeftAction(Sprite sprite, Formula degrees);

	public abstract TurnRightAction createTurnRightAction(Sprite sprite, Formula degrees);

	public abstract Action createChangeVariableAction(Sprite sprite, Formula variableFormula, UserVariable userVariable);

	public abstract Action createSetVariableAction(Sprite sprite, Formula variableFormula, UserVariable userVariable);

	public abstract IfLogicAction createIfLogcAction(Sprite sprite, Formula condition, Action ifAction,
			Action elseAction);

	public abstract RepeatAction createRepeatAction(Sprite sprite, Formula count, Action repeatedAction);

	public abstract WaitAction createDelayAction(Sprite sprite, Formula delay);

	public abstract Action createForeverAction(Sprite sprite, SequenceAction foreverSequence);

	public Action createSetBounceFactorAction(Sprite sprite, PhysicObject physicObject, Formula bounceFactor) {
		throw new RuntimeException("Not a physics Sprite!");
	}

	public Action createSetFrictionAction(Sprite sprite, PhysicObject physicObject, Formula friction) {
		throw new RuntimeException("Not a physics Sprite!");
	}

	public Action createSetGravityAction(Sprite sprite, PhysicWorld physicWorld, Formula gravityX, Formula gravityY) {
		throw new RuntimeException("Not a physics Sprite!");
	}

	public Action createSetMassAction(Sprite sprite, PhysicObject physicObject, Formula mass) {
		throw new RuntimeException("Not a physics Sprite!");
	}

	public Action createSetPhysicObjectTypeAction(Sprite sprite, PhysicObject physicObject, Type type) {
		throw new RuntimeException("Not a physics Sprite!");
	}

	public Action createSetVelocityAction(Sprite sprite, PhysicObject physicObject, Formula velocityX, Formula velocityY) {
		throw new RuntimeException("Not a physics Sprite!");
	}

	public Action createTurnLeftSpeedAction(Sprite sprite, PhysicObject physicObject, Formula speed) {
		throw new RuntimeException("Not a physics Sprite!");
	}

	public Action createTurnRightSpeedAction(Sprite sprite, PhysicObject physicObject, Formula speed) {
		throw new RuntimeException("Not a physics Sprite!");
	}

}
