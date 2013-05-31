package org.catrobat.catroid.test.hintsystem;

import org.catrobat.catroid.hintsystem.Hint;
import org.catrobat.catroid.ui.MainMenuActivity;

import android.test.ActivityInstrumentationTestCase2;

public class HintOverlayTest extends ActivityInstrumentationTestCase2<MainMenuActivity> {

	private Hint hint;

	public HintOverlayTest(Class<MainMenuActivity> activityClass) {
		super(activityClass);
	}

	@Override
	public void setUp() {
		hint = Hint.getInstance();
		Hint.setContext(getActivity());
		hint.overlayHint();
	}

	@Override
	public void tearDown() {
		hint = null;
	}

	public void testDispatchTouchEvent() {

		assertTrue("IncorrectDispatching", hint.dispatchTouchEvent(null));

	}

	public void testSetPositions() {

		assertTrue("Incorrect setting hint positions", hint.setHintPosition(100, 100, "TestString"));
	}

	public void testCreateBitmap() {

	}

}
