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
package com.lambdasoup.watchlater.ui.add

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import com.lambdasoup.watchlater.R

class PermissionsView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr), View.OnClickListener {

    var listener: Listener? = null

    init {
        LayoutInflater.from(context).inflate(R.layout.view_permissions, this)
        findViewById<View>(R.id.view_permissions_grant).setOnClickListener(this)
    }

    override fun onClick(view: View) {
        listener?.onGrantPermissionsClicked()
    }

    interface Listener {
        fun onGrantPermissionsClicked()
    }
}
