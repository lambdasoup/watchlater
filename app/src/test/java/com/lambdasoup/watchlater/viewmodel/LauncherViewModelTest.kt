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
package com.lambdasoup.watchlater.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import com.google.common.truth.Truth.assertThat
import com.lambdasoup.tea.testing.TeaTestEngineRule
import com.lambdasoup.watchlater.data.IntentResolverRepository
import com.lambdasoup.watchlater.data.IntentResolverRepository.ResolverProblems
import com.lambdasoup.watchlater.viewmodel.LauncherViewModel.Model
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.junit.runner.RunWith
import org.mockito.MockitoAnnotations.initMocks
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class LauncherViewModelTest {

    @get:Rule
    val aacRule: TestRule = InstantTaskExecutorRule()

    @get:Rule
    var teaRule: TestRule = TeaTestEngineRule()

    private val intentResolverRepository: IntentResolverRepository = mock()
    private val liveData = MutableLiveData<ResolverProblems>()

    private lateinit var viewModel: LauncherViewModel

    @Before
    fun setup() {
        initMocks(this)
        whenever(intentResolverRepository.getResolverState()).thenReturn(liveData)
        viewModel = LauncherViewModel(intentResolverRepository)
    }

    @Test
    fun `should update intentresolverrepository`() {
        viewModel.onResume()
        verify(intentResolverRepository).update()
    }

    @Test
    fun `should return intentresolverstate`() {
        val resolverProblem: ResolverProblems = mock()
        liveData.postValue(resolverProblem)

        assertThat(viewModel.model.value)
            .isEqualTo(Model(resolverProblems = resolverProblem))
    }
}
