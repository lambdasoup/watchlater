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

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.StringRes;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Locale;


public class ErrorFragment extends ChannelTitleAwareFragment {
	private static final String ARG_ERROR_RESULT  = "com.lambdasoup.watchlater.ARG_ERROR_RESULT";

	private AddActivity.ErrorResult errorResult;

	private OnFragmentInteractionListener mListener;


	public static ErrorFragment newInstance(String channelTitle, AddActivity.WatchlaterResult result) {
		if (result == null) {
			throw new IllegalArgumentException("Supplied result must be non-null");
		}
		if (result.isSuccess()) {
			throw new IllegalArgumentException("Supplied result " + result + " is not an error");
		}
		ErrorFragment fragment = new ErrorFragment();
		Bundle args = fragment.init(channelTitle);
		args.putParcelable(ARG_ERROR_RESULT, result);
		fragment.setArguments(args);
		return fragment;
	}

	public ErrorFragment() {
		// Required empty public constructor
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (getArguments() != null) {
			((AddActivity.WatchlaterResult) getArguments().getParcelable(ARG_ERROR_RESULT)).apply(success -> {
			}, err -> errorResult = err);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		View errorView = inflater.inflate(R.layout.fragment_error, container, false);
		TextView errorMsg = (TextView) errorView.findViewById(R.id.error_msg);
		errorMsg.setText(withChannelTitle(errorResult.msgId));

		for (AddActivity.ErrorResult.MoreErrorView errorButton : AddActivity.ErrorResult.MoreErrorView.values()) {
			errorView.findViewById(errorButton.buttonId).setVisibility(errorResult.additionalViews.contains(errorButton) ? View.VISIBLE : View.GONE);
		}

		errorView.findViewById(R.id.activetext_help_no_channel).setOnClickListener(v -> {
			if (mListener != null) {
				mListener.onShowHelp();
			}
		});
		errorView.findViewById(R.id.button_retry).setOnClickListener(v -> {
			if (mListener != null) {
				mListener.onRetry();
			}
		});
		return errorView;
	}


	@Override
	public void onAttach(Activity context) {
		super.onAttach(context);
		if (context instanceof OnFragmentInteractionListener) {
			mListener = (OnFragmentInteractionListener) context;
		} else {
			throw new RuntimeException(context.toString()
					+ " must implement OnFragmentInteractionListener");
		}
	}


	@Override
	public void onDetach() {
		super.onDetach();
		mListener = null;
	}


	public interface OnFragmentInteractionListener {
		void onShowHelp();

		void onRetry();
	}


}
