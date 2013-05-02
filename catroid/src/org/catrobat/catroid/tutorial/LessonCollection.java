package org.catrobat.catroid.tutorial;

import java.util.ArrayList;

import android.util.Log;

public class LessonCollection {
	private ArrayList<Lesson> lessonArray;
	private int currentLesson;
	private int currentPossibleLesson;
	private TutorialOverlay tutorialOverlay;

	public void setTutorialOverlay(TutorialOverlay tutorialOverlay) {
		this.tutorialOverlay = tutorialOverlay;
	}

	public int getLastPossibleLessonNumber() {
		return currentPossibleLesson;
	}

	public void setLastPossibleLessonNumber(int value) {
		currentPossibleLesson = value;
	}

	public LessonCollection() {
		lessonArray = new ArrayList<Lesson>();
	}

	public void addLesson(String lessonName) {
		Lesson lesson = new Lesson();
		lesson.lessonID = lessonArray.size();
		lesson.lessonName = lessonName;
		ArrayList<SurfaceObjectText> contentList = new ArrayList<SurfaceObjectText>();
		int[] pos = { 100, 100 };
		SurfaceObjectText text = new SurfaceObjectText(tutorialOverlay, "BLA BLA", pos);
		contentList.add(text);
		lesson.lessonContent = contentList;
		lessonArray.add(lesson);
		lessonArray.add(lesson);
	}

	public boolean switchToLesson(int lessonID) {
		if (lessonID >= lessonArray.size()) {
			return false;
		} else {
			currentLesson = lessonID;
			Log.i("tutorial", "Switched to Cl: " + currentLesson);
			return true;
		}
	}

	boolean executeTask() {
		return (lessonArray.get(currentLesson)).execute();
	}

	boolean nextLesson() {
		if ((currentLesson + 1) >= lessonArray.size()) {
			return false;
		} else {
			currentLesson++;
			if (currentPossibleLesson < currentLesson) {
				currentPossibleLesson = currentLesson;
			}
			return true;
		}
	}

	boolean previousLesson() {
		if ((currentLesson - 1) < 0) {
			return (false);
		} else {
			currentLesson--;
			return (true);
		}
	}

	ArrayList<String> getLessons() {
		ArrayList<String> lessonNames = new ArrayList<String>();
		for (Lesson tmp : lessonArray) {
			lessonNames.add(tmp.lessonName);
		}
		return lessonNames;
	}

	public void clean() {
		Log.i("tutorial", Thread.currentThread().getName() + ": LessonCollection: clean called!");
		for (Lesson lesson : lessonArray) {
			lesson.clean();
		}
	}

	public int getSizeOfCurrentLesson() {
		return (lessonArray.get(currentLesson)).getSizeOfLesson();
	}

	public int getSizeOfLessonCollection() {
		return lessonArray.size();
	}

	@Override
	protected void finalize() throws Throwable {
		Log.i("tutorial", Thread.currentThread().getName() + ": LessonCollection: finalize called!");
		super.finalize();
	}

}
