package org.catrobat.catroid.content.actions;

import com.badlogic.gdx.scenes.scene2d.actions.TemporalAction;

import org.catrobat.catroid.content.Sprite;
import org.catrobat.catroid.formulaeditor.Formula;

public class SayForBrickAction extends TemporalAction {

	private Formula durationInSeconds;
	private Sprite sprite;
	private String text;

	@Override
	protected void update(float percent) {
		// TODO
	}

	public void setDurationInSeconds(Formula durationInSeconds) {
		this.durationInSeconds = durationInSeconds;
	}

	public void setSprite(Sprite sprite) {
		this.sprite = sprite;
	}

	public void setText(String text) {
		this.text = text;
	}
}
