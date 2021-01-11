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
package com.lambdasoup.watchlater.ui

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import androidx.lifecycle.Observer
import com.lambdasoup.watchlater.R
import com.lambdasoup.watchlater.viewmodel.AddViewModel.VideoAdd

class ResultView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
) : AppCompatTextView(context, attrs, defStyleAttr), Observer<VideoAdd> {

    override fun onChanged(videoAdd: VideoAdd) {
        when (videoAdd) {
            is VideoAdd.Idle, VideoAdd.Progress -> clearResult()
            is VideoAdd.Success -> setSuccess()
            is VideoAdd.Error -> setError(videoAdd.error)
            is VideoAdd.HasIntent -> setResult(true, resources.getString(R.string.needs_youtube_permissions))
        }
    }

    private fun setSuccess() {
        setResult(false, resources.getString(R.string.success_added_video))
    }

    private fun setError(errorType: VideoAdd.ErrorType) {
        val errorStr: String = when (errorType) {
            VideoAdd.ErrorType.NoAccount -> resources.getString(R.string.error_no_account)
            VideoAdd.ErrorType.NoPermission -> resources.getString(R.string.error_no_permission)
            VideoAdd.ErrorType.YoutubeAlreadyInPlaylist -> resources.getString(R.string.error_already_in_playlist)
            else -> resources.getString(R.string.error_general, errorType.name)
        }
        setResult(true, resources.getString(R.string.could_not_add, errorStr))
    }

    private fun setResult(error: Boolean, msg: String) {
        visibility = VISIBLE
        if (error) {
            setBackgroundColor(resources.getColor(R.color.error_color, context.theme))
        } else {
            setBackgroundColor(resources.getColor(R.color.success_color, context.theme))
        }
        text = msg
    }

    private fun clearResult() {
        visibility = GONE
    }
}
