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
import android.widget.ListView;

import org.catrobat.catroid.R;
import org.catrobat.catroid.common.LookData;
import org.catrobat.catroid.content.Project;
import org.catrobat.catroid.content.Script;
import org.catrobat.catroid.content.Sprite;
import org.catrobat.catroid.content.StartScript;
import org.catrobat.catroid.content.WhenScript;
import org.catrobat.catroid.content.bricks.ChangeVariableBrick;
import org.catrobat.catroid.content.bricks.SetLookBrick;
import org.catrobat.catroid.formulaeditor.UserVariablesContainer;
import org.catrobat.catroid.io.StorageHandler;
import org.catrobat.catroid.multiplayer.MultiplayerBtManager;
import org.catrobat.catroid.ui.MainMenuActivity;
import org.catrobat.catroid.uitest.annotation.Device;
import org.catrobat.catroid.uitest.util.BTDummyClient;
import org.catrobat.catroid.uitest.util.BaseActivityInstrumentationTestCase;
import org.catrobat.catroid.uitest.util.UiTestUtils;

import java.io.File;
import java.util.ArrayList;

public class MultiplayerTest extends BaseActivityInstrumentationTestCase<MainMenuActivity> {
	private static final int IMAGE_FILE_ID = org.catrobat.catroid.uitest.R.raw.icon;
	private static final String PAIRED_BLUETOOTH_SERVER_DEVICE_NAME = "kitty";
	private static final String SPRITE_CAT = "cat";

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
	public void testBluetoothConnection() {
		String btConnectionOption = BTDummyClient.SERVERDUMMYMULTIPLAYER + BTDummyClient.SETASSERVER
				+ ("" + MultiplayerBtManager.CONNECTION_UUID).replaceAll("-", "");
		BTDummyClient.getInstance().initializeAndConnectToServer(btConnectionOption);
		solo.sleep(1000);

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
			if (deviceName.startsWith(PAIRED_BLUETOOTH_SERVER_DEVICE_NAME)) {
				connectedDeviceName = deviceName;
				break;
			}
		}
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

	private void createProject() {
		ArrayList<Sprite> spriteList = new ArrayList<Sprite>();
		spriteList.add(new Sprite("background"));

		Sprite sprite = new Sprite(SPRITE_CAT);

		Script startScript = new StartScript(sprite);
		SetLookBrick setLookBrick = new SetLookBrick(sprite);
		startScript.addBrick(setLookBrick);
		sprite.addScript(startScript);

		Script whenScript = new WhenScript(sprite);
		ChangeVariableBrick changeVariableBrick = new ChangeVariableBrick(sprite, 10.0);
		whenScript.addBrick(changeVariableBrick);
		sprite.addScript(whenScript);
		spriteList.add(sprite);

		Project project = UiTestUtils.createProject(UiTestUtils.DEFAULT_TEST_PROJECT_NAME, spriteList, null);

		String imageName = "image";
		File image = UiTestUtils.saveFileToProject(UiTestUtils.DEFAULT_TEST_PROJECT_NAME, imageName, IMAGE_FILE_ID,
				getInstrumentation().getContext(), UiTestUtils.FileTypes.IMAGE);

		UserVariablesContainer userVariablesContainer = project.getUserVariables();
		userVariablesContainer.addSharedUserVariable("shared");

		LookData lookData = new LookData();
		lookData.setLookFilename(image.getName());
		lookData.setLookName(imageName);
		setLookBrick.setLook(lookData);
		sprite.getLookDataList().add(lookData);
		StorageHandler.getInstance().saveProject(project);
	}

}
