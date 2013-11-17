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
package org.catrobat.catroid.standalone.tictactoe_master.ui;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.TextAppearanceSpan;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

import org.catrobat.catroid.standalone.tictactoe_master.ProjectManager;
import org.catrobat.catroid.standalone.tictactoe_master.R;
import org.catrobat.catroid.standalone.tictactoe_master.common.Constants;
import org.catrobat.catroid.standalone.tictactoe_master.formulaeditor.SensorHandler;
import org.catrobat.catroid.standalone.tictactoe_master.io.LoadProjectTask;
import org.catrobat.catroid.standalone.tictactoe_master.io.LoadProjectTask.OnLoadProjectCompleteListener;
import org.catrobat.catroid.standalone.tictactoe_master.stage.PreStageActivity;
import org.catrobat.catroid.standalone.tictactoe_master.stage.StageActivity;
import org.catrobat.catroid.standalone.tictactoe_master.transfers.CheckTokenTask;
import org.catrobat.catroid.standalone.tictactoe_master.transfers.CheckTokenTask.OnCheckTokenCompleteListener;
import org.catrobat.catroid.standalone.tictactoe_master.ui.controller.BackPackListManager;
import org.catrobat.catroid.standalone.tictactoe_master.ui.dialogs.AboutDialogFragment;
import org.catrobat.catroid.standalone.tictactoe_master.ui.dialogs.LoginRegisterDialog;
import org.catrobat.catroid.standalone.tictactoe_master.ui.dialogs.NewProjectDialog;
import org.catrobat.catroid.standalone.tictactoe_master.ui.dialogs.UploadProjectDialog;
import org.catrobat.catroid.standalone.tictactoe_master.utils.DownloadUtil;
import org.catrobat.catroid.standalone.tictactoe_master.utils.StatusBarNotificationManager;
import org.catrobat.catroid.standalone.tictactoe_master.utils.UtilFile;
import org.catrobat.catroid.standalone.tictactoe_master.utils.UtilZip;
import org.catrobat.catroid.standalone.tictactoe_master.utils.Utils;
import org.catrobat.catroid.standalone.tictactoe_master.web.ServerCalls;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.locks.Lock;

public class MainMenuActivity extends BaseActivity implements OnCheckTokenCompleteListener,
		OnLoadProjectCompleteListener {

	public static final String SHARED_PREFERENCES_SHOW_BROWSER_WARNING = "shared_preferences_browser_warning";

	private static final String TYPE_FILE = "file";
	private static final String TYPE_HTTP = "http";

	private static final String START_PROJECT = "Tic-Tac-Toe Master";
	private static final Boolean STANDALONE_MODE = true;

	private static final String ZIP_FILE_NAME = "tic_tac_toe_master.zip";

	private Lock viewSwitchLock = new ViewSwitchLock();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (!Utils.checkForExternalStorageAvailableAndDisplayErrorIfNot(this)) {
			return;
		}

		Utils.updateScreenWidthAndHeight(this);

		if (STANDALONE_MODE) {
			setContentView(R.layout.activity_main_menu_splashscreen);
			unzipProgramme();
		} else {
			setContentView(R.layout.activity_main_menu);

			final ActionBar actionBar = getSupportActionBar();
			actionBar.setDisplayUseLogoEnabled(true);
			actionBar.setTitle(R.string.app_name);

			findViewById(R.id.main_menu_button_continue).setEnabled(false);

			// Load external project from URL or local file system.
			Uri loadExternalProjectUri = getIntent().getData();
			getIntent().setData(null);

			if (loadExternalProjectUri != null) {
				loadProgramFromExternalSource(loadExternalProjectUri);
			}

			if (!BackPackListManager.isBackpackFlag()) {
				BackPackListManager.getInstance().setSoundInfoArrayListEmpty();
			}
		}

	}

	private void unzipProgramme() {

		//if (!ProjectManager.getInstance().canLoadProject(START_PROJECT)) {
		copyProgramZip();
		String zipFileString = Constants.DEFAULT_ROOT + "/" + ZIP_FILE_NAME;
		UtilZip.unZipFile(zipFileString, Constants.DEFAULT_ROOT);

		loadStageProject(START_PROJECT);

		File zipFile = new File(zipFileString);
		if (zipFile.exists()) {
			zipFile.delete();
		}
	}

	private void copyProgramZip() {
		AssetManager assetManager = getResources().getAssets();
		String[] files = null;
		try {
			files = assetManager.list("");
		} catch (IOException e) {
			Log.e("tag", "Failed to get asset file list.", e);
		}
		for (String filename : files) {
			if (filename.contains(ZIP_FILE_NAME)) {
				InputStream in = null;
				OutputStream out = null;
				try {
					in = assetManager.open(filename);
					File outFile = new File(Constants.DEFAULT_ROOT, filename);
					out = new FileOutputStream(outFile);
					copyFile(in, out);
					in.close();
					in = null;
					out.flush();
					out.close();
					out = null;
				} catch (IOException e) {
					Log.e("tag", "Failed to copy asset file: " + filename, e);
				}
			}
		}
	}

	private void copyFile(InputStream in, OutputStream out) throws IOException {
		byte[] buffer = new byte[1024];
		int read;
		while ((read = in.read(buffer)) != -1) {
			out.write(buffer, 0, read);
		}
	}

	private void loadStageProject(String projectName) {
		LoadProjectTask loadProjectTask = new LoadProjectTask(this, START_PROJECT, false, false);
		loadProjectTask.setOnLoadProjectCompleteListener(this);
		loadProjectTask.execute();
	}

	@Override
	protected void onResume() {
		super.onResume();

		if (!Utils.checkForExternalStorageAvailableAndDisplayErrorIfNot(this)) {
			return;
		}

		UtilFile.createStandardProjectIfRootDirectoryIsEmpty(this);
		PreStageActivity.shutdownPersistentResources();
		if (!STANDALONE_MODE) {
			setMainMenuButtonContinueText();
			findViewById(R.id.main_menu_button_continue).setEnabled(true);
			String projectName = getIntent().getStringExtra(StatusBarNotificationManager.EXTRA_PROJECT_NAME);
			if (projectName != null) {
				loadProjectInBackground(projectName);
			}
			getIntent().removeExtra(StatusBarNotificationManager.EXTRA_PROJECT_NAME);
		}
	}

	private void startStageProject() {
		ProjectManager.getInstance().getCurrentProject().getUserVariables().resetAllUserVariables();
		Intent intent = new Intent(this, PreStageActivity.class);
		startActivityForResult(intent, PreStageActivity.REQUEST_RESOURCES_INIT);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == PreStageActivity.REQUEST_RESOURCES_INIT && resultCode == RESULT_OK) {
			SensorHandler.startSensorListener(this);

			Intent intent = new Intent(MainMenuActivity.this, StageActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
				intent.addFlags(0x8000); // equal to Intent.FLAG_ACTIVITY_CLEAR_TASK which is only available from API level 11
			}
			startActivityForResult(intent, StageActivity.STAGE_ACTIVITY_FINISH);
		}
		if (requestCode == StageActivity.STAGE_ACTIVITY_FINISH) {
			SensorHandler.stopSensorListeners();
			finish();
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		if (!Utils.externalStorageAvailable()) {
			return;
		}

		if (ProjectManager.getInstance().getCurrentProject() != null) {
			ProjectManager.getInstance().saveProject();
			Utils.saveToPreferences(this, Constants.PREF_PROJECTNAME_KEY, ProjectManager.getInstance()
					.getCurrentProject().getName());
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		if (!STANDALONE_MODE) {
			getSupportMenuInflater().inflate(R.menu.menu_main_menu, menu);
		}
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.menu_rate_app:
				launchMarket();
				return true;
			case R.id.menu_about: {
				AboutDialogFragment aboutDialog = new AboutDialogFragment();
				aboutDialog.show(getSupportFragmentManager(), AboutDialogFragment.DIALOG_FRAGMENT_TAG);
				return true;
			}
		}
		return super.onOptionsItemSelected(item);
	}

	// Taken from http://stackoverflow.com/a/11270668
	private void launchMarket() {
		Uri uri = Uri.parse("market://details?id=" + getPackageName());
		Intent myAppLinkToMarket = new Intent(Intent.ACTION_VIEW, uri);
		try {
			startActivity(myAppLinkToMarket);
		} catch (ActivityNotFoundException e) {
			Toast.makeText(this, R.string.main_menu_play_store_not_installed, Toast.LENGTH_SHORT).show();
		}
	}

	// needed because of android:onClick in activity_main_menu.xml
	public void handleContinueButton(View view) {
		handleContinueButton();
	}

	public void handleContinueButton() {
		loadProjectInBackground(Utils.getCurrentProjectName(this));
	}

	private void loadProjectInBackground(String projectName) {
		if (!viewSwitchLock.tryLock()) {
			return;
		}
		LoadProjectTask loadProjectTask = new LoadProjectTask(this, projectName, false, true);
		loadProjectTask.setOnLoadProjectCompleteListener(this);
		loadProjectTask.execute();
	}

	@Override
	public void onLoadProjectSuccess(boolean startProjectActivity) {
		startStageProject();
		/*
		 * if (ProjectManager.getInstance().getCurrentProject() != null && startProjectActivity) {
		 * Intent intent = new Intent(MainMenuActivity.this, ProjectActivity.class);
		 * startActivity(intent);
		 * }
		 */
	}

	public void handleNewButton(View view) {
		if (!viewSwitchLock.tryLock()) {
			return;
		}
		NewProjectDialog dialog = new NewProjectDialog();
		dialog.show(getSupportFragmentManager(), NewProjectDialog.DIALOG_FRAGMENT_TAG);
	}

	public void handleProgramsButton(View view) {
		if (!viewSwitchLock.tryLock()) {
			return;
		}
		Intent intent = new Intent(MainMenuActivity.this, MyProjectsActivity.class);
		startActivity(intent);
	}

	public void handleHelpButton(View view) {
		if (!viewSwitchLock.tryLock()) {
			return;
		}
		Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(Constants.CATROBAT_HELP_URL));
		startActivity(browserIntent);
	}

	public void handleWebButton(View view) {
		if (!viewSwitchLock.tryLock()) {
			return;
		}

		// TODO just a quick fix for not properly working webview on old devices
		if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.GINGERBREAD_MR1) {
			final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
			boolean showBrowserWarning = preferences.getBoolean(SHARED_PREFERENCES_SHOW_BROWSER_WARNING, true);
			if (showBrowserWarning) {
				showWebWarningDialog();
			} else {
				Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(Constants.BASE_URL_HTTPS));
				startActivity(browserIntent);
			}
		} else {
			Intent intent = new Intent(MainMenuActivity.this, WebViewActivity.class);
			startActivity(intent);
		}
	}

	private void showWebWarningDialog() {
		final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
		final View checkboxView = View.inflate(this, R.layout.dialog_web_warning, null);

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(getText(R.string.main_menu_web_dialog_title));
		builder.setView(checkboxView);

		builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int id) {
				CheckBox dontShowAgainCheckBox = (CheckBox) checkboxView
						.findViewById(R.id.main_menu_web_dialog_dont_show_checkbox);
				if (dontShowAgainCheckBox != null && dontShowAgainCheckBox.isChecked()) {
					preferences.edit().putBoolean(SHARED_PREFERENCES_SHOW_BROWSER_WARNING, false).commit();
				}

				Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(Constants.BASE_URL_HTTPS));
				startActivity(browserIntent);
			}
		});
		builder.setNegativeButton(R.string.cancel_button, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int id) {
				dialog.cancel();
			}
		});

		AlertDialog alertDialog = builder.create();
		alertDialog.setCanceledOnTouchOutside(true);
		alertDialog.show();
	}

	public void handleUploadButton(View view) {
		if (!viewSwitchLock.tryLock()) {
			return;
		}
		if (ProjectManager.getInstance().getCurrentProject() == null) {
			LoadProjectTask loadProjectTask = new LoadProjectTask(this, Utils.getCurrentProjectName(this), false, false);
			loadProjectTask.setOnLoadProjectCompleteListener(this);
			loadProjectTask.execute();
		}
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
		String token = preferences.getString(Constants.TOKEN, Constants.NO_TOKEN);
		String username = preferences.getString(Constants.USERNAME, Constants.NO_USERNAME);

		if (token.equals(Constants.NO_TOKEN) || token.length() != ServerCalls.TOKEN_LENGTH
				|| token.equals(ServerCalls.TOKEN_CODE_INVALID)) {
			showLoginRegisterDialog();
		} else {
			CheckTokenTask checkTokenTask = new CheckTokenTask(this, token, username);
			checkTokenTask.setOnCheckTokenCompleteListener(this);
			checkTokenTask.execute();
		}
	}

	@Override
	public void onTokenNotValid() {
		showLoginRegisterDialog();
	}

	@Override
	public void onCheckTokenSuccess() {
		UploadProjectDialog uploadProjectDialog = new UploadProjectDialog();
		uploadProjectDialog.show(getSupportFragmentManager(), UploadProjectDialog.DIALOG_FRAGMENT_TAG);
	}

	private void showLoginRegisterDialog() {
		LoginRegisterDialog loginRegisterDialog = new LoginRegisterDialog();
		loginRegisterDialog.show(getSupportFragmentManager(), LoginRegisterDialog.DIALOG_FRAGMENT_TAG);
	}

	private void loadProgramFromExternalSource(Uri loadExternalProjectUri) {
		String scheme = loadExternalProjectUri.getScheme();
		if (scheme.startsWith((TYPE_HTTP))) {
			String url = loadExternalProjectUri.toString();
			DownloadUtil.getInstance().prepareDownloadAndStartIfPossible(this, url);
		} else if (scheme.equals(TYPE_FILE)) {

			String path = loadExternalProjectUri.getPath();
			int a = path.lastIndexOf('/') + 1;
			int b = path.lastIndexOf('.');
			String projectName = path.substring(a, b);
			if (!UtilZip.unZipFile(path, Utils.buildProjectPath(projectName))) {
				Utils.showErrorDialog(this, R.string.error_load_project);
			}
		}
	}

	private void setMainMenuButtonContinueText() {
		Button mainMenuButtonContinue = (Button) this.findViewById(R.id.main_menu_button_continue);
		TextAppearanceSpan textAppearanceSpan = new TextAppearanceSpan(this, R.style.MainMenuButtonTextSecondLine);
		SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder();
		String mainMenuContinue = this.getString(R.string.main_menu_continue);

		spannableStringBuilder.append(mainMenuContinue);
		spannableStringBuilder.append("\n");
		spannableStringBuilder.append(Utils.getCurrentProjectName(this));

		spannableStringBuilder.setSpan(textAppearanceSpan, mainMenuContinue.length() + 1,
				spannableStringBuilder.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);

		mainMenuButtonContinue.setText(spannableStringBuilder);
	}
}
