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

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


public class SuccessFragment extends ChannelTitleAwareFragment {

	private static final String ARG_SUCCESS_RESULT = "com.lambdasoup.watchlater.ARG_SUCCESS_RESULT";


	private AddActivity.SuccessResult successResult;


	public static SuccessFragment newInstance(String channelTitle, AddActivity.WatchlaterResult result) {
		if (result == null) {
			throw new IllegalArgumentException("Supplied result must be non-null");
		}
		if (!result.isSuccess()) {
			throw new IllegalArgumentException("Supplied result " + result + " is not a success");
		}
		SuccessFragment fragment = new SuccessFragment();
		Bundle args = fragment.init(channelTitle);
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
			AddActivity.WatchlaterResult result = getArguments().getParcelable(ARG_SUCCESS_RESULT);
			if (result != null) {
				result.apply(success -> successResult = success, err -> {
				});
			}
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

}
