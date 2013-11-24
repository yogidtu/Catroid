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
package org.catrobat.catroid.io;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.Log;

import org.catrobat.catroid.ProjectManager;
import org.catrobat.catroid.R;
import org.catrobat.catroid.content.Project;
import org.catrobat.catroid.content.Script;
import org.catrobat.catroid.content.Sprite;
import org.catrobat.catroid.content.bricks.Brick;
import org.catrobat.catroid.content.bricks.IfLogicBeginBrick;
import org.catrobat.catroid.content.bricks.IfLogicElseBrick;
import org.catrobat.catroid.content.bricks.IfLogicEndBrick;
import org.catrobat.catroid.content.bricks.LoopBeginBrick;
import org.catrobat.catroid.content.bricks.LoopEndBrick;
import org.catrobat.catroid.utils.Utils;

import java.util.ArrayList;

public class LoadProjectTask extends AsyncTask<Void, Void, Boolean> {
	private Activity activity;
	private String projectName;
	private ProgressDialog progressDialog;
	private boolean showErrorMessage;
	private boolean startProjectActivity;

	private OnLoadProjectCompleteListener onLoadProjectCompleteListener;
	private boolean autocorrectMode = true;

	public LoadProjectTask(Activity activity, String projectName, boolean showErrorMessage, boolean startProjectActivity) {
		this.activity = activity;
		this.projectName = projectName;
		this.showErrorMessage = showErrorMessage;
		this.startProjectActivity = startProjectActivity;
	}

	public void setOnLoadProjectCompleteListener(OnLoadProjectCompleteListener listener) {
		onLoadProjectCompleteListener = listener;
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		if (activity == null) {
			return;
		}
		String title = activity.getString(R.string.please_wait);
		String message = activity.getString(R.string.loading);
		progressDialog = ProgressDialog.show(activity, title, message);
	}

	@Override
	protected Boolean doInBackground(Void... arg0) {
		Project currentProject = ProjectManager.getInstance().getCurrentProject();
		if (currentProject == null) {
			boolean success = ProjectManager.getInstance().loadProject(projectName, activity, false);
			checkNestingBrickReferences();
			return success;
		} else if (!currentProject.getName().equals(projectName)) {
			boolean success = ProjectManager.getInstance().loadProject(projectName, activity, false);
			checkNestingBrickReferences();
			return success;
		}
		return true;
	}

	private void checkNestingBrickReferences() {
		if (autocorrectMode) {
			Project currentProject = ProjectManager.getInstance().getCurrentProject();
			boolean projectCorrect = true;
			if (currentProject != null) {
				for (Sprite currentSprite : currentProject.getSpriteList()) {
					int numberOfScripts = currentSprite.getNumberOfScripts();
					for (int pos = 0; pos < numberOfScripts; pos++) {
						Script script = currentSprite.getScript(pos);
						boolean scriptCorrect = true;
						for (Brick currentBrick : script.getBrickList()) {
							if (currentBrick instanceof IfLogicBeginBrick) {
								IfLogicElseBrick elseBrick = ((IfLogicBeginBrick) currentBrick).getIfElseBrick();
								IfLogicEndBrick endBrick = ((IfLogicBeginBrick) currentBrick).getIfEndBrick();
								if (elseBrick == null || endBrick == null || elseBrick.getIfBeginBrick() == null
										|| elseBrick.getIfEndBrick() == null || endBrick.getIfBeginBrick() == null
										|| endBrick.getIfElseBrick() == null
										|| !elseBrick.getIfBeginBrick().equals(currentBrick)
										|| !elseBrick.getIfEndBrick().equals(endBrick)
										|| !endBrick.getIfBeginBrick().equals(currentBrick)
										|| !endBrick.getIfElseBrick().equals(elseBrick)) {
									scriptCorrect = false;
									projectCorrect = false;
									Log.d("REFERENCE ERROR!!", "Brick has wrong reference:" + currentSprite + " "
											+ currentBrick);
								}
							} else if (currentBrick instanceof LoopBeginBrick) {
								LoopEndBrick endBrick = ((LoopBeginBrick) currentBrick).getLoopEndBrick();
								if (endBrick == null || endBrick.getLoopBeginBrick() == null
										|| !endBrick.getLoopBeginBrick().equals(currentBrick)) {
									scriptCorrect = false;
									projectCorrect = false;
									Log.d("REFERENCE ERROR!!", "Brick has wrong reference:" + currentSprite + " "
											+ currentBrick);
								}
							}
						}
						if (!scriptCorrect) {
							//correct references
							ArrayList<IfLogicBeginBrick> ifBeginList = new ArrayList<IfLogicBeginBrick>();
							ArrayList<LoopBeginBrick> loopBeginList = new ArrayList<LoopBeginBrick>();
							for (Brick currentBrick : script.getBrickList()) {
								if (currentBrick instanceof IfLogicBeginBrick) {
									ifBeginList.add((IfLogicBeginBrick) currentBrick);
								} else if (currentBrick instanceof LoopBeginBrick) {
									loopBeginList.add((LoopBeginBrick) currentBrick);
								} else if (currentBrick instanceof LoopEndBrick) {
									LoopBeginBrick loopBeginBrick = loopBeginList.get(loopBeginList.size() - 1);
									loopBeginBrick.setLoopEndBrick((LoopEndBrick) currentBrick);
									((LoopEndBrick) currentBrick).setLoopBeginBrick(loopBeginBrick);
									loopBeginList.remove(loopBeginBrick);
								} else if (currentBrick instanceof IfLogicElseBrick) {
									IfLogicBeginBrick ifBeginBrick = ifBeginList.get(ifBeginList.size() - 1);
									ifBeginBrick.setIfElseBrick((IfLogicElseBrick) currentBrick);
									((IfLogicElseBrick) currentBrick).setIfBeginBrick(ifBeginBrick);
								} else if (currentBrick instanceof IfLogicEndBrick) {
									IfLogicBeginBrick ifBeginBrick = ifBeginList.get(ifBeginList.size() - 1);
									IfLogicElseBrick elseBrick = ifBeginBrick.getIfElseBrick();
									ifBeginBrick.setIfEndBrick((IfLogicEndBrick) currentBrick);
									elseBrick.setIfEndBrick((IfLogicEndBrick) currentBrick);
									((IfLogicEndBrick) currentBrick).setIfBeginBrick(ifBeginBrick);
									((IfLogicEndBrick) currentBrick).setIfElseBrick(elseBrick);
									ifBeginList.remove(ifBeginBrick);
								}
							}
						}
					}
				}
			}
			if (!projectCorrect) {
				ProjectManager.getInstance().saveProject();
			}
		}
	}

	@Override
	protected void onPostExecute(Boolean success) {
		super.onPostExecute(success);

		if (progressDialog != null && progressDialog.isShowing()) {
			progressDialog.dismiss();
		}
		if (onLoadProjectCompleteListener != null) {
			if (!success && showErrorMessage) {
				Utils.showErrorDialog(activity, R.string.error_load_project);
			} else {
				onLoadProjectCompleteListener.onLoadProjectSuccess(startProjectActivity);
			}
		}
	}

	public interface OnLoadProjectCompleteListener {

		void onLoadProjectSuccess(boolean startProjectActivity);

	}
}
