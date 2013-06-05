/**
 *  Catroid: An on-device visual programming system for Android devices
 *  Copyright (C) 2010-2012 The Catrobat Team
 *  (<http://developer.catrobat.org/credits>)
 *  
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as
 *  published by the Free Software Foundation, either version 3 of the
 *  License, or (at your option) any later version.
 *  
 *  An additional term exception under section 7 of the GNU Affero
 *  General Public License, version 3, is available at
 *  http://developer.catrobat.org/license_additional_term
 *  
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU Affero General Public License for more details.
 *  
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.catrobat.catroid.tooltipsystem;

/**
 * @author amore
 * 
 */
public class ScreenParameters {
	private static ScreenParameters screenParameters = new ScreenParameters();

	private int actionBarMenuWidth = 21;
	private int actionBarMenuXPosition = 80;
	private int actionBarMenuHeight = 13;
	private int actionBarMenuYPosition = 0;

	private int mainMenuTooltipXPosition = 83;
	private int tooltipWidth = 17;
	private int tooltipHeight = 10;

	private int mainMenuContinueTooltipYPosition = 19;
	private int mainMenuNewTooltipYPosition = 38;
	private int mainMenuProgramsTooltipYPosition = 50;
	private int mainMenuForumTooltipYPosition = 63;
	private int mainMenuCommunityTooltipYPosition = 75;
	private int mainMenuUploadTooltipYPosition = 88;

	private int projectActivitySpriteBackgroundTooltipXPosition = 42;
	private int projectActivitySpriteBackgroundTooltipYPosition = 15;
	private int projectActivitySpriteObjectTooltipXPosition = 42;
	private int projectActivitySpriteObjectTooltipYPosition = 38;
	private int projectActivityAddButtonTooltipXPosition = 42;
	private int projectActivityAddButtonTooltipYPosition = 88;
	private int projectActivityPlayButtonTooltipXPosition = 83;
	private int projectActivityPlayButtonTooltipYPosition = 88;

	private int programMenuScriptsTooltipXPosition = 83;
	private int programMenuScriptsTooltipYPosition = 19;
	private int programMenuLooksTooltipXPosition = 83;
	private int programMenuLooksTooltipYPosition = 32;
	private int programMenuSoundsTooltipXPosition = 83;
	private int programMenuSoundsTooltipYPosition = 50;
	private int programMenuPlayButtonTooltipXPosition = 83;
	private int programMenuPlayButtonTooltipYPosition = 88;

	private ScreenParameters() {

	}

	private enum DENSITY {
		LDPI, MDPI, HDPI, XDPI
	}

	private DENSITY density;

	public static ScreenParameters getInstance() {
		if (screenParameters == null) {
			screenParameters = new ScreenParameters();
		}
		return screenParameters;
	}

	public void setDensityParameter(float density) {
		if (density < 1.0f) {
			this.density = DENSITY.LDPI;
		} else if (density == 1.0f) {
			this.density = DENSITY.MDPI;
		} else if (density == 1.5f) {
			this.density = DENSITY.HDPI;
		} else if (density > 1.5f) {
			this.density = DENSITY.XDPI;
		}
	}

	public DENSITY getDensity() {
		return density;
	}

	public int setRelativeCoordinatesToDensity(int value, boolean isWidth) {
		if (value > 100) {
			value = 100;
		}

		if (isWidth) {
			value = (int) ((value / 100.0f) * Tooltip.getInstance(null).getScreenWidth());
		} else {
			value = (int) ((value / 100.0f) * Tooltip.getInstance(null).getScreenHeight());
		}
		return value;
	}

	public int getActionBarMenuWidth() {
		return setRelativeCoordinatesToDensity(actionBarMenuWidth, true);
	}

	public int getActionBarMenuXPosition() {
		return setRelativeCoordinatesToDensity(actionBarMenuXPosition, true);
	}

	public int getActionBarMenuHeight() {
		return setRelativeCoordinatesToDensity(actionBarMenuHeight, false);
	}

	public int getActionBarMenuYPosition() {
		return setRelativeCoordinatesToDensity(actionBarMenuYPosition, false);
	}

	public int getMainMenuTooltipXPosition() {
		return setRelativeCoordinatesToDensity(mainMenuTooltipXPosition, true);
	}

	public int getTooltipWidth() {
		return setRelativeCoordinatesToDensity(tooltipWidth, true);
	}

	public int getTooltipHeight() {
		return setRelativeCoordinatesToDensity(tooltipHeight, false);
	}

	public int getMainMenuContinueTooltipYPosition() {
		return setRelativeCoordinatesToDensity(mainMenuContinueTooltipYPosition, false);
	}

	public int getMainMenuNewTooltipYPosition() {
		return setRelativeCoordinatesToDensity(mainMenuNewTooltipYPosition, false);
	}

	public int getMainMenuProgramsTooltipYPosition() {
		return setRelativeCoordinatesToDensity(mainMenuProgramsTooltipYPosition, false);
	}

	public int getMainMenuForumTooltipYPosition() {
		return setRelativeCoordinatesToDensity(mainMenuForumTooltipYPosition, false);
	}

	public int getMainMenuCommunityTooltipYPosition() {
		return setRelativeCoordinatesToDensity(mainMenuCommunityTooltipYPosition, false);
	}

	public int getMainMenuUploadTooltipYPosition() {
		return setRelativeCoordinatesToDensity(mainMenuUploadTooltipYPosition, false);
	}

	public int getProjectActivitySpriteBackgroundTooltipXPosition() {
		return setRelativeCoordinatesToDensity(projectActivitySpriteBackgroundTooltipXPosition, true);
	}

	public int getProjectActivitySpriteBackgroundTooltipYPosition() {
		return setRelativeCoordinatesToDensity(projectActivitySpriteBackgroundTooltipYPosition, false);
	}

	public int getProjectActivitySpriteObjectTooltipXPosition() {
		return setRelativeCoordinatesToDensity(projectActivitySpriteObjectTooltipXPosition, true);
	}

	public int getProjectActivitySpriteObjectTooltipYPosition() {
		return setRelativeCoordinatesToDensity(projectActivitySpriteObjectTooltipYPosition, false);
	}

	public int getProjectActivityAddButtonTooltipXPosition() {
		return setRelativeCoordinatesToDensity(projectActivityAddButtonTooltipXPosition, true);
	}

	public int getProjectActivityAddButtonTooltipYPosition() {
		return setRelativeCoordinatesToDensity(projectActivityAddButtonTooltipYPosition, false);
	}

	public int getProjectActivityPlayButtonTooltipXPosition() {
		return setRelativeCoordinatesToDensity(projectActivityPlayButtonTooltipXPosition, true);
	}

	public int getProjectActivityPlayButtonTooltipYPosition() {
		return setRelativeCoordinatesToDensity(projectActivityPlayButtonTooltipYPosition, false);
	}

	public static ScreenParameters getScreenParameters() {
		return screenParameters;
	}

	public int getProgramMenuScriptsTooltipXPosition() {
		return setRelativeCoordinatesToDensity(programMenuScriptsTooltipXPosition, true);
	}

	public int getProgramMenuScriptsTooltipYPosition() {
		return setRelativeCoordinatesToDensity(programMenuScriptsTooltipYPosition, false);
	}

	public int getProgramMenuLooksTooltipXPosition() {
		return setRelativeCoordinatesToDensity(programMenuLooksTooltipXPosition, true);
	}

	public int getProgramMenuLooksTooltipYPosition() {
		return setRelativeCoordinatesToDensity(programMenuLooksTooltipYPosition, false);
	}

	public int getProgramMenuSoundsTooltipXPosition() {
		return setRelativeCoordinatesToDensity(programMenuSoundsTooltipXPosition, true);
	}

	public int getProgramMenuSoundsTooltipYPosition() {
		return setRelativeCoordinatesToDensity(programMenuSoundsTooltipYPosition, false);
	}

	public int getProgramMenuPlayButtonTooltipXPosition() {
		return setRelativeCoordinatesToDensity(programMenuPlayButtonTooltipXPosition, true);
	}

	public int getProgramMenuPlayButtonTooltipYPosition() {
		return setRelativeCoordinatesToDensity(programMenuPlayButtonTooltipYPosition, false);
	}
}
