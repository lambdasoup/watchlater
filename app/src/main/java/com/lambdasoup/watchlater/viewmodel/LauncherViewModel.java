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

package com.lambdasoup.watchlater.viewmodel;

import androidx.lifecycle.LiveData;

import com.lambdasoup.watchlater.WatchLaterApplication;
import com.lambdasoup.watchlater.data.IntentResolverRepository;

public class LauncherViewModel extends WatchLaterViewModel {

	private final IntentResolverRepository intentResolverRepository;

	@SuppressWarnings("WeakerAccess")
	public LauncherViewModel(WatchLaterApplication application) {
		super(application);

		intentResolverRepository = application.getIntentResolverRepository();
	}

	public void update() {
		intentResolverRepository.update();
	}

	public LiveData<IntentResolverRepository.ResolverState> getResolverState() {
		return intentResolverRepository.getResolverState();
	}
}