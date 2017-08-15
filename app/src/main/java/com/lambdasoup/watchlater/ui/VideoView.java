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

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.lambdasoup.watchlater.R;
import com.lambdasoup.watchlater.data.YoutubeRepository;
import com.lambdasoup.watchlater.util.Util;
import com.lambdasoup.watchlater.viewmodel.AddViewModel;
import com.squareup.picasso.Picasso;

public class VideoView extends FrameLayout {

	private final View content;
	private final View progress;
	private final View error;

	private final int animationDuration;

	public VideoView(Context context, AttributeSet attrs) {
		super(context, attrs);

		LayoutInflater.from(context).inflate(R.layout.view_video, this);
		content = findViewById(R.id.video_content);
		progress = findViewById(R.id.video_progress);
		error = findViewById(R.id.video_error);

		animationDuration = getResources().getInteger(android.R.integer.config_shortAnimTime);
	}

	public void setVideoInfo(AddViewModel.VideoInfo info) {
		switch (info.state) {
			case PROGRESS:
				showProgress();
				break;

			case LOADED:
				showData(info.data);
				break;

			case ERROR:
				showError(info.error);
				break;
		}
	}

	private void showData(YoutubeRepository.Videos.Item item) {
		show(content);

		TextView title = content.findViewById(R.id.title);
		title.setText(item.snippet.title);

		TextView description = content.findViewById(R.id.description);
		description.setText(item.snippet.description);

		TextView duration = content.findViewById(R.id.duration);
		String formatted = Util.formatDuration(item.contentDetails.duration);
		duration.setText(formatted);

		ImageView thumbnailView = findViewById(R.id.thumbnail);
		Picasso.with(getContext())
				.load(item.snippet.thumbnails.medium.url)
				.into(thumbnailView);
	}

	private void showError(YoutubeRepository.ErrorType errorType) {
		show(error);

		TextView reason = error.findViewById(R.id.reason);
		switch (errorType) {
			case NETWORK:
				reason.setText(R.string.error_network);
				break;
			case VIDEO_NOT_FOUND:
				reason.setText(R.string.error_video_not_found);
				break;
			default:
				reason.setText(getResources().getString(R.string.could_not_load, errorType.toString()));
		}
	}

	private void showProgress() {
		show(progress);
	}

	private void show(View view) {
		View current = visibleChild();

		if (view == current) {
			return;
		}

		view.setAlpha(0f);
		view.setVisibility(View.VISIBLE);
		view.animate()
				.alpha(1f)
				.setDuration(animationDuration)
				.setListener(null);

		current.animate()
				.alpha(0f)
				.setDuration(animationDuration)
				.setListener(new AnimatorListenerAdapter() {
					@Override
					public void onAnimationEnd(Animator animation) {
						current.setVisibility(View.INVISIBLE);
					}
				});
	}

	private View visibleChild() {
		for (View view : new View[]{content, progress, error}) {
			if (view.getVisibility() == View.VISIBLE) {
				return view;
			}
		}
		throw new IllegalStateException("no visible children");
	}

}
