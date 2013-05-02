package org.catrobat.catroid.tutorial;

import java.util.ArrayList;

import android.util.Log;

public class Lesson {
	public int lessonID;
	public String lessonName;
	private int currentStep;
	public ArrayList<SurfaceObjectText> lessonContent = new ArrayList<SurfaceObjectText>();

	public Lesson() {
		this.currentStep = 0;
		Log.i("tutorial", Thread.currentThread().getName() + ": Lesson currentStep: " + this.currentStep);
	}

	public void clean() {
		lessonContent.clear();
	}

	public boolean execute() {
		Log.i("tutorial", Thread.currentThread().getName() + ": @ Step " + currentStep + " executing: "
				+ lessonContent.get(currentStep).toString());

		return true;
	}

	public int getSizeOfLesson() {
		return this.lessonContent.size();
	}
}
