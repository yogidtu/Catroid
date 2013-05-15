/**
 *  Catroid: An on-device graphical programming language for Android devices
 *  Copyright (C) 2010  Catroid development team 
 *  (<http://code.google.com/p/catroid/wiki/Credits>)
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.catrobat.catroid.tutorial;

import java.util.ArrayList;

import android.content.Context;
import android.util.Log;

/**
 * @author faxxe
 * 
 */
public class TutorialThread extends Thread implements Runnable {
	public boolean tutorialThreadRunning = true;
	private volatile ArrayList<String> notifies = new ArrayList<String>();
	private boolean interrupted = false;
	private boolean iAck = false;

	public TutorialThread(Context context) {
		Thread thisThread = new Thread(this);
		thisThread.setName("TutorialThread");
		Log.i("tutorial", Thread.currentThread().getName() + ": New TutorialThread started... ");
	}

	@Override
	public void run() {
		runTutorial();
	}

	public void startThread() {
		tutorialThreadRunning = true;
		this.start();
	}

	public void stopThread() {
		tutorialThreadRunning = false;
		boolean retry = true;
		while (retry) {
			try {
				synchronized (this) {
					this.notifyAll();
				}
				this.join(1);
				retry = false;
			} catch (InterruptedException e) {
			}
		}
	}

	public void notifyThread() {
		doNotify();
	}

	private void doNotify() {
		Log.i("tutorial", Thread.currentThread().getName() + ": notified");
		synchronized (this) {
			this.notify();
		}
	}

	public void waitThread() {
	}

	private void runTutorial() {
		while (tutorialThreadRunning) {

			if (!interrupted) {

				synchronized (this) {
					try {
						Log.i("tutorial", Thread.currentThread().getName() + ": waiting for notification");
						wait();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}

			}

			if (interrupted) {
			}
		}
		return;
	}

	public void setLastTutorModified(int stepsBack) {
		if (stepsBack == 0) {
			stepsBack++;
		}

	}

	public boolean getAck() {
		return iAck;
	}

	public boolean resetAndCheckIfEndTutorial() {
		Log.i("tutorial", "Tutorial stopped in TUT-Thread");

		if (tutorialThreadRunning) {
			Log.i("tutorial", "STOP Tutorial");
			Tutorial.getInstance(null).stopButtonTutorial();
			return true;
		}

		return false;
	}

	public void setNotification(String notification) {
		notifies.add(notification);
	}

	public void setInterrupt(boolean flag) {
		interrupted = flag;
	}

}
