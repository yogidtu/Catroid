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
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.EditText;
import android.widget.ListView;

import org.catrobat.catroid.ProjectManager;
import org.catrobat.catroid.R;
import org.catrobat.catroid.common.LookData;
import org.catrobat.catroid.common.ScreenValues;
import org.catrobat.catroid.content.Project;
import org.catrobat.catroid.content.Sprite;
import org.catrobat.catroid.content.StartScript;
import org.catrobat.catroid.content.WhenScript;
import org.catrobat.catroid.content.bricks.Brick;
import org.catrobat.catroid.content.bricks.ForeverBrick;
import org.catrobat.catroid.content.bricks.LoopBeginBrick;
import org.catrobat.catroid.content.bricks.LoopEndlessBrick;
import org.catrobat.catroid.content.bricks.PlaceAtBrick;
import org.catrobat.catroid.content.bricks.SetLookBrick;
import org.catrobat.catroid.content.bricks.SetVariableBrick;
import org.catrobat.catroid.content.bricks.SetXBrick;
import org.catrobat.catroid.formulaeditor.Formula;
import org.catrobat.catroid.formulaeditor.FormulaElement;
import org.catrobat.catroid.formulaeditor.UserVariablesContainer;
import org.catrobat.catroid.io.StorageHandler;
import org.catrobat.catroid.stage.StageActivity;
import org.catrobat.catroid.ui.MainMenuActivity;
import org.catrobat.catroid.ui.adapter.BrickAdapter;
import org.catrobat.catroid.ui.dialogs.NewVariableDialog;
import org.catrobat.catroid.uitest.annotation.Device;
import org.catrobat.catroid.uitest.util.BTDummyClient;
import org.catrobat.catroid.uitest.util.BaseActivityInstrumentationTestCase;
import org.catrobat.catroid.uitest.util.UiTestUtils;

import java.io.File;
import java.util.ArrayList;

public class MultiplayerUiTest extends BaseActivityInstrumentationTestCase<MainMenuActivity> {
	private static final String PAIRED_BLUETOOTH_SERVER_DEVICE_NAME = "kitty";
	private static final String SPRITE_CAT = "cat";
	private final int screenWidth = ScreenValues.SCREEN_WIDTH;
	private final int screenHeight = ScreenValues.SCREEN_HEIGHT;
	UserVariablesContainer userVariablesContainer = null;
	Sprite firstSprite = null;
	Sprite backGround = null;
	Project project = null;

	private final String filename = "catroid_sunglasses.png";

	public MultiplayerUiTest() {
		super(MainMenuActivity.class);
	}

	@Override
	public void setUp() throws Exception {
		super.setUp();
		createProject();
		UiTestUtils.prepareStageForTest();
	}

	@Device
	public void testSharedVariableTickerWithOption() {

		UiTestUtils.getIntoScriptActivityFromMainMenu(solo);
		ListView dragDropListView = UiTestUtils.getScriptListView(solo);
		BrickAdapter adapter = (BrickAdapter) dragDropListView.getAdapter();

		int childrenCount = adapter.getChildCountFromLastGroup();
		int groupCount = adapter.getScriptCount();

		assertEquals("Incorrect number of bricks.", 2, dragDropListView.getChildCount());
		assertEquals("Incorrect number of bricks.", 1, childrenCount);

		ArrayList<Brick> projectBrickList = project.getSpriteList().get(0).getScript(0).getBrickList();
		assertEquals("Incorrect number of bricks.", 1, projectBrickList.size());

		assertEquals("Wrong Brick instance.", projectBrickList.get(0), adapter.getChild(groupCount - 1, 0));
		assertNotNull("TextView does not exist.", solo.getText(solo.getString(R.string.brick_set_variable)));
		solo.clickOnText(userVariablesContainer.getSharedVariabel("shared").getName());
		solo.clickOnText(solo.getString(R.string.brick_variable_spinner_create_new_variable));
		assertTrue("NewVariableDialog not visible", solo.waitForFragmentByTag(NewVariableDialog.DIALOG_FRAGMENT_TAG));

		assertNotNull("The radiobutton is not shown even the option is active",
				solo.getView(R.id.dialog_formula_editor_variable_name_global_broadcast_variable_radio_button));

		solo.clickOnView(solo.getView(R.id.dialog_formula_editor_variable_name_global_broadcast_variable_radio_button));
		EditText editText = (EditText) solo.getView(R.id.dialog_formula_editor_variable_name_edit_text);
		solo.enterText(editText, "shared2");
		solo.sleep(500);
		solo.clickOnButton(solo.getString(R.string.ok));
		solo.sleep(500);

		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
		if (sharedPreferences.getBoolean("setting_multiplayer_option", true)) {
			sharedPreferences.edit().putBoolean("setting_multiplayer_option", false).commit();
		}

		solo.clickOnText(userVariablesContainer.getSharedVariabel("shared2").getName());
		solo.clickOnText(solo.getString(R.string.brick_variable_spinner_create_new_variable));
		assertTrue("NewVariableDialog not visible", solo.waitForFragmentByTag(NewVariableDialog.DIALOG_FRAGMENT_TAG));

		Log.d("Multiplayer",
				"" + solo.getView(R.id.dialog_formula_editor_variable_name_global_broadcast_variable_radio_button));

		assertTrue("Variable Dialog not shown",
				solo.waitForText(solo.getString(R.string.formula_editor_variable_dialog_for_all_sprites_broadcast)));
	}

	@Device
	public void testBluetoothConnectionAsServer() {

		BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
		assertTrue("Bluetooth not supported on device", btAdapter != null);
		if (!btAdapter.isEnabled()) {
			btAdapter.enable();
			solo.sleep(5000);
		}

		solo.clickOnText(solo.getString(R.string.main_menu_continue));
		UiTestUtils.clickOnBottomBar(solo, R.id.button_play);
		solo.sleep(1000);

		Log.d("Multiplayer", "String = " + BTDummyClient.MULTIPLAYERSETASCLIENT);
		BTDummyClient instance = new BTDummyClient();
		instance.initializeAndConnectToServer(BTDummyClient.MULTIPLAYERSETASCLIENT);

		solo.sleep(3000);
		solo.assertCurrentActivity("Not in Stage - connection failed", StageActivity.class);

		instance.sendSetVariableCommandToDummyServer("shared", -100.0);
		solo.sleep(1000);
		assertEquals("Set Variable faild wrong Value", -100.0, userVariablesContainer.getSharedVariabel("shared")
				.getValue(), 0.2);
		solo.sleep(500);
		solo.clickOnScreen(ScreenValues.SCREEN_WIDTH / 2 - 100, ScreenValues.SCREEN_HEIGHT / 2);
		assertEquals("Set Variable faild wrong Value", 100, userVariablesContainer.getSharedVariabel("shared")
				.getValue(), 0.2);
		solo.sleep(1000);

		Double checkValue = instance.getVariableValue("shared");
		assertEquals("Set Variable faild wrong Value", checkValue, userVariablesContainer.getSharedVariabel("shared")
				.getValue(), 0.2);

		solo.goBack();
		solo.goBack();

	}

	@Device
	public void testBluetoothSetVariable() {
		Log.d("Multiplayer", "String = " + BTDummyClient.MULTIPLAYERSETASSERVER);
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
		solo.sleep(2000);
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
		solo.assertCurrentActivity("Not in Stage - connection failed", StageActivity.class);

		instance.sendSetVariableCommandToDummyServer("shared", -100.0);
		solo.sleep(1000);
		assertEquals("Set Variable faild wrong Value", -100.0, userVariablesContainer.getSharedVariabel("shared")
				.getValue(), 0.2);
		solo.sleep(1000);
		solo.clickOnScreen(ScreenValues.SCREEN_WIDTH / 2 - 100, ScreenValues.SCREEN_HEIGHT / 2);
		assertEquals("Set Variable faild wrong Value", 100, userVariablesContainer.getSharedVariabel("shared")
				.getValue(), 0.2);
		solo.sleep(1000);

		Double checkValue = instance.getVariableValue("shared");
		assertEquals("Set Variable faild wrong Value", checkValue, userVariablesContainer.getSharedVariabel("shared")
				.getValue(), 0.2);

		solo.goBack();
		solo.goBack();

	}

	private void createProject() {
		project = new Project(null, UiTestUtils.DEFAULT_TEST_PROJECT_NAME);
		userVariablesContainer = project.getUserVariables();
		userVariablesContainer.addSharedUserVariable("shared");
		backGround = new Sprite("Background");
		SetVariableBrick setVariableBackGround = new SetVariableBrick(backGround, new Formula(0.0),
				userVariablesContainer.getUserVariable("shared", backGround));
		StartScript backGroundStartScript = new StartScript(backGround);
		backGroundStartScript.addBrick(setVariableBackGround);
		backGround.addScript(backGroundStartScript);

		firstSprite = new Sprite(SPRITE_CAT);
		StartScript startScriptCat = new StartScript(firstSprite);
		SetLookBrick setLookCat = new SetLookBrick(firstSprite);

		LookData lookDataCat = new LookData();
		lookDataCat.setLookName(filename);
		firstSprite.getLookDataList().add(lookDataCat);
		setLookCat.setLook(lookDataCat);
		startScriptCat.addBrick(setLookCat);

		PlaceAtBrick placeAtCat = new PlaceAtBrick(firstSprite, screenWidth / 2, screenHeight / 2);
		startScriptCat.addBrick(placeAtCat);
		firstSprite.addScript(startScriptCat);

		LoopBeginBrick repeatEndlessBrickStart = new ForeverBrick(firstSprite);
		LoopEndlessBrick repeatEndlessBrickEnd = new LoopEndlessBrick();
		repeatEndlessBrickStart.setLoopEndBrick(repeatEndlessBrickEnd);
		startScriptCat.addBrick(repeatEndlessBrickStart);

		SetXBrick setXBrick = new SetXBrick(firstSprite, new Formula(new FormulaElement(
				FormulaElement.ElementType.USER_VARIABLE, "shared", null)));
		startScriptCat.addBrick(setXBrick);
		startScriptCat.addBrick(repeatEndlessBrickEnd);

		WhenScript whenScriptCat = new WhenScript(firstSprite);
		SetVariableBrick setVariable = new SetVariableBrick(firstSprite, new Formula(100.0),
				userVariablesContainer.getUserVariable("shared", firstSprite));

		whenScriptCat.addBrick(setVariable);
		firstSprite.addScript(whenScriptCat);

		File catImageFile = UiTestUtils.saveFileToProject(project.getName(), filename,
				org.catrobat.catroid.uitest.R.drawable.catroid_sunglasses, getInstrumentation().getContext(),
				UiTestUtils.FileTypes.IMAGE);
		lookDataCat.setLookFilename(catImageFile.getName());

		project.addSprite(backGround);
		project.addSprite(firstSprite);
		ProjectManager.getInstance().setProject(project);
		StorageHandler.getInstance().saveProject(project);

		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
		if (!sharedPreferences.getBoolean("setting_multiplayer_option", false)) {
			sharedPreferences.edit().putBoolean("setting_multiplayer_option", true).commit();
		}

	}
}
