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

package com.lambdasoup.watchlater;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.lambdasoup.watchlater.YoutubeApi.ErrorTranslatingCallback;
import com.lambdasoup.watchlater.YoutubeApi.ErrorType;

import java.io.IOException;
import java.util.concurrent.Executor;

import retrofit.RestAdapter;
import retrofit.android.MainThreadExecutor;
import retrofit.client.OkClient;
import retrofit.client.Response;
import retrofit.converter.GsonConverter;

import static android.net.Uri.decode;
import static android.net.Uri.parse;


public class AddActivity extends Activity {
	private static final String TAG = AddActivity.class.getSimpleName();

	private static final String SCOPE_YOUTUBE = "oauth2:https://www.googleapis.com/auth/youtube";


	// fields are not final to be somewhat accessible for testing to inject other values
	@SuppressWarnings("FieldCanBeLocal")
	private static String YOUTUBE_ENDPOINT = "https://www.googleapis.com/youtube/v3";
	private static String ACCOUNT_TYPE_GOOGLE = "com.google";
	private static Executor OPTIONAL_RETROFIT_HTTP_EXECUTOR = null;

	private AccountManager manager;
	private YoutubeApi api;

	private WatchlaterDialogContent mainContent;

	private static final String KEY_ACCOUNT = "com.lambdasoup.watchlater_account";
	private static final String KEY_TOKEN = "com.lambdasoup.watchlater_token";
	private static final String KEY_PLAYLIST_ID = "com.lambdasoup.watchlater_playlistId";
	private static final String KEY_RESULT = "com.lambdasoup.watchlater_result";

	private Account account;
	private String token;
	private String playlistId;
	private WatchlaterResult result;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setDialogBehaviour();

		setContentView(R.layout.activity_add);

		mainContent = (WatchlaterDialogContent) findViewById(R.id.progress_animator);

		manager = AccountManager.get(this);
		setApiAdapter();

		if (savedInstanceState != null) {
			token = savedInstanceState.getString(KEY_TOKEN);
			playlistId = savedInstanceState.getString(KEY_PLAYLIST_ID);
			account = savedInstanceState.getParcelable(KEY_ACCOUNT);
			result = savedInstanceState.getParcelable(KEY_RESULT);
		}
		addToWatchLaterAndShow();
	}

	@Override
	protected void onSaveInstanceState(@NonNull Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putString(KEY_TOKEN, token);
		outState.putString(KEY_PLAYLIST_ID, playlistId);
		outState.putParcelable(KEY_ACCOUNT, account);
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
			case R.id.menu_about:
				showAbout();
				return true;
			case R.id.menu_open_with_youtube:
				openWithYoutube();
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	private void showAbout() {
		startActivity(new Intent(this, AboutActivity.class));
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
		mainContent.showProgress();
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
		Account[] accounts = manager.getAccountsByType(ACCOUNT_TYPE_GOOGLE);

		if (accounts.length != 1) {
			onMultipleAccounts();
			return;
		}

		this.account = accounts[0];
		addToWatchLaterAndShow();
	}

	private void onMultipleAccounts() {
		final ListView listView = (ListView) findViewById(R.id.account_list);
		View header = getLayoutInflater().inflate(R.layout.list_header_account_chooser, listView, false);
		listView.addHeaderView(header);
		listView.setEmptyView(findViewById(R.id.account_chooser_empty));

		final ArrayAdapter<Account> adapter = new ArrayAdapter<Account>(this, R.layout.item_account, manager.getAccountsByType(ACCOUNT_TYPE_GOOGLE)) {
			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				TextView accountName;
				if (convertView != null) {
					accountName = (TextView) convertView;
				} else {
					accountName = (TextView) getLayoutInflater().inflate(R.layout.item_account, parent, false);
				}
				accountName.setText(getItem(position).name);
				return accountName;
			}
		};
		listView.setAdapter(adapter);
		listView.setOnItemClickListener((parent, view, position, id) ->
				onAccountChosen(adapter.getItem(position - listView.getHeaderViewsCount()))
				);

		mainContent.showAccountChooser();
	}

	private void onAccountChosen(Account account) {
		this.account = account;
		addToWatchLaterAndShow();
	}


	private void insertPlaylistItemAndRetry() {
		mainContent.showProgress();
		try {
			YoutubeApi.ResourceId resourceId = new YoutubeApi.ResourceId(getVideoId());
			YoutubeApi.Snippet snippet = new YoutubeApi.Snippet(playlistId, resourceId, null, null);
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

	private void showError(ErrorResult errorResult) {
		if (isFinishing()) {
			showToast(errorResult.msgId);
			return;
		}

		mainContent.showError(errorResult);
	}

	private void showSuccess(SuccessResult successResult) {
		if (isFinishing()) {
			showToast(R.string.success_added_video);
			return;
		}
		mainContent.showSuccess(successResult);
	}



	private void showToast(int msgId) {
		Toast.makeText(this, msgId, Toast.LENGTH_SHORT).show();
	}

	public void onRetry(View v) {
		result = null;
		addToWatchLaterAndShow();
	}

	public void openWithYoutube() {
		try {
			Intent intent = new Intent()
					.setData(getIntent().getData())
					.setPackage("com.google.android.youtube");
			startActivity(intent);
		} catch (ActivityNotFoundException e) {
			showToast(R.string.error_youtube_player_missing);
		}

	};

	private void setPlaylistIdAndRetry() {
		mainContent.showProgress();
		api.listMyChannels(new ErrorHandlingCallback<YoutubeApi.Channels>() {
			@Override
			public void success(YoutubeApi.Channels channels, Response response) {
				playlistId = channels.items.get(0).contentDetails.relatedPlaylists.watchLater;
				addToWatchLaterAndShow();
			}
		});
	}

	private void onTokenInvalid() {
		manager.invalidateAuthToken(account.type, token);
		token = null;
		addToWatchLaterAndShow();
	}


	private class WatchlaterException extends Exception {
		public final ErrorType type;

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

	static class WatchlaterResult implements Parcelable {
		private final SuccessResult success;
		private final ErrorResult error;

		private WatchlaterResult(SuccessResult success, ErrorResult error) {
			if ((success == null) == (error == null)) {
				throw new IllegalArgumentException("Exactly one of success, error must be null");
			}
			this.success = success;
			this.error = error;
		}

		boolean isSuccess() {
			return success != null;
		}

		boolean isError() {
			return error != null;
		}

		interface VoidFunction<T> {
			void apply(T t);
		}

		void apply(VoidFunction<SuccessResult> onSuccess, VoidFunction<ErrorResult> onError) {
			if (isSuccess()) {
				onSuccess.apply(success);
			} else {
				onError.apply(error);
			}
		}

		static WatchlaterResult success(String title, String description) {
			return new WatchlaterResult(new SuccessResult(title, description), null);
		}

		static WatchlaterResult error(ErrorType errorType) {
			return new WatchlaterResult(null, ErrorResult.fromErrorType(errorType));
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

		protected WatchlaterResult(Parcel in) {
			this.success = in.readParcelable(SuccessResult.class.getClassLoader());
			int tmpError = in.readInt();
			this.error = tmpError == -1 ? null : ErrorResult.values()[tmpError];
		}

		public static final Creator<WatchlaterResult> CREATOR = new Creator<WatchlaterResult>() {
			public WatchlaterResult createFromParcel(Parcel source) {
				return new WatchlaterResult(source);
			}

			public WatchlaterResult[] newArray(int size) {
				return new WatchlaterResult[size];
			}
		};
	}


	static class SuccessResult implements Parcelable {
		final String title;
		final String description;

		SuccessResult(String title, String description) {
			this.title = title;
			this.description = description;
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

		protected SuccessResult(Parcel in) {
			this.title = in.readString();
			this.description = in.readString();
		}

		public static final Creator<SuccessResult> CREATOR = new Creator<SuccessResult>() {
			public SuccessResult createFromParcel(Parcel source) {
				return new SuccessResult(source);
			}

			public SuccessResult[] newArray(int size) {
				return new SuccessResult[size];
			}
		};
	}

	enum ErrorResult {
		ALREADY_IN_PLAYLIST(R.string.error_already_in_playlist, false),
		NEED_ACCESS(R.string.error_need_account, true),
		NOT_A_VIDEO(R.string.error_not_a_video, false),
		OTHER(R.string.error_other, true),
		PLAYLIST_FULL(R.string.error_playlist_full, true),
		VIDEO_NOT_FOUND(R.string.error_video_not_found, false);

		final int     msgId;
		final boolean allowRetry;

		ErrorResult(int msgId, boolean allowRetry) {
			this.allowRetry = allowRetry;
			this.msgId = msgId;
		}

		static ErrorResult fromErrorType(ErrorType errorType) {
			switch (errorType) {
				case ALREADY_IN_PLAYLIST:
					return ALREADY_IN_PLAYLIST;
				case NEED_ACCESS:
					return NEED_ACCESS;
				case NOT_A_VIDEO:
					return NOT_A_VIDEO;
				case OTHER:
				case NETWORK:
					return OTHER;
				case PLAYLIST_FULL:
					return PLAYLIST_FULL;
				case VIDEO_NOT_FOUND:
					return VIDEO_NOT_FOUND;
				default:
					throw new IllegalArgumentException("Unexpected error type: " + errorType);
			}
		}


	}
}
