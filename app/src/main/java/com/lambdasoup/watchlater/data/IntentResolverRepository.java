/*
 *   Copyright (c) 2015 - 2017
 *
 *   Maximilian Hille <mh@lambdasoup.com>
 *   Juliane Lehmann <jl@lambdasoup.com>
 *
 *   This file is part of Watch Later.
 *
 *   Watch Later is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   Watch Later is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with Watch Later.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.lambdasoup.watchlater.data;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;

public class IntentResolverRepository {

	private static final String ACTIVITY_YOUTUBE = "com.google.android.youtube.UrlActivity";
	private static final String EXAMPLE_URI      = "https://www.youtube.com/watch?v=tntOCGkgt98";

	private final PackageManager packageManager;

	private final MutableLiveData<ResolverState> resolverState = new MutableLiveData<>();

	public IntentResolverRepository(Context context) {
		packageManager = context.getPackageManager();
	}

	public LiveData<ResolverState> getResolverState() {
		return resolverState;
	}

	public void update() {
		Intent      resolveIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(EXAMPLE_URI));
		ResolveInfo resolveInfo   = packageManager.resolveActivity(resolveIntent, PackageManager.MATCH_DEFAULT_ONLY);
		switch (resolveInfo.activityInfo.name) {
			case ACTIVITY_YOUTUBE: {
				// youtube is set as default app to launch with, no chooser
				resolverState.setValue(ResolverState.YOUTUBE_ONLY);
				break;
			}

			default: {
				// some unknown app is set as the default app to launch with, without chooser.
				resolverState.setValue(ResolverState.OK);
			}
		}
	}

	public enum ResolverState {
		OK, YOUTUBE_ONLY
	}
}
