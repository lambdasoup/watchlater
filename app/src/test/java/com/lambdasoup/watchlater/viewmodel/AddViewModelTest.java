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
import android.content.Intent;
import android.net.Uri;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.lambdasoup.watchlater.WatchLaterApplication;
import com.lambdasoup.watchlater.data.AccountRepository;
import com.lambdasoup.watchlater.data.YoutubeRepository;
import com.lambdasoup.watchlater.util.VideoIdParser;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AddViewModelTest {

	@Rule
	public TestRule rule = new InstantTaskExecutorRule();

	@Mock
	private WatchLaterApplication application;

	@Mock
	private AccountRepository accountRepository;

	@Mock
	private VideoIdParser videoIdParser;

	@Mock
	private YoutubeRepository youtubeRepository;

	private AddViewModel viewModel;

	private MutableLiveData<Account> accountLiveData;

	@Before
	public void setup() {
		when(application.getAccountRepository()).thenReturn(accountRepository);
		when(application.getVideoIdParser()).thenReturn(videoIdParser);
		when(application.getYoutubeRepository()).thenReturn(youtubeRepository);

		accountLiveData = new MutableLiveData<>();
		when(accountRepository.get()).thenReturn(accountLiveData);

		viewModel = new AddViewModel(application);
	}

	@Test
	public void should_set_account() {
		Account account = mock(Account.class);

		viewModel.setAccount(account);

		verify(accountRepository).put(account);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void should_get_account() {
		LiveData<Account> liveData = mock(LiveData.class);
		when(accountRepository.get()).thenReturn(liveData);

		AddViewModel viewModel = new AddViewModel(application);
		LiveData<Account> defaultAccount = viewModel.getAccount();

		assertThat(defaultAccount).isEqualTo(liveData);
	}

	@Test
	public void should_remove_account() {
		viewModel.removeAccount();

		verify(accountRepository).clear();
	}

	@Test
	public void should_set_uri() {
		Uri uri = mock(Uri.class);
		when(videoIdParser.parseVideoId(uri)).thenReturn("test");

		viewModel.setVideoUri(uri);
	}

	@Test
	public void should_set_permission_needed() {
		viewModel.setPermissionNeeded(true);
		assertThat(viewModel.getPermissionNeeded().getValue()).isEqualTo(true);
	}

	@Test
	public void should_set_add_progress_when_permission_to_true() {
		viewModel.setPermissionNeeded(true);

		viewModel.setPermissionNeeded(false);

		AddViewModel.VideoAdd videoAdd = viewModel.getVideoAdd().getValue();
		assertThat(videoAdd).isNotNull();
		assertThat(videoAdd.state).isEqualTo(AddViewModel.VideoAdd.State.IDLE);
	}

	@Test
	public void should_error_when_watchlater_without_account() {
		viewModel.watchLater();

		AddViewModel.VideoAdd videoAdd = viewModel.getVideoAdd().getValue();
		assertThat(videoAdd).isNotNull();
		assertThat(videoAdd.state).isEqualTo(AddViewModel.VideoAdd.State.ERROR);
		assertThat(videoAdd.errorType).isEqualTo(AddViewModel.VideoAdd.ErrorType.NO_ACCOUNT);
	}

	@Test
	public void should_error_when_watchlater_without_permissions() {
		accountLiveData.setValue(mock(Account.class));
		viewModel.setPermissionNeeded(true);

		viewModel.watchLater();

		AddViewModel.VideoAdd videoAdd = viewModel.getVideoAdd().getValue();
		assertThat(videoAdd).isNotNull();
		assertThat(videoAdd.state).isEqualTo(AddViewModel.VideoAdd.State.ERROR);
		assertThat(videoAdd.errorType).isEqualTo(AddViewModel.VideoAdd.ErrorType.NO_PERMISSION);
	}

	@Test
	public void should_request_token_when_watchlater() {
		accountLiveData.setValue(mock(Account.class));
		viewModel.setPermissionNeeded(false);

		viewModel.watchLater();

		AddViewModel.VideoAdd videoAdd = viewModel.getVideoAdd().getValue();
		assertThat(videoAdd).isNotNull();
		assertThat(videoAdd.state).isEqualTo(AddViewModel.VideoAdd.State.PROGRESS);
		verify(accountRepository).getToken(viewModel);
	}

	@Test
	public void should_error_when_token_result_has_error() {
		viewModel.onToken(true, null, null);

		AddViewModel.VideoAdd videoAdd = viewModel.getVideoAdd().getValue();
		assertThat(videoAdd).isNotNull();
		assertThat(videoAdd.state).isEqualTo(AddViewModel.VideoAdd.State.ERROR);
		assertThat(videoAdd.errorType).isEqualTo(AddViewModel.VideoAdd.ErrorType.NO_ACCOUNT);
	}

	@Test
	public void should_intent_when_token_result_has_intent() {
		Intent intent = mock(Intent.class);
		viewModel.onToken(true, null, intent);

		AddViewModel.VideoAdd videoAdd = viewModel.getVideoAdd().getValue();
		assertThat(videoAdd).isNotNull();
		assertThat(videoAdd.state).isEqualTo(AddViewModel.VideoAdd.State.INTENT);
		assertThat(videoAdd.intent).isEqualTo(intent);
	}

	@Test(expected = IllegalStateException.class)
	public void should_throw_when_token_but_no_videoid() {
		viewModel.onToken(false, "test", null);
	}

	@Test
	public void should_add_video_when_token() {
		Uri uri = mock(Uri.class);
		when(videoIdParser.parseVideoId(uri)).thenReturn("video-id");
		viewModel.setVideoUri(uri);

		viewModel.onToken(false, "token", null);

		verify(youtubeRepository).addVideo("video-id", "token", viewModel);
	}

	@Test
	public void should_set_success() {
		viewModel.onAddResult(null, null);

		AddViewModel.VideoAdd videoAdd = viewModel.getVideoAdd().getValue();
		assertThat(videoAdd).isNotNull();
		assertThat(videoAdd.state).isEqualTo(AddViewModel.VideoAdd.State.SUCCESS);
	}

	@Test
	public void should_handle_invalid_token() {
		// first try -> clear token, retry
		viewModel.onAddResult(YoutubeRepository.ErrorType.INVALID_TOKEN, "token");
		verify(accountRepository).invalidateToken("token");
		verify(accountRepository).getToken(viewModel);

		// 2nd try -> error
		viewModel.onAddResult(YoutubeRepository.ErrorType.INVALID_TOKEN, "token2");
		AddViewModel.VideoAdd videoAdd = viewModel.getVideoAdd().getValue();
		assertThat(videoAdd).isNotNull();
		assertThat(videoAdd.state).isEqualTo(AddViewModel.VideoAdd.State.ERROR);
		assertThat(videoAdd.errorType).isEqualTo(AddViewModel.VideoAdd.ErrorType.OTHER);
	}

	@Test
	public void should_handle_already_in_playlist() {
		viewModel.onAddResult(YoutubeRepository.ErrorType.ALREADY_IN_PLAYLIST, null);

		AddViewModel.VideoAdd videoAdd = viewModel.getVideoAdd().getValue();
		assertThat(videoAdd).isNotNull();
		assertThat(videoAdd.state).isEqualTo(AddViewModel.VideoAdd.State.ERROR);
		assertThat(videoAdd.errorType).isEqualTo(AddViewModel.VideoAdd.ErrorType.YOUTUBE_ALREADY_IN_PLAYLIST);
	}

	@Test
	public void should_handle_generic_error() {
		viewModel.onAddResult(YoutubeRepository.ErrorType.NETWORK, null);

		AddViewModel.VideoAdd videoAdd = viewModel.getVideoAdd().getValue();
		assertThat(videoAdd).isNotNull();
		assertThat(videoAdd.state).isEqualTo(AddViewModel.VideoAdd.State.ERROR);
		assertThat(videoAdd.errorType).isEqualTo(AddViewModel.VideoAdd.ErrorType.OTHER);
	}

	@Test
	public void should_videoinfo_success() {
		YoutubeRepository.Videos.Item item = mock(YoutubeRepository.Videos.Item.class);
		viewModel.onVideoInfoResult(null, item);

		AddViewModel.VideoInfo videoInfo = viewModel.getVideoInfo().getValue();
		assertThat(videoInfo).isNotNull();
		assertThat(videoInfo.state).isEqualTo(AddViewModel.VideoInfo.State.LOADED);
		assertThat(videoInfo.data).isEqualTo(item);
	}

	@Test
	public void should_videoinfo_error() {
		viewModel.onVideoInfoResult(YoutubeRepository.ErrorType.VIDEO_NOT_FOUND, null);

		AddViewModel.VideoInfo videoInfo = viewModel.getVideoInfo().getValue();
		assertThat(videoInfo).isNotNull();
		assertThat(videoInfo.state).isEqualTo(AddViewModel.VideoInfo.State.ERROR);
		assertThat(videoInfo.error).isEqualTo(YoutubeRepository.ErrorType.VIDEO_NOT_FOUND);
	}
}
