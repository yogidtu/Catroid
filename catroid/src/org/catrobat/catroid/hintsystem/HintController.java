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

import org.catrobat.catroid.ProjectManager;
import org.catrobat.catroid.R;
import org.catrobat.catroid.soundrecorder.SoundRecorderActivity;
import org.catrobat.catroid.stage.StageActivity;
import org.catrobat.catroid.ui.ScriptActivity;
import org.catrobat.catroid.ui.SettingsActivity;
import org.catrobat.catroid.ui.dialogs.StageDialog;
import org.catrobat.catroid.ui.fragment.AddBrickFragment;
import org.catrobat.catroid.ui.fragment.BrickCategoryFragment;
import org.catrobat.catroid.ui.fragment.FormulaEditorFragment;
import org.catrobat.catroid.ui.fragment.LookFragment;
import org.catrobat.catroid.ui.fragment.ScriptFragment;
import org.catrobat.catroid.ui.fragment.SoundFragment;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.PreferenceManager;
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
			case 6:
				getStageHints();
				break;
			case 7:
				getSoundRecorderHints();
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
		coord = examineCoordinates(activity.findViewById(R.id.main_menu_button_web));
		allHints.add(createHint(coord, hintStrings[5]));
		coord = examineCoordinates(activity.findViewById(R.id.main_menu_button_upload));
		allHints.add(createHint(coord, hintStrings[6]));

		setSharedPreferences("PREF_HINT_MAINMENU_ACTIVE", false);
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

		setSharedPreferences("PREF_HINT_PROJECT_ACTIVE", false);
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

		setSharedPreferences("PREF_HINT_MYPROJECTS_ACTIVE", false);
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

		setSharedPreferences("PREF_HINT_PROGRAMMENU_ACTIVE", false);
	}

	private void getScriptHints() {
		switch (checkFragment()) {
			case 0:
				getBrickCategoryHints();
				break;
			case 1:
				getAddBrickHints();
				break;
			case 2:
				getFormulaEditorHints();
				break;
			case 3:
				getScriptingHints();
				break;
			case 4:
				getLooksHints();
				break;
			case 5:
				getSoundsHints();
				break;
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

		setSharedPreferences("PREF_HINT_SCRIPTS_ACTIVE", false);
	}

	private void getLooksHints() {
		Activity activity = (Activity) context;
		int[] coord = { 0, 0, 0 };
		Resources resources = activity.getResources();
		String[] hintStrings = resources.getStringArray(R.array.hints_looks);

		coord = examineCoordinates(activity.findViewById(R.id.script_fragment_container));
		allHints.add(createHint(coord, hintStrings[0]));
		coord = examineCoordinates(activity.findViewById(R.id.button_add));
		allHints.add(createHint(coord, hintStrings[1]));
		coord = examineCoordinates(activity.findViewById(R.id.button_play));
		allHints.add(createHint(coord, hintStrings[2]));

		setSharedPreferences("PREF_HINT_LOOKS_ACTIVE", false);

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

		setSharedPreferences("PREF_HINT_SOUNDS_ACTIVE", false);
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

		setSharedPreferences("PREF_HINT_BRICKCATEGORY_ACTIVE", false);
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
		int[] coord = { 0, 0, 0 };
		SettingsActivity activity = (SettingsActivity) context;
		ListView listView = activity.getListView();
		Resources resources = activity.getResources();
		String[] hintStrings = resources.getStringArray(R.array.hints_settings);
		coord = examineListCoordinates(listView.getChildAt(0));
		allHints.add(createHint(coord, hintStrings[0]));

		setSharedPreferences("PREF_HINT_SETTINGS_ACTIVE", false);
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

		setSharedPreferences("PREF_HINT_ADDBRICK_ACTIVE", false);

	}

	private void getStageHints() {

		int[] coord = { 0, 0, 0 };
		StageActivity activity = (StageActivity) context;
		StageDialog dialog = activity.getStageDialog();
		Resources resources = activity.getResources();
		String[] hintStrings = resources.getStringArray(R.array.hints_stage);

		if (!dialog.isShowing()) {
			coord = examineStageCoordinates();
			allHints.add(createHint(coord, hintStrings[0]));

			setSharedPreferences("PREF_HINT_STAGE_ACTIVE", false);
		} else {
			coord = examineCoordinates(dialog.findViewById(R.id.stage_dialog_button_back));
			allHints.add(createHint(coord, hintStrings[1]));
			coord = examineCoordinates(dialog.findViewById(R.id.stage_dialog_button_continue));
			allHints.add(createHint(coord, hintStrings[2]));
			coord = examineCoordinates(dialog.findViewById(R.id.stage_dialog_button_restart));
			allHints.add(createHint(coord, hintStrings[3]));
			coord = examineCoordinates(dialog.findViewById(R.id.stage_dialog_button_toggle_axes));
			allHints.add(createHint(coord, hintStrings[4]));
			coord = examineCoordinates(dialog.findViewById(R.id.stage_dialog_button_screenshot));
			allHints.add(createHint(coord, hintStrings[5]));

			setSharedPreferences("PREF_HINT_STAGEDIALOG_ACTIVE", false);

		}

	}

	private void getFormulaEditorHints() {
		int[] coord = { 0, 0, 0 };
		ScriptActivity activity = (ScriptActivity) context;
		FormulaEditorFragment fragment = (FormulaEditorFragment) activity.getSupportFragmentManager()
				.findFragmentByTag(FormulaEditorFragment.FORMULA_EDITOR_FRAGMENT_TAG);
		Resources resources = fragment.getActivity().getResources();
		String[] hintStrings = resources.getStringArray(R.array.hints_formula_editor);

		coord = examineCoordinates(activity.findViewById(R.id.formula_editor_keyboard_compute));
		allHints.add(createHint(coord, hintStrings[0]));

		setSharedPreferences("PREF_HINT_FORMULAEDITOR_ACTIVE", false);
	}

	private void getSoundRecorderHints() {
		int[] coord = { 0, 0, 0 };
		SoundRecorderActivity activity = (SoundRecorderActivity) context;
		Resources resources = activity.getResources();
		String[] hintStrings = resources.getStringArray(R.array.hints_sound_recorder);

		coord = examineCoordinates(activity.findViewById(R.id.soundrecorder_record_button));
		allHints.add(createHint(coord, hintStrings[0]));

		setSharedPreferences("PREF_HINT_SOUNDRECORDER_ACTIVE", false);

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

	public int checkActivity() {
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
		} else if (activity.getLocalClassName().compareTo("stage.StageActivity") == 0) {
			return 6;
		} else if (activity.getLocalClassName().compareTo("soundrecorder.SoundRecorderActivity") == 0) {
			return 7;
		}
		return -1;
	}

	public int checkFragment() {
		int fragmentNumber = -1;
		ScriptActivity activity = (ScriptActivity) context;

		Fragment isBrickCategory = activity.getSupportFragmentManager().findFragmentByTag(
				BrickCategoryFragment.BRICK_CATEGORY_FRAGMENT_TAG);
		Fragment isAddBrickFragment = activity.getSupportFragmentManager().findFragmentByTag(
				AddBrickFragment.ADD_BRICK_FRAGMENT_TAG);
		Fragment isFormulaEditor = activity.getSupportFragmentManager().findFragmentByTag(
				FormulaEditorFragment.FORMULA_EDITOR_FRAGMENT_TAG);
		Fragment isScriptFragment = activity.getSupportFragmentManager().findFragmentByTag(ScriptFragment.TAG);
		Fragment isLooksFragment = activity.getSupportFragmentManager().findFragmentByTag(LookFragment.TAG);
		Fragment isSoundFragment = activity.getSupportFragmentManager().findFragmentByTag(SoundFragment.TAG);

		if (isBrickCategory != null && isAddBrickFragment == null) {
			fragmentNumber = 0;
		} else if (isAddBrickFragment != null) {
			fragmentNumber = 1;
		} else if (isFormulaEditor != null) {
			fragmentNumber = 2;
		} else if (isScriptFragment != null) {
			fragmentNumber = 3;
		} else if (isLooksFragment != null) {
			fragmentNumber = 4;
		} else if (isSoundFragment != null) {
			fragmentNumber = 5;
		}
		return fragmentNumber;

	}

	private int[] examineStageCoordinates() {
		int[] coordinates = { 0, 0, 0 };
		coordinates[0] = ProjectManager.getInstance().getCurrentProject().getXmlHeader().virtualScreenWidth / 2;
		coordinates[1] = ProjectManager.getInstance().getCurrentProject().getXmlHeader().virtualScreenHeight / 2;
		coordinates[2] = ProjectManager.getInstance().getCurrentProject().getXmlHeader().virtualScreenWidth;
		coordinates = normalizeCoordinates(coordinates);

		return coordinates;
	}

	private int[] examineListCoordinates(View view) {
		int[] coordinates = { 0, 0, 0 };
		view.getLocationInWindow(coordinates);

		int viewWidth = view.getWidth();
		int viewHeight = view.getHeight();
		coordinates[0] = (coordinates[0] + viewWidth / 3) - 25;
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
		coordinates[0] = (coordinates[0] + viewWidth / 3) - 25;
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

	public boolean getSharedPreferencesIsHintActive(String preferenceName) {
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
		boolean isHintActive = pref.getBoolean(preferenceName, true);
		return isHintActive;
	}

	public void setSharedPreferences(String preferenceName, boolean tag) {
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
		SharedPreferences.Editor sharedPreferencesEditor = preferences.edit();
		sharedPreferencesEditor.putBoolean(preferenceName, tag);
		sharedPreferencesEditor.commit();
	}

}
