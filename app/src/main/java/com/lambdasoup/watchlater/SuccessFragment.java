/*
 * Copyright (c) 2015.
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

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.StringRes;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Locale;


public class SuccessFragment extends Fragment {
	private static final String ARG_CHANNEL_TITLE  = "com.lambdasoup.watchlater.ARG_CHANNEL_TITLE";
	private static final String ARG_SUCCESS_RESULT = "com.lambdasoup.watchlater.ARG_SUCCESS_RESULT";

	private String                    channelTitle;
	private AddActivity.SuccessResult successResult;


	public static SuccessFragment newInstance(String channelTitle, AddActivity.WatchlaterResult result) {
		if (result == null) {
			throw new IllegalArgumentException("Supplied result must be non-null");
		}
		if (! result.isSuccess()) {
			throw new IllegalArgumentException("Supplied result " + result + " is not a success");
		}
		SuccessFragment fragment = new SuccessFragment();
		Bundle args = new Bundle();
		args.putString(ARG_CHANNEL_TITLE, channelTitle);
		args.putParcelable(ARG_SUCCESS_RESULT, result);
		fragment.setArguments(args);
		return fragment;
	}

	public SuccessFragment() {
		// Required empty public constructor
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (getArguments() != null) {
			channelTitle = getArguments().getString(ARG_CHANNEL_TITLE);
			((AddActivity.WatchlaterResult) getArguments().getParcelable(ARG_SUCCESS_RESULT)).apply(success -> successResult = success, err -> {});
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		View successView = inflater.inflate(R.layout.fragment_success, container, false);

		CharSequence msg = withChannelTitle(R.string.success_added_video);

		TextView successMsg = (TextView) successView.findViewById(R.id.success_msg);
		successMsg.setText(msg);

		TextView title = (TextView) successView.findViewById(R.id.success_title);
		title.setText(successResult.title);

		TextView description = (TextView) successView.findViewById(R.id.success_description);
		description.setText(successResult.description);
		return successView;
	}

	// TODO: DRY
	private CharSequence withChannelTitle(@StringRes int msgId) {
		return String.format(
				Locale.getDefault(),
				getResources().getString(msgId),
				channelTitle);
	}
}
