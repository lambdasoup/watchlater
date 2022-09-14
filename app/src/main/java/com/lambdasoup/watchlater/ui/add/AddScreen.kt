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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lambdasoup.watchlater.R
import com.lambdasoup.watchlater.ui.MenuAction
import com.lambdasoup.watchlater.ui.OverflowMenu
import com.lambdasoup.watchlater.ui.WatchLaterTheme
import com.lambdasoup.watchlater.ui.padWithRoomForTextButtonContent
import com.lambdasoup.watchlater.viewmodel.AddViewModel

@Composable
fun AddScreen(
    onOverflowAction: (MenuAction) -> Unit,
    onSetAccount: () -> Unit,
    openPlaylistsOnYoutube: () -> Unit,
    onGrantPermissionsClicked: () -> Unit,
    onWatchNowClicked: () -> Unit,
    onWatchLaterClicked: (String) -> Unit,
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
                AccountPermission(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = dimensionResource(id = R.dimen.activity_horizontal_margin))
                        .padWithRoomForTextButtonContent(end = dimensionResource(id = R.dimen.activity_horizontal_margin))
                        .padding(top = 8.dp),
                    onGrantPermissionsClicked = onGrantPermissionsClicked,
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
                    view.setVideoInfo(viewState.value.videoInfo)
                }
            )

            Actions(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = dimensionResource(id = R.dimen.activity_horizontal_margin),
                        vertical = dimensionResource(id = R.dimen.activity_vertical_margin),
                    ),
                state = viewState.value.videoAdd,
                videoId = viewState.value.videoId,
                onWatchNowClicked = onWatchNowClicked,
                onWatchLaterClicked = onWatchLaterClicked,
            )

            Result(
                modifier = Modifier.fillMaxWidth(),
                videoAdd = viewState.value.videoAdd,
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