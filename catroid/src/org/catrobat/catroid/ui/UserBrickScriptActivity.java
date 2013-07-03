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
package org.catrobat.catroid.ui;

import org.catrobat.catroid.R;
import org.catrobat.catroid.content.bricks.Brick;
import org.catrobat.catroid.content.bricks.UserBrick;
import org.catrobat.catroid.ui.adapter.BrickAdapter;

import android.util.Log;
import android.widget.ArrayAdapter;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.OnNavigationListener;

/**
 * @author forestjohnson
 * 
 */
public class UserBrickScriptActivity extends ScriptActivity {

	public static UserBrick userBrick;

	public static void setUserBrick(Brick userBrick) {
		UserBrickScriptActivity.userBrick = (UserBrick) userBrick;
	}

	@Override
	protected void onResume() {
		super.onResume();

		if (userBrick != null) {
			setupBrickAdapter();

			setupActionBar();
		} else {
			Log.d("FOREST", "UserBrickScriptActivity.onResume with null userBrick");
		}

	}

	private void setupBrickAdapter() {

		BrickAdapter adapter = getScriptFragment().getAdapter();

		adapter.setUserBrick(userBrick);
		adapter.updateBrickList();
	}

	public void setupActionBar() {

		final ActionBar actionBar = getSupportActionBar();
		actionBar.setHomeButtonEnabled(true);
		actionBar.setDisplayShowTitleEnabled(false);
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);

		String[] items = new String[2];
		items[0] = getResources().getString(R.string.scripts);

		items[1] = userBrick.getName(this);

		final ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(this,
				R.layout.activity_script_spinner_item, items);

		actionBar.setListNavigationCallbacks(spinnerAdapter, new OnNavigationListener() {
			@Override
			public boolean onNavigationItemSelected(int itemPosition, long itemId) {
				if (isHoveringActive()) {
					getScriptFragment().getListView().animateHoveringBrick();
					return true;
				}

				// TODO handle back

				return true;
			}
		});
		actionBar.setSelectedNavigationItem(1);
	}

}
