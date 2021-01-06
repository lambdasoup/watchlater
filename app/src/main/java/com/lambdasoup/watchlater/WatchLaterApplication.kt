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
import androidx.annotation.VisibleForTesting
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory
import com.lambdasoup.watchlater.data.AccountRepository
import com.lambdasoup.watchlater.data.IntentResolverRepository
import com.lambdasoup.watchlater.data.YoutubeRepository
import com.lambdasoup.watchlater.util.VideoIdParser
import com.lambdasoup.watchlater.viewmodel.WatchLaterViewModel
import java.lang.reflect.InvocationTargetException

class WatchLaterApplication : Application() {

    lateinit var accountRepository: AccountRepository
    lateinit var youtubeRepository: YoutubeRepository
    lateinit var intentResolverRepository: IntentResolverRepository
    lateinit var videoIdParser: VideoIdParser

    @VisibleForTesting
    var viewModelProviderFactory: ViewModelProvider.Factory? = null

    override fun onCreate() {
        super.onCreate()
        accountRepository = AccountRepository(this)
        youtubeRepository = YoutubeRepository(this)
        intentResolverRepository = IntentResolverRepository(this)
        videoIdParser = VideoIdParser()
        viewModelProviderFactory = WatchLaterFactory(this)
    }

    private class WatchLaterFactory(private val application: WatchLaterApplication) : AndroidViewModelFactory(application) {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return if (WatchLaterViewModel::class.java.isAssignableFrom(modelClass)) {
                try {
                    modelClass.getConstructor(WatchLaterApplication::class.java).newInstance(application)
                } catch (e: NoSuchMethodException) {
                    throw RuntimeException("Cannot create an instance of $modelClass", e)
                } catch (e: IllegalAccessException) {
                    throw RuntimeException("Cannot create an instance of $modelClass", e)
                } catch (e: InstantiationException) {
                    throw RuntimeException("Cannot create an instance of $modelClass", e)
                } catch (e: InvocationTargetException) {
                    throw RuntimeException("Cannot create an instance of $modelClass", e)
                }
            } else super.create(modelClass)
        }
    }

}
