/*
 * Copyright (c) 2015.
 *
 *  Maximilian Hille <mh@lambdasoup.com>
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

package com.lambdasoup.watchlater.test;/*
 * Copyright (c) 2015. Maximilian Hille <mh@lambdasoup.com>
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

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Intent;
import android.net.Uri;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.ViewInteraction;
import android.support.test.espresso.action.ViewActions;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;

import com.lambdasoup.watchlater.AddActivity;
import com.lambdasoup.watchlater.R;
import com.squareup.okhttp.mockwebserver.MockResponse;
import com.squareup.okhttp.mockwebserver.MockWebServer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.lang.reflect.Field;
import java.util.Locale;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.Espresso.registerIdlingResources;
import static android.support.test.espresso.Espresso.unregisterIdlingResources;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

/**
 * Integration test for the {@link com.lambdasoup.watchlater.AddActivity}
 */
@LargeTest
@RunWith(AndroidJUnit4.class)
public class AddActivityTest  {

	private static final String TEST_ACCOUNT_TYPE = "com.lambdasoup.watchlater.test";

	private static final Account ACCOUNT_1 = new Account("test account 1", TEST_ACCOUNT_TYPE);
	private static final Account ACCOUNT_2 = new Account("test account 2", TEST_ACCOUNT_TYPE);
	public static final String CHANNEL_TITLE = "Testi Testsdottir";

	private        RetrofitHttpExecutorIdlingResource idlingExecutor;
	private static MockWebServer                      mockWebServer;
	private static RestfulDispatcher                  restfulDispatcher;

	@Rule
	public ActivityTestRule<AddActivity> activityTestRule = new ActivityTestRule<AddActivity>(AddActivity.class, false, false) {
		@Override
		protected Intent getActivityIntent() {
			Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://youtube.com/v/8f7h837f4"));
			return intent;
		}
	};

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		restfulDispatcher = new RestfulDispatcher();
		mockWebServer = new MockWebServer();
		mockWebServer.setDispatcher(restfulDispatcher);
		mockWebServer.start(8080);
	}

	@Before
	public void setUp() throws Exception {

		// inject retrofit http executor for espresso idling resource
		Field httpExecutor = AddActivity.class.getDeclaredField("OPTIONAL_RETROFIT_HTTP_EXECUTOR");
		httpExecutor.setAccessible(true);
		idlingExecutor = new RetrofitHttpExecutorIdlingResource();
		httpExecutor.set(AddActivity.class, idlingExecutor);
		registerIdlingResources(idlingExecutor);

		// inject test account type
		Field accountType = AddActivity.class.getDeclaredField("ACCOUNT_TYPE_GOOGLE");
		accountType.setAccessible(true);
		accountType.set(AddActivity.class, TEST_ACCOUNT_TYPE);

		// clear accounts
		AccountManager accountManager = AccountManager.get(InstrumentationRegistry.getInstrumentation().getContext());
		//noinspection ResourceType,deprecation
		accountManager.removeAccount(ACCOUNT_1, null, null).getResult();
		//noinspection ResourceType,deprecation
		accountManager.removeAccount(ACCOUNT_2, null, null).getResult();

		// inject mock backend
		Field endpoint = AddActivity.class.getDeclaredField("YOUTUBE_ENDPOINT");
		endpoint.setAccessible(true);
		endpoint.set(AddActivity.class, mockWebServer.url("/").toString());
	}

	@After
	public void tearDown() throws Exception {
		unregisterIdlingResources(idlingExecutor);
		restfulDispatcher.clear();
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		mockWebServer.shutdown();
	}

	private void addAccount(Account account) {
		AccountManager accountManager = AccountManager.get(InstrumentationRegistry.getInstrumentation().getContext());
		//noinspection ResourceType
		accountManager.addAccountExplicitly(account, null, null);
	}

	private String fillChannelTitle(int msgId) {
		return String.format(Locale.US, activityTestRule.getActivity().getResources().getString(msgId), CHANNEL_TITLE);
	}

	@Test
	public void noAccount() throws Exception {
		activityTestRule.launchActivity(null);

		onView(withText(R.string.no_account)).check(matches(isDisplayed()));
	}

	@Test
	public void multipleAccounts() throws Exception {
		addAccount(ACCOUNT_1);
		addAccount(ACCOUNT_2);

		activityTestRule.launchActivity(null);

		ViewInteraction accountChooserHeader = onView(withText(R.string.choose_account));
		accountChooserHeader.check(matches(isDisplayed()));
		accountChooserHeader.perform(ViewActions.click());
		accountChooserHeader.check(matches(isDisplayed()));

	}

	@Test
	public void addSuccess() throws Exception {
		String testDescription = "Description for the Test video";
		String testTitle = "Test Title";

		// set channel list response
		registerChannelListResponse();

		// set add video to list response
		{
			JSONObject json = new JSONObject();

			JSONObject snippet = new JSONObject();
			snippet.put("title", testTitle);
			snippet.put("description", testDescription);
			json.put("snippet", snippet);

			MockResponse response = new MockResponse();
			response.setBody(json.toString(8));
			restfulDispatcher.registerResponse("/playlistItems?part=snippet", response);
		}

		// set account
		addAccount(ACCOUNT_1);

		// launch activity
		activityTestRule.launchActivity(null);

		onView(withText(fillChannelTitle(R.string.success_added_video))).check(matches(isDisplayed()));
		onView(withText(testTitle)).check(matches(isDisplayed()));
		onView(withText(testDescription)).check(matches(isDisplayed()));
	}

	@Test
	public void addAlreadyInPlaylist() throws Exception {
		registerChannelListResponse();


		// set add video to list response
		{
            JSONObject error0 = new JSONObject();
            error0.put("domain", "youtube.playlistItem");
            error0.put("reason", "videoAlreadyInPlaylist");
            error0.put("message", "Video already in playlist.");
            JSONArray errors = new JSONArray();
            errors.put(error0);
            JSONObject error = new JSONObject();
            error.put("errors", errors);
            error.put("code", 409);
            error.put("message", "Video already in playlist.");
            JSONObject json = new JSONObject();
            json.put("error", error);

            MockResponse response = new MockResponse();
            response.setBody(json.toString(8));
            response.setStatus("HTTP/1.1 409 Conflict");
			restfulDispatcher.registerResponse("/playlistItems?part=snippet", response);
		}

		// set account
		addAccount(ACCOUNT_1);

		// launch activity
		activityTestRule.launchActivity(null);

		onView(withText(fillChannelTitle(R.string.error_already_in_playlist))).check(matches(isDisplayed()));
	}

	private void registerChannelListResponse() throws JSONException {
		// set channel list response
		{
			JSONObject json = new JSONObject();
			JSONArray items = new JSONArray();
			json.put("items", items);
			JSONObject channel = new JSONObject();
			items.put(channel);
			JSONObject contentDetails = new JSONObject();
			channel.put("contentDetails", contentDetails);
			JSONObject relatedPlaylists = new JSONObject();
			contentDetails.put("relatedPlaylists", relatedPlaylists);
			String watchLaterId = "45h7394875w3495";
			relatedPlaylists.put("watchLater", watchLaterId);
			JSONObject snippet = new JSONObject();
			channel.put("snippet", snippet);
			snippet.put("title", CHANNEL_TITLE);

            MockResponse response = new MockResponse();
            response.setBody(json.toString(8));
			restfulDispatcher.registerResponse("/channels?part=contentDetails,snippet&maxResults=50&mine=true", response);
		}
	}

	@Test
	public void authFail() throws Exception {
		// set channel list response
		{
			JSONObject error0 = new JSONObject();
			error0.put("domain", "youtube.playlistItem");
			error0.put("reason", "dailyLimitExceededUnreg");
			JSONArray errors = new JSONArray();
			errors.put(error0);
			JSONObject error = new JSONObject();
			error.put("errors", errors);
			error.put("code", 403);
			JSONObject json = new JSONObject();
			json.put("error", error);

			MockResponse response = new MockResponse();
			response.setBody(json.toString(8));
			response.setStatus("HTTP/1.1 403 Forbidden");
			restfulDispatcher.registerResponse("/channels?part=contentDetails,snippet&maxResults=50&mine=true", response);
		}
		// set account
		addAccount(ACCOUNT_1);

		// launch activity
		activityTestRule.launchActivity(null);

		onView(withText(fillChannelTitle(R.string.error_need_account))).check(matches(isDisplayed()));

	}

}
