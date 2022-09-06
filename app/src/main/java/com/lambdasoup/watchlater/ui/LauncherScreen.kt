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

package com.lambdasoup.watchlater.ui

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lambdasoup.watchlater.R
import com.lambdasoup.watchlater.viewmodel.LauncherViewModel

// TODO: all the theming
@Composable
fun LauncherScreen(
    onOverflowAction: (MenuAction) -> Unit,
    viewModel: LauncherViewModel = viewModel(),
) {
    val viewState = viewModel.model.observeAsState()
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(id = R.string.app_name)) },
                actions = {
                    OverflowMenu(onActionSelected = onOverflowAction)
                }
            )
        },
        content = { appBarPadding ->
            Column(
                modifier = Modifier.padding(appBarPadding),
                verticalArrangement = Arrangement.Top
            ) {
                viewState.value!!.resolverProblems.let { resolverProblems ->
                    if (resolverProblems != null &&
                        ((resolverProblems.verifiedDomainsMissing > 0) || !resolverProblems.watchLaterIsDefault)
                    ) {
                        SetupGuideCard(
                            onYoutubeSettingsClick = viewModel::onYoutubeSettings,
                            onWatchLaterSettingsClick = viewModel::onWatchLaterSettings,
                        )
                    }
                }
                ExampleCard(onOpenExampleVideoClick = viewModel::onTryExample)
            }
        }
    )
}

@Composable
fun SetupGuideCard(
    onYoutubeSettingsClick: () -> Unit,
    onWatchLaterSettingsClick: () -> Unit,
) {
    Column(
        verticalArrangement = Arrangement.Top
    ) {
        SetupHeader()
        Divider()
        SetupYoutube(onYoutubeSettingsClick = onYoutubeSettingsClick)
        Divider()
        SetupWatchLater(onWatchLaterSettingsClick = onWatchLaterSettingsClick)
    }
}

@Composable
fun SetupHeader() {
    Text(text = stringResource(id = R.string.launcher_action_title))
    Text(text = stringResource(id = R.string.launcher_action_text))
}

@Composable
fun SetupStep(
    @StringRes title: Int,
    @StringRes text: Int,
    @StringRes buttonText: Int,
    onButtonClick: () -> Unit,
) {
    Text(text = stringResource(id = title))
    Text(text = stringResource(id = text))
    Button(onClick = onButtonClick) {
        Text(text = stringResource(id = buttonText))
    }
}

@Composable
fun SetupYoutube(onYoutubeSettingsClick: () -> Unit) =
    SetupStep(
        title = R.string.launcher_youtube_action_title,
        text = R.string.launcher_youtube_action_text,
        buttonText = R.string.launcher_youtube_action_button,
        onButtonClick = onYoutubeSettingsClick,
    )

@Composable
fun SetupWatchLater(onWatchLaterSettingsClick: () -> Unit) =
    SetupStep(
        title = R.string.launcher_watchlater_action_title,
        text = R.string.launcher_watchlater_action_text,
        buttonText = R.string.launcher_watchlater_action_button,
        onButtonClick = onWatchLaterSettingsClick
    )

@Composable
fun ExampleCard(
    onOpenExampleVideoClick: () -> Unit
) {
    Column() {
        Text(text = stringResource(id = R.string.launcher_example_title))
        Text(text = stringResource(id = R.string.launcher_example_text))
        Button(
            onClick = onOpenExampleVideoClick
        ) {
            Text(text = stringResource(id = R.string.launcher_example_button))
        }
    }
}