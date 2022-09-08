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

@file:Suppress("ClassName")

package com.lambdasoup.watchlater.test

import android.app.Activity
import android.app.Instrumentation
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.content.pm.verify.domain.DomainVerificationManager
import android.content.pm.verify.domain.DomainVerificationUserState
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.performClick
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers
import com.lambdasoup.tea_junit4.TeaIdlingResource
import com.lambdasoup.watchlater.R
import com.lambdasoup.watchlater.onNodeWithTextRes
import com.lambdasoup.watchlater.ui.LauncherActivity
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.hamcrest.Matchers
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement

class Launcher_try_demo_link_Test : LauncherActivityTest(
    isWatchLaterDefault = true,
    isHostnameVerificationComplete = true
) {
    @Test
    fun try_demo_link() {
        clickDemoButton()
        assertVideoOpened()
    }
}

class Launcher_needs_default_Test : LauncherActivityTest(
    isWatchLaterDefault = false,
    isHostnameVerificationComplete = true
) {
    @Test
    fun needs_default() {
        assertSetupInstructionsVisible()
    }
}

class Launcher_needs_verification_Test : LauncherActivityTest(
    isWatchLaterDefault = true,
    isHostnameVerificationComplete = false
) {
    @Test
    fun needs_verification() {
        assertSetupInstructionsVisible()
    }
}

class Launcher_setup_complete_Test : LauncherActivityTest(
    isWatchLaterDefault = true,
    isHostnameVerificationComplete = true,
) {
    @Test
    fun setup_complete() {
        assertSetupInstructionsInvisible()
    }
}

class LauncherActivityEnvironmentRule(
    private val activityRule: AndroidComposeTestRule<*, *>,
    private val isWatchLaterDefault: Boolean,
    private val isHostnameVerificationComplete: Boolean
) : TestRule {
    private val idlingResource = TeaIdlingResource()
    private val packageManager: PackageManager = com.lambdasoup.watchlater.packageManager
    private val domainVerificationManager: DomainVerificationManager =
        com.lambdasoup.watchlater.domainVerificationManager
    private val server = MockWebServer()


    private fun before() {
        setupMocking()

        val resolveInfo = if (isWatchLaterDefault) {
            ResolveInfo().apply {
                activityInfo = ActivityInfo().apply {
                    name = "com.lambdasoup.watchlater.ui.AddActivity"
                }
            }
        } else {
            null
        }
        whenever(packageManager.resolveActivity(any(), eq(PackageManager.MATCH_DEFAULT_ONLY)))
            .thenReturn(resolveInfo)

        val state: DomainVerificationUserState = mock()
        val stateLookupResult = if (isHostnameVerificationComplete) {
            mapOf(
                "test.domain.1" to DomainVerificationUserState.DOMAIN_STATE_VERIFIED,
                "test.domain.2" to DomainVerificationUserState.DOMAIN_STATE_VERIFIED,
                "test.domain.3" to DomainVerificationUserState.DOMAIN_STATE_VERIFIED,
            )
        } else {
            mapOf(
                "test.domain.1" to DomainVerificationUserState.DOMAIN_STATE_VERIFIED,
                "test.domain.2" to DomainVerificationUserState.DOMAIN_STATE_NONE,
                "test.domain.3" to DomainVerificationUserState.DOMAIN_STATE_VERIFIED,
            )
        }
        whenever(state.hostToStateMap).thenReturn(stateLookupResult)
        whenever(domainVerificationManager.getDomainVerificationUserState(any()))
            .thenReturn(state)
    }

    private fun setupMocking() {
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

        whenever(domainVerificationManager.getDomainVerificationUserState(any())).thenReturn(
            mock()
        )

        activityRule.registerIdlingResource(idlingResource.composeIdlingResource)
    }

    private fun after() {
        activityRule.unregisterIdlingResource(idlingResource.composeIdlingResource)
        Intents.release()
        server.shutdown()
    }

    override fun apply(base: Statement, description: Description): Statement {
        return object : Statement() {
            @Throws(Throwable::class)
            override fun evaluate() {
                before()
                try {
                    base.evaluate()
                } finally {
                    after()
                }
            }
        }
    }

}

abstract class LauncherActivityTest(
    isWatchLaterDefault: Boolean,
    isHostnameVerificationComplete: Boolean
) {
    @get:Rule(order = 1)
    val activityRule = createAndroidComposeRule<LauncherActivity>()

    @get:Rule(order = 0)
    val environmentRule = LauncherActivityEnvironmentRule(
        activityRule = activityRule,
        isWatchLaterDefault = isWatchLaterDefault,
        isHostnameVerificationComplete = isHostnameVerificationComplete
    )

    fun clickDemoButton() {
        activityRule.onNodeWithTextRes(R.string.launcher_example_button, ignoreCase = true).performClick()
    }

    fun assertVideoOpened() {
        Intents.intended(Matchers.allOf(IntentMatchers.hasData("https://www.youtube.com/watch?v=dGFSjKuJfrI")))
    }

    fun assertSetupInstructionsVisible() {
        activityRule.onNodeWithTextRes(R.string.launcher_action_title).assertExists()
    }

    fun assertSetupInstructionsInvisible() {
        activityRule.onNodeWithTextRes(R.string.launcher_action_title).assertDoesNotExist()
    }
}
