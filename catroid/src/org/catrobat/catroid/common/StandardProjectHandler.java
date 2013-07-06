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
package org.catrobat.catroid.common;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Random;

import org.catrobat.catroid.ProjectManager;
import org.catrobat.catroid.R;
import org.catrobat.catroid.content.BroadcastScript;
import org.catrobat.catroid.content.PhysicSprite;
import org.catrobat.catroid.content.Project;
import org.catrobat.catroid.content.Script;
import org.catrobat.catroid.content.Sprite;
import org.catrobat.catroid.content.StartScript;
import org.catrobat.catroid.content.WhenScript;
import org.catrobat.catroid.content.bricks.BroadcastBrick;
import org.catrobat.catroid.content.bricks.ChangeSizeByNBrick;
import org.catrobat.catroid.content.bricks.ForeverBrick;
import org.catrobat.catroid.content.bricks.GlideToBrick;
import org.catrobat.catroid.content.bricks.HideBrick;
import org.catrobat.catroid.content.bricks.LoopEndlessBrick;
import org.catrobat.catroid.content.bricks.PlaceAtBrick;
import org.catrobat.catroid.content.bricks.PlaySoundBrick;
import org.catrobat.catroid.content.bricks.PointInDirectionBrick;
import org.catrobat.catroid.content.bricks.PointInDirectionBrick.Direction;
import org.catrobat.catroid.content.bricks.SetLookBrick;
import org.catrobat.catroid.content.bricks.SetSizeToBrick;
import org.catrobat.catroid.content.bricks.SetVariableBrick;
import org.catrobat.catroid.content.bricks.ShowBrick;
import org.catrobat.catroid.content.bricks.TurnLeftBrick;
import org.catrobat.catroid.content.bricks.WaitBrick;
import org.catrobat.catroid.content.bricks.physics.SetBounceFactorBrick;
import org.catrobat.catroid.content.bricks.physics.SetFrictionBrick;
import org.catrobat.catroid.content.bricks.physics.SetGravityBrick;
import org.catrobat.catroid.content.bricks.physics.SetPhysicObjectTypeBrick;
import org.catrobat.catroid.content.bricks.physics.SetVelocityBrick;
import org.catrobat.catroid.content.bricks.physics.TurnLeftSpeedBrick;
import org.catrobat.catroid.formulaeditor.Formula;
import org.catrobat.catroid.formulaeditor.FormulaElement;
import org.catrobat.catroid.formulaeditor.FormulaElement.ElementType;
import org.catrobat.catroid.formulaeditor.Functions;
import org.catrobat.catroid.formulaeditor.UserVariable;
import org.catrobat.catroid.formulaeditor.UserVariablesContainer;
import org.catrobat.catroid.io.StorageHandler;
import org.catrobat.catroid.physics.PhysicObject;
import org.catrobat.catroid.physics.PhysicWorld;
import org.catrobat.catroid.stage.StageListener;
import org.catrobat.catroid.utils.Utils;

import android.content.Context;

import com.badlogic.gdx.math.Vector2;

public class StandardProjectHandler {

	private static final String FILENAME_SEPARATOR = "_";

	public static Project createAndSaveStandardProject(Context context) throws IOException {
		String projectName = context.getString(R.string.default_project_name);
		return createAndSaveStandardProject(projectName, context);
	}

	public static Project createAndSaveStandardProjectMole(String projectName, Context context) throws IOException {
		String mole1Name = context.getString(R.string.default_project_sprites_mole_name) + " 1";
		String mole2Name = context.getString(R.string.default_project_sprites_mole_name) + " 2";
		String mole3Name = context.getString(R.string.default_project_sprites_mole_name) + " 3";
		String mole4Name = context.getString(R.string.default_project_sprites_mole_name) + " 4";
		String whackedMoleName = context.getString(R.string.default_project_sprites_mole_whacked);
		String soundName = context.getString(R.string.default_project_sprites_mole_sound);
		String backgroundName = context.getString(R.string.default_project_backgroundname);

		String varRandomFrom = context.getString(R.string.default_project_var_random_from);
		String varRandomTo = context.getString(R.string.default_project_var_random_to);

		Project defaultProject = new Project(context, projectName);
		defaultProject.setDeviceData(context); // density anywhere here
		StorageHandler.getInstance().saveProject(defaultProject);
		ProjectManager.getInstance().setProject(defaultProject);

		UserVariablesContainer userVariables = defaultProject.getUserVariables();

		Sprite backgroundSprite = defaultProject.getSpriteList().get(0);

		File mole1File = copyFromResourceInProject(projectName, Constants.IMAGE_DIRECTORY, mole1Name,
				R.drawable.default_project_mole_1, context);
		File mole2File = copyFromResourceInProject(projectName, Constants.IMAGE_DIRECTORY, mole2Name,
				R.drawable.default_project_mole_2, context);
		File whackedMoleFile = copyFromResourceInProject(projectName, Constants.IMAGE_DIRECTORY, whackedMoleName,
				R.drawable.default_project_mole_whacked, context);
		File soundFile1 = copyFromResourceInProject(projectName, Constants.SOUND_DIRECTORY, soundName,
				R.raw.default_project_sound_mole_1, context);
		File soundFile2 = copyFromResourceInProject(projectName, Constants.SOUND_DIRECTORY, soundName,
				R.raw.default_project_sound_mole_2, context);
		File soundFile3 = copyFromResourceInProject(projectName, Constants.SOUND_DIRECTORY, soundName,
				R.raw.default_project_sound_mole_3, context);
		File soundFile4 = copyFromResourceInProject(projectName, Constants.SOUND_DIRECTORY, soundName,
				R.raw.default_project_sound_mole_4, context);
		File backgroundFile = copyFromResourceInProject(projectName, Constants.IMAGE_DIRECTORY, backgroundName,
				R.drawable.default_project_background, context);

		copyFromResourceInProject(projectName, ".", StageListener.SCREENSHOT_AUTOMATIC_FILE_NAME,
				R.drawable.default_project_screenshot, context, false);

		LookData moleLookData1 = new LookData();
		moleLookData1.setLookName(mole1Name);
		moleLookData1.setLookFilename(mole1File.getName());

		LookData moleLookData2 = new LookData();
		moleLookData2.setLookName(mole2Name);
		moleLookData2.setLookFilename(mole2File.getName());

		LookData moleLookDataWhacked = new LookData();
		moleLookDataWhacked.setLookName(whackedMoleName);
		moleLookDataWhacked.setLookFilename(whackedMoleFile.getName());

		LookData backgroundLookData = new LookData();
		backgroundLookData.setLookName(backgroundName);
		backgroundLookData.setLookFilename(backgroundFile.getName());

		SoundInfo soundInfo = new SoundInfo();
		soundInfo.setTitle(soundName);
		soundInfo.setSoundFileName(soundFile1.getName());

		userVariables.addProjectUserVariable(varRandomFrom);
		UserVariable randomFrom = userVariables.getUserVariable(varRandomFrom, backgroundSprite);

		userVariables.addProjectUserVariable(varRandomTo);
		UserVariable randomTo = userVariables.getUserVariable(varRandomTo, backgroundSprite);

		// Background sprite
		backgroundSprite.getLookDataList().add(backgroundLookData);
		Script backgroundStartScript = new StartScript(backgroundSprite);

		SetLookBrick setLookBrick = new SetLookBrick(backgroundSprite);
		setLookBrick.setLook(backgroundLookData);
		backgroundStartScript.addBrick(setLookBrick);

		SetVariableBrick setVariableBrick = new SetVariableBrick(backgroundSprite, new Formula(1), randomFrom);
		backgroundStartScript.addBrick(setVariableBrick);

		setVariableBrick = new SetVariableBrick(backgroundSprite, new Formula(5), randomTo);
		backgroundStartScript.addBrick(setVariableBrick);

		backgroundSprite.addScript(backgroundStartScript);

		FormulaElement randomElement = new FormulaElement(ElementType.FUNCTION, Functions.RAND.toString(), null);
		randomElement.setLeftChild(new FormulaElement(ElementType.USER_VARIABLE, varRandomFrom, randomElement));
		randomElement.setRightChild(new FormulaElement(ElementType.USER_VARIABLE, varRandomTo, randomElement));
		Formula randomWait = new Formula(randomElement);

		FormulaElement waitOneOrTwoSeconds = new FormulaElement(ElementType.FUNCTION, Functions.RAND.toString(), null);
		waitOneOrTwoSeconds.setLeftChild(new FormulaElement(ElementType.NUMBER, "1", waitOneOrTwoSeconds));
		waitOneOrTwoSeconds.setRightChild(new FormulaElement(ElementType.NUMBER, "2", waitOneOrTwoSeconds));

		// Mole 1 sprite
		Sprite mole1Sprite = new Sprite(context.getString(R.string.default_project_sprites_mole_name) + " 1");
		mole1Sprite.getLookDataList().add(moleLookData1);
		mole1Sprite.getLookDataList().add(moleLookData2);
		mole1Sprite.getLookDataList().add(moleLookDataWhacked);
		mole1Sprite.getSoundList().add(soundInfo);

		Script mole1StartScript = new StartScript(mole1Sprite);
		Script mole1WhenScript = new WhenScript(mole1Sprite);

		// start script
		SetSizeToBrick setSizeToBrick = new SetSizeToBrick(mole1Sprite, new Formula(30));
		mole1StartScript.addBrick(setSizeToBrick);

		ForeverBrick foreverBrick = new ForeverBrick(mole1Sprite);
		mole1StartScript.addBrick(foreverBrick);

		PlaceAtBrick placeAtBrick = new PlaceAtBrick(mole1Sprite, -160, -110);
		mole1StartScript.addBrick(placeAtBrick);

		WaitBrick waitBrick = new WaitBrick(mole1Sprite, new Formula(waitOneOrTwoSeconds));
		mole1StartScript.addBrick(waitBrick);

		ShowBrick showBrick = new ShowBrick(mole1Sprite);
		mole1StartScript.addBrick(showBrick);

		setLookBrick = new SetLookBrick(mole1Sprite);
		setLookBrick.setLook(moleLookData1);
		mole1StartScript.addBrick(setLookBrick);

		GlideToBrick glideToBrick = new GlideToBrick(mole1Sprite, -160, -95, 100);
		mole1StartScript.addBrick(glideToBrick);

		setLookBrick = new SetLookBrick(mole1Sprite);
		setLookBrick.setLook(moleLookData2);
		mole1StartScript.addBrick(setLookBrick);

		waitBrick = new WaitBrick(mole1Sprite, randomWait.clone());
		mole1StartScript.addBrick(waitBrick);

		HideBrick hideBrick = new HideBrick(mole1Sprite);
		mole1StartScript.addBrick(hideBrick);

		waitBrick = new WaitBrick(mole1Sprite, randomWait.clone());
		mole1StartScript.addBrick(waitBrick);

		LoopEndlessBrick loopEndlessBrick = new LoopEndlessBrick(mole1Sprite, foreverBrick);
		mole1StartScript.addBrick(loopEndlessBrick);

		// when script		
		PlaySoundBrick playSoundBrick = new PlaySoundBrick(mole1Sprite);
		playSoundBrick.setSoundInfo(soundInfo);
		mole1WhenScript.addBrick(playSoundBrick);

		setLookBrick = new SetLookBrick(mole1Sprite);
		setLookBrick.setLook(moleLookDataWhacked);
		mole1WhenScript.addBrick(setLookBrick);

		waitBrick = new WaitBrick(mole1Sprite, 1500);
		mole1WhenScript.addBrick(waitBrick);

		hideBrick = new HideBrick(mole1Sprite);
		mole1WhenScript.addBrick(hideBrick);

		mole1Sprite.addScript(mole1StartScript);
		mole1Sprite.addScript(mole1WhenScript);
		defaultProject.addSprite(mole1Sprite);

		StorageHandler.getInstance().fillChecksumContainer();

		// Mole 2 sprite
		Sprite mole2Sprite = mole1Sprite.clone();
		mole2Sprite.getSoundList().get(0).setSoundFileName(soundFile2.getName());
		mole2Sprite.setName(mole2Name);
		defaultProject.addSprite(mole2Sprite);

		Script tempScript = mole2Sprite.getScript(0);
		placeAtBrick = (PlaceAtBrick) tempScript.getBrick(2);
		placeAtBrick.setXPosition(new Formula(160));
		placeAtBrick.setYPosition(new Formula(-110));

		glideToBrick = (GlideToBrick) tempScript.getBrick(6);
		glideToBrick.setXDestination(new Formula(160));
		glideToBrick.setYDestination(new Formula(-95));

		// Mole 3 sprite
		Sprite mole3Sprite = mole1Sprite.clone();
		mole3Sprite.getSoundList().get(0).setSoundFileName(soundFile3.getName());
		mole3Sprite.setName(mole3Name);
		defaultProject.addSprite(mole3Sprite);

		tempScript = mole3Sprite.getScript(0);
		placeAtBrick = (PlaceAtBrick) tempScript.getBrick(2);
		placeAtBrick.setXPosition(new Formula(-160));
		placeAtBrick.setYPosition(new Formula(-290));

		glideToBrick = (GlideToBrick) tempScript.getBrick(6);
		glideToBrick.setXDestination(new Formula(-160));
		glideToBrick.setYDestination(new Formula(-275));

		// Mole 4 sprite
		Sprite mole4Sprite = mole1Sprite.clone();
		mole4Sprite.getSoundList().get(0).setSoundFileName(soundFile4.getName());
		mole4Sprite.setName(mole4Name);
		defaultProject.addSprite(mole4Sprite);

		tempScript = mole4Sprite.getScript(0);
		placeAtBrick = (PlaceAtBrick) tempScript.getBrick(2);
		placeAtBrick.setXPosition(new Formula(160));
		placeAtBrick.setYPosition(new Formula(-290));

		glideToBrick = (GlideToBrick) tempScript.getBrick(6);
		glideToBrick.setXDestination(new Formula(160));
		glideToBrick.setYDestination(new Formula(-275));

		StorageHandler.getInstance().saveProject(defaultProject);

		return defaultProject;
	}

	// XXX: Only needed for pinball game and demonstration purposes. 
	private static String projectName;
	private static Context context;

	public static Project createAndSaveStandardProject(String projectName, Context context) throws IOException {
		StandardProjectHandler.context = context;
		StandardProjectHandler.projectName = projectName;

		Project defaultProject = new Project(context, projectName);
		defaultProject.getXmlHeader().virtualScreenWidth = 480;
		defaultProject.getXmlHeader().virtualScreenHeight = 800;
		StorageHandler.getInstance().saveProject(defaultProject);
		ProjectManager.getInstance().setProject(defaultProject);

		PhysicWorld physicWorld = defaultProject.getPhysicWorld();

		Sprite background = defaultProject.getSpriteList().get(0);

		Sprite ball = new PhysicSprite("Ball");

		Sprite leftButton = new Sprite("Left button");
		Sprite rightButton = new Sprite("Right button");

		Sprite leftArm = new PhysicSprite("Left arm");
		Sprite rightArm = new PhysicSprite("Right arm");

		Sprite[] upperBouncers = { new PhysicSprite("Middle cat bouncer"), new PhysicSprite("Right cat bouncer") };

		Sprite[] lowerBouncers = { new PhysicSprite("Left wool bouncer"), new PhysicSprite("Middle wool bouncer"),
				new PhysicSprite("Right wool bouncer") };

		Sprite middleBouncer = new PhysicSprite("Cat head bouncer");

		Sprite leftHardBouncer = new PhysicSprite("Left hard bouncer");
		Sprite leftHardBouncerBouncer = new PhysicSprite("Left hard bouncer bouncer");
		Sprite rightHardBouncer = new PhysicSprite("Right hard bouncer");
		Sprite rightHardBouncerBouncer = new PhysicSprite("Right hard bouncer bouncer");

		Sprite leftVerticalWall = new PhysicSprite("Left vertical wall");
		Sprite leftBottomWall = new PhysicSprite("Left bottom wall");
		Sprite rightVerticalWall = new PhysicSprite("Right vertical wall");
		Sprite rightBottomWall = new PhysicSprite("Right bottom wall");

		final String leftButtonPressed = "Left button pressed";
		final String rightButtonPressed = "Right button pressed";

		final float armMovingSpeed = 720.0f;
		float doodlydoo = 50.0f;

		// Background
		createElement(background, physicWorld, "background_480_800", R.drawable.background_480_800, new Vector2(),
				Float.NaN);
		StartScript startScript = new StartScript(ball);
		startScript.addBrick(new SetGravityBrick(ball, new Vector2(0.0f, -8.0f)));
		ball.addScript(startScript);

		// Ball
		Script ballStartScript = createElement(ball, physicWorld, "pinball", R.drawable.pinball, new Vector2(-200.0f,
				300.0f), Float.NaN);
		setPhysicProperties(ball, physicWorld, ballStartScript, PhysicObject.Type.DYNAMIC, 20.0f, 80.0f);

		// Ball v2
		String ballBroadcastMessage = "restart ball";
		BroadcastBrick ballBroadcastBrick = new BroadcastBrick(ball, ballBroadcastMessage);
		ballStartScript.addBrick(ballBroadcastBrick);
		ball.addScript(ballStartScript);

		BroadcastScript ballBroadcastScript = new BroadcastScript(ball, ballBroadcastMessage);
		ballBroadcastScript.addBrick(new PlaceAtBrick(ball, -200, 300));
		ballBroadcastScript.addBrick(new SetVelocityBrick(ball, new Vector2()));
		SetLookBrick ballSetLookBrick = new SetLookBrick(ball);
		ballSetLookBrick.setLook(ball.getLookDataList().get(0));
		ballBroadcastScript.addBrick(ballSetLookBrick);
		ball.addScript(ballBroadcastScript);

		// Buttons
		createElement(leftButton, physicWorld, "button", R.drawable.button, new Vector2(-175.0f, -330.0f), Float.NaN);
		createButtonPressed(leftButton, leftButtonPressed);
		createElement(rightButton, physicWorld, "button", R.drawable.button, new Vector2(175.0f, -330.0f), Float.NaN);
		createButtonPressed(rightButton, rightButtonPressed);

		// Arms
		Script leftArmStartScript = createElement(leftArm, physicWorld, "left_arm", R.drawable.left_arm, new Vector2(
				-80.0f, -315.0f), Float.NaN);
		setPhysicProperties(leftArm, physicWorld, leftArmStartScript, PhysicObject.Type.FIXED, 50.0f, -1.0f);
		createMovingArm(leftArm, leftButtonPressed, physicWorld, armMovingSpeed);
		Script rightArmStartScript = createElement(rightArm, physicWorld, "right_arm", R.drawable.right_arm,
				new Vector2(80.0f, -315.0f), Float.NaN);
		setPhysicProperties(rightArm, physicWorld, rightArmStartScript, PhysicObject.Type.FIXED, 50.0f, -1.0f);
		createMovingArm(rightArm, rightButtonPressed, physicWorld, -armMovingSpeed);

		// Lower walls
		Script leftVerticalWallStartScript = createElement(leftVerticalWall, physicWorld, "vertical_wall",
				R.drawable.vertical_wall, new Vector2(-232.0f, -160.0f), 8.0f);
		setPhysicProperties(leftVerticalWall, physicWorld, leftVerticalWallStartScript, PhysicObject.Type.FIXED, 5.0f,
				-1.0f);
		Script rightVerticalWallStartScript = createElement(rightVerticalWall, physicWorld, "vertical_wall",
				R.drawable.vertical_wall, new Vector2(232.0f, -160.0f), -8.0f);
		setPhysicProperties(rightVerticalWall, physicWorld, rightVerticalWallStartScript, PhysicObject.Type.FIXED,
				5.0f, -1.0f);

		Script leftBottomWallStartScript = createElement(leftBottomWall, physicWorld, "wall_bottom",
				R.drawable.wall_bottom, new Vector2(-155.0f, -255.0f), 58.5f);
		setPhysicProperties(leftBottomWall, physicWorld, leftBottomWallStartScript, PhysicObject.Type.FIXED, 5.0f,
				-1.0f);
		Script rightBottomWallStartScript = createElement(rightBottomWall, physicWorld, "wall_bottom",
				R.drawable.wall_bottom, new Vector2(155.0f, -255.0f), -58.5f);
		setPhysicProperties(rightBottomWall, physicWorld, rightBottomWallStartScript, PhysicObject.Type.FIXED, 5.0f,
				-1.0f);

		// Hard Bouncer
		Script leftHardBouncerStartScript = createElement(leftHardBouncer, physicWorld, "left_hard_bouncer",
				R.drawable.left_hard_bouncer, new Vector2(-140.0f, -165.0f), Float.NaN);
		setPhysicProperties(leftHardBouncer, physicWorld, leftHardBouncerStartScript, PhysicObject.Type.FIXED, 10.0f,
				-1.0f);
		Script leftHardBouncerBouncerStartScript = createElement(leftHardBouncerBouncer, physicWorld,
				"left_light_bouncer", R.drawable.left_light_bouncer, new Vector2(-129.0f, -163.0f), Float.NaN);
		setPhysicProperties(leftHardBouncerBouncer, physicWorld, leftHardBouncerBouncerStartScript,
				PhysicObject.Type.FIXED, 124.0f, -1.0f);

		Script rightHardBouncerStartScript = createElement(rightHardBouncer, physicWorld, "right_hard_bouncer",
				R.drawable.right_hard_bouncer, new Vector2(140.0f, -165.0f), Float.NaN);
		setPhysicProperties(rightHardBouncer, physicWorld, rightHardBouncerStartScript, PhysicObject.Type.FIXED, 10.0f,
				-1.0f);
		Script rightHardBouncerBouncerStartScript = createElement(rightHardBouncerBouncer, physicWorld,
				"right_light_bouncer", R.drawable.right_light_bouncer, new Vector2(129.0f, -163.0f), Float.NaN);
		setPhysicProperties(rightHardBouncerBouncer, physicWorld, rightHardBouncerBouncerStartScript,
				PhysicObject.Type.FIXED, 124.0f, -1.0f);

		// Lower wool bouncers
		Vector2[] lowerBouncersPositions = { new Vector2(-100.0f, -80.0f + doodlydoo),
				new Vector2(0.0f, -140.0f + doodlydoo), new Vector2(100.0f, -80.0f + doodlydoo) };
		for (int index = 0; index < lowerBouncers.length; index++) {
			Script lowerBouncerStartScript = createElement(lowerBouncers[index], physicWorld, "wolle_bouncer",
					R.drawable.wolle_bouncer, lowerBouncersPositions[index], new Random().nextInt(360));
			setPhysicProperties(lowerBouncers[index], physicWorld, lowerBouncerStartScript, PhysicObject.Type.FIXED,
					116.0f, -1.0f);
		}

		// Middle bouncer
		Script middleBouncerStartScript = createElement(middleBouncer, physicWorld, "lego", R.drawable.cat_bouncer,
				new Vector2(0.0f, 75.0f + doodlydoo), Float.NaN);
		setPhysicProperties(middleBouncer, physicWorld, middleBouncerStartScript, PhysicObject.Type.FIXED, 40.0f, 80.0f);
		middleBouncerStartScript.addBrick(new TurnLeftSpeedBrick(middleBouncer, 145));

		WhenScript whenPressedScript = new WhenScript(middleBouncer);
		whenPressedScript.setAction(0);

		BroadcastBrick bb = new BroadcastBrick(middleBouncer, ballBroadcastMessage);
		whenPressedScript.addBrick(bb);
		whenPressedScript.addBrick(new ChangeSizeByNBrick(middleBouncer, 20));
		middleBouncer.addScript(whenPressedScript);

		// Upper bouncers
		Vector2[] upperBouncersPositions = { new Vector2(0.0f, 240.f + doodlydoo),
				new Vector2(150.0f, 200.0f + doodlydoo) };
		for (int index = 0; index < upperBouncers.length; index++) {
			Script upperBouncersStartScript = createElement(upperBouncers[index], physicWorld, "cat_bouncer",
					R.drawable.cat_bouncer, upperBouncersPositions[index], Float.NaN);
			setPhysicProperties(upperBouncers[index], physicWorld, upperBouncersStartScript, PhysicObject.Type.FIXED,
					106.0f, -1.0f);
		}

		Sprite leftWall = new PhysicSprite("Left wall");
		Sprite rightWall = new PhysicSprite("Right wall");
		Sprite topWall = new PhysicSprite("Top wall");

		Script leftWallStartScript = createElement(leftWall, physicWorld, "wall", R.drawable.wall_bottom_left,
				new Vector2(-260, 110), 0);
		setPhysicProperties(leftWall, physicWorld, leftWallStartScript, PhysicObject.Type.FIXED, -1, -1);
		Script rightWallStartScript = createElement(rightWall, physicWorld, "wall", R.drawable.wall_bottom_rigth,
				new Vector2(260, 110), 0);
		setPhysicProperties(rightWall, physicWorld, rightWallStartScript, PhysicObject.Type.FIXED, -1, -1);

		Script topWallStartScript = createElement(topWall, physicWorld, "wall", R.drawable.wall_bottom, new Vector2(0,
				415), 90);
		setPhysicProperties(topWall, physicWorld, topWallStartScript, PhysicObject.Type.FIXED, -1, -1);

		defaultProject.addSprite(leftButton);
		defaultProject.addSprite(rightButton);
		defaultProject.addSprite(ball);
		defaultProject.addSprite(leftArm);
		defaultProject.addSprite(rightArm);
		defaultProject.addSprite(middleBouncer);
		defaultProject.addSprite(leftHardBouncerBouncer);
		defaultProject.addSprite(leftHardBouncer);
		defaultProject.addSprite(rightHardBouncerBouncer);
		defaultProject.addSprite(rightHardBouncer);
		defaultProject.addSprite(leftVerticalWall);
		defaultProject.addSprite(leftBottomWall);
		defaultProject.addSprite(rightVerticalWall);
		defaultProject.addSprite(rightBottomWall);
		defaultProject.addSprite(topWall);
		defaultProject.addSprite(leftWall);
		defaultProject.addSprite(rightWall);

		for (Sprite sprite : upperBouncers) {
			defaultProject.addSprite(sprite);
		}

		for (Sprite sprite : lowerBouncers) {
			defaultProject.addSprite(sprite);
		}

		return defaultProject;
	}

	private static Script createElement(Sprite sprite, PhysicWorld physicWorld, String fileName, int fileId,
			Vector2 position, float angle) throws IOException {
		File file = copyFromResourceInProject(projectName, Constants.IMAGE_DIRECTORY, fileName, fileId, context);
		LookData lookData = new LookData();
		lookData.setLookName(fileName);
		lookData.setLookFilename(file.getName());

		List<LookData> looks = sprite.getLookDataList();
		looks.add(lookData);

		SetLookBrick lookBrick = new SetLookBrick(sprite);
		lookBrick.setLook(lookData);

		Script startScript = new StartScript(sprite);
		startScript.addBrick(new PlaceAtBrick(sprite, (int) position.x, (int) position.y));
		startScript.addBrick(lookBrick);

		if (!Float.isNaN(angle)) {
			TurnLeftBrick turnLeftBrick = new TurnLeftBrick(sprite, angle);
			startScript.addBrick(turnLeftBrick);
		}

		sprite.addScript(startScript);
		return startScript;
	}

	private static Script setPhysicProperties(Sprite sprite, PhysicWorld physicWorld, Script startScript,
			PhysicObject.Type type, float bounce, float friction) {
		if (startScript == null) {
			startScript = new StartScript(sprite);
		}

		startScript.addBrick(new SetPhysicObjectTypeBrick(sprite, type));

		if (bounce >= 0.0f) {
			startScript.addBrick(new SetBounceFactorBrick(sprite, bounce));
		}

		if (friction >= 0.0f) {
			startScript.addBrick(new SetFrictionBrick(sprite, friction));
		}

		sprite.addScript(startScript);
		return startScript;
	}

	private static void createButtonPressed(Sprite sprite, String broadcastMessage) throws IOException {
		MessageContainer.addMessage(broadcastMessage);

		WhenScript whenPressedScript = new WhenScript(sprite);
		whenPressedScript.setAction(0);

		BroadcastBrick leftButtonBroadcastBrick = new BroadcastBrick(sprite, broadcastMessage);

		String filename = "button_pressed";
		File file = copyFromResourceInProject(projectName, Constants.IMAGE_DIRECTORY, filename,
				R.drawable.button_pressed, context);
		LookData lookData = new LookData();
		lookData.setLookName(filename);
		lookData.setLookFilename(file.getName());

		List<LookData> looks = sprite.getLookDataList();
		looks.add(lookData);

		SetLookBrick lookBrick = new SetLookBrick(sprite);
		lookBrick.setLook(lookData);

		WaitBrick waitBrick = new WaitBrick(sprite, 500);

		SetLookBrick lookBack = new SetLookBrick(sprite);
		lookBack.setLook(looks.get(0));

		whenPressedScript.addBrick(leftButtonBroadcastBrick);
		whenPressedScript.addBrick(lookBrick);
		whenPressedScript.addBrick(waitBrick);
		whenPressedScript.addBrick(lookBack);
		sprite.addScript(whenPressedScript);
	}

	private static void createMovingArm(Sprite sprite, String broadcastMessage, PhysicWorld physicWorld,
			float degreeSpeed) {
		BroadcastScript broadcastScript = new BroadcastScript(sprite, broadcastMessage);

		int waitInMillis = 110;

		broadcastScript.addBrick(new TurnLeftSpeedBrick(sprite, degreeSpeed));
		broadcastScript.addBrick(new WaitBrick(sprite, waitInMillis));

		broadcastScript.addBrick(new TurnLeftSpeedBrick(sprite, 0));
		broadcastScript.addBrick(new PointInDirectionBrick(sprite, Direction.DIRECTION_RIGHT));
		broadcastScript.addBrick(new WaitBrick(sprite, 25));
		broadcastScript.addBrick(new PointInDirectionBrick(sprite, Direction.DIRECTION_RIGHT));

		sprite.addScript(broadcastScript);
	}

	public static Project createAndSaveEmptyProject(String projectName, Context context) {
		Project emptyProject = new Project(context, projectName);
		emptyProject.setDeviceData(context);
		StorageHandler.getInstance().saveProject(emptyProject);
		ProjectManager.getInstance().setProject(emptyProject);

		return emptyProject;
	}

	private static File copyFromResourceInProject(String projectName, String directoryName, String outputName,
			int fileId, Context context) throws IOException {
		return copyFromResourceInProject(projectName, directoryName, outputName, fileId, context, true);
	}

	private static File copyFromResourceInProject(String projectName, String directoryName, String outputName,
			int fileId, Context context, boolean prependMd5) throws IOException {
		final String filePath = Utils.buildPath(Utils.buildProjectPath(projectName), directoryName, outputName);
		File copiedFile = new File(filePath);
		if (!copiedFile.exists()) {
			copiedFile.createNewFile();
		}
		InputStream in = context.getResources().openRawResource(fileId);
		OutputStream out = new BufferedOutputStream(new FileOutputStream(copiedFile), Constants.BUFFER_8K);
		byte[] buffer = new byte[Constants.BUFFER_8K];
		int length = 0;
		while ((length = in.read(buffer)) > 0) {
			out.write(buffer, 0, length);
		}

		in.close();
		out.flush();
		out.close();

		if (!prependMd5) {
			return copiedFile;
		}

		String directoryPath = Utils.buildPath(Utils.buildProjectPath(projectName), directoryName);
		String finalImageFileString = Utils.buildPath(directoryPath, Utils.md5Checksum(copiedFile) + FILENAME_SEPARATOR
				+ copiedFile.getName());
		File copiedFileWithMd5 = new File(finalImageFileString);
		copiedFile.renameTo(copiedFileWithMd5);

		return copiedFileWithMd5;
	}
}
