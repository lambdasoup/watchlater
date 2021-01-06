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
package com.lambdasoup.watchlater.util

import android.net.Uri

class VideoIdParser {
    fun parseVideoId(uri: Uri): String? {
        // e.g. vnd.youtube:jqxENMKaeCU
        if (uri.isOpaque) {
            return uri.schemeSpecificPart
        }

        // e.g. https://www.youtube.com/watch?v=jqxENMKaeCU
        var videoId = uri.getQueryParameter("v")
        if (videoId != null) {
            return videoId
        }

        // e.g.https://www.youtube.com/playlist?list=PLxLNk7y0uwqfXzUjcbVT3UuMjRd7pOv_U
        videoId = uri.getQueryParameter("list")
        if (videoId != null) {
            return null
        }

        // e.g. http://www.youtube.com/attribution_link?u=/watch%3Fv%3DJ1zNbWJC5aw%26feature%3Dem-subs_digest
        if (uri.pathSegments.isNotEmpty() && "attribution_link" == uri.pathSegments[0]) {
            val encodedUri = uri.getQueryParameter("u")
            return if (encodedUri != null) {
                parseVideoId(Uri.parse(Uri.decode(encodedUri)))
            } else {
                null
            }
        }

        // e.g. http://www.youtube.com/v/OdT9z-JjtJk
        // http://www.youtube.com/embed/UkWd0azv3fQ
        // http://youtu.be/jqxENMKaeCU
        return uri.lastPathSegment
    }
}
