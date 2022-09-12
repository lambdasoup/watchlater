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

import android.Manifest
import android.accounts.Account
import android.accounts.AccountManager
import android.annotation.TargetApi
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import com.lambdasoup.watchlater.BuildConfig
import com.lambdasoup.watchlater.R
import com.lambdasoup.watchlater.ui.AboutActivity
import com.lambdasoup.watchlater.ui.HelpActivity
import com.lambdasoup.watchlater.ui.MenuAction
import com.lambdasoup.watchlater.viewmodel.AddViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class AddActivity : AppCompatActivity(), ActionView.ActionListener {

    private val vm: AddViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AddScreen(
                onOverflowAction = this::onOverflowActionSelected,
                onSetAccount = this::askForAccount,
                openPlaylistsOnYoutube = { openWithYoutube(playVideo = false) },
                onGrantPermissionsClicked = this::tryAcquireAccountsPermission,
                actionListener = this,
            )
        }

        vm.setVideoUri(intent.data!!)

        vm.events.observe(this) { event ->
            when (event) {
                is AddViewModel.Event.OpenAuthIntent -> {
                    startActivityForResult(event.intent, REQUEST_ACCOUNT_INTENT)
                }
            }
        }
    }

    @Deprecated("Deprecated on platform")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            REQUEST_ACCOUNT -> {
                onRequestAccountResult(resultCode, data)
                return
            }
            REQUEST_ACCOUNT_INTENT -> {
                onRequestAccountIntentResult(resultCode)
                return
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun onRequestAccountResult(resultCode: Int, data: Intent?) {
        if (resultCode == RESULT_OK) {
            val name = data!!.getStringExtra(AccountManager.KEY_ACCOUNT_NAME)
            val type = data.getStringExtra(AccountManager.KEY_ACCOUNT_TYPE)
            vm.setAccount(Account(name, type))
        }
    }

    private fun onRequestAccountIntentResult(resultCode: Int) {
        if (resultCode == RESULT_OK) {
            vm.onAccountPermissionGranted()
        }
    }

    override fun onResume() {
        super.onResume()
        val needsPermission = needsPermission()
        vm.setPermissionNeeded(needsPermission)
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
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://lambdasoup.com/privacypolicy-watchlater/")))
            }
            MenuAction.Store -> {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID)))
            }
        }
    }

    private fun askForAccount() {
        val intent = newChooseAccountIntent()
        startActivityForResult(intent, REQUEST_ACCOUNT)
    }

    private fun tryAcquireAccountsPermission() {
        requestPermissions(arrayOf(Manifest.permission.GET_ACCOUNTS), PERMISSIONS_REQUEST_GET_ACCOUNTS)
    }

    private fun needsPermission(): Boolean {
        return if (Build.VERSION.SDK_INT < 26) {
            // below 26 we need GET_ACCOUNTS
            !hasAccountsPermission()
        } else {
            // starting with O we don't need any permissions at runtime
            false
        }
    }

    @TargetApi(23)
    private fun hasAccountsPermission(): Boolean {
        return (application.checkSelfPermission(Manifest.permission.GET_ACCOUNTS)
                == PackageManager.PERMISSION_GRANTED)
    }

    @Suppress("DEPRECATION")
    private fun newChooseAccountIntent(): Intent {
        val types = arrayOf(ACCOUNT_TYPE_GOOGLE)
        val title = getString(R.string.choose_account)
        return if (Build.VERSION.SDK_INT < 26) {
            AccountManager.newChooseAccountIntent(null, null,
                types, false, title, null,
                null, null)
        } else AccountManager.newChooseAccountIntent(null, null,
            types, title, null, null,
            null)
    }

    private fun openWithYoutube(playVideo: Boolean = true) {
        try {
            val youtubeIntent = Intent()
                .setPackage("com.google.android.youtube")
                .setAction(Intent.ACTION_VIEW)

            if (playVideo) {
                youtubeIntent.data = intent.data
            } else {
                youtubeIntent.data = Uri.parse("https://www.youtube.com/feed/library")
            }

            startActivity(youtubeIntent)
            finish()
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(this, R.string.error_youtube_player_missing, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onWatchNowClicked() {
        openWithYoutube()
    }

    override fun onWatchLaterClicked(videoId: String) {
        vm.watchLater(videoId)
    }

    companion object {
        private const val PERMISSIONS_REQUEST_GET_ACCOUNTS = 100
        private const val REQUEST_ACCOUNT = 1
        private const val REQUEST_ACCOUNT_INTENT = 2
        private const val ACCOUNT_TYPE_GOOGLE = "com.google"
    }
}
