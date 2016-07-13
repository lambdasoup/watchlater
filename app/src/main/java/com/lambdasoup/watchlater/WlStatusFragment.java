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
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.IOException;

import static com.lambdasoup.watchlater.TestActivity.LOADER_WL_STATUS;

/**
 * Created by jl on 11.07.16.
 */
public class WlStatusFragment extends Fragment {
	public static final String TAG                 = WlStatusFragment.class.getSimpleName();
	private static final String ARG_VIDEO_ID        = "com.lambdasoup.watchlater.ARG_VIDEO_ID";

	private static final String KEY_CHANNEL_TITLE   = "com.lambdasoup.watchlater.KEY_CHANNEL_TITLE";
	private static final String KEY_VIDEO_ID        = "com.lambdasoup.watchlater.KEY_VIDEO_ID";
	private static final String KEY_ACCOUNT         = "com.lambdasoup.watchlater.KEY_ACCOUNT";

	private static       String ACCOUNT_TYPE_GOOGLE = "com.google";

	private String  channelTitle;
	private Account account;
	private String  videoId;

	private OnFragmentInteractionListener listener;
	private Button buttonAdd;
	private TextView textInChannelWl;
	private ProgressBar progress;

	public WlStatusFragment() {
		// required argument-less public constructor
	}

	public static WlStatusFragment newInstance(@NonNull String videoId) {
		Log.d(TAG, "newInstance() called with: " + "videoId = [" + videoId + "]");
		WlStatusFragment fragment = new WlStatusFragment();
		Bundle args = new Bundle();
		args.putString(ARG_VIDEO_ID, videoId);
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (savedInstanceState != null) {
			// TODO if channelTitle then playlistId
			channelTitle = savedInstanceState.getString(KEY_CHANNEL_TITLE);
			videoId = savedInstanceState.getString(KEY_VIDEO_ID);
			account = savedInstanceState.getParcelable(KEY_ACCOUNT);
		} else if (getArguments() != null) {
			videoId = getArguments().getString(ARG_VIDEO_ID);
		}



	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putString(KEY_CHANNEL_TITLE, channelTitle);
		outState.putString(KEY_VIDEO_ID, videoId);
		outState.putParcelable(KEY_ACCOUNT, account);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		Log.d(TAG, "onCreateView() called with: " + "inflater = [" + inflater + "], container = [" + container + "], savedInstanceState = [" + savedInstanceState + "]");
		View wlStatusView = inflater.inflate(R.layout.fragment_wl_status, container, false);
		buttonAdd = (Button) wlStatusView.findViewById(R.id.button_add);
		textInChannelWl = (TextView) wlStatusView.findViewById(R.id.text_in_channel_wl);
		progress = (ProgressBar) wlStatusView.findViewById(R.id.progress);
		buttonAdd.setVisibility(View.GONE);
		buttonAdd.setOnClickListener(v -> onAddClicked());
		textInChannelWl.setVisibility(View.GONE);
		return wlStatusView;
	}


	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		Log.d(TAG, "onActivityCreated() called with: " + "savedInstanceState = [" + savedInstanceState + "]");
		super.onActivityCreated(savedInstanceState);
		if (supportsRuntimePermissions()) {
			listener.ensureAccountsPermission();
		} else {
			ensureAccount();
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		detachFromLoaders();
	}

	@Override
	public void onResume() {
		super.onResume();
		attachToLoaders();
	}

	private void attachToLoaders() {
		// TODO: for all loaders inheriting from ActivityAwareRetrofitLoader
		Loader<Object> loader = getLoaderManager().getLoader(LOADER_WL_STATUS);
		if (loader != null && loader instanceof ActivityAwareRetrofitLoader) {
			((ActivityAwareRetrofitLoader) loader).attachActivity(getActivity());
		}
	}

	private void detachFromLoaders() {
		// TODO: for all loaders inheriting from ActivityAwareRetrofitLoader
		Loader<Object> loader = getLoaderManager().getLoader(LOADER_WL_STATUS);
		if (loader != null && loader instanceof ActivityAwareRetrofitLoader) {
			((ActivityAwareRetrofitLoader) loader).detachActivity();
		}
	}

	private void onAddClicked() {
		Log.d(TAG, "onAddClicked");
	}

	public void onEnsuredAccountsPermission() {
		ensureAccount();
	}

	private void ensureAccount() {
		if (account == null) {
			Account[] accounts = AccountManager.get(getActivity()).getAccountsByType(ACCOUNT_TYPE_GOOGLE);

			if (accounts.length == 0) {
				listener.onError(ErrorResult.NO_ACCOUNT);
				return;
			} else if (accounts.length != 1) {
				Log.d(TAG, "multiple accounts not yet implemented, choosing first one");
				//			onMultipleAccounts();
				//			return;
			}
			account = accounts[0];
		}
		startLoadWlStatus();
	}

	private void startLoadWlStatus() {
		channelTitle = "HARDCODED_CHANNEL_TITLE";
		Bundle args = new Bundle();
		args.putString(ARG_VIDEO_ID, videoId);
		getActivity().getLoaderManager().initLoader(LOADER_WL_STATUS, args, new InWlLoaderCallbacks());
	}

	private void onStatusRefreshed(boolean inWl) {
		if (inWl) {
			progress.setVisibility(View.GONE);
			buttonAdd.setVisibility(View.GONE);
			textInChannelWl.setText(getString(R.string.text_in_channel_wl, channelTitle));
			textInChannelWl.setVisibility(View.VISIBLE);
		} else {
			progress.setVisibility(View.GONE);
			textInChannelWl.setVisibility(View.GONE);
			buttonAdd.setText(getString(R.string.button_add_to_channel_wl, channelTitle));
			buttonAdd.setVisibility(View.VISIBLE);
		}
	}


	// need to keep this for compatibility with API level < 23
	@SuppressWarnings("deprecation")
	@Override
	public void onAttach(Activity context) {
		super.onAttach(context);
		if (context instanceof OnFragmentInteractionListener) {
			listener = (OnFragmentInteractionListener) context;
		} else {
			throw new RuntimeException(context.toString()
					+ " must implement OnFragmentInteractionListener");
		}
	}


	@Override
	public void onDetach() {
		super.onDetach();
		listener = null;
	}


	public interface OnFragmentInteractionListener {
		void onError(ErrorResult errorResult);

		void ensureAccountsPermission();
	}

	private boolean supportsRuntimePermissions() {
		return (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1);
	}

	private class InWlLoaderCallbacks implements LoaderManager.LoaderCallbacks<PlaylistItemLoader.Result<YoutubeApi.PlaylistItemResponse>> {
		private final String TAG = InWlLoaderCallbacks.class.getSimpleName();

		@Override
		public Loader<PlaylistItemLoader.Result<YoutubeApi.PlaylistItemResponse>> onCreateLoader(int id, Bundle args) {
			Log.d(TAG, "onCreateLoader called");
			Log.d(TAG, "using account " + account);
			PlaylistItemLoader loader = new PlaylistItemLoader(getContext(), args.getString(ARG_VIDEO_ID), account);
			loader.init();
			loader.attachActivity(getActivity());
			return loader;
		}

		@Override
		public void onLoadFinished(Loader<PlaylistItemLoader.Result<YoutubeApi.PlaylistItemResponse>> loader, PlaylistItemLoader.Result<YoutubeApi.PlaylistItemResponse> data) {
			Log.d(TAG, "onLoadFinished called");
			progress.setVisibility(View.GONE);
			data.apply(playlistItemResponse -> {
						if (playlistItemResponse.isSuccessful()) {
							onStatusRefreshed(playlistItemResponse.body().pageInfo.totalResults != 0);
						} else {
							// TODO: proper error mapping
							try {
								Log.d(TAG, "response unsuccessful: " + playlistItemResponse.errorBody().string());
							} catch (IOException e) {
								Log.d(TAG, "could not parse response error body ", e);
							}
							listener.onError(ErrorResult.fromErrorType(YoutubeApi.ErrorType.OTHER));
						}

					},
					e -> listener.onError(ErrorResult.fromErrorType(YoutubeApi.ErrorType.NETWORK)));
		}

		@Override
		public void onLoaderReset(Loader<PlaylistItemLoader.Result<YoutubeApi.PlaylistItemResponse>> loader) {
			// TODO: ?
			Log.d(TAG, "onLoaderReset called");
		}
	}

}
