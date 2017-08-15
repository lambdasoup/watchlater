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

package com.lambdasoup.watchlater;

import android.app.Application;
import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.support.annotation.VisibleForTesting;

import com.lambdasoup.watchlater.data.AccountRepository;
import com.lambdasoup.watchlater.data.IntentResolverRepository;
import com.lambdasoup.watchlater.data.YoutubeRepository;
import com.lambdasoup.watchlater.util.VideoIdParser;
import com.lambdasoup.watchlater.viewmodel.WatchLaterViewModel;

import java.lang.reflect.InvocationTargetException;

public class WatchLaterApplication extends Application {

	private AccountRepository         accountRepository;
	private YoutubeRepository         youtubeRepository;
	private VideoIdParser             videoIdParser;
	private ViewModelProvider.Factory viewModelProviderFactory;
	private IntentResolverRepository  intentResolverRepository;

	@Override
	public void onCreate() {
		super.onCreate();

		accountRepository = new AccountRepository(this);
		youtubeRepository = new YoutubeRepository(this);
		intentResolverRepository = new IntentResolverRepository(this);
		videoIdParser = new VideoIdParser();

		viewModelProviderFactory = new WatchLaterFactory(this);
	}

	public AccountRepository getAccountRepository() {
		return accountRepository;
	}

	public YoutubeRepository getYoutubeRepository() {
		return youtubeRepository;
	}

	public VideoIdParser getVideoIdParser() {
		return videoIdParser;
	}

	public IntentResolverRepository getIntentResolverRepository() {
		return intentResolverRepository;
	}

	public ViewModelProvider.Factory getViewModelProviderFactory() {
		return viewModelProviderFactory;
	}

	@VisibleForTesting
	public void setViewModelProviderFactory(ViewModelProvider.Factory factory) {
		this.viewModelProviderFactory = factory;
	}

	private static class WatchLaterFactory extends ViewModelProviders.DefaultFactory {

		private final WatchLaterApplication application;

		private WatchLaterFactory(WatchLaterApplication application) {
			super(application);
			this.application = application;
		}

		@Override
		public <T extends ViewModel> T create(Class<T> modelClass) {
			if (WatchLaterViewModel.class.isAssignableFrom(modelClass)) {
				//noinspection TryWithIdenticalCatches
				try {
					return modelClass.getConstructor(WatchLaterApplication.class).newInstance(application);
				} catch (NoSuchMethodException e) {
					throw new RuntimeException("Cannot create an instance of " + modelClass, e);
				} catch (IllegalAccessException e) {
					throw new RuntimeException("Cannot create an instance of " + modelClass, e);
				} catch (InstantiationException e) {
					throw new RuntimeException("Cannot create an instance of " + modelClass, e);
				} catch (InvocationTargetException e) {
					throw new RuntimeException("Cannot create an instance of " + modelClass, e);
				}
			}

			return super.create(modelClass);
		}
	}
}
