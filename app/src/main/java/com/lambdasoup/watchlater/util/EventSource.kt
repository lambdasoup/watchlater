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

package com.lambdasoup.watchlater.util

import androidx.annotation.MainThread
import androidx.annotation.VisibleForTesting
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner

class EventSource<T> {

    private var events: MutableSet<T> = mutableSetOf()

    private var owner: LifecycleOwner? = null
    private var callback: ((T) -> Unit)? = null

    @MainThread
    fun observe(owner: LifecycleOwner, callback: (T) -> Unit) {
        if (this.owner !== null) {
            throw RuntimeException("EventSource can only have one owner at a time.")
        }

        this.owner = owner
        this.callback = callback

        owner.lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onDestroy(owner: LifecycleOwner) {
                this@EventSource.owner = null
                this@EventSource.callback = null
            }
        })

        flush()
    }

    @MainThread
    private fun flush() {
        val atLeast = owner?.lifecycle?.currentState?.isAtLeast(Lifecycle.State.CREATED)
        if (atLeast == null || atLeast == false) {
            return
        }

        for (event in events) {
            callback!!(event)
        }

        events.clear()
    }

    @MainThread
    fun submit(t: T) {
        events.add(t)
        flush()
    }

    @VisibleForTesting
    internal fun contains(t: T) = events.contains(t)
}
