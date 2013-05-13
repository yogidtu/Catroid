package org.catrobat.catroid.uitest.hintsystem;

import java.util.ArrayList;

import org.catrobat.catroid.hintsystem.HintOverlay;
import org.catrobat.catroid.ui.MainMenuActivity;
import org.catrobat.catroid.uitest.util.UiTestUtils;

import android.graphics.Bitmap;
import android.test.ActivityInstrumentationTestCase2;
import android.view.SurfaceView;
import android.view.View;

import com.jayway.android.robotium.solo.Solo;

public class SimpleHintTest extends ActivityInstrumentationTestCase2<MainMenuActivity> {

	private Solo solo;

	public SimpleHintTest() {
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

	public void testFirstTry() {
		solo.waitForActivity(MainMenuActivity.class.getSimpleName());
		ArrayList<View> viewList = solo.getViews();
		for (int i = 0; i < viewList.size(); i++) {
			View v = viewList.get(i);
			if (v instanceof HintOverlay) {

				SurfaceView currentView = (SurfaceView) v;
				Bitmap bitmap = currentView.getDrawingCache();

				int whatever = 0;
				whatever = whatever + 1;
			}
		}
	}

}
