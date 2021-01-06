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
package com.lambdasoup.watchlater.ui

import android.accounts.Account
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.lifecycle.Observer
import com.lambdasoup.watchlater.R

class AccountView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr), View.OnClickListener, Observer<Account?> {

    var listener: Listener? = null

    private val label: TextView

    init {
        LayoutInflater.from(context).inflate(R.layout.view_account, this)
        findViewById<View>(R.id.view_account_set).setOnClickListener(this)
        label = findViewById(R.id.view_account_label)
    }

    override fun onClick(view: View) {
        listener?.onSetAccount()
    }

    override fun onChanged(account: Account?) {
        if (account == null) {
            label.setText(R.string.account_empty)
            return
        }
        label.text = account.name
    }

    interface Listener {
        fun onSetAccount()
    }
}
