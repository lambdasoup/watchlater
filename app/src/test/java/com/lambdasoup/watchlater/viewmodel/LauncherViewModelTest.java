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

import android.arch.core.executor.testing.InstantTaskExecutorRule;
import android.arch.lifecycle.LiveData;

import com.lambdasoup.watchlater.WatchLaterApplication;
import com.lambdasoup.watchlater.data.IntentResolverRepository;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class LauncherViewModelTest {

	@Rule
	public TestRule rule = new InstantTaskExecutorRule();

	@Mock
	private WatchLaterApplication application;

	@Mock
	private IntentResolverRepository intentResolverRepository;

	private LauncherViewModel viewModel;

	@Before
	public void setup() {
		when(application.getIntentResolverRepository()).thenReturn(intentResolverRepository);

		viewModel = new LauncherViewModel(application);
	}

	@Test
	public void should_update_intentresolverrepository() {
		viewModel.update();
		verify(intentResolverRepository).update();
	}

	@SuppressWarnings("unchecked")
	@Test
	public void should_update_intentresolverstate() {
		LiveData<IntentResolverRepository.ResolverState> liveData = mock(LiveData.class);
		when(intentResolverRepository.getResolverState()).thenReturn(liveData);

		assertThat(viewModel.getResolverState()).isEqualTo(liveData);
	}

}
