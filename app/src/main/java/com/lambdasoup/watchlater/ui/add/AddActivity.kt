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
import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.lambdasoup.watchlater.BuildConfig
import com.lambdasoup.watchlater.R
import com.lambdasoup.watchlater.data.YoutubeRepository
import com.lambdasoup.watchlater.ui.*
import com.lambdasoup.watchlater.viewmodel.AddViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class AddActivity : AppCompatActivity(), ActionView.ActionListener {

    private val vm: AddViewModel by viewModel()

    private lateinit var actionView: ActionView
    private lateinit var permissionsView: PermissionsView
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
        val resultView = findViewById<ResultView>(R.id.add_result)
        val accountView = findViewById<ComposeView>(R.id.add_account)
        val playlistView = findViewById<ComposeView>(R.id.add_playlist)

        vm.setVideoUri(intent.data!!)
        vm.model.observe(this) {
            videoView.setVideoInfo(it.videoInfo)

            if (it.permissionNeeded != null) {
                onPermissionNeededChanged(it.permissionNeeded)
            }

            accountView.setContent {
                WatchLaterTheme {
                    Account(account = it.account, onSetAccount = this::askForAccount)
                }
            }

            resultView.onChanged(it.videoAdd)
            actionView.setState(it.videoAdd, it.videoId)

            playlistView.setContent {
                WatchLaterTheme {
                    Playlist(playlist = it.targetPlaylist, onSetPlaylist = vm::changePlaylist)
                }
            }

            setPlaylistSelection(it.playlistSelection)
        }

        vm.events.observe(this) { event ->
            when (event) {
                is AddViewModel.Event.OpenAuthIntent -> {
                    startActivityForResult(event.intent, REQUEST_ACCOUNT_INTENT)
                }
            }
        }
    }

    private fun onPermissionNeededChanged(permissionNeeded: Boolean) {
        if (!permissionNeeded) {
            permissionsView.visibility = View.GONE
        } else {
            permissionsView.visibility = View.VISIBLE
        }
    }

    private fun setPlaylistSelection(playlists: YoutubeRepository.Playlists?) {
        val fragment = supportFragmentManager
                .findFragmentByTag(PlaylistSelectionDialogFragment.TAG) as PlaylistSelectionDialogFragment?

        if (playlists == null) {
            fragment?.dismissAllowingStateLoss()
            return
        }

        if (fragment == null) {
            val newFragment = PlaylistSelectionDialogFragment()
            val ids: List<String> = playlists.items.map { it.id }
            val titles: List<String> = playlists.items.map { it.snippet.title }
            val bundle = Bundle()
            bundle.putStringArray(PlaylistSelectionDialogFragment.ARG_IDS, ids.toTypedArray())
            bundle.putStringArray(PlaylistSelectionDialogFragment.ARG_TITLES, titles.toTypedArray())
            newFragment.arguments = bundle
            newFragment.show(supportFragmentManager,
                PlaylistSelectionDialogFragment.TAG
            )
        }
    }

    class PlaylistSelectionDialogFragment : DialogFragment(), DialogInterface.OnClickListener {
        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            val titles = requireArguments().getStringArray(ARG_TITLES)

            val builder = MaterialAlertDialogBuilder(requireContext())
                    .setTitle(R.string.playlist_selection_title)

            if (titles == null || titles.isNotEmpty()) {
                builder.setItems(titles, this)
                builder.setNeutralButton(R.string.playlist_selection_edit) { _, _ ->
                    (context as AddActivity).openWithYoutube(playVideo = false)
                }

            } else {
                builder.setMessage(R.string.playlist_selection_message)
                builder.setNeutralButton(R.string.playlist_selection_create) { _, _ ->
                    (context as AddActivity).openWithYoutube(playVideo = false)
                }
            }

            return builder.create()
        }

        companion object {
            const val TAG = "PlaylistSelectionDialog"
            const val ARG_IDS = "ids"
            const val ARG_TITLES = "titles"
        }

        override fun onClick(dialog: DialogInterface, which: Int) {
            val titles = requireArguments().getStringArray(ARG_TITLES)
            val ids = requireArguments().getStringArray(ARG_IDS)

            (context as AddActivity).vm.selectPlaylist(
                YoutubeRepository.Playlists.Playlist(
                    id = ids!![which],
                    snippet = YoutubeRepository.Playlists.Playlist.Snippet(
                            title = titles!![which]
                    )
            ))
        }

        override fun onDismiss(dialog: DialogInterface) {
            super.onDismiss(dialog)

            (context as AddActivity).vm.clearPlaylists()
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
