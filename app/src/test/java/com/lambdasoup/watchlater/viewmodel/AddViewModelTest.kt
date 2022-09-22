/*
 * Copyright (c) 2015 - 2022
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
import androidx.lifecycle.MutableLiveData
import com.google.common.truth.Truth.assertThat
import com.lambdasoup.tea.testing.TeaTestEngineRule
import com.lambdasoup.watchlater.data.AccountRepository
import com.lambdasoup.watchlater.data.YoutubeRepository
import com.lambdasoup.watchlater.data.YoutubeRepository.*
import com.lambdasoup.watchlater.data.YoutubeRepository.Playlists.Playlist
import com.lambdasoup.watchlater.util.VideoIdParser
import com.lambdasoup.watchlater.viewmodel.AddViewModel.*
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Assert.assertTrue
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
    var aacRule: TestRule = InstantTaskExecutorRule()

    @get:Rule
    var teaRule: TestRule = TeaTestEngineRule()

    private val accountRepository: AccountRepository = mock()
    private val videoIdParser: VideoIdParser = mock()
    private val youtubeRepository: YoutubeRepository = mock()

    private lateinit var vm: AddViewModel
    private lateinit var accountLiveData: MutableLiveData<Account>
    private val playlistLiveData = MutableLiveData<Playlist?>()

    private val token = "token"
    private val uri: Uri = mock()
    private val videoId = "video-id"
    private val item: Videos.Item = mock()
    private val observedEvents = mutableListOf<Event>()
    private val playlist: Playlist = mock()

    @Before
    fun setup() {
        initMocks(this)

        accountLiveData = MutableLiveData()
        whenever(accountRepository.get()).thenReturn(accountLiveData)
        whenever(youtubeRepository.targetPlaylist).thenReturn(playlistLiveData)
        playlistLiveData.value = playlist

        vm = AddViewModel(accountRepository, youtubeRepository, videoIdParser)

        whenever(accountRepository.getAuthToken())
                .thenReturn(AccountRepository.AuthTokenResult.AuthToken(token))
        accountLiveData.value = mock()
        whenever(videoIdParser.parseVideoId(uri)).thenReturn(videoId)
        whenever(youtubeRepository.getVideoInfo(videoId, token))
                .thenReturn(VideoInfoResult.VideoInfo(item))
        whenever(youtubeRepository.addVideo(videoId, playlist, token))
                .thenReturn(AddVideoResult.Success)

        observedEvents.clear()

        vm.setVideoUri(uri)
        vm.setPermissionNeeded(false)
    }

    @Test
    fun `should set account`() {
        val account: Account = mock()

        vm.setAccount(account)

        verify(accountRepository).put(account)
    }

    @Test
    fun `should get account`() {
        val account: Account = mock()
        accountLiveData.value = account

        assertThat(vm.model.value!!.account).isEqualTo(account)
    }

    @Test
    fun `should set permission needed`() {
        vm.setPermissionNeeded(true)

        assertThat(vm.model.value!!.permissionNeeded).isEqualTo(true)
    }

    @Test
    fun `should set add progress when permission to true`() {
        vm.setPermissionNeeded(true)
        vm.setPermissionNeeded(false)

        val videoAdd = vm.model.value?.videoAdd
        assertThat(videoAdd).isNotNull()
        assertThat(videoAdd).isEqualTo(VideoAdd.Idle)
    }

    @Test
    fun `should error when watchlater without account`() {
        accountLiveData.value = null

        vm.watchLater(videoId)

        val videoAdd = vm.model.value!!.videoAdd
        assertThat(videoAdd).isNotNull()
        assertThat(videoAdd).isInstanceOf(VideoAdd.Error::class.java)
        assertThat((videoAdd as VideoAdd.Error).error).isEqualTo(VideoAdd.ErrorType.NoAccount)
    }

    @Test
    fun `should error when watchlater without permissions`() {
        accountLiveData.value = mock()

        vm.setPermissionNeeded(true)
        vm.watchLater(videoId)

        val videoAdd = vm.model.value!!.videoAdd
        assertThat(videoAdd).isNotNull()
        assertThat(videoAdd).isInstanceOf(VideoAdd.Error::class.java)
        assertThat((videoAdd as VideoAdd.Error).error).isEqualTo(VideoAdd.ErrorType.NoPermission)
    }

    @Test
    fun `should add to watchlater`() {
        vm.watchLater(videoId)

        val videoAdd = vm.model.value?.videoAdd
        assertThat(videoAdd).isEqualTo(VideoAdd.Success)
    }

    @Test
    fun `should error when token result has error`() {
        whenever(accountRepository.getAuthToken())
            .thenReturn(AccountRepository.AuthTokenResult.Error(AccountRepository.ErrorType.AccountRemoved))
        accountLiveData.value = mock()

        vm.watchLater(videoId)

        val videoAdd = vm.model.value!!.videoAdd
        assertThat(videoAdd).isNotNull()
        assertThat(videoAdd).isInstanceOf(VideoAdd.Error::class.java)
        assertThat((videoAdd as VideoAdd.Error).error).isEqualTo(VideoAdd.ErrorType.NoAccount)
    }

    @Test
    fun `should network error when token result has network error`() {
        whenever(accountRepository.getAuthToken())
            .thenReturn(AccountRepository.AuthTokenResult.Error(AccountRepository.ErrorType.Network))
        accountLiveData.value = mock()

        vm.watchLater(videoId)

        val videoAdd = vm.model.value!!.videoAdd
        assertThat(videoAdd).isNotNull()
        assertThat(videoAdd).isInstanceOf(VideoAdd.Error::class.java)
        assertThat((videoAdd as VideoAdd.Error).error).isEqualTo(VideoAdd.ErrorType.Network)
    }

    @Test
    fun `should intent when token result has intent`() {
        val intent: Intent = mock()
        whenever(accountRepository.getAuthToken())
                .thenReturn(AccountRepository.AuthTokenResult.HasIntent(intent))
        accountLiveData.value = mock()

        vm.watchLater(videoId)

        val videoAdd = vm.model.value!!.videoAdd
        assertThat(videoAdd).isNotNull()
        assertThat(videoAdd).isInstanceOf(VideoAdd.HasIntent::class.java)
        assertThat((videoAdd as VideoAdd.HasIntent).intent).isEqualTo(intent)

        assertTrue(vm.events.contains(Event.OpenAuthIntent(intent)))
    }

    @Test
    fun `should refresh token transparently`() {
        val token2 = "token2"

        whenever(youtubeRepository.addVideo(videoId, playlist, token))
            .thenReturn(AddVideoResult.Error(ErrorType.InvalidToken, token))
        whenever(youtubeRepository.addVideo(videoId, playlist, token2))
            .thenReturn(AddVideoResult.Success)

        whenever(accountRepository.getAuthToken())
                .thenReturn(AccountRepository.AuthTokenResult.AuthToken(token))
                .thenReturn(AccountRepository.AuthTokenResult.AuthToken(token2))

        vm.watchLater(videoId)

        verify(accountRepository).invalidateToken(token)
        val videoAdd = vm.model.value!!.videoAdd
        assertThat(videoAdd).isNotNull()
        assertThat(videoAdd).isEqualTo(VideoAdd.Success)
    }

    @Test
    fun `should error when token refresh fails`() {
        val token2 = "token2"

        whenever(youtubeRepository.addVideo(videoId, playlist, token))
            .thenReturn(AddVideoResult.Error(ErrorType.InvalidToken, token))
        whenever(youtubeRepository.addVideo(videoId, playlist, token2))
            .thenReturn(AddVideoResult.Error(ErrorType.InvalidToken, token2))

        whenever(accountRepository.getAuthToken())
                .thenReturn(AccountRepository.AuthTokenResult.AuthToken(token))
                .thenReturn(AccountRepository.AuthTokenResult.AuthToken(token2))

        vm.watchLater(videoId)

        verify(accountRepository).invalidateToken(token)
        val videoAdd = vm.model.value!!.videoAdd
        assertThat(videoAdd).isNotNull()
        assertThat(videoAdd).isEqualTo(VideoAdd.Error(VideoAdd.ErrorType.Other("InvalidToken")))
    }

    @Test
    fun `should handle generic error`() {
        whenever(youtubeRepository.addVideo(videoId, playlist, token))
            .thenReturn(AddVideoResult.Error(ErrorType.Other, token))

        vm.watchLater(videoId)

        val videoAdd = vm.model.value!!.videoAdd
        assertThat((videoAdd as VideoAdd.Error).error).isEqualTo(VideoAdd.ErrorType.Other("Other"))
    }

    @Test
    fun `should set videoid and get videoinfo`() {
        val uri: Uri = mock()
        val item: Videos.Item = mock()
        val videoId = "video-id"
        whenever(videoIdParser.parseVideoId(uri)).thenReturn(videoId)
        whenever(youtubeRepository.getVideoInfo(videoId, token))
            .thenReturn(VideoInfoResult.VideoInfo(item))

        vm.setVideoUri(uri)

        assertThat(vm.model.value!!.videoId).isEqualTo(videoId)
        assertThat(vm.model.value!!.videoInfo).isEqualTo(VideoInfo.Loaded(item))
    }

    @Test
    fun `should set videoinfo error`() {
        val uri: Uri = mock()
        val videoId = "video-id"
        whenever(videoIdParser.parseVideoId(uri)).thenReturn(videoId)
        whenever(youtubeRepository.getVideoInfo(videoId, token))
            .thenReturn(VideoInfoResult.Error(ErrorType.VideoNotFound))

        vm.setVideoUri(uri)

        assertThat(vm.model.value!!.videoId).isEqualTo(videoId)
        assertThat(vm.model.value!!.videoInfo)
            .isEqualTo(VideoInfo.Error(VideoInfo.ErrorType.Youtube(ErrorType.VideoNotFound)))
    }

    @Test
    fun `should reload videoinfo after error when new account is added`() {
        accountLiveData.value = null
        whenever(accountRepository.getAuthToken())
            .thenReturn(AccountRepository.AuthTokenResult.Error(AccountRepository.ErrorType.Network))

        vm.setVideoUri(uri)

        whenever(accountRepository.getAuthToken())
            .thenReturn(AccountRepository.AuthTokenResult.AuthToken(token))
        accountLiveData.value = mock()

        assertThat(vm.model.value!!.videoInfo)
            .isInstanceOf(VideoInfo.Loaded::class.java)
    }

    @Test
    fun `should remove videoAdd error when playlist gets changed`() {
        vm.selectPlaylist(mock())

        assertThat(vm.model.value!!.videoAdd)
            .isInstanceOf(VideoAdd.Idle::class.java)
    }
}
