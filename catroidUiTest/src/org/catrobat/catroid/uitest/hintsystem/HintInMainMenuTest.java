package org.catrobat.catroid.uitest.hintsystem;

import org.catrobat.catroid.R;
import org.catrobat.catroid.hintsystem.Hint;
import org.catrobat.catroid.ui.MainMenuActivity;
import org.catrobat.catroid.uitest.util.UiTestUtils;

import android.test.ActivityInstrumentationTestCase2;

import com.jayway.android.robotium.solo.Solo;

public class HintInMainMenuTest extends ActivityInstrumentationTestCase2<MainMenuActivity> {

	private Solo solo;

	public HintInMainMenuTest() {
		super(MainMenuActivity.class);
	}

	@Override
	public void setUp() throws Exception {
		super.setUp();
		solo = new Solo(getInstrumentation(), getActivity());
	}

	@Override
	public void tearDown() throws Exception {
		solo.finishOpenedActivities();
		UiTestUtils.clearAllUtilTestProjects();
		super.tearDown();
		solo = null;
	}

	public void testHintSystem() {
		solo.waitForActivity(MainMenuActivity.class.getSimpleName());
		solo.clickOnActionBarItem(R.id.button_tooltip);

		solo.sleep(200);

		Hint hint = Hint.getInstance();
		assertNotNull("The hint is null", hint);
		//		HintOverlay overlay = hint.getHintOverlay();
		//		assertNotNull("The overlay is null", overlay);
		assertNotNull("There are no Hints", Hint.getHints());
		//kann ich überprüfen, dass die tooltip buttons angezeigt werden??

		solo.clickOnScreen(410, 160);
		solo.sleep(200);
		//überprüfe ob bubble angezeigt wird

		solo.clickOnScreen(410, 310);
		solo.sleep(200);
		//überprüfe ob bubble angezeigt wird

		solo.clickOnScreen(410, 410);
		solo.sleep(200);
		//überprüfe ob bubble angezeigt wird

		solo.clickOnScreen(410, 510);
		solo.sleep(200);
		//überprüfe ob bubble angezeigt wird

		solo.clickOnScreen(410, 610);
		solo.sleep(200);
		//überprüfe ob bubble angezeigt wird

		solo.clickOnScreen(410, 710);
		solo.sleep(200);
		//überprüfe ob bubble angezeigt wird

	}
}
