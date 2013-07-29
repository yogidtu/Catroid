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

import java.io.File;

import org.catrobat.catroid.ProjectManager;
import org.catrobat.catroid.R;
import org.catrobat.catroid.common.Constants;
import org.catrobat.catroid.content.Project;
import org.catrobat.catroid.io.StorageHandler;
import org.catrobat.catroid.utils.UtilFile;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;

public class SelectProgramDialog extends Dialog {

	private Context context;

	public SelectProgramDialog(Context context) {
		super(context);
		this.context = context;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.dialog_lwp_select_program);

		RadioGroup radioGroup = new RadioGroup(context);
		radioGroup.setOrientation(RadioGroup.VERTICAL);
		radioGroup.setPadding(50, 50, 50, 50);

		File rootDirectory = new File(Constants.DEFAULT_ROOT);
		int numOfProjects = UtilFile.getProjectNames(rootDirectory).size();

		RadioButton[] radioButton = new RadioButton[numOfProjects];
		int i = 0;
		for (String projectName : UtilFile.getProjectNames(rootDirectory)) {
			radioButton[i] = new RadioButton(context);
			radioButton[i].setText(projectName);
			radioButton[i].setTextColor(Color.WHITE);
			radioGroup.addView(radioButton[i], i);
			i++;
		}

		final LinearLayout linearLayout = (LinearLayout) findViewById(R.id.lwp_linear_layout_rg);
		linearLayout.addView(radioGroup);
		//		context = this.getContext();

		radioGroup.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				RadioButton checkedRadioButton = (RadioButton) findViewById(checkedId);
				Project project = StorageHandler.getInstance().loadProject(checkedRadioButton.getText().toString());
				//				project.setDeviceData(context);
				ProjectManager.getInstance().setProject(project);
				StorageHandler.getInstance().saveProject(project);
			}
		});

	}
}
