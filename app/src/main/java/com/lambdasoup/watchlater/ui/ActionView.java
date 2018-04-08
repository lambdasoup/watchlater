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
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import com.lambdasoup.watchlater.R;
import com.lambdasoup.watchlater.viewmodel.AddViewModel;

public class ActionView extends LinearLayout implements View.OnClickListener {

	@Nullable
	private ActionListener listener;

	public ActionView(Context context, AttributeSet attrs) {
		super(context, attrs);

		LayoutInflater.from(context).inflate(R.layout.view_action, this);
		findViewById(R.id.action_watchnow).setOnClickListener(this);
		findViewById(R.id.action_watchlater).setOnClickListener(this);
	}

	void setListener(@Nullable ActionListener listener) {
		this.listener = listener;
	}

	void setState(AddViewModel.VideoAdd.State state) {
		switch (state) {
			case PROGRESS:
				setProgress(true);
				break;

			case IDLE:
			case ERROR:
			case SUCCESS:
			case INTENT:
				setProgress(false);
				break;
		}
	}

	private void setProgress(boolean progress) {
		findViewById(R.id.action_watchlater).setVisibility(progress ? INVISIBLE : VISIBLE);
		findViewById(R.id.action_progress).setVisibility(progress ? VISIBLE : INVISIBLE);
	}

	@Override
	public void onClick(View view) {
		if (listener == null) {
			return;
		}

		switch (view.getId()) {
			case R.id.action_watchlater:
				listener.onWatchLaterClicked();
				break;

			case R.id.action_watchnow:
				listener.onWatchNowClicked();
				break;
		}
	}

	interface ActionListener {
		void onWatchNowClicked();

		void onWatchLaterClicked();
	}
}
