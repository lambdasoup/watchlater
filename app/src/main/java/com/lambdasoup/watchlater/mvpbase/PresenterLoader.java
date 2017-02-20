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

package com.lambdasoup.watchlater.mvpbase;

import android.content.Context;
import android.content.Loader;
import android.util.Log;

/**
 * Created by jl on 13.07.16.
 */
public class PresenterLoader<T extends Presenter> extends Loader<T> {


	private final PresenterFactory<T> factory;
	private T presenter;

	public PresenterLoader(Context context, PresenterFactory<T> factory) {
		super(context);
		this.factory = factory;
	}

	@Override
	protected void onStartLoading() {
		// if we already own a presenter instance, simply deliver it.
		if (presenter != null) {
			deliverResult(presenter);
			return;
		}

		// Otherwise, force a load
		forceLoad();
	}

	@Override
	protected void onForceLoad() {
		// Create the Presenter using the Factory
		presenter = factory.create();

		// Deliver the result
		deliverResult(presenter);
	}

	@Override
	public void deliverResult(T data) {
		super.deliverResult(data);
	}

	@Override
	protected void onStopLoading() {
	}

	@Override
	protected void onReset() {
		if (presenter != null) {
			presenter.onDestroyed();
			presenter = null;
		}
	}

}



