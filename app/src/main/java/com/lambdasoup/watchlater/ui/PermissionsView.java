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

public class PermissionsView extends LinearLayout implements View.OnClickListener {

	@Nullable
	private Listener listener;

	public PermissionsView(Context context, AttributeSet attrs) {
		super(context, attrs);

		LayoutInflater.from(context).inflate(R.layout.view_permissions, this);
		findViewById(R.id.view_permissions_grant).setOnClickListener(this);
	}

	@Override
	public void onClick(View view) {
		if (listener == null) {
			return;
		}
		listener.onGrantPermissionsClicked();
	}

	public void setListener(@Nullable Listener listener) {
		this.listener = listener;
	}

	interface Listener {
		void onGrantPermissionsClicked();
	}
}
