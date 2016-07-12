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
import android.app.Application;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import okhttp3.Dispatcher;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by jl on 08.07.16.
 */
public class Watchlater extends Application {
	// fields are not final to be somewhat accessible for testing to inject other values
	@SuppressWarnings({"FieldCanBeLocal", "CanBeFinal"})
	private static String          YOUTUBE_ENDPOINT                = "https://www.googleapis.com/youtube/v3/";
	@SuppressWarnings("CanBeFinal")
	private static ExecutorService OPTIONAL_RETROFIT_HTTP_EXECUTOR = null;

	YoutubeApi apiWithoutAccount;

	@Override
	public void onCreate() {
		super.onCreate();

		OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
		httpClient.addInterceptor(chain -> {
			Request originalRequest = chain.request();
			HttpUrl originalUrl = originalRequest.url();
			HttpUrl url = originalUrl.newBuilder().addQueryParameter("key", getResources().getString(R.string.youtubeApiKey)).build();

			Request request = originalRequest.newBuilder().url(url).build();
			return chain.proceed(request);
		});

		apiWithoutAccount = buildYoutubeApi(httpClient);
	}


	public YoutubeApi buildYoutubeApi(OkHttpClient.Builder httpClientBuilder) {
		if (BuildConfig.DEBUG) {
			HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
			loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
			httpClientBuilder.networkInterceptors().add(loggingInterceptor);

			if (OPTIONAL_RETROFIT_HTTP_EXECUTOR != null) {
				httpClientBuilder.dispatcher(new Dispatcher(OPTIONAL_RETROFIT_HTTP_EXECUTOR));
			}
		}

		Retrofit.Builder retrofitBuilder = new Retrofit.Builder()
				.baseUrl(YOUTUBE_ENDPOINT)
				.addConverterFactory(GsonConverterFactory.create())
				.client(httpClientBuilder.build());

		return retrofitBuilder.build().create(YoutubeApi.class);
	}
}
