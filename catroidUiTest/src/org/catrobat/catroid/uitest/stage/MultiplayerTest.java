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
package org.catrobat.catroid.uitest.stage;

import android.bluetooth.BluetoothAdapter;
import android.util.Log;
import android.widget.ListView;

import org.catrobat.catroid.ProjectManager;
import org.catrobat.catroid.R;
import org.catrobat.catroid.common.LookData;
import org.catrobat.catroid.common.ScreenValues;
import org.catrobat.catroid.content.Project;
import org.catrobat.catroid.content.Script;
import org.catrobat.catroid.content.Sprite;
import org.catrobat.catroid.content.StartScript;
import org.catrobat.catroid.content.WhenScript;
import org.catrobat.catroid.content.bricks.SetLookBrick;
import org.catrobat.catroid.content.bricks.SetVariableBrick;
import org.catrobat.catroid.content.bricks.SetXBrick;
import org.catrobat.catroid.formulaeditor.Formula;
import org.catrobat.catroid.formulaeditor.FormulaElement;
import org.catrobat.catroid.formulaeditor.UserVariablesContainer;
import org.catrobat.catroid.ui.MainMenuActivity;
import org.catrobat.catroid.uitest.annotation.Device;
import org.catrobat.catroid.uitest.util.BTDummyClient;
import org.catrobat.catroid.uitest.util.BaseActivityInstrumentationTestCase;
import org.catrobat.catroid.uitest.util.UiTestUtils;

import java.io.File;

public class MultiplayerTest extends BaseActivityInstrumentationTestCase<MainMenuActivity> {
	private static final int IMAGE_FILE_ID = org.catrobat.catroid.uitest.R.raw.icon;
	private static final String PAIRED_BLUETOOTH_SERVER_DEVICE_NAME = "kitty";
	private static final String SPRITE_CAT = "cat";
	private SetXBrick setXBrick;

	public MultiplayerTest() {
		super(MainMenuActivity.class);
	}

	@Override
	public void setUp() throws Exception {
		super.setUp();
		createProject();
		UiTestUtils.prepareStageForTest();
	}

	@Device
	public void BluetoothConnection() {
		Log.d("Multiplayer", "STring = " + BTDummyClient.MULTIPLAYERSETASSERVER);
		//		BTDummyClient.getInstance().initializeAndConnectToServer(BTDummyClient.MULTIPLAYERSETASSERVER);

		BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
		assertTrue("Bluetooth not supported on device", btAdapter != null);
		if (!btAdapter.isEnabled()) {
			btAdapter.enable();
			solo.sleep(5000);
		}

		solo.clickOnText(solo.getString(R.string.main_menu_continue));
		UiTestUtils.clickOnBottomBar(solo, R.id.button_play);
		solo.sleep(3000);

		ListView deviceList = solo.getCurrentViews(ListView.class).get(0);
		String connectedDeviceName = null;
		for (int i = 0; i < deviceList.getCount(); i++) {
			String deviceName = (String) deviceList.getItemAtPosition(i);
			if (deviceName.startsWith(PAIRED_BLUETOOTH_SERVER_DEVICE_NAME + "2")) {
				connectedDeviceName = deviceName;
				break;
			}
		}
		solo.sleep(500);

		solo.clickOnText(connectedDeviceName);
		solo.sleep(5000);

		solo.goBack();
		solo.goBack();

		//		solo.clickOnText(SPRITE_CAT);
		//		solo.clickOnText(solo.getString(R.string.scripts));
		//		solo.sleep(2000);
		//		solo.goBack();
		//		solo.goBack();
		//		solo.goBack();
		//		solo.sleep(2000);

	}

	@Device
	public void testBluetoothSetVariable() {
		Log.d("Multiplayer", "STring = " + BTDummyClient.MULTIPLAYERSETASSERVER);
		BTDummyClient instance = new BTDummyClient();
		instance.initializeAndConnectToServer(BTDummyClient.MULTIPLAYERSETASSERVER);

		BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
		assertTrue("Bluetooth not supported on device", btAdapter != null);
		if (!btAdapter.isEnabled()) {
			btAdapter.enable();
			solo.sleep(5000);
		}

		solo.clickOnText(solo.getString(R.string.main_menu_continue));
		UiTestUtils.clickOnBottomBar(solo, R.id.button_play);
		solo.sleep(1000);

		ListView deviceList = solo.getCurrentViews(ListView.class).get(0);
		String connectedDeviceName = null;
		for (int i = 0; i < deviceList.getCount(); i++) {
			String deviceName = (String) deviceList.getItemAtPosition(i);
			if (deviceName.startsWith(PAIRED_BLUETOOTH_SERVER_DEVICE_NAME + "2")) {
				connectedDeviceName = deviceName;
				break;
			}
		}
		solo.sleep(500);
		solo.clickOnText(connectedDeviceName);
		solo.sleep(3000);

		solo.clickOnScreen((ScreenValues.SCREEN_WIDTH / 2), (ScreenValues.SCREEN_HEIGHT / 2));

		solo.sleep(2000);
		solo.goBack();
		solo.goBack();

		Double checkValue = instance.getVariableValue("shared");
		Log.d("Multiplayer", "variableValue = " + checkValue);

	}

	private void createProject() {
		Project project = new Project(null, UiTestUtils.DEFAULT_TEST_PROJECT_NAME);

		Sprite firstSprite = new Sprite("background");
		Sprite secondSprite = new Sprite(SPRITE_CAT);
		project.addSprite(firstSprite);
		project.addSprite(secondSprite);

		ProjectManager.getInstance().setProject(project);
		ProjectManager.getInstance().setCurrentSprite(firstSprite);

		Script startScript = new StartScript(secondSprite);
		SetLookBrick setLookBrick = new SetLookBrick(secondSprite);
		String imageName = "image";
		File image = UiTestUtils.saveFileToProject(UiTestUtils.DEFAULT_TEST_PROJECT_NAME, imageName, IMAGE_FILE_ID,
				getInstrumentation().getContext(), UiTestUtils.FileTypes.IMAGE);

		LookData lookData = new LookData();
		lookData.setLookFilename(image.getName());
		lookData.setLookName(imageName);
		setLookBrick.setLook(lookData);

		startScript.addBrick(setLookBrick);
		secondSprite.addScript(startScript);

		ProjectManager.getInstance().setCurrentSprite(secondSprite);

		UserVariablesContainer userVariablesContainer = project.getUserVariables();
		userVariablesContainer.addSharedUserVariable("shared");

		Script whenScript = new WhenScript(secondSprite);
		SetVariableBrick setVariable = new SetVariableBrick(secondSprite, new Formula(100), ProjectManager
				.getInstance().getCurrentProject().getUserVariables().getUserVariable("shared", secondSprite));
		setXBrick = new SetXBrick(secondSprite, new Formula(new FormulaElement(
				FormulaElement.ElementType.USER_VARIABLE, "shared", null)));

		whenScript.addBrick(setVariable);
		whenScript.addBrick(setXBrick);
		secondSprite.addScript(whenScript);

		//		StorageHandler.getInstance().saveProject(project);
	}
}
