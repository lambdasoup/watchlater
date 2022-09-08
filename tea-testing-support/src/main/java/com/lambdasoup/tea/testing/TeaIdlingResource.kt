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

package com.lambdasoup.tea.testing

import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.test.espresso.IdlingResource as EspressoIdlingResource
import androidx.compose.ui.test.IdlingResource as ComposeIdlingResource
import com.lambdasoup.tea.BuildConfig
import com.lambdasoup.tea.Tea
import java.util.concurrent.ScheduledThreadPoolExecutor

class TeaIdlingResource {
    private var scheduler = ScheduledThreadPoolExecutor(1)

    private val _espressoIdlingResource = EspressoIdlingResourceImpl()
    val espressoIdlingResource: EspressoIdlingResource = _espressoIdlingResource

    val composeIdlingResource = object : ComposeIdlingResource {
        override val isIdleNow: Boolean
            get() = this@TeaIdlingResource.isIdleNow

        override fun getDiagnosticMessageIfBusy(): String? {
            return if (isIdleNow) null else "${scheduler.queue.size} tasks in queue"
        }
    }

    init {
        Tea.createEngine = {
            object : Tea.Engine {
                private var handler = Handler(Looper.getMainLooper())
                override fun log(s: String) {
                    if (!BuildConfig.DEBUG) return
                    Log.d("TEA", s)
                }

                override fun execute(r: () -> Unit) = scheduler.execute(r)
                override fun post(r: () -> Unit): Unit = run { handler.post(r); updateListener() }
            }
        }
    }

    private fun updateListener() {
        if (isIdleNow) {
            _espressoIdlingResource.callback?.onTransitionToIdle()
        }
    }

    private val isIdleNow: Boolean
        get() = scheduler.activeCount == 0

    private inner class EspressoIdlingResourceImpl : EspressoIdlingResource {
        var callback: EspressoIdlingResource.ResourceCallback? = null

        override fun getName() = "Android TEA idling resource"

        override fun isIdleNow(): Boolean {
            return this@TeaIdlingResource.isIdleNow
        }

        override fun registerIdleTransitionCallback(callback: EspressoIdlingResource.ResourceCallback?) {
            this.callback = callback
        }
    }
}
