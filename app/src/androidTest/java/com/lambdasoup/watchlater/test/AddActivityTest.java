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
import android.content.Intent;
import android.net.Uri;
import android.support.test.InstrumentationRegistry;
import android.test.ActivityInstrumentationTestCase2;

import com.lambdasoup.watchlater.AddActivity;

import org.junit.Before;

import java.lang.reflect.Field;

/**
 * Integration test for the {@link com.lambdasoup.watchlater.AddActivity}
 */
public class AddActivityTest extends ActivityInstrumentationTestCase2<AddActivity> {

	public static final String MOCK_ENDPOINT = "http://localhost:8080/lol";
	private final static String TEST_ACCOUNT_TYPE = "com.lambdasoup.watchlater.test";

	public AddActivityTest() {
		super(AddActivity.class);
	}

	@Before
	public void setUp() throws Exception {
		super.setUp();
		injectInstrumentation(InstrumentationRegistry.getInstrumentation());

		// hack app to use test account type
		Field accountType = AddActivity.class.getDeclaredField("ACCOUNT_TYPE_GOOGLE");
		accountType.setAccessible(true);
		accountType.set(AddActivity.class, TEST_ACCOUNT_TYPE);

		Field endpoint = AddActivity.class.getDeclaredField("YOUTUBE_ENDPOINT");
		endpoint.setAccessible(true);
		endpoint.set(AddActivity.class, MOCK_ENDPOINT);

		// create test account
		AccountManager accountManager = AccountManager.get(getInstrumentation().getContext());
		Account account = new Account("test account 1", TEST_ACCOUNT_TYPE);
		//noinspection ResourceType
		accountManager.addAccountExplicitly(account, null, null);

		MockEndpoint mockEndpoint = new MockEndpoint("localhost", 8080);
		mockEndpoint.start();

		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public void test_add() {
		Intent addIntent = new Intent();
		addIntent.setData(Uri.parse("https://youtube.com/v/43yw96845"));
		setActivityIntent(addIntent);

		getActivity();

		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

}
