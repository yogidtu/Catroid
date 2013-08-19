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
package org.catrobat.catroid.content.bricks;

import java.util.List;

import org.catrobat.catroid.R;
import org.catrobat.catroid.content.BroadcastMessage;
import org.catrobat.catroid.content.RecognitionScript;
import org.catrobat.catroid.content.Script;
import org.catrobat.catroid.content.Sprite;
import org.catrobat.catroid.speechrecognition.RecognitionManager;
import org.catrobat.catroid.ui.ScriptActivity;
import org.catrobat.catroid.ui.dialogs.BrickTextDialog;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.badlogic.gdx.scenes.scene2d.actions.SequenceAction;

public class WhenSpeechReceiverBrick extends ScriptBrick implements BroadcastMessage {
	private static final long serialVersionUID = 1L;

	private RecognitionScript script;
	private String recognitionKeyword = "";

	public WhenSpeechReceiverBrick(Sprite sprite, RecognitionScript script) {
		this.script = script;
		this.sprite = sprite;
		recognitionKeyword = script.getBroadcastMessage();
	}

	public WhenSpeechReceiverBrick(Sprite sprite, String keyword) {
		this.sprite = sprite;
		this.recognitionKeyword = keyword;
		this.script = new RecognitionScript(sprite, keyword);
	}

	@Override
	public int getRequiredResources() {
		return (SPEECH_TO_TEXT | NETWORK_CONNECTION);
	}

	@Override
	public Brick copyBrickForSprite(Sprite sprite, Script script) {
		WhenSpeechReceiverBrick copyBrick = (WhenSpeechReceiverBrick) clone();
		copyBrick.sprite = sprite;
		copyBrick.script = (RecognitionScript) script;
		return copyBrick;
	}

	@Override
	public View getView(Context context, int brickId, final BaseAdapter baseAdapter) {
		if (animationState) {
			return view;

		}
		if (view == null) {
			alphaValue = 255;
		}
		view = View.inflate(context, R.layout.brick_when_speech_receive, null);
		view = getViewWithAlpha(alphaValue);

		setCheckboxView(R.id.brick_when_speech_checkbox);

		TextView textHolder = (TextView) view.findViewById(R.id.brick_when_speech_prototype_text_view);
		EditText editText = (EditText) view.findViewById(R.id.brick_when_speech_edit_text);
		editText.setText(recognitionKeyword);

		textHolder.setVisibility(View.GONE);
		editText.setVisibility(View.VISIBLE);

		editText.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View view) {
				if (checkbox.getVisibility() == View.VISIBLE) {
					return;
				}
				ScriptActivity activity = (ScriptActivity) view.getContext();

				BrickTextDialog editDialog = new BrickTextDialog() {
					@Override
					protected void initialize() {
						input.setText(recognitionKeyword);
						input.setSelectAllOnFocus(true);
					}

					@Override
					protected boolean getPositiveButtonEnabled() {
						return true;
					}

					@Override
					protected TextWatcher getInputTextChangedListener(Button buttonPositive) {
						return new TextWatcher() {
							@Override
							public void onTextChanged(CharSequence s, int start, int before, int count) {
							}

							@Override
							public void beforeTextChanged(CharSequence s, int start, int count, int after) {
							}

							@Override
							public void afterTextChanged(final Editable s) {
								if (s.toString().endsWith(" ") || s.toString().contains(" ")) {
									final String oneWord = s.toString().substring(0, s.toString().indexOf(" "));
									AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
									builder.setTitle(getActivity().getString(R.string.wordcheck_dialog_oneword_title))
											.setCancelable(false)
											.setPositiveButton(getActivity().getString(R.string.ok),
													new DialogInterface.OnClickListener() {
														@Override
														public void onClick(DialogInterface dialog, int id) {
															s.clear();
															s.insert(0, oneWord);
														}
													})
											.setMessage(
													getActivity().getString(R.string.wordcheck_dialog_oneword_content));

									builder.create();
									builder.show();
								}
							}
						};
					}

					@Override
					protected boolean handleOkButton() {
						final String givenRecognitionKeyword = (input.getText().toString()).trim();
						final ProgressDialog connectingProgressDialog = ProgressDialog.show(getActivity(), "",
								getResources().getString(R.string.please_wait), true);

						final BrickTextDialog brickTextDialog = this;
						new Thread() {

							@Override
							public void run() {
								int recognizeable = RecognitionManager.isWordRecognizeable(givenRecognitionKeyword);
								connectingProgressDialog.dismiss();
								if (recognizeable == 1) {
									recognitionKeyword = givenRecognitionKeyword;
									script.setBroadcastMessage(recognitionKeyword);
									getActivity().sendBroadcast(new Intent(ScriptActivity.ACTION_BRICK_LIST_CHANGED));
									brickTextDialog.dismiss();
								} else {
									getActivity().runOnUiThread(new Runnable() {
										@Override
										public void run() {
											AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
											builder.setTitle(
													getActivity().getString(R.string.wordcheck_dialog_dictionary_title))
													.setCancelable(false)
													.setPositiveButton(
															getActivity().getString(
																	R.string.wordcheck_dialog_dictionary_useanyway),
															new DialogInterface.OnClickListener() {
																@Override
																public void onClick(DialogInterface dialog, int id) {
																	recognitionKeyword = givenRecognitionKeyword;
																	script.setBroadcastMessage(recognitionKeyword);
																	getActivity()
																			.sendBroadcast(
																					new Intent(
																							ScriptActivity.ACTION_BRICK_LIST_CHANGED));
																	brickTextDialog.dismiss();
																}
															})
													.setNegativeButton(
															getActivity().getString(
																	R.string.wordcheck_dialog_dictionary_recheck),
															new DialogInterface.OnClickListener() {
																@Override
																public void onClick(DialogInterface dialog, int id) {
																}
															})
													.setMessage(
															getActivity().getString(
																	R.string.wordcheck_dialog_dictionary_content));
											builder.create();
											builder.show();
										}
									});
								}
							}
						}.start();
						return false;
					}
				};

				editDialog.show(activity.getSupportFragmentManager(), "dialog_when_speech_brick");
			}
		});

		return view;
	}

	@Override
	public View getPrototypeView(Context context) {
		View prototypeView = View.inflate(context, R.layout.brick_when_speech_receive, null);
		TextView textSpeak = (TextView) prototypeView.findViewById(R.id.brick_when_speech_prototype_text_view);
		textSpeak.setText(recognitionKeyword);
		return prototypeView;
	}

	@Override
	public Brick clone() {
		WhenSpeechReceiverBrick clonedBrick = new WhenSpeechReceiverBrick(getSprite(), this.script);
		clonedBrick.recognitionKeyword = this.recognitionKeyword;
		return clonedBrick;
	}

	@Override
	public Script initScript(Sprite sprite) {
		if (script == null) {
			script = new RecognitionScript(sprite, recognitionKeyword);
		}
		return script;
	}

	@Override
	public View getViewWithAlpha(int alphaValue) {

		if (view != null) {

			LinearLayout layout = (LinearLayout) view.findViewById(R.id.brick_when_speech_layout);
			Drawable background = layout.getBackground();
			background.setAlpha(alphaValue);
			this.alphaValue = (alphaValue);

		}

		return view;
	}

	@Override
	public List<SequenceAction> addActionToSequence(SequenceAction sequence) {
		return null;

	}

	@Override
	public String getBroadcastMessage() {
		if (script == null) {
			return recognitionKeyword;
		}
		return script.getBroadcastMessage();
	}
}
