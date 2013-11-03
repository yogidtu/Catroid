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

import android.app.AlertDialog;
import android.app.ListFragment;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.catrobat.catroid.ProjectManager;
import org.catrobat.catroid.common.Constants;
import org.catrobat.catroid.common.ProjectData;
import org.catrobat.catroid.content.Project;
import org.catrobat.catroid.io.StorageHandler;
import org.catrobat.catroid.ui.adapter.ProjectAdapter;
import org.catrobat.catroid.ui.adapter.ProjectAdapter.OnProjectClickedListener;
import org.catrobat.catroid.ui.dialogs.CustomAlertDialogBuilder;
import org.catrobat.catroid.utils.UtilFile;
import org.catrobat.catroid.utils.Utils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class SelectProgramFragment extends ListFragment implements OnProjectClickedListener {
	private String selectedProject;
	private SelectProgramFragment selectProgramFragment;

	private List<ProjectData> projectList;
	private ProjectAdapter adapter;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		selectProgramFragment = this;
		getActivity().getActionBar().setTitle(R.string.lwp_select_program);
		return inflater.inflate(R.layout.fragment_lwp_select_program, container, false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		initListeners();
	}

	private void initListeners() {
		File rootDirectory = new File(Constants.DEFAULT_ROOT);
		File projectCodeFile;
		projectList = new ArrayList<ProjectData>();
		for (String projectName : UtilFile.getProjectNames(rootDirectory)) {
			projectCodeFile = new File(Utils.buildPath(Utils.buildProjectPath(projectName), Constants.PROJECTCODE_NAME));
			projectList.add(new ProjectData(projectName, projectCodeFile.lastModified()));
		}

		Collections.sort(projectList, new SortIgnoreCase());

		adapter = new ProjectAdapter(getActivity(), R.layout.activity_my_projects_list_item,
				R.id.my_projects_activity_project_title, projectList);
		setListAdapter(adapter);
		initClickListener();
	}

	private void initClickListener() {
		adapter.setOnProjectEditListener(this);
	}

	private class SortIgnoreCase implements Comparator<ProjectData> {
		@Override
		public int compare(ProjectData o1, ProjectData o2) {
			String s1 = o1.projectName;
			String s2 = o2.projectName;
			return s1.toLowerCase(Locale.getDefault()).compareTo(s2.toLowerCase(Locale.getDefault()));
		}
	}

	private class LoadProject extends AsyncTask<Void, Void, Void> {
		private ProgressDialog progress;

		public LoadProject() {
			progress = new ProgressDialog(getActivity());
			progress.setTitle(getActivity().getString(R.string.please_wait));
			progress.setMessage(getActivity().getString(R.string.loading));
			progress.setCancelable(false);
		}

		@Override
		protected void onPreExecute() {
			progress.show();
			super.onPreExecute();
		}

		@Override
		protected Void doInBackground(Void... params) {
			Project project = StorageHandler.getInstance().loadProject(selectedProject);
			if (project != null) {
				ProjectManager projectManager = ProjectManager.getInstance();
				if (projectManager.getCurrentProject() != null
						&& projectManager.getCurrentProject().getName().equals(selectedProject)) {
					getFragmentManager().beginTransaction().remove(selectProgramFragment).commit();
					getFragmentManager().popBackStack();
					return null;
				}
				projectManager.setProject(project);
				SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
				Editor editor = sharedPreferences.edit();
				editor.putString(Constants.PREF_PROJECTNAME_KEY, selectedProject);
				editor.commit();
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			LiveWallpaper.changeWallpaperProgram();
			getFragmentManager().beginTransaction().remove(selectProgramFragment).commit();
			getFragmentManager().popBackStack();
			if (progress.isShowing()) {
				progress.dismiss();
			}
			super.onPostExecute(result);
		}
	}

	@Override
	public void onProjectChecked() {
	}

	@Override
	public void onProjectClicked(int position) {
		selectedProject = projectList.get(position).projectName;

		AlertDialog.Builder builder = new CustomAlertDialogBuilder(getActivity());
		builder.setTitle(selectedProject);
		builder.setMessage(R.string.lwp_confirm_set_program_message);
		builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
			}
		});

		builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				new LoadProject().execute();
			}
		});
		AlertDialog alertDialog = builder.create();
		alertDialog.show();
	}
}
