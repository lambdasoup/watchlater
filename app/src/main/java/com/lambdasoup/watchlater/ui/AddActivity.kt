/*
 * Copyright (c) 2015 - 2021
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
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import com.lambdasoup.watchlater.BuildConfig
import com.lambdasoup.watchlater.R
import com.lambdasoup.watchlater.viewmodel.AddViewModel
import com.lambdasoup.watchlater.viewmodel.AddViewModel.VideoAdd
import com.lambdasoup.watchlater.viewmodel.AddViewModel.VideoInfo

class AddActivity : WatchLaterActivity(), ActionView.ActionListener {

    private lateinit var actionView: ActionView
    private lateinit var permissionsView: PermissionsView
    private lateinit var viewModel: AddViewModel
    private lateinit var videoView: VideoView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        permissionsView = findViewById(R.id.add_permissions)
        permissionsView.listener = object : PermissionsView.Listener {
            override fun onGrantPermissionsClicked() {
                tryAcquireAccountsPermission()
            }
        }
        actionView = findViewById(R.id.add_action)
        actionView.listener = this
        videoView = findViewById(R.id.add_video)
        viewModel = getViewModel(AddViewModel::class.java)
        viewModel.getVideoAdd().observe(this, { videoAdd: VideoAdd -> onAddStatusChanged(videoAdd) })
        val resultView = findViewById<ResultView>(R.id.add_result)
        viewModel.getVideoAdd().observe(this, resultView)
        val accountView = findViewById<AccountView>(R.id.add_account)
        viewModel.account.observe(this, accountView)
        accountView!!.listener = object : AccountView.Listener {
            override fun onSetAccount() {
                askForAccount()
            }
        }
        viewModel.setVideoUri(intent.data!!)
        viewModel.getVideoInfo().observe(this, { info: VideoInfo -> videoView.setVideoInfo(info) })
        viewModel.getPermissionNeeded().observe(this, { permissionNeeded: Boolean -> onPermissionNeededChanged(permissionNeeded) })
    }

    private fun onPermissionNeededChanged(permissionNeeded: Boolean) {
        if (!permissionNeeded) {
            permissionsView.visibility = View.GONE
        } else {
            permissionsView.visibility = View.VISIBLE
        }
    }

    private fun onAddStatusChanged(videoAdd: VideoAdd) {
        if (videoAdd is VideoAdd.HasIntent) {
            startActivityForResult(videoAdd.intent, REQUEST_ACCOUNT_INTENT)
        }
        actionView.setState(videoAdd)
    }

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
            viewModel.setAccount(Account(name, type))
        }
    }

    private fun onRequestAccountIntentResult(resultCode: Int) {
        if (resultCode == RESULT_OK) {
            viewModel.watchLater()
        }
    }

    override fun onResume() {
        super.onResume()
        val needsPermission = needsPermission()
        viewModel.setPermissionNeeded(needsPermission)
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

    private fun askForAccount() {
        val intent = newChooseAccountIntent()
        startActivityForResult(intent, REQUEST_ACCOUNT)
    }

    @TargetApi(23)
    private fun tryAcquireAccountsPermission() {
        requestPermissions(arrayOf(Manifest.permission.GET_ACCOUNTS), PERMISSIONS_REQUEST_GET_ACCOUNTS)
    }

    private fun needsPermission(): Boolean {
        // below 23 permissions are granted at install time
        if (Build.VERSION.SDK_INT < 23) {
            return false
        }

        // below 26 we need GET_ACCOUNTS
        return if (Build.VERSION.SDK_INT < 26) {
            !hasAccountsPermission()
        } else false

        // starting with O we don't need any permissions at runtime
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

    private fun openWithYoutube() {
        try {
            val intent = Intent()
                    .setData(intent.data)
                    .setPackage("com.google.android.youtube")
            startActivity(intent)
            finish()
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(this, R.string.error_youtube_player_missing, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onWatchNowClicked() {
        openWithYoutube()
    }

    override fun onWatchLaterClicked() {
        viewModel.watchLater()
    }

    companion object {
        private const val PERMISSIONS_REQUEST_GET_ACCOUNTS = 100
        private const val REQUEST_ACCOUNT = 1
        private const val REQUEST_ACCOUNT_INTENT = 2
        private const val ACCOUNT_TYPE_GOOGLE = "com.google"
    }
}
