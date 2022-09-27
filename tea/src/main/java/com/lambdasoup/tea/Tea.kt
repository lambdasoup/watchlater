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

import android.os.Handler
import android.os.Looper
import android.util.Log
import java.util.concurrent.ScheduledThreadPoolExecutor
import kotlin.RuntimeException

class Tea<Model, Msg>(
    val init: Pair<Model, Cmd<Msg>>,
    val view: (Model) -> Unit,
    val update: (model: Model, msg: Msg) -> Pair<Model, Cmd<Msg>>,
    val subscriptions: (model: Model) -> Sub<Msg>,
) {

    private var model = init.first
    private var subs: Sub<Msg> = Sub.none()

    private val engine: Engine = createEngine()

    init {
        process(init.second)
        render()
    }

    private fun process(msg: Msg) {
        engine.log("$model -> ${prettyName(msg)} ")

        val (newModel, cmd) = update(model, msg)
        model = newModel

        process(cmd)
    }

    private fun process(cmd: Cmd<Msg>) {
        when (cmd) {
            is Cmd.AppMsg<Msg> -> process(cmd.msg)
            is Cmd.Event<Msg> -> runEvent(cmd.event)
            is Cmd.Task<Msg> -> runBg(cmd.task)
            is Cmd.Batch -> cmd.cmds.forEach(::process)
            is Cmd.None -> { /* done here */
            }
        }
    }

    private fun processAndRender(msg: Msg) {
        process(msg)
        render()
    }

    private fun runBg(task: () -> Msg) {
        engine.execute {
            try {
                val msg = task()
                engine.post {
                    processAndRender(msg)
                }
            } catch (t: Throwable) {
                engine.post {
                    throw RuntimeException("Tea task executor threw exception", t)
                }
            }
        }
    }

    private fun render() {
        view.invoke(model)

        val newSubs = subscriptions(model)
        if (subs != newSubs) {
            subs.unbind()
            newSubs.cb = { msg ->
                engine.post { processAndRender(msg) }
            }
            newSubs.bind()
        }
        subs = newSubs
    }

    private fun runEvent(event: () -> Unit) {
        event.invoke()
    }

    fun ui(msg: Msg) {
        processAndRender(msg)
    }

    fun clear() {
        subs.unbind()
    }

    companion object {
        /**
         * Overwrite before instantiating Tea to set a different engine. Only meant and useful for testing purposes
         * (see tea-testing-support).
         */
        var createEngine: () -> Engine = {
            DefaultEngine()
        }
    }

    interface Engine {
        fun execute(r: () -> Unit)
        fun post(r: () -> Unit)
        fun log(s: String)
    }

    class DefaultEngine : Engine {
        private var scheduler = ScheduledThreadPoolExecutor(1)
        private var handler = Handler(Looper.getMainLooper())
        override fun log(s: String) {
            if (!BuildConfig.DEBUG) return
            Log.d("TEA", s)
        }

        override fun execute(r: () -> Unit) = scheduler.execute(r)
        override fun post(r: () -> Unit): Unit = run { handler.post(r) }
    }
}
