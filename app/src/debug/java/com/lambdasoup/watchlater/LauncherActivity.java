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
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

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

		populateListView(VIDEO_EXAMPLE_URIS, R.id.list_video_example_links);
		populateListView(PLAYLIST_EXAMPLE_URIS, R.id.list_playlist_example_links);
		populateListView(NONEXAMPLE_URIS, R.id.list_nonexample_links);
	}

	private void populateListView(final String[] links, final int listViewId) {
		ArrayAdapter<String> adapter = new ArrayAdapter<>(
				this,
				R.layout.list_item_launcher,
				links);

		ListView listView = (ListView) findViewById(listViewId);
		listView.setAdapter(adapter);

		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				startActivity(new Intent(Intent.ACTION_VIEW, parse(links[position])));
			}
		});
	}



}
