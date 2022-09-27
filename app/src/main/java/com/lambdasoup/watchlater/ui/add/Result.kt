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
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.lambdasoup.watchlater.R
import com.lambdasoup.watchlater.ui.success
import com.lambdasoup.watchlater.viewmodel.AddViewModel.VideoAdd

@Composable
fun Result(
    videoAdd: VideoAdd,
    modifier: Modifier = Modifier,
) {
    when (videoAdd) {
        is VideoAdd.Idle, VideoAdd.Progress -> {
            // nothing to show
        }
        is VideoAdd.Success -> ShowSuccess(modifier = modifier)
        is VideoAdd.Error -> ShowError(errorType = videoAdd.error, modifier = modifier)
        is VideoAdd.HasIntent -> ShowResult(
            isError = true,
            R.string.needs_youtube_permissions,
            modifier,
        )
    }
}

@Composable
private fun ShowResult(
    isError: Boolean,
    @StringRes msgResId: Int,
    modifier: Modifier,
) {
    ShowResult(isError = isError, msg = stringResource(id = msgResId), modifier = modifier)
}

@Composable
private fun ShowResult(
    isError: Boolean,
    msg: String,
    modifier: Modifier,
) {
    Text(
        modifier = modifier
            .background(color = if (isError) MaterialTheme.colors.error else MaterialTheme.colors.success)
            .padding(all = 4.dp),
        text = msg,
        style = MaterialTheme.typography.caption.merge(
            TextStyle(
                color = MaterialTheme.colors.onError,
                textAlign = TextAlign.Center,
            )
        )
    )
}

@Composable
private fun ShowSuccess(
    modifier: Modifier,
) {
    ShowResult(isError = false, msgResId = R.string.success_added_video, modifier = modifier)
}

@Composable
private fun ShowError(
    errorType: VideoAdd.ErrorType,
    modifier: Modifier,
) {
    val errorStr: String = when (errorType) {
        is VideoAdd.ErrorType.NoAccount -> stringResource(R.string.error_no_account)
        is VideoAdd.ErrorType.NoPermission -> stringResource(R.string.error_no_permission)
        is VideoAdd.ErrorType.NoPlaylistSelected -> stringResource(R.string.error_no_playlist)
        is VideoAdd.ErrorType.Network -> stringResource(id = R.string.error_network)
        is VideoAdd.ErrorType.Other -> stringResource(R.string.error_general, errorType.msg)
    }
    ShowResult(
        isError = true,
        msg = stringResource(id = R.string.could_not_add, errorStr),
        modifier = modifier,
    )
}
