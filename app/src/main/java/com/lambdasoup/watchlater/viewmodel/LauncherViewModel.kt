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
package com.lambdasoup.watchlater.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.lambdasoup.tea.Cmd
import com.lambdasoup.tea.Sub
import com.lambdasoup.tea.Tea
import com.lambdasoup.tea.times
import com.lambdasoup.watchlater.WatchLaterApplication
import com.lambdasoup.watchlater.data.IntentResolverRepository.ResolverState
import com.lambdasoup.watchlater.util.EventSource
import com.lambdasoup.watchlater.viewmodel.LauncherViewModel.Msg.*

class LauncherViewModel(application: WatchLaterApplication) : WatchLaterViewModel(application) {

    private val repository = application.intentResolverRepository

    val events = EventSource<Event>()
    val model = MutableLiveData<Model>()

    private val onResume = Sub.create<Unit, Msg>()
    private val resolverStateSubscription = Sub.create<ResolverState, Msg>()

    private val updateRepository = Cmd.event<Msg> { repository.update() }
    private val openYouTubeSettings = Cmd.event<Msg> { events.submit(Event.OpenYouTubeSettings) }
    private val openExample = Cmd.event<Msg> { events.submit(Event.OpenExample) }

    private val tea = Tea(
            init = Model(resolverState = null) * Cmd.none(),
            view = model::setValue,
            update = ::update,
            subscriptions = ::subscriptions,
    )

    private fun update(model: Model, msg: Msg): Pair<Model, Cmd<Msg>> {
        return when (msg) {
            is OnResume -> model * updateRepository
            is YouTubeSettings -> model * openYouTubeSettings
            is TryExample -> model * openExample
            is OnResolverState ->
                    model.copy(resolverState = msg.resolverState) * Cmd.none()
        }
    }

    @Suppress("UNUSED_PARAMETER")
    private fun subscriptions(model: Model): Sub<Msg> =
            Sub.batch(
                    onResume { OnResume },
                    resolverStateSubscription { OnResolverState(it) },
            )

    private val observer =
            Observer<ResolverState> { resolverStateSubscription.submit(it) }

    init {
        repository.getResolverState().observeForever(observer)
    }

    data class Model(
            val resolverState: ResolverState?
    )

    sealed class Msg {
        object OnResume : Msg()
        object YouTubeSettings : Msg()
        object TryExample : Msg()
        data class OnResolverState(
                val resolverState: ResolverState
        ) : Msg()
    }

    sealed class Event {
        object OpenYouTubeSettings : Event()
        object OpenExample : Event()
    }

    fun onResume() {
        onResume.submit(Unit)
    }
    
    fun onYoutubeSettings() {
        tea.ui(YouTubeSettings)
    }
    
    fun onTryExample() {
        tea.ui(TryExample)
    }

    override fun onCleared() {
        repository.getResolverState().removeObserver(observer)
    }
}
