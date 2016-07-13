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

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.FragmentTransaction;
import android.app.LoaderManager;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

import java.io.IOException;

import static android.net.Uri.decode;
import static android.net.Uri.parse;

/**
 * Created by jl on 30.06.16.
 */
public class TestActivity extends Activity implements ErrorFragment.OnFragmentInteractionListener, WlStatusFragment.OnFragmentInteractionListener, VideoInfoFragment.OnFragmentInteractionListener {

	private static final String TAG                              = TestActivity.class.getSimpleName();
	private static final int    PERMISSIONS_REQUEST_GET_ACCOUNTS = 0;
	private              String channelTitle                     = "HARDCODED CHANNEL TITLE ACTIVITY";

	// all fragments use the activity's LoaderManager (otherwise loader gets reset on configuration change)
	public static final int    LOADER_VIDEO_INFO = 0;
	public static final int LOADER_WL_STATUS       = 1;

	private String videoId;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_add);


		try {
			videoId = getVideoId();
		} catch (WatchlaterException e) {
			Log.e(TAG, "could not get video id from intent uri ", e);
			onError(ErrorResult.fromErrorType(e.type));
		}

		FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
		if (getFragmentManager().findFragmentByTag(MainActivityMenuFragment.TAG) == null) {
			fragmentTransaction.add(MainActivityMenuFragment.newInstance(), MainActivityMenuFragment.TAG);
		}
		if (getFragmentManager().findFragmentByTag(VideoInfoFragment.TAG) == null) {
			fragmentTransaction.add(R.id.fragment_container, VideoInfoFragment.newInstance(videoId), VideoInfoFragment.TAG);
		}
		if (getFragmentManager().findFragmentByTag(WlStatusFragment.TAG) == null) {
			fragmentTransaction.add(R.id.fragment_container, WlStatusFragment.newInstance(videoId), WlStatusFragment.TAG);
		}
		fragmentTransaction.commit();

	}


	@Override
	public void onError(ErrorResult errorResult) {
		Log.d(TAG, "onError " + errorResult);

		getFragmentManager().beginTransaction().add(R.id.fragment_container, ErrorFragment.newInstance(channelTitle, errorResult)).commitAllowingStateLoss();
	}

	@TargetApi(23)
	@Override
	public void ensureAccountsPermission() {
		if (hasAccountsPermission()) {
			onAccountsPermissionGranted();
		} else {
			tryAcquireAccountsPermission();
		}
	}

	@TargetApi(23)
	private boolean hasAccountsPermission() {
		return checkSelfPermission(Manifest.permission.GET_ACCOUNTS) == PackageManager.PERMISSION_GRANTED;
	}

	@TargetApi(23)
	private void tryAcquireAccountsPermission() {
		requestPermissions(new String[]{Manifest.permission.GET_ACCOUNTS}, PERMISSIONS_REQUEST_GET_ACCOUNTS);
	}

	@TargetApi(23)
	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		switch (requestCode) {
			case PERMISSIONS_REQUEST_GET_ACCOUNTS: {
				if (grantResults.length > 0
						&& grantResults[0] == PackageManager.PERMISSION_GRANTED) {
					onAccountsPermissionGranted();
				} else {
					onError(ErrorResult.PERMISSION_REQUIRED_ACCOUNTS);
				}
				break;
			}
			default: {
				throw new RuntimeException("Unexpected permission request code: " + requestCode);
			}
		}
	}

	private void onAccountsPermissionGranted() {
		WlStatusFragment wlStatusFragment = (WlStatusFragment) getFragmentManager().findFragmentByTag(WlStatusFragment.TAG);
		if (wlStatusFragment != null) {
			wlStatusFragment.onEnsuredAccountsPermission();
		} else {
			throw new IllegalStateException("expected wlStatusFragment to be attached on return from permissions request get accounts");
		}
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


	private class WatchlaterException extends Exception {
		public final YoutubeApi.ErrorType type;

		@SuppressWarnings("SameParameterValue")
		public WatchlaterException(YoutubeApi.ErrorType type) {
			this.type = type;
		}
	}




}
