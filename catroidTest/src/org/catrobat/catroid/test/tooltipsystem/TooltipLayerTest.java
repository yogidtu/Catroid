package org.catrobat.catroid.test.tooltipsystem;

import org.catrobat.catroid.tooltipsystem.Tooltip;
import org.catrobat.catroid.tooltipsystem.TooltipLayer;
import org.catrobat.catroid.ui.MainMenuActivity;
import org.catrobat.catroid.ui.ProgramMenuActivity;
import org.catrobat.catroid.ui.ProjectActivity;

import android.content.Context;
import android.test.ActivityInstrumentationTestCase2;

public class TooltipLayerTest extends ActivityInstrumentationTestCase2<MainMenuActivity> {

	private Tooltip tooltip;
	private Context context;

	public TooltipLayerTest() {
		super(MainMenuActivity.class);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		context = this.getActivity();
		tooltip = Tooltip.getInstance(context);
		tooltip.setContext(context);
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

		check = tooltip.setTooltipPosition(-10, 0, "TestString");
		assertTrue("Incorrect setting hint positions", check);
		layer = tooltip.getController().getTooltipLayer();
		assertEquals("X value is not correct set", 0, layer.getTooltipPositionX());
		assertEquals("Y value is not correct set", 0, layer.getTooltipPositionY());
		assertEquals("Text is not correct set", "TestString", layer.getTooltipText());

		check = tooltip.setTooltipPosition(0, -10, "TestString");
		assertTrue("Incorrect setting hint positions", check);
		layer = tooltip.getController().getTooltipLayer();
		assertEquals("X value is not correct set", 0, layer.getTooltipPositionX());
		assertEquals("Y value is not correct set", 0, layer.getTooltipPositionY());
		assertEquals("Text is not correct set", "TestString", layer.getTooltipText());

		check = tooltip.setTooltipPosition(0, 0, null);
		assertTrue("Incorrect setting hint positions", check);
		layer = tooltip.getController().getTooltipLayer();
		assertEquals("X value is not correct set", 0, layer.getTooltipPositionX());
		assertEquals("Y value is not correct set", 0, layer.getTooltipPositionY());
		assertEquals("Text is not correct set", "", layer.getTooltipText());

		check = tooltip.setTooltipPosition(-10, -10, null);
		assertTrue("Incorrect setting hint positions", check);
		layer = tooltip.getController().getTooltipLayer();
		assertEquals("X value is not correct set", 0, layer.getTooltipPositionX());
		assertEquals("Y value is not correct set", 0, layer.getTooltipPositionY());
		assertEquals("Text is not correct set", "", layer.getTooltipText());

		check = tooltip.setTooltipPosition(Integer.MAX_VALUE, Integer.MAX_VALUE, null);
		assertTrue("Incorrect setting hint positions", check);
		layer = tooltip.getController().getTooltipLayer();
		assertEquals("X value is not correct set", 0, layer.getTooltipPositionX());
		assertEquals("Y value is not correct set", 0, layer.getTooltipPositionY());
		assertEquals("Text is not correct set", "", layer.getTooltipText());

		check = tooltip.setTooltipPosition(1000, 1000, null);
		assertTrue("Incorrect setting hint positions", check);
		layer = tooltip.getController().getTooltipLayer();
		assertEquals("X value is not correct set", 0, layer.getTooltipPositionX());
		assertEquals("Y value is not correct set", 0, layer.getTooltipPositionY());
		assertEquals("Text is not correct set", "", layer.getTooltipText());

	}

	@SuppressWarnings("static-access")
	public void testAddTooltipButtonsToMainMenuActivity() {
		MainMenuActivity activity = (MainMenuActivity) context;
		boolean check = tooltip.getController().getTooltipLayer().addTooltipButtonsToMainMenuActivity();
		assertTrue("Tooltip Buttons not added correctly", check);
		assertTrue("Tooltip System flag is not active", activity.tooltipActive);
	}

	@SuppressWarnings("static-access")
	public void testAddTooltipButtonsToProjectActivity() {
		ProjectActivity activity = (ProjectActivity) context;
		boolean check = tooltip.getController().getTooltipLayer().addTooltipButtonsToMainMenuActivity();
		assertTrue("Tooltip Buttons not added correctly", check);
		assertTrue("Tooltip System flag is not active", activity.tooltipActive);
	}

	@SuppressWarnings("static-access")
	public void testAddTooltipButtonsToProgramMenuActivity() {
		ProgramMenuActivity activity = (ProgramMenuActivity) context;
		boolean check = tooltip.getController().getTooltipLayer().addTooltipButtonsToMainMenuActivity();
		assertTrue("Tooltip Buttons not added correctly", check);
		assertTrue("Tooltip System flag is not active", activity.tooltipActive);
	}

	@SuppressWarnings("static-access")
	public void testRemoveMainMenuActivityTooltipButtons() {
		MainMenuActivity activity = (MainMenuActivity) context;
		boolean check = tooltip.getController().getTooltipLayer().removeMainMenuActivityTooltipButtons();
		assertTrue("Tooltip Buttons not removed correctly", check);
		assertFalse("Tooltip System flag is still active", activity.tooltipActive);
	}

	@SuppressWarnings("static-access")
	public void testRemoveProjectActivityTooltipButtons() {
		ProjectActivity activity = (ProjectActivity) context;
		boolean check = tooltip.getController().getTooltipLayer().removeMainMenuActivityTooltipButtons();
		assertTrue("Tooltip Buttons not removed correctly", check);
		assertFalse("Tooltip System flag is still active", activity.tooltipActive);
	}

	@SuppressWarnings("static-access")
	public void testRemoveProgramMenuActivityTooltipButtons() {
		ProgramMenuActivity activity = (ProgramMenuActivity) context;
		boolean check = tooltip.getController().getTooltipLayer().removeMainMenuActivityTooltipButtons();
		assertTrue("Tooltip Buttons not removed correctly", check);
		assertFalse("Tooltip System flag is still active", activity.tooltipActive);

	}

}
