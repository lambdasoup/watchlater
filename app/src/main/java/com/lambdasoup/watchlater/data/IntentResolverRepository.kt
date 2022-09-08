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
package com.lambdasoup.watchlater.data

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.verify.domain.DomainVerificationManager
import android.content.pm.verify.domain.DomainVerificationUserState
import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class IntentResolverRepository(
    private val context: Context,
    private val packageManager: PackageManager,
) {

    private val _resolverState = MutableLiveData<ResolverProblems>()

    fun getResolverState(): LiveData<ResolverProblems> {
        return _resolverState
    }

    fun update() {
        _resolverState.value = ResolverProblems(
            watchLaterIsDefault = watchLaterIsDefault(),
            verifiedDomainsMissing = if (Build.VERSION.SDK_INT >= 31) {
                domainVerificationMissing()
            } else {
                0
            }
        )
    }

    @RequiresApi(31)
    private fun domainVerificationMissing(): Int {
        val domainVerificationManager =
            context.getSystemService(DomainVerificationManager::class.java)
        val wlState = domainVerificationManager.getDomainVerificationUserState(context.packageName)
            ?: throw RuntimeException("app does not declare any domains")
        val watchlater = wlState.hostToStateMap.count { entry ->
            entry.value == DomainVerificationUserState.DOMAIN_STATE_NONE
        }
        return watchlater
    }

    private fun watchLaterIsDefault(): Boolean {
        val resolveIntent = Intent(Intent.ACTION_VIEW, Uri.parse(EXAMPLE_URI))
        val resolveInfo =
            packageManager.resolveActivity(resolveIntent, PackageManager.MATCH_DEFAULT_ONLY)
        return when (resolveInfo?.activityInfo?.name) {

            // youtube is set as default app to launch with, no chooser
            ACTIVITY_WATCHLATER -> true

            // some unknown app is set as the default app to launch with, without chooser.
            else -> false

        }
    }

    data class ResolverProblems(
        val watchLaterIsDefault: Boolean,
        val verifiedDomainsMissing: Int,
    )

    companion object {
        private const val ACTIVITY_WATCHLATER = "com.lambdasoup.watchlater.ui.add.AddActivity"
        private const val EXAMPLE_URI = "https://www.youtube.com/watch?v=tntOCGkgt98"
    }
}
