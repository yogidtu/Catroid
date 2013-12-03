/**
 *  Catroid: An on-device visual programming system for AndHardwareNfcTest.javaroid devices
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
package org.catrobat.catroid.uitest.arduinohardware;

import org.catrobat.catroid.R;
import org.catrobat.catroid.content.Project;
import org.catrobat.catroid.content.Sprite;
import org.catrobat.catroid.content.WhenScript;
import org.catrobat.catroid.content.bricks.SetVariableBrick;
import org.catrobat.catroid.formulaeditor.Formula;
import org.catrobat.catroid.formulaeditor.FormulaElement;
import org.catrobat.catroid.formulaeditor.InternFormulaParser;
import org.catrobat.catroid.formulaeditor.InternToken;
import org.catrobat.catroid.formulaeditor.InternTokenType;
import org.catrobat.catroid.formulaeditor.Sensors;
import org.catrobat.catroid.formulaeditor.UserVariable;
import org.catrobat.catroid.formulaeditor.UserVariablesContainer;
import org.catrobat.catroid.nfc.NfcHandler;
import org.catrobat.catroid.stage.StageActivity;
import org.catrobat.catroid.ui.MainMenuActivity;
import org.catrobat.catroid.uitest.util.BaseActivityInstrumentationTestCase;
import org.catrobat.catroid.uitest.util.UiTestUtils;
import org.catrobat.catroid.uitest.util.arduinohardwaretest.ArduinoConnection;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class HardwareNfcTest extends BaseActivityInstrumentationTestCase<MainMenuActivity> {

	private static final int NUMBER_EMULATE_TRIES = 3;
	private UserVariable userVariable;

	public HardwareNfcTest() {
		super(MainMenuActivity.class);
	}

	public void testNfcUid() {
		int emulateUid = 0x123456;
		byte[] expectedUid = { (byte) 0x08, 0x12, 0x34, 0x56 }; // first byte is fixed to 0x08

		createAndStartProjectWithWaitBrickAndSetNfcSensorToUserVariable();
		solo.sleep(6000);
		//ArduinoConnection ac = new ArduinoConnection("192.168.0.166", 6789);
		ArduinoConnection ac = new ArduinoConnection("129.27.202.103", 6789);

		try {
			System.out.println("starting emulation");
			boolean emulateOk = false;
			for (int i = 0; i < NUMBER_EMULATE_TRIES && emulateOk == false; i++) {
				emulateOk = ac.nfcEmulateTag(emulateUid, false);
			}
			System.out.println("emulation ended");
			assertTrue("Arduino timed out when emulating nfc tag. (no read from emulated tag occured)", emulateOk);
		} catch (Exception e) {
			fail("Connection or communication to arduino failed.");
			e.printStackTrace();
		}

		solo.clickOnScreen(200, 200);
		solo.sleep(2000);

		assertEquals("uid does not match!", NfcHandler.convertByteArrayToDouble(expectedUid), userVariable.getValue());
		assertEquals("uid_sensor was not resetted to zero", 0.0, NfcHandler.getInstance().getAndResetUid());

	}

	private void createAndStartProjectWithWaitBrickAndSetNfcSensorToUserVariable() {
		ArrayList<Sprite> spriteList = new ArrayList<Sprite>();

		Sprite sprite = new Sprite("background");

		WhenScript whenScript = new WhenScript(sprite);

		//		SpeakBrick speakBrick = new SpeakBrick(sprite, "tappped");
		//		whenScript.addBrick(speakBrick);

		String uservariableName = "nfcUidStorageVariable";
		userVariable = new UserVariable(uservariableName);
		List<InternToken> internTokenList = new LinkedList<InternToken>();
		internTokenList.add(new InternToken(InternTokenType.SENSOR, Sensors.NFC_UID.name()));
		InternFormulaParser internFormulaParser = new InternFormulaParser(internTokenList);
		FormulaElement root = internFormulaParser.parseFormula();
		Formula formula = new Formula(root);

		SetVariableBrick setVariableBrick = new SetVariableBrick(sprite, formula, userVariable);
		whenScript.addBrick(setVariableBrick);

		sprite.addScript(whenScript);
		spriteList.add(sprite);

		Project project = UiTestUtils.createProject(UiTestUtils.DEFAULT_TEST_PROJECT_NAME, spriteList, null);
		UserVariablesContainer userVariables = project.getUserVariables();
		userVariables.addProjectUserVariable(uservariableName);

		UiTestUtils.prepareStageForTest();
		UiTestUtils.getIntoSpritesFromMainMenu(solo);
		UiTestUtils.clickOnBottomBar(solo, R.id.button_play);
		solo.waitForActivity(StageActivity.class.getSimpleName());
	}
}
