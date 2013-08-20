package org.catrobat.catroid.ui;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Spinner;
import android.widget.TextView;

import org.catrobat.catroid.R;

import java.util.LinkedList;

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
	private static final int MIN_TEXT_FIELD_WIDTH_DP = 100;
	private static final int MAX_TEXT_FIELD_WIDTH_DP = 350;
	private static final int LINES_TO_ALLOCATE = 10;
	private static final int ELEMENTS_TO_ALLOCATE = 10;

	private int customPadding = 0;
	private int horizontalSpacing = 0;
	private int verticalSpacing = 0;
	private int orientation = 0;
	protected boolean debugDraw = false;

	protected LinkedList<LineData> lines;

	public BrickLayout(Context context) {
		super(context);
		allocateLineData();
		this.readStyleParameters(context, null);
	}

	public BrickLayout(Context context, AttributeSet attributeSet) {
		super(context, attributeSet);
		allocateLineData();
		this.readStyleParameters(context, attributeSet);
	}

	public BrickLayout(Context context, AttributeSet attributeSet, int defStyle) {
		super(context, attributeSet, defStyle);
		allocateLineData();
		this.readStyleParameters(context, attributeSet);
	}

	private void allocateLineData() {
		lines = new LinkedList<LineData>();
		for (int i = 0; i < LINES_TO_ALLOCATE; i++) {
			allocateNewLine();
		}
	}

	private LineData allocateNewLine() {
		LineData d = new LineData();
		for (int j = 0; j < ELEMENTS_TO_ALLOCATE; j++) {
			d.elements.add(new ElementData(null, 0, 0, 0, 0));
		}
		lines.add(d);
		return d;
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int sizeWidth = MeasureSpec.getSize(widthMeasureSpec) - this.getPaddingRight() - this.getPaddingLeft();
		int sizeHeight = MeasureSpec.getSize(heightMeasureSpec) - this.getPaddingTop() - this.getPaddingBottom();

		int modeWidth = MeasureSpec.getMode(widthMeasureSpec);
		int modeHeight = MeasureSpec.getMode(heightMeasureSpec);

		int lineThicknessWithSpacing = 0;
		int lineThickness = 0;
		int lineLengthWithSpacing = 0;
		int lineLength = 0;

		int prevLinePosition = 0;

		int controlMaxLength = 0;
		int controlMaxThickness = 0;

		for (LineData d : lines) {
			d.allowableTextFieldWidth = 0;
			d.height = 0;
			d.minHeight = 0;
			d.numberOfTextFields = 0;
			d.totalTextFieldWidth = 0;
			for (ElementData ed : d.elements) {
				ed.height = 0;
				ed.width = 0;
				ed.posY = 0;
				ed.posX = 0;
				ed.view = null;
			}
		}

		LineData currentLine = lines.getFirst();

		// ************************ BEGIN PRE-LAYOUT (decide on a maximum width for text fields) ************************
		// 1. adding text to a text field never causes a line break
		// 2. text fields use as much space as possible
		// 3. on wider screens, line breaks are removed entirely and the layout is one line

		final int count = getChildCount();
		int elementInLineIndex = 0;
		for (int i = 0; i < count; i++) {
			final View child = getChildAt(i);
			if (child.getVisibility() == GONE) {
				continue;
			}

			if (child instanceof Spinner) {
				child.measure(MeasureSpec.makeMeasureSpec(sizeWidth, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(
						sizeHeight, modeHeight == MeasureSpec.EXACTLY ? MeasureSpec.AT_MOST : modeHeight));
			} else {
				child.measure(MeasureSpec.makeMeasureSpec(sizeWidth,
						modeWidth == MeasureSpec.EXACTLY ? MeasureSpec.AT_MOST : modeWidth), MeasureSpec
						.makeMeasureSpec(sizeHeight, modeHeight == MeasureSpec.EXACTLY ? MeasureSpec.AT_MOST
								: modeHeight));
			}

			Resources r = getResources();
			LayoutParams lp = (LayoutParams) child.getLayoutParams();

			int hSpacing = this.getHorizontalSpacing(lp);

			int childWidth = child.getMeasuredWidth();
			if (lp.textField) {
				childWidth = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, MIN_TEXT_FIELD_WIDTH_DP,
						r.getDisplayMetrics());
			}

			lineLength = lineLengthWithSpacing + childWidth;
			lineLengthWithSpacing = lineLength + hSpacing;

			int maxTextFieldWidthPixels = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
					MAX_TEXT_FIELD_WIDTH_DP, r.getDisplayMetrics());

			int currentTextFieldWidth = currentLine.totalTextFieldWidth + (lp.textField ? childWidth : 0);
			int currentTextFieldCount = currentLine.numberOfTextFields + (lp.textField ? 1 : 0);
			int lineLengthWithMaxTextField = (lineLength - (currentTextFieldWidth + hSpacing))
					+ (currentTextFieldCount * maxTextFieldWidthPixels);
			boolean preEmptiveNewLine = lp.newLine && lineLengthWithMaxTextField > sizeWidth;

			boolean newLine = preEmptiveNewLine || (modeWidth != MeasureSpec.UNSPECIFIED && lineLength > sizeWidth);

			if (newLine) {
				prevLinePosition = prevLinePosition + lineThicknessWithSpacing;

				int usedChildWidth = (lp.textField ? childWidth : 0);
				int endingWidthOfLineMinusFields = (lineLength - (usedChildWidth + hSpacing + currentLine.totalTextFieldWidth));
				float allowalbeWidth = (float) (sizeWidth - (endingWidthOfLineMinusFields))
						/ currentLine.numberOfTextFields;
				currentLine.allowableTextFieldWidth = (int) Math.floor(allowalbeWidth);

				Log.d("FOREST", "BL1: " + endingWidthOfLineMinusFields + ", " + currentLine.numberOfTextFields + ", "
						+ sizeWidth + "--> " + currentLine.allowableTextFieldWidth);

				currentLine = getNextLine(currentLine);

				lineLength = childWidth;
				lineLengthWithSpacing = lineLength + hSpacing;

				elementInLineIndex = 0;
			}

			getElement(currentLine, elementInLineIndex).view = child;
			elementInLineIndex++;

			if (lp.textField) {
				currentLine.totalTextFieldWidth += childWidth;
				currentLine.numberOfTextFields++;
			}
		}

		int endingWidthOfLineMinusFields = (lineLength - currentLine.totalTextFieldWidth);
		float allowalbeWidth = (float) (sizeWidth - endingWidthOfLineMinusFields) / currentLine.numberOfTextFields;
		currentLine.allowableTextFieldWidth = (int) Math.floor(allowalbeWidth);
		Log.d("FOREST", "BL2: " + endingWidthOfLineMinusFields + ", " + currentLine.numberOfTextFields + ", "
				+ sizeWidth + "--> " + currentLine.allowableTextFieldWidth);

		int minAllowableTextFieldWidth = Integer.MAX_VALUE;
		for (LineData d : lines) {
			if (d.allowableTextFieldWidth > 0 && d.allowableTextFieldWidth < minAllowableTextFieldWidth) {
				minAllowableTextFieldWidth = d.allowableTextFieldWidth;
			}
		}

		for (LineData d : lines) {
			for (ElementData ed : d.elements) {
				if (ed.view != null) {
					LayoutParams lp = (LayoutParams) ed.view.getLayoutParams();
					if (lp.textField) {
						((TextView) ed.view).setMaxWidth(minAllowableTextFieldWidth);
					}
				}
			}
		}

		// ************************ BEGIN LAYOUT ************************

		lineThicknessWithSpacing = 0;
		lineThickness = 0;
		lineLengthWithSpacing = 0;
		lineLength = 0;

		prevLinePosition = 0;

		controlMaxLength = 0;
		controlMaxThickness = 0;
		currentLine = lines.getFirst();

		boolean firstLine = true;
		for (LineData line : lines) {
			boolean newLine = !firstLine;
			for (ElementData element : line.elements) {
				View child = element.view;
				if (child == null || child.getVisibility() == GONE) {
					continue;
				}

				if (child instanceof Spinner) {
					child.measure(MeasureSpec.makeMeasureSpec(sizeWidth, MeasureSpec.EXACTLY), MeasureSpec
							.makeMeasureSpec(sizeHeight, modeHeight == MeasureSpec.EXACTLY ? MeasureSpec.AT_MOST
									: modeHeight));
				} else {
					child.measure(MeasureSpec.makeMeasureSpec(sizeWidth,
							modeWidth == MeasureSpec.EXACTLY ? MeasureSpec.AT_MOST : modeWidth), MeasureSpec
							.makeMeasureSpec(sizeHeight, modeHeight == MeasureSpec.EXACTLY ? MeasureSpec.AT_MOST
									: modeHeight));
				}

				LayoutParams lp = (LayoutParams) child.getLayoutParams();

				int hSpacing = this.getHorizontalSpacing(lp);
				int vSpacing = this.getVerticalSpacing(lp);

				int childWidth = child.getMeasuredWidth();
				int childHeight = child.getMeasuredHeight();

				boolean updateSmallestHeight = currentLine.minHeight == 0 || currentLine.minHeight > childHeight;
				currentLine.minHeight = (updateSmallestHeight ? childHeight : currentLine.minHeight);

				lineLength = lineLengthWithSpacing + childWidth;
				lineLengthWithSpacing = lineLength + hSpacing;

				if (lp.newLine && !newLine) {
					lineLength += hSpacing;
					lineLengthWithSpacing += hSpacing;
				}

				if (newLine) {
					newLine = false;
					prevLinePosition = prevLinePosition + lineThicknessWithSpacing;

					currentLine = getNextLine(currentLine);

					lineThickness = childHeight;
					lineLength = childWidth;
					lineThicknessWithSpacing = childHeight + vSpacing;
					lineLengthWithSpacing = lineLength + hSpacing;
				}

				lineThicknessWithSpacing = Math.max(lineThicknessWithSpacing, childHeight + vSpacing);
				lineThickness = Math.max(lineThickness, childHeight);

				currentLine.height = lineThickness;

				int posX = getPaddingLeft() + lineLength - childWidth;
				int posY = getPaddingTop() + prevLinePosition;

				element.posX = posX;
				element.posY = posY;
				element.width = childWidth;
				element.height = childHeight;

				controlMaxLength = Math.max(controlMaxLength, lineLength);
				controlMaxThickness = prevLinePosition + lineThickness;
			}
			firstLine = false;
		}

		int x = controlMaxLength;
		int y = controlMaxThickness;

		y += customPadding * 2;

		y = Math.max(y, getSuggestedMinimumHeight());

		int yAdjust = Math.round((y - controlMaxThickness) * 0.5f);

		if (y > getSuggestedMinimumHeight()) {
			yAdjust += Math.round(lines.get(0).minHeight * -0.15f);
		}

		for (LineData d : lines) {
			for (ElementData ed : d.elements) {
				if (ed.view != null) {
					int yAdjust2 = 0;
					if (ed.height < d.height) {
						yAdjust2 = Math.round((d.height - ed.height) * 0.5f);
					}

					ed.posY += yAdjust + yAdjust2;
					LayoutParams lp = (LayoutParams) ed.view.getLayoutParams();
					lp.setPosition(ed.posX, ed.posY);
				}
			}
		}

		this.setMeasuredDimension(resolveSize(x, widthMeasureSpec), resolveSize(y, heightMeasureSpec));
	}

	private LineData getNextLine(LineData currentLine) {
		int index = lines.indexOf(currentLine) + 1;
		if (index < lines.size()) {
			return lines.get(index);
		} else {
			return allocateNewLine();
		}
	}

	private ElementData getElement(LineData currentLine, int elementInLineIndex) {
		if (elementInLineIndex < currentLine.elements.size()) {
			return currentLine.elements.get(elementInLineIndex);
		} else {
			ElementData d = new ElementData(null, 0, 0, 0, 0);
			currentLine.elements.add(d);
			return d;
		}
	}

	private int getVerticalSpacing(LayoutParams lp) {
		int vSpacing;
		if (lp.verticalSpacingSpecified()) {
			vSpacing = lp.verticalSpacing;
		} else {
			vSpacing = this.verticalSpacing;
		}
		return vSpacing;
	}

	private int getHorizontalSpacing(LayoutParams lp) {
		int hSpacing;
		if (lp.horizontalSpacingSpecified()) {
			hSpacing = lp.horizontalSpacing;
		} else {
			hSpacing = this.horizontalSpacing;
		}
		return hSpacing;
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		final int count = getChildCount();
		for (int i = 0; i < count; i++) {
			View child = getChildAt(i);
			LayoutParams lp = (LayoutParams) child.getLayoutParams();
			child.layout(lp.x, lp.y, lp.x + child.getMeasuredWidth(), lp.y + child.getMeasuredHeight());
		}
	}

	@Override
	protected boolean drawChild(Canvas canvas, View child, long drawingTime) {
		boolean more = super.drawChild(canvas, child, drawingTime);
		this.drawDebugInfo(canvas, child);
		return more;
	}

	@Override
	protected boolean checkLayoutParams(ViewGroup.LayoutParams p) {
		return p instanceof LayoutParams;
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
		TypedArray a = context.obtainStyledAttributes(attributeSet, R.styleable.BrickLayout);
		try {
			customPadding = a.getDimensionPixelSize(R.styleable.BrickLayout_customPadding, 0);
			horizontalSpacing = a.getDimensionPixelSize(R.styleable.BrickLayout_horizontalSpacing, 0);
			verticalSpacing = a.getDimensionPixelSize(R.styleable.BrickLayout_verticalSpacing, 0);
			orientation = a.getInteger(R.styleable.BrickLayout_orientation, HORIZONTAL);
			debugDraw = a.getBoolean(R.styleable.BrickLayout_debugDraw, false);
		} finally {
			a.recycle();
		}
	}

	public void drawDebugInfo(Canvas canvas, View child) {
		if (!debugDraw) {
			return;
		}

		Paint childPaint = this.createPaint(0xffffff00);
		Paint layoutPaint = this.createPaint(0xff00ff00);
		Paint newLinePaint = this.createPaint(0xffff0000);

		LayoutParams lp = (LayoutParams) child.getLayoutParams();

		if (lp.horizontalSpacing > 0) {
			float x = child.getRight();
			float y = child.getTop() + child.getHeight() / 2.0f;
			canvas.drawLine(x, y, x + lp.horizontalSpacing, y, childPaint);
			canvas.drawLine(x + lp.horizontalSpacing - 4.0f, y - 4.0f, x + lp.horizontalSpacing, y, childPaint);
			canvas.drawLine(x + lp.horizontalSpacing - 4.0f, y + 4.0f, x + lp.horizontalSpacing, y, childPaint);
		} else if (this.horizontalSpacing > 0) {
			float x = child.getRight();
			float y = child.getTop() + child.getHeight() / 2.0f;
			canvas.drawLine(x, y, x + this.horizontalSpacing, y, layoutPaint);
			canvas.drawLine(x + this.horizontalSpacing - 4.0f, y - 4.0f, x + this.horizontalSpacing, y, layoutPaint);
			canvas.drawLine(x + this.horizontalSpacing - 4.0f, y + 4.0f, x + this.horizontalSpacing, y, layoutPaint);
		}

		if (lp.verticalSpacing > 0) {
			float x = child.getLeft() + child.getWidth() / 2.0f;
			float y = child.getBottom();
			canvas.drawLine(x, y, x, y + lp.verticalSpacing, childPaint);
			canvas.drawLine(x - 4.0f, y + lp.verticalSpacing - 4.0f, x, y + lp.verticalSpacing, childPaint);
			canvas.drawLine(x + 4.0f, y + lp.verticalSpacing - 4.0f, x, y + lp.verticalSpacing, childPaint);
		} else if (this.verticalSpacing > 0) {
			float x = child.getLeft() + child.getWidth() / 2.0f;
			float y = child.getBottom();
			canvas.drawLine(x, y, x, y + this.verticalSpacing, layoutPaint);
			canvas.drawLine(x - 4.0f, y + this.verticalSpacing - 4.0f, x, y + this.verticalSpacing, layoutPaint);
			canvas.drawLine(x + 4.0f, y + this.verticalSpacing - 4.0f, x, y + this.verticalSpacing, layoutPaint);
		}

		if (lp.newLine) {
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
		public int totalTextFieldWidth;
		public int allowableTextFieldWidth;
		public int numberOfTextFields;
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
		private boolean textField = false;

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

		public void setWidth(int width) {
			this.width = width;
		}

		private void readStyleParameters(Context context, AttributeSet attributeSet) {
			TypedArray a = context.obtainStyledAttributes(attributeSet, R.styleable.FlowLayout_LayoutParams);
			try {
				horizontalSpacing = a.getDimensionPixelSize(
						R.styleable.FlowLayout_LayoutParams_layout_horizontalSpacing, NO_SPACING);
				verticalSpacing = a.getDimensionPixelSize(R.styleable.FlowLayout_LayoutParams_layout_verticalSpacing,
						NO_SPACING);
				newLine = a.getBoolean(R.styleable.FlowLayout_LayoutParams_layout_newLine, false);
				textField = a.getBoolean(R.styleable.FlowLayout_LayoutParams_layout_textField, false);
			} finally {
				a.recycle();
			}
		}
	}
}
