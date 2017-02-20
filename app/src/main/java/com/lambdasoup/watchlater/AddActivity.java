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
import android.app.Activity;

import com.lambdasoup.watchlater.youtubeApi.YoutubeApi;

import retrofit2.Retrofit;


public class AddActivity extends Activity {

	private static final String PERMISSION_GET_ACCOUNTS          = "android.permission.GET_ACCOUNTS";
	private static final int    PERMISSIONS_REQUEST_GET_ACCOUNTS = 100;
	private static final String KEY_ACCOUNT                      = "com.lambdasoup.watchlater_account";
	private static final String KEY_TOKEN                        = "com.lambdasoup.watchlater_token";
	private static final String KEY_PLAYLIST_ID                  = "com.lambdasoup.watchlater_playlistId";
	private static final String KEY_CHANNEL_TITLE                = "com.lambdasoup.watchlater_channelTitle";
	private static final String KEY_RESULT                       = "com.lambdasoup.watchlater_result";
	private static final String TAG                              = AddActivity.class.getSimpleName();
	private static final String KEY_AUTHENTICATOR                = "com.lambdasoup.watchlater_authenticator";
	// fields are not final to be somewhat accessible for testing to inject other values
	@SuppressWarnings({"FieldCanBeLocal", "CanBeFinal"})
	private static       String YOUTUBE_ENDPOINT                 = "https://www.googleapis.com/youtube/v3/";
	@SuppressWarnings("CanBeFinal")
	private static       String ACCOUNT_TYPE_GOOGLE              = "com.google";

	private AccountManager             manager;
	private Retrofit                   retrofit;
	private YoutubeApi                 api;
	//	private FragmentCoordinator        fragmentCoordinator;
	private Account                    account;
	private String                     playlistId;
	private String                     channelTitle;
	private WatchlaterResult           result;
	private GoogleAccountAuthenticator authenticator;

//	@Override
//	protected void onCreate(Bundle savedInstanceState) {
//		super.onCreate(savedInstanceState);
//
//		setDialogBehaviour();
//
//		setContentView(R.layout.activity_add_old);
//
//		fragmentCoordinator = new FragmentCoordinator();
//
//		manager = AccountManager.get(this);
//
//		if (savedInstanceState != null) {
//			authenticator = savedInstanceState.getParcelable(KEY_AUTHENTICATOR);
//			//noinspection ConstantConditions
//			authenticator.init(this);
//			playlistId = savedInstanceState.getString(KEY_PLAYLIST_ID);
//			setAccount(savedInstanceState.getParcelable(KEY_ACCOUNT));
//			channelTitle = savedInstanceState.getString(KEY_CHANNEL_TITLE);
//			result = savedInstanceState.getParcelable(KEY_RESULT);
//		}
//
//		setApiAdapter();
//
//		if (getFragmentManager().findFragmentByTag(MainActivityMenuFragment.TAG) == null) {
//			getFragmentManager().beginTransaction().add(MainActivityMenuFragment.newInstance(), MainActivityMenuFragment.TAG).commit();
//		}
//		addToWatchLaterAndShow();
//	}
//
//	@Override
//	protected void onSaveInstanceState(@NonNull Bundle outState) {
//		super.onSaveInstanceState(outState);
//		outState.putParcelable(KEY_AUTHENTICATOR, authenticator);
//		outState.putString(KEY_PLAYLIST_ID, playlistId);
//		outState.putParcelable(KEY_ACCOUNT, account);
//		outState.putString(KEY_CHANNEL_TITLE, channelTitle);
//		outState.putParcelable(KEY_RESULT, result);
//	}
//
//	private void setDialogBehaviour() {
//		requestWindowFeature(Window.FEATURE_ACTION_BAR);
//		getWindow().setFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND,
//				WindowManager.LayoutParams.FLAG_DIM_BEHIND);
//		WindowManager.LayoutParams params = getWindow().getAttributes();
//		params.height = WindowManager.LayoutParams.WRAP_CONTENT;
//		params.width = getResources().getDimensionPixelSize(R.dimen.dialog_width);
//		params.alpha = 1.0f;
//		params.dimAmount = 0.5f;
//		getWindow().setAttributes(params);
//	}
//
//	@Override
//	public boolean onCreateOptionsMenu(Menu menu) {
//		MenuInflater inflater = getMenuInflater();
//		inflater.inflate(R.menu.menu_add, menu);
//		return super.onCreateOptionsMenu(menu);
//	}
//
//	@Override
//	public boolean onOptionsItemSelected(MenuItem item) {
//		switch (item.getItemId()) {
//			case R.id.menu_open_with_youtube:
//				openWithYoutube();
//				return true;
//			default:
//				return super.onOptionsItemSelected(item);
//		}
//	}
//
//
//	private void addToWatchLaterAndShow() {
//		if (result != null) {
//			// we're done, there will be no retry of this method
//			result.apply(fragmentCoordinator::showSuccess, fragmentCoordinator::showError);
//			return;
//		}
//		// still work to do and things to try
//
//		if (account == null) {
//			setGoogleAccountAndRetry();
//			return;
//		}
//
//
//		if (playlistId == null) {
//			setPlaylistIdAndRetry();
//			return;
//		}
//
//		insertPlaylistItemAndRetry();
//	}
//
//	private boolean supportsRuntimePermissions() {
//		return (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1);
//	}
//
//
//	@TargetApi(23)
//	private boolean hasAccountsPermission() {
//		return checkSelfPermission(PERMISSION_GET_ACCOUNTS) == PackageManager.PERMISSION_GRANTED;
//	}
//
//	@TargetApi(23)
//	private void tryAcquireAccountsPermission() {
//		requestPermissions(new String[]{PERMISSION_GET_ACCOUNTS}, PERMISSIONS_REQUEST_GET_ACCOUNTS);
//		fragmentCoordinator.showProgress();
//	}
//
//	@TargetApi(23)
//	@Override
//	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//		switch (requestCode) {
//			case PERMISSIONS_REQUEST_GET_ACCOUNTS: {
//				if (grantResults.length > 0
//						&& grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//					addToWatchLaterAndShow();
//				} else {
//					onResult(WatchlaterResult.error(ErrorType.PERMISSION_REQUIRED_ACCOUNTS));
//				}
//				break;
//			}
//			default: {
//				throw new RuntimeException("Unexpected permission request code: " + requestCode);
//			}
//		}
//	}
//
//	private String getVideoId() throws WatchlaterException {
//		return getVideoId(getIntent().getData());
//	}
//
//	private String getVideoId(Uri uri) throws WatchlaterException {
//		// e.g. vnd.youtube:jqxENMKaeCU
//		if (uri.isOpaque()) {
//			return uri.getSchemeSpecificPart();
//		}
//
//		// e.g. https://www.youtube.com/watch?v=jqxENMKaeCU
//		String videoId = uri.getQueryParameter("v");
//		if (videoId != null) {
//			return videoId;
//		}
//
//		// e.g.https://www.youtube.com/playlist?list=PLxLNk7y0uwqfXzUjcbVT3UuMjRd7pOv_U
//		videoId = uri.getQueryParameter("list");
//		if (videoId != null) {
//			throw new WatchlaterException(ErrorType.NOT_A_VIDEO);
//		}
//
//		// e.g. http://www.youtube.com/attribution_link?u=/watch%3Fv%3DJ1zNbWJC5aw%26feature%3Dem-subs_digest
//		if (!uri.getPathSegments().isEmpty() && "attribution_link".equals(uri.getPathSegments().get(0))) {
//			String encodedUri = uri.getQueryParameter("u");
//			if (encodedUri != null) {
//				return getVideoId(parse(decode(encodedUri)));
//			} else {
//				throw new WatchlaterException(ErrorType.NOT_A_VIDEO);
//			}
//		}
//
//		// e.g. http://www.youtube.com/v/OdT9z-JjtJk
//		// http://www.youtube.com/embed/UkWd0azv3fQ
//		// http://youtu.be/jqxENMKaeCU
//		return uri.getLastPathSegment();
//	}
//
//
//	private void setAccount(Account account) {
//		this.account = account;
//		//authenticator.setAccount(account);
//	}
//
////	private void setAuthTokenAndRetry() {
////		fragmentCoordinator.showProgress();
////		manager.getAuthToken(account, SCOPE_YOUTUBE, null, this, future -> {
////			try {
////				setToken(future.getResult().getString(AccountManager.KEY_AUTHTOKEN));
////				addToWatchLaterAndShow();
////			} catch (OperationCanceledException e) {
////				onResult(WatchlaterResult.error(ErrorType.NEED_ACCESS));
////			} catch (IOException e) {
////				onResult(WatchlaterResult.error(ErrorType.NETWORK));
////			} catch (AuthenticatorException e) {
////				onResult(WatchlaterResult.error(ErrorType.OTHER));
////			}
////		}, null);
////	}
//
//	private void setGoogleAccountAndRetry() {
//		if (supportsRuntimePermissions()) {
//			if (!hasAccountsPermission()) {
//				tryAcquireAccountsPermission();
//				return;
//			}
//		}
//		Account[] accounts = manager.getAccountsByType(ACCOUNT_TYPE_GOOGLE);
//
//		if (accounts.length == 0) {
//			onResult(WatchlaterResult.error(ErrorType.NO_ACCOUNT));
//			return;
//		} else if (accounts.length != 1) {
//			onMultipleAccounts();
//			return;
//		}
//
//		onAccountChosen(accounts[0]);
//	}
//
//
//	private void onMultipleAccounts() {
//		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
//		String defaultAccountName = prefs.getString(PREF_KEY_DEFAULT_ACCOUNT_NAME, null);
//		Account[] accounts = manager.getAccountsByType(ACCOUNT_TYPE_GOOGLE);
//		for (Account account : accounts) {
//			if (account.name.equals(defaultAccountName)) {
//				onAccountChosen(account);
//				return;
//			}
//		}
//
//		// no account set as default or default account not available any more
//		if (defaultAccountName != null) { // out of date default account
//			prefs.edit().remove(PREF_KEY_DEFAULT_ACCOUNT_NAME).apply();
//		}
//
//		fragmentCoordinator.showAccountChooser(accounts);
//	}
//
//
//	@Override
//	public void onAccountChosen(Account account) {
//		setAccount(account);
//		addToWatchLaterAndShow();
//	}
//
//	@Override
//	public void onSetDefaultAccount(Account account) {
//		PreferenceManager.getDefaultSharedPreferences(this).edit().putString(PREF_KEY_DEFAULT_ACCOUNT_NAME, account.name).apply();
//	}
//
//	private void insertPlaylistItemAndRetry() {
//		fragmentCoordinator.showProgress();
//		try {
//			PlaylistItem.Snippet.ResourceId resourceId = new PlaylistItem.Snippet.ResourceId(getVideoId());
//			PlaylistItem.Snippet snippet = new PlaylistItem.Snippet(playlistId, resourceId, null, null);
//			PlaylistItem item = new PlaylistItem(snippet);
//
//			api.insertPlaylistItem(item).enqueue(new ErrorHandlingCallback<PlaylistItem>() {
//				@Override
//				public void success(YoutubeApi.PlaylistItem playlistItem) {
//					onResult(WatchlaterResult.success(playlistItem.snippet.title, playlistItem.snippet.description));
//				}
//			});
//		} catch (WatchlaterException error) {
//			onResult(WatchlaterResult.error(error.type));
//		}
//	}
//
//	private void onResult(WatchlaterResult result) {
//		this.result = result;
//		addToWatchLaterAndShow();
//	}
//
//	private void setApiAdapter() {
////		if (authenticator == null) {
////			authenticator = new GoogleAccountAuthenticator();
////			authenticator.init(this);
////		}
////		OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
////		httpClient.interceptors().add(authenticator);
////		httpClient.authenticator(authenticator);
////
////		if (BuildConfig.DEBUG) {
////			HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
////			loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
////			httpClient.networkInterceptors().add(loggingInterceptor);
////
////			if (OPTIONAL_RETROFIT_HTTP_EXECUTOR != null) {
////				httpClient.dispatcher(new Dispatcher(OPTIONAL_RETROFIT_HTTP_EXECUTOR));
////			}
////		}
////
////		Retrofit.Builder retrofitBuilder = new Retrofit.Builder()
////				.baseUrl(YOUTUBE_ENDPOINT)
////				.addConverterFactory(GsonConverterFactory.create())
////				.client(httpClient.build());
////
////		retrofit = retrofitBuilder.build();
////		api = retrofit.create(YoutubeApi.class);
//
//	}
//
//
//	private CharSequence withChannelTitle(@StringRes int msgId) {
//		return String.format(
//				Locale.getDefault(),
//				getResources().getString(msgId),
//				channelTitle);
//	}
//
//
//	@SuppressWarnings("SameParameterValue")
//	private void showToast(@StringRes int msgId) {
//		showToast(getResources().getText(msgId));
//	}
//
//	private void showToast(CharSequence msg) {
//		Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
//	}
//
//	public void onRetry() {
//		result = null;
//		addToWatchLaterAndShow();
//	}
//
//	public void onShowHelp() {
//		startActivity(new Intent(this, HelpActivity.class));
//	}
//
//	private void openWithYoutube() {
//		try {
//			Intent intent = new Intent()
//					.setData(getIntent().getData())
//					.setPackage("com.google.android.youtube");
//			startActivity(intent);
//		} catch (ActivityNotFoundException e) {
//			showToast(R.string.error_youtube_player_missing);
//		}
//
//	}
//
//	private void setPlaylistIdAndRetry() {
//		fragmentCoordinator.showProgress();
//		api.listMyChannels().enqueue(new ErrorHandlingCallback<YoutubeApi.Channels>() {
//			@Override
//			public void success(YoutubeApi.Channels channels) {
//				if (channels.items.isEmpty()) {
//					onResult(WatchlaterResult.error(ErrorType.ACCOUNT_HAS_NO_CHANNEL));
//					return;
//				}
//				playlistId = channels.items.get(0).contentDetails.relatedPlaylists.watchLater;
//				channelTitle = channels.items.get(0).snippet.title;
//				if (channelTitle == null || channelTitle.isEmpty()) {
//					channelTitle = account.name;
//				}
//				addToWatchLaterAndShow();
//			}
//		});
//	}
//
//
//	private class WatchlaterException extends Exception {
//		public final ErrorType type;
//
//		@SuppressWarnings("SameParameterValue")
//		public WatchlaterException(ErrorType type) {
//			this.type = type;
//		}
//	}
//
//	private abstract class ErrorHandlingCallback<T> extends ErrorTranslatingCallback<T> {
//		protected ErrorHandlingCallback() {
//			super(retrofit);
//		}
//
//		@Override
//		public void failure(ErrorType errorType) {
//			switch (errorType) {
//				case INVALID_TOKEN:
//					// TODO: does this occur? handle it.
//					Log.d(TAG, "Failed with invalid token!");
//					break;
//				default:
//					onResult(WatchlaterResult.error(errorType));
//			}
//		}
//	}
//
//	private class FragmentCoordinator {
//
//		public void showProgress() {
//			showFragment(ProgressFragment.newInstance());
//		}
//
//		public void showAccountChooser(Account[] accounts) {
//			showFragment(AccountChooserFragment.newInstance(accounts));
//		}
//
//		public void showError(ErrorResult errorResult) {
//			if (isFinishing()) {
//				showToast(withChannelTitle(errorResult.msgId));
//				return;
//			}
//			showFragment(ErrorFragment.newInstance(channelTitle, errorResult));
//		}
//
//		public void showSuccess(SuccessResult successResult) {
//			if (isFinishing()) {
//				showToast(withChannelTitle(R.string.success_added_video));
//				return;
//			}
//			showFragment(SuccessFragment.newInstance(channelTitle, successResult));
//		}
//
//		private void showFragment(Fragment fragment) {
//			if (isFinishing()) {
//				return;
//			}
//			Fragment currentFragment = getFragmentManager().findFragmentById(R.id.fragment_container);
//			if (currentFragment != null && currentFragment.getClass().equals(fragment.getClass())) {
//				return;
//			}
//			getFragmentManager()
//					.beginTransaction()
//					.replace(R.id.fragment_container, fragment)
//					.setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out)
//					.commitAllowingStateLoss();
//		}
//	}
}
