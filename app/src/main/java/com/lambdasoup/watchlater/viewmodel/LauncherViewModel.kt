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

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import com.lambdasoup.tea.Cmd
import com.lambdasoup.tea.Sub
import com.lambdasoup.tea.Tea
import com.lambdasoup.tea.times
import com.lambdasoup.watchlater.data.IntentResolverRepository
import com.lambdasoup.watchlater.data.IntentResolverRepository.ResolverProblems
import com.lambdasoup.watchlater.util.EventSource
import com.lambdasoup.watchlater.viewmodel.LauncherViewModel.Msg.OnResolverState
import com.lambdasoup.watchlater.viewmodel.LauncherViewModel.Msg.OnResume
import com.lambdasoup.watchlater.viewmodel.LauncherViewModel.Msg.OnWatchLaterSettings
import com.lambdasoup.watchlater.viewmodel.LauncherViewModel.Msg.OnYouTubeSettings
import com.lambdasoup.watchlater.viewmodel.LauncherViewModel.Msg.TryExample

class LauncherViewModel(private val repository: IntentResolverRepository) : ViewModel() {

    val events = EventSource<Event>()
    val model = MutableLiveData<Model>()

    private val onResume = Sub.create<Unit, Msg>()
    private val resolverStateSubscription = Sub.create<ResolverProblems, Msg>()

    private val updateRepository = Cmd.event<Msg> { repository.update() }
    private val openYouTubeSettings = Cmd.event<Msg> { events.submit(Event.OpenYouTubeSettings) }
    private val openWatchLaterSettings =
        Cmd.event<Msg> { events.submit(Event.OpenWatchLaterSettings) }
    private val openExample = Cmd.event<Msg> { events.submit(Event.OpenExample) }

    private val tea = Tea(
        init = Model(resolverProblems = null) * Cmd.none(),
        view = model::setValue,
        update = ::update,
        subscriptions = ::subscriptions,
    )

    private fun update(model: Model, msg: Msg): Pair<Model, Cmd<Msg>> {
        return when (msg) {
            is OnResume -> model * updateRepository
            is OnYouTubeSettings -> model * openYouTubeSettings
            is OnWatchLaterSettings -> model * openWatchLaterSettings
            is TryExample -> model * openExample
            is OnResolverState ->
                model.copy(resolverProblems = msg.resolverProblems) * Cmd.none()
        }
    }

    @Suppress("UNUSED_PARAMETER")
    private fun subscriptions(model: Model): Sub<Msg> =
        Sub.batch(
            onResume { OnResume },
            resolverStateSubscription { OnResolverState(it) },
        )

    private val observer =
        Observer<ResolverProblems> { resolverStateSubscription.submit(it) }

    init {
        repository.getResolverState().observeForever(observer)
    }

    data class Model(
        val resolverProblems: ResolverProblems?
    )

    sealed class Msg {
        object OnResume : Msg()
        object OnYouTubeSettings : Msg()
        object OnWatchLaterSettings : Msg()
        object TryExample : Msg()
        data class OnResolverState(
            val resolverProblems: ResolverProblems
        ) : Msg()
    }

    sealed class Event {
        object OpenYouTubeSettings : Event()
        object OpenWatchLaterSettings : Event()
        object OpenExample : Event()
    }

    fun onResume() {
        onResume.submit(Unit)
    }

    fun onYoutubeSettings() {
        tea.ui(OnYouTubeSettings)
    }

    fun onWatchLaterSettings() {
        tea.ui(OnWatchLaterSettings)
    }

    fun onTryExample() {
        tea.ui(TryExample)
    }

    override fun onCleared() {
        repository.getResolverState().removeObserver(observer)
    }
}
