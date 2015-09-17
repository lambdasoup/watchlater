/*
 * Copyright (c) 2015. Maximilian Hille <mh@lambdasoup.com>
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
import android.support.annotation.IdRes;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ViewAnimator;

/**
 * Created by stroke on 17.09.15.
 */
public class AddressableViewAnimator extends ViewAnimator {
    public AddressableViewAnimator(Context context) {
        super(context);
    }

    public AddressableViewAnimator(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void switchToView(@IdRes int id) {
        if (getCurrentView().getId() == id) {
            return;
        }

        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            if (child.getId() == id) {
                setDisplayedChild(i);
                return;
            }
        }
        throw new IllegalArgumentException("animator " + getId() + " does not have a child with id " + id);
    }
}
