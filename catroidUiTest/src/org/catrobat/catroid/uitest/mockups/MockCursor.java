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
package org.catrobat.catroid.uitest.mockups;

import java.util.HashMap;
import java.util.Map;

import android.util.SparseArray;

public class MockCursor extends android.test.mock.MockCursor {

	private final Map<String, Integer> columns = new HashMap<String, Integer>();
	private final SparseArray<String> strings = new SparseArray<String>();

	public MockCursor(Map<String, String> values) {
		int index = 0;
		for (Map.Entry<String, String> pair : values.entrySet()) {
			columns.put(pair.getKey(), index);
			strings.put(index, pair.getValue());
			index++;
		}
	}

	@Override
	public boolean moveToFirst() {
		return true;
	}

	@Override
	public int getColumnIndex(String columnName) {
		return columns.get(columnName);
	}

	@Override
	public String getString(int columnIndex) {
		return strings.get(columnIndex);
	}
}
