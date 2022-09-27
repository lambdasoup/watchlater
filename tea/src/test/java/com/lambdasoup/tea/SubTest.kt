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

import com.nhaarman.mockitokotlin2.mock
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class SubTest {

    sealed class Msg

    private val sub1 = Sub.create<Unit, Msg>()
    private val sub2 = Sub.create<Unit, Msg>()

    @Test
    fun `none should always be equals`() {
        val none1: Sub<Msg> = Sub.none()
        val none2: Sub<Msg> = Sub.none()

        assertEquals(none1, none2)
    }

    @Test
    fun `batch should be different than none`() {
        val batch: Sub<Msg> = Sub.batch()
        val none: Sub<Msg> = Sub.none()

        assertNotEquals(batch, none)
    }

    @Test
    fun `different batches should be different`() {
        val batch1: Sub<Msg> = Sub.batch(Sub.none())
        val batch2: Sub<Msg> = Sub.batch()

        assertNotEquals(batch1, batch2)
    }

    @Test
    fun `different subs should be different`() {
        assertNotEquals(sub1, sub2)
    }

    @Test
    fun `same subs should be equal`() {
        assertEquals(sub1, sub1)
    }

    @Test
    fun `equal batches should be equal`() {
        run {
            val batch1: Sub<Msg> = Sub.batch()
            val batch2: Sub<Msg> = Sub.batch()

            assertEquals(batch1, batch2)
        }

        run {
            val batch1: Sub<Msg> = Sub.batch(Sub.none(), Sub.batch())
            val batch2: Sub<Msg> = Sub.batch(Sub.none(), Sub.batch())

            assertEquals(batch1, batch2)
        }

        run {
            val batch1: Sub<Msg> = Sub.batch(Sub.none(), Sub.batch(), sub1 { mock() })
            val batch2: Sub<Msg> = Sub.batch(Sub.none(), Sub.batch(), sub1 { mock() })

            assertEquals(batch1, batch2)
        }
    }
}
