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

package com.lambdasoup.watchlater.wlstatus;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import com.lambdasoup.watchlater.GoogleAccountAuthenticator;
import com.lambdasoup.watchlater.R;
import com.lambdasoup.watchlater.youtubeApi.PlaylistItem;
import com.lambdasoup.watchlater.youtubeApi.RetrofitFactory;
import com.lambdasoup.watchlater.youtubeApi.YoutubeApi;
import com.lambdasoup.watchlater.model.ErrorResult;
import com.lambdasoup.watchlater.model.InWlStatus;
import com.lambdasoup.watchlater.mvpbase.Presenter;
import com.lambdasoup.watchlater.youtubeApi.ErrorTranslatingCallback;
import com.lambdasoup.watchlater.youtubeApi.ErrorType;
import com.lambdasoup.watchlater.youtubeApi.PlaylistItemResponse;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;

/**
 * Created by jl on 13.07.16.
 */
public class WlStatusPresenter implements Presenter<WlStatusView> {
	private static final String TAG                 = WlStatusPresenter.class.getSimpleName();
	// TODO: dynamically fetch playlistId and channelTitle; use "WL" as fallback
	private static final String PLAYLIST_ID         = "WL";
	private static       String ACCOUNT_TYPE_GOOGLE = "com.google";

	private Context                    context;
	private String                     channelTitle;
	private Account                    account;
	private String                     videoId;
	private Retrofit                   retrofit;
	private YoutubeApi                 youtubeApi;
	private GoogleAccountAuthenticator authenticator;
	private WlStatusView               view;
	private InWlStatus inWlStatus = InWlStatus.UNKNOWN;
	private ErrorResult errorResult;
	private ErrorTranslatingCallback<PlaylistItemResponse> inWlStatusCallback;
	private ErrorTranslatingCallback<PlaylistItem> addToWlCallback;

	/**
	 * @param videoId
	 * @param context
	 */
	public WlStatusPresenter(String videoId, Context context) {
		this.videoId = videoId;
		this.context = context.getApplicationContext();
	}




	public void onEnsuredAccountsPermission() {
		ensureAccount();
	}

	private void ensureAccount() {
		if (account == null) {
			Account[] accounts = AccountManager.get(context).getAccountsByType(ACCOUNT_TYPE_GOOGLE);

			if (accounts.length == 0) {
				onError(ErrorResult.NO_ACCOUNT);
				return;
			} else if (accounts.length != 1) {
				Log.d(TAG, "multiple accounts not yet implemented, choosing first one");
				//			onMultipleAccounts();
				//			return;
			}
			account = accounts[0];
		}
		startLoadWlStatus();
	}

	private void ensureApiInitialized() {
		// TODO: move all the retrofit related stuff into its own object
		if (youtubeApi == null) {
			authenticator = new GoogleAccountAuthenticator(account);
			authenticator.init(context);
			OkHttpClient.Builder httpClientBuilder = new OkHttpClient.Builder()
					.authenticator(authenticator)
					.addInterceptor(authenticator);
			// TODO: remove
			httpClientBuilder.addNetworkInterceptor(chain -> {
				try {
					Thread.sleep(5000);
				} catch (InterruptedException e) {

				}
				return chain.proceed(chain.request());
			});
			RetrofitFactory.ApiAndRetrofit apiAndRetrofit = RetrofitFactory.getInstance().buildYoutubeApi(httpClientBuilder);
			retrofit = apiAndRetrofit.retrofit;
			youtubeApi = apiAndRetrofit.youtubeApi;
			inWlStatusCallback = new ErrorTranslatingCallback<PlaylistItemResponse>(retrofit) {
				@Override
				protected void failure(ErrorType errorType) {
					onError(ErrorResult.fromErrorType(errorType));
				}

				@Override
				protected void success(PlaylistItemResponse result) {
					InWlStatus inWlStatus = result.pageInfo.totalResults == 0 ? InWlStatus.NO : InWlStatus.YES;
					onWlStatusUpdated(inWlStatus);
				}
			};
			addToWlCallback = new ErrorTranslatingCallback<PlaylistItem>(retrofit){

				@Override
				protected void failure(ErrorType errorType) {
					if (errorType == ErrorType.ALREADY_IN_PLAYLIST) {
						onWlStatusUpdated(InWlStatus.YES);
					} else {
						// TODO: reset inWlStatus to previous value (might have been "unknown") ?
						onWlStatusUpdated(InWlStatus.NO);
						onError(ErrorResult.fromErrorType(errorType));
					}
				}

				@Override
				protected void success(PlaylistItem result) {
					onWlStatusUpdated(InWlStatus.YES);
				}
			};
		}
	}

	private void startLoadWlStatus() {
		ensureApiInitialized();
		onWlStatusUpdated(InWlStatus.UNKNOWN);
		// TODO: remove error?
		youtubeApi.getPlaylistItemInWl(videoId).enqueue(inWlStatusCallback);
	}

	public void onAddRequested() {
		ensureApiInitialized();
		onWlStatusUpdated(InWlStatus.ADDING);
		// TODO: remove error?
		PlaylistItem.Snippet snippet = new PlaylistItem.Snippet(PLAYLIST_ID, new PlaylistItem.Snippet.ResourceId(videoId), null, null);
		PlaylistItem playlistItem = new PlaylistItem(snippet);
		youtubeApi.insertPlaylistItem(playlistItem).enqueue(addToWlCallback);
	}

	private void onError(ErrorResult errorResult) {
		this.errorResult = errorResult;
		if (view != null) {
			view.onError(errorResult);
		} else {
			Toast.makeText(context, errorResult.msgId, Toast.LENGTH_LONG).show();
		}
	}

	private void onWlStatusUpdated(InWlStatus inWlStatus) {
		this.inWlStatus = inWlStatus;
		if (view != null) {
			view.onWlStatusUpdated(inWlStatus);
		} else if (inWlStatus == InWlStatus.YES) {
			Toast.makeText(context, R.string.success_added_video, Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	public void onViewAttached(WlStatusView view) {
		this.view = view;
		if (errorResult != null) {
			view.onError(errorResult);
		}
		if (inWlStatus != null) {
			view.onWlStatusUpdated(inWlStatus);
		}

		if (authenticator != null) {
			authenticator.attachActivity(view.getActivity());
		}

		if (account == null) {
			if (supportsRuntimePermissions()) {
				view.ensureAccountsPermission();
			} else {
				ensureAccount();
			}
		}
	}

	@Override
	public void onViewDetached() {
		this.view = null;
		if (authenticator != null) {
			authenticator.detachActivity();
		}
	}

	@Override
	public void onDestroyed() {
		// TODO: cancel running calls
	}

	private boolean supportsRuntimePermissions() {
		return (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1);
	}


}
