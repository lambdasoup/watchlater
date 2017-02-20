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
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.lambdasoup.watchlater.model.ErrorResult;


public class ErrorFragment extends ChannelTitleAwareFragment {
    private static final String ARG_ERROR_RESULT = "com.lambdasoup.watchlater.ARG_ERROR_RESULT";

    private ErrorResult errorResult;

    private OnFragmentInteractionListener listener;


    public ErrorFragment() {
        // Required empty public constructor
    }

    public static ErrorFragment newInstance(@NonNull String channelTitle, @NonNull ErrorResult result) {
        ErrorFragment fragment = new ErrorFragment();
        Bundle args = fragment.init(channelTitle);
        args.putSerializable(ARG_ERROR_RESULT, result);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            errorResult = (ErrorResult) getArguments().getSerializable(ARG_ERROR_RESULT);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View errorView = inflater.inflate(R.layout.fragment_error, container, false);
        TextView errorMsg = (TextView) errorView.findViewById(R.id.error_msg);
        errorMsg.setText(withChannelTitle(errorResult.msgId));

        for (ErrorResult.ErrorExtraViewId errorButton : ErrorResult.ErrorExtraViewId.values()) {
            errorView.findViewById(errorButton.buttonId).setVisibility(errorResult.additionalViewIds.contains(errorButton) ? View.VISIBLE : View.GONE);
        }

        errorView.findViewById(R.id.activetext_help_no_channel).setOnClickListener(v -> listener.onShowHelp());
        errorView.findViewById(R.id.button_retry).setOnClickListener(v -> listener.onRetry());
        return errorView;
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
        void onShowHelp();

        void onRetry();
    }


}
