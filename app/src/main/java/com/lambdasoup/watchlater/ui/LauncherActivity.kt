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

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.lambdasoup.watchlater.BuildConfig
import com.lambdasoup.watchlater.R
import com.lambdasoup.watchlater.data.IntentResolverRepository.ResolvedApp
import com.lambdasoup.watchlater.data.IntentResolverRepository.ResolverState
import com.lambdasoup.watchlater.viewmodel.LauncherViewModel

class LauncherActivity : WatchLaterActivity() {

    private lateinit var viewModel: LauncherViewModel

    private val diffUtil = object : DiffUtil.ItemCallback<ResolvedApp>() {
        override fun areItemsTheSame(oldItem: ResolvedApp, newItem: ResolvedApp): Boolean {
            // TODO
            return false
        }

        override fun areContentsTheSame(oldItem: ResolvedApp, newItem: ResolvedApp): Boolean {
            // TODO
            return false
        }
    }

    private val adapter = object : ListAdapter<ResolvedApp, ViewHolder>(diffUtil) {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return object : ViewHolder(layoutInflater.inflate(R.layout.item_resolvedapp, parent, false)) {}
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = getItem(position)
            val nameView: TextView = holder.itemView.findViewById(R.id.name)
            nameView.text = item.name
            val iconView: ImageView = holder.itemView.findViewById(R.id.icon)
            iconView.setImageDrawable(item.icon)
        }
    }

    private class ResolvedAppViewHolder(view: View) : ViewHolder(view)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_launcher)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        viewModel = getViewModel(LauncherViewModel::class.java)

        val recyclerView: RecyclerView = findViewById(R.id.resolver_list)
        recyclerView.adapter = adapter

        viewModel.resolverState.observe(this, { resolverState: ResolverState -> onResolverStateChanged(resolverState) })
        viewModel.resolverState2.observe(this, this::onResolver2StateChanged)
        findViewById<View>(R.id.launcher_youtube_button).setOnClickListener { v: View? -> openYoutubeSettings() }
        findViewById<View>(R.id.launcher_example_button).setOnClickListener { v: View? -> openExampleVideo() }
    }

    private fun onResolverStateChanged(resolverState: ResolverState) {
        val view = findViewById<View>(R.id.launcher_youtube_action)
        when (resolverState) {
            ResolverState.OK -> view.visibility = View.GONE
            ResolverState.YOUTUBE_ONLY -> view.visibility = View.VISIBLE
        }
    }

    private fun onResolver2StateChanged(list: List<ResolvedApp>) {
        adapter.submitList(list)
    }

    override fun onResume() {
        super.onResume()
        viewModel.update()
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
        // check if youtube is installed
        if (packageManager.resolveActivity(INTENT_YOUTUBE_APP, PackageManager.MATCH_DEFAULT_ONLY) == null) {
            Toast.makeText(this, R.string.no_youtube_installed, Toast.LENGTH_SHORT).show()
            return
        }
        if (packageManager.resolveActivity(INTENT_YOUTUBE_SETTINGS, PackageManager.MATCH_DEFAULT_ONLY) == null) {
            Toast.makeText(this, R.string.no_youtube_settings_activity, Toast.LENGTH_SHORT).show()
            return
        }
        startActivity(INTENT_YOUTUBE_SETTINGS)
    }

    companion object {
        private val EXAMPLE_URI = Uri.parse("https://www.youtube.com/watch?v=dGFSjKuJfrI")
        val EXAMPLE_INTENT = Intent(Intent.ACTION_VIEW, EXAMPLE_URI)
        private val INTENT_YOUTUBE_APP = Intent().setData(EXAMPLE_URI).setPackage("com.google.android.youtube")
        private val INTENT_YOUTUBE_SETTINGS = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:com.google.android.youtube"))
    }
}
