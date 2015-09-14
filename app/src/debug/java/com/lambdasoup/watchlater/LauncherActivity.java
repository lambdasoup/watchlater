/*
 * Copyright (c) 2015. Maximilian Hille <mh@lambdasoup.com>
 *
 * This file is part of Watch Later.
 *
 * Watch Later is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Watch Later is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Watch Later.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.lambdasoup.watchlater;

import android.app.Activity;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;

import static android.net.Uri.parse;


public class LauncherActivity extends Activity {

	private static final String[] VIDEO_EXAMPLE_URIS = {
			"https://www.youtube.com/watch?v=jqxENMKaeCU",
			"https://youtu.be/1qG61X3bkKs",
			"http://www.youtube.com/attribution_link?u=/watch%3Fv%3DJ1zNbWJC5aw%26feature%3Dem-subs_digest",
			"http://www.youtube.com/v/OdT9z-JjtJk",
			"http://www.youtube.com/embed/UkWd0azv3fQ"
	};

	private static final String[] PLAYLIST_EXAMPLE_URIS = {
			"https://www.youtube.com/embed/videoseries?list=PL0INsTTU1k2UO-2-AwomFmAs4nuZU9ht3",
			"http://www.youtube.com/attribution_link?u=/playlist%3Flist%3DPL0INsTTU1k2UO-2-AwomFmAs4nuZU9ht3%26feature%3Dem-share_playlist_user",
			"https://www.youtube.com/playlist?list=PLxLNk7y0uwqfXzUjcbVT3UuMjRd7pOv_U"
	};

	private static final String[] NONEXAMPLE_URIS = {
			"https://www.youtube.com/channel/UC2bkHVIDjXS7sgrgjFtzOXQ"
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_launcher);

		ListView listView = (ListView) findViewById(R.id.list);
		final FlatGroupAdapter adapter = new FlatGroupAdapter(
				getApplicationContext(),
				new FlatGroupAdapter.Group(R.string.video_example_uris, VIDEO_EXAMPLE_URIS),
				new FlatGroupAdapter.Group(R.string.playlist_example_uris, PLAYLIST_EXAMPLE_URIS),
				new FlatGroupAdapter.Group(R.string.non_example_urls, NONEXAMPLE_URIS)

		);
		listView.setAdapter(adapter);
		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				FlatGroupAdapter.ItemInfo itemInfo = adapter.getItem(position);
				if (itemInfo.isHeader()) {
					return;
				}
				startActivity(new Intent(Intent.ACTION_VIEW, parse(itemInfo.item)));
			}
		});
	}

	private static class FlatGroupAdapter extends BaseAdapter {
		private final Group[] groups;
		private final Context context;

		FlatGroupAdapter(Context context, Group... groups) {
			this.context = context;
			this.groups = groups;
		}

		@Override
		public int getCount() {
			int count = 0;
			for (Group group : groups) {
				count += group.getFlatLength();
			}
			return count;
		}

		@Override
		public ItemInfo getItem(int position) {
			GroupPosition groupPosition = getGroupPosition(position);
			if (groupPosition.position == GroupPosition.HEADER_POSITION) {
				return new ItemInfo(groups[groupPosition.group].headerId, null);
			} else {
				return new ItemInfo(null, groups[groupPosition.group].data[groupPosition.position]);
			}
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

			ItemInfo itemInfo = getItem(position);
			if (itemInfo.isHeader()) {
				View rowView = inflater.inflate(R.layout.list_header_launcher, parent, false);
				TextView textView = (TextView) rowView.findViewById(R.id.text);
				textView.setText(itemInfo.headerId);
				return rowView;
			} else {
				View rowView = inflater.inflate(android.R.layout.simple_list_item_1, parent, false);
				TextView textView = (TextView) rowView.findViewById(android.R.id.text1);
				textView.setText(itemInfo.item);
				return rowView;
			}
		}

		@Override
		public boolean isEnabled(int position) {
			return getGroupPosition(position).position != GroupPosition.HEADER_POSITION;
		}

		private GroupPosition getGroupPosition(int flatPosition) {
			int aboveCurrentGroup = 0;
			for (int groupId = 0; groupId < groups.length; groupId++) {
				if (flatPosition < aboveCurrentGroup + groups[groupId].getFlatLength()) {
					return new GroupPosition(groupId, flatPosition - aboveCurrentGroup - 1);
				}
				aboveCurrentGroup += groups[groupId].getFlatLength();
			}
			throw new IllegalArgumentException("position must be in [0, getCount())");
		}

		private static class GroupPosition {
			public static final int HEADER_POSITION = -1;

			public final int group;
			public final int position;

			private GroupPosition(int group, int position) {
				this.group = group;
				this.position = position;
			}
		}

		public static class Group {
			public final int headerId;
			public final String[] data;

			private Group(int headerId, String[] data) {
				this.headerId = headerId;
				this.data = data;
			}

			public int getFlatLength() {
				return data.length + 1;
			}
		}

		public static class ItemInfo {
			public final Integer headerId;
			public final String item;

			private ItemInfo(Integer headerId, String item) {
				if (headerId == null && item == null
						|| headerId != null && item != null) {
					throw new IllegalArgumentException("Exactly one of header and item must be null.");
				}
				this.headerId = headerId;
				this.item = item;
			}

			public boolean isHeader() {
				return headerId != null;
			}
		}
	}
}
