package org.catrobat.catroid.ui;

import org.catrobat.catroid.R;
import org.catrobat.catroid.common.ScreenValues;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.text.format.Time;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

/**
 * Author: Romain Guy, Forest Johnson
 * <p/>
 * Using example: <?xml version="4.0" encoding="utf-8"?> <com.example.android.layout.FlowLayout
 * xmlns:f="http://schemas.android.com/apk/res/org.apmem.android"
 * xmlns:android="http://schemas.android.com/apk/res/android" f:horizontalSpacing="6dip" f:verticalSpacing="12dip"
 * android:layout_width="wrap_content" android:layout_height="wrap_content" android:paddingLeft="6dip"
 * android:paddingTop="6dip" android:paddingRight="12dip"> <Button android:layout_width="wrap_content"
 * android:layout_height="wrap_content" f:layout_horizontalSpacing="32dip" f:layout_breakLine="true"
 * android:text="Cancel" />
 * <p/>
 * </com.example.android.layout.FlowLayout>
 */
public class DragNDropBrickLayout extends BrickLayout {

	private boolean dragging;

	private int lastInsertableSpaceIndex;
	private boolean justStartedDragging;
	private int draggedItemIndex;
	private int dragPointOffsetX;
	private int dragPointOffsetY;

	private int viewToWindowSpaceX;
	private int viewToWindowSpaceY;

	private long dragBeganMillis;
	private long dragEndMillis;

	private WeirdFloatingWindowData dragView;
	private WeirdFloatingWindowData dragCursor1;
	private WeirdFloatingWindowData dragCursor2;

	public DragAndDropBrickLayoutListener parent;

	public DragNDropBrickLayout(Context context) {
		super(context);
	}

	public DragNDropBrickLayout(Context context, AttributeSet attributeSet) {
		super(context, attributeSet);
	}

	public DragNDropBrickLayout(Context context, AttributeSet attributeSet, int defStyle) {
		super(context, attributeSet, defStyle);
	}

	public void setListener(DragAndDropBrickLayoutListener parent) {
		this.parent = parent;
	}

	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		final int action = ev.getAction();
		final int x = (int) ev.getX();
		final int y = (int) ev.getY();
		viewToWindowSpaceX = (int) ev.getRawX() - x;
		viewToWindowSpaceY = (int) ev.getRawY() - y;

		switch (action) {
			case MotionEvent.ACTION_DOWN:
				int itemPosition = click(x, y);
				if (itemPosition != -1) {
					beginDrag(x, y, itemPosition);
				}
				break;
			case MotionEvent.ACTION_MOVE:
				if (dragging) {
					drag(x, y);
				}
				break;
			case MotionEvent.ACTION_CANCEL:
			case MotionEvent.ACTION_UP:
			default:
				if (dragging) {
					drop(x, y);
				}
				break;
		}
		return true;
	}

	private int click(int x, int y) {
		int itemPosition = 0;

		for (BrickLayout.LineData line : lines) {
			for (BrickLayout.ElementData e : line.elements) {
				if (x > e.posX && y > e.posY && x < e.posX + e.width && y < e.posY + e.height) {
					dragPointOffsetX = (e.posX - x);
					dragPointOffsetY = (e.posY - y);
					return itemPosition;
				}
				itemPosition++;
			}
		}
		return -1;
	}

	private void beginDrag(int x, int y, int itemIndex) {
		dragBeganMillis = getMillisNow();

		// frequent dragdrops can cause a null reference when the event for the new drag happens before the drop finishes.
		if (dragging || dragBeganMillis - dragEndMillis < 200) {
			return;
		}

		justStartedDragging = true;
		draggedItemIndex = itemIndex;

		stopDrag();

		View item = getChildAt(itemIndex);
		if (item == null) {
			return;
		}
		item.setDrawingCacheEnabled(true);
		item.setVisibility(View.INVISIBLE);

		// Create a copy of the drawing cache so that it does not get recycled
		// by the framework when the list tries to clean up memory
		Bitmap bitmap = Bitmap.createBitmap(item.getDrawingCache());

		dragView = makeWeirdFloatingWindow(bitmap, item.getWidth(), item.getHeight());

		dragCursor1 = makeWeirdFloatingWindow(View.inflate(getContext(), R.layout.brick_user_data_insert, null));
		dragCursor2 = makeWeirdFloatingWindow(View.inflate(getContext(), R.layout.brick_user_data_insert, null));

		dragging = true;

		drag(x, y);
	}

	// move the drag view
	private void drag(int x, int y) {
		int centerOfDraggedElementX = x + dragPointOffsetX;
		int centerOfDraggedElementY = y + dragPointOffsetY;

		positionWierdFloatingWindow(dragView, centerOfDraggedElementX, centerOfDraggedElementY);

		int insertableSpaceIndex = findClosestInsertableSpace(centerOfDraggedElementX, centerOfDraggedElementY);

		if (justStartedDragging || lastInsertableSpaceIndex != insertableSpaceIndex) {

			repositionCursors(insertableSpaceIndex);

			lastInsertableSpaceIndex = insertableSpaceIndex;
			justStartedDragging = false;
		}
	}

	private void drop(int x, int y) {
		dragEndMillis = getMillisNow();

		long difference = dragEndMillis - dragBeganMillis;
		if (difference < 400 && draggedItemIndex == lastInsertableSpaceIndex) {
			parent.click(draggedItemIndex);
		} else {
			parent.reorder(draggedItemIndex, lastInsertableSpaceIndex);
		}

		stopDrag();
	}

	private void stopDrag() {
		removeWeirdFloatingWindow(dragView);
		removeWeirdFloatingWindow(dragCursor1);
		removeWeirdFloatingWindow(dragCursor2);

		dragView = null;
		dragCursor1 = null;
		dragCursor2 = null;

		View item = getChildAt(draggedItemIndex);
		if (item == null) {
			return;
		}
		item.setVisibility(VISIBLE);

		dragging = false;
	}

	private int countElements() {
		int previousElementIndex = 0;
		for (BrickLayout.LineData line : lines) {
			previousElementIndex += line.elements.size();
		}
		return previousElementIndex;
	}

	/**
	 * 
	 * Finds the space closest to x,y where an element can be inserted
	 * 
	 * @returns index of the element before the space or -1 for the beginning of the array
	 */
	private int findClosestInsertableSpace(int x, int y) {
		int previousElementIndex = -1;
		int closestPreviousElementIndex = -1;
		float closestDistance = 99999999;

		for (BrickLayout.LineData line : lines) {
			for (BrickLayout.ElementData e : line.elements) {

				if (e.view.getVisibility() == GONE) {
					previousElementIndex++;
					continue;
				}

				float edgeX = e.posX - (e.width * 0.5f);
				float edgeY = e.posY;
				float dx = edgeX - x;
				float dy = edgeY - y;
				float d = dx * dx + dy * dy;

				if (d < closestDistance) {
					closestDistance = d;
					closestPreviousElementIndex = previousElementIndex;
				}
				previousElementIndex++;

				edgeX = e.posX + (e.width * 0.5f);
				dx = edgeX - x;
				d = dx * dx + dy * dy;

				if (d < closestDistance) {
					closestDistance = d;
					closestPreviousElementIndex = previousElementIndex;
				}
			}
		}
		return closestPreviousElementIndex;
	}

	private long getMillisNow() {
		Time time = new Time();
		time.setToNow();
		return time.toMillis(true);
	}

	private void repositionCursors(int insertableSpaceIndex) {
		if (dragCursor1 != null && dragCursor1.view != null && insertableSpaceIndex >= 0) {
			BrickLayout.ElementData previousElement = getElement(insertableSpaceIndex);

			int rightEdgeOfPreviousElementX = previousElement.posX + previousElement.width;
			int rightEdgeOfPreviousElementY = previousElement.posY + (int) (previousElement.height * 0.5f);

			positionWierdFloatingWindow(dragCursor1, rightEdgeOfPreviousElementX, rightEdgeOfPreviousElementY);
			dragCursor1.view.setVisibility(VISIBLE);
		} else {
			dragCursor1.view.setVisibility(GONE);
		}

		if (dragCursor2 != null && dragCursor2.view != null && insertableSpaceIndex < countElements() - 1) {
			BrickLayout.ElementData nextElement = getElement(insertableSpaceIndex + 1);

			int leftEdgeOfNextElementX = nextElement.posX;
			int leftEdgeOfNextElementY = nextElement.posY + (int) (nextElement.height * 0.5f);

			positionWierdFloatingWindow(dragCursor2, leftEdgeOfNextElementX, leftEdgeOfNextElementY);
			dragCursor2.view.setVisibility(VISIBLE);
		} else {
			dragCursor2.view.setVisibility(GONE);
		}
	}

	private BrickLayout.ElementData getElement(int i) {
		int index = 0;

		for (BrickLayout.LineData line : lines) {
			for (BrickLayout.ElementData e : line.elements) {
				if (index == i) {
					return e;
				}
				index++;
			}
		}
		return null;
	}

	private WeirdFloatingWindowData makeWeirdFloatingWindow(Bitmap bitmap, int width, int height) {
		Context context = getContext();
		ImageView v = new ImageView(context);
		v.setImageBitmap(bitmap);

		WindowManager mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		mWindowManager.addView(v, getFloatingWindowParams());

		return new WeirdFloatingWindowData(v, width, height);
	}

	private WeirdFloatingWindowData makeWeirdFloatingWindow(View view) {
		Context context = getContext();
		WindowManager mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		mWindowManager.addView(view, getFloatingWindowParams());

		return new WeirdFloatingWindowData(view, view.getWidth(), view.getHeight());
	}

	private void positionWierdFloatingWindow(WeirdFloatingWindowData window, int x, int y) {
		if (window != null && window.view != null) {
			WindowManager.LayoutParams layoutParams = (WindowManager.LayoutParams) window.view.getLayoutParams();
			int uncenteringX = (int) (ScreenValues.SCREEN_WIDTH * -0.5f) + (int) (window.width * 0.5f);
			int uncenteringY = (int) (ScreenValues.SCREEN_HEIGHT * -0.5f) + (int) (window.height * 0.5f);
			layoutParams.x = x + viewToWindowSpaceX + uncenteringX;
			layoutParams.y = y + viewToWindowSpaceY + uncenteringY;

			WindowManager mWindowManager = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
			mWindowManager.updateViewLayout(window.view, layoutParams);

		}

	}

	private void removeWeirdFloatingWindow(WeirdFloatingWindowData window) {
		if (window != null && window.view != null) {
			window.view.setVisibility(GONE);
			WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
			wm.removeView(window.view);
		}
	}

	private WindowManager.LayoutParams getFloatingWindowParams() {
		WindowManager.LayoutParams windowParams = new WindowManager.LayoutParams();
		windowParams.gravity = Gravity.CENTER;
		windowParams.x = 0;
		windowParams.y = 0;

		windowParams.height = LayoutParams.WRAP_CONTENT;
		windowParams.width = LayoutParams.WRAP_CONTENT;
		windowParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
				| WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
				| WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS;
		windowParams.format = PixelFormat.TRANSLUCENT;
		windowParams.windowAnimations = 0;
		return windowParams;
	}

	private class WeirdFloatingWindowData {
		public View view;
		public int width;
		public int height;

		public WeirdFloatingWindowData(View view, int width, int height) {
			this.view = view;
			this.width = width;
			this.height = height;
		}
	}

}
