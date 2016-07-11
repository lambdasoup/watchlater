/*
 * Copyright (c) 2015 - 2016
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

package com.lambdasoup.watchlater;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import java.io.IOException;

import okhttp3.Authenticator;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Route;

/**
 * Created by jl on 07.07.16.
 */
public class GoogleAccountAuthenticator implements Authenticator, Interceptor, Parcelable {
	private static final String TAG           = GoogleAccountAuthenticator.class.getSimpleName();
	private static final String SCOPE_YOUTUBE = "oauth2:https://www.googleapis.com/auth/youtube";
	private final Account        account;
	private       AccountManager manager;
	private       String         token;
	private       Activity       activity;

	/**
	 * call init after construction to obtain a usable instance
	 */
	public GoogleAccountAuthenticator(Account account) {
		this.account = account;
	}

	public void init(Context context) {
		manager = AccountManager.get(context);
	}


	@Override
	public Request authenticate(Route route, Response response) throws IOException {
		Log.d(TAG, "authenticate: account=" + account + " token=" + token);
		if (responseCount(response) >= 3) {
			Log.d(TAG, "giving up authentication after too many retries");
			return null;
		}
		if (account != null && token != null) {
			manager.invalidateAuthToken(account.type, token);
			Log.d(TAG, "auth token invalidated");
			try {
				setAuthToken();
			} catch (AuthTokenAcquisitionFailure authTokenAcquisitionFailure) {
				return null;
			}
			return withAuthHeader(response.request());
		} else {
			return null;
		}
	}

	private int responseCount(Response response) {
		Response priorResponse = response.priorResponse();
		return priorResponse == null ? 1 : responseCount(priorResponse) + 1;
	}

	@Override
	public Response intercept(Interceptor.Chain chain) throws IOException {
		if (token == null) {
			try {
				setAuthToken();
			} catch (AuthTokenAcquisitionFailure authTokenAcquisitionFailure) {
				return null;
			}
		}
		Log.d(TAG, "adding auth header with token " + token);
		return chain.proceed(withAuthHeader(chain.request()));
	}

	private void setAuthToken() throws AuthTokenAcquisitionFailure {
		Log.d(TAG, "setting auth token using account " + account);
		try {
			if (activity != null) {
				AccountManagerFuture<Bundle> future = manager.getAuthToken(account, SCOPE_YOUTUBE, null, activity, null, null);
				// block thread until user interaction finished
				Bundle result = future.getResult();
				token = result.getString(AccountManager.KEY_AUTHTOKEN);
			} else {
				AccountManagerFuture<Bundle> future = manager.getAuthToken(account, SCOPE_YOUTUBE, null, true, null, null);
				// block thread until user interaction finished
				Bundle result = future.getResult();
				token = result.getString(AccountManager.KEY_AUTHTOKEN);
			}
			Log.d(TAG, "new token is " + token);
		} catch (OperationCanceledException | AuthenticatorException | IOException e) {
			Log.d(TAG, "auth token acquisition failed");
			throw new AuthTokenAcquisitionFailure(e);
		}
	}

	private Request withAuthHeader(Request original) {
		return original.newBuilder().header("Authorization", "Bearer " + token).build();
	}

	public void attachActivity(Activity activity) {
		this.activity = activity;
	}

	public void detachActivity() {
		this.activity = null;
	}


	public static class AuthTokenAcquisitionFailure extends Exception {
		public AuthTokenAcquisitionFailure() {
		}

		public AuthTokenAcquisitionFailure(String detailMessage) {
			super(detailMessage);
		}

		public AuthTokenAcquisitionFailure(String detailMessage, Throwable throwable) {
			super(detailMessage, throwable);
		}

		public AuthTokenAcquisitionFailure(Throwable throwable) {
			super(throwable);
		}
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeParcelable(this.account, flags);
		dest.writeString(this.token);
	}

	protected GoogleAccountAuthenticator(Parcel in) {
		this.account = in.readParcelable(Account.class.getClassLoader());
		this.token = in.readString();
	}

	public static final Parcelable.Creator<GoogleAccountAuthenticator> CREATOR = new Parcelable.Creator<GoogleAccountAuthenticator>() {
		@Override
		public GoogleAccountAuthenticator createFromParcel(Parcel source) {
			return new GoogleAccountAuthenticator(source);
		}

		@Override
		public GoogleAccountAuthenticator[] newArray(int size) {
			return new GoogleAccountAuthenticator[size];
		}
	};
}
