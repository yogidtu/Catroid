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
package org.catrobat.catroid.ui.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ListView;

import org.catrobat.catroid.ProjectManager;
import org.catrobat.catroid.R;
import org.catrobat.catroid.content.Script;
import org.catrobat.catroid.content.Sprite;
import org.catrobat.catroid.content.StartScript;
import org.catrobat.catroid.content.bricks.AllowedAfterDeadEndBrick;
import org.catrobat.catroid.content.bricks.Brick;
import org.catrobat.catroid.content.bricks.DeadEndBrick;
import org.catrobat.catroid.content.bricks.NestingBrick;
import org.catrobat.catroid.content.bricks.ScriptBrick;
import org.catrobat.catroid.ui.ViewSwitchLock;
import org.catrobat.catroid.ui.controller.ScriptController;
import org.catrobat.catroid.ui.dialogs.CustomAlertDialogBuilder;
import org.catrobat.catroid.ui.dragndrop.DragAndDropListView;
import org.catrobat.catroid.ui.dragndrop.DragAndDropListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.Lock;

public class ScriptAdapter extends BaseAdapter implements DragAndDropListener, OnClickListener,
		ScriptActivityAdapterInterface {

	private static final String TAG = ScriptAdapter.class.getSimpleName();
	private Context context;

	public Context getContext() {
		return context;
	}

	private Sprite sprite;
	private int dragTargetPosition;
	private Brick draggedBrick;
	private DragAndDropListView dragAndDropListView;
	private View insertionView;
	private boolean initInsertedBrick;
	private boolean addingNewBrick;
	private int positionOfInsertedBrick;
	private Script scriptToDelete;

	private boolean firstDrag;
	private int fromBeginDrag, toEndDrag;
	private boolean retryScriptDragging;
	private boolean showDetails = false;

	private List<Brick> brickList;

	private List<Brick> animatedBricks;

	private List<Brick> checkedBricks = new ArrayList<Brick>();

	private int selectMode;
	private OnBrickEditListener onBrickEditListener;

	private boolean actionMode = false;

	private Lock viewSwitchLock = new ViewSwitchLock();

	public int listItemCount = 0;

	private int clickItemPosition = 0;

	public ScriptAdapter(Context context, Sprite sprite, DragAndDropListView listView) {
		this.context = context;
		this.sprite = sprite;
		dragAndDropListView = listView;
		insertionView = View.inflate(context, R.layout.brick_insert, null);
		initInsertedBrick = false;
		addingNewBrick = false;
		firstDrag = true;
		retryScriptDragging = false;
		animatedBricks = new ArrayList<Brick>();
		this.selectMode = ListView.CHOICE_MODE_NONE;
		initBrickList();
	}

	public void initBrickList() {
		brickList = new ArrayList<Brick>();

		Sprite sprite = ProjectManager.getInstance().getCurrentSprite();

		int numberOfScripts = sprite.getNumberOfScripts();
		for (int scriptPosition = 0; scriptPosition < numberOfScripts; scriptPosition++) {
			Script script = sprite.getScript(scriptPosition);
			brickList.add(script.getScriptBrick());
			script.getScriptBrick().setBrickAdapter(this);
			for (Brick brick : script.getBrickList()) {
				brickList.add(brick);
				brick.setBrickAdapter(this);
			}
		}
	}

	public boolean isActionMode() {
		return actionMode;
	}

	public void setActionMode(boolean actionMode) {
		this.actionMode = actionMode;
	}

	public List<Brick> getBrickList() {
		return brickList;
	}

	public void setBrickList(List<Brick> brickList) {
		this.brickList = brickList;
	}

	@Override
	public void drag(int from, int to) {

		if (to < 0 || to >= brickList.size()) {
			to = brickList.size() - 1;
		}
		if (from < 0 || from >= brickList.size()) {
			from = brickList.size() - 1;
		}
		if (draggedBrick == null) {
			draggedBrick = (Brick) getItem(from);
			notifyDataSetChanged();
		}

		if (firstDrag) {
			fromBeginDrag = from;
			firstDrag = false;
		}

		if (draggedBrick instanceof NestingBrick) {
			NestingBrick nestingBrick = (NestingBrick) draggedBrick;
			if (nestingBrick.isInitialized()) {
				to = ScriptController.getInstance()
						.getDraggedNestingBricksToPosition(nestingBrick, from, to, brickList);
			}
		} else if (draggedBrick instanceof ScriptBrick) {
			int currentPosition = to;
			brickList.remove(draggedBrick);
			brickList.add(to, draggedBrick);
			to = ScriptController.getInstance().getNewPositionForScriptBrick(to, draggedBrick, brickList);
			dragTargetPosition = to;
			if (currentPosition != to) {
				retryScriptDragging = true;
			} else {
				retryScriptDragging = false;
			}
		}

		to = ScriptController.getInstance().getNewPositionIfEndingBrickIsThere(to, draggedBrick, brickList, this);

		if (!(draggedBrick instanceof ScriptBrick)) {
			if (to != 0) {
				dragTargetPosition = to;
			} else {
				dragTargetPosition = 1;
				to = 1;
			}
		}

		brickList.remove(draggedBrick);
		brickList.add(dragTargetPosition, draggedBrick);

		toEndDrag = to;

		animatedBricks.clear();

		notifyDataSetChanged();
	}

	@Override
	public void drop() {
		int to = toEndDrag;

		if (to < 0 || to >= brickList.size()) {
			to = brickList.size() - 1;
		}

		if (retryScriptDragging
				|| to != ScriptController.getInstance().getNewPositionForScriptBrick(to, draggedBrick, brickList)) {
			ScriptController.getInstance().scrollToPosition(dragTargetPosition, dragAndDropListView, brickList);
			draggedBrick = null;
			initInsertedBrick = true;
			positionOfInsertedBrick = dragTargetPosition;
			notifyDataSetChanged();

			retryScriptDragging = false;
			return;
		}
		int tempTo = ScriptController.getInstance().getNewPositionIfEndingBrickIsThere(to, draggedBrick, brickList,
				this);
		if (to != tempTo) {
			to = tempTo;
		}

		if (addingNewBrick) {
			if (draggedBrick instanceof ScriptBrick) {
				ScriptController.getInstance().addScriptToProject(to, (ScriptBrick) draggedBrick, brickList, sprite);
			} else {
				ScriptController.getInstance().addBrickToPosition(to, draggedBrick, brickList, sprite);
			}

			addingNewBrick = false;
		} else {
			ScriptController.getInstance().moveExistingProjectBrick(fromBeginDrag, toEndDrag, draggedBrick, brickList,
					sprite);
		}

		draggedBrick = null;
		firstDrag = true;

		initBrickList();
		notifyDataSetChanged();

		int scrollTo = to;
		if (scrollTo >= brickList.size() - 1) {
			scrollTo = getCount() - 1;
		}
		dragAndDropListView.smoothScrollToPosition(scrollTo);
	}

	public void addNewBrick(int position, Brick brickToBeAdded) {

		if (draggedBrick != null) {
			Log.w(TAG, "Want to add Brick while there is another one currently dragged.");
			return;
		}

		Sprite currentSprite = ProjectManager.getInstance().getCurrentSprite();
		int scriptCount = currentSprite.getNumberOfScripts();
		if (scriptCount == 0 && brickToBeAdded instanceof ScriptBrick) {
			currentSprite.addScript(((ScriptBrick) brickToBeAdded).initScript(currentSprite));
			initBrickList();
			notifyDataSetChanged();
			return;
		}

		if (position < 0) {
			position = 0;
		} else if (position > brickList.size()) {
			position = brickList.size();
		}

		if (brickToBeAdded instanceof ScriptBrick) {

			brickList.add(position, brickToBeAdded);
			position = ScriptController.getInstance().getNewPositionForScriptBrick(position, brickToBeAdded, brickList);
			brickList.remove(brickToBeAdded);
			brickList.add(position, brickToBeAdded);
			ScriptController.getInstance().scrollToPosition(position, dragAndDropListView, brickList);

		} else {

			position = ScriptController.getInstance().getNewPositionIfEndingBrickIsThere(position, brickToBeAdded,
					brickList, this);
			position = position <= 0 ? 1 : position;
			position = position > brickList.size() ? brickList.size() : position;
			brickList.add(position, brickToBeAdded);

		}

		initInsertedBrick = true;
		positionOfInsertedBrick = position;

		if (scriptCount == 0) {
			Script script = new StartScript(currentSprite);
			currentSprite.addScript(script);
			brickList.add(0, script.getScriptBrick());
			ProjectManager.getInstance().setCurrentScript(script);
			positionOfInsertedBrick = 1;
		}

		notifyDataSetChanged();

	}

	public void addNewMultipleBricks(int position, Brick brickToBeAdded) {

		if (draggedBrick != null) {
			Log.w(TAG, "Want to add Brick while there is another one currently dragged.");
			return;
		}

		Sprite currentSprite = ProjectManager.getInstance().getCurrentSprite();
		int scriptCount = currentSprite.getNumberOfScripts();
		if (scriptCount == 0 && brickToBeAdded instanceof ScriptBrick) {
			currentSprite.addScript(((ScriptBrick) brickToBeAdded).initScript(currentSprite));
			initBrickList();
			notifyDataSetChanged();
			return;
		}

		if (position < 0) {
			position = 0;
		} else if (position > brickList.size()) {
			position = brickList.size();
		}

		if (brickToBeAdded instanceof ScriptBrick) {

			brickList.add(position, brickToBeAdded);
			position = ScriptController.getInstance().getNewPositionForScriptBrick(position, brickToBeAdded, brickList);
			brickList.remove(brickToBeAdded);
			brickList.add(position, brickToBeAdded);
			ScriptController.getInstance().scrollToPosition(position, dragAndDropListView, brickList);

		} else {

			position = ScriptController.getInstance().getNewPositionIfEndingBrickIsThere(position, brickToBeAdded,
					brickList, this);
			position = position <= 0 ? 1 : position;
			position = position > brickList.size() ? brickList.size() : position;
			brickList.add(position, brickToBeAdded);

		}

		if (scriptCount == 0) {
			Script script = new StartScript(currentSprite);
			currentSprite.addScript(script);
			brickList.add(0, script.getScriptBrick());
			positionOfInsertedBrick = 1;
		}

		notifyDataSetChanged();

		ProjectManager.getInstance().saveProject();

	}

	@Override
	public void remove(int iWillBeIgnored) {
		// list will not be changed until user action ACTION_UP - therefore take the value from the begin
		removeFromBrickListAndProject(fromBeginDrag, false);
	}

	public void removeFromBrickListAndProject(int index, boolean removeScript) {
		if (addingNewBrick) {
			brickList.remove(draggedBrick);
		} else {
			int temp[] = ScriptController.getInstance().getScriptAndBrickIndexFromProject(index, brickList, sprite);
			Script script = ProjectManager.getInstance().getCurrentSprite().getScript(temp[0]);
			if (script != null) {

				Brick brick = script.getBrick(temp[1]);
				if (brick instanceof NestingBrick) {
					for (Brick tempBrick : ((NestingBrick) brick).getAllNestingBrickParts(true)) {
						script.removeBrick(tempBrick);
					}
				} else {
					script.removeBrick(brick);
				}
				if (removeScript) {
					brickList.remove(script);
				}
			}
		}

		firstDrag = true;
		draggedBrick = null;
		addingNewBrick = false;

		initBrickList();
		notifyDataSetChanged();
	}

	public void removeDraggedBrick() {
		if (!addingNewBrick) {
			draggedBrick = null;
			firstDrag = true;
			notifyDataSetChanged();
			return;
		}

		brickList.remove(draggedBrick);

		firstDrag = true;
		draggedBrick = null;
		addingNewBrick = false;

		initBrickList();
		notifyDataSetChanged();
	}

	public OnLongClickListener getOnLongClickListener() {
		return dragAndDropListView;
	}

	@Override
	public int getCount() {
		return brickList.size();
	}

	@Override
	public Object getItem(int element) {
		if (element < 0 || element >= brickList.size()) {
			return null;
		}
		return brickList.get(element);
	}

	@Override
	public long getItemId(int index) {
		return index;
	}

	//TODO: move to ScriptFragment?
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (draggedBrick != null && dragTargetPosition == position) {
			return insertionView;
		}
		listItemCount = position + 1;

		Object item = getItem(position);

		if (item instanceof ScriptBrick && (!initInsertedBrick || position != positionOfInsertedBrick)) {
			View scriptBrickView = ((Brick) item).getView(context, position, this);
			if (draggedBrick == null) {
				scriptBrickView.setOnClickListener(this);
			}
			return scriptBrickView;
		}

		View currentBrickView;
		// dirty HACK
		// without the footer, position can be 0, and list.get(-1) caused an Indexoutofboundsexception
		// no clean solution was found
		if (position == 0) {
			if (item instanceof AllowedAfterDeadEndBrick && brickList.get(position) instanceof DeadEndBrick) {
				currentBrickView = ((AllowedAfterDeadEndBrick) item).getNoPuzzleView(context, position, this);
			} else {
				currentBrickView = ((Brick) item).getView(context, position, this);
			}
		} else {
			if (item instanceof AllowedAfterDeadEndBrick && brickList.get(position - 1) instanceof DeadEndBrick) {
				currentBrickView = ((AllowedAfterDeadEndBrick) item).getNoPuzzleView(context, position, this);
			} else {
				currentBrickView = ((Brick) item).getView(context, position, this);
			}
		}

		// this one is working but causes null pointer exceptions on movement and control bricks?!
		//		currentBrickView.setOnLongClickListener(longClickListener);

		// Hack!!!
		// if wrapper isn't used the longClick event won't be triggered
		ViewGroup wrapper = (ViewGroup) View.inflate(context, R.layout.brick_wrapper, null);
		if (currentBrickView.getParent() != null) {
			((ViewGroup) currentBrickView.getParent()).removeView(currentBrickView);
		}

		wrapper.addView(currentBrickView);
		if (draggedBrick == null) {
			if ((selectMode == ListView.CHOICE_MODE_NONE)) {
				wrapper.setOnClickListener(this);
				if (!(item instanceof DeadEndBrick)) {
					wrapper.setOnLongClickListener(dragAndDropListView);
				}
			}
		}

		if (position == positionOfInsertedBrick && initInsertedBrick && (selectMode == ListView.CHOICE_MODE_NONE)) {
			initInsertedBrick = false;
			addingNewBrick = true;
			dragAndDropListView.setInsertedBrick(position);

			dragAndDropListView.setDraggingNewBrick();
			dragAndDropListView.onLongClick(currentBrickView);

			return insertionView;
		}

		if (animatedBricks.contains(brickList.get(position))) {
			Animation animation = AnimationUtils.loadAnimation(context, R.anim.blink);
			wrapper.startAnimation(animation);
			animatedBricks.remove(brickList.get(position));
		}
		return wrapper;
	}

	public void updateProjectBrickList() {
		initBrickList();
		notifyDataSetChanged();
	}

	@Override
	public void setTouchedScript(int index) {
		if (index >= 0 && index < brickList.size() && brickList.get(index) instanceof ScriptBrick
				&& draggedBrick == null) {
			int scriptIndex = ScriptController.getInstance().getScriptIndexFromProject(index, sprite);
			ProjectManager.getInstance().setCurrentScript(sprite.getScript(scriptIndex));
		}
	}

	@Override
	public void onClick(final View view) {
		if (!viewSwitchLock.tryLock()) {
			return;
		}

		animatedBricks.clear();
		ScriptController.getInstance().onClick(view, brickList, context, sprite, dragAndDropListView, selectMode);
		notifyDataSetChanged();
	}

	//TODO: move to ScriptFragment!!
	public void showConfirmDeleteScriptDialog(int itemPosition) {
		this.clickItemPosition = itemPosition;
		int titleId;

		if (getItem(clickItemPosition) instanceof ScriptBrick) {
			titleId = R.string.dialog_confirm_delete_script_title;
		} else {
			titleId = R.string.dialog_confirm_delete_brick_title;
		}

		AlertDialog.Builder builder = new CustomAlertDialogBuilder(context);
		builder.setTitle(titleId);
		builder.setMessage(R.string.dialog_confirm_delete_brick_message);
		builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int id) {
				if (getItem(clickItemPosition) instanceof ScriptBrick) {
					scriptToDelete = ((ScriptBrick) getItem(clickItemPosition)).initScript(ProjectManager.getInstance()
							.getCurrentSprite());
					handleScriptDelete(sprite, scriptToDelete);
					scriptToDelete = null;
				} else {
					removeFromBrickListAndProject(clickItemPosition, false);
				}
			}
		});
		builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int id) {
				dialog.cancel();
			}
		});
		builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
			@Override
			public void onCancel(DialogInterface dialog) {
				scriptToDelete = null;
			}
		});

		AlertDialog alertDialog = builder.create();
		alertDialog.show();
	}

	@Override
	public void setShowDetails(boolean showDetails) {
		this.showDetails = showDetails;
	}

	@Override
	public boolean getShowDetails() {
		return showDetails;
	}

	@Override
	public void setSelectMode(int mode) {
		selectMode = mode;
	}

	@Override
	public int getSelectMode() {
		return selectMode;
	}

	@Override
	public int getAmountOfCheckedItems() {
		return getCheckedBricks().size();
	}

	@Override
	public Set<Integer> getCheckedItems() {
		return null;
	}

	@Override
	public void clearCheckedItems() {
		checkedBricks.clear();
		setCheckboxVisibility(View.GONE);
		uncheckAllItems();
		ScriptController.getInstance().enableAllBricks(brickList);
		notifyDataSetChanged();
	}

	private void uncheckAllItems() {
		for (Brick brick : brickList) {
			CheckBox checkbox = brick.getCheckBox();
			if (checkbox != null) {
				checkbox.setChecked(false);
			}
		}
	}

	public void checkAllItems() {
		for (Brick brick : brickList) {
			if (brick instanceof ScriptBrick) {
				if (brick.getCheckBox() != null) {
					brick.getCheckBox().setChecked(true);
					brick.setCheckedBoolean(true);
				}
				ScriptController.getInstance().smartBrickSelection(brick, true, this, brickList);
			}
		}
	}

	public void setCheckboxVisibility(int visibility) {
		for (Brick brick : brickList) {
			brick.setCheckboxVisibility(visibility);
		}
	}

	public interface OnBrickEditListener {

		public void onBrickEdit(View view);

		public void onBrickChecked();
	}

	public void setOnBrickEditListener(OnBrickEditListener listener) {
		onBrickEditListener = listener;
	}

	public void addElementToCheckedBricks(Brick brick) {
		if (!(checkedBricks.contains(brick))) {
			checkedBricks.add(brick);
		}
	}

	public void removeElementFromCheckedBricks(Brick brick) {
		checkedBricks.remove(brick);
	}

	public void addElementToAnimatedBricks(Brick brick) {
		animatedBricks.add(brick);
	}

	public void handleScriptDelete(Sprite spriteToEdit, Script scriptToDelete) {
		spriteToEdit.removeScript(scriptToDelete);
		if (spriteToEdit.getNumberOfScripts() == 0) {
			ProjectManager.getInstance().setCurrentScript(null);
			updateProjectBrickList();
		} else {
			int lastScriptIndex = spriteToEdit.getNumberOfScripts() - 1;
			Script lastScript = spriteToEdit.getScript(lastScriptIndex);
			ProjectManager.getInstance().setCurrentScript(lastScript);
			updateProjectBrickList();
		}
	}

	public List<Brick> getCheckedBricks() {
		return checkedBricks;
	}

	public List<Brick> getCheckedBricksFromScriptBrick(ScriptBrick brick) {
		int brickPosition = checkedBricks.indexOf(brick);
		if (brickPosition >= 0) {
			List<Brick> checkedBricksInScript = new ArrayList<Brick>();
			while ((brickPosition < brickList.size()) && !(brickList.get(brickPosition) instanceof ScriptBrick)) {
				checkedBricksInScript.add(brickList.get(brickPosition));
				brickPosition++;
			}
			return checkedBricksInScript;
		}
		return null;
	}

	public List<Brick> getReversedCheckedBrickList() {
		List<Brick> reverseCheckedList = new ArrayList<Brick>();
		for (int counter = checkedBricks.size() - 1; counter >= 0; counter--) {
			reverseCheckedList.add(checkedBricks.get(counter));
		}
		return reverseCheckedList;
	}

	public void animateSelectedBricks() {
		if (!animatedBricks.isEmpty()) {

			for (final Brick animationBrick : animatedBricks) {
				Animation animation = AnimationUtils.loadAnimation(context, R.anim.blink);

				animation.setAnimationListener(new AnimationListener() {

					@Override
					public void onAnimationStart(Animation animation) {
						animationBrick.setAnimationState(true);
					}

					@Override
					public void onAnimationRepeat(Animation animation) {

					}

					@Override
					public void onAnimationEnd(Animation animation) {
						animationBrick.setAnimationState(false);
					}
				});
				int position = animatedBricks.indexOf(animationBrick);
				animationBrick.setAnimationState(true);
				View view = animationBrick.getView(context, position, this);

				if (view.hasWindowFocus()) {
					view.startAnimation(animation);
				}
			}

		}
		animatedBricks.clear();
	}

	public OnBrickEditListener getOnBrickEditListener() {
		return onBrickEditListener;
	}

	public List<Brick> getAnimatedBricks() {
		return animatedBricks;
	}

	public void handleCheck(Brick brick, boolean isChecked) {
		ScriptController.getInstance().handleCheck(brick, isChecked, selectMode, this, brickList);
	}
}
