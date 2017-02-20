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

package com.lambdasoup.watchlater.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.StringRes;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.lambdasoup.watchlater.R;

/**
 * Created by jl on 15.07.16.
 */
public class ProgressButton extends LinearLayout {
	public static final String TAG = "progressButton";
	private final ProgressBar progress;
	private final TextView textView;

	public ProgressButton(Context context) {
		this(context, null);
		Log.d(TAG, "1-arg version called");
	}

	public ProgressButton(Context context, AttributeSet attrs) {
		this(context, attrs, R.attr.progressButtonStyle);
		Log.d(TAG, "2-arg version called");
	}

	public ProgressButton(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		Log.d(TAG, "3-arg version called");
		Log.d(TAG, "ProgressButton() called with: " + "context = [" + context + "], attrs = [" + attrs + "], defStyleAttr = [" + defStyleAttr + "]");
		TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ProgressButton, defStyleAttr, R.style.ProgressButton);
		Log.d(TAG, "attrs: " + a);
		CharSequence buttonText = a.getText(R.styleable.ProgressButton_text);
		a.recycle();

		setOrientation(HORIZONTAL);
		setGravity(Gravity.CENTER_VERTICAL);

		LayoutInflater.from(context).inflate(R.layout.view_progress_button, this, true);

		progress = (ProgressBar) getChildAt(0);
		textView = (TextView) getChildAt(1);

		setLoadingState(false);
		setText(buttonText);
	}



	public void setLoadingState(boolean loadingState) {
		if (loadingState) {
			progress.setVisibility(VISIBLE);
			setEnabled(false);
		} else {
			// TODO: fade out?
			progress.setVisibility(GONE);
			setEnabled(true);
		}
	}

	public void setText(CharSequence value) {
		textView.setText(value);
	}

	public void setText(@StringRes int strRes) {
		textView.setText(strRes);
	}

}
