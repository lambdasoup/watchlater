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

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.ContentAlpha
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import com.google.accompanist.placeholder.PlaceholderHighlight
import com.google.accompanist.placeholder.material.placeholder
import com.google.accompanist.placeholder.material.shimmer
import com.lambdasoup.watchlater.R
import com.lambdasoup.watchlater.data.YoutubeRepository
import com.lambdasoup.watchlater.util.formatDuration
import com.lambdasoup.watchlater.viewmodel.AddViewModel.VideoInfo

@Composable
fun VideoSnippet(
    videoInfo: VideoInfo,
    modifier: Modifier = Modifier,
) {
    val loading = videoInfo is VideoInfo.Progress

    val painter = when (videoInfo) {
        is VideoInfo.Loaded -> rememberAsyncImagePainter(model = videoInfo.data.snippet.thumbnails.medium.url)
        else -> remember { ColorPainter(Color.Transparent) }
    }

    val imageLoaded = videoInfo is VideoInfo.Loaded &&
            (painter as AsyncImagePainter).state is AsyncImagePainter.State.Success

    val headerText = when (videoInfo) {
        is VideoInfo.Loaded -> videoInfo.data.snippet.title
        is VideoInfo.Error -> stringResource(id = R.string.video_error_title)
        is VideoInfo.Progress -> "Lorem Ipsum"
    }

    val subheaderText = when (videoInfo) {
        is VideoInfo.Loaded -> formatDuration(videoInfo.data.contentDetails.duration)
        is VideoInfo.Progress -> "12:34"
        is VideoInfo.Error -> ""
    }

    val bodyText = when (videoInfo) {
        is VideoInfo.Loaded -> videoInfo.data.snippet.description
        is VideoInfo.Error -> errorText(errorType = videoInfo.error)
        is VideoInfo.Progress -> "Lorem ipsum dolor sit amet yadda yadda this is loading still"
    }

    Row(
        modifier = modifier,
    ) {
        AnimatedVisibility(
            visible = videoInfo !is VideoInfo.Error,
        ) {
            Image(
                modifier = Modifier
                    .width(160.dp)
                    .height(90.dp)
                    .padding(end = 16.dp)
                    .placeholder(
                        visible = loading || !imageLoaded,
                        highlight = PlaceholderHighlight.shimmer()
                    ),
                painter = painter,
                contentDescription = stringResource(id = R.string.thumbnail_cd)
            )
        }

        Column(
            modifier = Modifier.weight(1f),
        ) {
            Text(
                modifier = Modifier
                    .placeholder(
                        visible = loading,
                        highlight = PlaceholderHighlight.shimmer()
                    ),
                text = headerText,
                style = MaterialTheme.typography.subtitle2,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

            CompositionLocalProvider(LocalContentAlpha.provides(ContentAlpha.medium)) {
                Text(
                    modifier = Modifier
                        .placeholder(
                            visible = loading,
                            highlight = PlaceholderHighlight.shimmer()
                        ),
                    text = subheaderText,
                    maxLines = 1,
                    style = MaterialTheme.typography.body2,
                    fontStyle = FontStyle.Italic,
                    overflow = TextOverflow.Ellipsis,
                )

                Text(
                    modifier = Modifier
                        .placeholder(
                            visible = loading,
                            highlight = PlaceholderHighlight.shimmer()
                        ),
                    text = bodyText,
                    style = MaterialTheme.typography.caption,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun errorText(
    errorType: VideoInfo.ErrorType,
): String =
    when (errorType) {
        is VideoInfo.ErrorType.Youtube -> when (errorType.error) {
            YoutubeRepository.ErrorType.Network -> stringResource(id = R.string.error_network)
            YoutubeRepository.ErrorType.VideoNotFound -> stringResource(id = R.string.error_video_not_found)
            else -> stringResource(id = R.string.could_not_load,
                                   errorType.toString())
        }
        is VideoInfo.ErrorType.InvalidVideoId -> stringResource(id = R.string.error_invalid_video_id)
        is VideoInfo.ErrorType.NoAccount -> stringResource(id = R.string.error_video_info_no_account)
        is VideoInfo.ErrorType.Network -> stringResource(id = R.string.error_network)
        is VideoInfo.ErrorType.Other -> stringResource(id = R.string.error_general, errorType.msg)
    }
