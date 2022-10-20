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

package com.lambdasoup.watchlater.ui.launcher

import android.content.res.Configuration
import androidx.annotation.StringRes
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.OpenInNew
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonElevation
import androidx.compose.material3.Card
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.modifier.modifierLocalConsumer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.lambdasoup.watchlater.R
import com.lambdasoup.watchlater.data.IntentResolverRepository
import com.lambdasoup.watchlater.ui.MenuAction
import com.lambdasoup.watchlater.ui.OverflowMenu
import com.lambdasoup.watchlater.ui.WatchLaterTheme
import com.lambdasoup.watchlater.viewmodel.LauncherViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LauncherScreen(
    onOverflowAction: (MenuAction) -> Unit,
    onYoutubeSettingsClick: () -> Unit,
    onWatchLaterSettingsClick: () -> Unit,
    onOpenExampleVideoClick: () -> Unit,
    viewModel: LiveData<LauncherViewModel.Model>,
) {
    WatchLaterTheme {
        // Remember a SystemUiController
        val systemUiController = rememberSystemUiController()
        val useDarkIcons = !isSystemInDarkTheme()

        val primaryColor = MaterialTheme.colorScheme.primary
        DisposableEffect(systemUiController, useDarkIcons) {
            systemUiController.setStatusBarColor(
                color = primaryColor,
                darkIcons = false,
            )

            systemUiController.setNavigationBarColor(
                color = Color.Transparent,
                darkIcons = useDarkIcons,
            )

            onDispose {}
        }

        val viewState = viewModel.observeAsState()

        Scaffold(
            topBar = {
                TopAppBar(
                    colors = TopAppBarDefaults.smallTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    ),
                    title = { Text(text = stringResource(id = R.string.app_name)) },
                    actions = {
                        OverflowMenu(onActionSelected = onOverflowAction)
                    },
                )
            },
            content = { paddingValues ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(
                            top = paddingValues.calculateTopPadding() + 8.dp,
                            start = 8.dp,
                            end = 8.dp,
                        )
                        .verticalScroll(state = rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    viewState.value!!.resolverProblems.let { resolverProblems ->
                        if (resolverProblems != null &&
                            ((resolverProblems.verifiedDomainsMissing > 0) || !resolverProblems.watchLaterIsDefault)
                        ) {
                            SetupGuideCard(
                                onYoutubeSettingsClick = onYoutubeSettingsClick,
                                onWatchLaterSettingsClick = onWatchLaterSettingsClick,
                            )
                        }
                    }
                    ExampleCard(onOpenExampleVideoClick = onOpenExampleVideoClick)
                }
            }
        )
    }
}

@Composable
fun SetupGuideCard(
    onYoutubeSettingsClick: () -> Unit,
    onWatchLaterSettingsClick: () -> Unit,
) {
    Card {
        Column(
            modifier = Modifier.padding(all = 8.dp),
        ) {
            SetupHeader()

            Divider()

            SetupYoutube(onYoutubeSettingsClick = onYoutubeSettingsClick)

            Divider()

            SetupWatchLater(onWatchLaterSettingsClick = onWatchLaterSettingsClick)
        }
    }
}

@Composable
fun SetupHeader() {
    Column(
        modifier = Modifier.padding(
            horizontal = 16.dp,
        )
    ) {
        Text(
            text = stringResource(id = R.string.launcher_action_title),
            style = MaterialTheme.typography.titleLarge,
        )
        Text(
            modifier = Modifier.padding(vertical = 16.dp),
            text = stringResource(id = R.string.launcher_action_text),
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
fun SetupStep(
    @StringRes title: Int,
    @StringRes text: Int,
    @StringRes buttonText: Int,
    onButtonClick: () -> Unit,
) {
    Column(
        modifier = Modifier.padding(
            vertical = 8.dp,
            horizontal = 16.dp,
        )
    ) {
        Text(
            modifier = Modifier.padding(vertical = 8.dp),
            text = stringResource(id = title),
            style = MaterialTheme.typography.titleSmall,
        )
        Text(
            text = stringResource(id = text),
        )
        TextButton(
            modifier = Modifier.align(Alignment.End),
            onClick = onButtonClick,
            content = {
                Row() {
                    Text(stringResource(buttonText))
                    Icon(imageVector = Icons.Outlined.OpenInNew, contentDescription = null)
                }
            }
        )
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
    onOpenExampleVideoClick: () -> Unit,
) {
    Card {
        Column(
            modifier = Modifier
                .padding(vertical = 16.dp)
                .padding(end = 16.dp)
        ) {
            Text(
                text = stringResource(id = R.string.launcher_example_title),
                style = MaterialTheme.typography.titleLarge,
            )
            Text(
                text = stringResource(id = R.string.launcher_example_text),
                style = MaterialTheme.typography.bodyMedium,
            )
            TextButton(
                onClick = onOpenExampleVideoClick,
                content = { Text(stringResource(R.string.launcher_example_button)) },
            )
        }
    }
}

@Composable
private fun VerticalSpace() = Spacer(modifier = Modifier.height(16.dp))

@Preview(name = "english", locale = "en")
@Preview(name = "night", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(name = "deutsch", locale = "de")
@Composable
fun LauncherScreenPreview() =
    LauncherScreen(
        onOverflowAction = {},
        onYoutubeSettingsClick = {},
        onWatchLaterSettingsClick = {},
        onOpenExampleVideoClick = {},
        viewModel = MutableLiveData(
            LauncherViewModel.Model(
                resolverProblems = IntentResolverRepository.ResolverProblems(
                    watchLaterIsDefault = false,
                    verifiedDomainsMissing = 3,
                )
            )
        )
    )
