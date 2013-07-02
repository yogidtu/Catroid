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
package org.catrobat.catroid.ui.fragment;

import org.catrobat.catroid.ProjectManager;
import org.catrobat.catroid.R;
import org.catrobat.catroid.content.bricks.UserBrick;
import org.catrobat.catroid.content.bricks.UserBrickUIData;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;

public class UserBrickDataEditorFragment extends SherlockFragment implements OnKeyListener {

	private static final String BRICK_DATA_EDITOR_FRAGMENT_TAG = "brick_data_editor_fragment";
	private static final String BRICK_BUNDLE_ARGUMENT = "current_brick";
	private Context context;
	private UserBrick currentBrick;
	private LinearLayout editorBrickSpace;
	private View brickView;

	private View fragmentView;

	public UserBrickDataEditorFragment() {
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);

		getSherlockActivity().getSupportActionBar().setTitle(getString(R.string.brick_data_editor_title));

		currentBrick = (UserBrick) getArguments().getSerializable(BRICK_BUNDLE_ARGUMENT);
		Log.d("FOREST", "USDB onCreate + " + currentBrick.toString());
	}

	public static void showFragment(View view, UserBrick brick) {

		SherlockFragmentActivity activity = null;
		activity = (SherlockFragmentActivity) view.getContext();

		UserBrickDataEditorFragment dataEditorFragment = (UserBrickDataEditorFragment) activity
				.getSupportFragmentManager().findFragmentByTag(BRICK_DATA_EDITOR_FRAGMENT_TAG);

		FragmentManager fragmentManager = activity.getSupportFragmentManager();
		FragmentTransaction fragTransaction = fragmentManager.beginTransaction();

		if (dataEditorFragment == null) {
			dataEditorFragment = new UserBrickDataEditorFragment();
			Bundle bundle = new Bundle();
			bundle.putSerializable(BRICK_BUNDLE_ARGUMENT, brick);
			dataEditorFragment.setArguments(bundle);

			fragTransaction.add(R.id.script_fragment_container, dataEditorFragment, BRICK_DATA_EDITOR_FRAGMENT_TAG);
			fragTransaction.hide(fragmentManager.findFragmentByTag(ScriptFragment.TAG));
			fragTransaction.show(dataEditorFragment);
			activity.findViewById(R.id.bottom_bar).setVisibility(View.GONE);

			Log.d("FOREST", "USDB onClick 11");

		} else if (dataEditorFragment.isHidden()) {
			dataEditorFragment.updateBrickView(brick);
			fragTransaction.hide(fragmentManager.findFragmentByTag(ScriptFragment.TAG));
			fragTransaction.show(dataEditorFragment);
			activity.findViewById(R.id.bottom_bar).setVisibility(View.GONE);
		} else {
			// ??
		}
		fragTransaction.commit();
	}

	public void updateBrickView() {
		updateBrickView(currentBrick);
	}

	private void updateBrickView(UserBrick newBrick) {
		currentBrick = newBrick;
		editorBrickSpace.removeAllViews();
		View newBrickView = newBrick.getView(context, 0, null);
		editorBrickSpace.addView(newBrickView, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
				LinearLayout.LayoutParams.MATCH_PARENT));
		brickView = newBrickView;

	}

	private void onUserDismiss() {
		SherlockFragmentActivity activity = getSherlockActivity();
		FragmentManager fragmentManager = activity.getSupportFragmentManager();
		FragmentTransaction fragTransaction = fragmentManager.beginTransaction();
		fragTransaction.hide(this);
		fragTransaction.show(fragmentManager.findFragmentByTag(ScriptFragment.TAG));
		fragTransaction.commit();
		activity.getSupportActionBar().setTitle(ProjectManager.getInstance().getCurrentSprite().getName());

		getSherlockActivity().getSupportActionBar().setDisplayShowTitleEnabled(false);
		getSherlockActivity().getSupportActionBar().setNavigationMode(
				com.actionbarsherlock.app.ActionBar.NAVIGATION_MODE_LIST);

		activity.findViewById(R.id.bottom_bar).setVisibility(View.VISIBLE);
		activity.findViewById(R.id.bottom_bar_separator).setVisibility(View.VISIBLE);
		activity.findViewById(R.id.button_play).setVisibility(View.VISIBLE);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		fragmentView = inflater.inflate(R.layout.fragment_brick_data_editor, container, false);
		fragmentView.setFocusableInTouchMode(true);
		fragmentView.requestFocus();

		context = getActivity();
		brickView = View.inflate(context, R.layout.brick_user, null);

		Context context = brickView.getContext();

		LinearLayout layout = (LinearLayout) brickView.findViewById(R.id.brick_user_layout);
		if (layout.getChildCount() > 0) {
			layout.removeAllViews();
		}

		for (UserBrickUIData d : currentBrick.uiData) {
			//d
		}

		editorBrickSpace = (LinearLayout) fragmentView.findViewById(R.id.brick_data_editor_brick_space);

		editorBrickSpace.addView(brickView);

		return fragmentView;
	}

	@Override
	public void onStart() {

		getView().requestFocus();
		/*
		 * View.OnTouchListener touchListener = new View.OnTouchListener() {
		 * 
		 * @Override
		 * public boolean onTouch(View view, MotionEvent event) {
		 * 
		 * 
		 * return false;
		 * }
		 * };
		 * 
		 * for (int index = 0; index < formulaEditorKeyboard.getChildCount(); index++) {
		 * LinearLayout child = (LinearLayout) formulaEditorKeyboard.getChildAt(index);
		 * for (int nestedIndex = 0; nestedIndex < child.getChildCount(); nestedIndex++) {
		 * View view = child.getChildAt(nestedIndex);
		 * view.setOnTouchListener(touchListener);
		 * }
		 * }
		 * 
		 * updateButtonViewOnKeyboard();
		 */

		super.onStart();
	}

	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		for (int index = 0; index < menu.size(); index++) {
			menu.getItem(index).setVisible(false);
		}

		getSherlockActivity().getSupportActionBar().setNavigationMode(
				com.actionbarsherlock.app.ActionBar.NAVIGATION_MODE_STANDARD);
		getSherlockActivity().getSupportActionBar().setDisplayShowTitleEnabled(true);
		getSherlockActivity().getSupportActionBar().setTitle(getString(R.string.brick_data_editor_title));

		super.onPrepareOptionsMenu(menu);
	}

	private void showToast(int ressourceId) {
		Toast.makeText(context, getString(ressourceId), Toast.LENGTH_LONG).show();
	}

	@Override
	public boolean onKey(View view, int keyCode, KeyEvent event) {
		switch (keyCode) {
			case KeyEvent.KEYCODE_BACK:
				onUserDismiss();
				return true;
		}
		return false;
	}

}
