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
import com.squareup.moshi.JsonClass
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Converter
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
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
                .addConverterFactory(MoshiConverterFactory.create())
                .client(httpClient.build())
        retrofit = retrofitBuilder.build()
        api = retrofit.create(YoutubeApi::class.java)
    }

    private val youtubeErrorConverter: Converter<ResponseBody?, YouTubeError> =
            retrofit.responseBodyConverter(YouTubeError::class.java, arrayOfNulls(0))

    fun getVideoInfo(videoId: String): VideoInfoResult {
        val response: Response<Videos>
        try {
            response = api.listVideos(videoId, apiKey).execute()
        } catch (e: IOException) {
            return VideoInfoResult.Error(ErrorType.Network)
        }

        if (!response.isSuccessful) {
            return VideoInfoResult.Error(translateError(response))
        }

        val videos = response.body() ?: return VideoInfoResult.Error(ErrorType.Other)

        if (videos.items.isEmpty()) {
            return VideoInfoResult.Error(ErrorType.VideoNotFound)
        }

        return VideoInfoResult.VideoInfo(videos.items[0])
    }

    fun addVideo(videoId: String?, token: String): AddVideoResult {
        val resourceId = ResourceId(videoId)
        val snippet = PlaylistItem.Snippet("WL", resourceId, null, null)
        val item = PlaylistItem(snippet)
        val auth = "Bearer $token"

        val response: Response<PlaylistItem?>
        try {
            response = api.insertPlaylistItem(item, auth).execute()
        } catch (e: IOException) {
            return AddVideoResult.Error(ErrorType.Network, token)
        }

        if (!response.isSuccessful) {
            return AddVideoResult.Error(translateError(response), token)
        }

        return AddVideoResult.Success
    }

    enum class ErrorType {
        NeedAccess, Network, Other, PlaylistFull, InvalidToken, VideoNotFound, AlreadyInPlaylist
    }

    sealed class AddVideoResult {
        object Success : AddVideoResult()
        data class Error(
                val type: ErrorType,
                val token: String,
        ) : AddVideoResult()
    }

    sealed class VideoInfoResult {
        data class VideoInfo(val item: Videos.Item) : VideoInfoResult()
        data class Error(
                val type: ErrorType
        ) : VideoInfoResult()
    }

    private interface YoutubeApi {
        @POST("playlistItems?part=snippet")
        fun insertPlaylistItem(@Body playlistItem: PlaylistItem, @Header("Authorization") auth: String): Call<PlaylistItem?>

        @GET("videos?part=snippet,contentDetails&maxResults=1")
        fun listVideos(@Query("id") id: String, @Query("key") apiKey: String): Call<Videos>
    }

    @JsonClass(generateAdapter = true)
    data class Videos(
            val items: List<Item>,
    ) {

        @JsonClass(generateAdapter = true)
        data class Item(
                val id: String,
                val snippet: Snippet,
                val contentDetails: ContentDetails,
        ) {

            @JsonClass(generateAdapter = true)
            data class Snippet(
                    val title: String,
                    val description: String,
                    val thumbnails: Thumbnails,
            ) {

                @JsonClass(generateAdapter = true)
                data class Thumbnails(
                        val medium: Thumbnail,
                ) {

                    @JsonClass(generateAdapter = true)
                    data class Thumbnail(
                            val url: String,
                    )
                }
            }

            @JsonClass(generateAdapter = true)
            data class ContentDetails(
                    val duration: String,
            )
        }
    }

    @JsonClass(generateAdapter = true)
    data class PlaylistItem(
            val snippet: Snippet,
    ) {

        @JsonClass(generateAdapter = true)
        data class Snippet(
                val playlistId: String,
                val resourceId: ResourceId,
                val title: String?,
                val description: String?,
        ) {

            @JsonClass(generateAdapter = true)
            data class ResourceId(
                    val videoId: String?,
                    val kind: String = "youtube#video",
            )
        }
    }

    @JsonClass(generateAdapter = true)
    data class YouTubeError(
            val error: RootError,
    ) {

        @JsonClass(generateAdapter = true)
        data class RootError(
                val code: Int,
                val message: String,
                val errors: List<ErrorDetail>?,
        ) {

            @JsonClass(generateAdapter = true)
            data class ErrorDetail(
                    val domain: String,
                    val reason: String,
                    val message: String,
            )
        }
    }

    private fun <T> translateError(errorResponse: Response<T>): ErrorType {
        val youtubeError: YouTubeError = try {
            youtubeErrorConverter.convert(errorResponse.errorBody()!!)
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

    companion object {
        private const val YOUTUBE_ENDPOINT = "https://www.googleapis.com/youtube/v3/"

        const val DAILY_LIMIT_EXCEEDED_UNREG = "dailyLimitExceededUnreg"
        const val VIDEO_ALREADY_IN_PLAYLIST = "videoAlreadyInPlaylist"
        const val PLAYLIST_CONTAINS_MAXIMUM_NUMBER_OF_VIDEOS = "playlistContainsMaximumNumberOfVideos"
        const val VIDEO_NOT_FOUND = "videoNotFound"
    }
}
