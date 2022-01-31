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

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.lambdasoup.watchlater.BuildConfig
import com.lambdasoup.watchlater.R
import com.lambdasoup.watchlater.WatchLaterApplication
import com.lambdasoup.watchlater.viewmodel.LauncherViewModel
import com.lambdasoup.watchlater.viewmodel.LauncherViewModel.Event

class LauncherActivity : AppCompatActivity() {

    private val vm: LauncherViewModel by viewModels {
        (applicationContext as WatchLaterApplication).viewModelProviderFactory
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_launcher)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        vm.model.observe(this, { render(it) })
        vm.events.observe(this, { event ->
            when (event) {
                Event.OpenYouTubeSettings -> openYoutubeSettings()
                Event.OpenExample -> openExampleVideo()
                Event.OpenWatchLaterSettings -> openWatchLaterSettings()
            }
        })
    }

    private fun render(model: LauncherViewModel.Model) {
        findViewById<View>(R.id.launcher_youtube_button).setOnClickListener { vm.onYoutubeSettings() }
        findViewById<View>(R.id.launcher_watchlater_button).setOnClickListener { vm.onWatchLaterSettings() }
        findViewById<View>(R.id.launcher_example_button).setOnClickListener { vm.onTryExample() }

        val watchLaterView = findViewById<View>(R.id.launcher_watchlater_action)
        watchLaterView.visibility = if (model.resolverProblems != null &&
            ((model.resolverProblems.verifiedDomainsMissing > 0) || !model.resolverProblems.watchLaterIsDefault)) {
            View.VISIBLE
        } else {
            View.GONE
        }
    }

    override fun onResume() {
        super.onResume()
        vm.onResume()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_add, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_about -> {
                startActivity(Intent(this, AboutActivity::class.java))
                true
            }
            R.id.menu_help -> {
                startActivity(Intent(this, HelpActivity::class.java))
                true
            }
            R.id.menu_privacy -> {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://lambdasoup.com/privacypolicy-watchlater/")))
                true
            }
            R.id.menu_store -> {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID)))
                true
            }
            else -> super.onOptionsItemSelected(item)
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
        if (packageManager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY ) == null) {
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
        val intent = Intent(action, Uri.parse("package:${packageName}"))
        startActivity(intent)
    }

    companion object {
        private val EXAMPLE_URI = Uri.parse("https://www.youtube.com/watch?v=dGFSjKuJfrI")
        private val EXAMPLE_INTENT = Intent(Intent.ACTION_VIEW, EXAMPLE_URI)
    }
}
