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

import android.content.Context
import android.content.Intent
import android.content.Intent.ACTION_VIEW
import android.webkit.WebResourceRequest
import android.webkit.WebView
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.google.accompanist.web.AccompanistWebViewClient
import com.google.accompanist.web.WebView
import com.google.accompanist.web.rememberWebViewState
import com.lambdasoup.watchlater.R

@Composable
fun WebviewScreen(
    initialUrl: String,
    onUpNavigation: () -> Unit,
) {
    WatchLaterTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(text = stringResource(id = R.string.app_name)) },
                    navigationIcon = {
                        IconButton(onClick = onUpNavigation) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = stringResource(id = R.string.navigation_close)
                            )
                        }
                    },
                )
            }
        ) { padding ->
            val state = rememberWebViewState(url = initialUrl)
            val context = LocalContext.current

            WebView(
                modifier = Modifier.padding(padding),
                state = state,
                captureBackPresses = true,
                client = remember { ExternalOpeningWebViewClient(context) }
            )
        }
    }
}

private class ExternalOpeningWebViewClient(
    private val context: Context,
) : AccompanistWebViewClient() {
    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
        if (request?.url?.scheme != "file") {
            context.startActivity(
                Intent(ACTION_VIEW, request?.url)
            )
            return true
        }
        return super.shouldOverrideUrlLoading(view, request)
    }
}
