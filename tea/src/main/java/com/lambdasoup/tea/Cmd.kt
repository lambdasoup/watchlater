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

sealed class Cmd<Msg> {
    internal data class AppMsg<Msg>(
            val msg: Msg,
    ) : Cmd<Msg>()

    internal data class Event<Msg>(
            val event: () -> Unit,
    ) : Cmd<Msg>()

    internal data class Task<Msg>(
            val task: () -> Msg,
    ) : Cmd<Msg>()
    
    internal data class Batch<Msg>(
            val cmds: Set<Cmd<Msg>>
    ) : Cmd<Msg>()

    internal class None<Msg> : Cmd<Msg>()

    companion object {
        fun <Msg> none(): Cmd<Msg> = None()
        fun <Msg> batch(vararg cmds: Cmd<Msg>): Cmd<Msg> = Batch(setOf(*cmds))
        fun <Msg> event(f: () -> Unit): Cmd<Msg> = Event(f)
        fun <Msg, T> event(f: (T) -> Unit): (T) -> Cmd<Msg> = { t -> Event { f(t) } }
        fun <Msg, T> task(f: () -> (T)): ((T) -> Msg) -> Cmd<Msg> = { g ->
            Task { g(f()) }
        }
        fun <Msg, R, T> task(f: (R) -> (T)): (R, (T) -> Msg) -> Cmd<Msg> = { r,g ->
            Task { g(f(r)) }
        }
        fun <Msg, R, S, T> task(f: (R, S) -> (T)): (R, S, (T) -> Msg) -> Cmd<Msg> = { r, s,g ->
            Task { g(f(r, s)) }
        }
    }
}
