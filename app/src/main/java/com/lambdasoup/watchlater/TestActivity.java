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
import android.accounts.AccountManager;
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

import static android.net.Uri.decode;
import static android.net.Uri.parse;

/**
 * Created by jl on 30.06.16.
 */
public class TestActivity extends Activity implements ErrorFragment.OnFragmentInteractionListener {
	private static       String ACCOUNT_TYPE_GOOGLE = "com.google";
	private static final int    LOADER_VIDEO_INFO   = 0;
	private static final int    LOADER_IN_WL        = 1;
	private static final String ARG_VIDEO_ID        = "com.lambdasoup.watchlater.argument_videoId";
	private static final String TAG                 = TestActivity.class.getSimpleName();
	private              String channelTitle        = "HARDCODED CHANNEL TITLE";
	private FragmentCoordinator fragmentCoordinator;
	private String              videoId;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setDialogBehaviour();

		setContentView(R.layout.activity_add);

		fragmentCoordinator = new FragmentCoordinator();


		if (getFragmentManager().findFragmentByTag(MainActivityMenuFragment.TAG) == null) {
			getFragmentManager().beginTransaction().add(MainActivityMenuFragment.newInstance(), MainActivityMenuFragment.TAG).commit();
		}
		fragmentCoordinator.showProgress();

		try {
			videoId = getVideoId();
		} catch (WatchlaterException e) {
			Log.e(TAG, "could not get video id from intent uri ", e);
			onResult(WatchlaterResult.error(e.type));
		}

		Bundle loaderArgs = new Bundle();
		loaderArgs.putString(ARG_VIDEO_ID, videoId);
		getLoaderManager().initLoader(LOADER_VIDEO_INFO, loaderArgs, new VideoInfoLoaderCallbacks());

	}

	@Override
	protected void onPause() {
		super.onPause();
		detachFromLoaders();
	}

	@Override
	protected void onResume() {
		super.onResume();
		attachToLoaders();
	}

	private void attachToLoaders() {
		// TODO: for all loaders inheriting from ActivityAwareRetrofitLoader
		Loader<Object> loader = getLoaderManager().getLoader(LOADER_IN_WL);
		if (loader != null && loader instanceof ActivityAwareRetrofitLoader) {
			((ActivityAwareRetrofitLoader) loader).attachActivity(this);
		}
	}

	private void detachFromLoaders() {
		// TODO: for all loaders inheriting from ActivityAwareRetrofitLoader
		Loader<Object> loader = getLoaderManager().getLoader(LOADER_IN_WL);
		if (loader != null && loader instanceof ActivityAwareRetrofitLoader) {
			((ActivityAwareRetrofitLoader) loader).detachActivity();
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

	private class VideoInfoLoaderCallbacks implements LoaderManager.LoaderCallbacks<VideoInfoLoader.Result<YoutubeApi.Video>> {
		private final String TAG = VideoInfoLoaderCallbacks.class.getSimpleName();
		@Override
		public Loader<VideoInfoLoader.Result<YoutubeApi.Video>> onCreateLoader(int id, Bundle args) {
			Log.d(TAG, "onCreateLoader called");
			VideoInfoLoader loader = new VideoInfoLoader(TestActivity.this, args.getString(ARG_VIDEO_ID));
			loader.init();
			return loader;
		}

		@Override
		public void onLoadFinished(Loader<VideoInfoLoader.Result<YoutubeApi.Video>> loader, VideoInfoLoader.Result<YoutubeApi.Video> data) {
			Log.d(TAG, "onLoadFinished called");
			data.apply(videoResponse -> {
						if (videoResponse.isSuccessful()) {
							if (videoResponse.body().items.size() == 0) {
								onResult(WatchlaterResult.error(YoutubeApi.ErrorType.VIDEO_NOT_FOUND));
							} else {
								YoutubeApi.Video.VideoResource.Snippet snippet = videoResponse.body().items.get(0).snippet;
								Bundle args = new Bundle();
								args.putString(ARG_VIDEO_ID, videoId);
								getLoaderManager().initLoader(LOADER_IN_WL, args, new InWlLoaderCallbacks());
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
		public void onLoaderReset(Loader<VideoInfoLoader.Result<YoutubeApi.Video>> loader) {
			// TODO: ?
			Log.d(TAG, "onLoaderReset called");
		}
	}

	private class InWlLoaderCallbacks implements LoaderManager.LoaderCallbacks<PlaylistItemLoader.Result<YoutubeApi.PlaylistItemResponse>> {
		private final String TAG = InWlLoaderCallbacks.class.getSimpleName();
		@Override
		public Loader<PlaylistItemLoader.Result<YoutubeApi.PlaylistItemResponse>> onCreateLoader(int id, Bundle args) {
			Log.d(TAG, "onCreateLoader called");
			// TODO: proper account choice
			Account account = AccountManager.get(getApplicationContext()).getAccountsByType(ACCOUNT_TYPE_GOOGLE)[0];
			Log.d(TAG, "using account " + account);
			PlaylistItemLoader loader = new PlaylistItemLoader(TestActivity.this, args.getString(ARG_VIDEO_ID), account);
			loader.init();
			loader.attachActivity(TestActivity.this);
			return loader;
		}

		@Override
		public void onLoadFinished(Loader<PlaylistItemLoader.Result<YoutubeApi.PlaylistItemResponse>> loader, PlaylistItemLoader.Result<YoutubeApi.PlaylistItemResponse> data) {
			Log.d(TAG, "onLoadFinished called");
			data.apply(playlistItemResponse -> {
						if (playlistItemResponse.isSuccessful()) {

							if (playlistItemResponse.body().pageInfo.totalResults == 0) {
								Log.d(TAG, "video is not in WL");
							} else {
								Log.d(TAG, "video is in WL");
							}
						} else {
							// TODO: proper error mapping
							try {
								Log.d(TAG, "response unsuccessful: " + playlistItemResponse.errorBody().string());
							} catch (IOException e) {
								Log.d(TAG, "could not parse response error body ", e);
							}
							onResult(WatchlaterResult.error(YoutubeApi.ErrorType.OTHER));
						}

					},
					e -> onResult(WatchlaterResult.error(YoutubeApi.ErrorType.NETWORK)));
		}

		@Override
		public void onLoaderReset(Loader<PlaylistItemLoader.Result<YoutubeApi.PlaylistItemResponse>> loader) {
			// TODO: ?
			Log.d(TAG, "onLoaderReset called");
		}
	}
}
