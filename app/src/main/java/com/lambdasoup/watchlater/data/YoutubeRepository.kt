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

import android.content.Context
import com.lambdasoup.watchlater.BuildConfig
import com.lambdasoup.watchlater.R
import com.lambdasoup.watchlater.data.YoutubeRepository.PlaylistItem.Snippet.ResourceId
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.*
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import java.io.IOException

class YoutubeRepository(context: Context) {

    private val retrofit: Retrofit
    private val apiKey: String
    private val api: YoutubeApi

    init {
        val httpClient = OkHttpClient.Builder()
        if (BuildConfig.DEBUG) {
            val loggingInterceptor = HttpLoggingInterceptor()
            loggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
            httpClient.networkInterceptors().add(loggingInterceptor)
        }
        apiKey = context.getString(R.string.youtube_api_key)
        val retrofitBuilder = Retrofit.Builder()
                .baseUrl(YOUTUBE_ENDPOINT)
                .addConverterFactory(GsonConverterFactory.create())
                .client(httpClient.build())
        retrofit = retrofitBuilder.build()
        api = retrofit.create(YoutubeApi::class.java)
    }

    fun getVideoInfo(videoId: String, callback: VideoInfoCallback) {
        val value = object : ErrorTranslatingCallback<Videos>(retrofit) {
            override fun failure(errorType: ErrorType) {
                callback.onVideoInfoResult(errorType, null)
            }

            override fun success(result: Videos) {
                callback.onVideoInfoResult(null, result.items[0])
            }
        }
        api.listVideos(videoId, apiKey).enqueue(value)
    }

    fun addVideo(videoId: String?, token: String, callback: AddVideoCallback) {
        val resourceId = ResourceId(videoId)
        val snippet = PlaylistItem.Snippet("WL", resourceId, null, null)
        val item = PlaylistItem(snippet)
        val auth = "Bearer $token"
        api.insertPlaylistItem(item, auth).enqueue(object : ErrorTranslatingCallback<PlaylistItem?>(retrofit) {
            override fun failure(errorType: ErrorType) {
                callback.onAddResult(errorType, token)
            }

            override fun success(result: PlaylistItem?) {
                callback.onAddResult(null, token)
            }
        })
    }

    enum class ErrorType {
        NeedAccess, Network, Other, PlaylistFull, InvalidToken, VideoNotFound, AlreadyInPlaylist
    }

    interface VideoInfoCallback {
        fun onVideoInfoResult(errorType: ErrorType?, item: Videos.Item?)
    }

    interface AddVideoCallback {
        fun onAddResult(errorType: ErrorType?, token: String?)
    }

    private interface YoutubeApi {
        @POST("playlistItems?part=snippet")
        fun insertPlaylistItem(@Body playlistItem: PlaylistItem, @Header("Authorization") auth: String): Call<PlaylistItem?>

        @GET("videos?part=snippet,contentDetails&maxResults=1")
        fun listVideos(@Query("id") id: String, @Query("key") apiKey: String): Call<Videos>
    }

    class Videos(val items: List<Item>) {
        class Item(val id: String, val snippet: Snippet, val contentDetails: ContentDetails) {
            class Snippet(val title: String, val description: String, val thumbnails: Thumbnails) {
                class Thumbnails(val medium: Thumbnail) {
                    class Thumbnail(val url: String)
                }
            }

            class ContentDetails(val duration: String)
        }
    }

    internal class PlaylistItem(val snippet: Snippet) {
        class Snippet internal constructor(val playlistId: String, val resourceId: ResourceId, val title: String?, val description: String?) {
            internal class ResourceId(val videoId: String?) {
                val kind = "youtube#video"
            }
        }
    }

    internal class YouTubeError(val error: RootError) {
        internal inner class RootError(val code: Int, val message: String, val errors: List<ErrorDetail>?) {
            internal inner class ErrorDetail(val domain: String, val reason: String, val message: String)
        }
    }

    internal abstract class ErrorTranslatingCallback<T>(retrofit: Retrofit) : Callback<T> {
        private val youTubeErrorConverter: Converter<ResponseBody?, YouTubeError> =
                retrofit.responseBodyConverter(YouTubeError::class.java, arrayOfNulls(0))

        private fun translateError(errorResponse: Response<T>): ErrorType {
            val youtubeError: YouTubeError = try {
                youTubeErrorConverter.convert(errorResponse.errorBody()!!)
            } catch (e: IOException) {
                return ErrorType.Other
            } ?: return ErrorType.Other
            var errorDetail = ""
            if (youtubeError.error.errors != null
                    && youtubeError.error.errors.isNotEmpty()) {
                errorDetail = youtubeError.error.errors[0].reason
            }
            return when (errorResponse.code()) {
                401 -> ErrorType.InvalidToken
                403 -> {
                    when (errorDetail) {
                        DAILY_LIMIT_EXCEEDED_UNREG -> return ErrorType.InvalidToken
                        PLAYLIST_CONTAINS_MAXIMUM_NUMBER_OF_VIDEOS -> return ErrorType.PlaylistFull
                    }
                    ErrorType.NeedAccess
                }
                404 -> {
                    when (errorDetail) {
                        VIDEO_NOT_FOUND -> return ErrorType.VideoNotFound
                    }
                    ErrorType.Other
                }
                409 -> {
                    when (errorDetail) {
                        VIDEO_ALREADY_IN_PLAYLIST -> return ErrorType.AlreadyInPlaylist
                    }
                    ErrorType.Other
                }
                else -> ErrorType.Other
            }
        }

        override fun onFailure(call: Call<T>, t: Throwable) {
            failure(ErrorType.Network)
        }

        override fun onResponse(call: Call<T>, response: Response<T>) {
            if (response.isSuccessful) {
                success(response.body()!!)
            } else {
                failure(translateError(response))
            }
        }

        protected abstract fun failure(errorType: ErrorType)
        protected abstract fun success(result: T)

        companion object {
            const val DAILY_LIMIT_EXCEEDED_UNREG = "dailyLimitExceededUnreg"
            const val VIDEO_ALREADY_IN_PLAYLIST = "videoAlreadyInPlaylist"
            const val PLAYLIST_CONTAINS_MAXIMUM_NUMBER_OF_VIDEOS = "playlistContainsMaximumNumberOfVideos"
            const val VIDEO_NOT_FOUND = "videoNotFound"
        }

    }

    companion object {
        private const val YOUTUBE_ENDPOINT = "https://www.googleapis.com/youtube/v3/"
    }
}
