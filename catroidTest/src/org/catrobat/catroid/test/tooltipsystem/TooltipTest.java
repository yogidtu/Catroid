package org.catrobat.catroid.test.tooltipsystem;

import org.catrobat.catroid.R;
import org.catrobat.catroid.tooltipsystem.Tooltip;
import org.catrobat.catroid.tooltipsystem.TooltipController;
import org.catrobat.catroid.tooltipsystem.TooltipLayer;
import org.catrobat.catroid.tooltipsystem.TooltipObject;
import org.catrobat.catroid.ui.MainMenuActivity;

import android.app.Activity;
import android.content.Context;
import android.test.ActivityInstrumentationTestCase2;
import android.util.DisplayMetrics;
import android.view.WindowManager;

public class TooltipTest extends ActivityInstrumentationTestCase2<MainMenuActivity> {

	private Tooltip tooltip;
	private Context context;

	public TooltipTest() {
		super(MainMenuActivity.class);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		context = this.getActivity();
		tooltip = Tooltip.getInstance(context);

	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		tooltip = null;
	}

	public void testGetInstance() {
		tooltip = Tooltip.getInstance(context);
		TooltipController controller = tooltip.getController();
		assertNotNull("Tooltip must not be null", tooltip);
		assertNotNull("Controller must not be null", controller);
		assertNotNull("TooltipLayer must not be null", controller.getTooltipLayer());
		assertNotNull("TooltipObject must not be null", controller.getTooltip(0));
	}

	public void testGetTooltip() {
		TooltipObject tooltipObject = tooltip.getTooltipObjectForScreenObject(R.id.main_menu_button_continue);
		assertNotNull("Tooltip Object is not null", tooltipObject);
		//TODO check the text, coordinates for all objects in every activity

	}

	public void testGetScreenHeight() {
		int height = tooltip.getScreenHeight();
		DisplayMetrics deviceDisplayMetrics = new DisplayMetrics();
		WindowManager windowManager = ((Activity) context).getWindowManager();
		windowManager.getDefaultDisplay().getMetrics(deviceDisplayMetrics);
		int screenHeight = deviceDisplayMetrics.heightPixels;
		assertEquals("Screen Height is not correct", screenHeight, height);
	}

	public void testGetScreenWidth() {
		int width = tooltip.getScreenWidth();
		DisplayMetrics deviceDisplayMetrics = new DisplayMetrics();
		WindowManager windowManager = ((Activity) context).getWindowManager();
		windowManager.getDefaultDisplay().getMetrics(deviceDisplayMetrics);
		int screenWidth = deviceDisplayMetrics.widthPixels;
		assertEquals("Screen Width is not correct", screenWidth, width);
	}

	public void testGetController() {
		TooltipController controller = tooltip.getController();
		assertNotNull("Tooltip Controller is not null", controller);
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
