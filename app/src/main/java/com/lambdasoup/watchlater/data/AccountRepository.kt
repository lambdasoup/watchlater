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
package com.lambdasoup.watchlater.data

import android.accounts.Account
import android.accounts.AccountManager
import android.accounts.AuthenticatorException
import android.accounts.OperationCanceledException
import android.content.Intent
import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import androidx.annotation.MainThread
import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.lambdasoup.watchlater.data.AccountRepository.ErrorType.*
import java.io.IOException

class AccountRepository(
    private val accountManager: AccountManager,
    private val sharedPreferences: SharedPreferences,
) : OnSharedPreferenceChangeListener {

    private val liveData = MutableLiveData<Account?>()

    init {
        sharedPreferences.registerOnSharedPreferenceChangeListener(this)
        updateLiveData()
    }

    private fun getAccount(): Account? {
        val name = sharedPreferences.getString(PREF_KEY_DEFAULT_ACCOUNT_NAME, null) ?: return null
        return Account(name, ACCOUNT_TYPE_GOOGLE)
    }

    @MainThread
    private fun updateLiveData() {
        val account = getAccount()
        liveData.value = account
    }

    fun put(account: Account) {
        val prefEditor = sharedPreferences.edit()
        prefEditor.putString(PREF_KEY_DEFAULT_ACCOUNT_NAME, account.name)
        prefEditor.apply()
    }

    fun get(): LiveData<Account?> {
        return liveData
    }

    @WorkerThread
    fun getAuthToken(): AuthTokenResult {
        val account = getAccount()
        val accounts = accountManager.getAccountsByType(ACCOUNT_TYPE_GOOGLE)
        val isAccountOk = listOf(*accounts).contains(account)
        if (!isAccountOk) {
            val prefEditor = sharedPreferences.edit()
            prefEditor.remove(PREF_KEY_DEFAULT_ACCOUNT_NAME)
            prefEditor.apply()
            return AuthTokenResult.Error(AccountRemoved)
        }

        val future = accountManager.getAuthToken(account, SCOPE_YOUTUBE, null, false, null, null)

        try {
            val result = future.result
            val intent = result.getParcelable<Intent>(AccountManager.KEY_INTENT)
            if (intent != null) {
                return AuthTokenResult.HasIntent(intent)
            }
            return AuthTokenResult.AuthToken(result.getString(AccountManager.KEY_AUTHTOKEN)!!)
        } catch (e: OperationCanceledException) {
            return AuthTokenResult.Error(Other(e.message.orEmpty()))
        } catch (e: IOException) {
            return AuthTokenResult.Error(Network)
        } catch (e: AuthenticatorException) {
            return AuthTokenResult.Error(Other(e.message.orEmpty()))
        }
    }

    fun invalidateToken(token: String?) {
        accountManager.invalidateAuthToken(ACCOUNT_TYPE_GOOGLE, token)
    }

    override fun onSharedPreferenceChanged(prefs: SharedPreferences, key: String) {
        updateLiveData()
    }

    sealed class AuthTokenResult {
        data class Error(val errorType: ErrorType) : AuthTokenResult()
        data class AuthToken(val token: String) : AuthTokenResult()
        data class HasIntent(val intent: Intent) : AuthTokenResult()
    }

    sealed class ErrorType {
        object AccountRemoved : ErrorType()
        object Network : ErrorType()
        data class Other(val msg: String) : ErrorType()
    }

    companion object {
        private const val PREF_KEY_DEFAULT_ACCOUNT_NAME = "pref_key_default_account_name"
        private const val SCOPE_YOUTUBE = "oauth2:https://www.googleapis.com/auth/youtube"
        private const val ACCOUNT_TYPE_GOOGLE = "com.google"
    }
}
