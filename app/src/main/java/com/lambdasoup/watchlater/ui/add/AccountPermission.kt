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

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ContentAlpha
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.lambdasoup.watchlater.R
import com.lambdasoup.watchlater.ui.WatchLaterTextButton

@Composable
fun AccountPermission(
    onGrantPermissionsClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
    ) {
        Column(
            Modifier
                .weight(1f, fill = true)
                .padding(end = 16.dp)
        ) {
            Text(
                text = stringResource(id = R.string.add_permissions_description),
                style = MaterialTheme.typography.subtitle2,
            )
            CompositionLocalProvider(LocalContentAlpha.provides(ContentAlpha.medium)) {
                Text(
                    text = stringResource(id = R.string.add_permissions_rationale),
                    style = MaterialTheme.typography.caption,
                )
            }
        }

        WatchLaterTextButton(
            onClick = onGrantPermissionsClicked,
            label = R.string.add_permissions_grant
        )
    }
}
