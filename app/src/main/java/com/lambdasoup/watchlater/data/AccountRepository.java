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

package com.lambdasoup.watchlater.data;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.io.IOException;

import static java.util.Arrays.asList;

public class AccountRepository implements SharedPreferences.OnSharedPreferenceChangeListener {

	private static final String PREF_KEY_DEFAULT_ACCOUNT_NAME = "pref_key_default_account_name";

	private static final String SCOPE_YOUTUBE       = "oauth2:https://www.googleapis.com/auth/youtube";
	private final static String ACCOUNT_TYPE_GOOGLE = "com.google";

	private final SharedPreferences prefs;
	private final MutableLiveData<Account> account = new MutableLiveData<>();
	private final AccountManager manager;

	public AccountRepository(Context context) {
		prefs = PreferenceManager.getDefaultSharedPreferences(context);
		prefs.registerOnSharedPreferenceChangeListener(this);

		manager = AccountManager.get(context);

		load();
	}

	private void load() {
		String name = prefs.getString(PREF_KEY_DEFAULT_ACCOUNT_NAME, null);
		if (name != null) {
			account.setValue(new Account(name, ACCOUNT_TYPE_GOOGLE));
		} else {
			account.setValue(null);
		}
	}

	public void put(Account account) {
		SharedPreferences.Editor prefEditor = prefs.edit();
		prefEditor.putString(PREF_KEY_DEFAULT_ACCOUNT_NAME, account.name);
		prefEditor.apply();

		load();
	}

	public void clear() {
		SharedPreferences.Editor prefEditor = prefs.edit();
		prefEditor.remove(PREF_KEY_DEFAULT_ACCOUNT_NAME);
		prefEditor.apply();

		load();
	}

	public LiveData<Account> get() {
		return account;
	}

	public void getToken(TokenCallback callback) {
		if (!isAccountOk()) {
			clear();
			callback.onToken(true, null, null);
			return;
		}

		manager.getAuthToken(account.getValue(),
				SCOPE_YOUTUBE, null, false, accountManagerFuture -> {
					try {
						Bundle result = accountManagerFuture.getResult();
						Intent intent = result.getParcelable(AccountManager.KEY_INTENT);
						if (intent != null) {
							callback.onToken(true, null, intent);
							return;
						}
						callback.onToken(false, result.getString(AccountManager.KEY_AUTHTOKEN), null);
					} catch (OperationCanceledException | IOException | AuthenticatorException e) {
						throw new RuntimeException("could not get token", e);
					}
				}, null);
	}

	public void invalidateToken(String token) {
		manager.invalidateAuthToken(ACCOUNT_TYPE_GOOGLE, token);
	}

	private boolean isAccountOk() {
		Account[] accounts = manager.getAccountsByType(ACCOUNT_TYPE_GOOGLE);
		return asList(accounts).contains(account.getValue());
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
		load();
	}

	public interface TokenCallback {
		void onToken(boolean hasError, String token, Intent intent);
	}

}
