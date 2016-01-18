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

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;

import java.util.Locale;

// TODO: comment & explain! Or solve with composition instead of inheritance
public class ChannelTitleAwareFragment extends Fragment {
	private static final String ARG_CHANNEL_TITLE  = "com.lambdasoup.watchlater.ARG_CHANNEL_TITLE";

	private String                    channelTitle;

	public static ChannelTitleAwareFragment newInstance(String channelTitle) {
		ChannelTitleAwareFragment fragment = new ChannelTitleAwareFragment();
		fragment.init(channelTitle);
		return fragment;
	}

	@SuppressWarnings("WeakerAccess")
	public ChannelTitleAwareFragment() {
		// public default constructor is necessary
	}

	Bundle init(@NonNull String channelTitle) {
		Bundle args = new Bundle();
		args.putString(ARG_CHANNEL_TITLE, channelTitle);
		setArguments(args);
		return args;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (getArguments() != null) {
			channelTitle = getArguments().getString(ARG_CHANNEL_TITLE);
		}
	}


	CharSequence withChannelTitle(@StringRes int msgId) {
		return String.format(
				Locale.getDefault(),
				getResources().getString(msgId),
				channelTitle);
	}
}
