package org.catrobat.catroid.livewallpaper.test.utils;


import java.io.File;

import java.util.ArrayList;

import org.catrobat.catroid.ProjectManager; 
import org.catrobat.catroid.common.SoundInfo;
import org.catrobat.catroid.content.Project;
import org.catrobat.catroid.content.Script;
import org.catrobat.catroid.content.Sprite;
import org.catrobat.catroid.content.StartScript;
import org.catrobat.catroid.content.bricks.PlaySoundBrick;
import org.catrobat.catroid.livewallpaper.LiveWallpaper;
import org.catrobat.catroid.uitest.util.UiTestUtils;


public class LiveWallpaperTestUtils {
	
	private final static int RESOURCE_SOUND = org.catrobat.catroid.uitest.R.raw.longsound;
	private final static String projectName = "Sound testing project"; 
	private static File soundFile;
	private static ArrayList<SoundInfo> soundInfoList;
	
	public static void createSoundTestingProject(){
		ProjectManager projectManager = ProjectManager.getInstance();
		Project project = new Project(null, projectName);
		Sprite firstSprite = new Sprite("The first sprite");
		Script testScript = new StartScript(firstSprite);

		PlaySoundBrick playSoundBrick = new PlaySoundBrick(firstSprite);
		testScript.addBrick(playSoundBrick);

		firstSprite.addScript(testScript);
		project.addSprite(firstSprite);

		projectManager.setProject(project);
		projectManager.setCurrentSprite(firstSprite);
		projectManager.setCurrentScript(testScript);
		soundInfoList = projectManager.getCurrentSprite().getSoundList();

		soundFile = UiTestUtils.saveFileToProject(UiTestUtils.DEFAULT_TEST_PROJECT_NAME, "longsound.mp3",
				RESOURCE_SOUND, LiveWallpaper.context, UiTestUtils.FileTypes.SOUND);
		SoundInfo soundInfo = new SoundInfo();
		soundInfo.setSoundFileName(soundFile.getName());
		soundInfo.setTitle("test sound");
		
		soundInfoList.add(soundInfo);
		ProjectManager.getInstance().getFileChecksumContainer()
				.addChecksum(soundInfo.getChecksum(), soundInfo.getAbsolutePath());
	}
}
