package org.catrobat.catroid.content.bricks;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.View.OnClickListener;

import org.catrobat.catroid.R;
import org.catrobat.catroid.common.ScreenValues;
import org.catrobat.catroid.content.Sprite;
import org.catrobat.catroid.formulaeditor.Formula;
import org.catrobat.catroid.ui.fragment.FormulaEditorFragment;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public abstract class BubbleBrickBaseType extends BrickBaseType implements OnClickListener, FormulaBrick {

	private static final long serialVersionUID = 1L;
	protected static final int STRING_OFFSET = 20;
	protected static final int BOUNDARY_PIXEL = 30;

	protected Formula text;
	protected Formula duration;
	protected transient View prototypeView;
	protected transient View bubble;
	protected transient Context context;
	protected byte[] bubbleByteArray;

	public BubbleBrickBaseType() {
	}

	public BubbleBrickBaseType(Sprite sprite, String say, int seconds, Context context) {
		this.sprite = sprite;
		this.text = new Formula(say);
		this.duration = new Formula(seconds);
		this.context = context;
	}

	public BubbleBrickBaseType(Sprite sprite, Formula say, Formula seconds, Context context) {
		this.sprite = sprite;
		this.text = say;
		this.duration = seconds;
		this.context = context;
	}

	@Override
	public Formula getFormula() {
		return text;
	}

	@Override
	public int getRequiredResources() {
		return NO_RESOURCES;
	}

	@Override
	public void onClick(View view) {
		if (checkbox.getVisibility() == View.VISIBLE) {
			return;
		}
		switch (view.getId()) {
			case R.id.brick_say_edit_text:
				FormulaEditorFragment.showFragment(view, this, text);
				break;

			case R.id.brick_think_edit_text:
				FormulaEditorFragment.showFragment(view, this, text);
				break;

			case R.id.brick_for_edit_text:
				FormulaEditorFragment.showFragment(view, this, duration);
				break;
		}
	}

	protected String getNormalizedText() {
		String text = this.text.interpretString(sprite);
		String normalizedText = new String();

		for (int index = 0; index < text.length(); index++) {
			if (index % STRING_OFFSET == 0) {
				normalizedText += "\n";
			}
			//TODO: max size of text.
			normalizedText += text.charAt(index);
		}
		return normalizedText;
	}

	protected void updateBubbleByteArrayFromDrawingCache() {
		bubble.setDrawingCacheEnabled(true);
		bubble.measure(MeasureSpec.makeMeasureSpec(ScreenValues.SCREEN_WIDTH - BOUNDARY_PIXEL, MeasureSpec.AT_MOST),
				MeasureSpec.makeMeasureSpec(ScreenValues.SCREEN_HEIGHT - BOUNDARY_PIXEL, MeasureSpec.AT_MOST));
		bubble.layout(0, 0, bubble.getMeasuredWidth(), bubble.getMeasuredHeight());

		Bitmap bitmap = bubble.getDrawingCache();
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		bitmap.compress(Bitmap.CompressFormat.PNG, 0, stream);
		bubbleByteArray = stream.toByteArray();
		try {
			stream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		bubble.setDrawingCacheEnabled(false);
	}

	public void setContext(Context applicationContext) {
		this.context = applicationContext;
	}
}
