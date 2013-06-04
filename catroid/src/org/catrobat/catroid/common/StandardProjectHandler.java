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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
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
import org.catrobat.catroid.content.bricks.IfOnEdgeBounceBrick;
import org.catrobat.catroid.content.bricks.LoopEndBrick;
import org.catrobat.catroid.content.bricks.PlaceAtBrick;
import org.catrobat.catroid.content.bricks.PointInDirectionBrick;
import org.catrobat.catroid.content.bricks.PointInDirectionBrick.Direction;
import org.catrobat.catroid.content.bricks.SetLookBrick;
import org.catrobat.catroid.content.bricks.TurnLeftBrick;
import org.catrobat.catroid.content.bricks.WaitBrick;
import org.catrobat.catroid.content.bricks.physics.SetBounceFactorBrick;
import org.catrobat.catroid.content.bricks.physics.SetFrictionBrick;
import org.catrobat.catroid.content.bricks.physics.SetGravityBrick;
import org.catrobat.catroid.content.bricks.physics.SetPhysicObjectTypeBrick;
import org.catrobat.catroid.content.bricks.physics.SetVelocityBrick;
import org.catrobat.catroid.content.bricks.physics.TurnLeftSpeedBrick;
import org.catrobat.catroid.io.StorageHandler;
import org.catrobat.catroid.physics.PhysicObject;
import org.catrobat.catroid.physics.PhysicWorld;
import org.catrobat.catroid.utils.ImageEditing;
import org.catrobat.catroid.utils.Utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;

import com.badlogic.gdx.math.Vector2;

public class StandardProjectHandler {

	private static final String FILENAME_SEPARATOR = "_";

	public static Project createAndSaveStandardProject(Context context) throws IOException {
		String projectName = context.getString(R.string.default_project_name);
		return createAndSaveStandardProject(projectName, context);
	}

	private static Context context;
	private static String projectName;

	public static Project createAndSaveStandardProject(String projectName, Context context) throws IOException {
		Project defaultProject = new Project(context, projectName);
		defaultProject.getXmlHeader().virtualScreenWidth = 480;
		defaultProject.getXmlHeader().virtualScreenHeight = 800;
		StorageHandler.getInstance().saveProject(defaultProject);
		ProjectManager.getInstance().setProject(defaultProject);

		StandardProjectHandler.context = context;
		StandardProjectHandler.projectName = defaultProject.getName();
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
		BroadcastBrick ballBroadcastBrick = new BroadcastBrick(ball);
		ballBroadcastBrick.setSelectedMessage(ballBroadcastMessage);
		ballStartScript.addBrick(ballBroadcastBrick);
		ball.addScript(ballStartScript);

		BroadcastScript ballBroadcastScript = new BroadcastScript(ball);
		ballBroadcastScript.setBroadcastMessage(ballBroadcastMessage);
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

		BroadcastBrick bb = new BroadcastBrick(middleBouncer);
		bb.setSelectedMessage(ballBroadcastMessage);
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
		File file = savePictureFromResourceInProject(projectName, fileName, fileId, context);
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

		BroadcastBrick leftButtonBroadcastBrick = new BroadcastBrick(sprite);
		leftButtonBroadcastBrick.setSelectedMessage(broadcastMessage);

		String filename = "button_pressed";
		File file = savePictureFromResourceInProject(projectName, filename, R.drawable.button_pressed, context);
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
		BroadcastScript broadcastScript = new BroadcastScript(sprite);
		broadcastScript.setBroadcastMessage(broadcastMessage);

		int waitInMillis = 110;

		broadcastScript.addBrick(new TurnLeftSpeedBrick(sprite, degreeSpeed));
		broadcastScript.addBrick(new WaitBrick(sprite, waitInMillis));

		broadcastScript.addBrick(new TurnLeftSpeedBrick(sprite, 0));
		broadcastScript.addBrick(new PointInDirectionBrick(sprite, Direction.DIRECTION_RIGHT));
		broadcastScript.addBrick(new WaitBrick(sprite, 25));
		broadcastScript.addBrick(new PointInDirectionBrick(sprite, Direction.DIRECTION_RIGHT));

		sprite.addScript(broadcastScript);
	}

	public static Project createAndSaveStandardProjectDefault(String projectName, Context context) throws IOException {
		String normalCatName = context.getString(R.string.default_project_sprites_pocketcode_normalcat);
		String banzaiCatName = context.getString(R.string.default_project_sprites_pocketcode_banzaicat);
		String cheshireCatName = context.getString(R.string.default_project_sprites_pocketcode_cheshirecat);
		String backgroundName = context.getString(R.string.default_project_backgroundname);

		Project defaultProject = new Project(context, projectName);
		StorageHandler.getInstance().saveProject(defaultProject);
		ProjectManager.getInstance().setProject(defaultProject);
		// XXX: Sprite sprite = new PhysicSprite(context.getString(R.string.default_project_sprites_pocketcode_name));
		Sprite sprite = new PhysicSprite(context.getString(R.string.default_project_sprites_pocketcode_name));
		Sprite backgroundSprite = defaultProject.getSpriteList().get(0);

		Script backgroundStartScript = new StartScript(backgroundSprite);
		Script startScript = new StartScript(sprite);
		Script whenScript = new WhenScript(sprite);

		File backgroundFile = createBackgroundImage(projectName, backgroundName,
				context.getString(R.string.default_project_backgroundcolor));

		File normalCat = copyAndScaleImageToProject(projectName, context, normalCatName, R.drawable.catroid);
		File banzaiCat = copyAndScaleImageToProject(projectName, context, banzaiCatName, R.drawable.catroid_banzai);
		File cheshireCat = copyAndScaleImageToProject(projectName, context, cheshireCatName,
				R.drawable.catroid_cheshire);

		LookData normalCatLookData = new LookData();
		normalCatLookData.setLookName(normalCatName);
		normalCatLookData.setLookFilename(normalCat.getName());

		LookData banzaiCatLookData = new LookData();
		banzaiCatLookData.setLookName(banzaiCatName);
		banzaiCatLookData.setLookFilename(banzaiCat.getName());

		LookData cheshireCatLookData = new LookData();
		cheshireCatLookData.setLookName(cheshireCatName);
		cheshireCatLookData.setLookFilename(cheshireCat.getName());

		LookData backgroundLookData = new LookData();
		backgroundLookData.setLookName(backgroundName);
		backgroundLookData.setLookFilename(backgroundFile.getName());

		ArrayList<LookData> lookDataList = sprite.getLookDataList();
		lookDataList.add(normalCatLookData);
		lookDataList.add(banzaiCatLookData);
		lookDataList.add(cheshireCatLookData);
		ArrayList<LookData> lookDataList2 = backgroundSprite.getLookDataList();
		lookDataList2.add(backgroundLookData);

		SetLookBrick setLookBrick = new SetLookBrick(sprite);
		setLookBrick.setLook(normalCatLookData);

		SetLookBrick setLookBrick1 = new SetLookBrick(sprite);
		setLookBrick1.setLook(normalCatLookData);

		SetLookBrick setLookBrick2 = new SetLookBrick(sprite);
		setLookBrick2.setLook(banzaiCatLookData);

		SetLookBrick setLookBrick3 = new SetLookBrick(sprite);
		setLookBrick3.setLook(cheshireCatLookData);

		SetLookBrick backgroundBrick = new SetLookBrick(backgroundSprite);
		backgroundBrick.setLook(backgroundLookData);

		WaitBrick waitBrick1 = new WaitBrick(sprite, 500);
		WaitBrick waitBrick2 = new WaitBrick(sprite, 500);

		startScript.addBrick(setLookBrick);
		// XXX: BEGIN
		startScript.addBrick(new SetPhysicObjectTypeBrick(sprite, PhysicObject.Type.DYNAMIC));
		startScript.addBrick(new SetBounceFactorBrick(sprite, 90.0f));

		ForeverBrick foreverBrick = new ForeverBrick(sprite);
		LoopEndBrick loopEndBrick = new LoopEndBrick(sprite, foreverBrick);
		IfOnEdgeBounceBrick ifOnEdgeBounceBrick = new IfOnEdgeBounceBrick(sprite);

		startScript.addBrick(foreverBrick);
		startScript.addBrick(ifOnEdgeBounceBrick);
		startScript.addBrick(loopEndBrick);
		// XXX: END

		whenScript.addBrick(setLookBrick2);
		whenScript.addBrick(waitBrick1);
		whenScript.addBrick(setLookBrick3);
		whenScript.addBrick(waitBrick2);
		whenScript.addBrick(setLookBrick1);
		backgroundStartScript.addBrick(backgroundBrick);

		defaultProject.addSprite(sprite);
		sprite.addScript(startScript);
		// XXX: sprite.addScript(whenScript);
		backgroundSprite.addScript(backgroundStartScript);

		StorageHandler.getInstance().saveProject(defaultProject);

		return defaultProject;
	}

	private static File createBackgroundImage(String projectName, String backgroundName, String backgroundColor)
			throws FileNotFoundException {
		String directoryName = Utils.buildPath(Utils.buildProjectPath(projectName), Constants.IMAGE_DIRECTORY);
		File backgroundTemp = new File(Utils.buildPath(directoryName, backgroundName));
		Bitmap backgroundBitmap = ImageEditing.createSingleColorBitmap(Values.SCREEN_WIDTH, Values.SCREEN_HEIGHT,
				Color.parseColor(backgroundColor));
		StorageHandler.saveBitmapToImageFile(backgroundTemp, backgroundBitmap);
		File backgroundFile = new File(directoryName, Utils.md5Checksum(backgroundTemp) + FILENAME_SEPARATOR
				+ backgroundTemp.getName());
		backgroundTemp.renameTo(backgroundFile);
		return backgroundFile;
	}

	private static File copyAndScaleImageToProject(String projectName, Context context, String imageName, int imageId)
			throws IOException {
		String directoryName = Utils.buildPath(Utils.buildProjectPath(projectName), Constants.IMAGE_DIRECTORY);
		File tempImageFile = savePictureFromResourceInProject(projectName, imageName, imageId, context);

		int[] dimensions = ImageEditing.getImageDimensions(tempImageFile.getAbsolutePath());
		int originalWidth = dimensions[0];
		int originalHeight = dimensions[1];
		double ratio = (double) originalHeight / (double) originalWidth;

		// scale the cat, that its always 1/3 of the screen width
		Bitmap tempBitmap = ImageEditing.getScaledBitmapFromPath(tempImageFile.getAbsolutePath(),
				Values.SCREEN_WIDTH / 3, (int) (Values.SCREEN_WIDTH / 3 * ratio), false);
		StorageHandler.saveBitmapToImageFile(tempImageFile, tempBitmap);

		String finalImageFileString = Utils.buildPath(directoryName, Utils.md5Checksum(tempImageFile)
				+ FILENAME_SEPARATOR + tempImageFile.getName());
		File finalImageFile = new File(finalImageFileString);
		tempImageFile.renameTo(finalImageFile);

		return finalImageFile;
	}

	private static File savePictureFromResourceInProject(String project, String outputName, int fileId, Context context)
			throws IOException {

		final String imagePath = Utils
				.buildPath(Utils.buildProjectPath(project), Constants.IMAGE_DIRECTORY, outputName);
		File testImage = new File(imagePath);
		if (!testImage.exists()) {
			testImage.createNewFile();
		}
		InputStream in = context.getResources().openRawResource(fileId);
		OutputStream out = new BufferedOutputStream(new FileOutputStream(testImage), Constants.BUFFER_8K);
		byte[] buffer = new byte[Constants.BUFFER_8K];
		int length = 0;
		while ((length = in.read(buffer)) > 0) {
			out.write(buffer, 0, length);
		}

		in.close();
		out.flush();
		out.close();

		return testImage;
	}

}
