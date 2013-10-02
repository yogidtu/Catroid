/**
 *  Catroid: An on-device visual programming system for Android devices
 *  Copyright (C) 2010-2013 The Catrobat Team
 *  (<http://developer.catrobat.org/credits>)
 *  
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as
 *  published by the Free Software Foundation, either version 3 of the
 *  License, or (at your option) any later version.
 *  
 *  An additional term exception under section 7 of the GNU Affero
 *  General Public License, version 3, is available at
 *  http://developer.catrobat.org/license_additional_term
 *  
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU Affero General Public License for more details.
 *  
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.catrobat.catroid.ui.adapter;

import static android.text.format.DateUtils.DAY_IN_MILLIS;
import static android.text.format.DateUtils.FORMAT_ABBREV_MONTH;
import static android.text.format.DateUtils.FORMAT_SHOW_TIME;
import static android.text.format.DateUtils.FORMAT_SHOW_YEAR;
import static android.text.format.DateUtils.SECOND_IN_MILLIS;
import static android.text.format.DateUtils.isToday;

import android.content.Context;
import android.text.format.DateUtils;
import android.text.format.Time;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.catrobat.catroid.R;
import org.catrobat.catroid.common.ProjectData;
import org.catrobat.catroid.stage.StageListener;
import org.catrobat.catroid.utils.UtilFile;
import org.catrobat.catroid.utils.Utils;

import java.io.File;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;
import java.util.TreeSet;

public class ProjectAdapter extends BaseAdapter {
	private static LayoutInflater inflater;
	private boolean showDetails = false;
	private int selectMode = ListView.CHOICE_MODE_NONE;;
	private List<ProjectData> projects;
	private Set<Integer> checkedProjects = new TreeSet<Integer>();
	private OnProjectEditListener onProjectEditListener;
	private Context context;

	private static class ViewHolder {
		private RelativeLayout background;
		private CheckBox checkbox;
		private TextView projectName;
		private ImageView image;
		private TextView size;
		private TextView dateChanged;
		private View projectDetails;
		private ImageView arrow;
	}

	public interface OnProjectEditListener {
		public void onProjectChecked();

		public void onProjectEdit(int position);
	}

	public ProjectAdapter(Context context, List<ProjectData> projects) {
		inflater = LayoutInflater.from(context);
		this.context = context;
		this.projects = projects;
	}

	@Override
	public int getCount() {
		return projects.size();
	}

	@Override
	public ProjectData getItem(int position) {
		return projects.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public int getViewTypeCount() {
		return 1;
	}

	public void setOnProjectEditListener(OnProjectEditListener listener) {
		onProjectEditListener = listener;
	}

	public void setShowDetails(boolean showDetails) {
		this.showDetails = showDetails;
	}

	public boolean getShowDetails() {
		return showDetails;
	}

	public void setSelectMode(int selectMode) {
		this.selectMode = selectMode;
	}

	public int getSelectMode() {
		return selectMode;
	}

	public Set<Integer> getCheckedProjects() {
		return checkedProjects;
	}

	public int getAmountOfCheckedProjects() {
		return checkedProjects.size();
	}

	public void addCheckedProject(int position) {
		checkedProjects.add(position);
	}

	public void clearCheckedProjects() {
		checkedProjects.clear();
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		final ViewHolder holder;
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.activity_my_projects_list_item, null);
			holder = new ViewHolder();
			holder.background = (RelativeLayout) convertView.findViewById(R.id.my_projects_activity_item_background);
			holder.checkbox = (CheckBox) convertView.findViewById(R.id.project_checkbox);
			holder.projectName = (TextView) convertView.findViewById(R.id.my_projects_activity_project_title);
			holder.image = (ImageView) convertView.findViewById(R.id.my_projects_activity_project_image);
			holder.size = (TextView) convertView.findViewById(R.id.my_projects_activity_size_of_project_2);
			holder.dateChanged = (TextView) convertView.findViewById(R.id.my_projects_activity_project_changed_2);
			holder.projectDetails = convertView.findViewById(R.id.my_projects_activity_list_item_details);
			holder.arrow = (ImageView) convertView.findViewById(R.id.arrow_right);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		ProjectData projectData = getItem(position);

		holder.projectName.setText(projectData.projectName);

		File projectDirectory = new File(Utils.buildProjectPath(projectData.projectName));

		setThumbnail(holder, projectDirectory);

		setOnLongClickListener(holder);

		setOnClickListener(holder, position);

		setOnCheckedChangeListener(holder, position);

		if (showDetails) {
			holder.projectDetails.setVisibility(View.VISIBLE);
			holder.projectName.setSingleLine(false);
			holder.size.setText(UtilFile.getSizeAsString(projectDirectory));
			holder.dateChanged.setText(lastModified(projectData));
		} else {
			holder.projectDetails.setVisibility(View.GONE);
			holder.projectName.setSingleLine(true);
		}

		if (selectMode == ListView.CHOICE_MODE_NONE) {
			holder.checkbox.setVisibility(View.GONE);
			holder.arrow.setVisibility(View.VISIBLE);
			holder.background.setBackgroundResource(R.drawable.button_background_selector);
			clearCheckedProjects();
		} else {
			holder.checkbox.setVisibility(View.VISIBLE);
			holder.checkbox.setChecked(checkedProjects.contains(position));
			holder.arrow.setVisibility(View.GONE);
			holder.background.setBackgroundResource(R.drawable.button_background_shadowed);
		}

		return convertView;
	}

	private void setOnClickListener(final ViewHolder holder, final int position) {
		holder.background.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View view) {
				if (selectMode != ListView.CHOICE_MODE_NONE) {
					holder.checkbox.setChecked(!holder.checkbox.isChecked());
				} else if (onProjectEditListener != null) {
					onProjectEditListener.onProjectEdit(position);
				}

			}
		});
	}

	private void setOnLongClickListener(final ViewHolder holder) {
		holder.background.setOnLongClickListener(new OnLongClickListener() {

			@Override
			public boolean onLongClick(View view) {
				if (selectMode != ListView.CHOICE_MODE_NONE) {
					return true;
				}
				return false;
			}
		});
	}

	private void setOnCheckedChangeListener(final ViewHolder holder, final int position) {
		holder.checkbox.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (isChecked) {
					if (selectMode == ListView.CHOICE_MODE_SINGLE) {
						clearCheckedProjects();
					}
					checkedProjects.add(position);
				} else {
					checkedProjects.remove(position);
				}
				notifyDataSetChanged();

				if (onProjectEditListener != null) {
					onProjectEditListener.onProjectChecked();
				}
			}
		});
	}

	private void setThumbnail(final ViewHolder holder, File projectDirectory) {
		File screenshot = new File(projectDirectory, StageListener.SCREENSHOT_MANUAL_FILE_NAME);
		if (!(screenshot.exists() && screenshot.isFile() && screenshot.length() > 0)) {
			screenshot = new File(projectDirectory, StageListener.SCREENSHOT_AUTOMATIC_FILE_NAME);
		}
		if (!(screenshot.exists() && screenshot.isFile() && screenshot.length() > 0)) {
			holder.image.setImageBitmap(null);
		} else {
			Picasso.with(context) //
					.load(screenshot) //
					.resizeDimen(R.dimen.thumbnail_width, R.dimen.thumbnail_height) //
					.centerCrop() //
					.into(holder.image);
		}
	}

	private String lastModified(ProjectData projectData) {
		String lastModificationDate;
		long now = System.currentTimeMillis();
		if (isToday(projectData.lastUsed)) {
			lastModificationDate = DateUtils.getRelativeTimeSpanString(projectData.lastUsed, now, DAY_IN_MILLIS) + " "
					+ DateUtils.formatDateTime(context, projectData.lastUsed, FORMAT_SHOW_TIME);
		} else if (isYesterday(projectData.lastUsed)) {
			lastModificationDate = DateUtils.getRelativeTimeSpanString(projectData.lastUsed, now, DAY_IN_MILLIS,
					FORMAT_ABBREV_MONTH | FORMAT_SHOW_YEAR).toString();
		} else {
			lastModificationDate = DateUtils.formatDateTime(context, projectData.lastUsed, FORMAT_ABBREV_MONTH
					| FORMAT_SHOW_YEAR);
		}
		return lastModificationDate;
	}

	private boolean isYesterday(long day) {
		long today = System.currentTimeMillis();
		int startDay = Time.getJulianDay(day, TimeZone.getDefault().getOffset(day) / SECOND_IN_MILLIS);
		int currentDay = Time.getJulianDay(today, TimeZone.getDefault().getOffset(today) / SECOND_IN_MILLIS);

		return Math.abs(currentDay - startDay) == 1;
	}

}
