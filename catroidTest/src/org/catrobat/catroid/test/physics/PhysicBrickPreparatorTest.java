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
package org.catrobat.catroid.test.physics;

import java.util.HashSet;
import java.util.Set;

import org.catrobat.catroid.content.BroadcastScript;
import org.catrobat.catroid.content.Project;
import org.catrobat.catroid.content.Script;
import org.catrobat.catroid.content.Sprite;
import org.catrobat.catroid.content.StartScript;
import org.catrobat.catroid.content.WhenScript;
import org.catrobat.catroid.content.bricks.Brick;
import org.catrobat.catroid.content.bricks.BroadcastBrick;
import org.catrobat.catroid.content.bricks.ChangeGhostEffectByNBrick;
import org.catrobat.catroid.content.bricks.HideBrick;
import org.catrobat.catroid.content.bricks.IfOnEdgeBounceBrick;
import org.catrobat.catroid.content.bricks.SetBounceFactorBrick;
import org.catrobat.catroid.content.bricks.SetFrictionBrick;
import org.catrobat.catroid.content.bricks.SetGravityBrick;
import org.catrobat.catroid.content.bricks.SetMassBrick;
import org.catrobat.catroid.content.bricks.SetPhysicObjectTypeBrick;
import org.catrobat.catroid.content.bricks.SetXBrick;
import org.catrobat.catroid.content.bricks.TurnLeftBrick;
import org.catrobat.catroid.content.bricks.TurnRightSpeedBrick;
import org.catrobat.catroid.physics.PhysicBrickPreparator;
import org.catrobat.catroid.physics.PhysicObject;
import org.catrobat.catroid.physics.PhysicObject.Type;
import org.catrobat.catroid.physics.PhysicObjectBrick;
import org.catrobat.catroid.physics.PhysicWorld;
import org.catrobat.catroid.physics.PhysicWorldBrick;
import org.catrobat.catroid.test.utils.TestUtils;

import android.test.AndroidTestCase;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.GdxNativesLoader;

public class PhysicBrickPreparatorTest extends AndroidTestCase {
	static {
		GdxNativesLoader.load();
	}

	@Override
	public void setUp() {
	}

	@Override
	public void tearDown() {
	}

	public void testNoPhysicBricksToPrepare() {
		Project project = new Project();
		Sprite sprite = new Sprite("TestSprite");

		Script startScript = new StartScript(sprite);
		startScript.addBrick(new SetXBrick(sprite, 0));
		startScript.addBrick(new ChangeGhostEffectByNBrick(sprite, 5.0f));

		Script broadcastScript = new BroadcastScript(sprite);
		broadcastScript.addBrick(new HideBrick(sprite));
		broadcastScript.addBrick(new BroadcastBrick(sprite));

		Script whenScript = new WhenScript(sprite);
		whenScript.addBrick(new TurnLeftBrick(sprite, 2.123));

		sprite.addScript(startScript);
		sprite.addScript(broadcastScript);
		sprite.addScript(whenScript);
		project.addSprite(sprite);

		PhysicWorldMock physicWorldMock = new PhysicWorldMock();
		PhysicBrickPreparator physicBrickPreparator = new PhysicBrickPreparator(physicWorldMock);
		physicBrickPreparator.prepare(project);

		assertEquals("Physic objects have been created", 0, physicWorldMock.getPhysicObjectExecutedCount);
	}

	public void testSimplePhysicObjectBrick() {
		Project project = new Project();
		Sprite sprite = new Sprite("TestSprite");
		Script script = new StartScript(sprite);
		PhysicObjectBrick physicObjectBrick = new SetMassBrick(sprite, PhysicObject.DEFAULT_MASS);
		script.addBrick(physicObjectBrick);
		sprite.addScript(script);
		project.addSprite(sprite);

		assertNull("Physic object already has been set",
				TestUtils.getPrivateField("physicObject", physicObjectBrick, false));

		PhysicWorldMock physicWorldMock = new PhysicWorldMock();
		PhysicBrickPreparator physicBrickPreparator = new PhysicBrickPreparator(physicWorldMock);
		physicBrickPreparator.prepare(project);

		assertEquals("Wrong number of get physic object calls", 1, physicWorldMock.getPhysicObjectExecutedCount);
		assertEquals("Wrong number of physic objects stored", 1, physicWorldMock.physicObjects.size());
		assertTrue("Physic world hasn't stored the right physic object", physicWorldMock.physicObjects.contains(sprite));

		assertNotNull("Brick has no physic object", TestUtils.getPrivateField("physicObject", physicObjectBrick, false));
		assertEquals("Brick has wrong physic object", physicWorldMock.getPhysicObject(sprite),
				TestUtils.getPrivateField("physicObject", physicObjectBrick, false));
	}

	public void testMultiplePhysicBricksInOneSprite() {
		Project project = new Project();
		Sprite sprite = new Sprite("TestSprite");
		Script script = new StartScript(sprite);
		PhysicObjectBrick[] physicObjectBricks = { new SetMassBrick(sprite, PhysicObject.DEFAULT_MASS),
				new TurnRightSpeedBrick(sprite, 0.0f), new SetPhysicObjectTypeBrick(sprite, Type.DYNAMIC) };
		for (PhysicObjectBrick physicObjectBrick : physicObjectBricks) {
			script.addBrick(physicObjectBrick);
		}
		sprite.addScript(script);
		project.addSprite(sprite);

		for (PhysicObjectBrick physicObjectBrick : physicObjectBricks) {
			assertNull("Physic object already has been set",
					TestUtils.getPrivateField("physicObject", physicObjectBrick, false));
		}

		PhysicWorldMock physicWorldMock = new PhysicWorldMock();
		PhysicBrickPreparator physicBrickPreparator = new PhysicBrickPreparator(physicWorldMock);
		physicBrickPreparator.prepare(project);

		assertEquals("Wrong number of get physic object calls", 1, physicWorldMock.getPhysicObjectExecutedCount);
		assertEquals("Wrong number of physic objects stored", 1, physicWorldMock.physicObjects.size());
		assertTrue("Physic world hasn't stored the right physic object", physicWorldMock.physicObjects.contains(sprite));

		PhysicObject physicObject = physicWorldMock.getPhysicObject(sprite);
		for (PhysicObjectBrick physicObjectBrick : physicObjectBricks) {
			assertNotNull("Brick has no physic object",
					TestUtils.getPrivateField("physicObject", physicObjectBrick, false));
			assertEquals("Brick has wrong physic object", physicObject,
					TestUtils.getPrivateField("physicObject", physicObjectBrick, false));
		}
	}

	public void testSimplePhysicWorldBrick() {
		Project project = new Project();
		Sprite sprite = new Sprite("TestSprite");
		Script script = new StartScript(sprite);
		PhysicWorldBrick physicWorldBrick = new SetGravityBrick(sprite, PhysicWorld.DEFAULT_GRAVITY);
		script.addBrick(physicWorldBrick);
		sprite.addScript(script);
		project.addSprite(sprite);

		assertNull("Physic world has already been set",
				TestUtils.getPrivateField("physicWorld", physicWorldBrick, false));

		PhysicWorldMock physicWorldMock = new PhysicWorldMock();
		PhysicBrickPreparator physicBrickPreparator = new PhysicBrickPreparator(physicWorldMock);
		physicBrickPreparator.prepare(project);

		assertNotNull("Physic world hasn't been set", TestUtils.getPrivateField("physicWorld", physicWorldBrick, false));
		assertEquals("Wrong physic world", physicWorldMock,
				TestUtils.getPrivateField("physicWorld", physicWorldBrick, false));
	}

	public void testMultipleScriptBricksIncludingMultiplePhysicBricksInOneSprite() {
		Vector2 gravity = new Vector2(1.2f, -3.4f);
		Project project = new Project();
		Sprite sprite = new Sprite("TestSprite");

		Script startScript = new StartScript(sprite);
		startScript.addBrick(new SetXBrick(sprite, 0));
		startScript.addBrick(new ChangeGhostEffectByNBrick(sprite, 5.0f));
		startScript.addBrick(new SetGravityBrick(sprite, PhysicWorld.DEFAULT_GRAVITY));

		Script broadcastScript = new BroadcastScript(sprite);
		broadcastScript.addBrick(new HideBrick(sprite));
		broadcastScript.addBrick(new BroadcastBrick(sprite));
		broadcastScript.addBrick(new SetGravityBrick(sprite, gravity));

		Script whenScript = new WhenScript(sprite);
		whenScript.addBrick(new IfOnEdgeBounceBrick(sprite));
		whenScript.addBrick(new SetPhysicObjectTypeBrick(sprite, Type.FIXED));
		whenScript.addBrick(new SetBounceFactorBrick(sprite, 1.0f));
		whenScript.addBrick(new SetFrictionBrick(sprite, 0.5f));

		sprite.addScript(startScript);
		sprite.addScript(broadcastScript);
		sprite.addScript(whenScript);
		project.addSprite(sprite);

		PhysicWorldMock physicWorldMock = new PhysicWorldMock();
		PhysicBrickPreparator physicBrickPreparator = new PhysicBrickPreparator(physicWorldMock);
		physicBrickPreparator.prepare(project);

		assertEquals("Wrong number of get physic object calls", 1, physicWorldMock.getPhysicObjectExecutedCount);
		assertEquals("Wrong number of physic objects stored", 1, physicWorldMock.physicObjects.size());
		assertTrue("Physic world hasn't stored the right physic object", physicWorldMock.physicObjects.contains(sprite));

		PhysicObject physicObject = physicWorldMock.getPhysicObject(sprite);
		for (int index = 0; index < sprite.getNumberOfScripts(); index++) {
			Script script = sprite.getScript(index);

			for (Brick brick : script.getBrickList()) {
				if (brick instanceof PhysicWorldBrick) {
					assertEquals("Wrong physic world", physicWorldMock,
							TestUtils.getPrivateField("physicWorld", brick, false));
				} else if (brick instanceof PhysicObjectBrick) {
					assertEquals("Wrong physic object", physicObject,
							TestUtils.getPrivateField("physicObject", brick, false));
				}
			}
		}
	}

	private class PhysicWorldMock extends PhysicWorld {
		private int getPhysicObjectExecutedCount = 0;
		private Set<Sprite> physicObjects = new HashSet<Sprite>();

		@Override
		public PhysicObject getPhysicObject(Sprite sprite) {
			PhysicObject physicObject = super.getPhysicObject(sprite);

			getPhysicObjectExecutedCount++;
			physicObjects.add(sprite);

			return physicObject;
		}
	}
}
