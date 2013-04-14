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
package org.catrobat.catroid.hintsystem;

import java.util.ArrayList;

import org.catrobat.catroid.R;
import org.catrobat.catroid.ui.ScriptActivity;
import org.catrobat.catroid.ui.dialogs.AddBrickDialog;
import org.catrobat.catroid.ui.fragment.BrickCategoryFragment;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.View;
import android.widget.ListView;

import com.actionbarsherlock.app.SherlockListFragment;

/**
 * @author amore
 * 
 */
public class HintController {

	private ArrayList<HintObject> allHints;
	private Context context;

	public HintController(Context context) {
		this.context = context;
		allHints = new ArrayList<HintObject>();
	}

	public ArrayList<HintObject> getHints(Context context) {
		Activity currentActivity = (Activity) context;
		allHints.clear();

		switch (checkActivity(currentActivity)) {
			case 0:
				if (!Hint.welcome) {
					getWelcomeHint(currentActivity);

				} else {
					getMainMenuHints(currentActivity);
				}
				break;
			case 1:
				getProjectHints(currentActivity);
				break;
			case 2:
				getMyProjectsHints(currentActivity);
				break;
			case 3:
				getProgramMenuHints(currentActivity);
				break;
			case 4:
				getScriptHints(currentActivity);
				break;
			case 5:
				getSettingsHints(currentActivity);
				break;
		}
		return allHints;
	}

	private HintObject createHint(int[] coordinates, String text) {
		HintObject hint = new HintObject(coordinates, text);
		return hint;
	}

	private void getWelcomeHint(Activity activity) {
		int[] coord = { 0, 0, 0 };
		Resources resources = activity.getResources();
		String[] hintStrings = resources.getStringArray(R.array.hints_mainmenu);

		coord = examineCoordinates(activity, R.id.main_menu);
		allHints.add(createHint(coord, hintStrings[0]));

	}

	private void getMainMenuHints(Activity activity) {
		int[] coord = { 0, 0, 0 };
		Resources resources = activity.getResources();
		String[] hintStrings = resources.getStringArray(R.array.hints_mainmenu);

		coord = examineCoordinates(activity, R.id.main_menu_button_continue);
		allHints.add(createHint(coord, hintStrings[1]));
		coord = examineCoordinates(activity, R.id.main_menu_button_new);
		allHints.add(createHint(coord, hintStrings[2]));
		coord = examineCoordinates(activity, R.id.main_menu_button_programs);
		allHints.add(createHint(coord, hintStrings[3]));
		coord = examineCoordinates(activity, R.id.main_menu_button_forum);
		allHints.add(createHint(coord, hintStrings[4]));
		coord = examineCoordinates(activity, R.id.main_menu_button_upload);
		allHints.add(createHint(coord, hintStrings[5]));

	}

	private void getProjectHints(Activity activity) {
		int[] coord = { 0, 0, 0 };
		Resources resources = activity.getResources();
		String[] hintStrings = resources.getStringArray(R.array.hints_project);

		coord = examineCoordinates(activity, R.id.spritelist_item_background);
		allHints.add(createHint(coord, hintStrings[0]));
		coord = examineCoordinates(activity, R.id.fragment_sprites_list);
		allHints.add(createHint(coord, hintStrings[1]));
		coord = examineCoordinates(activity, R.id.button_add);
		allHints.add(createHint(coord, hintStrings[2]));
		coord = examineCoordinates(activity, R.id.button_play);
		allHints.add(createHint(coord, hintStrings[3]));
	}

	private void getMyProjectsHints(Activity activity) {
		int[] coord = { 0, 0, 0 };
		Resources resources = activity.getResources();
		String[] hintStrings = resources.getStringArray(R.array.hints_myprojects);

		coord = examineCoordinates(activity, R.id.my_projects_activity_project_title);
		allHints.add(createHint(coord, hintStrings[0]));
		coord = examineCoordinates(activity, R.id.button_add);
		allHints.add(createHint(coord, hintStrings[1]));

	}

	private void getProgramMenuHints(Activity activity) {
		int[] coord = { 0, 0, 0 };
		Resources resources = activity.getResources();
		String[] hintStrings = resources.getStringArray(R.array.hints_programmenu);

		coord = examineCoordinates(activity, R.id.program_menu_button_scripts);
		allHints.add(createHint(coord, hintStrings[0]));
		coord = examineCoordinates(activity, R.id.program_menu_button_looks);
		allHints.add(createHint(coord, hintStrings[1]));
		coord = examineCoordinates(activity, R.id.program_menu_button_sounds);
		allHints.add(createHint(coord, hintStrings[2]));
		coord = examineCoordinates(activity, R.id.button_play);
		allHints.add(createHint(coord, hintStrings[3]));
	}

	private void getScriptHints(Activity currentActivity) {
		Bundle bundle = currentActivity.getIntent().getExtras();
		Fragment isBrickCategory = ((ScriptActivity) currentActivity).getSupportFragmentManager().findFragmentByTag(
				BrickCategoryFragment.BRICK_CATEGORY_FRAGMENT_TAG);
		Fragment isAddBrickDialog = ((ScriptActivity) currentActivity).getSupportFragmentManager().findFragmentByTag(
				AddBrickDialog.DIALOG_FRAGMENT_TAG);
		if (isBrickCategory != null && isAddBrickDialog == null) {
			getBrickCategoryHints(currentActivity);
		} else if (isAddBrickDialog != null) {
			getAddBrickHints(currentActivity);
		} else {
			if (bundle.getInt(ScriptActivity.EXTRA_FRAGMENT_POSITION, ScriptActivity.FRAGMENT_SCRIPTS) == 0) {
				getScriptingHints(currentActivity);
			} else if (bundle.getInt(ScriptActivity.EXTRA_FRAGMENT_POSITION, ScriptActivity.FRAGMENT_LOOKS) == 1) {
				getLooksHints(currentActivity);
			} else if (bundle.getInt(ScriptActivity.EXTRA_FRAGMENT_POSITION, ScriptActivity.FRAGMENT_SOUNDS) == 2) {
				getSoundsHints(currentActivity);
			}
		}
	}

	private void getScriptingHints(Activity activity) {
		int[] coord = { 0, 0, 0 };
		Resources resources = activity.getResources();
		String[] hintStrings = resources.getStringArray(R.array.hints_scripts);

		coord = examineCoordinates(activity, R.id.brick_list_view);
		allHints.add(createHint(coord, hintStrings[0]));
		coord = examineCoordinates(activity, R.id.button_add);
		allHints.add(createHint(coord, hintStrings[1]));
		coord = examineCoordinates(activity, R.id.button_play);
		allHints.add(createHint(coord, hintStrings[2]));
	}

	private void getLooksHints(Activity activity) {
		int[] coord = { 0, 0, 0 };
		Resources resources = activity.getResources();
		String[] hintStrings = resources.getStringArray(R.array.hints_looks);

		coord = examineCoordinates(activity, R.id.fragment_look_item_relative_layout);
		allHints.add(createHint(coord, hintStrings[0]));
		coord = examineCoordinates(activity, R.id.button_add);
		allHints.add(createHint(coord, hintStrings[1]));
		coord = examineCoordinates(activity, R.id.button_play);
		allHints.add(createHint(coord, hintStrings[2]));

	}

	private void getSoundsHints(Activity activity) {
		int[] coord = { 0, 0, 0 };
		Resources resources = activity.getResources();
		String[] hintStrings = resources.getStringArray(R.array.hints_sounds);

		coord = examineCoordinates(activity, R.id.fragment_sound_relative_layout);
		allHints.add(createHint(coord, hintStrings[0]));
		coord = examineCoordinates(activity, R.id.button_add);
		allHints.add(createHint(coord, hintStrings[1]));
		coord = examineCoordinates(activity, R.id.button_play);
		allHints.add(createHint(coord, hintStrings[2]));

	}

	private void getBrickCategoryHints(Activity activity) {
		int[] coord = { 0, 0, 0 };
		Resources resources = activity.getResources();
		String[] hintStrings = resources.getStringArray(R.array.hints_brick_categories);

		ScriptActivity currentActivity = (ScriptActivity) activity;
		SherlockListFragment currentFragment = (SherlockListFragment) currentActivity.getSupportFragmentManager()
				.findFragmentByTag(BrickCategoryFragment.BRICK_CATEGORY_FRAGMENT_TAG);
		ListView listView = currentFragment.getListView();

		for (int i = 0; i < listView.getChildCount(); i++) {
			coord = examineListCoordinates(listView.getChildAt(i));
			allHints.add(createHint(coord, hintStrings[i]));
		}

	}

	private void getAddBrickHints(Activity activity) {
		ScriptActivity currentActivity = (ScriptActivity) activity;
		AddBrickDialog fragment = (AddBrickDialog) currentActivity.getSupportFragmentManager().findFragmentByTag(
				AddBrickDialog.DIALOG_FRAGMENT_TAG);

		switch (checkCategory(fragment)) {
			case 0:
				getBrickHints(fragment, R.array.hints_brick_control);
				break;

			case 1:
				getBrickHints(fragment, R.array.hints_brick_motion);
				break;

			case 2:
				getBrickHints(fragment, R.array.hints_brick_sounds);
				break;

			case 3:
				getBrickHints(fragment, R.array.hints_brick_looks);
				break;

			case 4:
				getBrickHints(fragment, R.array.hints_brick_variables);
				break;

			case 5:
				getBrickHints(fragment, R.array.hints_brick_lego);
				break;

		}

	}

	private void getSettingsHints(Activity activity) {

	}

	private void getBrickHints(AddBrickDialog fragment, int id) {
		int[] coord = { 0, 0, 0 };
		Resources resources = fragment.getActivity().getResources();
		String[] hintStrings = {};

		ListView listView = fragment.getListView();

		hintStrings = resources.getStringArray(id);
		for (int i = 0; i < listView.getChildCount() - 1; i++) {
			coord = examineListCoordinates(listView.getChildAt(i));
			allHints.add(createHint(coord, hintStrings[i]));
		}

	}

	private int checkCategory(AddBrickDialog fragment) {
		Resources resources = fragment.getActivity().getResources();
		Bundle arg = fragment.getArguments();
		String selectedCategory = arg.getString("selected_category");

		if (selectedCategory.equals(resources.getString(R.string.category_control))) {
			return 0;
		} else if (selectedCategory.equals(resources.getString(R.string.category_motion))) {
			return 1;
		} else if (selectedCategory.equals(resources.getString(R.string.category_sound))) {
			return 2;
		} else if (selectedCategory.equals(resources.getString(R.string.category_looks))) {
			return 3;
		} else if (selectedCategory.equals(resources.getString(R.string.category_variables))) {
			return 4;
		} else if (selectedCategory.equals(resources.getString(R.string.category_lego_nxt))) {
			return 5;
		}
		return -1;
	}

	private int checkActivity(Activity activity) {

		if (activity.getLocalClassName().compareTo("ui.MainMenuActivity") == 0) {
			return 0;
		} else if (activity.getLocalClassName().compareTo("ui.ProjectActivity") == 0) {
			return 1;
		} else if (activity.getLocalClassName().compareTo("ui.MyProjectActivity") == 0) {
			return 2;
		} else if (activity.getLocalClassName().compareTo("ui.ProgramMenuActivity") == 0) {
			return 3;
		} else if (activity.getLocalClassName().compareTo("ui.ScriptActivity") == 0) {
			return 4;
		} else if (activity.getLocalClassName().compareTo("ui.SettingsActivity") == 0) {
			return 5;
		}
		return -1;
	}

	public int[] examineListCoordinates(View view) {
		int[] coordinates = { 0, 0, 0 };
		view.getLocationInWindow(coordinates);

		int viewWidth = view.getWidth();
		int viewHeight = view.getHeight();
		coordinates[0] = (coordinates[0] + viewWidth / 2) - 25;
		coordinates[1] = (coordinates[1] + viewHeight / 2) - 25;
		coordinates[2] = viewWidth;
		coordinates = normalizeCoordinates(coordinates);

		return coordinates;

	}

	public int[] examineCoordinates(Activity activity, int id) {
		int[] coordinates = { 0, 0, 0 };
		View currentView = activity.findViewById(id);
		currentView.getLocationInWindow(coordinates);
		int viewWidth = currentView.getWidth();
		int viewHeight = currentView.getHeight();
		coordinates[0] = (coordinates[0] + viewWidth / 2) - 25;
		coordinates[1] = (coordinates[1] + viewHeight / 2) - 25;
		coordinates[2] = viewWidth;
		coordinates = normalizeCoordinates(coordinates);

		return coordinates;
	}

	public int[] normalizeCoordinates(int[] coordinates) {
		float x = (float) coordinates[0] / Hint.getInstance().getScreenWidth() * 100;
		float y = (float) coordinates[1] / Hint.getInstance().getScreenHeight() * 100;

		coordinates[0] = (int) x;
		coordinates[1] = (int) y;
		return coordinates;
	}

}
