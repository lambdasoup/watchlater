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

import android.app.Activity;
import android.app.Fragment;
import android.app.LoaderManager;
import android.content.Loader;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.StringRes;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import java.io.IOException;
import java.util.Locale;
import java.util.concurrent.ExecutorService;

import okhttp3.Dispatcher;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static android.net.Uri.decode;
import static android.net.Uri.parse;

/**
 * Created by jl on 30.06.16.
 */
public class TestActivity extends Activity implements LoaderManager.LoaderCallbacks<TestLoader.Result<YoutubeApi.Video>>, ErrorFragment.OnFragmentInteractionListener {
	private static final int             LOADER_VIDEO_INFO               = 0;
	private static final String          ARG_VIDEO_ID                    = "com.lambdasoup.watchlater.argument_videoId";
	private static final String          TAG                             = TestActivity.class.getSimpleName();
	@SuppressWarnings({"FieldCanBeLocal", "CanBeFinal"})
	private static       String          YOUTUBE_ENDPOINT                = "https://www.googleapis.com/youtube/v3/";
	@SuppressWarnings("CanBeFinal")
	private static       ExecutorService OPTIONAL_RETROFIT_HTTP_EXECUTOR = null;
	private Retrofit   retrofit;
	private YoutubeApi api;
	private String channelTitle = "HARDCODED CHANNEL TITLE";
	private FragmentCoordinator fragmentCoordinator;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setDialogBehaviour();

		setContentView(R.layout.activity_add);

		fragmentCoordinator = new FragmentCoordinator();

		setApiAdapter();


		if (getFragmentManager().findFragmentByTag(MainActivityMenuFragment.TAG) == null) {
			getFragmentManager().beginTransaction().add(MainActivityMenuFragment.newInstance(), MainActivityMenuFragment.TAG).commit();
		}
		fragmentCoordinator.showProgress();

		try {
			Bundle loaderArgs = new Bundle();
			loaderArgs.putString(ARG_VIDEO_ID, getVideoId());
			getLoaderManager().initLoader(LOADER_VIDEO_INFO, loaderArgs, this);
		} catch (WatchlaterException e) {
			onResult(WatchlaterResult.error(e.type));
		}
	}

	private void onResult(WatchlaterResult result) {
		result.apply(fragmentCoordinator::showSuccess, fragmentCoordinator::showError);
	}

	private void setDialogBehaviour() {
		requestWindowFeature(Window.FEATURE_ACTION_BAR);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND,
				WindowManager.LayoutParams.FLAG_DIM_BEHIND);
		WindowManager.LayoutParams params = getWindow().getAttributes();
		params.height = WindowManager.LayoutParams.WRAP_CONTENT;
		params.width = getResources().getDimensionPixelSize(R.dimen.dialog_width);
		params.alpha = 1.0f;
		params.dimAmount = 0.5f;
		getWindow().setAttributes(params);
	}

	private void setApiAdapter() {
		OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
//		httpClient.interceptors().add(chain -> {
//			Request request = chain.request().newBuilder().addHeader("Authorization", "Bearer " + token).build();
//			return chain.proceed(request);
//		});

		if (BuildConfig.DEBUG) {
			HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
			loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
			httpClient.networkInterceptors().add(loggingInterceptor);

			if (OPTIONAL_RETROFIT_HTTP_EXECUTOR != null) {
				httpClient.dispatcher(new Dispatcher(OPTIONAL_RETROFIT_HTTP_EXECUTOR));
			}
		}

		Retrofit.Builder retrofitBuilder = new Retrofit.Builder()
				.baseUrl(YOUTUBE_ENDPOINT)
				.addConverterFactory(GsonConverterFactory.create())
				.client(httpClient.build());

		retrofit = retrofitBuilder.build();
		api = retrofit.create(YoutubeApi.class);
	}

	private void showToast(CharSequence msg) {
		Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
	}

	private CharSequence withChannelTitle(@StringRes int msgId) {
		return String.format(
				Locale.getDefault(),
				getResources().getString(msgId),
				channelTitle);
	}

	private String getVideoId() throws WatchlaterException {
		return getVideoId(getIntent().getData());
	}

	private String getVideoId(Uri uri) throws WatchlaterException {
		// e.g. vnd.youtube:jqxENMKaeCU
		if (uri.isOpaque()) {
			return uri.getSchemeSpecificPart();
		}

		// e.g. https://www.youtube.com/watch?v=jqxENMKaeCU
		String videoId = uri.getQueryParameter("v");
		if (videoId != null) {
			return videoId;
		}

		// e.g.https://www.youtube.com/playlist?list=PLxLNk7y0uwqfXzUjcbVT3UuMjRd7pOv_U
		videoId = uri.getQueryParameter("list");
		if (videoId != null) {
			throw new WatchlaterException(YoutubeApi.ErrorType.NOT_A_VIDEO);
		}

		// e.g. http://www.youtube.com/attribution_link?u=/watch%3Fv%3DJ1zNbWJC5aw%26feature%3Dem-subs_digest
		if (!uri.getPathSegments().isEmpty() && "attribution_link".equals(uri.getPathSegments().get(0))) {
			String encodedUri = uri.getQueryParameter("u");
			if (encodedUri != null) {
				return getVideoId(parse(decode(encodedUri)));
			} else {
				throw new WatchlaterException(YoutubeApi.ErrorType.NOT_A_VIDEO);
			}
		}

		// e.g. http://www.youtube.com/v/OdT9z-JjtJk
		// http://www.youtube.com/embed/UkWd0azv3fQ
		// http://youtu.be/jqxENMKaeCU
		return uri.getLastPathSegment();
	}

	@Override
	public Loader<TestLoader.Result<YoutubeApi.Video>> onCreateLoader(int id, Bundle args) {
		Log.d(TAG, "onCreateLoader called");
		return new TestLoader(this, api, args.getString(ARG_VIDEO_ID));
	}

	@Override
	public void onLoadFinished(Loader<TestLoader.Result<YoutubeApi.Video>> loader, TestLoader.Result<YoutubeApi.Video> data) {
		Log.d(TAG, "onLoadFinished called");
		data.apply(videoResponse -> {
					if (videoResponse.isSuccessful()) {
						if (videoResponse.body().items.size() == 0) {
							onResult(WatchlaterResult.error(YoutubeApi.ErrorType.VIDEO_NOT_FOUND));
						} else {
							YoutubeApi.Video.VideoResource.Snippet snippet = videoResponse.body().items.get(0).snippet;
							onResult(WatchlaterResult.success(snippet.title, snippet.description));
						}
					} else {
						// TODO: proper error mapping
						try {
							Log.d(TAG, "response unsuccessful: " + videoResponse.errorBody().string());
						} catch (IOException e) {
							Log.d(TAG, "could not parse response error body ", e);
						}
						onResult(WatchlaterResult.error(YoutubeApi.ErrorType.OTHER));
					}

				},
				e -> onResult(WatchlaterResult.error(YoutubeApi.ErrorType.NETWORK)));
	}

	@Override
	public void onLoaderReset(Loader<TestLoader.Result<YoutubeApi.Video>> loader) {
		// TODO: ?
		Log.d(TAG, "onLoaderReset called");
	}

	@Override
	public void onShowHelp() {
		Log.d(TAG, "onShowHelp");
	}

	@Override
	public void onRetry() {
		Log.d(TAG, "onRetry");
	}

	private class FragmentCoordinator {

		public void showProgress() {
			showFragment(ProgressFragment.newInstance());
		}


		public void showError(ErrorResult errorResult) {
			if (isFinishing()) {
				showToast(withChannelTitle(errorResult.msgId));
				return;
			}
			showFragment(ErrorFragment.newInstance(channelTitle, errorResult));
		}

		public void showSuccess(SuccessResult successResult) {
			if (isFinishing()) {
				showToast(withChannelTitle(R.string.success_added_video));
				return;
			}
			showFragment(SuccessFragment.newInstance(channelTitle, successResult));
		}

		private void showFragment(Fragment fragment) {
			if (isFinishing()) {
				return;
			}
			Fragment currentFragment = getFragmentManager().findFragmentById(R.id.fragment_container);
			if (currentFragment != null && currentFragment.getClass().equals(fragment.getClass())) {
				return;
			}
			getFragmentManager()
					.beginTransaction()
					.replace(R.id.fragment_container, fragment)
					.setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out)
					.commitAllowingStateLoss();
		}
	}

	private class WatchlaterException extends Exception {
		public final YoutubeApi.ErrorType type;

		@SuppressWarnings("SameParameterValue")
		public WatchlaterException(YoutubeApi.ErrorType type) {
			this.type = type;
		}
	}
}
