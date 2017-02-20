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

import com.lambdasoup.watchlater.model.ErrorResult;
import com.lambdasoup.watchlater.youtubeApi.ErrorType;
import com.lambdasoup.watchlater.youtubeApi.YoutubeApi;


class WatchlaterResult implements Parcelable {
    public static final Creator<WatchlaterResult> CREATOR = new Creator<WatchlaterResult>() {
        public WatchlaterResult createFromParcel(Parcel source) {
            return new WatchlaterResult(source);
        }

        public WatchlaterResult[] newArray(int size) {
            return new WatchlaterResult[size];
        }
    };
    private final SuccessResult success;
    private final ErrorResult   error;

    private WatchlaterResult(SuccessResult success, ErrorResult error) {
        if ((success == null) == (error == null)) {
            throw new IllegalArgumentException("Exactly one of success, error must be null");
        }
        this.success = success;
        this.error = error;
    }

    WatchlaterResult(Parcel in) {
        this.success = in.readParcelable(SuccessResult.class.getClassLoader());
        int tmpError = in.readInt();
        this.error = tmpError == -1 ? null : ErrorResult.values()[tmpError];
    }

    static WatchlaterResult success(String title, String description) {
        return new WatchlaterResult(new SuccessResult(title, description), null);
    }

    static WatchlaterResult error(ErrorType errorType) {
        return new WatchlaterResult(null, ErrorResult.fromErrorType(errorType));
    }

    private boolean isSuccess() {
        return success != null;
    }

    void apply(VoidFunction<SuccessResult> onSuccess, VoidFunction<ErrorResult> onError) {
        if (isSuccess()) {
            onSuccess.apply(success);
        } else {
            onError.apply(error);
        }
    }

    @Override
    public String toString() {
        if (isSuccess()) {
            return "WatchlaterResult " + success;
        } else {
            return "WatchlaterResult " + error;
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(this.success, 0);
        dest.writeInt(this.error == null ? -1 : this.error.ordinal());
    }

    interface VoidFunction<T> {
        void apply(T t);
    }
}
