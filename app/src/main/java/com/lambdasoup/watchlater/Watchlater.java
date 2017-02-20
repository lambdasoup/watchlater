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

import android.app.Application;
import android.app.LoaderManager;

import com.lambdasoup.watchlater.youtubeApi.RetrofitFactory;
import com.lambdasoup.watchlater.youtubeApi.YoutubeApi;

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


	public YoutubeApi apiWithoutAccount;

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

		apiWithoutAccount = RetrofitFactory.getInstance().buildYoutubeApi(httpClient).youtubeApi;
	}



}
