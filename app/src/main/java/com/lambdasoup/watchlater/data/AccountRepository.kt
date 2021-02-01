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
package com.lambdasoup.watchlater.data

import android.accounts.*
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.preference.PreferenceManager
import java.io.IOException

class AccountRepository(context: Context?) : OnSharedPreferenceChangeListener {

    private val prefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
    private val account = MutableLiveData<Account>()
    private val manager: AccountManager
    private fun load() {
        val name = prefs.getString(PREF_KEY_DEFAULT_ACCOUNT_NAME, null)
        if (name != null) {
            account.setValue(Account(name, ACCOUNT_TYPE_GOOGLE))
        } else {
            account.setValue(null)
        }
    }

    fun put(account: Account) {
        val prefEditor = prefs.edit()
        prefEditor.putString(PREF_KEY_DEFAULT_ACCOUNT_NAME, account.name)
        prefEditor.apply()
        load()
    }

    fun clear() {
        val prefEditor = prefs.edit()
        prefEditor.remove(PREF_KEY_DEFAULT_ACCOUNT_NAME)
        prefEditor.apply()
        load()
    }

    fun get(): LiveData<Account> {
        return account
    }

    @WorkerThread
    fun getAuthToken(): AuthTokenResult {
        if (!isAccountOk) {
            clear()
            return AuthTokenResult.Error
        }

        val future = manager.getAuthToken(account.value, SCOPE_YOUTUBE, null, false, null, null)

        try {
            val result = future.result
            val intent = result.getParcelable<Intent>(AccountManager.KEY_INTENT)
            if (intent != null) {
                return AuthTokenResult.HasIntent(intent)
            }
            return AuthTokenResult.AuthToken(result.getString(AccountManager.KEY_AUTHTOKEN)!!)
        } catch (e: OperationCanceledException) {
            throw RuntimeException("could not get token", e)
        } catch (e: IOException) {
            throw RuntimeException("could not get token", e)
        } catch (e: AuthenticatorException) {
            throw RuntimeException("could not get token", e)
        }
    }

    fun invalidateToken(token: String?) {
        manager.invalidateAuthToken(ACCOUNT_TYPE_GOOGLE, token)
    }

    private val isAccountOk: Boolean
        get() {
            val accounts = manager.getAccountsByType(ACCOUNT_TYPE_GOOGLE)
            return listOf(*accounts).contains(account.value)
        }

    override fun onSharedPreferenceChanged(prefs: SharedPreferences, key: String) {
        load()
    }

    sealed class AuthTokenResult {
        object Error: AuthTokenResult()
        data class AuthToken(val token: String): AuthTokenResult()
        data class HasIntent(val intent: Intent): AuthTokenResult()
    }

    companion object {
        private const val PREF_KEY_DEFAULT_ACCOUNT_NAME = "pref_key_default_account_name"
        private const val SCOPE_YOUTUBE = "oauth2:https://www.googleapis.com/auth/youtube"
        private const val ACCOUNT_TYPE_GOOGLE = "com.google"
    }

    init {
        prefs.registerOnSharedPreferenceChangeListener(this)
        manager = AccountManager.get(context)
        load()
    }
}
