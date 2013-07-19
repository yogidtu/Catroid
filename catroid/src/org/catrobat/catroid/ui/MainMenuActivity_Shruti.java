package org.catrobat.catroid.ui;

import org.catrobat.catroid.R;
import org.catrobat.catroid.ui.fragment.ProjectsListFragment;
import org.catrobat.catroid.ui.fragment.WebViewFragment_Shruti;

import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragmentActivity;

public class MainMenuActivity_Shruti extends SherlockFragmentActivity implements
		WebViewFragment_Shruti.OnHeadlineSelectedListener,
		MainMenuActivityFragment_Shruti.OnWebSelectedListener,

		MainMenuActivityFragment_Shruti.OnHeadlineSelectedListenerList {
	public static int flag = -1;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.fragment_home_screen_shruti);
		MainMenuActivityFragment_Shruti listFragment = (MainMenuActivityFragment_Shruti) getSupportFragmentManager()
				.findFragmentById(R.id.projectList);

		android.support.v4.app.FragmentTransaction ft = getSupportFragmentManager()
				.beginTransaction();
		listFragment = new MainMenuActivityFragment_Shruti();
		Log.v("reached111111", "till here");
		ft.replace(R.id.projectList, listFragment, "List_Fragment");
		ft.addToBackStack(null);
		ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
		ft.commit();
		Log.v("reached", "till here");

		/*
		 * ProjectsListFragment listFragment = (ProjectsListFragment)
		 * getSupportFragmentManager().findFragmentById( R.id.projectList);
		 * 
		 * if (savedInstanceState == null) {
		 * android.support.v4.app.FragmentTransaction ft =
		 * getSupportFragmentManager().beginTransaction(); listFragment = new
		 * ProjectsListFragment(); Log.v("reached111111", "till here");
		 * ft.replace(R.id.projectList, listFragment, "List_Fragment");
		 * 
		 * ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
		 * ft.commit(); Log.v("reached", "till here");
		 */

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.catrobat.catroid.ui.MainMenuActivityFragment_Shruti.
	 * OnHeadlineSelectedListenerList#onArticleSelectedList(int)
	 */
	public static int flag4;
	public static int flag5;

	@Override
	public void onArticleSelectedList(int position) {
		// TODO Auto-generated method stub
		// flag3 = -1;

		// Toast.makeText(this, "flag = " + flag + "and " + "position = " +
		// position, Toast.LENGTH_LONG).show();
		flag4 = position;
		try {
			if (flag4 == 1) {

				getFragmentManager().popBackStack();
				MyProjectsActivity f5 = new MyProjectsActivity();
				android.support.v4.app.FragmentTransaction ft = getSupportFragmentManager()
						.beginTransaction();
				ft.replace(R.id.projectList, f5); // f2_container is your
													// FrameLayout container
				ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
				ft.addToBackStack(null);
				ft.commit();
				// flag = -1;
				flag4 = -1;

			}
		} catch (Exception e) {
			Toast.makeText(this, "Error!!!!!!", Toast.LENGTH_LONG).show();
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.catrobat.catroid.ui.MainMenuActivityFragment_Shruti.OnWebSelectedListener
	 * #onWebSeleced(int)
	 */
	String yourData;
	public static BroadcastReceiver receiver;
	public static int flag3 = -1;

	@Override
	public void onWebSelected(int position) {
		// TODO Auto-generated method stub
		flag5 = position;
		try {
			if (flag5 == 1) {

				getFragmentManager().popBackStack();
				WebViewFragment_Shruti f5 = new WebViewFragment_Shruti();
				android.support.v4.app.FragmentTransaction ft = getSupportFragmentManager()
						.beginTransaction();
				ft.replace(R.id.projectList, f5); // f2_container is your
													// FrameLayout container
				ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
				ft.addToBackStack(null);
				ft.commit();
				// flag = -1;
				f5.setURLContent("https://pocketcode.org");
				flag5 = -1;

			}
		} catch (Exception e) {
			Toast.makeText(this, "Error!!!!!!", Toast.LENGTH_LONG).show();
		}

	}

	@Override
	public void onArticleSelected(int position) {
		// TODO Auto-generated method stub
		// flag3 = -1;
		receiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				yourData = intent.getStringExtra("tag");
			}
		};

		IntentFilter filter = new IntentFilter();
		filter.addAction("com.your.app.DATA_BROADCAST");
		registerReceiver(receiver, filter);
		flag3 = 1;
		flag = position;
		// Toast.makeText(this, "flag = " + flag + "and " + "position = " +
		// position, Toast.LENGTH_LONG).show();
		try {
			if (flag == 1) {
				String flag1 = yourData;
				if (flag1.equals("1")) {
					// getFragmentManager().popBackStack();
					ProjectsListFragment f4 = new ProjectsListFragment();
					android.support.v4.app.FragmentTransaction ft = getSupportFragmentManager()
							.beginTransaction();
					ft.replace(R.id.projectList, f4); // f2_container is your
														// FrameLayout container
					ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
					ft.addToBackStack(null);
					ft.commit();
					flag = -1;
					flag1 = "null";

				}
			}
		} catch (Exception e) {
			// Toast.makeText(this, "Error!!!!!!", Toast.LENGTH_LONG).show();
		}
	}

	@Override
	protected void onStop() {
		if (flag3 == 1) {
			unregisterReceiver(receiver);
			flag3 = -1;

		}
		super.onStop();
	}

}
