:+------------------+
:| SOURCE DIRECTORY |
:+------------------+

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

:+-------------------+
:|   RES DIRECTORY   |
:+-------------------+

: Needs to be done

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