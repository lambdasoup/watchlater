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

package com.lambdasoup.watchlater;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Fragment;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.lambdasoup.watchlater.YoutubeApi.ErrorTranslatingCallback;
import com.lambdasoup.watchlater.YoutubeApi.ErrorType;

import java.io.IOException;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.Executor;

import retrofit.RestAdapter;
import retrofit.android.MainThreadExecutor;
import retrofit.client.OkClient;
import retrofit.client.Response;
import retrofit.converter.GsonConverter;

import static android.net.Uri.decode;
import static android.net.Uri.parse;
import static com.lambdasoup.watchlater.SettingsActivity.PREF_KEY_DEFAULT_ACCOUNT_NAME;


public class AddActivity extends Activity implements ErrorFragment.OnFragmentInteractionListener, AccountChooserFragment.OnFragmentInteractionListener {

	private static final String   SCOPE_YOUTUBE                    = "oauth2:https://www.googleapis.com/auth/youtube";
	private static final String   PERMISSION_GET_ACCOUNTS          = "android.permission.GET_ACCOUNTS";
	private static final int      PERMISSIONS_REQUEST_GET_ACCOUNTS = 100;
	private static final String   KEY_ACCOUNT                      = "com.lambdasoup.watchlater_account";
	private static final String   KEY_TOKEN                        = "com.lambdasoup.watchlater_token";
	private static final String   KEY_PLAYLIST_ID                  = "com.lambdasoup.watchlater_playlistId";
	private static final String   KEY_CHANNEL_TITLE                = "com.lambdasoup.watchlater_channelTitle";
	private static final String   KEY_RESULT                       = "com.lambdasoup.watchlater_result";
	// fields are not final to be somewhat accessible for testing to inject other values
	@SuppressWarnings({"FieldCanBeLocal", "CanBeFinal"})
	private static       String   YOUTUBE_ENDPOINT                 = "https://www.googleapis.com/youtube/v3";
	@SuppressWarnings("CanBeFinal")
	private static       String   ACCOUNT_TYPE_GOOGLE              = "com.google";
	@SuppressWarnings("CanBeFinal")
	private static       Executor OPTIONAL_RETROFIT_HTTP_EXECUTOR  = null;
	private AccountManager      manager;
	private YoutubeApi          api;
	private FragmentCoordinator fragmentCoordinator;
	private Account             account;
	private String              token;
	private String              playlistId;
	private String              channelTitle;
	private WatchlaterResult    result;
	private boolean             tokenRetried;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setDialogBehaviour();

		setContentView(R.layout.activity_add);

		fragmentCoordinator = new FragmentCoordinator();

		manager = AccountManager.get(this);
		setApiAdapter();

		if (savedInstanceState != null) {
			token = savedInstanceState.getString(KEY_TOKEN);
			playlistId = savedInstanceState.getString(KEY_PLAYLIST_ID);
			account = savedInstanceState.getParcelable(KEY_ACCOUNT);
			channelTitle = savedInstanceState.getString(KEY_CHANNEL_TITLE);
			result = savedInstanceState.getParcelable(KEY_RESULT);
		}

		if (getFragmentManager().findFragmentByTag(MainActivityMenuFragment.TAG) == null) {
			getFragmentManager().beginTransaction().add(MainActivityMenuFragment.newInstance(), MainActivityMenuFragment.TAG).commit();
		}
		addToWatchLaterAndShow();
	}

	@Override
	protected void onSaveInstanceState(@NonNull Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putString(KEY_TOKEN, token);
		outState.putString(KEY_PLAYLIST_ID, playlistId);
		outState.putParcelable(KEY_ACCOUNT, account);
		outState.putString(KEY_CHANNEL_TITLE, channelTitle);
		outState.putParcelable(KEY_RESULT, result);
	}

	private void setDialogBehaviour() {
		requestWindowFeature(Window.FEATURE_ACTION_BAR);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND,
				WindowManager.LayoutParams.FLAG_DIM_BEHIND);
		WindowManager.LayoutParams params = getWindow().getAttributes();
		params.height = WindowManager.LayoutParams.WRAP_CONTENT;
		params.width = getResources().getDimensionPixelSize(R.dimen.dialog_width);
		params.alpha = 1.0f;
		params.dimAmount = 0.5f;
		getWindow().setAttributes(params);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu_add, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.menu_open_with_youtube:
				openWithYoutube();
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}


	private void addToWatchLaterAndShow() {
		if (result != null) {
			// we're done, there will be no retry of this method
			result.apply(this::showSuccess, this::showError);
			return;
		}
		// still work to do and things to try

		if (account == null) {
			setGoogleAccountAndRetry();
			return;
		}

		if (token == null) {
			setAuthTokenAndRetry();
			return;
		}

		if (playlistId == null) {
			setPlaylistIdAndRetry();
			return;
		}

		insertPlaylistItemAndRetry();
	}

	private boolean supportsRuntimePermissions() {
		return (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1);
	}


	@TargetApi(23)
	private boolean hasAccountsPermission() {
		return checkSelfPermission(PERMISSION_GET_ACCOUNTS) == PackageManager.PERMISSION_GRANTED;
	}

	@TargetApi(23)
	private void tryAcquireAccountsPermission() {
		requestPermissions(new String[]{PERMISSION_GET_ACCOUNTS}, PERMISSIONS_REQUEST_GET_ACCOUNTS);
		fragmentCoordinator.showProgress();
	}

	@TargetApi(23)
	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		switch (requestCode) {
			case PERMISSIONS_REQUEST_GET_ACCOUNTS: {
				if (grantResults.length > 0
						&& grantResults[0] == PackageManager.PERMISSION_GRANTED) {
					addToWatchLaterAndShow();
				} else {
					onResult(WatchlaterResult.error(ErrorType.PERMISSION_REQUIRED_ACCOUNTS));
				}
				break;
			}
			default: {
				throw new RuntimeException("Unexpected permission request code: " + requestCode);
			}
		}
	}

	private String getVideoId() throws WatchlaterException {
		return getVideoId(getIntent().getData());
	}

	private String getVideoId(Uri uri) throws WatchlaterException {
		// e.g. https://www.youtube.com/watch?v=jqxENMKaeCU
		String videoId = uri.getQueryParameter("v");
		if (videoId != null) {
			return videoId;
		}

		// e.g.https://www.youtube.com/playlist?list=PLxLNk7y0uwqfXzUjcbVT3UuMjRd7pOv_U
		videoId = uri.getQueryParameter("list");
		if (videoId != null) {
			throw new WatchlaterException(ErrorType.NOT_A_VIDEO);
		}

		// e.g. http://www.youtube.com/attribution_link?u=/watch%3Fv%3DJ1zNbWJC5aw%26feature%3Dem-subs_digest
		if (!uri.getPathSegments().isEmpty() && "attribution_link".equals(uri.getPathSegments().get(0))) {
			String encodedUri = uri.getQueryParameter("u");
			if (encodedUri != null) {
				return getVideoId(parse(decode(encodedUri)));
			} else {
				throw new WatchlaterException(ErrorType.NOT_A_VIDEO);
			}
		}

		// e.g. http://www.youtube.com/v/OdT9z-JjtJk
		// http://www.youtube.com/embed/UkWd0azv3fQ
		// http://youtu.be/jqxENMKaeCU
		return uri.getLastPathSegment();
	}

	private void setAuthTokenAndRetry() {
		fragmentCoordinator.showProgress();
		manager.getAuthToken(account, SCOPE_YOUTUBE, null, this, future -> {
			try {
				token = future.getResult().getString(AccountManager.KEY_AUTHTOKEN);
				addToWatchLaterAndShow();
			} catch (OperationCanceledException e) {
				onResult(WatchlaterResult.error(ErrorType.NEED_ACCESS));
			} catch (IOException e) {
				onResult(WatchlaterResult.error(ErrorType.NETWORK));
			} catch (AuthenticatorException e) {
				onResult(WatchlaterResult.error(ErrorType.OTHER));
			}
		}, null);
	}

	private void setGoogleAccountAndRetry() {
		if (supportsRuntimePermissions()) {
			if (!hasAccountsPermission()) {
				tryAcquireAccountsPermission();
				return;
			}
		}
		Account[] accounts = manager.getAccountsByType(ACCOUNT_TYPE_GOOGLE);

		if (accounts.length == 0) {
			onResult(WatchlaterResult.error(ErrorType.NO_ACCOUNT));
			return;
		} else if (accounts.length != 1) {
			onMultipleAccounts();
			return;
		}

		onAccountChosen(accounts[0]);
	}


	private void onMultipleAccounts() {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		String defaultAccountName = prefs.getString(PREF_KEY_DEFAULT_ACCOUNT_NAME, null);
		Account[] accounts = manager.getAccountsByType(ACCOUNT_TYPE_GOOGLE);
		for (Account account : accounts) {
			if (account.name.equals(defaultAccountName)) {
				onAccountChosen(account);
				return;
			}
		}

		// no account set as default or default account not available any more
		if (defaultAccountName != null) { // out of date default account
			prefs.edit().remove(PREF_KEY_DEFAULT_ACCOUNT_NAME).apply();
		}

		fragmentCoordinator.showAccountChooser(accounts);
	}


	@Override
	public void onAccountChosen(Account account) {
		this.account = account;
		addToWatchLaterAndShow();
	}

	@Override
	public void onSetDefaultAccount(Account account) {
		PreferenceManager.getDefaultSharedPreferences(this).edit().putString(PREF_KEY_DEFAULT_ACCOUNT_NAME, account.name).apply();
	}

	private void insertPlaylistItemAndRetry() {
		fragmentCoordinator.showProgress();
		try {
			YoutubeApi.PlaylistItem.Snippet.ResourceId resourceId = new YoutubeApi.PlaylistItem.Snippet.ResourceId(getVideoId());
			YoutubeApi.PlaylistItem.Snippet snippet = new YoutubeApi.PlaylistItem.Snippet(playlistId, resourceId, null, null);
			YoutubeApi.PlaylistItem item = new YoutubeApi.PlaylistItem(snippet);

			api.insertPlaylistItem(item, new ErrorHandlingCallback<YoutubeApi.PlaylistItem>() {
				@Override
				public void success(YoutubeApi.PlaylistItem playlistItem, Response response) {
					onResult(WatchlaterResult.success(playlistItem.snippet.title, playlistItem.snippet.description));
				}
			});
		} catch (WatchlaterException error) {
			onResult(WatchlaterResult.error(error.type));
		}
	}

	private void onResult(WatchlaterResult result) {
		this.result = result;
		addToWatchLaterAndShow();
	}

	private void setApiAdapter() {
		Gson gson = new GsonBuilder().create();

		RestAdapter.Builder builder = new RestAdapter.Builder()
				.setClient(new OkClient())
				.setConverter(new GsonConverter(gson))
				.setRequestInterceptor(request -> request.addHeader("Authorization", "Bearer " + token))
				.setEndpoint(YOUTUBE_ENDPOINT);

		if (BuildConfig.DEBUG) {
			builder.setLogLevel(RestAdapter.LogLevel.FULL);
			if (OPTIONAL_RETROFIT_HTTP_EXECUTOR != null) {
				builder.setExecutors(OPTIONAL_RETROFIT_HTTP_EXECUTOR, new MainThreadExecutor());
			}
		}

		RestAdapter adapter = builder.build();
		api = adapter.create(YoutubeApi.class);
	}

	private CharSequence withChannelTitle(@StringRes int msgId) {
		return String.format(
				Locale.getDefault(),
				getResources().getString(msgId),
				channelTitle);
	}

	private void showError(ErrorResult errorResult) {
		if (isFinishing()) {
			showToast(withChannelTitle(errorResult.msgId));
			return;
		}

		fragmentCoordinator.showError();
	}

	private void showSuccess(@SuppressWarnings("UnusedParameters") SuccessResult successResult) {
		if (isFinishing()) {
			showToast(withChannelTitle(R.string.success_added_video));
			return;
		}
		fragmentCoordinator.showSuccess();
	}

	@SuppressWarnings("SameParameterValue")
	private void showToast(@StringRes int msgId) {
		showToast(getResources().getText(msgId));
	}

	private void showToast(CharSequence msg) {
		Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
	}

	public void onRetry() {
		result = null;
		addToWatchLaterAndShow();
	}

	public void onShowHelp() {
		startActivity(new Intent(this, HelpActivity.class));
	}

	private void openWithYoutube() {
		try {
			Intent intent = new Intent()
					.setData(getIntent().getData())
					.setPackage("com.google.android.youtube");
			startActivity(intent);
		} catch (ActivityNotFoundException e) {
			showToast(R.string.error_youtube_player_missing);
		}

	}

	private void setPlaylistIdAndRetry() {
		fragmentCoordinator.showProgress();
		api.listMyChannels(new ErrorHandlingCallback<YoutubeApi.Channels>() {
			@Override
			public void success(YoutubeApi.Channels channels, Response response) {
				if (channels.items.isEmpty()) {
					onResult(WatchlaterResult.error(ErrorType.ACCOUNT_HAS_NO_CHANNEL));
					return;
				}
				playlistId = channels.items.get(0).contentDetails.relatedPlaylists.watchLater;
				channelTitle = channels.items.get(0).snippet.title;
				if (channelTitle == null || channelTitle.isEmpty()) {
					channelTitle = account.name;
				}
				addToWatchLaterAndShow();
			}
		});
	}

	private void onTokenInvalid() {
		if (tokenRetried) {
			onResult(WatchlaterResult.error(ErrorType.NEED_ACCESS));
		}
		manager.invalidateAuthToken(account.type, token);
		token = null;
		tokenRetried = true;
		addToWatchLaterAndShow();
	}


	enum ErrorResult {
		ALREADY_IN_PLAYLIST(R.string.error_already_in_playlist),
		NEED_ACCESS(R.string.error_need_account, MoreErrorView.RETRY),
		NOT_A_VIDEO(R.string.error_not_a_video),
		OTHER(R.string.error_other, MoreErrorView.RETRY),
		PERMISSION_REQUIRED_ACCOUNTS(R.string.error_permission_required_accounts, MoreErrorView.RETRY),
		PLAYLIST_FULL(R.string.error_playlist_full, MoreErrorView.RETRY),
		VIDEO_NOT_FOUND(R.string.error_video_not_found),
		NO_ACCOUNT(R.string.error_no_account, MoreErrorView.RETRY),
		ACCOUNT_HAS_NO_CHANNEL(R.string.error_account_has_no_channel, MoreErrorView.RETRY, MoreErrorView.HELP_NO_CHANNEL);

		final int                msgId;
		final Set<MoreErrorView> additionalViews;

		ErrorResult(int msgId, @IdRes MoreErrorView... additionalViews) {
			this.additionalViews = additionalViews.length == 0 ? EnumSet.noneOf(MoreErrorView.class) : EnumSet.copyOf(Arrays.asList(additionalViews));
			this.msgId = msgId;
		}

		static ErrorResult fromErrorType(ErrorType errorType) {
			switch (errorType) {
				case ACCOUNT_HAS_NO_CHANNEL:
					return ACCOUNT_HAS_NO_CHANNEL;
				case ALREADY_IN_PLAYLIST:
					return ALREADY_IN_PLAYLIST;
				case NEED_ACCESS:
					return NEED_ACCESS;
				case NO_ACCOUNT:
					return NO_ACCOUNT;
				case NOT_A_VIDEO:
					return NOT_A_VIDEO;
				case OTHER:
				case NETWORK:
					return OTHER;
				case PERMISSION_REQUIRED_ACCOUNTS:
					return PERMISSION_REQUIRED_ACCOUNTS;
				case PLAYLIST_FULL:
					return PLAYLIST_FULL;
				case VIDEO_NOT_FOUND:
					return VIDEO_NOT_FOUND;
				default:
					throw new IllegalArgumentException("Unexpected error type: " + errorType);
			}
		}

		enum MoreErrorView {
			RETRY(R.id.button_retry),
			HELP_NO_CHANNEL(R.id.activetext_help_no_channel);

			final
			@IdRes
			int buttonId;

			MoreErrorView(@IdRes int buttonId) {
				this.buttonId = buttonId;
			}
		}
	}

	static class WatchlaterResult implements Parcelable {
		public static final Creator<WatchlaterResult> CREATOR = new Creator<WatchlaterResult>() {
			public WatchlaterResult createFromParcel(Parcel source) {
				return new WatchlaterResult(source);
			}

			public WatchlaterResult[] newArray(int size) {
				return new WatchlaterResult[size];
			}
		};
		private final SuccessResult success;
		private final ErrorResult   error;

		private WatchlaterResult(SuccessResult success, ErrorResult error) {
			if ((success == null) == (error == null)) {
				throw new IllegalArgumentException("Exactly one of success, error must be null");
			}
			this.success = success;
			this.error = error;
		}

		WatchlaterResult(Parcel in) {
			this.success = in.readParcelable(SuccessResult.class.getClassLoader());
			int tmpError = in.readInt();
			this.error = tmpError == -1 ? null : ErrorResult.values()[tmpError];
		}

		static WatchlaterResult success(String title, String description) {
			return new WatchlaterResult(new SuccessResult(title, description), null);
		}

		static WatchlaterResult error(ErrorType errorType) {
			return new WatchlaterResult(null, ErrorResult.fromErrorType(errorType));
		}

		boolean isSuccess() {
			return success != null;
		}

		void apply(VoidFunction<SuccessResult> onSuccess, VoidFunction<ErrorResult> onError) {
			if (isSuccess()) {
				onSuccess.apply(success);
			} else {
				onError.apply(error);
			}
		}

		@Override
		public String toString() {
			if (isSuccess()) {
				return "WatchlaterResult " + success;
			} else {
				return "WatchlaterResult " + error;
			}
		}

		@Override
		public int describeContents() {
			return 0;
		}

		@Override
		public void writeToParcel(Parcel dest, int flags) {
			dest.writeParcelable(this.success, 0);
			dest.writeInt(this.error == null ? -1 : this.error.ordinal());
		}

		interface VoidFunction<T> {
			void apply(T t);
		}
	}

	static class SuccessResult implements Parcelable {
		public static final Creator<SuccessResult> CREATOR = new Creator<SuccessResult>() {
			public SuccessResult createFromParcel(Parcel source) {
				return new SuccessResult(source);
			}

			public SuccessResult[] newArray(int size) {
				return new SuccessResult[size];
			}
		};
		final String title;
		final String description;


		SuccessResult(String title, String description) {
			this.title = title;
			this.description = description;
		}


		SuccessResult(Parcel in) {
			this.title = in.readString();
			this.description = in.readString();
		}

		@Override
		public String toString() {
			return "SuccessResult{" +
					"title='" + title + '\'' +
					", description='" + description + '\'' +
					'}';
		}

		@Override
		public int describeContents() {
			return 0;
		}

		@Override
		public void writeToParcel(Parcel dest, int flags) {
			dest.writeString(this.title);
			dest.writeString(this.description);
		}
	}

	private class WatchlaterException extends Exception {
		public final ErrorType type;

		@SuppressWarnings("SameParameterValue")
		public WatchlaterException(ErrorType type) {
			this.type = type;
		}
	}

	private abstract class ErrorHandlingCallback<T> extends ErrorTranslatingCallback<T> {
		@Override
		public void failure(ErrorType errorType) {
			switch (errorType) {
				case INVALID_TOKEN:
					onTokenInvalid();
					break;
				default:
					onResult(WatchlaterResult.error(errorType));
			}
		}
	}

	private class FragmentCoordinator {

		public void showProgress() {
			showFragment(ProgressFragment.newInstance());
		}

		public void showAccountChooser(Account[] accounts) {
			showFragment(AccountChooserFragment.newInstance(accounts));
		}

		public void showError() {
			showFragment(ErrorFragment.newInstance(channelTitle, result));
		}

		public void showSuccess() {
			showFragment(SuccessFragment.newInstance(channelTitle, result));
		}

		private void showFragment(Fragment fragment) {
			Fragment currentFragment = getFragmentManager().findFragmentById(R.id.fragment_container);
			if (currentFragment != null && currentFragment.getClass().equals(fragment.getClass())) {
				return;
			}
			getFragmentManager()
					.beginTransaction()
					.replace(R.id.fragment_container, fragment)
					.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
					.commit();
		}
	}
}
