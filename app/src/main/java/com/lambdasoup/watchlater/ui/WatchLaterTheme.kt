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
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp

private val Lightblue600 = Color(0xFF039BE5)
private val Lightblue600Dark = Color(0xFF006DB3)
private val Red600 = Color(0xFFE53935)
private val Red600Dark = Color(0xFFAB000D)
private val LightGrey = Color(0xFFF0F0F0)

private val DarkColors = darkColors(
    // using default Material palette
)

private val LightColors = lightColors(
    primary = Red600,
    primaryVariant = Red600Dark,
    secondary = Lightblue600,
    secondaryVariant = Lightblue600Dark,
    background = LightGrey,
)

@Composable
fun WatchLaterTheme(
    content: @Composable () -> Unit
) {
    val isDark = isSystemInDarkTheme()
    MaterialTheme(
        colors = if (isDark) DarkColors else LightColors,
        typography = Typography(),
        shapes = Shapes(),
        content = content
    )
}

@Composable
fun WatchLaterTextButton(
    onClick: () -> Unit,
    @StringRes label: Int,
) = TextButton(
    onClick = onClick,
    colors = ButtonDefaults.textButtonColors(
        contentColor = MaterialTheme.colors.secondary
    ),
    // No default text button padding in this project. Makes it so much easier to align the buttons with everything
    // else.
    contentPadding = PaddingValues(all = 0.dp),
) {
    Text(
        text = stringResource(id = label).uppercase(),
        style = MaterialTheme.typography.button
    )
}