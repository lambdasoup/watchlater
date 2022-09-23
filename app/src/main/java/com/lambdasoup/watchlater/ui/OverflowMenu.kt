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
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import com.lambdasoup.watchlater.R

@Composable
fun OverflowMenu(
    onActionSelected: (MenuAction) -> Unit,
    actions: List<MenuAction> = listOf(MenuAction.Help, MenuAction.Store, MenuAction.About, MenuAction.PrivacyPolicy)
) {
    var showMenu by remember { mutableStateOf(false) }

    CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.high) {
        IconButton(
            onClick = {
                showMenu = !showMenu
            }) {
            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = stringResource(id = R.string.menu_overflow),
            )
        }
    }
    DropdownMenu(
        expanded = showMenu,
        onDismissRequest = { showMenu = false },
    ) {
        actions.forEach { action ->
            DropdownMenuItem(
                onClick = {
                    showMenu = false
                    onActionSelected(action)
                }
            ) {
                Text(stringResource(id = action.label))
            }
        }
    }
}

sealed class MenuAction(
    @StringRes val label: Int,
) {
    object Help : MenuAction(R.string.menu_help)
    object Store : MenuAction(R.string.menu_store)
    object About : MenuAction(R.string.menu_about)
    object PrivacyPolicy : MenuAction(R.string.menu_privacypolicy)
}
