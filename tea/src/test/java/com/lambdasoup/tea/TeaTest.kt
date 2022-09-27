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

package com.lambdasoup.tea

import com.lambdasoup.tea.TeaTest.Msg.OnSubMsg
import com.lambdasoup.tea.TeaTest.Msg.TaskResult
import com.lambdasoup.tea.testing.TeaTestEngineRule
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.inOrder
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Rule
import org.junit.Test

class TeaTest {

    @get:Rule
    var rule = TeaTestEngineRule(autoExecute = false)

    data class Model(
        val string: String = "test-string",
        val int: Int = 0,
    )

    sealed class Msg {
        data class OnSubMsg(val i: Int) : Msg()
        data class TaskResult(val s: String) : Msg()
    }

    @Test
    fun `should do initial render with init model`() {
        val view: (Model) -> Unit = mock()
        val update: (Model, Msg) -> Pair<Model, Cmd<Msg>> = mock()
        val subscriptions: (Model) -> Sub<Msg> = mock()
        whenever(subscriptions.invoke(any())).thenReturn(Sub.none())

        val tea = Tea(
            init = Model(string = "test-name") * Cmd.none(),
            view = view,
            update = update,
            subscriptions = subscriptions,
        )
        tea.clear()

        verify(view).invoke(Model(string = "test-name"))
        verify(subscriptions).invoke(Model(string = "test-name"))
    }

    @Test
    fun `should bind and unbind subscriptions`() {
        val view: (Model) -> Unit = mock()
        val update: (Model, Msg) -> Pair<Model, Cmd<Msg>> = mock()
        val sub: Sub<Msg> = mock()

        val tea = Tea(
            init = Model(string = "test-name") * Cmd.none(),
            view = view,
            update = update,
            subscriptions = { sub },
        )

        verify(sub).bind()

        tea.clear()

        verify(sub).unbind()
    }

    @Test
    fun `should execute task`() {
        val view: (Model) -> Unit = mock()
        val task = Cmd.task<Msg, String> { "test-result" }

        val tea = Tea(
            init = Model() * task { TaskResult("test-result") },
            view = view,
            update = { model, msg ->
                when (msg) {
                    is TaskResult -> model.copy(string = msg.s) * Cmd.none()
                    else -> model * Cmd.none()
                }
            },
            subscriptions = { Sub.none() },
        )

        rule.proceed()

        tea.clear()

        inOrder(view) {
            verify(view).invoke(Model())
            verify(view).invoke(Model(string = "test-result"))
        }
    }

    @Test
    fun `should process sub`() {
        val view: (Model) -> Unit = mock()
        val sub = Sub.create<Int, Msg>()

        val tea = Tea(
            init = Model() * Cmd.none(),
            view = view,
            update = { model, msg ->
                when (msg) {
                    is OnSubMsg -> model.copy(int = msg.i) * Cmd.none()
                    else -> model * Cmd.none()
                }
            },
            subscriptions = { sub { OnSubMsg(it) } },
        )

        sub.submit(1337)

        tea.clear()

        inOrder(view) {
            verify(view).invoke(Model())
            verify(view).invoke(Model().copy(int = 1337))
        }
    }
}
