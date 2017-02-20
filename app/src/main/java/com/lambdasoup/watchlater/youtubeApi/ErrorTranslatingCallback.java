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

import android.util.Log;

import java.io.IOException;
import java.lang.annotation.Annotation;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Converter;
import retrofit2.Response;
import retrofit2.Retrofit;

/**
 * Created by jl on 14.07.16.
 */


public abstract class ErrorTranslatingCallback<T> implements Callback<T> {
	private static final String TAG                                       = ErrorTranslatingCallback.class.getSimpleName();

	public static final String DAILY_LIMIT_EXCEEDED_UNREG                 = "dailyLimitExceededUnreg";
	public static final String VIDEO_ALREADY_IN_PLAYLIST                  = "videoAlreadyInPlaylist";
	public static final String PLAYLIST_CONTAINS_MAXIMUM_NUMBER_OF_VIDEOS = "playlistContainsMaximumNumberOfVideos";
	public static final String VIDEO_NOT_FOUND                            = "videoNotFound";

	private final Converter<ResponseBody, YouTubeError> youTubeErrorConverter;


	protected ErrorTranslatingCallback(Retrofit retrofit) {
		youTubeErrorConverter = retrofit.responseBodyConverter(YouTubeError.class, new Annotation[0]);
	}


	public ErrorType translateError(Response<T> errorResponse) {
		YouTubeError youtubeError;
		try {
			youtubeError = youTubeErrorConverter.convert(errorResponse.errorBody());
		} catch (IOException e) {
			Log.d(TAG, "Expected a youtube api error response, got instead: " + errorResponse.message());
			return ErrorType.OTHER;
		}

		if (youtubeError == null) {
			Log.d(TAG, "Expected a youtube api error response, got instead: " + errorResponse.message());
			return ErrorType.OTHER;
		}

		String errorDetail = "";
		if (youtubeError.error.errors != null
				&& youtubeError.error.errors.size() >= 1) {
			errorDetail = youtubeError.error.errors.get(0).reason;
		}

		switch (errorResponse.code()) {
			case 401:
				return ErrorType.INVALID_TOKEN;
			case 403:
				switch (errorDetail) {
					case DAILY_LIMIT_EXCEEDED_UNREG:
						return ErrorType.INVALID_TOKEN;
					case PLAYLIST_CONTAINS_MAXIMUM_NUMBER_OF_VIDEOS:
						return ErrorType.PLAYLIST_FULL;
				}
				return ErrorType.NEED_ACCESS;
			case 404:
				switch (errorDetail) {
					case VIDEO_NOT_FOUND:
						return ErrorType.VIDEO_NOT_FOUND;
				}
				return ErrorType.OTHER;
			case 409:
				switch (errorDetail) {
					case VIDEO_ALREADY_IN_PLAYLIST:
						return ErrorType.ALREADY_IN_PLAYLIST;
				}
				return ErrorType.OTHER;

			default:
				return ErrorType.OTHER;
		}
	}

	@Override
	public void onFailure(Call<T> call, Throwable t) {
		failure(ErrorType.NETWORK);
	}

	@Override
	public void onResponse(Call<T> call, Response<T> response) {
		if (response.isSuccessful()) {
			success(response.body());
		} else {
			failure(translateError(response));
		}
	}

	protected abstract void failure(ErrorType errorType);

	protected abstract void success(T result);
}
