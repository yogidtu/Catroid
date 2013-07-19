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

import java.util.ArrayList;
import java.util.List;

import org.catrobat.catroid.R;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnShowListener;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import com.actionbarsherlock.app.SherlockDialogFragment;

public class UserBrickEditElementDialog extends SherlockDialogFragment {

	public static final String DIALOG_FRAGMENT_TAG = "dialog_new_text_catroid";

	private static int stringResourceOfTitle;
	private static int stringResourceOfDefaultText;

	public UserBrickEditElementDialog() {
		super();
	}

	public interface DialogListener {
		void onFinishDialog(String text);
	}

	private List<DialogListener> listenerList = new ArrayList<UserBrickEditElementDialog.DialogListener>();

	public static void setTitle(int stringResource) {
		stringResourceOfTitle = stringResource;
	}

	public static void setDefaultText(int stringResource) {
		stringResourceOfDefaultText = stringResource;
	}

	@Override
	public void onCancel(DialogInterface dialog) {
		super.onCancel(dialog);
		finishDialog(null);
	}

	@Override
	public Dialog onCreateDialog(Bundle bundle) {
		final View dialogView = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_brick_editor_edit_element,
				null);

		final Dialog dialogNewVariable = new AlertDialog.Builder(getActivity()).setView(dialogView)
				.setTitle(stringResourceOfTitle).setNegativeButton(R.string.cancel_button, new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.cancel();
					}
				}).setPositiveButton(R.string.ok, new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						handleOkButton(dialogView);
					}
				}).create();

		dialogNewVariable.setCanceledOnTouchOutside(true);

		dialogNewVariable.setOnShowListener(new OnShowListener() {
			@Override
			public void onShow(DialogInterface dialog) {
				handleOnShow(dialogNewVariable);
			}
		});

		return dialogNewVariable;
	}

	public void addDialogListener(DialogListener newVariableDialogListener) {
		listenerList.add(newVariableDialogListener);
	}

	private void finishDialog(String text) {
		for (DialogListener newVariableDialogListener : listenerList) {
			newVariableDialogListener.onFinishDialog(text);
		}
	}

	private void handleOkButton(View dialogView) {
		EditText variableNameEditText = (EditText) dialogView
				.findViewById(R.id.dialog_brick_editor_edit_element_edit_text);

		String variableName = variableNameEditText.getText().toString();
		finishDialog(variableName);
	}

	private void handleOnShow(final Dialog dialogNewVariable) {
		final Button positiveButton = ((AlertDialog) dialogNewVariable).getButton(AlertDialog.BUTTON_POSITIVE);

		EditText dialogEditText = (EditText) dialogNewVariable
				.findViewById(R.id.dialog_brick_editor_edit_element_edit_text);

		InputMethodManager inputMethodManager = (InputMethodManager) getSherlockActivity().getSystemService(
				Context.INPUT_METHOD_SERVICE);
		inputMethodManager.showSoftInput(dialogEditText, InputMethodManager.SHOW_IMPLICIT);

	}

}
