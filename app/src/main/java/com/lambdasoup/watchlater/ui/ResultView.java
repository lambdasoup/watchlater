/*
 *   Copyright (c) 2015 - 2017
 *
 *   Maximilian Hille <mh@lambdasoup.com>
 *   Juliane Lehmann <jl@lambdasoup.com>
 *
 *   This file is part of Watch Later.
 *
 *   Watch Later is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   Watch Later is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with Watch Later.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.lambdasoup.watchlater.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;

import com.lambdasoup.watchlater.R;
import com.lambdasoup.watchlater.viewmodel.AddViewModel;

public class ResultView extends androidx.appcompat.widget.AppCompatTextView
		implements Observer<AddViewModel.VideoAdd> {

	public ResultView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	public void onChanged(@Nullable AddViewModel.VideoAdd videoAdd) {
		if (videoAdd == null) {
			throw new IllegalArgumentException("add status should never be null");
		}

		switch (videoAdd.state) {
			case IDLE:
			case PROGRESS:
				clearResult();
				break;
			case SUCCESS:
				setSuccess();
				break;
			case ERROR:
				setError(videoAdd.errorType);
				break;
			case INTENT:
				setResult(true, getResources().getString(R.string.needs_youtube_permissions));
				break;
		}
	}

	private void setSuccess() {
		setResult(false, getResources().getString(R.string.success_added_video));
	}

	private void setError(AddViewModel.VideoAdd.ErrorType errorType) {
		String errorStr;
		switch (errorType) {
			case NO_ACCOUNT:
				errorStr = getResources().getString(R.string.error_no_account);
				break;
			case NO_PERMISSION:
				errorStr = getResources().getString(R.string.error_no_permission);
				break;
			case YOUTUBE_ALREADY_IN_PLAYLIST:
				errorStr = getResources().getString(R.string.error_already_in_playlist);
				break;
			default:
				errorStr = getResources().getString(R.string.error_general, errorType.name());
				break;
		}
		setResult(true, getResources().getString(R.string.could_not_add, errorStr));
	}

	private void setResult(boolean error, String msg) {
		setVisibility(View.VISIBLE);

		if (error) {
			setBackgroundColor(getResources().getColor(R.color.error_color));
		} else {
			setBackgroundColor(getResources().getColor(R.color.success_color));
		}

		setText(msg);
	}

	private void clearResult() {
		setVisibility(View.GONE);
	}
}
