package org.catrobat.catroid.tutorial;

import java.util.ArrayList;

import org.catrobat.catroid.R;

import android.content.Context;
import android.content.res.Resources;
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
		Context context = Tutorial.getInstance(null).getActualContext();
		Resources resources = context.getResources();
		Lesson lesson = new Lesson();
		lesson.lessonID = lessonArray.size();
		lesson.lessonName = lessonName;
		ArrayList<Task> contentList = new ArrayList<Task>();
		int[] pos = { 100, 400 };
		SurfaceObjectTutor tutor = new SurfaceObjectTutor(tutorialOverlay, pos);
		SurfaceObjectText text = new SurfaceObjectText(tutorialOverlay,
				resources.getString(R.string.tutorial_mandatory_welcome));
		SurfaceObjectBubble bubble = new SurfaceObjectBubble(tutorialOverlay, text);
		SurfaceObjectNextButton next = new SurfaceObjectNextButton(tutorialOverlay);

		Task task = new Task(text, tutor, bubble);
		contentList.add(task);
		lesson.setLessonContent(contentList);
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
