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

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.lambdasoup.watchlater.R
import com.lambdasoup.watchlater.data.YoutubeRepository
import com.lambdasoup.watchlater.data.YoutubeRepository.Videos
import com.lambdasoup.watchlater.util.formatDuration
import com.lambdasoup.watchlater.viewmodel.AddViewModel
import com.lambdasoup.watchlater.viewmodel.AddViewModel.VideoInfo.*

class VideoView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val content: View
    private val progress: View
    private val error: View
    private val animationDuration: Int

    init {
        LayoutInflater.from(context).inflate(R.layout.view_video, this)
        content = findViewById(R.id.video_content)
        progress = findViewById(R.id.video_progress)
        error = findViewById(R.id.video_error)
        animationDuration = resources.getInteger(android.R.integer.config_shortAnimTime)
    }

    fun setVideoInfo(info: AddViewModel.VideoInfo) {
        return when (info) {
            is Progress -> showProgress()
            is Loaded -> showData(info.data)
            is Error -> showError(info.error)
        }
    }

    private fun showData(item: Videos.Item) {
        show(content)
        val title: TextView = content.findViewById(R.id.title)
        title.text = item.snippet.title
        val description: TextView = content.findViewById(R.id.description)
        description.text = item.snippet.description
        val duration: TextView = content.findViewById(R.id.duration)
        val formatted: String = formatDuration(item.contentDetails.duration)
        duration.text = formatted
        val thumbnailView: ImageView = findViewById(R.id.thumbnail)
        Glide.with(context)
                .load(item.snippet.thumbnails.medium.url)
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(thumbnailView)
    }

    private fun showError(errorType: ErrorType) {
        show(error)
        val reason: TextView = error.findViewById(R.id.reason)
        when (errorType) {
            is ErrorType.Youtube -> when (errorType.error) {
                YoutubeRepository.ErrorType.Network -> reason.setText(R.string.error_network)
                YoutubeRepository.ErrorType.VideoNotFound -> reason.setText(R.string.error_video_not_found)
                else -> reason.text = resources.getString(R.string.could_not_load, errorType.toString())
            }
            is ErrorType.InvalidVideoId -> reason.setText(R.string.error_invalid_video_id)
            is ErrorType.NoAccount -> reason.setText(R.string.error_video_info_no_account)
        }
    }

    private fun showProgress() {
        show(progress)
    }

    private fun show(view: View) {
        val current = visibleChild()
        if (view === current) {
            return
        }
        view.alpha = 0f
        view.visibility = View.VISIBLE
        view.animate()
                .alpha(1f)
                .setDuration(animationDuration.toLong())
                .setListener(null)
        current.animate()
                .alpha(0f)
                .setDuration(animationDuration.toLong())
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        current.visibility = View.INVISIBLE
                    }
                })
    }

    private fun visibleChild(): View {
        for (view in arrayOf(content, progress, error)) {
            if (view.visibility == View.VISIBLE) {
                return view
            }
        }
        throw IllegalStateException("no visible children")
    }
}
