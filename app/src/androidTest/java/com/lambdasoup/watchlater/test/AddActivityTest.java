/*
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

import android.os.Process;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Intent;
import android.net.Uri;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.IdlingResource;
import android.test.ActivityInstrumentationTestCase2;

import com.lambdasoup.watchlater.AddActivity;
import com.lambdasoup.watchlater.R;
import com.squareup.okhttp.mockwebserver.MockResponse;
import com.squareup.okhttp.mockwebserver.MockWebServer;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;


import static android.os.Process.THREAD_PRIORITY_BACKGROUND;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.Espresso.registerIdlingResources;
import static android.support.test.espresso.Espresso.unregisterIdlingResources;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

/**
 * Integration test for the {@link com.lambdasoup.watchlater.AddActivity}
 */
public class AddActivityTest extends ActivityInstrumentationTestCase2<AddActivity> {

	private static final String TEST_ACCOUNT_TYPE = "com.lambdasoup.watchlater.test";

	private static final Account ACCOUNT_1 = new Account("test account 1", TEST_ACCOUNT_TYPE);
	private static final Account ACCOUNT_2 = new Account("test account 2", TEST_ACCOUNT_TYPE);

	private RetrofitHttpExecutorIdlingResource idlingExecutor;
    private MockWebServer mockWebServer;

	public AddActivityTest() throws IOException {
		super(AddActivity.class);
	}

	@Override
	public void setUp() throws Exception {
		super.setUp();
		injectInstrumentation(InstrumentationRegistry.getInstrumentation());

        mockWebServer = new MockWebServer();
        mockWebServer.start(8080);

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
		AccountManager accountManager = AccountManager.get(getInstrumentation().getContext());
		//noinspection ResourceType,deprecation
		accountManager.removeAccount(ACCOUNT_1, null, null).getResult();
		//noinspection ResourceType,deprecation
		accountManager.removeAccount(ACCOUNT_2, null, null).getResult();

		// inject mock backend
		Field endpoint = AddActivity.class.getDeclaredField("YOUTUBE_ENDPOINT");
		endpoint.setAccessible(true);
		endpoint.set(AddActivity.class, mockWebServer.url("/").toString());
	}

	@Override
	protected void tearDown() throws Exception {
		mockWebServer.shutdown();
		unregisterIdlingResources(idlingExecutor);

		super.tearDown();
	}

	private void addAccount(Account account) {
		AccountManager accountManager = AccountManager.get(getInstrumentation().getContext());
		//noinspection ResourceType
		accountManager.addAccountExplicitly(account, null, null);
	}

	public void test_noAccount() {
		getActivity();

		onView(withText(R.string.no_account)).check(matches(isDisplayed()));
	}

	public void test_multipleAccounts() {
		addAccount(ACCOUNT_1);
		addAccount(ACCOUNT_2);

		getActivity();

		onView(withText(R.string.choose_account)).check(matches(isDisplayed()));
	}

	public void test_add_success() throws Exception {
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

            MockResponse response = new MockResponse();
            response.setBody(json.toString(8));
            mockWebServer.enqueue(response);
		}

		// set add video to list response
		{
			JSONObject json = new JSONObject();

            MockResponse response = new MockResponse();
            response.setBody(json.toString(8));
            mockWebServer.enqueue(response);
		}

		// set account
		addAccount(ACCOUNT_1);

		// set activity arg
		Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://youtube.com/v/8f7h837f4"));
		setActivityIntent(intent);

		getActivity();

		onView(withText(R.string.success_added_video)).check(matches(isDisplayed()));
	}

	public void test_add_already_in_playlist() throws Exception {
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

            MockResponse response = new MockResponse();
            response.setBody(json.toString(8));
            mockWebServer.enqueue(response);
		}

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
            mockWebServer.enqueue(response);
		}

		// set account
		addAccount(ACCOUNT_1);

		// set activity arg
		Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://youtube.com/v/8f7h837f4"));
		setActivityIntent(intent);

		getActivity();

		onView(withText(R.string.error_already_in_playlist)).check(matches(isDisplayed()));
	}

	private static class RetrofitHttpExecutorIdlingResource extends ThreadPoolExecutor implements IdlingResource {

		public static final String IDLE_THREAD_NAME = "RetrofitReplacement-Idle";
		private final AtomicInteger currentTaskCount = new AtomicInteger(0);
		private volatile ResourceCallback idleTransitionCallback;

		public RetrofitHttpExecutorIdlingResource() {
			super(0, Integer.MAX_VALUE, 60L, TimeUnit.SECONDS, new SynchronousQueue<Runnable>(),
                    new ThreadFactory() {
                        @Override
                        public Thread newThread(final Runnable r) {
                            return new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    Process.setThreadPriority(THREAD_PRIORITY_BACKGROUND);
                                    r.run();
                                }
                            }, IDLE_THREAD_NAME);
                        }
                    }
            );
		}

		@Override
		public String getName() {
			return RetrofitHttpExecutorIdlingResource.class.getName();
		}

		@Override
		public boolean isIdleNow() {
			boolean idle = currentTaskCount.intValue() == 0;
			if (idle && idleTransitionCallback != null) {
				idleTransitionCallback.onTransitionToIdle();
			}
			return idle;
		}

		@Override
		public void registerIdleTransitionCallback(ResourceCallback resourceCallback) {
			this.idleTransitionCallback = resourceCallback;
		}

		@Override
		protected void afterExecute(Runnable r, Throwable t) {
			super.afterExecute(r, t);
			if (currentTaskCount.decrementAndGet() == 0 && idleTransitionCallback != null) {
				idleTransitionCallback.onTransitionToIdle();
			}
		}

		@Override
		public void execute(Runnable command) {
			currentTaskCount.incrementAndGet();
			super.execute(command);
		}
	}

}
