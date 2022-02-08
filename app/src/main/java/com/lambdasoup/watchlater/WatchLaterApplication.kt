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
package com.lambdasoup.watchlater

import android.app.Application
import com.lambdasoup.watchlater.data.AccountRepository
import com.lambdasoup.watchlater.data.IntentResolverRepository
import com.lambdasoup.watchlater.data.YoutubeRepository
import com.lambdasoup.watchlater.util.VideoIdParser
import com.lambdasoup.watchlater.viewmodel.AddViewModel
import com.lambdasoup.watchlater.viewmodel.LauncherViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.GlobalContext.startKoin
import org.koin.dsl.module

class WatchLaterApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        startKoin{
            // TODO https://github.com/InsertKoinIO/koin/issues/1242
            // androidLogger()
            androidContext(this@WatchLaterApplication)
            modules(appModule)
        }
    }
}

val appModule = module {

    single { YoutubeRepository(get()) }
    single { IntentResolverRepository(get()) }
    single { AccountRepository(get()) }
    single { VideoIdParser() }

    viewModel { AddViewModel(get(), get(), get()) }
    viewModel { LauncherViewModel(get()) }
}
