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
package org.catrobat.catroid.ui.controller;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;

import org.catrobat.catroid.ProjectManager;
import org.catrobat.catroid.R;
import org.catrobat.catroid.content.Script;
import org.catrobat.catroid.content.Sprite;
import org.catrobat.catroid.content.bricks.AllowedAfterDeadEndBrick;
import org.catrobat.catroid.content.bricks.Brick;
import org.catrobat.catroid.content.bricks.DeadEndBrick;
import org.catrobat.catroid.content.bricks.FormulaBrick;
import org.catrobat.catroid.content.bricks.NestingBrick;
import org.catrobat.catroid.content.bricks.ScriptBrick;
import org.catrobat.catroid.ui.SoundViewHolder;
import org.catrobat.catroid.ui.adapter.ScriptAdapter;
import org.catrobat.catroid.ui.adapter.SoundBaseAdapter;
import org.catrobat.catroid.ui.dragndrop.DragAndDropListView;
import org.catrobat.catroid.ui.fragment.FormulaEditorFragment;

import java.util.ArrayList;
import java.util.List;

public class ScriptController {

	private static final String TAG = ScriptController.class.getSimpleName();
	private static final int ALPHA_FULL = 255;
	private static final int ALPHA_GREYED = 100;

	private static ScriptController instance;

	public static ScriptController getInstance() {
		if (instance == null) {
			instance = new ScriptController();
		}
		return instance;
	}

	public void updateScriptLogic(Context context, final int position, final SoundViewHolder holder,
			final SoundBaseAdapter soundAdapter) {

	}

	public void copyBrick(Brick brick, ScriptAdapter adapter, Sprite sprite) {

		if (brick instanceof ScriptBrick) {
			Script scriptToEdit = ((ScriptBrick) brick).initScript(ProjectManager.getInstance().getCurrentSprite());

			Script clonedScript = scriptToEdit.copyScriptForSprite(sprite);

			sprite.addScript(clonedScript);
			adapter.initBrickList();
			adapter.notifyDataSetChanged();

			return;
		}
		int brickId = adapter.getBrickList().indexOf(brick);
		if (brickId == -1) {
			return;
		}

		int newPosition = adapter.getCount();

		Brick copy = brick.clone();

		Script scriptList;
		scriptList = ProjectManager.getInstance().getCurrentScript();
		scriptList.addBrick(copy);
		adapter.addNewMultipleBricks(newPosition, copy);
		adapter.initBrickList();

		ProjectManager.getInstance().saveProject();
		adapter.notifyDataSetChanged();
	}

	private void deleteBrick(Brick brick, ScriptAdapter adapter, Sprite sprite) {

		if (brick instanceof ScriptBrick) {
			Script scriptToEdit = ((ScriptBrick) brick).initScript(ProjectManager.getInstance().getCurrentSprite());
			adapter.handleScriptDelete(sprite, scriptToEdit);
			return;
		}
		int brickId = adapter.getBrickList().indexOf(brick);
		if (brickId == -1) {
			return;
		}
		adapter.removeFromBrickListAndProject(brickId, true);
	}

	public void deleteCheckedBricks(ScriptAdapter adapter, Sprite sprite) {
		List<Brick> checkedBricks = adapter.getReversedCheckedBrickList();

		for (Brick brick : checkedBricks) {
			deleteBrick(brick, adapter, sprite);
		}
	}

	public int getNewPositionIfEndingBrickIsThere(int to, Brick brick, List<Brick> brickList, ScriptAdapter adapter) {
		int currentPosition = brickList.indexOf(brick);

		if (adapter.getItem(to) instanceof AllowedAfterDeadEndBrick && !(adapter.getItem(to) instanceof DeadEndBrick)
				&& adapter.getItem(to - 1) instanceof DeadEndBrick) {
			if (currentPosition > to) {
				return to + 1;
			} else {
				return to;
			}
		} else if (adapter.getItem(to) instanceof DeadEndBrick) {
			for (int item = to - 1; item >= 0; item--) {
				if (!(adapter.getItem(item) instanceof DeadEndBrick)) {
					if (currentPosition > item) {
						return item + 1;
					} else {
						return item;
					}
				}
			}
		}

		return to;
	}

	public int getDraggedNestingBricksToPosition(NestingBrick nestingBrick, int from, int to, List<Brick> brickList) {
		List<NestingBrick> nestingBrickList = nestingBrick.getAllNestingBrickParts(true);
		int restrictedTop = 0;
		int restrictedBottom = brickList.size();

		int tempPosition;
		int currentPosition = to;
		boolean passedBrick = false;
		for (NestingBrick temp : nestingBrickList) {
			tempPosition = brickList.indexOf(temp);
			if (temp != nestingBrick) {
				if (!passedBrick) {
					restrictedTop = tempPosition;
				}
				if (passedBrick) {
					restrictedBottom = tempPosition;
					break;
				}
			} else {
				passedBrick = true;
				currentPosition = tempPosition;
			}
		}

		for (int i = currentPosition; i > restrictedTop; i--) {
			if (checkIfScriptOrOtherNestingBrick(brickList.get(i), nestingBrickList)) {
				restrictedTop = i;
				break;
			}
		}

		for (int i = currentPosition; i < restrictedBottom; i++) {
			if (checkIfScriptOrOtherNestingBrick(brickList.get(i), nestingBrickList)) {
				restrictedBottom = i;
				break;
			}
		}

		to = to <= restrictedTop ? restrictedTop + 1 : to;
		to = to >= restrictedBottom ? restrictedBottom - 1 : to;

		return to;
	}

	private boolean checkIfScriptOrOtherNestingBrick(Brick brick, List<NestingBrick> nestingBrickList) {
		if (brick instanceof ScriptBrick) {
			return true;
		}
		if (brick instanceof NestingBrick && !nestingBrickList.contains(brick)) {
			return true;
		}

		return false;
	}

	public void addScriptToProject(int position, ScriptBrick scriptBrick, List<Brick> brickList, Sprite sprite) {
		Sprite currentSprite = ProjectManager.getInstance().getCurrentSprite();

		int[] temp = getScriptAndBrickIndexFromProject(position, brickList, sprite);

		int scriptPosition = temp[0];
		int brickPosition = temp[1];

		Script newScript = scriptBrick.initScript(currentSprite);
		if (currentSprite.getNumberOfBricks() > 0) {
			int addScriptTo = position == 0 ? 0 : scriptPosition + 1;
			currentSprite.addScript(addScriptTo, newScript);
		} else {
			currentSprite.addScript(newScript);
		}

		Script previousScript = currentSprite.getScript(scriptPosition);
		if (previousScript != null) {
			Brick brick;
			int size = previousScript.getBrickList().size();
			for (int i = brickPosition; i < size; i++) {
				brick = previousScript.getBrick(brickPosition);
				previousScript.removeBrick(brick);
				newScript.addBrick(brick);
			}
		}
		ProjectManager.getInstance().setCurrentScript(newScript);
	}

	public void moveExistingProjectBrick(int from, int to, Brick draggedBrick, List<Brick> brickList, Sprite sprite) {
		Sprite currentSprite = ProjectManager.getInstance().getCurrentSprite();

		int[] tempFrom = getScriptAndBrickIndexFromProject(from, brickList, sprite);
		int scriptPositionFrom = tempFrom[0];
		int brickPositionFrom = tempFrom[1];

		Script fromScript = currentSprite.getScript(scriptPositionFrom);

		Brick brick = fromScript.getBrick(brickPositionFrom);
		if (draggedBrick != brick) {
			Log.e(TAG, "Want to save wrong brick");
			return;
		}
		fromScript.removeBrick(brick);

		int[] tempTo = getScriptAndBrickIndexFromProject(to, brickList, sprite);
		int scriptPositionTo = tempTo[0];
		int brickPositionTo = tempTo[1];

		Script toScript = currentSprite.getScript(scriptPositionTo);

		toScript.addBrick(brickPositionTo, brick);
	}

	//TODO: brick param removed, was there twice.. correct?
	public void addBrickToPosition(int position, Brick draggedBrick, List<Brick> brickList, Sprite sprite) {
		Sprite currentSprite = ProjectManager.getInstance().getCurrentSprite();

		int[] temp = getScriptAndBrickIndexFromProject(position, brickList, sprite);

		int scriptPosition = temp[0];
		int brickPosition = temp[1];

		Script script = currentSprite.getScript(scriptPosition);

		if (draggedBrick instanceof NestingBrick) { // TODO: was brick
			((NestingBrick) draggedBrick).initialize();
			List<NestingBrick> nestingBrickList = ((NestingBrick) draggedBrick).getAllNestingBrickParts(true);
			for (int i = 0; i < nestingBrickList.size(); i++) {
				if (nestingBrickList.get(i) instanceof DeadEndBrick) {
					if (i < nestingBrickList.size() - 1) {
						Log.w(TAG, "Adding a DeadEndBrick in the middle of the NestingBricks");
					}
					position = getPositionForDeadEndBrick(position, brickList);
					temp = getScriptAndBrickIndexFromProject(position, brickList, sprite);
					script.addBrick(temp[1], nestingBrickList.get(i));
				} else {
					script.addBrick(brickPosition + i, nestingBrickList.get(i));
				}
			}
		} else {
			script.addBrick(brickPosition, draggedBrick); // TODO: was brick
		}
	}

	private int getPositionForDeadEndBrick(int position, List<Brick> brickList) {
		for (int i = position + 1; i < brickList.size(); i++) {
			if (brickList.get(i) instanceof AllowedAfterDeadEndBrick || brickList.get(i) instanceof DeadEndBrick) {
				return i;
			}

			if (brickList.get(i) instanceof NestingBrick) {
				List<NestingBrick> tempList = ((NestingBrick) brickList.get(i)).getAllNestingBrickParts(true);
				int currentPosition = i;
				i = brickList.indexOf(tempList.get(tempList.size() - 1)) + 1;
				if (i < 0) {
					i = currentPosition;
				} else if (i >= brickList.size()) {
					return brickList.size();
				}
			}

			if (brickList.get(i) instanceof AllowedAfterDeadEndBrick || brickList.get(i) instanceof DeadEndBrick) {
				return i;
			}
		}

		return brickList.size();
	}

	public int[] getScriptAndBrickIndexFromProject(int position, List<Brick> brickList, Sprite sprite) {
		int[] returnValue = new int[2];

		if (position >= brickList.size()) {

			returnValue[0] = sprite.getNumberOfScripts() - 1;
			if (returnValue[0] < 0) {
				returnValue[0] = 0;
				returnValue[1] = 0;
			} else {
				Script script = sprite.getScript(returnValue[0]);
				if (script != null) {
					returnValue[1] = script.getBrickList().size();
				} else {
					returnValue[1] = 0;
				}
			}

			return returnValue;
		}

		int scriptPosition = 0;
		int scriptOffset;
		for (scriptOffset = 0; scriptOffset < position;) {
			scriptOffset += sprite.getScript(scriptPosition).getBrickList().size() + 1;
			if (scriptOffset < position) {
				scriptPosition++;
			}
		}
		scriptOffset -= sprite.getScript(scriptPosition).getBrickList().size();

		returnValue[0] = scriptPosition;
		List<Brick> brickListFromProject = sprite.getScript(scriptPosition).getBrickList();
		int brickPosition = position;
		if (scriptOffset > 0) {
			brickPosition -= scriptOffset;
		}

		Brick brickFromProject;
		if (brickListFromProject.size() != 0 && brickPosition < brickListFromProject.size()) {
			brickFromProject = brickListFromProject.get(brickPosition);
		} else {
			brickFromProject = null;
		}

		returnValue[1] = sprite.getScript(scriptPosition).getBrickList().indexOf(brickFromProject);
		if (returnValue[1] < 0) {
			returnValue[1] = sprite.getScript(scriptPosition).getBrickList().size();
		}

		return returnValue;
	}

	public void scrollToPosition(final int position, DragAndDropListView dragAndDropListView, List<Brick> brickList) {
		DragAndDropListView list = dragAndDropListView;
		if (list.getFirstVisiblePosition() < position && position < list.getLastVisiblePosition()) {
			return;
		}

		list.setIsScrolling();
		if (position <= list.getFirstVisiblePosition()) {
			list.smoothScrollToPosition(0, position + 2);
		} else {
			list.smoothScrollToPosition(brickList.size() - 1, position - 2);
		}

	}

	public int getNewPositionForScriptBrick(int position, Brick brick, List<Brick> brickList) {
		if (brickList.size() == 0) {
			return 0;
		}
		if (!(brick instanceof ScriptBrick)) {
			return position;
		}

		int lastPossiblePosition = position;
		int nextPossiblePosition = position;

		for (int currentPosition = position; currentPosition < brickList.size(); currentPosition++) {
			if (brickList.get(currentPosition) instanceof NestingBrick) {
				List<NestingBrick> bricks = ((NestingBrick) brickList.get(currentPosition))
						.getAllNestingBrickParts(true);
				int beginningPosition = brickList.indexOf(bricks.get(0));
				int endingPosition = brickList.indexOf(bricks.get(bricks.size() - 1));
				if (position >= beginningPosition && position <= endingPosition) {
					lastPossiblePosition = beginningPosition;
					nextPossiblePosition = endingPosition;
					currentPosition = endingPosition;
				}
			}

			if (brickList.get(currentPosition) instanceof ScriptBrick && brickList.get(currentPosition) != brick) {
				break;
			}
		}

		if (position <= lastPossiblePosition) {
			return position;
		} else if (position - lastPossiblePosition < nextPossiblePosition - position) {
			return lastPossiblePosition;
		} else {
			return nextPossiblePosition;
		}
	}

	public int getScriptIndexFromProject(int index, Sprite sprite) {
		int scriptIndex = 0;
		for (int position = 0; position < index;) {

			position += sprite.getScript(scriptIndex).getBrickList().size() + 1;
			if (position <= index) {
				scriptIndex++;
			}
		}
		return scriptIndex;
	}

	public int getChildCountFromLastGroup() {
		return ProjectManager.getInstance().getCurrentSprite().getScript(getScriptCount() - 1).getBrickList().size();
	}

	public Brick getChild(int scriptPosition, int brickPosition) {
		Sprite sprite = ProjectManager.getInstance().getCurrentSprite();

		return sprite.getScript(scriptPosition).getBrick(brickPosition);
	}

	public int getScriptCount() {
		return ProjectManager.getInstance().getCurrentSprite().getNumberOfScripts();
	}

	public void onClick(final View view, final List<Brick> brickList, final Context context, Sprite sprite,
			final DragAndDropListView dragAndDropListView, int selectMode) {
		final int itemPosition = calculateItemPositionAndTouchPointY(view, dragAndDropListView);
		final List<CharSequence> items = new ArrayList<CharSequence>();

		if (brickList.get(itemPosition) instanceof ScriptBrick) {
			int scriptIndex = getScriptIndexFromProject(itemPosition, sprite);
			ProjectManager.getInstance().setCurrentScript(sprite.getScript(scriptIndex));
		}

		if (!(brickList.get(itemPosition) instanceof DeadEndBrick)
				&& !(brickList.get(itemPosition) instanceof ScriptBrick)) {
			items.add(context.getText(R.string.brick_context_dialog_move_brick));
		}
		if (brickList.get(itemPosition) instanceof NestingBrick) {
			items.add(context.getText(R.string.brick_context_dialog_animate_bricks));
		}
		if (!(brickList.get(itemPosition) instanceof ScriptBrick)) {
			items.add(context.getText(R.string.brick_context_dialog_copy_brick));
			items.add(context.getText(R.string.brick_context_dialog_delete_brick));
		} else {
			items.add(context.getText(R.string.brick_context_dialog_delete_script));
		}
		if (brickList.get(itemPosition) instanceof FormulaBrick) {
			items.add(context.getText(R.string.brick_context_dialog_formula_edit_brick));
		}

		AlertDialog.Builder builder = new AlertDialog.Builder(context);

		boolean drawingCacheEnabled = view.isDrawingCacheEnabled();
		view.setDrawingCacheEnabled(true);
		view.setDrawingCacheBackgroundColor(Color.TRANSPARENT);
		view.buildDrawingCache(true);

		if (view.getDrawingCache() != null) {
			Bitmap bitmap = Bitmap.createBitmap(view.getDrawingCache());
			view.setDrawingCacheEnabled(drawingCacheEnabled);

			ImageView imageView = dragAndDropListView.getGlowingBorder(bitmap);
			builder.setCustomTitle(imageView);
		}

		builder.setItems(items.toArray(new CharSequence[items.size()]), new DialogInterface.OnClickListener() {
			private ScriptAdapter adapter;

			@Override
			public void onClick(DialogInterface dialog, int item) {
				CharSequence clickedItemText = items.get(item);
				if (clickedItemText.equals(context.getText(R.string.brick_context_dialog_move_brick))) {
					view.performLongClick();
				} else if (clickedItemText.equals(context.getText(R.string.brick_context_dialog_copy_brick))) {
					copyBrickListAndProject(itemPosition, dragAndDropListView, adapter);
				} else if (clickedItemText.equals(context.getText(R.string.brick_context_dialog_delete_brick))
						|| clickedItemText.equals(context.getText(R.string.brick_context_dialog_delete_script))) {
					adapter.showConfirmDeleteScriptDialog(itemPosition);
				} else if (clickedItemText.equals(context.getText(R.string.brick_context_dialog_animate_bricks))) {
					int itemPosition = calculateItemPositionAndTouchPointY(view, dragAndDropListView);
					Brick brick = brickList.get(itemPosition);
					if (brick instanceof NestingBrick) {
						List<NestingBrick> list = ((NestingBrick) brick).getAllNestingBrickParts(true);
						for (Brick tempBrick : list) {
							adapter.addElementToAnimatedBricks(tempBrick);
						}
					}
					adapter.notifyDataSetChanged();
				} else if (clickedItemText.equals(context.getText(R.string.brick_context_dialog_formula_edit_brick))) {

					if (brickList.get(itemPosition) instanceof FormulaBrick) {
						FormulaEditorFragment.showFragment(view, brickList.get(itemPosition),
								((FormulaBrick) brickList.get(itemPosition)).getFormula());
					}
				}
			}
		});
		AlertDialog alertDialog = builder.create();

		if ((selectMode == ListView.CHOICE_MODE_NONE)) {
			alertDialog.show();
		}
	}

	protected void copyBrickListAndProject(int itemPosition, DragAndDropListView dragAndDropListView,
			ScriptAdapter adapter) {
		Brick copy;
		Brick origin = (Brick) (dragAndDropListView.getItemAtPosition(itemPosition));
		copy = origin.clone();
		adapter.addNewBrick(itemPosition, copy);
	}

	private int calculateItemPositionAndTouchPointY(View view, DragAndDropListView dragAndDropListView) {
		int itemPosition = AdapterView.INVALID_POSITION;
		itemPosition = dragAndDropListView.pointToPosition(view.getLeft(), view.getTop());

		return itemPosition;
	}

	public void handleCheck(Brick brick, boolean isChecked, int selectMode, ScriptAdapter adapter, List<Brick> brickList) {
		if (brick == null) {
			return;
		}
		if (isChecked) {
			if (selectMode == ListView.CHOICE_MODE_SINGLE) {
				adapter.clearCheckedItems();
			}
			if (brick.getCheckBox() != null && smartBrickSelection(brick, isChecked, adapter, brickList)) {
				return;
			}
			adapter.addElementToCheckedBricks(brick);
		} else {
			if (brick.getCheckBox() != null && smartBrickSelection(brick, isChecked, adapter, brickList)) {
				return;
			}
			adapter.removeElementFromCheckedBricks(brick);
		}
		adapter.notifyDataSetChanged();

		if (adapter.getOnBrickEditListener() != null) {
			adapter.getOnBrickEditListener().onBrickChecked();
		}
	}

	private void handleBrickEnabledState(Brick brick, boolean enableState) {
		if (brick.getCheckBox() != null) {
			brick.getCheckBox().setEnabled(enableState);
		}
		if (enableState) {
			brick.getViewWithAlpha(ALPHA_FULL);
		} else {
			brick.getViewWithAlpha(ALPHA_GREYED);
		}
	}

	public void enableAllBricks(List<Brick> brickList) {
		for (Brick brick : brickList) {
			if (brick.getCheckBox() != null) {
				brick.getCheckBox().setEnabled(true);
			}
			brick.getViewWithAlpha(ALPHA_FULL);
		}
	}

	public boolean smartBrickSelection(Brick brick, boolean checked, ScriptAdapter adapter, List<Brick> brickList) {

		if (brick instanceof ScriptBrick) {

			if (checked) {
				adapter.addElementToCheckedBricks(brick);
				adapter.addElementToAnimatedBricks(brick);
			} else {
				adapter.removeElementFromCheckedBricks(brick);
			}

			int brickPosition = brickList.indexOf(brick) + 1;
			while ((brickPosition < brickList.size()) && !(brickList.get(brickPosition) instanceof ScriptBrick)) {
				Brick currentBrick = brickList.get(brickPosition);
				if (currentBrick == null) {
					break;
				}
				if (checked) {
					adapter.addElementToCheckedBricks(currentBrick);
					adapter.addElementToAnimatedBricks(currentBrick);
				} else {
					adapter.removeElementFromCheckedBricks(currentBrick);
				}
				if (currentBrick.getCheckBox() != null) {
					currentBrick.getCheckBox().setChecked(checked);
					currentBrick.setCheckedBoolean(checked);
				}
				handleBrickEnabledState(currentBrick, !checked);
				adapter.notifyDataSetChanged();
				brickPosition++;
			}

			adapter.animateSelectedBricks();

			if (adapter.getOnBrickEditListener() != null) {
				adapter.getOnBrickEditListener().onBrickChecked();
			}
			adapter.notifyDataSetChanged();
			return true;
		} else if (brick instanceof NestingBrick) {
			int counter = 1;
			int from = 0;
			int to = 0;
			for (Brick currentBrick : ((NestingBrick) brick).getAllNestingBrickParts(false)) {
				if (currentBrick == null) {
					break;
				}
				if (checked) {
					adapter.addElementToCheckedBricks(currentBrick);
					adapter.addElementToAnimatedBricks(currentBrick);
				} else {
					adapter.removeElementFromCheckedBricks(currentBrick);
				}
				if (counter == 1) {
					from = brickList.indexOf(currentBrick);
					counter++;
				} else {
					to = brickList.indexOf(currentBrick);
				}
				currentBrick.getCheckBox().setChecked(checked);
			}
			if (from > to) {
				int temp = from;
				from = to;
				to = temp;
			}
			from++;
			while (from < to) {
				Brick currentBrick = brickList.get(from);
				if (checked) {
					adapter.addElementToCheckedBricks(currentBrick);
					adapter.addElementToAnimatedBricks(currentBrick);
				} else {
					adapter.removeElementFromCheckedBricks(currentBrick);
				}
				currentBrick.getCheckBox().setChecked(checked);
				handleBrickEnabledState(currentBrick, !checked);
				from++;
			}

			adapter.animateSelectedBricks();

			if (adapter.getOnBrickEditListener() != null) {
				adapter.getOnBrickEditListener().onBrickChecked();
			}
			adapter.notifyDataSetChanged();
			return true;
		}
		return false;
	}
}
