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

import android.app.Fragment;
import android.app.LoaderManager;
import android.content.Loader;
import android.os.Bundle;
import android.util.Log;

/**
 * Created by jl on 14.07.16.
 */
public abstract class BasePresenterFragment<P extends Presenter<V>, V> extends Fragment {

	private static final String TAG = BasePresenterFragment.class.getSimpleName();
	private static final int LOADER_ID = 101;

	// boolean flag to avoid delivering the result twice. Calling initLoader in onActivityCreated makes
	// onLoadFinished will be called twice during configuration change.
	// TODO: check if necessary with framework loaders
	private boolean delivered = false;
	private Presenter<V> presenter;

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		// LoaderCallbacks as an object, so no hint regarding loader will be leak to the subclasses.
		// TODO: ?? above
		// TODO: check if activity loadermanager is necessary to use (and loader id needs to be injected)
		getLoaderManager().initLoader(LOADER_ID, null, new LoaderManager.LoaderCallbacks<P>() {
			@Override
			public final Loader<P> onCreateLoader(int id, Bundle args) {
				return new PresenterLoader<>(getActivity(), getPresenterFactory());
			}

			@Override
			public final void onLoadFinished(Loader<P> loader, P presenter) {
				if (!delivered) {
					BasePresenterFragment.this.presenter = presenter;
					delivered = true;
					onPresenterPrepared(presenter);
				}
			}

			@Override
			public final void onLoaderReset(Loader<P> loader) {
				BasePresenterFragment.this.presenter = null;
				onPresenterDestroyed();
			}
		});
	}

	@Override
	public void onResume() {
		super.onResume();
		presenter.onViewAttached(getPresenterView());
	}

	@Override
	public void onPause() {
		presenter.onViewDetached();
		super.onPause();
	}

	protected abstract String tag();

	protected abstract PresenterFactory<P> getPresenterFactory();

	protected abstract void onPresenterPrepared(P presenter);

	protected void onPresenterDestroyed() {
		// hook for subclasses
	}

	// Override in case of fragment not implementing Presenter<View> interface
	protected V getPresenterView() {
		return (V) this;
	}
}
