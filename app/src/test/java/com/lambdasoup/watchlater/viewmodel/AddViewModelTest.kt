/*
 * Copyright (c) 2015 - 2021
 *
 * Maximilian Hille <mh@lambdasoup.com>
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
package com.lambdasoup.watchlater.viewmodel

import android.accounts.Account
import android.content.Intent
import android.net.Uri
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.common.truth.Truth.assertThat
import com.lambdasoup.watchlater.WatchLaterApplication
import com.lambdasoup.watchlater.data.AccountRepository
import com.lambdasoup.watchlater.data.YoutubeRepository
import com.lambdasoup.watchlater.data.YoutubeRepository.Videos
import com.lambdasoup.watchlater.util.VideoIdParser
import com.lambdasoup.watchlater.viewmodel.AddViewModel.VideoAdd
import com.lambdasoup.watchlater.viewmodel.AddViewModel.VideoInfo
import com.nhaarman.mockitokotlin2.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.junit.runner.RunWith
import org.mockito.MockitoAnnotations.initMocks
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class AddViewModelTest {

    @get:Rule
    var rule: TestRule = InstantTaskExecutorRule()

    private val application: WatchLaterApplication = mock()
    private val accountRepository: AccountRepository = mock()
    private val videoIdParser: VideoIdParser = mock()
    private val youtubeRepository: YoutubeRepository = mock()

    private lateinit var viewModel: AddViewModel
    private lateinit var accountLiveData: MutableLiveData<Account>

    @Before
    fun setup() {
        initMocks(this)

        whenever(application.accountRepository).thenReturn(accountRepository)
        whenever(application.videoIdParser).thenReturn(videoIdParser)
        whenever(application.youtubeRepository).thenReturn(youtubeRepository)
        accountLiveData = MutableLiveData()
        whenever(accountRepository.get()).thenReturn(accountLiveData)
        viewModel = AddViewModel(application)
    }

    @Test
    fun should_set_account() {
        val account: Account = mock()

        viewModel.setAccount(account)

        verify(accountRepository).put(account)
    }

    @Test
    fun should_get_account() {
        val liveData: LiveData<Account> = mock()
        whenever(accountRepository.get()).thenReturn(liveData)
        val viewModel = AddViewModel(application)
        val defaultAccount = viewModel.account

        assertThat(defaultAccount).isEqualTo(liveData)
    }

    @Test
    fun should_remove_account() {
        viewModel.removeAccount()

        verify(accountRepository).clear()
    }

    @Test
    fun should_set_uri() {
        val uri: Uri = mock()
        whenever(videoIdParser.parseVideoId(uri)).thenReturn("test")
        viewModel.setVideoUri(uri)

        verify(youtubeRepository).getVideoInfo(eq("test"), any())
    }

    @Test
    fun should_set_permission_needed() {
        viewModel.setPermissionNeeded(true)

        assertThat(viewModel.getPermissionNeeded().value).isEqualTo(true)
    }

    @Test
    fun should_set_add_progress_when_permission_to_true() {
        viewModel.setPermissionNeeded(true)
        viewModel.setPermissionNeeded(false)

        val videoAdd = viewModel.getVideoAdd().value
        assertThat(videoAdd).isNotNull()
        assertThat(videoAdd).isInstanceOf(VideoAdd.Idle::class.java)
    }

    @Test
    fun should_error_when_watchlater_without_account() {
        viewModel.watchLater()

        val videoAdd = viewModel.getVideoAdd().value
        assertThat(videoAdd).isNotNull()
        assertThat(videoAdd).isInstanceOf(VideoAdd.Error::class.java)
        assertThat((videoAdd as VideoAdd.Error).error).isEqualTo(VideoAdd.ErrorType.NoAccount)
    }

    @Test
    fun should_error_when_watchlater_without_permissions() {
        accountLiveData.value = mock()

        viewModel.setPermissionNeeded(true)
        viewModel.watchLater()

        val videoAdd = viewModel.getVideoAdd().value
        assertThat(videoAdd).isNotNull()
        assertThat(videoAdd).isInstanceOf(VideoAdd.Error::class.java)
        assertThat((videoAdd as VideoAdd.Error).error).isEqualTo(VideoAdd.ErrorType.NoPermission)
    }

    @Test
    fun should_request_token_when_watchlater() {
        accountLiveData.value = mock()

        viewModel.setPermissionNeeded(false)
        viewModel.watchLater()

        val videoAdd = viewModel.getVideoAdd().value
        assertThat(videoAdd).isNotNull()
        assertThat(videoAdd).isInstanceOf(VideoAdd.Progress::class.java)
        verify(accountRepository).getToken(viewModel)
    }

    @Test
    fun should_error_when_token_result_has_error() {
        viewModel.onToken(true, null, null)

        val videoAdd = viewModel.getVideoAdd().value
        assertThat(videoAdd).isNotNull()
        assertThat(videoAdd).isInstanceOf(VideoAdd.Error::class.java)
        assertThat((videoAdd as VideoAdd.Error).error).isEqualTo(VideoAdd.ErrorType.NoAccount)
    }

    @Test
    fun should_intent_when_token_result_has_intent() {
        val intent: Intent = mock()

        viewModel.onToken(true, null, intent)

        val videoAdd = viewModel.getVideoAdd().value
        assertThat(videoAdd).isNotNull()
        assertThat(videoAdd).isInstanceOf(VideoAdd.HasIntent::class.java)
        assertThat((videoAdd as VideoAdd.HasIntent).intent).isEqualTo(intent)
    }

    @Test(expected = IllegalStateException::class)
    fun should_throw_when_token_but_no_videoid() {
        viewModel.onToken(false, "test", null)
    }

    @Test
    fun should_add_video_when_token() {
        val uri: Uri = mock()
        whenever(videoIdParser.parseVideoId(uri)).thenReturn("video-id")

        viewModel.setVideoUri(uri)
        viewModel.onToken(false, "token", null)

        verify(youtubeRepository).addVideo("video-id", "token", viewModel)
    }

    @Test
    fun should_set_success() {
        viewModel.onAddResult(null, null)

        val videoAdd = viewModel.getVideoAdd().value
        assertThat(videoAdd).isNotNull()
        assertThat(videoAdd).isInstanceOf(VideoAdd.Success::class.java)
    }

    @Test
    fun should_handle_invalid_token() {
        // first try -> clear token, retry
        viewModel.onAddResult(YoutubeRepository.ErrorType.InvalidToken, "token")
        verify(accountRepository).invalidateToken("token")
        verify(accountRepository).getToken(viewModel)

        // 2nd try -> error
        viewModel.onAddResult(YoutubeRepository.ErrorType.InvalidToken, "token2")
        val videoAdd = viewModel.getVideoAdd().value
        assertThat(videoAdd).isNotNull()
        assertThat(videoAdd).isInstanceOf(VideoAdd.Error::class.java)
        assertThat((videoAdd as VideoAdd.Error).error).isEqualTo(VideoAdd.ErrorType.Other)
    }

    @Test
    fun should_handle_already_in_playlist() {
        viewModel.onAddResult(YoutubeRepository.ErrorType.AlreadyInPlaylist, null)

        val videoAdd = viewModel.getVideoAdd().value
        assertThat(videoAdd).isNotNull()
        assertThat(videoAdd).isInstanceOf(VideoAdd.Error::class.java)
        assertThat((videoAdd as VideoAdd.Error).error).isEqualTo(VideoAdd.ErrorType.YoutubeAlreadyInPlaylist)
    }

    @Test
    fun should_handle_generic_error() {
        viewModel.onAddResult(YoutubeRepository.ErrorType.Network, null)

        val videoAdd = viewModel.getVideoAdd().value
        assertThat(videoAdd).isNotNull()
        assertThat(videoAdd).isInstanceOf(VideoAdd.Error::class.java)
        assertThat((videoAdd as VideoAdd.Error).error).isEqualTo(VideoAdd.ErrorType.Other)
    }

    @Test
    fun should_videoinfo_success() {
        val item: Videos.Item = mock()

        viewModel.onVideoInfoResult(null, item)

        val videoInfo = viewModel.getVideoInfo().value
        assertThat(videoInfo).isNotNull()
        assertThat(videoInfo).isInstanceOf(VideoInfo.Loaded::class.java)
        assertThat((videoInfo as VideoInfo.Loaded).data).isEqualTo(item)
    }

    @Test
    fun should_videoinfo_error() {
        viewModel.onVideoInfoResult(YoutubeRepository.ErrorType.VideoNotFound, null)

        val videoInfo = viewModel.getVideoInfo().value
        assertThat(videoInfo).isNotNull()
        assertThat(videoInfo).isInstanceOf(VideoInfo.Error::class.java)
        assertThat((videoInfo as VideoInfo.Error).error).isEqualTo(YoutubeRepository.ErrorType.VideoNotFound)
    }
}
