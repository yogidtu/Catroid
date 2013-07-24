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
import org.catrobat.catroid.utils.Utils;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
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
		setContentView(R.layout.dialog_lwp_select_program);

		final RadioGroup rg = new RadioGroup(context);
		rg.setOrientation(RadioGroup.VERTICAL);
		rg.setPadding(50, 0, 50, 50);

		File rootDirectory = new File(Constants.DEFAULT_ROOT);
		int num_of_projects = UtilFile.getProjectNames(rootDirectory).size();

		//		Log.d("RGB", "Number of projects: " + num_of_projects);
		final RadioButton[] rb = new RadioButton[num_of_projects];
		int i = 0;
		for (String projectName : UtilFile.getProjectNames(rootDirectory)) {
			rb[i] = new RadioButton(context);
			rb[i].setText(projectName);
			rb[i].setTextColor(Color.WHITE);
			rg.addView(rb[i], i); //the RadioButtons are added to the radioGroup instead of the layout			
			i++;
		}

		final LinearLayout ll = (LinearLayout) findViewById(R.id.lwp_linear_layout_rg);
		ll.addView(rg);

		rg.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				//TODO: Implement me
				RadioButton rbb = (RadioButton) group.getChildAt(checkedId - 1);
				Log.d("RGB", "Radio Button Selected: " + checkedId);
				Log.d("RGB", "Text: " + rbb.getText());
				//				boolean test = ProjectManager.getInstance().loadProject(rbb.getText().toString(), context, false);
				Project project = StorageHandler.getInstance().loadProject(rbb.getText().toString());
				ProjectManager.getInstance().setProject(project);
				//				ProjectManager.getInstance().saveProject();
				boolean temp = Utils.isStandardProject(project, context);
				Log.d("RGB", "is standard: " + temp);

				//				SelectProgramDialog.this.dismiss();
			}
		});

		//		for (String projectName : UtilFile.getProjectNames(rootDirectory)) {
		//
		//		}

		//		final Button setProgram = new Button(context);
		//		setProgram.setText("Set Program");
		//		//		setProgram.setTextColor(Color.WHITE);
		//		ll.addView(setProgram);
		//
		//		setProgram.setOnClickListener(new View.OnClickListener() {
		//
		//			@Override
		//			public void onClick(View v) {
		//				// TODO Auto-generated method stub
		//				//				for (int i = 0; i < 5; i++) {
		//				//					rg.removeView(rb[i]);//now the RadioButtons are in the RadioGroup
		//				//				}
		//				//				ll.removeView(setProgram);
		//				SelectProgramDialog.this.dismiss();
		//			}
		//		});
	}
}
