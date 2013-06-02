package org.catrobat.catroid.test.tooltipsystem;

import org.catrobat.catroid.tooltipsystem.Tooltip;
import org.catrobat.catroid.ui.MainMenuActivity;

import android.test.ActivityInstrumentationTestCase2;

public class TooltipLayerTest extends ActivityInstrumentationTestCase2<MainMenuActivity> {

	private Tooltip hint;

	public TooltipLayerTest(Class<MainMenuActivity> activityClass) {
		super(activityClass);
	}

	@Override
	public void setUp() {
		hint = Tooltip.getInstance();
		Tooltip.setContext(getActivity());
		hint.startTooltipSystem();
	}

	@Override
	public void tearDown() {
		hint = null;
	}

	public void testDispatchTouchEvent() {

		assertTrue("IncorrectDispatching", hint.dispatchTouchEvent(null));

	}

	public void testSetPositions() {

		assertTrue("Incorrect setting hint positions", hint.setTooltipPosition(100, 100, "TestString"));
	}

	public void testCreateBitmap() {

	}

}
