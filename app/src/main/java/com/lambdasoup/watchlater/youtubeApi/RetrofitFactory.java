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

package com.lambdasoup.watchlater.youtubeApi;

import com.lambdasoup.watchlater.BuildConfig;

import java.util.concurrent.ExecutorService;

import okhttp3.Dispatcher;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by jl on 14.07.16.
 */
public class RetrofitFactory {
	@SuppressWarnings("FieldCanBeLocal")
	private static final String          YOUTUBE_ENDPOINT                = "https://www.googleapis.com/youtube/v3/";
	private static final ExecutorService OPTIONAL_RETROFIT_HTTP_EXECUTOR = null;

	private static final RetrofitFactory INSTANCE = new RetrofitFactory();

	public static RetrofitFactory getInstance() {
		return INSTANCE;
	}

	public ApiAndRetrofit buildYoutubeApi(OkHttpClient.Builder httpClientBuilder) {
		if (BuildConfig.DEBUG) {
			HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
			loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
			httpClientBuilder.networkInterceptors().add(loggingInterceptor);

			//noinspection ConstantConditions
			if (OPTIONAL_RETROFIT_HTTP_EXECUTOR != null) {
				httpClientBuilder.dispatcher(new Dispatcher(OPTIONAL_RETROFIT_HTTP_EXECUTOR));
			}
		}

		Retrofit retrofit = new Retrofit.Builder()
				.baseUrl(YOUTUBE_ENDPOINT)
				.addConverterFactory(GsonConverterFactory.create())
				.client(httpClientBuilder.build())
				.build();

		return new ApiAndRetrofit(retrofit, retrofit.create(YoutubeApi.class));
	}

	public static class ApiAndRetrofit {
		public final Retrofit retrofit;
		public final YoutubeApi youtubeApi;

		public ApiAndRetrofit(Retrofit retrofit, YoutubeApi youtubeApi) {
			this.retrofit = retrofit;
			this.youtubeApi = youtubeApi;
		}
	}
}
