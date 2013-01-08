package org.catrobat.catroid.uitest.content.brick;

import java.util.ArrayList;

import org.catrobat.catroid.ProjectManager;
import org.catrobat.catroid.R;
import org.catrobat.catroid.content.Project;
import org.catrobat.catroid.content.Script;
import org.catrobat.catroid.content.Sprite;
import org.catrobat.catroid.content.StartScript;
import org.catrobat.catroid.content.bricks.Brick;
import org.catrobat.catroid.content.bricks.SetPhysicObjectTypeBrick;
import org.catrobat.catroid.physics.PhysicObject.Type;
import org.catrobat.catroid.ui.ScriptTabActivity;
import org.catrobat.catroid.ui.adapter.BrickAdapter;
import org.catrobat.catroid.ui.fragment.ScriptFragment;
import org.catrobat.catroid.uitest.util.UiTestUtils;

import android.test.ActivityInstrumentationTestCase2;
import android.test.suitebuilder.annotation.Smoke;

import com.jayway.android.robotium.solo.Solo;

public class SetPhysicObjectTypeBrickTest extends ActivityInstrumentationTestCase2<ScriptTabActivity> {
	private Solo solo;
	private Project project;
	private SetPhysicObjectTypeBrick setPhysicObjectTypeBrick;

	public SetPhysicObjectTypeBrickTest() {
		super(ScriptTabActivity.class);
	}

	@Override
	public void setUp() throws Exception {
		createProject();
		solo = new Solo(getInstrumentation(), getActivity());
	}

	@Override
	public void tearDown() throws Exception {
		try {
			solo.finalize();
		} catch (Throwable e) {
			e.printStackTrace();
		}

		getActivity().finish();
		super.tearDown();
	}

	@Smoke
	public void testPhysicObjectTypeBrick() {
		ScriptTabActivity activity = (ScriptTabActivity) solo.getCurrentActivity();
		ScriptFragment fragment = (ScriptFragment) activity.getTabFragment(ScriptTabActivity.INDEX_TAB_SCRIPTS);
		BrickAdapter adapter = fragment.getAdapter();

		int childrenCount = adapter.getChildCountFromLastGroup();
		int groupCount = adapter.getScriptCount();

		assertEquals("Incorrect number of bricks.", 2 + 1, solo.getCurrentListViews().get(0).getChildCount()); // don't forget the footer
		assertEquals("Incorrect number of bricks.", 1, childrenCount);

		ArrayList<Brick> projectBrickList = project.getSpriteList().get(0).getScript(0).getBrickList();
		assertEquals("Incorrect number of bricks.", 1, projectBrickList.size());

		assertEquals("Wrong Brick instance.", projectBrickList.get(0), adapter.getChild(groupCount - 1, 0));
		String textSetPhysicObjectType = solo.getString(R.string.brick_set_physic_object_type);
		assertNotNull("TextView does not exist.", solo.getText(textSetPhysicObjectType));

		checkSpinnerItemPressed(0);
		checkSpinnerItemPressed(1);
		checkSpinnerItemPressed(2);
	}

	private void checkSpinnerItemPressed(int spinnerItemIndex) {
		String[] physicObjectTypes = getActivity().getResources().getStringArray(R.array.physic_object_type);

		solo.pressSpinnerItem(0, spinnerItemIndex);
		solo.sleep(200);
		solo.waitForActivity(ScriptTabActivity.class.getSimpleName());

		Type choosenPhysicType = (Type) UiTestUtils.getPrivateField("type", setPhysicObjectTypeBrick);
		assertEquals("Wrong text in field.", Type.values()[spinnerItemIndex], choosenPhysicType);
		assertEquals("Value in Brick is not updated.", physicObjectTypes[spinnerItemIndex], solo.getCurrentSpinners()
				.get(0).getSelectedItem());
	}

	private void createProject() {
		project = new Project(null, "testProject");
		Sprite sprite = new Sprite("cat");
		Script script = new StartScript(sprite);
		setPhysicObjectTypeBrick = new SetPhysicObjectTypeBrick(sprite, Type.DYNAMIC);
		script.addBrick(setPhysicObjectTypeBrick);

		sprite.addScript(script);
		project.addSprite(sprite);

		ProjectManager.getInstance().setProject(project);
		ProjectManager.getInstance().setCurrentSprite(sprite);
		ProjectManager.getInstance().setCurrentScript(script);
	}

}
