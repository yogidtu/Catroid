package org.catrobat.catroid.test.tooltipsystem;

import org.catrobat.catroid.tooltipsystem.Tooltip;
import org.catrobat.catroid.tooltipsystem.TooltipLayer;
import org.catrobat.catroid.ui.MainMenuActivity;

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
}
