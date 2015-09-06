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

import android.accounts.Account;
import android.accounts.AccountManager;
import android.support.test.InstrumentationRegistry;
import android.test.ActivityInstrumentationTestCase2;

import com.lambdasoup.watchlater.AddActivity;
import com.lambdasoup.watchlater.R;

import org.junit.After;
import org.junit.Before;

import java.lang.reflect.Field;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

/**
 * Integration test for the {@link com.lambdasoup.watchlater.AddActivity}
 */
public class AddActivityTest extends ActivityInstrumentationTestCase2<AddActivity> {

	private static final String MOCK_ENDPOINT = "http://localhost:8080/lol";
	private static final String TEST_ACCOUNT_TYPE = "com.lambdasoup.watchlater.test";

	private static final Account ACCOUNT_1 = new Account("test account 1", TEST_ACCOUNT_TYPE);
	private static final Account ACCOUNT_2 = new Account("test account 2", TEST_ACCOUNT_TYPE);
	private MockEndpoint mockEndpoint;

	public AddActivityTest() {
		super(AddActivity.class);
	}

	@Before
	public void setUp() throws Exception {
		super.setUp();
		injectInstrumentation(InstrumentationRegistry.getInstrumentation());

		// inject test account type
		Field accountType = AddActivity.class.getDeclaredField("ACCOUNT_TYPE_GOOGLE");
		accountType.setAccessible(true);
		accountType.set(AddActivity.class, TEST_ACCOUNT_TYPE);

		// inject mock backend
		Field endpoint = AddActivity.class.getDeclaredField("YOUTUBE_ENDPOINT");
		endpoint.setAccessible(true);
		endpoint.set(AddActivity.class, MOCK_ENDPOINT);
		mockEndpoint = new MockEndpoint("localhost", 8080);
		mockEndpoint.start();
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	@After
	public void tearDown() throws Exception {
		// clear accounts
		AccountManager accountManager = AccountManager.get(getInstrumentation().getContext());
		//noinspection ResourceType,deprecation
		accountManager.removeAccount(ACCOUNT_1, null, null);
		//noinspection ResourceType,deprecation
		accountManager.removeAccount(ACCOUNT_2, null, null);

		// shut down mock endpoint
		mockEndpoint.stop();
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

}
