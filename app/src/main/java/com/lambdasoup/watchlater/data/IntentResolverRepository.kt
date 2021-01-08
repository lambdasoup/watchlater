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
package com.lambdasoup.watchlater.data

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.graphics.drawable.Drawable
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class IntentResolverRepository(context: Context) {

    private val pm: PackageManager = context.packageManager

    private val _resolverState = MutableLiveData<ResolverState>()

    fun getResolverState(): LiveData<ResolverState> {
        return _resolverState
    }

    private val _resolverState2 = MutableLiveData<List<ResolvedApp>>()

    fun getResolverState2(): LiveData<List<ResolvedApp>> {
        return _resolverState2
    }

    fun update() {
        val resolveIntent = Intent(Intent.ACTION_VIEW, Uri.parse(EXAMPLE_URI))
        val resolveInfo = pm.resolveActivity(resolveIntent, PackageManager.MATCH_ALL)
        when (resolveInfo?.activityInfo?.name) {
            ACTIVITY_YOUTUBE -> {

                // youtube is set as default app to launch with, no chooser
                _resolverState.setValue(ResolverState.YOUTUBE_ONLY)
            }
            else -> {

                // some unknown app is set as the default app to launch with, without chooser.
                _resolverState.setValue(ResolverState.OK)
            }
        }

        val queryIntentActivities = pm.queryIntentActivities(resolveIntent, PackageManager.MATCH_ALL)
        _resolverState2.value = queryIntentActivities.map(this::buildResolvedApp)
    }

    private fun buildResolvedApp(info: ResolveInfo): ResolvedApp {
        return ResolvedApp(
                name = info.loadLabel(pm),
                icon = info.loadIcon(pm),
        )
    }

    enum class ResolverState {
        OK, YOUTUBE_ONLY
    }

    data class ResolvedApp(
            val name: CharSequence,
            val icon: Drawable,
    )

    companion object {
        private const val ACTIVITY_YOUTUBE = "com.google.android.youtube.UrlActivity"
        private const val EXAMPLE_URI = "https://www.youtube.com/watch?v=tntOCGkgt98"
    }
}
