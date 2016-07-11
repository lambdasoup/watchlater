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

import android.accounts.Account;
import android.content.Context;
import android.content.Loader;
import android.os.Build;
import android.support.annotation.NonNull;
import android.util.Log;

import java.io.IOException;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by jl on 27.06.16.
 */
public class PlaylistItemLoader extends ActivityAwareRetrofitLoader<YoutubeApi.PlaylistItemResponse> {
	private static final String TAG = PlaylistItemLoader.class.getSimpleName();
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
	public PlaylistItemLoader(Context context, String videoId, Account account) {
		super(context, account);
		Log.d(TAG, "PlaylistItemLoader() called with: " + "context = [" + context + "], videoId = [" + videoId + "], account = [" + account + "]");
		this.videoId = videoId;
	}


	@NonNull
	@Override
	protected Call<YoutubeApi.PlaylistItemResponse> getCall(@NonNull YoutubeApi api) {
		return api.getPlaylistItemInWl(videoId);
	}


}
