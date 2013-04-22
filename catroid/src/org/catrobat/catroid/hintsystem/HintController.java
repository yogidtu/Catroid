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
import org.catrobat.catroid.ui.fragment.AddBrickFragment;
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

	public HintController() {
		allHints = new ArrayList<HintObject>();
	}

	public ArrayList<HintObject> getHints() {
		allHints.clear();

		switch (checkActivity()) {
			case 0:
				if (!Hint.welcome) {
					getWelcomeHint();

				} else {
					getMainMenuHints();
				}
				break;
			case 1:
				getProjectHints();
				break;
			case 2:
				getMyProjectsHints();
				break;
			case 3:
				getProgramMenuHints();
				break;
			case 4:
				getScriptHints();
				break;
			case 5:
				getSettingsHints();
				break;
		}
		return allHints;
	}

	private HintObject createHint(int[] coordinates, String text) {
		HintObject hint = new HintObject(coordinates, text);
		return hint;
	}

	private void getWelcomeHint() {
		Activity activity = (Activity) context;
		int[] coord = { 0, 0, 0 };
		Resources resources = activity.getResources();
		String[] hintStrings = resources.getStringArray(R.array.hints_mainmenu);

		coord = examineCoordinates(activity.findViewById(R.id.main_menu));
		allHints.add(createHint(coord, hintStrings[0]));

	}

	private void getMainMenuHints() {
		Activity activity = (Activity) context;
		int[] coord = { 0, 0, 0 };
		Resources resources = activity.getResources();
		String[] hintStrings = resources.getStringArray(R.array.hints_mainmenu);

		coord = examineCoordinates(activity.findViewById(R.id.main_menu_button_continue));
		allHints.add(createHint(coord, hintStrings[1]));
		coord = examineCoordinates(activity.findViewById(R.id.main_menu_button_new));
		allHints.add(createHint(coord, hintStrings[2]));
		coord = examineCoordinates(activity.findViewById(R.id.main_menu_button_programs));
		allHints.add(createHint(coord, hintStrings[3]));
		coord = examineCoordinates(activity.findViewById(R.id.main_menu_button_forum));
		allHints.add(createHint(coord, hintStrings[4]));
		coord = examineCoordinates(activity.findViewById(R.id.main_menu_button_upload));
		allHints.add(createHint(coord, hintStrings[5]));

	}

	private void getProjectHints() {
		Activity activity = (Activity) context;
		int[] coord = { 0, 0, 0 };
		Resources resources = activity.getResources();
		String[] hintStrings = resources.getStringArray(R.array.hints_project);

		coord = examineCoordinates(activity.findViewById(R.id.spritelist_item_background));
		allHints.add(createHint(coord, hintStrings[0]));
		coord = examineCoordinates(activity.findViewById(R.id.fragment_sprites_list));
		allHints.add(createHint(coord, hintStrings[1]));
		coord = examineCoordinates(activity.findViewById(R.id.button_add));
		allHints.add(createHint(coord, hintStrings[2]));
		coord = examineCoordinates(activity.findViewById(R.id.button_play));
		allHints.add(createHint(coord, hintStrings[3]));
	}

	private void getMyProjectsHints() {
		Activity activity = (Activity) context;
		int[] coord = { 0, 0, 0 };
		Resources resources = activity.getResources();
		String[] hintStrings = resources.getStringArray(R.array.hints_myprojects);

		coord = examineCoordinates(activity.findViewById(R.id.my_projects_activity_project_title));
		allHints.add(createHint(coord, hintStrings[0]));
		coord = examineCoordinates(activity.findViewById(R.id.button_add));
		allHints.add(createHint(coord, hintStrings[1]));

	}

	private void getProgramMenuHints() {
		Activity activity = (Activity) context;
		int[] coord = { 0, 0, 0 };
		Resources resources = activity.getResources();
		String[] hintStrings = resources.getStringArray(R.array.hints_programmenu);

		coord = examineCoordinates(activity.findViewById(R.id.program_menu_button_scripts));
		allHints.add(createHint(coord, hintStrings[0]));
		coord = examineCoordinates(activity.findViewById(R.id.program_menu_button_looks));
		allHints.add(createHint(coord, hintStrings[1]));
		coord = examineCoordinates(activity.findViewById(R.id.program_menu_button_sounds));
		allHints.add(createHint(coord, hintStrings[2]));
		coord = examineCoordinates(activity.findViewById(R.id.button_play));
		allHints.add(createHint(coord, hintStrings[3]));
	}

	private void getScriptHints() {
		ScriptActivity activity = (ScriptActivity) context;
		Bundle bundle = activity.getIntent().getExtras();
		Fragment isBrickCategory = activity.getSupportFragmentManager().findFragmentByTag(
				BrickCategoryFragment.BRICK_CATEGORY_FRAGMENT_TAG);
		Fragment isAddBrickFragment = activity.getSupportFragmentManager().findFragmentByTag(
				AddBrickFragment.ADD_BRICK_FRAGMENT_TAG);
		if (isBrickCategory != null && isAddBrickFragment == null) {
			getBrickCategoryHints();
		} else if (isAddBrickFragment != null) {
			getAddBrickHints();
		} else {
			if (bundle.getInt(ScriptActivity.EXTRA_FRAGMENT_POSITION, ScriptActivity.FRAGMENT_SCRIPTS) == 0) {
				getScriptingHints();
			} else if (bundle.getInt(ScriptActivity.EXTRA_FRAGMENT_POSITION, ScriptActivity.FRAGMENT_LOOKS) == 1) {
				getLooksHints();
			} else if (bundle.getInt(ScriptActivity.EXTRA_FRAGMENT_POSITION, ScriptActivity.FRAGMENT_SOUNDS) == 2) {
				getSoundsHints();
			}
		}
	}

	private void getScriptingHints() {
		Activity activity = (Activity) context;
		int[] coord = { 0, 0, 0 };
		Resources resources = activity.getResources();
		String[] hintStrings = resources.getStringArray(R.array.hints_scripts);

		coord = examineCoordinates(activity.findViewById(R.id.brick_list_view));
		allHints.add(createHint(coord, hintStrings[0]));
		coord = examineCoordinates(activity.findViewById(R.id.button_add));
		allHints.add(createHint(coord, hintStrings[1]));
		coord = examineCoordinates(activity.findViewById(R.id.button_play));
		allHints.add(createHint(coord, hintStrings[2]));
	}

	private void getLooksHints() {
		Activity activity = (Activity) context;
		int[] coord = { 0, 0, 0 };
		Resources resources = activity.getResources();
		String[] hintStrings = resources.getStringArray(R.array.hints_looks);

		View v = activity.findViewById(R.id.fragment_look_item_relative_layout);

		coord = examineCoordinates(activity.findViewById(R.id.script_fragment_container));
		allHints.add(createHint(coord, hintStrings[0]));
		coord = examineCoordinates(activity.findViewById(R.id.button_add));
		allHints.add(createHint(coord, hintStrings[1]));
		coord = examineCoordinates(activity.findViewById(R.id.button_play));
		allHints.add(createHint(coord, hintStrings[2]));

	}

	private void getSoundsHints() {
		Activity activity = (Activity) context;
		int[] coord = { 0, 0, 0 };
		Resources resources = activity.getResources();
		String[] hintStrings = resources.getStringArray(R.array.hints_sounds);

		coord = examineCoordinates(activity.findViewById(R.id.fragment_sound_relative_layout));
		allHints.add(createHint(coord, hintStrings[0]));
		coord = examineCoordinates(activity.findViewById(R.id.button_add));
		allHints.add(createHint(coord, hintStrings[1]));
		coord = examineCoordinates(activity.findViewById(R.id.button_play));
		allHints.add(createHint(coord, hintStrings[2]));

	}

	private void getBrickCategoryHints() {
		ScriptActivity activity = (ScriptActivity) context;
		int[] coord = { 0, 0, 0 };
		Resources resources = activity.getResources();
		String[] hintStrings = resources.getStringArray(R.array.hints_brick_categories);

		SherlockListFragment currentFragment = (SherlockListFragment) activity.getSupportFragmentManager()
				.findFragmentByTag(BrickCategoryFragment.BRICK_CATEGORY_FRAGMENT_TAG);
		ListView listView = currentFragment.getListView();

		for (int i = 0; i < listView.getChildCount(); i++) {
			coord = examineListCoordinates(listView.getChildAt(i));
			allHints.add(createHint(coord, hintStrings[i]));
		}

	}

	private void getAddBrickHints() {
		switch (checkCategory()) {
			case 0:
				getBrickHints(R.array.hints_brick_control);
				break;

			case 1:
				getBrickHints(R.array.hints_brick_motion);
				break;

			case 2:
				getBrickHints(R.array.hints_brick_sounds);
				break;

			case 3:
				getBrickHints(R.array.hints_brick_looks);
				break;

			case 4:
				getBrickHints(R.array.hints_brick_variables);
				break;

			case 5:
				getBrickHints(R.array.hints_brick_lego);
				break;

		}

	}

	private void getSettingsHints() {

	}

	private void getBrickHints(int id) {
		int[] coord = { 0, 0, 0 };
		ScriptActivity activity = (ScriptActivity) context;
		AddBrickFragment fragment = (AddBrickFragment) activity.getSupportFragmentManager().findFragmentByTag(
				AddBrickFragment.ADD_BRICK_FRAGMENT_TAG);
		Resources resources = fragment.getActivity().getResources();
		ListView listView = fragment.getListView();

		String[] hintStrings = resources.getStringArray(id);
		for (int i = 0; i < listView.getChildCount() - 1; i++) {
			coord = examineListCoordinates(listView.getChildAt(i));
			allHints.add(createHint(coord, hintStrings[i]));
		}

	}

	private int checkCategory() {
		ScriptActivity activity = (ScriptActivity) context;
		AddBrickFragment fragment = (AddBrickFragment) activity.getSupportFragmentManager().findFragmentByTag(
				AddBrickFragment.ADD_BRICK_FRAGMENT_TAG);
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

	private int checkActivity() {
		Activity activity = (Activity) context;

		if (activity.getLocalClassName().compareTo("ui.MainMenuActivity") == 0) {
			return 0;
		} else if (activity.getLocalClassName().compareTo("ui.ProjectActivity") == 0) {
			return 1;
		} else if (activity.getLocalClassName().compareTo("ui.MyProjectsActivity") == 0) {
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

	private int[] examineListCoordinates(View view) {
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

	private int[] examineCoordinates(View view) {
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

	private int[] normalizeCoordinates(int[] coordinates) {
		float x = (float) coordinates[0] / Hint.getInstance().getScreenWidth() * 100;
		float y = (float) coordinates[1] / Hint.getInstance().getScreenHeight() * 100;

		coordinates[0] = (int) x;
		coordinates[1] = (int) y;
		return coordinates;
	}

	public void setContext(Context con) {
		this.context = con;
	}

}
