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
import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;

import okhttp3.OkHttpClient;

/**
 * Created by jl on 11.07.16.
 */
public abstract class ActivityAwareRetrofitLoader<T> extends RetrofitLoader<T> {
	private final Account account;
	protected Activity activity;
	protected GoogleAccountAuthenticator authenticator;

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
	public ActivityAwareRetrofitLoader(Context context, Account account) {
		super(context);
		this.account = account;
	}

	@NonNull
	@Override
	protected YoutubeApi buildApi() {
		authenticator = new GoogleAccountAuthenticator(account);
		authenticator.init(getContext());
		OkHttpClient.Builder httpClientBuilder = new OkHttpClient.Builder()
				.authenticator(authenticator)
				.addInterceptor(authenticator);
		return ((Watchlater) getContext().getApplicationContext()).buildYoutubeApi(httpClientBuilder);
	}

	public void attachActivity(@NonNull Activity activity) {
		authenticator.attachActivity(activity);
	}

	public void detachActivity() {
		authenticator.detachActivity();
	}
}
