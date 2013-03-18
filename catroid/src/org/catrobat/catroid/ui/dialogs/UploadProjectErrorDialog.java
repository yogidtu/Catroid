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
package org.catrobat.catroid.ui.dialogs;

import org.catrobat.catroid.R;
import org.catrobat.catroid.common.Constants;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class UploadProjectErrorDialog extends Dialog {

	private Context context;

	public UploadProjectErrorDialog(Context context) {
		super(context);
		this.context = context;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dialog_upload_project_error);

		setTitle(R.string.error);
		setCanceledOnTouchOutside(true);

		TextView errorMessageTextView = (TextView) findViewById(R.id.dialog_upload_project_error_text_view_message);
		errorMessageTextView.setText(R.string.error_project_upload_version);

		TextView apkUrlTextView = (TextView) findViewById(R.id.dialog_upload_project_error_text_view_apk_link);
		apkUrlTextView.setMovementMethod(LinkMovementMethod.getInstance());

		String apkUrl = context.getString(R.string.link_template, Constants.LATEST_CATROID_VERSION_LINK,
				context.getString(R.string.dialog_upload_project_apk_link_text));

		apkUrlTextView.setText(Html.fromHtml(apkUrl));

		Button okButton = (Button) findViewById(R.id.dialog_upload_project_error_ok_button);
		okButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View view) {
				dismiss();
			}
		});
	}

}
