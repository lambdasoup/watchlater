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

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Indication
import androidx.compose.foundation.IndicationInstance
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.lambdasoup.watchlater.R
import com.lambdasoup.watchlater.data.YoutubeRepository
import com.lambdasoup.watchlater.ui.MenuAction
import com.lambdasoup.watchlater.ui.OverflowMenu
import com.lambdasoup.watchlater.ui.WatchLaterTheme
import com.lambdasoup.watchlater.ui.padWithRoomForTextButtonContent
import com.lambdasoup.watchlater.viewmodel.AddViewModel

@Composable
fun AddScreen(
    onClickOutside: () -> Unit,
    onOverflowAction: (MenuAction) -> Unit,
    onSetAccount: () -> Unit,
    openPlaylistsOnYoutube: () -> Unit,
    onGrantPermissionsClicked: () -> Unit,
    onWatchNowClicked: () -> Unit,
    onWatchLaterClicked: (String) -> Unit,
    onChangePlaylistClicked: () -> Unit,
    onAbortChangePlaylist: () -> Unit,
    onSelectPlaylist: (YoutubeRepository.Playlists.Playlist) -> Unit,
    viewModel: LiveData<AddViewModel.Model>,
) {
    WatchLaterTheme {
        @Suppress("UNCHECKED_CAST")
        val viewState = viewModel.observeAsState() as State<AddViewModel.Model>

        // no ripple when dismissing activity by clicking "outside"
        // fill fully to avoid janky layout from the window resizing itself to wrap its compose content -
        // so we emulate the "dialog style" by hand
        val interactionsClickOutside = remember { MutableInteractionSource() }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(
                    interactionSource = interactionsClickOutside,
                    indication = NoIndication,
                    onClick = onClickOutside
                ),
            contentAlignment = Alignment.Center,
        ) {
            Surface(
                modifier = Modifier
                    .widthIn(max = 480.dp)
                    .fillMaxWidth()
                    // padding _seems_ unbalanced, but what with the automatically added status bar padding - have a
                    // look in landscape orientation.
                    .padding(horizontal = 32.dp)
                    .padding(top = 8.dp, bottom = 32.dp)
                    .clickable(
                        enabled = false,
                        onClick = {}
                    ),
            ) {

                Column {
                    TopAppBar(
                        modifier = Modifier.fillMaxWidth(),
                        title = { Text(text = stringResource(id = R.string.app_name)) },
                        actions = {
                            OverflowMenu(onActionSelected = onOverflowAction)
                        },
                    )

                    Account(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = dimensionResource(id = R.dimen.activity_horizontal_margin))
                            .padWithRoomForTextButtonContent(end = dimensionResource(id = R.dimen.activity_horizontal_margin)),
                        account = viewState.value.account,
                        onSetAccount = onSetAccount,
                    )

                    Playlist(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = dimensionResource(id = R.dimen.activity_horizontal_margin))
                            .padWithRoomForTextButtonContent(end = dimensionResource(id = R.dimen.activity_horizontal_margin)),
                        playlist = viewState.value.targetPlaylist,
                        onSetPlaylist = onChangePlaylistClicked,
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

                    VideoSnippet(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                horizontal = dimensionResource(id = R.dimen.activity_horizontal_margin),
                                vertical = dimensionResource(id = R.dimen.activity_vertical_margin)
                            ),
                        videoInfo = viewState.value.videoInfo,
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
                    onDialogDismiss = onAbortChangePlaylist,
                    openPlaylistsOnYoutube = openPlaylistsOnYoutube,
                    onPlaylistSelected = onSelectPlaylist,
                    playlists = viewState.value.playlistSelection
                )
            }
        }
    }
}

private object NoIndication : Indication {
    private object NoIndicationInstance : IndicationInstance {
        override fun ContentDrawScope.drawIndication() {
            drawContent()
        }
    }

    @Composable
    override fun rememberUpdatedInstance(interactionSource: InteractionSource): IndicationInstance {
        return NoIndicationInstance
    }
}

@Preview(name = "deutsch", locale = "de")
@Preview(name = "english", locale = "en")
@Preview(name = "night", uiMode = UI_MODE_NIGHT_YES)
@Composable
fun AddScreenPreview() = AddScreen(
    onClickOutside = {},
    onOverflowAction = {},
    onSetAccount = {},
    openPlaylistsOnYoutube = {},
    onGrantPermissionsClicked = {},
    onWatchNowClicked = {},
    onWatchLaterClicked = {},
    onChangePlaylistClicked = {},
    onSelectPlaylist = {},
    onAbortChangePlaylist = {},
    viewModel = MutableLiveData(
        AddViewModel.Model(
            videoId = "foo",
            videoAdd = AddViewModel.VideoAdd.Success,
            videoInfo = AddViewModel.VideoInfo.Progress,
            account = null,
            permissionNeeded = false,
            tokenRetried = false,
            targetPlaylist = null,
            playlistSelection = null,
        )
    )
)
