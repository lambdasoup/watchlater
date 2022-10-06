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

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import com.lambdasoup.watchlater.BuildConfig
import com.lambdasoup.watchlater.R
import com.lambdasoup.watchlater.ui.AboutActivity
import com.lambdasoup.watchlater.ui.HelpActivity
import com.lambdasoup.watchlater.ui.MenuAction
import com.lambdasoup.watchlater.viewmodel.LauncherViewModel
import com.lambdasoup.watchlater.viewmodel.LauncherViewModel.Event
import org.koin.androidx.viewmodel.ext.android.viewModel

class LauncherActivity : AppCompatActivity() {

    private val vm: LauncherViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LauncherScreen(
                onOverflowAction = this::onOverflowActionSelected,
                onYoutubeSettingsClick = vm::onYoutubeSettings,
                onOpenExampleVideoClick = this::openExampleVideo,
                onWatchLaterSettingsClick = this::openWatchLaterSettings,
                viewModel = vm.model,
            )
        }

        vm.events.observe(this) { event ->
            when (event) {
                Event.OpenYouTubeSettings -> openYoutubeSettings()
                Event.OpenExample -> openExampleVideo()
                Event.OpenWatchLaterSettings -> openWatchLaterSettings()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        vm.onResume()
    }

    private fun onOverflowActionSelected(menuAction: MenuAction) {
        when (menuAction) {
            MenuAction.About -> {
                startActivity(Intent(this, AboutActivity::class.java))
            }
            MenuAction.Help -> {
                startActivity(Intent(this, HelpActivity::class.java))
            }
            MenuAction.PrivacyPolicy -> {
                startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://lambdasoup.com/privacypolicy-watchlater/")
                    )
                )
            }
            MenuAction.Store -> {
                startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID)
                    )
                )
            }
        }
    }

    private fun openExampleVideo() {
        startActivity(EXAMPLE_INTENT)
    }

    private fun openYoutubeSettings() {
        val action = when {
            Build.VERSION.SDK_INT >= 31 -> Settings.ACTION_APP_OPEN_BY_DEFAULT_SETTINGS
            else -> Settings.ACTION_APPLICATION_DETAILS_SETTINGS
        }
        val intent = Intent(action, Uri.parse("package:com.google.android.youtube"))
        if (packageManager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY) == null) {
            Toast.makeText(this, R.string.no_youtube_settings_activity, Toast.LENGTH_SHORT).show()
            return
        }
        startActivity(intent)
    }

    private fun openWatchLaterSettings() {
        val action = when {
            Build.VERSION.SDK_INT >= 31 -> Settings.ACTION_APP_OPEN_BY_DEFAULT_SETTINGS
            else -> Settings.ACTION_APPLICATION_DETAILS_SETTINGS
        }
        val intent = Intent(action, Uri.parse("package:$packageName"))
        startActivity(intent)
    }

    companion object {
        private val EXAMPLE_URI = Uri.parse("https://www.youtube.com/watch?v=dGFSjKuJfrI")
        private val EXAMPLE_INTENT = Intent(Intent.ACTION_VIEW, EXAMPLE_URI)
    }
}
