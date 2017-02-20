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
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.Loader;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.lambdasoup.watchlater.model.ErrorResult;
import com.lambdasoup.watchlater.model.VideoInfo;
import com.lambdasoup.watchlater.youtubeApi.ErrorType;
import com.lambdasoup.watchlater.youtubeApi.Video;
import com.lambdasoup.watchlater.youtubeApi.YoutubeApi;

import java.io.IOException;

import static com.lambdasoup.watchlater.TestActivity.LOADER_VIDEO_INFO;

/**
 * Created by jl on 12.07.16.
 */
public class VideoInfoFragment extends Fragment {
	public static final  String TAG               = VideoInfoFragment.class.getSimpleName();
	private static final String ARG_VIDEO_ID      = "com.lambdasoup.watchlater.ARG_VIDEO_ID";


	private String videoId;

	private OnFragmentInteractionListener listener;
	private TextView                      textTitle;
	private TextView                      textDescription;

	public VideoInfoFragment() {
		// required argument-less public constructor
	}

	public static VideoInfoFragment newInstance(@NonNull String videoId) {
		Log.d(TAG, "newInstance() called with: " + "videoId = [" + videoId + "]");
		VideoInfoFragment fragment = new VideoInfoFragment();
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
		if (videoId == null) {
			throw new IllegalArgumentException("ARG_VIDEO_ID is missing or null");
		}
	}


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		View videoInfoView = inflater.inflate(R.layout.fragment_video_info, container, false);
		textTitle = (TextView) videoInfoView.findViewById(R.id.text_title);
		textDescription = (TextView) videoInfoView.findViewById(R.id.text_description);
		Button buttonViewInYoutube = (Button) videoInfoView.findViewById(R.id.button_view_in_youtube);
		buttonViewInYoutube.setOnClickListener(v -> onViewInYoutube());
		return videoInfoView;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		startLoadVideoInfo();
	}


	private void startLoadVideoInfo() {
		Log.d(TAG, "startLoadVideoInfo() called, videoId is " + videoId);
		Bundle args = new Bundle();
		args.putString(ARG_VIDEO_ID, videoId);
		getActivity().getLoaderManager().initLoader(LOADER_VIDEO_INFO, args, new VideoInfoLoaderCallbacks());
	}

	private void onVideoInfo(VideoInfo videoInfo) {
		textTitle.setText(videoInfo.title);
		textDescription.setText(videoInfo.description);
	}

	private void onViewInYoutube() {
		try {
			// TODO: transport original uri better
			Intent intent = new Intent()
					.setData(getActivity().getIntent().getData())
					.setPackage("com.google.android.youtube");
			startActivity(intent);
		} catch (ActivityNotFoundException e) {
			Toast.makeText(getActivity(), R.string.error_youtube_player_missing, Toast.LENGTH_LONG).show();
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
	}

	private class VideoInfoLoaderCallbacks implements LoaderManager.LoaderCallbacks<VideoInfoLoader.Result<Video>> {
		private final String TAG = VideoInfoLoaderCallbacks.class.getSimpleName();

		@Override
		public Loader<VideoInfoLoader.Result<Video>> onCreateLoader(int id, Bundle args) {
			Log.d(TAG, "onCreateLoader() called with: " + "id = [" + id + "], args = [" + args + "]");
			VideoInfoLoader loader = new VideoInfoLoader(getContext(), args.getString(ARG_VIDEO_ID));
			loader.init();
			return loader;
		}

		@Override
		public void onLoadFinished(Loader<VideoInfoLoader.Result<Video>> loader, VideoInfoLoader.Result<Video> data) {
			Log.d(TAG, "onLoadFinished called");
			// TODO: make progress invisible
			data.apply(videoResponse -> {
						if (videoResponse.isSuccessful()) {
							if (videoResponse.body().items.size() == 0) {
								listener.onError(ErrorResult.fromErrorType(ErrorType.VIDEO_NOT_FOUND));
							} else {
								Video.VideoResource.Snippet snippet = videoResponse.body().items.get(0).snippet;
								onVideoInfo(new VideoInfo(snippet.title, snippet.description));
							}
						} else {
							// TODO: proper error mapping
							try {
								Log.d(TAG, "response unsuccessful: " + videoResponse.errorBody().string());
							} catch (IOException e) {
								Log.d(TAG, "could not parse response error body ", e);
							}
							listener.onError(ErrorResult.fromErrorType(ErrorType.OTHER));
						}

					},
					e -> listener.onError(ErrorResult.fromErrorType(ErrorType.NETWORK)));
		}

		@Override
		public void onLoaderReset(Loader<VideoInfoLoader.Result<Video>> loader) {
			// TODO: ?
			Log.d(TAG, "onLoaderReset called");
		}
	}
}
