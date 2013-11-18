:   +------------------+
:   | SOURCE DIRECTORY |
:   +------------------+

:org.catrobat.catroid.bluetooth
cd "%~dp0\src\org\catrobat\catroid\bluetooth"
del "BluetoothManager.java"
del "DeviceListActivity.java"
cd ".."

:org.catrobat.catroid.content.bricks
cd "%~dp0\src\org\catrobat\catroid\content\bricks"
del "LegoNxtMotorActionBrick.java"
del "LegoNxtMotorStopBrick.java"
del "LegoNxtMotorTurnAngleBrick.java"
del "LegoNxtPlayToneBrick.java"
cd ".."
cd ".."

:org.catrobat:catroid.formulaeditor
cd "%~dp0\src\org\catrobat\catroid\formulaeditor"
del "ExternInternRepresentationMapping.java"
del "ExternToken.java"
del "FormulaEditorEditText.java"
del "FormulaEditorHistory.java"
del "InternFormula.java"
del "InternFormulaKeyboardAdapter.java"
del "InternFormulaState.java"
del "InternFormulaTokenSelection.java"
del "InternToExternGenerator.java"
cd ".."

:org.catrobat.catroid.legonxt - delete whole
call:deleteFolderAndSubfolder "%~dp0\src\org\catrobat\catroid\legonxt"

:org.catrobat.catroid.soundrecorder
cd "%~dp0\src\org\catrobat\catroid\soundrecorder"
del "SoundRecorderActivity.java"
cd ".."

:org.catrobat.catroid.stage
cd "%~dp0\src\org\catrobat\catroid\stage"
del "StageActivity.java"
cd ".."

:org.catrobat.catroid.transfers
cd "%~dp0\src\org\catrobat\catroid\transfers"
del "ProjectUploadService.java"
del "RegistrationTask.java"
cd ".."

:org.catrobat.catroid.ui
cd "%~dp0\src\org\catrobat\catroid\ui"
del "BackPackActivity.java"
del "BaseActivity.java"
del "BottomBar.java"
del "BrickLayout.java"
del "LookViewHolder.java"
del "MainMenuActivity.java"
del "MyProjectsActivity.java"
del "ProgramMenuActivity.java"
del "ProjectActivity.java"
del "ScriptActivity.java"
del "SettingsActivity.java"
del "ViewSwitchLock.java"
del "WebViewActivity.java"

:org.catrobat.catroid.ui.adapter
cd "adapter"
del "BackPackSoundAdapter.java"
del "BrickAdapter.java"
del "BrickCategoryAdapter.java"
del "LookAdapter.java"
del "LookBaseAdapter.java"
del "PrototypeBrickAdapter.java"
del "ScriptActivityAdapterInterface.java"
del "SoundAdapter.java"
del "SoundBaseAdapter.java"
del "SpriteAdapter.java"
del "UserVariableAdapter.java"
del "UserVariableAdapterWrapper.java"
cd ".."

:org.catrobat.catroid.ui.controller
cd "controller"
del "LookController.java"
del "SoundController.java"
cd ".."

:org.catrobat.catroid.ui.dialogs
cd "dialogs"
del "BrickTextDialog.java"
del "CopyProjectDialog.java"
del "DeleteLookDialog.java"
del "DeleteSoundDialog.java"
del "FormulaEditorComputeDialog.java"
del "LoginRegisterDialog.java"
del "MultiLineTextDialog.java"
del "NewLookDialog.java"
del "NewProjectDialog.java"
del "NewSpriteDialog.java"
del "NewVariableDialog.java"
del "RenameLookDialog.java"
del "RenameSoundDialog.java"
del "RenameSpriteDialog.java"
del "SetDescriptionDialog.java"
del "StageDialog.java"
del "UploadProjectDialog.java"
cd ".."

:org.catrobat.catroid.ui.dragndrop - delete whole
call:deleteFolderAndSubfolder "%~dp0\src\org\catrobat\catroid\ui\dragndrop"

:org.catrobat.catroid.ui.fragment - delete whole
call:deleteFolderAndSubfolder "%~dp0\src\org\catrobat\catroid\ui\fragment"
cd ".."

:org.catrobat.catroid.utils
cd "%~dp0\src\org\catrobat\catroid\utils"
del "StatusBarNotificationManager.java"
cd ".."

:   +-------------------+
:   |   RES DIRECTORY   |
:   +-------------------+

: Needs to be done

:res.layout
cd "%~dp0\res\layout
del "action_mode_select_all.xml"
del "activity_my_projects.xml"
del "activity_program_menu.xml"
del "activity_project.xml"
del "activity_project_background_headline.xml"
del "activity_project_objects_headline.xml"
del "activity_project_sprite_title_details_view.xml"
del "activity_project_spritelist_item.xml"
del "activity_script.xml"
del "brick_broadcast.xml"
del "brick_broadcast_receive.xml"
del "brick_broadcast_wait.xml"
del "brick_category_control.xml"
del "brick_category_lego_nxt.xml"
del "brick_category_looks.xml"
del "brick_category_motion.xml"
del "brick_category_sound.xml"
del "brick_category_uservariables.xml"
del "brick_change_brightness.xml"
del "brick_change_ghost_effect.xml"
del "brick_change_size_by_n.xml"
del "brick_change_variable_by.xml"
del "brick_change_volume_by.xml"
del "brick_change_x.xml"
del "brick_change_y.xml"
del "brick_clear_graphic_effect.xml"
del "brick_forever.xml"
del "brick_glide_to.xml"
del "brick_go_back.xml"
del "brick_go_to_front.xml"
del "brick_hide.xml"
del "brick_if_begin_if.xml"
del "brick_if_else.xml"
del "brick_if_end_if.xml"
del "brick_if_on_edge_bounce.xml"
del "brick_insert.xml"
del "brick_loop_end.xml"
del "brick_loop_end_no_puzzle.xml"
del "brick_loop_endless.xml"
del "brick_loop_endless_no_puzzle.xml"
del "brick_move_n_steps.xml"
del "brick_next_look.xml"
del "brick_note.xml"
del "brick_nxt_motor_action.xml"
del "brick_nxt_motor_stop.xml"
del "brick_nxt_motor_turn_angle.xml"
del "brick_nxt_play_tone.xml"
del "brick_place_at.xml"
del "brick_play_sound.xml"
del "brick_point_in_direction.xml"
del "brick_point_to.xml"
del "brick_repeat.xml"
del "brick_set_brightness.xml"
del "brick_set_ghost_effect.xml"
del "brick_set_look.xml"
del "brick_set_size_to.xml"
del "brick_set_variable.xml"
del "brick_set_volume_to.xml"
del "brick_set_x.xml"
del "brick_set_y.xml"
del "brick_show.xml"
del "brick_speak.xml"
del "brick_stop_all_sounds.xml"
del "brick_turn_left.xml"
del "brick_turn_right.xml"
del "brick_wait.xml"
del "brick_when.xml"
del "brick_when_started.xml"
del "brick_wrapper.xml"
del "device_list.xml"
del "device_name.xml"
del "dialog_formula_editor_variable_name.xml"
del "dialog_formulaeditor_compute.xml"
del "dialog_login_register.xml"
del "dialog_new_project.xml"
del "dialog_stage.xml"
del "dialog_text_multiline_dialog.xml"
del "dialog_upload_project.xml"
del "formula_editor_keyboard.xml"
del "fragment_brick_add.xml"
del "fragment_brick_categories.xml"
del "fragment_formula_editor.xml"
del "fragment_formula_editor_list.xml"
del "fragment_formula_editor_list_item.xml"
del "fragment_formula_editor_variablelist.xml"
del "fragment_formula_editor_variablelist_global_headline.xml"
del "fragment_formula_editor_variablelist_item.xml"
del "fragment_formula_editor_variablelist_local_headline.xml"
del "fragment_look.xml"
del "fragment_look_looklist_item.xml"
del "fragment_projects_list.xml"
del "fragment_script.xml"
del "fragment_sounds.xml"
del "fragment_sprites_list.xml"
cd ".."

:res.menu - delte whole
call:deleteFolderAndSubfolder "%~dp0\res\menu"

:res.values
cd "%~dp0\res\values"
del "arrays.xml"
cd ".."

:res.xml
cd "%~dp0\res\xml"
del "preferences.xml"
cd ".."

echo.&goto:eof

:deleteFolderAndSubfolder
@echo off
IF exist %~1 ( echo %~1 exists
cd /d %~1
for /F "delims=" %%i in ('dir /b') do (rmdir "%%i" /s/q || del "%%i" /s/q)
cd ..
rmdir %~1 )
goto:eof

:deleteAllFiles
del "*.*" /Q
goto:eof