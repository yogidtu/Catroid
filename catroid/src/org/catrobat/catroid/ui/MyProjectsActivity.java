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

import java.util.concurrent.locks.Lock;

import org.catrobat.catroid.R;
import org.catrobat.catroid.ui.adapter.ProjectAdapter;
import org.catrobat.catroid.ui.dialogs.NewProjectDialog;
import org.catrobat.catroid.ui.fragment.ProjectsListFragment;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

public class MyProjectsActivity extends SherlockFragment {

	private ActionBar actionBar;
	private Lock viewSwitchLock = new ViewSwitchLock();
	private ProjectsListFragment projectsListFragment;

	View rootView;
	private SherlockFragmentActivity mActivity;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);

		Log.v("reached", "till here");
		rootView = inflater.inflate(R.layout.activity_my_projects, container, false);

		projectsListFragment = (ProjectsListFragment) getChildFragmentManager().findFragmentById(
				R.id.fragment_projects_list);

		android.support.v4.app.FragmentTransaction ft = getChildFragmentManager().beginTransaction();
		projectsListFragment = new ProjectsListFragment();
		Log.v("reached111111", "till here");
		ft.replace(R.id.fragment_projects_list, projectsListFragment, "List_Fragment");
		ft.addToBackStack(null);
		ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
		ft.commit();
		Log.v("reached", "till here");

		// Button button_add = (Button)
		// getActivity().findViewById(R.id.button_add);
		// button_add.setOnClickListener(this);
		return rootView;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		// setContentView(R.layout.activity_my_projects);
		setUpActionBar();
		// setHasOptionsMenu(true);
		getActivity().findViewById(R.id.bottom_bar_separator).setVisibility(View.GONE);
		getActivity().findViewById(R.id.button_play).setVisibility(View.GONE);

	}

	// Code from Stackoverflow to reduce memory problems
	// onDestroy() and unbindDrawables() methods taken from
	// http://stackoverflow.com/a/6779067

	/*
	 * @Override public void onDestroy() { super.onDestroy();
	 * 
	 * unbindDrawables(rootView); System.gc(); }
	 * 
	 * private void unbindDrawables(View rootView) {
	 * 
	 * if (rootView.getBackground() != null) {
	 * rootView.getBackground().setCallback(null); } if (rootView instanceof
	 * ViewGroup && !(rootView instanceof AdapterView)) { for (int i = 0; i <
	 * ((ViewGroup) rootView).getChildCount(); i++) {
	 * unbindDrawables(((ViewGroup) rootView).getChildAt(i)); } ((ViewGroup)
	 * rootView).removeAllViews(); } }
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);

	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.menu_myprojects, menu);
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		handleShowDetails(projectsListFragment.getShowDetails(), menu.findItem(R.id.show_details));
		return;
	}

	/*
	 * public final void onCreateOptionsMenu(android.view.Menu menu,
	 * android.view.MenuInflater inflater) { onCreateOptionsMenu(new
	 * MenuWrapper(menu), mActivity.getSupportMenuInflater()); }
	 * 
	 * @Override public void onCreateOptionsMenu(Menu menu, MenuInflater
	 * inflater) { // Nothing to see here. }
	 * 
	 * @Override public final void onPrepareOptionsMenu(android.view.Menu menu)
	 * { onPrepareOptionsMenu(new MenuWrapper(menu)); }
	 * 
	 * @Override public void onPrepareOptionsMenu(Menu menu) { // Nothing to see
	 * here. }
	 */

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home: {
				Intent intent = new Intent(getActivity(), MainMenuActivityFragment_Shruti.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(intent);
				return true;
			}
			case R.id.copy: {
				projectsListFragment.startCopyActionMode();
				break;
			}
			case R.id.delete: {
				projectsListFragment.startDeleteActionMode();
				break;
			}
			case R.id.rename: {
				projectsListFragment.startRenameActionMode();
				break;
			}
			case R.id.show_details: {
				handleShowDetails(!projectsListFragment.getShowDetails(), item);
				break;
			}
			case R.id.settings: {
				Intent intent = new Intent(getActivity(), SettingsActivity.class);
				startActivity(intent);
				break;
			}
		}
		return super.onOptionsItemSelected(item);
	}

	private void setUpActionBar() {
		String title = getResources().getString(R.string.my_projects_activity_title);

		actionBar = getSherlockActivity().getSupportActionBar();
		actionBar.setTitle(title);
		actionBar.setHomeButtonEnabled(true);
	}

	public boolean dispatchKeyEvent(KeyEvent event) {
		if (projectsListFragment.getActionModeActive()) {
			if (event.getKeyCode() == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {
				ProjectAdapter adapter = (ProjectAdapter) projectsListFragment.getListAdapter();
				adapter.clearCheckedProjects();
			}
		}
		return dispatchKeyEvent(event);
	}

	public void handleAddButton(View view) {
		if (!viewSwitchLock.tryLock()) {
			return;
		}

		NewProjectDialog dialog = new NewProjectDialog();

		dialog.show(getFragmentManager(), NewProjectDialog.DIALOG_FRAGMENT_TAG);

	}

	private void handleShowDetails(boolean showDetails, MenuItem item) {
		projectsListFragment.setShowDetails(showDetails);

		String menuItemText = "";
		if (showDetails) {
			menuItemText = getString(R.string.hide_details);
		} else {
			menuItemText = getString(R.string.show_details);
		}
		item.setTitle(menuItemText);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.view.View.OnClickListener#onClick(android.view.View)
	 */
	/*
	 * @Override public void onClick(View v) { // TODO Auto-generated method
	 * stub handleAddButton(v);
	 * 
	 * }
	 */
	@Override
	public void onAttach(Activity activity) {
		if (!(activity instanceof SherlockFragmentActivity)) {
			throw new IllegalStateException(getClass().getSimpleName()
					+ " must be attached to a SherlockFragmentActivity.");
		}
		mActivity = (SherlockFragmentActivity) activity;

		super.onAttach(activity);
	}

	/*
	 * @Override public void onDetach() { mActivity = null; super.onDetach(); }
	 */
}
