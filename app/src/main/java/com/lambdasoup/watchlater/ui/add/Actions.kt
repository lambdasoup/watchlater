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

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lambdasoup.watchlater.R
import com.lambdasoup.watchlater.ui.WatchLaterButton
import com.lambdasoup.watchlater.viewmodel.AddViewModel.VideoAdd

@Composable
fun Actions(
    state: VideoAdd,
    videoId: String?,
    onWatchNowClicked: () -> Unit,
    onWatchLaterClicked: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val hasVideo = videoId != null
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        WatchLaterButton(
            modifier = Modifier.weight(1f, fill = true),
            onClick = onWatchNowClicked,
            enabled = hasVideo,
            label = R.string.action_watchnow,
        )

        Spacer(modifier = Modifier.width(16.dp))

        Box(
            modifier = Modifier
                .weight(1f, fill = true)
        ) {
            if (state == VideoAdd.Progress) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = MaterialTheme.colors.secondary,
                )
            } else {
                WatchLaterButton(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { videoId?.let { onWatchLaterClicked(it) } },
                    enabled = hasVideo,
                    label = R.string.action_watchlater,
                )
            }
        }
    }
}
