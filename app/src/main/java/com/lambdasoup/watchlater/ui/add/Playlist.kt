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

import androidx.compose.foundation.layout.Row
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.lambdasoup.watchlater.R
import com.lambdasoup.watchlater.data.YoutubeRepository.Playlists.Playlist
import com.lambdasoup.watchlater.ui.WatchLaterTextButton

@Composable
fun Playlist(
    modifier: Modifier = Modifier,
    playlist: Playlist?,
    onSetPlaylist: () -> Unit,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            modifier = Modifier.weight(1f, fill = true),
            text = playlist?.snippet?.title ?: stringResource(id = R.string.playlist_empty),
            style = MaterialTheme.typography.body2
        )

        WatchLaterTextButton(
            onClick = onSetPlaylist,
            label = R.string.account_set
        )
    }
}
