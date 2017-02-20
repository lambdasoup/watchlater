/*
 * Copyright (c) 2015 - 2016
 *
 *  Maximilian Hille <mh@lambdasoup.com>
 * Juliane Lehmann <jl@lambdasoup.com>
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

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import com.lambdasoup.watchlater.youtubeApi.Video;
import com.lambdasoup.watchlater.youtubeApi.YoutubeApi;

import retrofit2.Call;

/**
 * Created by jl on 27.06.16.
 */
public class VideoInfoLoader extends RetrofitLoader<Video>  {
	private static final String TAG = VideoInfoLoader.class.getSimpleName();
	private final String videoId;


	/**
	 * Stores away the application context associated with context.
	 * Since Loaders can be used across multiple activities it's dangerous to
	 * store the context directly; always use {@link #getContext()} to retrieve
	 * the Loader's Context, don't use the constructor argument directly.
	 * The Context returned by {@link #getContext} is safe to use across
	 * Activity instances.
	 *
	 * @param context used to retrieve the application context.
	 */
	public VideoInfoLoader(Context context, String videoId) {
		super(context);
		this.videoId = videoId;
	}


	@NonNull
	@Override
	protected YoutubeApi buildApi() {
		return ((Watchlater) getContext().getApplicationContext()).apiWithoutAccount;
	}

	@NonNull
	@Override
	protected Call<Video> getCall(@NonNull YoutubeApi api) {
		Log.d(TAG, "getCall: videoId " + videoId);
		return api.getVideoInfo(videoId);
	}


}
