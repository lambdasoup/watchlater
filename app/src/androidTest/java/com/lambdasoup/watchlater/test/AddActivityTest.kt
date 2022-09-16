/*
 * Copyright (c) 2015 - 2022
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

package com.lambdasoup.watchlater.test

import android.accounts.Account
import android.accounts.AccountManager
import android.accounts.AccountManagerFuture
import android.app.Activity
import android.app.Instrumentation
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers
import com.lambdasoup.tea.testing.TeaIdlingResource
import com.lambdasoup.watchlater.R
import com.lambdasoup.watchlater.onNodeWithTextRes
import com.lambdasoup.watchlater.ui.add.AddActivity
import com.nhaarman.mockitokotlin2.whenever
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.hamcrest.Matchers
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito
import java.util.concurrent.TimeUnit

class AddActivityTest {
    @Test
    fun see_video_info() {
        setupGoogleAccountIsSet()

        openWatchlaterViaYoutubeUri()

        assertVideoInfoVisible()
    }

    @Test
    fun watch_now() {
        setupGoogleAccountIsSet()
        setupPlaylistSelected()

        openWatchlaterViaYoutubeUri()

        clickWatchNow()

        assertYoutubeOpenedWithVideoUri()
    }


    @get:Rule
    val composeTestRule = createEmptyComposeRule()

    private lateinit var scenario: ActivityScenario<Activity>
    private val accountManager = com.lambdasoup.watchlater.accountManager
    private val sharedPreferences = com.lambdasoup.watchlater.sharedPreferences
    private val server = MockWebServer()
    private val idlingResource = TeaIdlingResource()

    @Before
    fun before() {
        server.start(port = 8080)
        server.dispatcher = object : Dispatcher() {
            override fun dispatch(request: RecordedRequest) =
                when (request.path) {
                    "/videos?part=snippet,contentDetails&maxResults=1&id=dGFSjKuJfrI" ->
                        MockResponse()
                            .setResponseCode(200)
                            .setBody(CucumberTest.VIDEO_INFO_JSON)
                    else -> MockResponse()
                        .setResponseCode(404)
                        .setBody("unhandled path + ${request.path}")
                }
        }

        Intents.init()
        Intents.intending(IntentMatchers.hasAction(Intent.ACTION_VIEW))
            .respondWith(Instrumentation.ActivityResult(Activity.RESULT_OK, Intent()))

        composeTestRule.registerIdlingResource(idlingResource.composeIdlingResource)
    }

    @After
    fun after() {
        composeTestRule.unregisterIdlingResource(idlingResource.composeIdlingResource)
        Intents.release()
        server.shutdown()
        Mockito.reset(accountManager, sharedPreferences)
    }

    private fun setupGoogleAccountIsSet() {
        whenever(sharedPreferences.getString("pref_key_default_account_name", null))
            .thenReturn("test@google.com")
        val account = Account("test@google.com", "com.google")
        whenever(accountManager.getAccountsByType("com.google"))
            .thenReturn(
                arrayOf(account)
            )
        whenever(
            accountManager.getAuthToken(
                account,
                "oauth2:https://www.googleapis.com/auth/youtube",
                null,
                false,
                null,
                null
            )
        )
            .thenReturn(object : AccountManagerFuture<Bundle> {
                override fun cancel(p0: Boolean) = throw NotImplementedError()

                override fun isCancelled() = throw NotImplementedError()

                override fun isDone() = throw NotImplementedError()

                override fun getResult() = Bundle().apply {
                    putString(AccountManager.KEY_AUTHTOKEN, "test-auth-token")
                }

                override fun getResult(p0: Long, p1: TimeUnit?) = throw NotImplementedError()
            })
    }

    private fun setupPlaylistSelected() {
        whenever(sharedPreferences.getString("playlist-id", null))
            .thenReturn("test-playlist-id")
        whenever(sharedPreferences.getString("playlist-title", null))
            .thenReturn("Test playlist title")
    }

    private fun openWatchlaterViaYoutubeUri() {
        val intent = Intent(
            ApplicationProvider.getApplicationContext(),
            AddActivity::class.java
        ).apply {
            action = Intent.ACTION_VIEW
            data = Uri.parse("https://www.youtube.com/watch?v=dGFSjKuJfrI")
        }
        scenario = ActivityScenario.launch(intent)
    }

    private fun clickWatchNow() =
        composeTestRule.onNodeWithTextRes(R.string.action_watchnow, ignoreCase = true).performClick()

    private fun assertVideoInfoVisible() =
        composeTestRule.onNodeWithText("Test video title").assertExists()

    private fun assertYoutubeOpenedWithVideoUri() {
        Intents.intended(
            Matchers.allOf(
                IntentMatchers.hasData("https://www.youtube.com/watch?v=dGFSjKuJfrI"),
                IntentMatchers.hasAction(Intent.ACTION_VIEW),
                IntentMatchers.hasPackage("com.google.android.youtube"),
            )
        )
    }
}
