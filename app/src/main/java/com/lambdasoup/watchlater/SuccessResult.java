/*
 * Copyright (c) 2015 - 2016
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

import android.os.Parcel;
import android.os.Parcelable;


class SuccessResult implements Parcelable {
    public static final Creator<SuccessResult> CREATOR = new Creator<SuccessResult>() {
        public SuccessResult createFromParcel(Parcel source) {
            return new SuccessResult(source);
        }

        public SuccessResult[] newArray(int size) {
            return new SuccessResult[size];
        }
    };
    final String title;
    final String description;


    SuccessResult(String title, String description) {
        this.title = title;
        this.description = description;
    }


    SuccessResult(Parcel in) {
        this.title = in.readString();
        this.description = in.readString();
    }

    @Override
    public String toString() {
        return "SuccessResult{" +
                "title='" + title + '\'' +
                ", description='" + description + '\'' +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.title);
        dest.writeString(this.description);
    }
}
