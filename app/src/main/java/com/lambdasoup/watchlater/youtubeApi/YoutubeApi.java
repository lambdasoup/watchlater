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

package com.lambdasoup.watchlater.youtubeApi;

import android.util.Log;

import com.lambdasoup.watchlater.youtubeApi.Channels;
import com.lambdasoup.watchlater.youtubeApi.ErrorType;
import com.lambdasoup.watchlater.youtubeApi.PlaylistItem;
import com.lambdasoup.watchlater.youtubeApi.PlaylistItemResponse;
import com.lambdasoup.watchlater.youtubeApi.Video;
import com.lambdasoup.watchlater.youtubeApi.YouTubeError;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Converter;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Query;

/**
 * Youtube's Data Api
 * https://developers.google.com/youtube/v3/docs/
 * <p>
 * Created by mh on 22.02.15.
 */
public interface YoutubeApi {
    String TAG = "YoutubeApi";

    @GET("channels?part=contentDetails,snippet&maxResults=50&mine=true")
    Call<Channels> listMyChannels();

    @POST("playlistItems?part=snippet")
    Call<PlaylistItem> insertPlaylistItem(@Body PlaylistItem playlistItem);

    @GET("playlistItems?part=id&maxResults=1&playlistId=WL")
    Call<PlaylistItemResponse> getPlaylistItemInWl(@Query("videoId") String videoId);

	@Headers("Referer: lambdasoup.com/watchlater")
    @GET("videos?part=snippet")
    Call<Video> getVideoInfo(@Query("id") String id);




}
