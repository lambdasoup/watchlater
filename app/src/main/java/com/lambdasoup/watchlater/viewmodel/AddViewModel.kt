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
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.lambdasoup.watchlater.WatchLaterApplication
import com.lambdasoup.watchlater.data.AccountRepository
import com.lambdasoup.watchlater.data.AccountRepository.TokenCallback
import com.lambdasoup.watchlater.data.YoutubeRepository
import com.lambdasoup.watchlater.data.YoutubeRepository.AddVideoCallback
import com.lambdasoup.watchlater.data.YoutubeRepository.VideoInfoCallback
import com.lambdasoup.watchlater.util.VideoIdParser

class AddViewModel(application: WatchLaterApplication) : WatchLaterViewModel(application), TokenCallback, AddVideoCallback, VideoInfoCallback {

    private val accountRepository: AccountRepository = application.accountRepository
    private val youtubeRepository: YoutubeRepository = application.youtubeRepository
    private val videoIdParser: VideoIdParser = application.videoIdParser
    private val permissionNeeded = MutableLiveData<Boolean>()
    val account: LiveData<Account>
    private val _videoAdd = MutableLiveData<VideoAdd>()
    private val videoInfo = MutableLiveData<VideoInfo>()
    private var tokenRetried = false
    private var videoId: String? = null

    init {
        account = accountRepository.get()
        _videoAdd.value = VideoAdd.Idle
        videoInfo.value = VideoInfo.Progress
    }

    fun setAccount(account: Account?) {
        accountRepository.put(account!!)
        _videoAdd.value = VideoAdd.Idle
    }

    fun removeAccount() {
        accountRepository.clear()
    }

    fun setVideoUri(uri: Uri) {
        videoId = videoIdParser.parseVideoId(uri)
        youtubeRepository.getVideoInfo(videoId!!, this)
    }

    fun getVideoInfo(): LiveData<VideoInfo> {
        return videoInfo
    }

    fun getVideoAdd(): LiveData<VideoAdd> {
        return _videoAdd
    }

    fun getPermissionNeeded(): LiveData<Boolean> {
        return permissionNeeded
    }

    fun setPermissionNeeded(needsPermission: Boolean) {
        // only reset add state when permission state changes to positive
        val oldValue = permissionNeeded.value
        if (oldValue != null && oldValue && !needsPermission) {
            _videoAdd.value = VideoAdd.Idle
        }
        permissionNeeded.value = needsPermission
    }

    fun watchLater() {
        _videoAdd.value = VideoAdd.Progress
        if (account.value == null) {
            _videoAdd.value = VideoAdd.Error(VideoAdd.ErrorType.NoAccount)
            return
        }

        val permissionNeeded = permissionNeeded.value
        if (permissionNeeded != null && permissionNeeded) {
            _videoAdd.value = VideoAdd.Error(VideoAdd.ErrorType.NoPermission)
            return
        }
        accountRepository.getToken(this)
    }

    override fun onToken(hasError: Boolean, token: String?, intent: Intent?) {
        if (hasError && intent != null) {
            _videoAdd.value = VideoAdd.HasIntent(intent)
            return
        }
        if (hasError) {
            _videoAdd.value = VideoAdd.Error(VideoAdd.ErrorType.NoAccount)
            return
        }
        checkNotNull(videoId) { "cannot query video without id" }
        youtubeRepository.addVideo(videoId, token!!, this)
    }

    override fun onAddResult(errorType: YoutubeRepository.ErrorType?, token: String?) {
        if (errorType == null) {
            _videoAdd.value = VideoAdd.Success
            return
        }
        when (errorType) {
            YoutubeRepository.ErrorType.InvalidToken -> {
                if (tokenRetried) {
                    _videoAdd.value = VideoAdd.Error(VideoAdd.ErrorType.Other)
                    return
                }
                tokenRetried = true
                accountRepository.invalidateToken(token)
                accountRepository.getToken(this)
                return
            }
            YoutubeRepository.ErrorType.AlreadyInPlaylist -> {
                _videoAdd.value = VideoAdd.Error(VideoAdd.ErrorType.YoutubeAlreadyInPlaylist)
                return
            }
            else -> _videoAdd.setValue(VideoAdd.Error(VideoAdd.ErrorType.Other))
        }
    }

    override fun onVideoInfoResult(errorType: YoutubeRepository.ErrorType?, item: YoutubeRepository.Videos.Item?) {
        if (errorType != null) {
            videoInfo.setValue(VideoInfo.Error(errorType))
        } else {
            videoInfo.setValue(VideoInfo.Loaded(item!!))
        }
    }

    sealed class VideoAdd {
        object Idle : VideoAdd()
        object Progress : VideoAdd()
        object Success : VideoAdd()
        data class Error(val error: ErrorType) : VideoAdd()
        data class HasIntent(val intent: Intent) : VideoAdd()

        enum class ErrorType {
            Other, NoAccount, NoPermission, YoutubeAlreadyInPlaylist
        }
    }

    sealed class VideoInfo {
        object Progress : VideoInfo()
        data class Loaded(val data: YoutubeRepository.Videos.Item) : VideoInfo()
        data class Error(val error: YoutubeRepository.ErrorType) : VideoInfo()
    }
}
