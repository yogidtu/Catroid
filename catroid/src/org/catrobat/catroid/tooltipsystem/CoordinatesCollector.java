/**
 *  Catroid: An on-device visual programming system for Android devices
 *  Copyright (C) 2010-2013 The Catrobat Team
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

import android.view.MotionEvent;

/**
 * @author amore
 * 
 */
public class CoordinatesCollector {
	private ScreenParameters screenParameters;

	public CoordinatesCollector() {
		screenParameters = ScreenParameters.getInstance();
	}

	public boolean isActionBarClicked(MotionEvent ev) {
		if (ev.getY() > screenParameters.getActionBarMenuHeight()) {
			return false;
		} else {
			return true;
		}
	}

	public boolean isMenuButtonActionBarPosition(MotionEvent ev) {
		if (ev.getX() > screenParameters.getActionBarMenuXPosition()
				&& ev.getX() < screenParameters.getActionBarMenuXPosition() + screenParameters.getActionBarMenuWidth()) {
			if (ev.getY() > screenParameters.getActionBarMenuYPosition()
					&& ev.getY() < screenParameters.getActionBarMenuYPosition()
							+ screenParameters.getActionBarMenuHeight()) {
				return true;
			}
		}
		return false;
	}

	public boolean isOnMainMenuActivityContinueTooltipPosition(MotionEvent ev) {
		if (ev.getX() > screenParameters.getMainMenuTooltipXPosition()
				&& ev.getX() < screenParameters.getMainMenuTooltipXPosition() + screenParameters.getTooltipWidth()) {
			if (ev.getY() > screenParameters.getMainMenuContinueTooltipYPosition()
					&& ev.getY() < screenParameters.getMainMenuContinueTooltipYPosition()
							+ screenParameters.getTooltipHeight()) {
				return true;
			}
		}
		return false;
	}

	public boolean isOnMainMenuActivityNewTooltipPosition(MotionEvent ev) {
		if (ev.getX() > screenParameters.getMainMenuTooltipXPosition()
				&& ev.getX() < screenParameters.getMainMenuTooltipXPosition() + screenParameters.getTooltipWidth()) {
			if (ev.getY() > screenParameters.getMainMenuNewTooltipYPosition()
					&& ev.getY() < screenParameters.getMainMenuNewTooltipYPosition()
							+ screenParameters.getTooltipHeight()) {
				return true;
			}
		}
		return false;
	}

	public boolean isOnMainMenuActivityProgramsTooltipPosition(MotionEvent ev) {
		if (ev.getX() > screenParameters.getMainMenuTooltipXPosition()
				&& ev.getX() < screenParameters.getMainMenuTooltipXPosition() + screenParameters.getTooltipWidth()) {
			if (ev.getY() > screenParameters.getMainMenuProgramsTooltipYPosition()
					&& ev.getY() < screenParameters.getMainMenuProgramsTooltipYPosition()
							+ screenParameters.getTooltipHeight()) {
				return true;
			}
		}
		return false;
	}

	public boolean isOnMainMenuActivityForumTooltipPosition(MotionEvent ev) {
		if (ev.getX() > screenParameters.getMainMenuTooltipXPosition()
				&& ev.getX() < screenParameters.getMainMenuTooltipXPosition() + screenParameters.getTooltipWidth()) {
			if (ev.getY() > screenParameters.getMainMenuForumTooltipYPosition()
					&& ev.getY() < screenParameters.getMainMenuForumTooltipYPosition()
							+ screenParameters.getTooltipHeight()) {
				return true;
			}
		}
		return false;
	}

	public boolean isOnMainMenuActivityWebTooltipPosition(MotionEvent ev) {
		if (ev.getX() > screenParameters.getMainMenuTooltipXPosition()
				&& ev.getX() < screenParameters.getMainMenuTooltipXPosition() + screenParameters.getTooltipWidth()) {
			if (ev.getY() > screenParameters.getMainMenuCommunityTooltipYPosition()
					&& ev.getY() < screenParameters.getMainMenuCommunityTooltipYPosition()
							+ screenParameters.getTooltipHeight()) {
				return true;
			}
		}
		return false;
	}

	public boolean isOnMainMenuActivityUploadTooltipPosition(MotionEvent ev) {
		if (ev.getX() > screenParameters.getMainMenuTooltipXPosition()
				&& ev.getX() < screenParameters.getMainMenuTooltipXPosition() + screenParameters.getTooltipWidth()) {
			if (ev.getY() > screenParameters.getMainMenuUploadTooltipYPosition()
					&& ev.getY() < screenParameters.getMainMenuUploadTooltipYPosition()
							+ screenParameters.getTooltipHeight()) {
				return true;
			}
		}
		return false;
	}

	public boolean isOnProjectActivityPlayButtonTooltipPosition(MotionEvent ev) {
		if (ev.getY() > screenParameters.getProjectActivityAddButtonTooltipYPosition()
				&& ev.getY() < screenParameters.getProjectActivityAddButtonTooltipYPosition()
						+ screenParameters.getTooltipHeight()) {
			if (ev.getX() > screenParameters.getProjectActivityPlayButtonTooltipXPosition()
					&& ev.getX() < screenParameters.getProjectActivityPlayButtonTooltipXPosition()
							+ screenParameters.getTooltipWidth()) {
				return true;
			}
		}
		return false;
	}

	public boolean isOnProjectActivityAddButtonTooltipPosition(MotionEvent ev) {
		if (ev.getY() > screenParameters.getProjectActivityAddButtonTooltipYPosition()
				&& ev.getY() < screenParameters.getProjectActivityAddButtonTooltipYPosition()
						+ screenParameters.getTooltipHeight()) {
			if (ev.getX() > screenParameters.getProjectActivityAddButtonTooltipXPosition()
					&& ev.getX() < screenParameters.getProjectActivityAddButtonTooltipXPosition()
							+ screenParameters.getTooltipWidth()) {
				return true;
			}
		}
		return false;
	}

	public boolean isOnProjectActivityBackgroundTooltipPosition(MotionEvent ev) {
		if (ev.getX() > screenParameters.getProjectActivitySpriteBackgroundTooltipXPosition()
				&& ev.getX() < screenParameters.getProjectActivitySpriteBackgroundTooltipXPosition()
						+ screenParameters.getTooltipWidth()) {
			if (ev.getY() > screenParameters.getProjectActivitySpriteBackgroundTooltipYPosition()
					&& ev.getY() < screenParameters.getProjectActivitySpriteBackgroundTooltipYPosition()
							+ screenParameters.getTooltipHeight()) {
				return true;
			}
		}
		return false;
	}

	public boolean isOnProjectActivityObjectTooltipPosition(MotionEvent ev) {
		if (ev.getX() > screenParameters.getProjectActivitySpriteBackgroundTooltipXPosition()
				&& ev.getX() < screenParameters.getProjectActivitySpriteBackgroundTooltipXPosition()
						+ screenParameters.getTooltipWidth()) {
			if (ev.getY() > screenParameters.getProjectActivitySpriteObjectTooltipYPosition()
					&& ev.getY() < screenParameters.getProjectActivitySpriteObjectTooltipYPosition()
							+ screenParameters.getTooltipHeight()) {
				return true;
			}
		}
		return false;
	}

	public boolean isOnProgramMenuActivityScriptsTooltipPosition(MotionEvent ev) {
		if (ev.getX() > screenParameters.getProgramMenuScriptsTooltipXPosition()
				&& ev.getX() < screenParameters.getProgramMenuScriptsTooltipXPosition()
						+ screenParameters.getTooltipWidth()) {
			if (ev.getY() > screenParameters.getProgramMenuScriptsTooltipYPosition()
					&& ev.getY() < screenParameters.getProgramMenuScriptsTooltipYPosition()
							+ screenParameters.getTooltipHeight()) {
				return true;
			}
		}
		return false;
	}

	public boolean isOnProgramMenuActivityLooksTooltipPosition(MotionEvent ev) {
		if (ev.getX() > screenParameters.getProgramMenuLooksTooltipXPosition()
				&& ev.getX() < screenParameters.getProgramMenuLooksTooltipXPosition()
						+ screenParameters.getTooltipWidth()) {
			if (ev.getY() > screenParameters.getProgramMenuLooksTooltipYPosition()
					&& ev.getY() < screenParameters.getProgramMenuLooksTooltipYPosition()
							+ screenParameters.getTooltipHeight()) {
				return true;
			}
		}
		return false;
	}

	public boolean isOnProgramMenuActivitySoundsTooltipPosition(MotionEvent ev) {
		if (ev.getX() > screenParameters.getProgramMenuSoundsTooltipXPosition()
				&& ev.getX() < screenParameters.getProgramMenuSoundsTooltipXPosition()
						+ screenParameters.getTooltipWidth()) {
			if (ev.getY() > screenParameters.getProgramMenuSoundsTooltipYPosition()
					&& ev.getY() < screenParameters.getProgramMenuSoundsTooltipYPosition()
							+ screenParameters.getTooltipHeight()) {
				return true;
			}
		}
		return false;
	}

	public boolean isOnProgramMenuActivityPlayButtonTooltipPosition(MotionEvent ev) {
		if (ev.getX() > screenParameters.getProgramMenuPlayButtonTooltipXPosition()
				&& ev.getX() < screenParameters.getProgramMenuPlayButtonTooltipXPosition()
						+ screenParameters.getTooltipWidth()) {
			if (ev.getY() > screenParameters.getProgramMenuPlayButtonTooltipYPosition()
					&& ev.getY() < screenParameters.getProgramMenuPlayButtonTooltipYPosition()
							+ screenParameters.getTooltipHeight()) {
				return true;
			}
		}
		return false;
	}

}
