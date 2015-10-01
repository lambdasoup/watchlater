/*
 * Copyright (c) 2015.
 *
 *  Maximilian Hille <mh@lambdasoup.com>
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

package com.lambdasoup.watchlater;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

import com.lambdasoup.watchlater.AddActivity.SuccessResult;

/**
 * Created by stroke on 17.09.15.
 */
public class WatchlaterDialogContent extends AddressableViewAnimator {
    public WatchlaterDialogContent(Context context) {
        super(context);
    }

    public WatchlaterDialogContent(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    void showAccountChooser() {	switchToView(R.id.account_chooser); }


    void showProgress() { switchToView(R.id.progress); }


    void showError() {
       switchToView(R.id.error);
    }

    void showSuccess() {
        switchToView(R.id.success);
    }

}
