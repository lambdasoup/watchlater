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
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.content.pm.verify.domain.DomainVerificationManager
import android.content.pm.verify.domain.DomainVerificationUserState
import android.net.Uri
import android.os.Bundle
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers.*
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withText
import com.lambdasoup.tea.TeaIdlingResource
import com.lambdasoup.watchlater.R
import com.lambdasoup.watchlater.ui.AddActivity
import com.lambdasoup.watchlater.ui.LauncherActivity
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.cucumber.java.After
import io.cucumber.java.Before
import io.cucumber.java.en.Given
import io.cucumber.java.en.Then
import io.cucumber.java.en.When
import io.cucumber.junit.CucumberOptions
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.not
import java.util.concurrent.TimeUnit

@CucumberOptions(
    features = ["features"],
    strict = true,
    name = [".*"],
)
class CucumberTest {

    private var scenario: ActivityScenario<Activity>? = null
    private val packageManager: PackageManager = com.lambdasoup.watchlater.packageManager
    private val domainVerificationManager: DomainVerificationManager =
        com.lambdasoup.watchlater.domainVerificationManager
    private val accountManager = com.lambdasoup.watchlater.accountManager
    private val sharedPreferences = com.lambdasoup.watchlater.sharedPreferences

    private val server = MockWebServer()

    @Before
    fun before() {
        server.start(port = 8080)
        server.dispatcher = object : Dispatcher() {
            override fun dispatch(request: RecordedRequest) =
                when (request.path) {
                    "/videos?part=snippet,contentDetails&maxResults=1&id=dGFSjKuJfrI" ->
                        MockResponse()
                            .setResponseCode(200)
                            .setBody(VIDEO_INFO_JSON)
                    else -> MockResponse()
                        .setResponseCode(404)
                        .setBody("unhandled path + ${request.path}")
                }
        }

        Intents.init()
        Intents.intending(hasAction(Intent.ACTION_VIEW))
            .respondWith(Instrumentation.ActivityResult(Activity.RESULT_OK, Intent()))

        whenever(domainVerificationManager.getDomainVerificationUserState(any())).thenReturn(
            mock()
        )

        IdlingRegistry.getInstance().register(TeaIdlingResource())
    }

    @After
    fun after() {
        Intents.release()
        server.shutdown()
    }

    @When("I open the Launcher")
    fun openLauncher() {
        val intent = Intent(
            ApplicationProvider.getApplicationContext(),
            LauncherActivity::class.java
        )
        scenario = ActivityScenario.launch(intent)
    }

    @When("click on the demo button")
    fun clickDemoButton() {
        onView(withText(R.string.launcher_example_button)).perform(click())
    }

    @Given("Watch Later is not set as default")
    fun watchLaterIsNotDefault() {
        whenever(packageManager.resolveActivity(any(), eq(PackageManager.MATCH_DEFAULT_ONLY)))
            .thenReturn(null)
    }

    @Given("Watch Later is set as default")
    fun watchLaterIsDefault() {
        val resolveInfo: ResolveInfo = ResolveInfo().apply {
            activityInfo = ActivityInfo().apply {
                name = "com.lambdasoup.watchlater.ui.AddActivity"
            }
        }
        whenever(packageManager.resolveActivity(any(), eq(PackageManager.MATCH_DEFAULT_ONLY)))
            .thenReturn(resolveInfo)
    }

    @Given("Watch Later is not missing any hostname verifications")
    fun isNotMissingVerifications() {
        val state: DomainVerificationUserState = mock()
        whenever(state.hostToStateMap).thenReturn(
            mapOf(
                "test.domain.1" to DomainVerificationUserState.DOMAIN_STATE_VERIFIED,
                "test.domain.2" to DomainVerificationUserState.DOMAIN_STATE_VERIFIED,
                "test.domain.3" to DomainVerificationUserState.DOMAIN_STATE_VERIFIED,
            )
        )
        whenever(domainVerificationManager.getDomainVerificationUserState(any()))
            .thenReturn(state)
    }

    @Given("Watch Later is missing some hostname verifications")
    fun isMissingVerifications() {
        val state: DomainVerificationUserState = mock()
        whenever(state.hostToStateMap).thenReturn(
            mapOf(
                "test.domain.1" to DomainVerificationUserState.DOMAIN_STATE_VERIFIED,
                "test.domain.2" to DomainVerificationUserState.DOMAIN_STATE_NONE,
                "test.domain.3" to DomainVerificationUserState.DOMAIN_STATE_VERIFIED,
            )
        )
        whenever(domainVerificationManager.getDomainVerificationUserState(any()))
            .thenReturn(state)
    }

    @Then("I see the Launcher")
    fun launcherIsOpen() {
        onView(withText(R.string.launcher_example_title))
            .check(matches(isDisplayed()))
    }

    @Then("I see the setup instructions")
    fun setupInstructionsVisible() {
        onView(withText(R.string.launcher_action_title))
            .check(matches(isDisplayed()))
    }

    @Then("I do not see the setup instructions")
    fun setupInstructionsNotVisible() {
        onView(withText(R.string.launcher_action_title))
            .check(matches(not(isDisplayed())))
    }

    @Then("the video is opened")
    fun videoIsOpened() {
        Intents.intended(allOf(hasData("https://www.youtube.com/watch?v=dGFSjKuJfrI")))
    }

    @Given("the Google account is set")
    fun googleAccountSet() {
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

    @Given("a playlist has been selected")
    fun playlistSet() {
        whenever(sharedPreferences.getString("playlist-id", null))
            .thenReturn("test-playlist-id")
        whenever(sharedPreferences.getString("playlist-title", null))
            .thenReturn("Test playlist title")
    }

    @When("I open Watch Later via a YouTube URL")
    fun watchLaterOpensFromUrl() {
        val intent = Intent(
            ApplicationProvider.getApplicationContext(),
            AddActivity::class.java
        )
        intent.action = Intent.ACTION_VIEW
        intent.data = Uri.parse("https://www.youtube.com/watch?v=dGFSjKuJfrI")
        scenario = ActivityScenario.launch(intent)
    }

    @When("the user clicks on 'Watch now'")
    fun userClicksWatchNow() {
        onView(withText(R.string.action_watchnow)).perform(click())
    }

    @Then("the YouTube app is opened with the video URL")
    fun youtubeOpened() {
        Intents.intended(
            allOf(
                hasData("https://www.youtube.com/watch?v=dGFSjKuJfrI"),
                hasAction(Intent.ACTION_VIEW),
                hasPackage("com.google.android.youtube"),
            )
        )
    }

    @Then("I see the video info")
    fun videoInfoDisplayed() {
        onView(withText("Test video title"))
            .check(matches(isDisplayed()))
    }

    companion object {
        const val VIDEO_INFO_JSON = """
            {
                "items": [
                    {
                        "id": "dGFSjKuJfrI",
                        "snippet": {
                            "title": "Test video title",
                            "description": "Test video description",
                            "thumbnails": {
                                "medium": {
                                    "url": "http://localhost:8080/test-thumbnail"
                                }
                            }
                        },
                        "contentDetails": {
                            "duration": "1m35s"
                        }
                    }
                ]
            } 
        """
    }
}
