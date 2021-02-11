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
import com.lambdasoup.watchlater.data.YoutubeRepository.AddVideoResult
import com.lambdasoup.watchlater.data.YoutubeRepository.VideoInfoResult
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

    private val addVideo = Cmd.task<Msg, String, String, AddVideoResult> { videoId, token ->
        youtubeRepository.addVideo(videoId = videoId, token = token)
    }

    private val invalidateAuthToken = Cmd.event<Msg, String> { accountRepository.invalidateToken(it) }

    private val onAccountPermissionGranted = Sub.create<Unit, Msg>()

    private val observer: Observer<Account?> = Observer { account -> accountSubscription.submit(account) }
    private val accountSubscription = Sub.create<Account?, Msg>(
            bind = {
                accountRepository.get().observeForever(observer)
            },
            unbind = {
                accountRepository.get().removeObserver(observer)
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
                    } else {
                        model.copy(videoAdd = VideoAdd.Progress) *
                                getAuthToken { OnAuthTokenResult(it, msg.videoId) }
                    }
                }

                is SetAccount -> model.copy(videoAdd = VideoAdd.Idle) *
                        Cmd.event<Msg> { accountRepository.put(msg.account) }

                is SetVideoUri -> {
                    val videoId = videoIdParser.parseVideoId(msg.uri)
                    if (videoId != null) {
                        model.copy(videoId = videoId) *
                                getVideoInfo(videoId) { OnVideoInfoResult(it) }
                    } else {
                        model.copy(
                                videoId = null,
                                videoInfo = VideoInfo.Error(YoutubeRepository.ErrorType.Other),
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

                is RemoveAccount -> model * Cmd.event<Msg> { accountRepository.clear() }

                is OnAccount -> model.copy(account = msg.account) * Cmd.none()

                is OnAuthTokenResult -> when (msg.result) {
                    is AuthTokenResult.Error ->
                        model.copy(videoAdd = VideoAdd.Error(VideoAdd.ErrorType.NoAccount)) *
                                Cmd.none()
                    is AuthTokenResult.AuthToken -> model *
                            addVideo(msg.videoId, msg.result.token) {
                                OnAddVideoResult(it, msg.videoId)
                            }
                    is AuthTokenResult.HasIntent ->
                        model.copy(videoAdd = VideoAdd.HasIntent(msg.result.intent)) *
                                Cmd.event<Msg> { events.submit(Event.OpenAuthIntent(msg.result.intent)) }
                }

                is OnAddVideoResult -> when (msg.result) {
                    is AddVideoResult.Success -> model.copy(videoAdd = VideoAdd.Success) * Cmd.none()
                    is AddVideoResult.Error ->
                        when (msg.result.type) {
                            YoutubeRepository.ErrorType.InvalidToken -> {
                                if (model.tokenRetried) {
                                    model.copy(
                                            videoAdd = VideoAdd.Error(VideoAdd.ErrorType.Other)
                                    ) * Cmd.none()
                                } else {
                                    model.copy(tokenRetried = true) * Cmd.batch(
                                            invalidateAuthToken(msg.result.token),
                                            getAuthToken { OnAuthTokenResult(it, msg.videoId) }
                                    )
                                }
                            }
                            YoutubeRepository.ErrorType.AlreadyInPlaylist ->
                                model.copy(
                                        videoAdd = VideoAdd.Error(VideoAdd.ErrorType.YoutubeAlreadyInPlaylist)
                                ) * Cmd.none()

                            YoutubeRepository.ErrorType.PlaylistOperationUnsupported ->
                                model.copy(
                                        videoAdd = VideoAdd.Error(VideoAdd.ErrorType.OperationUnsupported)
                                ) * Cmd.none()

                            else
                            -> model.copy(videoAdd = VideoAdd.Error(VideoAdd.ErrorType.Other)) * Cmd.none()
                        }
                }
            }

    private fun subscriptions(model: Model) =
            Sub.batch(
                    accountSubscription { OnAccount(it) },
                    if (model.videoId != null) {
                        onAccountPermissionGranted { WatchLater(model.videoId) }
                    } else {
                        Sub.none()
                    }
            )

    data class Model(
            val videoId: String?,
            val videoAdd: VideoAdd,
            val videoInfo: VideoInfo,
            val account: Account?,
            val permissionNeeded: Boolean?,
            val tokenRetried: Boolean,
    )

    sealed class Msg {
        data class WatchLater(val videoId: String) : Msg()
        data class SetAccount(val account: Account) : Msg()
        data class OnAccount(val account: Account?) : Msg()
        object RemoveAccount : Msg()
        data class SetVideoUri(val uri: Uri) : Msg()
        data class SetPermissionNeeded(val permissionNeeded: Boolean) : Msg()
        data class OnVideoInfoResult(val result: VideoInfoResult) : Msg()
        data class OnAuthTokenResult(
                val result: AuthTokenResult,
                val videoId: String,
        ) : Msg()

        data class OnAddVideoResult(
                val result: AddVideoResult,
                val videoId: String,
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

    fun removeAccount() {
        tea.ui(RemoveAccount)
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

    sealed class VideoAdd {
        object Idle : VideoAdd()
        object Progress : VideoAdd()
        object Success : VideoAdd()
        data class Error(val error: ErrorType) : VideoAdd()
        data class HasIntent(val intent: Intent) : VideoAdd()

        enum class ErrorType {
            Other, NoAccount, NoPermission, YoutubeAlreadyInPlaylist, OperationUnsupported
        }
    }

    sealed class VideoInfo {
        object Progress : VideoInfo()
        data class Loaded(val data: Item) : VideoInfo()
        data class Error(val error: YoutubeRepository.ErrorType) : VideoInfo()
    }
}
