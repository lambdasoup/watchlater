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
package com.lambdasoup.watchlater.ui

import android.app.Activity
import android.app.Instrumentation.ActivityResult
import android.content.Intent
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.MutableLiveData
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents.*
import androidx.test.espresso.intent.matcher.IntentMatchers.hasAction
import androidx.test.espresso.intent.matcher.IntentMatchers.hasData
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.lambdasoup.watchlater.R
import com.lambdasoup.watchlater.data.IntentResolverRepository.ResolverState
import com.lambdasoup.watchlater.util.EventSource
import com.lambdasoup.watchlater.viewmodel.LauncherViewModel
import com.lambdasoup.watchlater.viewmodel.LauncherViewModel.Event
import com.lambdasoup.watchlater.viewmodel.LauncherViewModel.Model
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.not
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.reset

@RunWith(AndroidJUnit4::class)
@LargeTest
class LauncherActivityTest : WatchLaterActivityTest() {

    private val mockViewModel: LauncherViewModel = mock()
    
    private val	model = MutableLiveData<Model>()
    private val events = EventSource<Event>()

    @Before
    fun setup() {
        reset(mockViewModel)
        whenever(mockViewModel.model).thenReturn(model)
        whenever(mockViewModel.events).thenReturn(events)
        setViewModel(mockViewModel)

        model.postValue(Model(resolverState = null))
        events.clear()
        
        val scenario = ActivityScenario.launch(LauncherActivity::class.java)
        scenario.moveToState(Lifecycle.State.RESUMED)
        init()
    }

    @After
    fun teardown() {
        release()
    }

    @Test
    fun should_update_viewmodel() {
        verify(mockViewModel).onResume()
    }

    @Test
    fun should_display_action_required_and_example() {
        onView(withId(R.id.launcher_youtube_action_action_title)).check(matches(withText(R.string.launcher_youtube_action_title)))
        onView(withId(R.id.launcher_youtube_action_text)).check(matches(withText(R.string.launcher_youtube_action_text)))
        onView(withId(R.id.launcher_youtube_button)).check(matches(isDisplayed()))
        onView(withId(R.id.launcher_example_title)).check(matches(withText(R.string.launcher_example_title)))
        onView(withId(R.id.launcher_example_text)).check(matches(withText(R.string.launcher_example_text)))
        onView(withId(R.id.launcher_example_button)).check(matches(isDisplayed()))
    }

    @Test
    fun should_not_display_action_required_but_should_show_example() {
        model.postValue(Model(ResolverState.OK))

        onView(withId(R.id.launcher_youtube_action_action_title)).check(matches(not(isDisplayed())))
        onView(withId(R.id.launcher_youtube_action_text)).check(matches(not(isDisplayed())))
        onView(withId(R.id.launcher_youtube_button)).check(matches(not(isDisplayed())))
        onView(withId(R.id.launcher_example_title)).check(matches(withText(R.string.launcher_example_title)))
        onView(withId(R.id.launcher_example_text)).check(matches(withText(R.string.launcher_example_text)))
        onView(withId(R.id.launcher_example_button)).check(matches(isDisplayed()))
    }

    @Test
    fun should_invoke_try_video() {
        onView(withId(R.id.launcher_example_button)).perform(click())

        verify(mockViewModel).onTryExample()
    }

    @Test
    fun should_open_example_video() {
        val resultData = Intent()
        val result = ActivityResult(Activity.RESULT_OK, resultData)
        intending(allOf(
                hasAction(LauncherActivity.EXAMPLE_INTENT.action),
                hasData(LauncherActivity.EXAMPLE_INTENT.data))).respondWith(result)

        events.submit(Event.OpenExample)

        intended(allOf(
                hasAction(LauncherActivity.EXAMPLE_INTENT.action),
                hasData(LauncherActivity.EXAMPLE_INTENT.data)))
    }
}
