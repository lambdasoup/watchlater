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

import android.app.Activity;
import android.app.Instrumentation;
import android.arch.lifecycle.MutableLiveData;
import android.content.Intent;
import android.net.Uri;
import android.support.test.espresso.intent.rule.IntentsTestRule;
import android.support.test.filters.LargeTest;
import android.support.test.runner.AndroidJUnit4;

import com.lambdasoup.watchlater.R;
import com.lambdasoup.watchlater.data.IntentResolverRepository;
import com.lambdasoup.watchlater.viewmodel.LauncherViewModel;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.intent.Intents.intended;
import static android.support.test.espresso.intent.Intents.intending;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasAction;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasData;
import static android.support.test.espresso.intent.matcher.IntentMatchers.toPackage;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class LauncherActivityTest extends WatchLaterActivityTest {

	private final LauncherViewModel                 mockViewModel = mock(LauncherViewModel.class);
	@Rule
	public        IntentsTestRule<LauncherActivity> rule          = new IntentsTestRule<>(LauncherActivity.class, false, false);
	private MutableLiveData<IntentResolverRepository.ResolverState> resolverStateLiveData;

	@Before
	public void setup() {
		resolverStateLiveData = new MutableLiveData<>();
		reset(mockViewModel);
		when(mockViewModel.getResolverState()).thenReturn(resolverStateLiveData);

		setViewModel(mockViewModel);

		rule.launchActivity(null);
	}

	@After
	public void teardown() {
		rule.finishActivity();
	}

	@Test
	public void should_update_viewmodel() {
		verify(mockViewModel).update();
	}

	@Test
	public void should_display_action_required_and_example() {
		onView(withId(R.id.launcher_youtube_action_action_title)).check(matches(withText(R.string.launcher_youtube_action_title)));
		onView(withId(R.id.launcher_youtube_action_text)).check(matches(withText(R.string.launcher_youtube_action_text)));
		onView(withId(R.id.launcher_youtube_button)).check(matches(isDisplayed()));

		onView(withId(R.id.launcher_example_title)).check(matches(withText(R.string.launcher_example_title)));
		onView(withId(R.id.launcher_example_text)).check(matches(withText(R.string.launcher_example_text)));
		onView(withId(R.id.launcher_example_button)).check(matches(isDisplayed()));
	}

	@Test
	public void should_not_display_action_required_but_should_show_example() {
		resolverStateLiveData.postValue(IntentResolverRepository.ResolverState.OK);

		onView(withId(R.id.launcher_youtube_action_action_title)).check(matches(not(isDisplayed())));
		onView(withId(R.id.launcher_youtube_action_text)).check(matches(not(isDisplayed())));
		onView(withId(R.id.launcher_youtube_button)).check(matches(not(isDisplayed())));

		onView(withId(R.id.launcher_example_title)).check(matches(withText(R.string.launcher_example_title)));
		onView(withId(R.id.launcher_example_text)).check(matches(withText(R.string.launcher_example_text)));
		onView(withId(R.id.launcher_example_button)).check(matches(isDisplayed()));
	}

	@Test
	public void should_open_example_video() {
		Intent resultData = new Intent();
		Instrumentation.ActivityResult result =
				new Instrumentation.ActivityResult(Activity.RESULT_OK, resultData);

		intending(allOf(
				hasAction(LauncherActivity.EXAMPLE_INTENT.getAction()),
				hasData(LauncherActivity.EXAMPLE_INTENT.getData()))).respondWith(result);

		onView(withId(R.id.launcher_example_button)).perform(click());

		intended(allOf(
				hasAction(LauncherActivity.EXAMPLE_INTENT.getAction()),
				hasData(LauncherActivity.EXAMPLE_INTENT.getData())));
	}
}
