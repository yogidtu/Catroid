package org.catrobat.catroid.test.tooltipsystem;

import org.catrobat.catroid.R;
import org.catrobat.catroid.tooltipsystem.Tooltip;
import org.catrobat.catroid.tooltipsystem.TooltipController;
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

	}

	public void testStopTooltipSystem() {

	}

	public void testStartTooltipSystem() {

	}

	public void testSetTooltipPosition() {

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

	//check in UI Test for all activities
	public void testCheckActivity() {
		int value = tooltip.checkActivity();
		assertEquals("The Activity Value is not correct", value, 0);
	}

	public void testGetController() {
		TooltipController controller = tooltip.getController();
		assertNotNull("Tooltip Controller is not null", controller);
	}
}
