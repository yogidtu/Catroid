package org.catrobat.catroid.livewallpaper.test;

import com.android.uiautomator.core.UiObject;
import com.android.uiautomator.core.UiObjectNotFoundException;
import com.android.uiautomator.core.UiScrollable;
import com.android.uiautomator.core.UiSelector;
import com.android.uiautomator.testrunner.UiAutomatorTestCase;

public class InitialTest extends UiAutomatorTestCase {

	private static int timeout = 2000;
	
	
	public void testCase() throws UiObjectNotFoundException {
		navigateToLiveWallpaper(); 		
		UiObject lwpSettingsButton = new UiObject(new UiSelector().textContains("Settings"));
		  assertTrue("Unable to detect Settings button", 
			         lwpSettingsButton.exists());
		
		lwpSettingsButton.clickAndWaitForNewWindow(timeout);
	}
	
	private void navigateToLiveWallpaper() throws UiObjectNotFoundException{
		
		getUiDevice().pressHome();
		
		UiObject allAppsButton = new UiObject(new UiSelector().description("Apps"));
		allAppsButton.clickAndWaitForNewWindow(timeout);
		
		// in the apps tabs, we can simulate a user swiping until
	      // they come to the Settings app icon.  Since the container view 
	      // is scrollable, we can use a UiScrollable object.
	      UiScrollable appViews = new UiScrollable(new UiSelector()
	         .scrollable(true));
	      
	      // Set the swiping mode to horizontal (the default is vertical)
	      appViews.setAsHorizontalList();
	      UiObject settingsApp = appViews.getChildByText(new UiSelector()
	         .className(android.widget.TextView.class.getName()), 
	         "Settings");
	      settingsApp.clickAndWaitForNewWindow();
	      
	
		UiObject displayButton = new UiObject(new UiSelector().text("Display"));
		displayButton.clickAndWaitForNewWindow(timeout);
		
		UiObject backgroundButton = new UiObject(new UiSelector().text("Wallpaper"));
		backgroundButton.clickAndWaitForNewWindow(timeout);
		
		UiObject lwpButton = new UiObject(new UiSelector().textContains("Live"));
		lwpButton.clickAndWaitForNewWindow(timeout);
		
		UiObject lwp = new UiObject(new UiSelector().text("Pocket Code"));
		lwp.clickAndWaitForNewWindow(timeout);

	}
	
}