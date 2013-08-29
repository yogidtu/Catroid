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
package org.catrobat.catroid;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import org.catrobat.catroid.common.Constants;
import org.catrobat.catroid.common.FileChecksumContainer;
import org.catrobat.catroid.common.MessageContainer;
import org.catrobat.catroid.common.StandardProjectHandler;
import org.catrobat.catroid.content.Project;
import org.catrobat.catroid.content.Script;
import org.catrobat.catroid.content.Sprite;
import org.catrobat.catroid.io.StorageHandler;
import org.catrobat.catroid.utils.Utils;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class ProjectManager {
	private static final ProjectManager INSTANCE = new ProjectManager();

	private Project project;
	private Script currentScript;
	private Sprite currentSprite;

	private FileChecksumContainer fileChecksumContainer = new FileChecksumContainer();

	private ProjectManager() {
	}

	public static ProjectManager getInstance() {
		return INSTANCE;
	}

	public boolean loadProject(String projectName, Context context, boolean errorMessage) {
		fileChecksumContainer = new FileChecksumContainer();
		Project oldProject = project;
		MessageContainer.createBackup();
		project = StorageHandler.getInstance().loadProject(projectName);

		loadVirtualGamepadImages(projectName, context);

		if (project == null) {
			if (oldProject != null) {
				project = oldProject;
				MessageContainer.restoreBackup();
			} else {
				project = Utils.findValidProject();
				if (project == null) {
					try {
						project = StandardProjectHandler.createAndSaveStandardProject(context);
						MessageContainer.clearBackup();
					} catch (IOException e) {
						if (errorMessage) {
							Utils.showErrorDialog(context, context.getString(R.string.error_load_project));
						}
						Log.e("CATROID", "Cannot load project.", e);
						return false;
					}
				}
			}
			if (errorMessage) {
				Utils.showErrorDialog(context, context.getString(R.string.error_load_project));
			}
			return false;
		} else if (!Utils.isApplicationDebuggable(context)
				&& (project.getCatrobatLanguageVersion() > Constants.SUPPORTED_CATROBAT_LANGUAGE_VERSION)) {
			project = oldProject;
			if (errorMessage) {
				Utils.showErrorDialog(context, context.getString(R.string.error_project_compatability));
				// TODO show dialog to download latest catroid version instead
			}
			return false;
		} else if (!Utils.isApplicationDebuggable(context)
				&& (project.getCatrobatLanguageVersion() < Constants.SUPPORTED_CATROBAT_LANGUAGE_VERSION)) {
			project = oldProject;
			if (errorMessage) {
				Utils.showErrorDialog(context, context.getString(R.string.error_project_compatability));
				// TODO show dialog to convert project to a supported version
			}
			return false;
		} else {
			// Set generic localized name on background sprite and move it to the back.
			if (project.getSpriteList().size() > 0) {
				project.getSpriteList().get(0).setName(context.getString(R.string.background));
				project.getSpriteList().get(0).look.setZIndex(0);
			}
			MessageContainer.clearBackup();
			currentSprite = null;
			currentScript = null;
			Utils.saveToPreferences(context, Constants.PREF_PROJECTNAME_KEY, project.getName());
			return true;
		}
	}

	public boolean canLoadProject(String projectName) {
		return StorageHandler.getInstance().loadProject(projectName) != null;
	}

	public void saveProject() {
		if (project == null) {
			return;
		}

		SaveProjectAsynchronousTask saveTask = new SaveProjectAsynchronousTask();
		saveTask.execute();
	}

	public boolean initializeDefaultProject(Context context) {
		try {
			fileChecksumContainer = new FileChecksumContainer();
			project = StandardProjectHandler.createAndSaveStandardProject(context);
			currentSprite = null;
			currentScript = null;
			return true;
		} catch (Exception e) {
			Log.e("CATROID", "Cannot initialize default project.", e);
			Utils.showErrorDialog(context, context.getString(R.string.error_load_project));
			return false;
		}
	}

	public void initializeNewProject(String projectName, Context context, boolean empty) throws IOException {
		fileChecksumContainer = new FileChecksumContainer();

		if (empty) {
			project = StandardProjectHandler.createAndSaveEmptyProject(projectName, context);
		} else {
			project = StandardProjectHandler.createAndSaveStandardProject(projectName, context);
		}

		currentSprite = null;
		currentScript = null;
		saveProject();
	}

	public Project getCurrentProject() {
		return project;
	}

	public void setProject(Project project) {
		currentScript = null;
		currentSprite = null;

		this.project = project;
	}

	public void deleteCurrentProject() {
		StorageHandler.getInstance().deleteProject(project);
		project = null;
	}

	public boolean renameProject(String newProjectName, Context context) {
		if (StorageHandler.getInstance().projectExistsCheckCase(newProjectName)) {
			Utils.showErrorDialog(context, context.getString(R.string.error_project_exists));
			return false;
		}

		String oldProjectPath = Utils.buildProjectPath(project.getName());
		File oldProjectDirectory = new File(oldProjectPath);

		String newProjectPath = Utils.buildProjectPath(newProjectName);
		File newProjectDirectory = new File(newProjectPath);

		boolean directoryRenamed = false;

		if (oldProjectPath.equalsIgnoreCase(newProjectPath)) {
			String tmpProjectPath = Utils.buildProjectPath(createTemporaryDirectoryName(newProjectName));
			File tmpProjectDirectory = new File(tmpProjectPath);
			directoryRenamed = oldProjectDirectory.renameTo(tmpProjectDirectory);
			if (directoryRenamed) {
				directoryRenamed = tmpProjectDirectory.renameTo(newProjectDirectory);
			}
		} else {
			directoryRenamed = oldProjectDirectory.renameTo(newProjectDirectory);
		}

		if (directoryRenamed) {
			project.setName(newProjectName);
			StorageHandler.getInstance().saveProject(project);
		}

		if (!directoryRenamed) {
			Utils.showErrorDialog(context, context.getString(R.string.error_rename_project));
		}

		return directoryRenamed;
	}

	public boolean renameProjectNameAndDescription(String newProjectName, String newProjectDescription, Context context) {
		if (StorageHandler.getInstance().projectExistsCheckCase(newProjectName)) {
			Utils.showErrorDialog(context, context.getString(R.string.error_project_exists));
			return false;
		}

		String oldProjectPath = Utils.buildProjectPath(project.getName());
		File oldProjectDirectory = new File(oldProjectPath);

		String newProjectPath = Utils.buildProjectPath(newProjectName);
		File newProjectDirectory = new File(newProjectPath);

		boolean directoryRenamed = false;

		if (oldProjectPath.equalsIgnoreCase(newProjectPath)) {
			String tmpProjectPath = Utils.buildProjectPath(createTemporaryDirectoryName(newProjectName));
			File tmpProjectDirectory = new File(tmpProjectPath);
			directoryRenamed = oldProjectDirectory.renameTo(tmpProjectDirectory);
			if (directoryRenamed) {
				directoryRenamed = tmpProjectDirectory.renameTo(newProjectDirectory);
			}
		} else {
			directoryRenamed = oldProjectDirectory.renameTo(newProjectDirectory);
		}

		if (directoryRenamed) {
			project.setName(newProjectName);
			project.setDescription(newProjectDescription);
			this.saveProject();
		}

		if (!directoryRenamed) {
			Utils.showErrorDialog(context, context.getString(R.string.error_rename_project));
		}

		return directoryRenamed;
	}

	public Sprite getCurrentSprite() {
		return currentSprite;
	}

	public void setCurrentSprite(Sprite sprite) {
		currentSprite = sprite;
	}

	public Script getCurrentScript() {
		return currentScript;
	}

	public void setCurrentScript(Script script) {
		if (script == null) {
			currentScript = null;
		} else if (currentSprite.getScriptIndex(script) != -1) {
			currentScript = script;
		}
	}

	public void addSprite(Sprite sprite) {
		project.addSprite(sprite);
	}

	public void addScript(Script script) {
		currentSprite.addScript(script);
	}

	public boolean spriteExists(String spriteName) {
		for (Sprite sprite : project.getSpriteList()) {
			if (sprite.getName().equalsIgnoreCase(spriteName)) {
				return true;
			}
		}
		return false;
	}

	public int getCurrentSpritePosition() {
		return project.getSpriteList().indexOf(currentSprite);
	}

	public int getCurrentScriptPosition() {
		int currentSpritePosition = this.getCurrentSpritePosition();
		if (currentSpritePosition == -1) {
			return -1;
		}

		return project.getSpriteList().get(currentSpritePosition).getScriptIndex(currentScript);
	}

	private String createTemporaryDirectoryName(String projectDirectoryName) {
		String temporaryDirectorySuffix = "_tmp";
		String temporaryDirectoryName = projectDirectoryName + temporaryDirectorySuffix;
		int suffixCounter = 0;
		while (Utils.checkIfProjectExistsOrIsDownloadingIgnoreCase(temporaryDirectoryName)) {
			temporaryDirectoryName = projectDirectoryName + temporaryDirectorySuffix + suffixCounter;
			suffixCounter++;
		}
		return temporaryDirectoryName;
	}

	public FileChecksumContainer getFileChecksumContainer() {
		return this.fileChecksumContainer;
	}

	public void setFileChecksumContainer(FileChecksumContainer fileChecksumContainer) {
		this.fileChecksumContainer = fileChecksumContainer;
	}

	private class SaveProjectAsynchronousTask extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... params) {
			StorageHandler.getInstance().saveProject(project);
			return null;
		}
	}

	public static void loadVirtualGamepadImages(String projectName, Context context) {

		String path = Utils.buildPath(Utils.buildProjectPath(projectName), Constants.IMAGE_DIRECTORY);
		String[] imagePath = new String[] { Utils.buildPath(path, Constants.VGP_IMAGE_PAD_CENTER),
				Utils.buildPath(path, Constants.VGP_IMAGE_PAD_STRAIGHT),
				Utils.buildPath(path, Constants.VGP_IMAGE_PAD_DIAGONAL),
				Utils.buildPath(path, Constants.VGP_IMAGE_BUTTON_TOUCH),
				Utils.buildPath(path, Constants.VGP_IMAGE_BUTTON_HOLD),
				Utils.buildPath(path, Constants.VGP_IMAGE_BUTTON_SWIPE) };
		int[] resList = new int[] { org.catrobat.catroid.R.drawable.vgp_dpad_center,
				org.catrobat.catroid.R.drawable.vgp_dpad_straight, org.catrobat.catroid.R.drawable.vgp_dpad_diagonal,
				org.catrobat.catroid.R.drawable.vgp_button_touch, org.catrobat.catroid.R.drawable.vgp_button_hold,
				org.catrobat.catroid.R.drawable.vgp_button_swipe };

		for (int i = 0; i < imagePath.length; i++) {
			File file = new File(imagePath[i]);
			try {
				if (!file.exists()) {
					file.createNewFile();
				}
				InputStream in = context.getResources().openRawResource(resList[i]);
				OutputStream out = new BufferedOutputStream(new FileOutputStream(file), Constants.BUFFER_8K);
				byte[] buffer = new byte[Constants.BUFFER_8K];
				int length = 0;
				while ((length = in.read(buffer)) > 0) {
					out.write(buffer, 0, length);
				}
				in.close();
				out.flush();
				out.close();

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
