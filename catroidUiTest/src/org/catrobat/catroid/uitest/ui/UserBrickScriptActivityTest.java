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
package org.catrobat.catroid.uitest.ui;

import android.test.ActivityInstrumentationTestCase2;

import com.jayway.android.robotium.solo.Solo;

import org.catrobat.catroid.R;
import org.catrobat.catroid.content.Script;
import org.catrobat.catroid.content.bricks.UserBrick;
import org.catrobat.catroid.ui.MainMenuActivity;
import org.catrobat.catroid.ui.UserBrickScriptActivity;
import org.catrobat.catroid.ui.fragment.AddBrickFragment;
import org.catrobat.catroid.ui.fragment.UserBrickDataEditorFragment;
import org.catrobat.catroid.uitest.util.UiTestUtils;

public class UserBrickScriptActivityTest extends ActivityInstrumentationTestCase2<MainMenuActivity> {
	private Solo solo = null;

	public UserBrickScriptActivityTest() {
		super(MainMenuActivity.class);
	}

	@Override
	public void setUp() throws Exception {
		super.setUp();
		UiTestUtils.prepareStageForTest();
		UiTestUtils.createTestProjectWithUserBrick();

		solo = new Solo(getInstrumentation(), getActivity());
		UiTestUtils.getIntoScriptActivityFromMainMenu(solo);
	}

	@Override
	public void tearDown() throws Exception {
		UiTestUtils.goBackToHome(getInstrumentation());
		solo.finishOpenedActivities();
		UiTestUtils.clearAllUtilTestProjects();
		super.tearDown();
		solo = null;
	}

	public void testUserBrickEditInstanceScriptChangesOtherInstanceScript() throws InterruptedException {
		UiTestUtils.addNewBrick(solo, R.string.category_user_bricks, UiTestUtils.TEST_USER_BRICK_NAME, 0);

		// click away the floating brick prompt (where to place the brick?)
		// this workaround  is used because the hovering brick does not contain text that we can search for,
		// as it is a prerendered imageview. Why doesn't robotium have an OCR feature? :P 

		// click on position x brick-heights above/below the place where the brick currently is
		int[] location = UiTestUtils.dragFloatingBrick(solo, -1);
		assertTrue("was not able to find the brick we just added: first user brick", location != null);
		solo.sleep(200);

		Script currentScript = UiTestUtils.getProjectManager().getCurrentScript();
		int indexOfUserBrickInScript = currentScript.containsBrickOfTypeReturnsFirstIndex(UserBrick.class);
		assertTrue("current script should contain a User Brick after we tried to add one.",
				indexOfUserBrickInScript != -1);

		UserBrick userBrick = (UserBrick) currentScript.getBrick(indexOfUserBrickInScript);
		assertTrue("we should be able to cast the brick we found to a User Brick.", userBrick != null);

		// click on the user brick in the list to open it's menu
		solo.clickOnText(UiTestUtils.TEST_USER_BRICK_NAME);

		solo.sleep(200);

		showSourceAndEditBrick(UiTestUtils.TEST_USER_BRICK_NAME);

		// add a new brick to the internal script of the user brick
		UiTestUtils.addNewBrick(solo, R.string.brick_change_y_by);

		// place it
		location = UiTestUtils.dragFloatingBrick(solo, 1);
		assertTrue("was not able to find the brick we just added: brick inside user brick", location != null);
		solo.sleep(200);

		// go back to normal script activity
		solo.goBack();
		solo.sleep(600);
		solo.goBack();
		solo.sleep(600);

		UiTestUtils.addNewBrick(solo, R.string.category_user_bricks, UiTestUtils.TEST_USER_BRICK_NAME, 0);

		location = UiTestUtils.dragFloatingBrick(solo, 1);
		assertTrue("was not able to find the brick we just added: second user brick", location != null);

		solo.sleep(200);

		// click on the location the brick was just dragged to.
		solo.clickLongOnScreen(location[0], location[1], 10);

		showSourceAndEditBrick(UiTestUtils.TEST_USER_BRICK_NAME);

		String brickAddedToUserBrickScriptName = solo.getCurrentActivity().getString(R.string.brick_change_y_by);
		assertTrue("was not able to find the script we added to the other instance",
				solo.searchText(brickAddedToUserBrickScriptName));
	}

	public void testCantEditBrickDataWhileAddingNewBrick() throws InterruptedException {
		UiTestUtils.addNewBrick(solo, R.string.category_user_bricks, UiTestUtils.TEST_USER_BRICK_NAME, 0);
		UiTestUtils.dragFloatingBrick(solo, -1);
		solo.sleep(200);

		// click on the user brick in the list to open it's menu
		solo.clickOnText(UiTestUtils.TEST_USER_BRICK_NAME);

		solo.sleep(200);

		showSourceAndEditBrick(UiTestUtils.TEST_USER_BRICK_NAME);

		// add a new brick to the internal script of the user brick
		UiTestUtils.addNewBrick(solo, R.string.brick_change_y_by);

		// place it (this should click on the define brick)
		UiTestUtils.dragFloatingBrick(solo, -1);

		boolean wentToDataEditor = solo.waitForFragmentByTag(
				UserBrickDataEditorFragment.BRICK_DATA_EDITOR_FRAGMENT_TAG, 800);

		assertTrue("the userBrickDataEditor should not be open!!", !wentToDataEditor);
	}

	public void showSourceAndEditBrick(String brickName) {
		String stringOnShowSourceButton = solo.getCurrentActivity()
				.getString(R.string.brick_context_dialog_show_source);
		solo.clickOnText(stringOnShowSourceButton);

		//Log.d("FOREST", "showSourceAndEditBrick: waitForFragmentByTag");

		boolean addBrickShowedUp = solo.waitForFragmentByTag(AddBrickFragment.ADD_BRICK_FRAGMENT_TAG, 1000);
		assertTrue("addBrickShowedUp should have showed up", addBrickShowedUp);

		boolean brickShowedUp = solo.waitForText(brickName, 0, 1000);
		assertTrue(brickName + " should have showed up", brickShowedUp);

		UiTestUtils.clickOnBrickInAddBrickFragment(solo, brickName, false);

		String stringOnEditButton = solo.getCurrentActivity().getString(R.string.brick_context_dialog_edit_brick);

		boolean editButtonShowedUp = solo.waitForText(stringOnEditButton, 0, 2000);
		assertTrue(stringOnEditButton + " should have showed up", editButtonShowedUp);

		solo.clickOnText(stringOnEditButton);

		boolean activityShowedUp = solo.waitForActivity(UserBrickScriptActivity.class, 500);
		assertTrue("UserBrickScriptActivity should have showed up", activityShowedUp);

		solo.sleep(50);
	}

}
