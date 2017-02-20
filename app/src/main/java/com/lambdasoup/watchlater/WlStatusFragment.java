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
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.lambdasoup.watchlater.model.ErrorResult;
import com.lambdasoup.watchlater.model.InWlStatus;
import com.lambdasoup.watchlater.mvpbase.BasePresenterFragment;
import com.lambdasoup.watchlater.mvpbase.PresenterFactory;
import com.lambdasoup.watchlater.widget.ProgressButton;
import com.lambdasoup.watchlater.wlstatus.WlStatusPresenter;
import com.lambdasoup.watchlater.wlstatus.WlStatusView;

/**
 * Created by jl on 11.07.16.
 */
public class WlStatusFragment extends BasePresenterFragment<WlStatusPresenter, WlStatusView> implements WlStatusView {
	public static final  String TAG                              = WlStatusFragment.class.getSimpleName();
	private static final String ARG_VIDEO_ID                     = "com.lambdasoup.watchlater.ARG_VIDEO_ID";
	private static final int    PERMISSIONS_REQUEST_GET_ACCOUNTS = 101;

	private WlStatusPresenter             presenter;

	private String videoId;

	private ProgressButton buttonAdd;
	private TextView       textInChannelWl;
	private ProgressBar    progress;

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

		if (getArguments() != null) {
			videoId = getArguments().getString(ARG_VIDEO_ID);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		Log.d(TAG, "onCreateView() called with: " + "inflater = [" + inflater + "], container = [" + container + "], savedInstanceState = [" + savedInstanceState + "]");
		View wlStatusView = inflater.inflate(R.layout.fragment_wl_status, container, false);
		buttonAdd = (ProgressButton) wlStatusView.findViewById(R.id.button_add);
		textInChannelWl = (TextView) wlStatusView.findViewById(R.id.text_in_channel_wl);
		progress = (ProgressBar) wlStatusView.findViewById(R.id.progress);
		buttonAdd.setVisibility(View.GONE);
		textInChannelWl.setVisibility(View.GONE);
		progress.setVisibility(View.GONE);
		return wlStatusView;
	}


	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		Log.d(TAG, "onActivityCreated() called with: " + "savedInstanceState = [" + savedInstanceState + "]");
		super.onActivityCreated(savedInstanceState);
	}

	@Override
	public void onResume() {
		super.onResume();
		// presenter is initialized now
		Log.d(TAG, "presenter in onResume: " + presenter);
	}

	@Override
	public void onPause() {
		super.onPause();
	}

	@Override
	protected String tag() {
		return "wlStatus";
	}

	@Override
	protected PresenterFactory<WlStatusPresenter> getPresenterFactory() {
		return () -> new WlStatusPresenter(videoId, getActivity());
	}

	@Override
	protected void onPresenterPrepared(WlStatusPresenter presenter) {
		this.presenter = presenter;
		buttonAdd.setOnClickListener(v -> presenter.onAddRequested());
	}

	@Override
	protected void onPresenterDestroyed() {
		this.presenter = null;
	}

	@TargetApi(23)
	@Override
	public void ensureAccountsPermission() {
		if (hasAccountsPermission()) {
			presenter.onEnsuredAccountsPermission();
		} else {
			tryAcquireAccountsPermission();
		}
	}

	@TargetApi(23)
	private boolean hasAccountsPermission() {
		return getContext().checkSelfPermission(Manifest.permission.GET_ACCOUNTS) == PackageManager.PERMISSION_GRANTED;
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
					presenter.onEnsuredAccountsPermission();
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


	@Override
	public void onError(ErrorResult errorResult) {
		// TODO: child fragment?
	}

	@Override
	public void onWlStatusUpdated(InWlStatus inWlStatus) {
		// TODO: transitions!
		switch (inWlStatus) {
			case UNKNOWN: {
				progress.setVisibility(View.VISIBLE);
				buttonAdd.setVisibility(View.GONE);
				textInChannelWl.setVisibility(View.GONE);
				break;
			}
			case NO: {
				progress.setVisibility(View.GONE);
				buttonAdd.setText(getString(R.string.button_add_to_channel_wl, "HARDCODED CHANNEL TITLE"));
				buttonAdd.setLoadingState(false);
				buttonAdd.setVisibility(View.VISIBLE);
				textInChannelWl.setVisibility(View.GONE);
				break;
			}
			case ADDING: {
				progress.setVisibility(View.GONE);
				buttonAdd.setText(getString(R.string.button_add_to_channel_wl, "HARDCODED CHANNEL TITLE"));
				buttonAdd.setLoadingState(true);
				buttonAdd.setVisibility(View.VISIBLE);
				textInChannelWl.setVisibility(View.GONE);
				break;
			}
			case YES: {
				progress.setVisibility(View.GONE);
				buttonAdd.setVisibility(View.GONE);
				textInChannelWl.setText(getString(R.string.text_in_channel_wl, "HARDCODED CHANNEL TITLE"));
				textInChannelWl.setVisibility(View.VISIBLE);
			}
		}
	}





}
