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

import android.content.Context;
import android.view.MotionEvent;

/**
 * @author amore
 * 
 */
public class Tooltip {

	private static Tooltip tooltip = new Tooltip();
	private static Context context;

	static TooltipController controller = new TooltipController();

	public static Tooltip getInstance() {

		if (tooltip == null) {
			tooltip = new Tooltip();
		}
		return tooltip;
	}

	public static void setContext(Context con) {
		context = con;
		controller.setContext(context);
	}

	public void startTooltipSystem() {
		controller.startTooltipSystem();
	}

	public void stopTooltipSystem() {
		controller.stopTooltipSystem();
	}

	public boolean setTooltipPosition(int x, int y, String text) {
		return controller.setTooltipPosition(x, y, text);

	}

	public static ToolTipObject getTooltip(int id) {
		return controller.getTooltip(id);
	}

	public int getScreenHeight() {
		return controller.getScreenHeight();
	}

	public int getScreenWidth() {
		return controller.getScreenWidth();
	}

	public boolean dispatchTouchEvent(MotionEvent ev) {
		return controller.dispatchTouchEvent(ev);

	}

	public int checkActivity() {
		return controller.checkActivity();
	}

}
