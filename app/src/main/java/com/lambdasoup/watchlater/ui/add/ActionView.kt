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
package com.lambdasoup.watchlater.ui.add

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import com.lambdasoup.watchlater.R
import com.lambdasoup.watchlater.viewmodel.AddViewModel.VideoAdd

class ActionView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    var listener: ActionListener? = null

    init {
        LayoutInflater.from(context).inflate(R.layout.view_action, this)
    }
    
    private val watchNowButton = findViewById<View>(R.id.action_watchnow)
    init {
        watchNowButton.setOnClickListener { listener?.onWatchNowClicked() }
    }

    private val watchLaterButton = findViewById<View>(R.id.action_watchlater)

    fun setState(state: VideoAdd, videoId: String?) {
        when (state) {
            VideoAdd.Progress -> setProgress(true)
            else -> setProgress(false)
        }

        if (videoId != null) {
            watchLaterButton.isEnabled = true
            watchNowButton.isEnabled = true
            watchLaterButton.setOnClickListener { listener?.onWatchLaterClicked(videoId) }
        } else {
            watchLaterButton.isEnabled = false
            watchNowButton.isEnabled = false
        }
    }

    private fun setProgress(progress: Boolean) {
        findViewById<View>(R.id.action_watchlater).visibility = if (progress) INVISIBLE else VISIBLE
        findViewById<View>(R.id.action_progress).visibility = if (progress) VISIBLE else INVISIBLE
    }

    interface ActionListener {
        fun onWatchNowClicked()
        fun onWatchLaterClicked(videoId: String)
    }
}
