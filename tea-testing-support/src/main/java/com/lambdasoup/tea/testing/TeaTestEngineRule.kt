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

import com.lambdasoup.tea.Tea
import org.junit.rules.TestWatcher
import org.junit.runner.Description

class TeaTestEngineRule(val autoExecute: Boolean = true) : TestWatcher() {

    private val rs: MutableList<() -> Unit> = mutableListOf()

    override fun starting(description: Description) {
        super.starting(description)
        Tea.createEngine = {
            object : Tea.Engine {
                override fun execute(r: () -> Unit) {
                    if (autoExecute) {
                        r()
                    } else {
                        rs.add(r)
                    }
                }

                override fun post(r: () -> Unit) {
                    r()
                }

                override fun log(s: String) { // ignore
                }
            }
        }
    }

    fun proceed() {
        val r = rs.removeAt(0)
        r()
    }

    override fun finished(description: Description) {
        super.finished(description)
        Tea.createEngine = {
            throw RuntimeException("reusing not supported")
        }
    }
}
