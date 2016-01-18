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

import android.support.annotation.IdRes;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.Set;


enum ErrorResult {
    ALREADY_IN_PLAYLIST(R.string.error_already_in_playlist),
    NEED_ACCESS(R.string.error_need_account, ErrorExtraViewId.RETRY),
    NOT_A_VIDEO(R.string.error_not_a_video),
    OTHER(R.string.error_other, ErrorExtraViewId.RETRY),
    PERMISSION_REQUIRED_ACCOUNTS(R.string.error_permission_required_accounts, ErrorExtraViewId.RETRY),
    PLAYLIST_FULL(R.string.error_playlist_full, ErrorExtraViewId.RETRY),
    VIDEO_NOT_FOUND(R.string.error_video_not_found),
    NO_ACCOUNT(R.string.error_no_account, ErrorExtraViewId.RETRY),
    ACCOUNT_HAS_NO_CHANNEL(R.string.error_account_has_no_channel, ErrorExtraViewId.RETRY, ErrorExtraViewId.HELP_NO_CHANNEL);

    final int msgId;
    final Set<ErrorExtraViewId> additionalViewIds;

    ErrorResult(int msgId, @IdRes ErrorExtraViewId... additionalViewIds) {
        this.additionalViewIds = additionalViewIds.length == 0 ? EnumSet.noneOf(ErrorExtraViewId.class) : EnumSet.copyOf(Arrays.asList(additionalViewIds));
        this.msgId = msgId;
    }

    static ErrorResult fromErrorType(YoutubeApi.ErrorType errorType) {
        switch (errorType) {
            case ACCOUNT_HAS_NO_CHANNEL:
                return ACCOUNT_HAS_NO_CHANNEL;
            case ALREADY_IN_PLAYLIST:
                return ALREADY_IN_PLAYLIST;
            case NEED_ACCESS:
                return NEED_ACCESS;
            case NO_ACCOUNT:
                return NO_ACCOUNT;
            case NOT_A_VIDEO:
                return NOT_A_VIDEO;
            case OTHER:
            case NETWORK:
                return OTHER;
            case PERMISSION_REQUIRED_ACCOUNTS:
                return PERMISSION_REQUIRED_ACCOUNTS;
            case PLAYLIST_FULL:
                return PLAYLIST_FULL;
            case VIDEO_NOT_FOUND:
                return VIDEO_NOT_FOUND;
            default:
                throw new IllegalArgumentException("Unexpected error type: " + errorType);
        }
    }

    enum ErrorExtraViewId {
        RETRY(R.id.button_retry),
        HELP_NO_CHANNEL(R.id.activetext_help_no_channel);

        final
        @IdRes
        int buttonId;

        ErrorExtraViewId(@IdRes int buttonId) {
            this.buttonId = buttonId;
        }
    }
}
