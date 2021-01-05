/*
 * Copyright (c) 2015 - 2021
 *
 * Maximilian Hille <mh@lambdasoup.com>
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

package com.lambdasoup.watchlater.ui;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Intent;

import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.MutableLiveData;
import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.lambdasoup.watchlater.R;
import com.lambdasoup.watchlater.data.IntentResolverRepository;
import com.lambdasoup.watchlater.viewmodel.LauncherViewModel;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.Intents.intending;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasAction;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasData;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.not;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class LauncherActivityTest extends WatchLaterActivityTest {

    private final LauncherViewModel mockViewModel = mock(LauncherViewModel.class);
    private MutableLiveData<IntentResolverRepository.ResolverState> resolverStateLiveData;

    @Before
    public void setup() {
        resolverStateLiveData = new MutableLiveData<>();
        reset(mockViewModel);
        when(mockViewModel.getResolverState()).thenReturn(resolverStateLiveData);

        setViewModel(mockViewModel);

        Intents.init();
        ActivityScenario<LauncherActivity> scenario = ActivityScenario.launch(LauncherActivity.class);
        scenario.moveToState(Lifecycle.State.RESUMED);
    }

    @After
    public void teardown() {
        Intents.release();
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
