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

import android.app.Instrumentation;

import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.test.InstrumentationRegistry;

import com.lambdasoup.watchlater.WatchLaterApplication;

abstract class WatchLaterActivityTest {

	void setViewModel(ViewModel mockViewModel) {
		Instrumentation       instrumentation = InstrumentationRegistry.getInstrumentation();
		WatchLaterApplication app             = (WatchLaterApplication) instrumentation.getTargetContext().getApplicationContext();
		app.setViewModelProviderFactory(new ViewModelProvider.Factory() {
			@SuppressWarnings("unchecked")
			@Override
			public <T extends ViewModel> T create(Class<T> modelClass) {
				return (T) mockViewModel;
			}
		});
	}

}
