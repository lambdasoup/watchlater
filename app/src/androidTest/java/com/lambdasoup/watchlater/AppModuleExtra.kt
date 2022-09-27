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
package com.lambdasoup.watchlater

import android.accounts.AccountManager
import android.content.ContextWrapper
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.content.pm.verify.domain.DomainVerificationManager
import com.lambdasoup.watchlater.data.IntentResolverRepository
import com.lambdasoup.watchlater.data.YoutubeRepository
import com.nhaarman.mockitokotlin2.mock
import org.koin.dsl.module

val packageManager: PackageManager = mock()
val domainVerificationManager: DomainVerificationManager = mock()
val accountManager: AccountManager = mock()
val sharedPreferences: SharedPreferences = mock()

@Suppress("unused")
val appModuleExtra = module {
    single { packageManager }
    single {
        IntentResolverRepository(
            object : ContextWrapper(get()) {
                override fun getSystemService(name: String) =
                    when (name) {
                        "domain_verification" -> domainVerificationManager
                        else -> super.getSystemService(name)
                    }
            },
            get(),
        )
    }
    single { accountManager }
    single { sharedPreferences }
    single { YoutubeRepository(get(), "http://localhost:8080/") }
}
