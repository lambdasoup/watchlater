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

package com.lambdasoup.tea

abstract class Sub<Msg> internal constructor() {

    internal var cb: ((Msg) -> Unit)? = null

    open fun bind() {}

    open fun unbind() {}

    fun submit(msg: Msg) {
        cb?.invoke(msg)
    }

    companion object {
        fun <Msg> batch(vararg subs: Sub<Msg>): Sub<Msg> = BatchSub(setOf(*subs))
        fun <Msg> none(): Sub<Msg> = NoneSub()
        fun <T, Msg> create(
                bind: (() -> Unit)? = null,
                unbind: (() -> Unit)? = null,
        ) = Base<T, Msg>(bind, unbind)
    }

    class Base<T, Msg> internal constructor(
            val bind: (() -> Unit)? = null,
            val unbind: (() -> Unit)? = null,
    ) {
        private val sub = object : Sub<Msg>() {
            override fun bind() {
                bind?.invoke()
            }

            override fun unbind() {
                unbind?.invoke()
            }
        }

        private var f: ((T) -> Msg)? = null

        operator fun invoke(f: (T) -> Msg): Sub<Msg> {
            this.f = f
            return sub
        }

        fun submit(t: T) {
            val x = f?.invoke(t) ?: return
            sub.submit(x)
        }
    }
}

private data class BatchSub<Msg>(val subs: Set<Sub<Msg>>) : Sub<Msg>() {

    override fun bind() {
        for (sub in subs) {
            sub.cb = this.cb
            sub.bind()
        }
    }

    override fun unbind() {
        for (sub in subs) {
            sub.unbind()
            sub.cb = null
        }
    }
}

private class NoneSub<Msg> : Sub<Msg>() {

    // we should be able to do this with a singleton instance instead
    // kotlin type magic needed
    override fun equals(other: Any?): Boolean {
        return other?.javaClass?.isAssignableFrom(NoneSub::class.java) ?: false
    }

    override fun hashCode(): Int = 0
}
