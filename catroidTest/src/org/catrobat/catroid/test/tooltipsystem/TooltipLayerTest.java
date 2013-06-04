package org.catrobat.catroid.test.tooltipsystem;

import org.catrobat.catroid.tooltipsystem.Tooltip;
import org.catrobat.catroid.tooltipsystem.TooltipLayer;
import org.catrobat.catroid.ui.MainMenuActivity;

import android.test.ActivityInstrumentationTestCase2;

public class TooltipLayerTest extends ActivityInstrumentationTestCase2<MainMenuActivity> {

	private Tooltip tooltip;

	public TooltipLayerTest() {
		super(MainMenuActivity.class);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		tooltip = Tooltip.getInstance();
		tooltip.setContext(getActivity());
		tooltip.startTooltipSystem();
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		tooltip = null;
	}

	public void testSetPositions() {
		boolean check = tooltip.setTooltipPosition(100, 100, "TestString");
		assertTrue("Incorrect setting hint positions", check);
		TooltipLayer layer = tooltip.getController().getTooltipLayer();
		assertEquals("X value is not correct set", 100, layer.getTooltipPositionX());
		assertEquals("Y value is not correct set", 100, layer.getTooltipPositionY());
		assertEquals("Text is not correct set", "TestString", layer.getTooltipText());

		//TODO check with other values like negative....
	}

	public void testCreateBitmap() {

	}

	public void testAddTooltipButtonsToMainMenuActivity() {

	}

	public void testAddTooltipButtonsToProjectActivity() {

	}

	public void testAddTooltipButtonsToProgramMenuActivity() {

	}

	public void testMainMenuActivityTooltipButtons() {

	}

	public void testProjectActivityTooltipButtons() {

	}

	public void testProgramMenuActivityTooltipButtons() {

	}

}
