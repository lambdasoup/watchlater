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
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.lambdasoup.tea.Cmd
import com.lambdasoup.tea.Sub
import com.lambdasoup.tea.Tea
import com.lambdasoup.tea.times
import com.lambdasoup.watchlater.WatchLaterApplication
import com.lambdasoup.watchlater.data.AccountRepository
import com.lambdasoup.watchlater.data.AccountRepository.AuthTokenResult
import com.lambdasoup.watchlater.data.YoutubeRepository
import com.lambdasoup.watchlater.data.YoutubeRepository.*
import com.lambdasoup.watchlater.data.YoutubeRepository.Playlists.Playlist
import com.lambdasoup.watchlater.data.YoutubeRepository.Videos.Item
import com.lambdasoup.watchlater.util.EventSource
import com.lambdasoup.watchlater.util.VideoIdParser
import com.lambdasoup.watchlater.viewmodel.AddViewModel.Msg.*

class AddViewModel(application: WatchLaterApplication) : WatchLaterViewModel(application) {

    private val accountRepository: AccountRepository = application.accountRepository
    private val youtubeRepository: YoutubeRepository = application.youtubeRepository
    private val videoIdParser: VideoIdParser = application.videoIdParser

    private val getVideoInfo = Cmd.task<Msg, String, VideoInfoResult> {
        youtubeRepository.getVideoInfo(it)
    }

    private val getAuthToken = Cmd.task<Msg, AuthTokenResult> {
        accountRepository.getAuthToken()
    }

    private val getPlaylists = Cmd.task<Msg, String, PlaylistsResult> {
        youtubeRepository.getPlaylists(it)
    }

    private val addVideo = Cmd.task<Msg, String, String, Playlist, AddVideoResult> { videoId, token, playlist ->
        youtubeRepository.addVideo(videoId = videoId, token = token, playlist = playlist)
    }

    private val invalidateAuthToken = Cmd.event<Msg, String> { accountRepository.invalidateToken(it) }

    private val onAccountPermissionGranted = Sub.create<Unit, Msg>()

    private val accountObserver: Observer<Account?> = Observer { account -> accountSubscription.submit(account) }
    private val accountSubscription = Sub.create<Account?, Msg>(
            bind = {
                accountRepository.get().observeForever(accountObserver)
            },
            unbind = {
                accountRepository.get().removeObserver(accountObserver)
            }
    )

    private val playlistObserver: Observer<Playlist?> = Observer { playlistSubscription.submit(it) }
    private val playlistSubscription = Sub.create<Playlist?, Msg>(
            bind = {
                youtubeRepository.targetPlaylist.observeForever(playlistObserver)
            },
            unbind = {
                youtubeRepository.targetPlaylist.removeObserver(playlistObserver)
            }
    )

    override fun onCleared() {
        super.onCleared()

        tea.clear()
    }

    val events = EventSource<Event>()
    val model = MutableLiveData<Model>()

    private val tea = Tea(
            init = Model(
                    videoId = null,
                    videoAdd = VideoAdd.Idle,
                    videoInfo = VideoInfo.Progress,
                    account = null,
                    permissionNeeded = null,
                    tokenRetried = false,
                    targetPlaylist = null,
                    playlistSelection = null,
            ) * Cmd.none(),
            view = model::postValue,
            update = ::update,
            subscriptions = ::subscriptions,
    )

    private fun update(model: Model, msg: Msg): Pair<Model, Cmd<Msg>> =
            when (msg) {
                is WatchLater -> {
                    if (model.account == null) {
                        model.copy(videoAdd = VideoAdd.Error(VideoAdd.ErrorType.NoAccount)) *
                                Cmd.none()
                    } else if (model.permissionNeeded != null && model.permissionNeeded) {
                        model.copy(videoAdd = VideoAdd.Error(VideoAdd.ErrorType.NoPermission)) *
                                Cmd.none()
                    } else if (model.targetPlaylist == null) {
                        model.copy(videoAdd = VideoAdd.Error(VideoAdd.ErrorType.NoPlaylistSelected)) *
                                Cmd.none()
                    } else {
                        model.copy(videoAdd = VideoAdd.Progress) *
                                getAuthToken { OnInsertTokenResult(it, msg.videoId, model.targetPlaylist) }
                    }
                }

                is SetAccount -> model.copy(videoAdd = VideoAdd.Idle) *
                        Cmd.event<Msg> { accountRepository.put(msg.account) }

                is ChangePlaylist ->
                    if (model.account == null) {
                        model.copy(videoAdd = VideoAdd.Error(VideoAdd.ErrorType.NoAccount)) *
                                Cmd.none()
                    } else {
                        model * getAuthToken { OnPlaylistsTokenResult(it) }
                    }

                is OnPlaylistsTokenResult -> when (msg.result) {
                    is AuthTokenResult.Error ->
                        model.copy(videoAdd = VideoAdd.Error(VideoAdd.ErrorType.NoAccount)) *
                                Cmd.none()
                    is AuthTokenResult.AuthToken ->
                        model * getPlaylists(msg.result.token) { OnPlaylistResult(it, msg.result.token) }
                    is AuthTokenResult.HasIntent ->
                        model.copy(videoAdd = VideoAdd.HasIntent(msg.result.intent)) *
                                Cmd.event<Msg> { events.submit(Event.OpenAuthIntent(msg.result.intent)) }
                }

                is SetVideoUri -> {
                    val videoId = videoIdParser.parseVideoId(msg.uri)
                    if (videoId != null) {
                        model.copy(videoId = videoId) *
                                getVideoInfo(videoId) { OnVideoInfoResult(it) }
                    } else {
                        model.copy(
                                videoId = null,
                                videoInfo = VideoInfo.Error(ErrorType.Other),
                        ) * Cmd.none()
                    }
                }

                is OnVideoInfoResult -> when (msg.result) {
                    is VideoInfoResult.VideoInfo ->
                        model.copy(videoInfo = VideoInfo.Loaded(msg.result.item)) * Cmd.none()
                    is VideoInfoResult.Error ->
                        model.copy(videoInfo = VideoInfo.Error(msg.result.type)) * Cmd.none()
                }

                is SetPermissionNeeded -> {
                    // only reset add state when permission state changes to positive
                    val oldValue = model.permissionNeeded
                    val videoAdd = if (oldValue != null && oldValue && !msg.permissionNeeded) {
                        VideoAdd.Idle
                    } else {
                        model.videoAdd
                    }
                    model.copy(
                            permissionNeeded = msg.permissionNeeded,
                            videoAdd = videoAdd,
                    ) * Cmd.none()
                }

                is OnAccount -> model.copy(account = msg.account) * Cmd.none()

                is OnInsertTokenResult -> when (msg.result) {
                    is AuthTokenResult.Error ->
                        model.copy(videoAdd = VideoAdd.Error(VideoAdd.ErrorType.NoAccount)) *
                                Cmd.none()
                    is AuthTokenResult.AuthToken -> model *
                            addVideo(msg.videoId, msg.result.token, msg.targetPlaylist) {
                                OnAddVideoResult(it, msg.videoId, msg.targetPlaylist)
                            }
                    is AuthTokenResult.HasIntent ->
                        model.copy(videoAdd = VideoAdd.HasIntent(msg.result.intent)) *
                                Cmd.event<Msg> { events.submit(Event.OpenAuthIntent(msg.result.intent)) }
                }

                is OnAddVideoResult -> when (msg.result) {
                    is AddVideoResult.Success -> model.copy(videoAdd = VideoAdd.Success) * Cmd.none()
                    is AddVideoResult.Error ->
                        when (msg.result.type) {
                            ErrorType.InvalidToken -> {
                                if (model.tokenRetried) {
                                    model.copy(
                                            videoAdd = VideoAdd.Error(VideoAdd.ErrorType.Other)
                                    ) * Cmd.none()
                                } else {
                                    model.copy(tokenRetried = true) * Cmd.batch(
                                            invalidateAuthToken(msg.result.token),
                                            getAuthToken { OnInsertTokenResult(it, msg.videoId, msg.targetPlaylist) }
                                    )
                                }
                            }
                            else ->
                                model.copy(videoAdd = VideoAdd.Error(VideoAdd.ErrorType.Other)) * Cmd.none()
                        }
                }

                is OnPlaylistResult ->
                    when (msg.result) {
                        is PlaylistsResult.Error -> when (msg.result.type) {
                            ErrorType.InvalidToken -> {
                                if (model.tokenRetried) {
                                    model.copy(
                                            videoAdd = VideoAdd.Error(VideoAdd.ErrorType.Other)
                                    ) * Cmd.none()
                                } else {
                                    model.copy(tokenRetried = true) * Cmd.batch(
                                            invalidateAuthToken(msg.token),
                                            getAuthToken { OnPlaylistsTokenResult(it) }
                                    )
                                }
                            }
                            else ->
                                model.copy(videoAdd = VideoAdd.Error(VideoAdd.ErrorType.Other)) * Cmd.none()
                        }

                        is PlaylistsResult.Ok -> {
                            model.copy(playlistSelection = msg.result.playlists) * Cmd.none()
                        }

                    }

                is OnAccountPermissionGranted -> model.copy(videoAdd = VideoAdd.Idle) * Cmd.none()

                is OnTargetPlaylist -> model.copy(targetPlaylist = msg.playlist) * Cmd.none()

                is SelectPlaylist -> model.copy(playlistSelection = null) *
                        Cmd.event<Msg> { youtubeRepository.setPlaylist(msg.playlist) }

                is ClearPlaylists -> model.copy(playlistSelection = null) * Cmd.none()
            }

    @Suppress("UNUSED_PARAMETER")
    private fun subscriptions(model: Model) =
            Sub.batch(
                    accountSubscription { OnAccount(it) },
                    onAccountPermissionGranted { OnAccountPermissionGranted },
                    playlistSubscription { OnTargetPlaylist(it) },
            )

    data class Model(
            val videoId: String?,
            val videoAdd: VideoAdd,
            val videoInfo: VideoInfo,
            val account: Account?,
            val permissionNeeded: Boolean?,
            val tokenRetried: Boolean,
            val targetPlaylist: Playlist?,
            val playlistSelection: Playlists?,
    )

    sealed class Msg {
        data class WatchLater(val videoId: String) : Msg()
        data class SetAccount(val account: Account) : Msg()
        data class OnAccount(val account: Account?) : Msg()
        data class OnTargetPlaylist(val playlist: Playlist?) : Msg()
        object OnAccountPermissionGranted : Msg()
        object ChangePlaylist : Msg()
        data class OnPlaylistResult(
                val result: PlaylistsResult,
                val token: String,
        ) : Msg()

        object ClearPlaylists : Msg()
        data class SelectPlaylist(val playlist: Playlist?) : Msg()
        data class SetVideoUri(val uri: Uri) : Msg()
        data class SetPermissionNeeded(val permissionNeeded: Boolean) : Msg()
        data class OnVideoInfoResult(val result: VideoInfoResult) : Msg()
        data class OnInsertTokenResult(
                val result: AuthTokenResult,
                val videoId: String,
                val targetPlaylist: Playlist,
        ) : Msg()

        data class OnPlaylistsTokenResult(
                val result: AuthTokenResult,
        ) : Msg()

        data class OnAddVideoResult(
                val result: AddVideoResult,
                val videoId: String,
                val targetPlaylist: Playlist,
        ) : Msg()
    }

    sealed class Event {
        data class OpenAuthIntent(val intent: Intent) : Event()
    }

    fun onAccountPermissionGranted() {
        onAccountPermissionGranted.submit(Unit)
    }

    fun setAccount(account: Account) {
        tea.ui(SetAccount(account))
    }

    fun setVideoUri(uri: Uri) {
        tea.ui(SetVideoUri(uri))
    }

    fun setPermissionNeeded(needsPermission: Boolean) {
        tea.ui(SetPermissionNeeded(needsPermission))
    }

    fun watchLater(videoId: String) {
        tea.ui(WatchLater(videoId))
    }

    fun changePlaylist() {
        tea.ui(ChangePlaylist)
    }

    fun selectPlaylist(playlist: Playlist) {
        tea.ui(SelectPlaylist(playlist))
    }

    fun clearPlaylists() {
        tea.ui(ClearPlaylists)
    }

    sealed class VideoAdd {
        object Idle : VideoAdd()
        object Progress : VideoAdd()
        object Success : VideoAdd()
        data class Error(val error: ErrorType) : VideoAdd()
        data class HasIntent(val intent: Intent) : VideoAdd()

        enum class ErrorType {
            Other, NoAccount, NoPermission, NoPlaylistSelected
        }
    }

    sealed class VideoInfo {
        object Progress : VideoInfo()
        data class Loaded(val data: Item) : VideoInfo()
        data class Error(val error: ErrorType) : VideoInfo()
    }
}
