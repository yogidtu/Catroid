package org.catrobat.catroid.uitest.tooltipsystem;

import org.catrobat.catroid.R;
import org.catrobat.catroid.tooltipsystem.Tooltip;
import org.catrobat.catroid.ui.MainMenuActivity;
import org.catrobat.catroid.ui.ProgramMenuActivity;
import org.catrobat.catroid.uitest.util.UiTestUtils;

import android.test.ActivityInstrumentationTestCase2;

import com.jayway.android.robotium.solo.Solo;

public class TooltipInProgramMenuActivityTest extends ActivityInstrumentationTestCase2<MainMenuActivity> {

	private Solo solo;

	public TooltipInProgramMenuActivityTest() {
		super(MainMenuActivity.class);
	}

	@Override
	public void setUp() throws Exception {
		super.setUp();
		solo = new Solo(getInstrumentation(), getActivity());
		UiTestUtils.getIntoProgramMenuFromMainMenu(solo, 0);
	}

	@Override
	public void tearDown() throws Exception {
		solo.finishOpenedActivities();
		UiTestUtils.clearAllUtilTestProjects();
		super.tearDown();
		solo = null;
	}

	public void testTooltipSystemInProgramMenuActivity() {
		solo.waitForActivity(ProgramMenuActivity.class.getSimpleName());
		solo.clickOnActionBarItem(R.id.button_tooltip);

		solo.sleep(200);
		assertTrue("the tooltip system is active", ProgramMenuActivity.tooltipActive);

		//check if the tooltip button is shown on all positions

		solo.clickOnScreen(400, 160);
		solo.sleep(200);
		//überprüfe ob bubble angezeigt wird
		//bubble wurde auf tooltiplayer gezeichnet und kann nicht mehr abgerufen werden
		//wie kann ich dann überprüfen ob die bubble angezeigt wird???????

		solo.clickOnScreen(400, 310);
		solo.sleep(200);
		//überprüfe ob bubble angezeigt wird

		solo.clickOnScreen(400, 410);
		solo.sleep(200);
		//überprüfe ob bubble angezeigt wird

		solo.clickOnActionBarItem(R.id.button_tooltip);
		solo.sleep(200);
		//überprüfe ob alles weg ist

		assertFalse("Tooltip flag is not set inactive", ProgramMenuActivity.tooltipActive);
	}

	public void testAddTooltipButtonsToProgramMenuActivity() {
		solo.clickOnActionBarItem(R.id.button_tooltip);
		solo.sleep(200);
		assertTrue("Tooltip flag is not set active", ProgramMenuActivity.tooltipActive);
		solo.clickOnActionBarItem(R.id.button_tooltip);
		assertFalse("Tooltip flag is not set inactive", ProgramMenuActivity.tooltipActive);
	}

	public void testRemoveProgramMenuActivityTooltipButtons() {
		solo.clickOnActionBarItem(R.id.button_tooltip);
		solo.sleep(500);
		solo.clickOnActionBarItem(R.id.button_tooltip);
		assertFalse("Tooltip flag is not set inactive", ProgramMenuActivity.tooltipActive);
		solo.clickOnActionBarItem(R.id.button_tooltip);
		assertTrue("Tooltip flag is not set active", ProgramMenuActivity.tooltipActive);
	}

	public void testCheckActivity() {
		solo.waitForActivity(ProgramMenuActivity.class.getName());
		solo.clickOnActionBarItem(R.id.button_tooltip);
		Tooltip tooltip = Tooltip.getInstance(solo.getCurrentActivity());
		int value = tooltip.checkActivity();
		assertEquals("The Activity Value is not correct", value, 3);
	}
}
