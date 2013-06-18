package org.catrobat.catroid.uitest.tooltipsystem;

import org.catrobat.catroid.R;
import org.catrobat.catroid.tooltipsystem.Tooltip;
import org.catrobat.catroid.ui.MainMenuActivity;
import org.catrobat.catroid.ui.ProjectActivity;
import org.catrobat.catroid.uitest.util.UiTestUtils;

import android.test.ActivityInstrumentationTestCase2;

import com.jayway.android.robotium.solo.Solo;

public class TooltipInProjectActivityTest extends ActivityInstrumentationTestCase2<MainMenuActivity> {

	private Solo solo;

	public TooltipInProjectActivityTest() {
		super(MainMenuActivity.class);
	}

	@Override
	public void setUp() throws Exception {
		super.setUp();
		solo = new Solo(getInstrumentation(), getActivity());
		UiTestUtils.getIntoSpritesFromMainMenu(solo);
	}

	@Override
	public void tearDown() throws Exception {
		solo.finishOpenedActivities();
		UiTestUtils.clearAllUtilTestProjects();
		super.tearDown();
		solo = null;
	}

	public void testTooltipSystemInProjectActivity() {
		solo.waitForActivity(ProjectActivity.class.getSimpleName());
		solo.clickOnActionBarItem(R.id.button_tooltip);

		solo.sleep(200);
		assertTrue("the tooltip system is active", ProjectActivity.tooltipActive);

		//check if the tooltip button is shown on all positions

		solo.clickOnScreen(210, 125);
		solo.sleep(200);
		//überprüfe ob bubble angezeigt wird
		//bubble wurde auf tooltiplayer gezeichnet und kann nicht mehr abgerufen werden
		//wie kann ich dann überprüfen ob die bubble angezeigt wird???????

		solo.clickOnScreen(210, 310);
		solo.sleep(200);
		//überprüfe ob bubble angezeigt wird

		solo.clickOnScreen(210, 710);
		solo.sleep(200);
		//überprüfe ob bubble angezeigt wird

		solo.clickOnScreen(400, 710);
		solo.sleep(200);
		//überprüfe ob bubble angezeigt wird

		solo.clickOnActionBarItem(R.id.button_tooltip);
		solo.sleep(200);
		//überprüfe ob alles weg ist

		assertFalse("Tooltip flag is not set inactive", ProjectActivity.tooltipActive);

	}

	public void testAddTooltipButtonsToProjectActivity() {
		solo.clickOnActionBarItem(R.id.button_tooltip);
		solo.sleep(200);
		assertTrue("Tooltip flag is not set active", ProjectActivity.tooltipActive);
		solo.clickOnActionBarItem(R.id.button_tooltip);
		assertFalse("Tooltip flag is not set inactive", ProjectActivity.tooltipActive);
	}

	public void testRemoveProjectActivityTooltipButtons() {
		solo.clickOnActionBarItem(R.id.button_tooltip);
		solo.sleep(500);
		solo.clickOnActionBarItem(R.id.button_tooltip);
		assertFalse("Tooltip flag is not set inactive", ProjectActivity.tooltipActive);
		solo.clickOnActionBarItem(R.id.button_tooltip);
		assertTrue("Tooltip flag is not set active", ProjectActivity.tooltipActive);
	}

	public void testCheckActivity() {
		solo.waitForActivity(ProjectActivity.class.getName());
		Tooltip tooltip = Tooltip.getInstance(solo.getCurrentActivity());
		int value = tooltip.checkActivity();
		assertEquals("The Activity Value is not correct", value, 1);

	}

}
