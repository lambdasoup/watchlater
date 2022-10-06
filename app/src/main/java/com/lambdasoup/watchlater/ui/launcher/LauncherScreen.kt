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

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lambdasoup.watchlater.R
import com.lambdasoup.watchlater.ui.MenuAction
import com.lambdasoup.watchlater.ui.OverflowMenu
import com.lambdasoup.watchlater.ui.WatchLaterTheme
import com.lambdasoup.watchlater.viewmodel.LauncherViewModel
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LauncherScreen(
    onOverflowAction: (MenuAction) -> Unit,
    viewModel: LauncherViewModel = viewModel(),
) {
    WatchLaterTheme {
        val viewState = viewModel.model.observeAsState()

        // Sadly, AppBarHeight is private in androidx.compose.material.AppBar
        val topBarHeight = 56.dp
        val topBarHeightPx = with(LocalDensity.current) { topBarHeight.roundToPx().toFloat() }

        val topBarOffsetHeightPx = remember { mutableStateOf(0f) }
        val nestedScrollConnection = remember {
            object : NestedScrollConnection {
                override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                    // try to consume before LazyColumn to collapse toolbar if needed, hence pre-scroll
                    val delta = available.y
                    val newOffset = topBarOffsetHeightPx.value + delta
                    topBarOffsetHeightPx.value = newOffset.coerceIn(-topBarHeightPx, 0f)
                    // here's the catch: let's pretend we consumed 0 in any case, since we want
                    // Column to scroll anyway for good UX
                    // We're basically watching scroll without taking it
                    return Offset.Zero
                }
            }
        }
        val scrollState = rememberScrollState()
        if (scrollState.maxValue == Int.MAX_VALUE || scrollState.maxValue == 0) {
            // nestedScrollConnection won't learn when scrolling becomes impossible, so we check here
            topBarOffsetHeightPx.value = 0f
        }

        // Not using Scaffold here, because it has opinions on where the topBar is that aren't interested in the
        // offset due to scrolling.
        Box(
            modifier = Modifier
                .fillMaxSize()
                .nestedScroll(nestedScrollConnection),
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(scrollState)
            ) {
                Spacer(modifier = Modifier.height(topBarHeight))

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

            // placed last because of drawing order
            TopAppBar(
                modifier = Modifier
                    .height(topBarHeight)
                    .offset {
                        IntOffset(
                            x = 0,
                            y = topBarOffsetHeightPx.value.roundToInt()
                        )
                    },
                title = { Text(text = stringResource(id = R.string.app_name)) },
                actions = {
                    OverflowMenu(onActionSelected = onOverflowAction)
                },
            )
        }
    }
}

@Composable
fun SetupGuideCard(
    onYoutubeSettingsClick: () -> Unit,
    onWatchLaterSettingsClick: () -> Unit,
) {
    Card(
        modifier = Modifier.padding(
            horizontal = dimensionResource(id = R.dimen.activity_horizontal_margin),
            vertical = dimensionResource(id = R.dimen.activity_vertical_margin)
        )
    ) {
        Column {
            VerticalSpace()

            SetupHeader()

            VerticalSpace()
            Divider()
            VerticalSpace()

            SetupYoutube(onYoutubeSettingsClick = onYoutubeSettingsClick)

            VerticalSpace()
            Divider()
            VerticalSpace()

            SetupWatchLater(onWatchLaterSettingsClick = onWatchLaterSettingsClick)

            VerticalSpace()
        }
    }
}

@Composable
fun SetupHeader() {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Text(
            text = stringResource(id = R.string.launcher_action_title),
            style = MaterialTheme.typography.titleLarge,
        )
        VerticalSpace()
        CompositionLocalProvider(LocalContentColor provides LocalContentColor.current.copy(alpha = 0.4f)) {
            Text(
                text = stringResource(id = R.string.launcher_action_text),
                style = MaterialTheme.typography.bodyMedium
            )
        }
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
        modifier = Modifier
            .padding(
                end = dimensionResource(id = R.dimen.activity_horizontal_margin),
            )
    ) {
        Text(
            text = stringResource(id = title),
            style = MaterialTheme.typography.titleSmall,
        )
        VerticalSpace()
        CompositionLocalProvider(LocalContentColor provides LocalContentColor.current.copy(alpha = 0.4f)) {
            Text(
                text = stringResource(id = text),
                style = MaterialTheme.typography.bodyMedium
            )
        }
        VerticalSpace()
        TextButton(
            onClick = onButtonClick,
            content = { Text(stringResource(buttonText)) },
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
    Card(
        modifier = Modifier.padding(
            all = dimensionResource(id = R.dimen.activity_horizontal_margin),
        )
    ) {
        Column(
            modifier = Modifier
                .padding(vertical = 16.dp)
                .padding(end = 16.dp)
        ) {
            Text(
                text = stringResource(id = R.string.launcher_example_title),
                style = MaterialTheme.typography.titleLarge,
            )
            VerticalSpace()
            CompositionLocalProvider(LocalContentColor provides LocalContentColor.current.copy(alpha = 0.4f)) {
                Text(
                    text = stringResource(id = R.string.launcher_example_text),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            VerticalSpace()
            TextButton(
                onClick = onOpenExampleVideoClick,
                content = { Text(stringResource(R.string.launcher_example_button)) },
            )
        }
    }
}

@Composable
private fun VerticalSpace() = Spacer(modifier = Modifier.height(16.dp))
