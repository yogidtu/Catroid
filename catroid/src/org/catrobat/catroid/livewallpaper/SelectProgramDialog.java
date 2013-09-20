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
package org.catrobat.catroid.livewallpaper;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;

import org.catrobat.catroid.ProjectManager;
import org.catrobat.catroid.common.Constants;
import org.catrobat.catroid.content.Project;
import org.catrobat.catroid.io.StorageHandler;
import org.catrobat.catroid.utils.UtilFile;

import java.io.File;
import java.util.List;

public class SelectProgramDialog extends Dialog {

	private Context context;
	private String selectedProject;

	public SelectProgramDialog(Context context) {
		super(context);
		this.context = context;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.dialog_lwp_select_program);
		addRadioButtons();
		addOkButton();
		addCancelButton();
	}

	private void addRadioButtons() {
		RadioGroup radioGroup = (RadioGroup) findViewById(R.id.dialog_lwp_select_project_radiogroup);

		File rootDirectory = new File(Constants.DEFAULT_ROOT);
		int numOfProjects = UtilFile.getProjectNames(rootDirectory).size();

		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
		String currentProjectName = sharedPreferences.getString(Constants.PREF_PROJECTNAME_KEY, null);

		RadioButton[] radioButton = new RadioButton[numOfProjects];
		int i = 0;
		List<String> projectNames = UtilFile.getProjectNames(rootDirectory);
		java.util.Collections.sort(projectNames);
		for (String projectName : projectNames) {
			radioButton[i] = new RadioButton(context);
			radioButton[i].setText(projectName);
			radioButton[i].setTextColor(Color.WHITE);
			radioGroup.addView(radioButton[i], i);
			if (projectName.equals(currentProjectName)) {
				radioGroup.check(radioButton[i].getId());
			}
			i++;
		}

		radioGroup.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				RadioButton checkedRadioButton = (RadioButton) findViewById(checkedId);
				selectedProject = checkedRadioButton.getText().toString();
			}
		});
	}

	private void addOkButton() {
		Button okButton = (Button) findViewById(R.id.dialog_lwp_select_project_ok_button);
		okButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {

				if (selectedProject == null) {
					dismiss();
					return;
				}

				ProjectManager projectManager = ProjectManager.getInstance();
				if (projectManager.getCurrentProject() != null
						&& projectManager.getCurrentProject().getName().equals(selectedProject)) {
					dismiss();
					return;
				}

				Project project = StorageHandler.getInstance().loadProject(selectedProject);
				if (project != null) {
					projectManager.setProject(project);
					SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
					Editor editor = sharedPreferences.edit();
					editor.putString(Constants.PREF_PROJECTNAME_KEY, selectedProject);
					editor.commit();
					LiveWallpaper.liveWallpaperEngine.changeWallpaperProgram();
					dismiss();
					//display toast

				} else {
					//display toast, not successful  - error???????
				}

			}
		});
	}

	private void addCancelButton() {
		Button cancelButton = (Button) findViewById(R.id.dialog_lwp_select_project_cancel_button);
		cancelButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				dismiss();
			}
		});
	}
}
