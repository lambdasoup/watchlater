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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.lambdasoup.watchlater.ui.MenuAction
import com.lambdasoup.watchlater.viewmodel.AddViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lambdasoup.watchlater.R
import com.lambdasoup.watchlater.ui.OverflowMenu
import com.lambdasoup.watchlater.ui.WatchLaterTheme
import com.lambdasoup.watchlater.ui.padWithRoomForTextButtonContent

@Composable
fun AddScreen(
    onOverflowAction: (MenuAction) -> Unit,
    onSetAccount: () -> Unit,
    openPlaylistsOnYoutube: () -> Unit,
    onGrantPermissionsClicked: () -> Unit,
    actionListener: ActionView.ActionListener,
    viewModel: AddViewModel = viewModel(),
) {
    WatchLaterTheme {
        @Suppress("UNCHECKED_CAST")
        val viewState = viewModel.model.observeAsState() as State<AddViewModel.Model>

        Column {
            TopAppBar(
                title = { Text(text = stringResource(id = R.string.app_name)) },
                actions = {
                    OverflowMenu(onActionSelected = onOverflowAction)
                },
            )

            Account(
                modifier = Modifier
                    .padding(start = dimensionResource(id = R.dimen.activity_horizontal_margin))
                    .padWithRoomForTextButtonContent(end = dimensionResource(id = R.dimen.activity_horizontal_margin)),
                account = viewState.value.account,
                onSetAccount = onSetAccount,
            )

            Playlist(
                modifier = Modifier
                    .padding(start = dimensionResource(id = R.dimen.activity_horizontal_margin))
                    .padWithRoomForTextButtonContent(end = dimensionResource(id = R.dimen.activity_horizontal_margin)),
                playlist = viewState.value.targetPlaylist,
                onSetPlaylist = viewModel::changePlaylist,
            )

            AnimatedVisibility(visible = viewState.value.permissionNeeded == true) {
                AndroidView(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = dimensionResource(id = R.dimen.activity_horizontal_margin))
                        .padding(top = 8.dp),
                    factory = { context ->
                        PermissionsView(context).apply {
                            listener = PermissionsView.Listener(onGrantPermissionsClicked)
                        }
                    }
                )
            }

            AndroidView(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = dimensionResource(id = R.dimen.activity_horizontal_margin),
                        vertical = dimensionResource(id = R.dimen.activity_vertical_margin)
                    ),
                factory = { context ->
                    VideoView(context)
                },
                update = { view ->
                    viewState.value.let { view.setVideoInfo(it.videoInfo) }
                }
            )

            AndroidView(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = 12.dp,
                        vertical = 8.dp,
                    ),
                factory = { context ->
                    ActionView(context).apply {
                        listener = actionListener
                    }
                },
                update = { view ->
                    viewState.value.let { view.setState(it.videoAdd, it.videoId) }
                }
            )

            AndroidView(
                modifier = Modifier
                    .fillMaxWidth(),
                factory = { context ->
                    ResultView(context)
                },
                update = { view ->
                    viewState.value.let { view.onChanged(it.videoAdd) }
                }
            )
        }

        PlaylistSelection(
            onDialogDismiss = viewModel::clearPlaylists,
            openPlaylistsOnYoutube = openPlaylistsOnYoutube,
            onPlaylistSelected = viewModel::selectPlaylist,
            playlists = viewState.value.playlistSelection
        )
    }
}
