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

import org.junit.Test

import org.junit.Assert.*

class UtilTest {

    object Object

    @Test
    fun `should pretty print object`() {
        val actual = prettyName(Object)

        assertEquals("Object", actual)
    }

    sealed class SealedClass {
        @Suppress("unused")
        object Object: SealedClass()
    }

    @Test
    fun `should pretty print object from sealed class`() {
        val actual = prettyName(Object)

        assertEquals("Object", actual)
    }

}
