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
package org.catrobat.catroid.test.content;

import android.test.AndroidTestCase;

import org.catrobat.catroid.ProjectManager;
import org.catrobat.catroid.common.Constants;
import org.catrobat.catroid.common.FileChecksumContainer;
import org.catrobat.catroid.content.Project;
import org.catrobat.catroid.io.StorageHandler;
import org.catrobat.catroid.test.R;
import org.catrobat.catroid.test.utils.TestUtils;
import org.catrobat.catroid.utils.Utils;

import java.io.File;
import java.io.IOException;

public class FileChecksumContainerTest extends AndroidTestCase {

	private static final int IMAGE_FILE_ID = R.raw.icon;
	private StorageHandler storageHandler;
	private ProjectManager projectManager;
	private File testImage;
	private File testSound;

	public FileChecksumContainerTest() throws IOException {
	}

	@Override
	protected void setUp() throws Exception {

		storageHandler = StorageHandler.getInstance();
		projectManager = ProjectManager.getInstance();

		TestUtils.createTestProjectWithDefaultName(getContext());
		Project currentProject = projectManager.getCurrentProject();
		currentProject.getXmlHeader().virtualScreenHeight = 1000;
		currentProject.getXmlHeader().virtualScreenWidth = 1000;

		testImage = TestUtils.saveFileToProject(currentProject.getName(), "testImage.png", IMAGE_FILE_ID, getContext(),
				0);
		testSound = TestUtils.saveFileToProject(currentProject.getName(), "testSound.mp3", R.raw.testsound,
				getContext(), 1);
		ProjectManager.getInstance().saveProject();
	}

	@Override
	protected void tearDown() throws Exception {
		if (testImage != null && testImage.exists()) {
			testImage.delete();
		}
		if (testSound != null && testSound.exists()) {
			testSound.delete();
		}
		TestUtils.deleteTestProjects();
		super.tearDown();
	}

	public void testContainer() throws IOException, InterruptedException {
		Project currentProject = projectManager.getCurrentProject();

		storageHandler.copyImage(currentProject.getName(), testImage.getAbsolutePath(), null);

		String checksumImage = Utils.md5Checksum(testImage);

		FileChecksumContainer fileChecksumContainer = projectManager.getFileChecksumContainer();
		assertTrue("Checksum isn't in container", fileChecksumContainer.containsChecksum(checksumImage));

		//wait to get a different timestamp on next file
		Thread.sleep(2000);

		File newTestImage = storageHandler.copyImage(currentProject.getName(), testImage.getAbsolutePath(), null);
		File imageDirectory = new File(Constants.DEFAULT_ROOT + "/" + currentProject.getName() + "/"
				+ Constants.IMAGE_DIRECTORY + "/");
		File[] filesImage = imageDirectory.listFiles();

		//nomedia file is also in images folder
		assertEquals("Wrong amount of files in imagefolder", 2, filesImage.length);

		File newTestSound = storageHandler.copySoundFile(testSound.getAbsolutePath());
		String checksumSound = Utils.md5Checksum(testSound);
		assertTrue("Checksum isn't in container", fileChecksumContainer.containsChecksum(checksumSound));
		File soundDirectory = new File(Constants.DEFAULT_ROOT + "/" + currentProject.getName() + "/"
				+ Constants.SOUND_DIRECTORY);
		File[] filesSound = soundDirectory.listFiles();

		//nomedia file is also in sounds folder
		assertEquals("Wrong amount of files in soundfolder", 2, filesSound.length);

		fileChecksumContainer.decrementUsage(newTestImage.getAbsolutePath());
		assertTrue("Checksum was deleted", fileChecksumContainer.containsChecksum(checksumImage));
		fileChecksumContainer.decrementUsage(newTestImage.getAbsolutePath());
		assertFalse("Checksum wasn't deleted", fileChecksumContainer.containsChecksum(checksumImage));
		fileChecksumContainer.decrementUsage(newTestSound.getAbsolutePath());
		assertFalse("Checksum wasn't deleted", fileChecksumContainer.containsChecksum(checksumSound));
	}

	public void testDeleteFile() throws IOException, InterruptedException {
		Project currentProject = projectManager.getCurrentProject();
		File newTestImage1 = storageHandler.copyImage(currentProject.getName(), testImage.getAbsolutePath(), null);
		//wait to get a different timestamp on next file
		Thread.sleep(2000);

		storageHandler.deleteFile(newTestImage1.getAbsolutePath());
		File imageDirectory = new File(Constants.DEFAULT_ROOT + "/" + currentProject.getName() + "/"
				+ Constants.IMAGE_DIRECTORY);
		File[] filesImage = imageDirectory.listFiles();
		assertEquals("Wrong amount of files in folder", 1, filesImage.length);

		File newTestSound = storageHandler.copySoundFile(testSound.getAbsolutePath());
		storageHandler.deleteFile(newTestSound.getAbsolutePath());

		File soundDirectory = new File(Constants.DEFAULT_ROOT + "/" + currentProject.getName() + "/"
				+ Constants.SOUND_DIRECTORY);
		File[] filesSound = soundDirectory.listFiles();

		assertEquals("Wrong amount of files in folder", 1, filesSound.length);
	}
}
