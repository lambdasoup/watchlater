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
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import com.lambdasoup.watchlater.R
import com.lambdasoup.watchlater.viewmodel.AddViewModel.VideoAdd

class ActionView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr), View.OnClickListener {

    var listener: ActionListener? = null

    init {
        LayoutInflater.from(context).inflate(R.layout.view_action, this)
        findViewById<View>(R.id.action_watchnow).setOnClickListener(this)
        findViewById<View>(R.id.action_watchlater).setOnClickListener(this)
    }

    fun setState(state: VideoAdd) {
        when (state) {
            VideoAdd.Progress -> setProgress(true)
            else -> setProgress(false)
        }
    }

    private fun setProgress(progress: Boolean) {
        findViewById<View>(R.id.action_watchlater).visibility = if (progress) INVISIBLE else VISIBLE
        findViewById<View>(R.id.action_progress).visibility = if (progress) VISIBLE else INVISIBLE
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.action_watchlater -> listener?.onWatchLaterClicked()
            R.id.action_watchnow -> listener?.onWatchNowClicked()
        }
    }

    interface ActionListener {
        fun onWatchNowClicked()
        fun onWatchLaterClicked()
    }
}
