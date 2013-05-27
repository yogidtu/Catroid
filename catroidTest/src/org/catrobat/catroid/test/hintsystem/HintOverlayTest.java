package org.catrobat.catroid.test.hintsystem;

import org.catrobat.catroid.hintsystem.HintOverlay;
import org.catrobat.catroid.ui.MainMenuActivity;

import android.test.ActivityInstrumentationTestCase2;

public class HintOverlayTest extends ActivityInstrumentationTestCase2<MainMenuActivity> {

	public HintOverlayTest(Class<MainMenuActivity> activityClass) {
		super(activityClass);
	}

	public void testSimpleOverlayTest() {
		HintOverlay overlay = new HintOverlay(getActivity().getApplicationContext());

	}

}
