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

import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ContentAlpha
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.lambdasoup.watchlater.R
import com.lambdasoup.watchlater.data.YoutubeRepository
import com.lambdasoup.watchlater.ui.WatchLaterTextButton
import com.lambdasoup.watchlater.ui.padAlignTextButtonContentStart
import com.lambdasoup.watchlater.ui.padWithRoomForTextButtonContent

@Composable
fun PlaylistSelection(
    onDialogDismiss: () -> Unit,
    openPlaylistsOnYoutube: () -> Unit,
    onPlaylistSelected: (YoutubeRepository.Playlists.Playlist) -> Unit,
    playlists: YoutubeRepository.Playlists?,
) {
    if (playlists == null) {
        return
    }

    NeutralButtonDialog(
        onDialogDismiss = onDialogDismiss,
        titleRes = R.string.playlist_selection_title,
        onButtonClick = openPlaylistsOnYoutube,
        buttonLabelRes = if (playlists.items.isNotEmpty()) {
            R.string.playlist_selection_edit
        } else {
            R.string.playlist_selection_create
        },
    ) {
        if (playlists.items.isNotEmpty()) {
            Column {
                playlists.items.forEach { playlist ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .requiredHeight(dimensionResource(id = R.dimen.touch_target_min_size))
                            .clickable { onPlaylistSelected(playlist) }
                    ) {
                        Text(
                            text = playlist.snippet.title,
                            modifier = Modifier
                                .align(Alignment.CenterVertically)
                        )
                    }
                }
            }
        } else {
            Text(
                text = stringResource(id = R.string.playlist_selection_message),
                style = MaterialTheme.typography.body2
            )
        }
    }
}

@Composable
private fun NeutralButtonDialog(
    onDialogDismiss: () -> Unit,
    @StringRes titleRes: Int,
    onButtonClick: () -> Unit,
    @StringRes buttonLabelRes: Int,
    content: @Composable () -> Unit,
) {
    Dialog(
        onDismissRequest = onDialogDismiss,
    ) {
        Surface(
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colors.surface,
            contentColor = MaterialTheme.colors.onSurface,
            modifier = Modifier.padding(24.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(vertical = 24.dp)
                    .padWithRoomForTextButtonContent(start = 24.dp)
                    .padding(end = 24.dp)
            ) {
                // Title
                CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.high) {
                    Text(
                        text = stringResource(id = titleRes),
                        modifier = Modifier
                            .padding(bottom = 12.dp)
                            .padAlignTextButtonContentStart(),
                        style = MaterialTheme.typography.subtitle1,
                    )
                }

                // content
                CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                    val scrollState = rememberScrollState()
                    Box(
                        modifier = Modifier
                            .verticalScroll(scrollState)
                            .weight(weight = 1f, fill = false)
                            .padAlignTextButtonContentStart(),
                    ) {
                        content()
                    }
                }

                // single button in neutral position
                WatchLaterTextButton(
                    modifier = Modifier.padding(top = 12.dp),
                    onClick = onButtonClick,
                    label = buttonLabelRes,
                )
            }
        }
    }
}
