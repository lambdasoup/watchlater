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
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import static android.net.Uri.parse;


public class LauncherActivity extends Activity {

	private static final Uri EXAMPLE_URI = parse("https://www.youtube.com/watch?v=jqxENMKaeCU");

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_launcher);
	}

	public void onExample(View v) {
		startActivity(new Intent(Intent.ACTION_VIEW, EXAMPLE_URI));
	}

}
