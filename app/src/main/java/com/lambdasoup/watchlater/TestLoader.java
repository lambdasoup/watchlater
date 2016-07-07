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
import android.content.Loader;
import android.os.Build;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by jl on 27.06.16.
 */
public class TestLoader extends Loader<TestLoader.Result<YoutubeApi.Video>> implements Callback<YoutubeApi.Video> {
	private final YoutubeApi             api;
	private final String                 videoId;
	private       TestLoader.Result<YoutubeApi.Video>       data;
	private       Call<YoutubeApi.Video> call;


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
	public TestLoader(Context context, YoutubeApi api, String videoId) {
		super(context);
		// TODO: token is unnecessary for video details request
		this.api = api;
		this.videoId = videoId;
	}

	@Override
	protected void onStartLoading() {
		if (this.data != null) {
			deliverResult(this.data);
		} else {
			forceLoad();
		}
	}

	@Override
	protected void onStopLoading() {
		cancelLoad();
	}

	@Override
	protected boolean onCancelLoad() {
		// TODO: try cancel
		if (call == null) {
			return false;
		}
		call.cancel();
		return true;
		// TODO: onCancelListener?

		// TODO: release resources
	}


	@Override
	protected void onForceLoad() {
		cancelLoad();
		this.call = api.getVideoInfo(videoId);
		this.call.enqueue(this);
	}

	@Override
	protected void onReset() {
		onStopLoading();

		if (this.data != null) {
			this.data = null;
		}
	}

	@Override
	public void deliverResult(TestLoader.Result<YoutubeApi.Video> data) {
		if (isReset()) {
			// loader has been reset; ignore result and invalidate data
			return;
		}
		this.data = data;
		if (isStarted()) {
			super.deliverResult(data);
		}

	}


	@Override
	public void onResponse(Call<YoutubeApi.Video> call, Response<YoutubeApi.Video> response) {
		onLoadCompleted(Result.response(response));
	}

	@Override
	public void onFailure(Call<YoutubeApi.Video> call, Throwable t) {
		onLoadCompleted(Result.exception((IOException) t));
	}

	private void onLoadCompleted(Result<YoutubeApi.Video> result) {
		if (isAbandoned()) {
			return;
		} else {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
				commitContentChanged();
			}
			deliverResult(result);
		}

	}

	/**
	 * Result of a retrofit load - either a response, or an IOException.
	 *
	 * @param <T> Model type to be loaded
	 */
	public static class Result<T> {
		private final Response<T> response;
		private final IOException exception;

		private Result(Response<T> response, IOException exception) {
			if ((response == null) == (exception == null)) {
				throw new IllegalArgumentException("exactly one of response, exception must be null");
			}
			this.response = response;
			this.exception = exception;
		}

		static <T> Result<T> response(Response<T> response) {
			return new Result<>(response, null);
		}

		static <T> Result<T> exception(IOException exception) {
			return new Result<>(null, exception);
		}

		boolean isResponse() {
			return response != null;
		}

		void apply(VoidFunction<Response<T>> onResponse, VoidFunction<IOException> onException) {
			if (isResponse()) {
				onResponse.apply(response);
			} else {
				onException.apply(exception);
			}
		}
	}

	/**
	 * No guava around, so let's just define our own functional interface of type S -> void.
	 *
	 * @param <S>
	 */
	public interface VoidFunction<S> {
		void apply(S s);
	}
}
