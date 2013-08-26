package org.catrobat.catroid.ui;

import java.util.LinkedList;

import org.catrobat.catroid.R;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

/**
 * Author: Romain Guy
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
public class BrickLayout extends ViewGroup {
	public static final int HORIZONTAL = 0;
	public static final int VERTICAL = 1;

	private int customPadding = 0;
	private int horizontalSpacing = 0;
	private int verticalSpacing = 0;
	private int orientation = 0;
	protected boolean debugDraw = false;

	protected LinkedList<LineData> lines;

	public BrickLayout(Context context) {
		super(context);

		this.readStyleParameters(context, null);
	}

	public BrickLayout(Context context, AttributeSet attributeSet) {
		super(context, attributeSet);

		this.readStyleParameters(context, attributeSet);
	}

	public BrickLayout(Context context, AttributeSet attributeSet, int defStyle) {
		super(context, attributeSet, defStyle);

		this.readStyleParameters(context, attributeSet);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int sizeWidth = MeasureSpec.getSize(widthMeasureSpec) - this.getPaddingRight() - this.getPaddingLeft();
		int sizeHeight = MeasureSpec.getSize(heightMeasureSpec) - this.getPaddingTop() - this.getPaddingBottom();

		int modeWidth = MeasureSpec.getMode(widthMeasureSpec);
		int modeHeight = MeasureSpec.getMode(heightMeasureSpec);

		int lineThicknessWithorizontalSpacing = 0;
		int lineThickness = 0;
		int lineLengthWithorizontalSpacing = 0;
		int lineLength;

		int prevLinePosition = 0;

		int controlMaxLength = 0;
		int controlMaxThickness = 0;

		lines = new LinkedList<LineData>();
		LineData currentLine = newLine(lines);

		final int count = getChildCount();
		for (int i = 0; i < count; i++) {
			final View child = getChildAt(i);
			if (child.getVisibility() == GONE) {
				continue;
			}

			child.measure(MeasureSpec.makeMeasureSpec(sizeWidth, modeWidth == MeasureSpec.EXACTLY ? MeasureSpec.AT_MOST
					: modeWidth), MeasureSpec.makeMeasureSpec(sizeHeight,
					modeHeight == MeasureSpec.EXACTLY ? MeasureSpec.AT_MOST : modeHeight));

			LayoutParams layoutParams = (LayoutParams) child.getLayoutParams();

			int horizontalSpacing = this.getHorizontalSpacing(layoutParams);
			int verticalSpacing = this.getVerticalSpacing(layoutParams);

			int childWidth = child.getMeasuredWidth();
			int childHeight = child.getMeasuredHeight();

			boolean updateSmallestHeight = currentLine.minHeight == 0 || currentLine.minHeight > childHeight;
			currentLine.minHeight = (updateSmallestHeight ? childHeight : currentLine.minHeight);

			lineLength = lineLengthWithorizontalSpacing + childWidth;
			lineLengthWithorizontalSpacing = lineLength + horizontalSpacing;

			boolean newLine = layoutParams.newLine || (modeWidth != MeasureSpec.UNSPECIFIED && lineLength > sizeWidth);
			if (newLine) {
				prevLinePosition = prevLinePosition + lineThicknessWithorizontalSpacing;

				currentLine = newLine(lines);

				lineThickness = childHeight;
				lineLength = childWidth;
				lineThicknessWithorizontalSpacing = childHeight + verticalSpacing;
				lineLengthWithorizontalSpacing = lineLength + horizontalSpacing;
			}

			lineThicknessWithorizontalSpacing = Math.max(lineThicknessWithorizontalSpacing, childHeight
					+ verticalSpacing);
			lineThickness = Math.max(lineThickness, childHeight);

			currentLine.height = lineThickness;

			int posX = getPaddingLeft() + lineLength - childWidth;
			int posY = getPaddingTop() + prevLinePosition;

			ElementData ed = new ElementData(child, posX, posY, childWidth, childHeight);
			currentLine.elements.add(ed);

			controlMaxLength = Math.max(controlMaxLength, lineLength);
			controlMaxThickness = prevLinePosition + lineThickness;
		}

		int x = controlMaxLength;
		int y = controlMaxThickness;

		y += customPadding * 2;

		y = Math.max(y, getSuggestedMinimumHeight());

		int yAdjust = Math.round((y - controlMaxThickness) * 0.5f);

		if (y > getSuggestedMinimumHeight()) {
			yAdjust += Math.round(lines.get(0).minHeight * -0.15f);
		}

		for (LineData lineData : lines) {
			for (ElementData elementData : lineData.elements) {
				int yAdjust2 = 0;
				if (elementData.height < lineData.height) {
					yAdjust2 = Math.round((lineData.height - elementData.height) * 0.5f);
				}

				elementData.posY += yAdjust + yAdjust2;
				LayoutParams layoutParams = (LayoutParams) elementData.view.getLayoutParams();
				layoutParams.setPosition(elementData.posX, elementData.posY);
			}
		}

		this.setMeasuredDimension(resolveSize(x, widthMeasureSpec), resolveSize(y, heightMeasureSpec));
	}

	public void myForceLayout() {
		LinkedList<View> children = new LinkedList<View>();
		for (int i = 0; i < getChildCount(); i++) {
			children.add(getChildAt(i));
		}

		removeAllViews();

		for (View child : children) {
			this.addView(child);
		}
	}

	private LineData newLine(LinkedList<LineData> lines) {
		LineData toAdd = new LineData();
		lines.add(toAdd);
		return toAdd;
	}

	private int getVerticalSpacing(LayoutParams layoutParams) {
		int verticalSpacing;
		if (layoutParams.verticalSpacingSpecified()) {
			verticalSpacing = layoutParams.verticalSpacing;
		} else {
			verticalSpacing = this.verticalSpacing;
		}
		return verticalSpacing;
	}

	private int getHorizontalSpacing(LayoutParams layoutParams) {
		int horizontalSpacing;
		if (layoutParams.horizontalSpacingSpecified()) {
			horizontalSpacing = layoutParams.horizontalSpacing;
		} else {
			horizontalSpacing = this.horizontalSpacing;
		}
		return horizontalSpacing;
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		final int count = getChildCount();
		for (int i = 0; i < count; i++) {
			View child = getChildAt(i);
			LayoutParams layoutParams = (LayoutParams) child.getLayoutParams();
			child.layout(layoutParams.x, layoutParams.y, layoutParams.x + child.getMeasuredWidth(), layoutParams.y
					+ child.getMeasuredHeight());
		}
	}

	@Override
	protected boolean drawChild(Canvas canvas, View child, long drawingTime) {
		boolean more = super.drawChild(canvas, child, drawingTime);
		this.drawDebugInfo(canvas, child);
		return more;
	}

	@Override
	protected boolean checkLayoutParams(ViewGroup.LayoutParams layoutParams) {
		return layoutParams instanceof LayoutParams;
	}

	@Override
	protected LayoutParams generateDefaultLayoutParams() {
		return new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
	}

	@Override
	public LayoutParams generateLayoutParams(AttributeSet attributeSet) {
		return new LayoutParams(getContext(), attributeSet);
	}

	@Override
	protected LayoutParams generateLayoutParams(ViewGroup.LayoutParams p) {
		return new LayoutParams(p);
	}

	private void readStyleParameters(Context context, AttributeSet attributeSet) {
		TypedArray styledAttributes = context.obtainStyledAttributes(attributeSet, R.styleable.BrickLayout);
		try {
			customPadding = styledAttributes.getDimensionPixelSize(R.styleable.BrickLayout_customPadding, 0);
			horizontalSpacing = styledAttributes.getDimensionPixelSize(R.styleable.BrickLayout_horizontalSpacing, 0);
			verticalSpacing = styledAttributes.getDimensionPixelSize(R.styleable.BrickLayout_verticalSpacing, 0);
			orientation = styledAttributes.getInteger(R.styleable.BrickLayout_orientation, HORIZONTAL);
			debugDraw = styledAttributes.getBoolean(R.styleable.BrickLayout_debugDraw, false);
		} finally {
			styledAttributes.recycle();
		}
	}

	public void drawDebugInfo(Canvas canvas, View child) {
		if (!debugDraw) {
			return;
		}

		Paint childPaint = this.createPaint(0xffffff00);
		Paint layoutPaint = this.createPaint(0xff00ff00);
		Paint newLinePaint = this.createPaint(0xffff0000);

		LayoutParams layoutParams = (LayoutParams) child.getLayoutParams();

		if (layoutParams.horizontalSpacing > 0) {
			float x = child.getRight();
			float y = child.getTop() + child.getHeight() / 2.0f;
			canvas.drawLine(x, y, x + layoutParams.horizontalSpacing, y, childPaint);
			canvas.drawLine(x + layoutParams.horizontalSpacing - 4.0f, y - 4.0f, x + layoutParams.horizontalSpacing, y,
					childPaint);
			canvas.drawLine(x + layoutParams.horizontalSpacing - 4.0f, y + 4.0f, x + layoutParams.horizontalSpacing, y,
					childPaint);
		} else if (this.horizontalSpacing > 0) {
			float x = child.getRight();
			float y = child.getTop() + child.getHeight() / 2.0f;
			canvas.drawLine(x, y, x + this.horizontalSpacing, y, layoutPaint);
			canvas.drawLine(x + this.horizontalSpacing - 4.0f, y - 4.0f, x + this.horizontalSpacing, y, layoutPaint);
			canvas.drawLine(x + this.horizontalSpacing - 4.0f, y + 4.0f, x + this.horizontalSpacing, y, layoutPaint);
		}

		if (layoutParams.verticalSpacing > 0) {
			float x = child.getLeft() + child.getWidth() / 2.0f;
			float y = child.getBottom();
			canvas.drawLine(x, y, x, y + layoutParams.verticalSpacing, childPaint);
			canvas.drawLine(x - 4.0f, y + layoutParams.verticalSpacing - 4.0f, x, y + layoutParams.verticalSpacing,
					childPaint);
			canvas.drawLine(x + 4.0f, y + layoutParams.verticalSpacing - 4.0f, x, y + layoutParams.verticalSpacing,
					childPaint);
		} else if (this.verticalSpacing > 0) {
			float x = child.getLeft() + child.getWidth() / 2.0f;
			float y = child.getBottom();
			canvas.drawLine(x, y, x, y + this.verticalSpacing, layoutPaint);
			canvas.drawLine(x - 4.0f, y + this.verticalSpacing - 4.0f, x, y + this.verticalSpacing, layoutPaint);
			canvas.drawLine(x + 4.0f, y + this.verticalSpacing - 4.0f, x, y + this.verticalSpacing, layoutPaint);
		}

		if (layoutParams.newLine) {
			if (orientation == HORIZONTAL) {
				float x = child.getLeft();
				float y = child.getTop() + child.getHeight() / 2.0f;
				canvas.drawLine(x, y - 6.0f, x, y + 6.0f, newLinePaint);
			} else {
				float x = child.getLeft() + child.getWidth() / 2.0f;
				float y = child.getTop();
				canvas.drawLine(x - 6.0f, y, x + 6.0f, y, newLinePaint);
			}
		}
	}

	protected Paint createPaint(int color) {
		Paint paint = new Paint();
		paint.setAntiAlias(true);
		paint.setColor(color);
		paint.setStrokeWidth(2.0f);
		return paint;
	}

	protected class LineData {
		public int minHeight;
		public int height;
		public LinkedList<ElementData> elements;

		public LineData() {
			elements = new LinkedList<ElementData>();
		}
	}

	protected class ElementData {
		public int posX;
		public int posY;
		public int height;
		public int width;
		public View view;

		public ElementData(View view, int posX, int posY, int childWidth, int childHeight) {
			this.posX = posX;
			this.posY = posY;
			this.height = childHeight;
			this.width = childWidth;
			this.view = view;
		}
	}

	public static class LayoutParams extends ViewGroup.LayoutParams {
		private static int NO_SPACING = -1;

		private int x;
		private int y;
		private int horizontalSpacing = NO_SPACING;
		private int verticalSpacing = NO_SPACING;
		private boolean newLine = false;

		public LayoutParams(Context context, AttributeSet attributeSet) {
			super(context, attributeSet);
			this.readStyleParameters(context, attributeSet);
		}

		public LayoutParams(int width, int height) {
			super(width, height);
		}

		public LayoutParams(ViewGroup.LayoutParams layoutParams) {
			super(layoutParams);
		}

		public boolean horizontalSpacingSpecified() {
			return horizontalSpacing != NO_SPACING;
		}

		public boolean verticalSpacingSpecified() {
			return verticalSpacing != NO_SPACING;
		}

		public void setPosition(int x, int y) {
			this.x = x;
			this.y = y;
		}

		private void readStyleParameters(Context context, AttributeSet attributeSet) {
			TypedArray styledAttributes = context.obtainStyledAttributes(attributeSet,
					R.styleable.FlowLayout_LayoutParams);
			try {
				horizontalSpacing = styledAttributes.getDimensionPixelSize(
						R.styleable.FlowLayout_LayoutParams_layout_horizontalSpacing, NO_SPACING);
				verticalSpacing = styledAttributes.getDimensionPixelSize(
						R.styleable.FlowLayout_LayoutParams_layout_verticalSpacing, NO_SPACING);
				newLine = styledAttributes.getBoolean(R.styleable.FlowLayout_LayoutParams_layout_newLine, false);
			} finally {
				styledAttributes.recycle();
			}
		}
	}
}
