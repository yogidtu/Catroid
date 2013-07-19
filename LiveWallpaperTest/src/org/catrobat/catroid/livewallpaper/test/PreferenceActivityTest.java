package org.catrobat.catroid.livewallpaper.test;

import com.android.uiautomator.core.UiObject;
import com.android.uiautomator.core.UiObjectNotFoundException;
import com.android.uiautomator.core.UiScrollable;
import com.android.uiautomator.core.UiSelector;
import com.android.uiautomator.testrunner.UiAutomatorTestCase;

public class PreferenceActivityTest extends UiAutomatorTestCase {

	private static int timeout = 2000;
	
	public void testSettingsActivity() throws UiObjectNotFoundException {
		navigateToLiveWallpaper(); 		
		UiObject lwpSettingsButton = new UiObject(new UiSelector().textContains("Settings"));
		assertTrue("Unable to detect live wallpaper settings", lwpSettingsButton.exists());
		lwpSettingsButton.clickAndWaitForNewWindow(timeout);
		
		testAboutPocketCodeDialog();
		testAboutWallpaperDialog(); 
		testAllowSoundsPreference(); 
		
	}
	
	
	private void testAllowSoundsPreference() {
		UiObject allowSounds = new UiObject(new UiSelector().text("Allow sounds"));
		assertTrue("Unable to detect allow sounds in the preference list", allowSounds.exists());
		
		//TODO: implement and test the functionality
		
	}


	private void testAboutWallpaperDialog() throws UiObjectNotFoundException {
		UiObject aboutThisWallpaper = new UiObject(new UiSelector().text("About this wallpaper"));
		assertTrue("Unable to detect about this wallpaper in the preference list", aboutThisWallpaper.exists());
		
		aboutThisWallpaper.clickAndWaitForNewWindow();
		
		UiObject aboutThisWallpaperTitle = new UiObject(new UiSelector().text("About this wallpaper"));
		assertTrue("Unable to detect about this wallpaper title in the dialog", aboutThisWallpaperTitle.exists());
		
		//TODO: search for the project name - maybe set up some test project???? 
		
		getUiDevice().pressBack();
		
	}


	private void testAboutPocketCodeDialog() throws UiObjectNotFoundException{
		UiObject about = new UiObject(new UiSelector().text("About Pocket Code"));
		assertTrue("Unable to detect about text", about.exists());
		about.clickAndWaitForNewWindow(timeout);
		
		UiObject aboutTitle = new UiObject (new UiSelector().textContains("About Pocket Code")); 
		assertTrue("The about dialog title was not found", aboutTitle.exists());
		
		UiObject aboutText = new UiObject (new UiSelector().textContains("Pocket code is a programming environment")); 
		assertTrue("The about dialog text was not found", aboutText.exists());
		
		UiObject licenseUrl = new UiObject (new UiSelector().textContains("Pocket code license")); 
		assertTrue("The license URL was not found", licenseUrl.exists());
		//just to make sure it doesn't crash
		licenseUrl.clickAndWaitForNewWindow(timeout);
		getUiDevice().pressBack(); 
		
		UiObject aboutUrl = new UiObject (new UiSelector().textContains("About Catrobat")); 
		assertTrue("The about catrobat URL was not found", aboutUrl.exists());
		//just to make sure it doesn't crash
		aboutUrl.clickAndWaitForNewWindow(timeout);
		getUiDevice().pressBack(); 
		
		getUiDevice().pressBack(); 
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
		
		UiObject lwp = new UiObject(new UiSelector().textContains("Pocket Code"));
		lwp.clickAndWaitForNewWindow(timeout);

	}
	
}