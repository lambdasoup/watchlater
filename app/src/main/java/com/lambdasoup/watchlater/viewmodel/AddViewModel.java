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

package com.lambdasoup.watchlater.viewmodel;

import android.accounts.Account;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.net.Uri;

import com.lambdasoup.watchlater.WatchLaterApplication;
import com.lambdasoup.watchlater.data.AccountRepository;
import com.lambdasoup.watchlater.data.YoutubeRepository;
import com.lambdasoup.watchlater.util.VideoIdParser;

public class AddViewModel extends WatchLaterViewModel
		implements AccountRepository.TokenCallback, YoutubeRepository.AddVideoCallback,
		YoutubeRepository.VideoInfoCallback {

	private final AccountRepository accountRepository;
	private final YoutubeRepository youtubeRepository;
	private final VideoIdParser     videoIdParser;

	private final MutableLiveData<Boolean> permissionNeeded = new MutableLiveData<>();
	private final LiveData<Account> account;
	private final MutableLiveData<VideoAdd>  videoAdd  = new MutableLiveData<>();
	private final MutableLiveData<VideoInfo> videoInfo = new MutableLiveData<>();

	private boolean tokenRetried;
	private String  videoId;

	@SuppressWarnings("WeakerAccess")
	public AddViewModel(WatchLaterApplication application) {
		super(application);

		accountRepository = application.getAccountRepository();
		account = accountRepository.get();

		youtubeRepository = application.getYoutubeRepository();
		videoIdParser = application.getVideoIdParser();

		videoAdd.setValue(VideoAdd.IDLE());
		videoInfo.setValue(new VideoInfo(VideoInfo.State.PROGRESS, null, null));
	}

	public LiveData<Account> getAccount() {
		return account;
	}

	public void setAccount(Account account) {
		accountRepository.put(account);
		videoAdd.setValue(VideoAdd.IDLE());
	}

	void removeAccount() {
		accountRepository.clear();
	}

	public void setVideoUri(Uri uri) {
		videoId = videoIdParser.parseVideoId(uri);
		youtubeRepository.getVideoInfo(videoId, this);
	}

	public LiveData<VideoInfo> getVideoInfo() {
		return videoInfo;
	}

	public LiveData<VideoAdd> getVideoAdd() {
		return videoAdd;
	}

	public LiveData<Boolean> getPermissionNeeded() {
		return permissionNeeded;
	}

	public void setPermissionNeeded(boolean needsPermission) {
		// only reset add state when permission state changes to positive
		Boolean oldValue = permissionNeeded.getValue();
		if (oldValue != null && oldValue == true && needsPermission == false) {
			videoAdd.setValue(VideoAdd.IDLE());
		}

		permissionNeeded.setValue(needsPermission);
	}

	public void watchLater() {
		videoAdd.setValue(VideoAdd.PROGRESS());

		if (account.getValue() == null) {
			videoAdd.setValue(VideoAdd.ERROR(VideoAdd.ErrorType.NO_ACCOUNT));
			return;
		}

		if (permissionNeeded.getValue() != null && permissionNeeded.getValue()) {
			videoAdd.setValue(VideoAdd.ERROR(VideoAdd.ErrorType.NO_PERMISSION));
			return;
		}

		accountRepository.getToken(this);
	}

	@Override
	public void onToken(boolean hasError, String token) {
		if (hasError) {
			videoAdd.setValue(VideoAdd.ERROR(VideoAdd.ErrorType.NO_ACCOUNT));
			return;
		}

		if (videoId == null) {
			throw new IllegalStateException("cannot query video without id");
		}

		youtubeRepository.addVideo(videoId, token, this);
	}

	@Override
	public void onAddResult(YoutubeRepository.ErrorType errorType, String token) {
		if (errorType == null) {
			videoAdd.setValue(VideoAdd.SUCCESS());
			return;
		}

		switch (errorType) {
			case INVALID_TOKEN:
				if (tokenRetried) {
					videoAdd.setValue(VideoAdd.ERROR(VideoAdd.ErrorType.OTHER));
					return;
				}
				tokenRetried = true;
				accountRepository.invalidateToken(token);
				accountRepository.getToken(this);
				return;

			case ALREADY_IN_PLAYLIST:
				videoAdd.setValue(VideoAdd.ERROR(VideoAdd.ErrorType.YOUTUBE_ALREADY_IN_PLAYLIST));
				return;

			default:
				videoAdd.setValue(VideoAdd.ERROR(VideoAdd.ErrorType.OTHER));
		}
	}

	@Override
	public void onVideoInfoResult(YoutubeRepository.ErrorType errorType, YoutubeRepository.Videos.Item item) {
		if (errorType != null) {
			videoInfo.setValue(new VideoInfo(VideoInfo.State.ERROR, null, errorType));
		} else {
			videoInfo.setValue(new VideoInfo(VideoInfo.State.LOADED, item, null));
		}
	}

	public static class VideoAdd {

		public final State     state;
		public final ErrorType errorType;

		private VideoAdd(State state, ErrorType errorType) {
			this.state = state;
			this.errorType = errorType;
		}

		public static VideoAdd SUCCESS() {
			return new VideoAdd(State.SUCCESS, null);
		}

		public static VideoAdd ERROR(ErrorType errorType) {
			return new VideoAdd(State.ERROR, errorType);
		}

		static VideoAdd IDLE() {
			return new VideoAdd(State.IDLE, null);
		}

		static VideoAdd PROGRESS() {
			return new VideoAdd(State.PROGRESS, null);
		}

		public enum State {
			IDLE, PROGRESS, SUCCESS, ERROR
		}

		public enum ErrorType {
			OTHER, NO_ACCOUNT, NO_PERMISSION, YOUTUBE_ALREADY_IN_PLAYLIST
		}
	}

	public static class VideoInfo {

		public final State                         state;
		public final YoutubeRepository.Videos.Item data;
		public final YoutubeRepository.ErrorType   error;

		public VideoInfo(State state, YoutubeRepository.Videos.Item data, YoutubeRepository.ErrorType error) {
			this.state = state;
			this.data = data;
			this.error = error;
		}

		public enum State {
			PROGRESS, LOADED, ERROR
		}

	}

}