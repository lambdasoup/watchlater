/*
 *   Copyright (c) 2015 - 2017
 *
 *   Maximilian Hille <mh@lambdasoup.com>
 *   Juliane Lehmann <jl@lambdasoup.com>
 *
 *   This file is part of Watch Later.
 *
 *   Watch Later is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   Watch Later is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with Watch Later.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.lambdasoup.watchlater.data;

import android.content.Context;

import com.lambdasoup.watchlater.BuildConfig;
import com.lambdasoup.watchlater.R;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Converter;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Query;

public class YoutubeRepository {

	private static final String YOUTUBE_ENDPOINT = "https://www.googleapis.com/youtube/v3/";

	private final Retrofit   retrofit;
	private final String     apiKey;
	private final YoutubeApi api;

	public YoutubeRepository(Context context) {
		OkHttpClient.Builder httpClient = new OkHttpClient.Builder();

		if (BuildConfig.DEBUG) {
			HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
			loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
			httpClient.networkInterceptors().add(loggingInterceptor);
		}

		apiKey = context.getString(R.string.youtube_api_key);

		Retrofit.Builder retrofitBuilder = new Retrofit.Builder()
				.baseUrl(YOUTUBE_ENDPOINT)
				.addConverterFactory(GsonConverterFactory.create())
				.client(httpClient.build());

		retrofit = retrofitBuilder.build();
		api = retrofit.create(YoutubeApi.class);
	}

	public void getVideoInfo(String videoId, VideoInfoCallback callback) {
		api.listVideos(videoId, apiKey).enqueue(new ErrorTranslatingCallback<Videos>(retrofit) {
			@Override
			protected void failure(ErrorType errorType) {
				callback.onVideoInfoResult(errorType, null);
			}

			@Override
			protected void success(Videos result) {
				callback.onVideoInfoResult(null, result.items.get(0));
			}
		});
	}

	public void addVideo(String videoId, String token, AddVideoCallback callback) {
		PlaylistItem.Snippet.ResourceId resourceId = new PlaylistItem.Snippet.ResourceId(videoId);
		PlaylistItem.Snippet snippet = new PlaylistItem.Snippet("WL", resourceId, null, null);
		PlaylistItem item = new PlaylistItem(snippet);
		String auth = "Bearer " + token;
		api.insertPlaylistItem(item, auth).enqueue(new ErrorTranslatingCallback<PlaylistItem>(retrofit) {
			@Override
			protected void failure(ErrorType errorType) {
				callback.onAddResult(errorType, token);
			}

			@Override
			protected void success(PlaylistItem result) {
				callback.onAddResult(null, token);
			}
		});
	}

	public enum ErrorType {
		NEED_ACCESS, NETWORK, OTHER, PLAYLIST_FULL, INVALID_TOKEN, VIDEO_NOT_FOUND, ALREADY_IN_PLAYLIST
	}

	public interface VideoInfoCallback {
		void onVideoInfoResult(ErrorType errorType, Videos.Item item);
	}

	public interface AddVideoCallback {
		void onAddResult(ErrorType errorType, String token);
	}

	private interface YoutubeApi {

		@POST("playlistItems?part=snippet")
		Call<PlaylistItem> insertPlaylistItem(@Body PlaylistItem playlistItem, @Header("Authorization") String auth);

		@GET("videos?part=snippet,contentDetails&maxResults=1")
		Call<Videos> listVideos(@Query("id") String id, @Query("key") String apiKey);

	}

	@SuppressWarnings({"SameParameterValue", "unused"})
	public static class Videos {

		final List<Item> items;

		public Videos(List<Item> items) {
			this.items = items;
		}

		public static class Item {
			public final String         id;
			public final Snippet        snippet;
			public final ContentDetails contentDetails;

			public Item(String id, Snippet snippet, ContentDetails contentDetails) {
				this.id = id;
				this.snippet = snippet;
				this.contentDetails = contentDetails;
			}

			public static class Snippet {
				public final String     title;
				public final String     description;
				public final Thumbnails thumbnails;

				public Snippet(String title, String description, Thumbnails thumbnails) {
					this.title = title;
					this.description = description;
					this.thumbnails = thumbnails;
				}

				public static class Thumbnails {
					public final Thumbnail medium;

					public Thumbnails(Thumbnail medium) {
						this.medium = medium;
					}

					public static class Thumbnail {
						public final String url;

						public Thumbnail(String url) {
							this.url = url;
						}
					}
				}
			}

			public static class ContentDetails {
				public final String duration;

				public ContentDetails(String duration) {
					this.duration = duration;
				}
			}
		}
	}

	@SuppressWarnings({"SameParameterValue", "unused"})
	static class PlaylistItem {
		final Snippet snippet;

		PlaylistItem(Snippet snippet) {
			this.snippet = snippet;
		}

		public static class Snippet {
			public final String     title;
			final        String     description;
			final        String     playlistId;
			final        ResourceId resourceId;

			Snippet(String playlistId, ResourceId resourceId, String title, String description) {
				this.playlistId = playlistId;
				this.resourceId = resourceId;
				this.title = title;
				this.description = description;
			}

			static class ResourceId {
				@SuppressWarnings("unused")
				final String kind = "youtube#video";
				final String videoId;

				ResourceId(String videoId) {
					this.videoId = videoId;
				}
			}
		}
	}

	@SuppressWarnings({"SameParameterValue", "unused"})
	static class YouTubeError {
		final RootError error;

		public YouTubeError(RootError error) {
			this.error = error;
		}

		class RootError {
			final int               code;
			final String            message;
			final List<ErrorDetail> errors;

			public RootError(int code, String message, List<ErrorDetail> errors) {
				this.code = code;
				this.message = message;
				this.errors = errors;
			}

			class ErrorDetail {
				final String domain;
				final String reason;
				final String message;

				public ErrorDetail(String domain, String reason, String message) {
					this.domain = domain;
					this.reason = reason;
					this.message = message;
				}
			}
		}
	}

	static abstract class ErrorTranslatingCallback<T> implements Callback<T> {

		static final String DAILY_LIMIT_EXCEEDED_UNREG                 = "dailyLimitExceededUnreg";
		static final String VIDEO_ALREADY_IN_PLAYLIST                  = "videoAlreadyInPlaylist";
		static final String PLAYLIST_CONTAINS_MAXIMUM_NUMBER_OF_VIDEOS = "playlistContainsMaximumNumberOfVideos";
		static final String VIDEO_NOT_FOUND                            = "videoNotFound";

		private final Converter<ResponseBody, YouTubeError> youTubeErrorConverter;


		ErrorTranslatingCallback(Retrofit retrofit) {
			youTubeErrorConverter = retrofit.responseBodyConverter(YouTubeError.class, new Annotation[0]);
		}


		ErrorType translateError(Response<T> errorResponse) {
			YouTubeError youtubeError;
			try {
				youtubeError = youTubeErrorConverter.convert(errorResponse.errorBody());
			} catch (IOException e) {
				return ErrorType.OTHER;
			}

			if (youtubeError == null) {
				return ErrorType.OTHER;
			}

			String errorDetail = "";
			if (youtubeError.error.errors != null
					&& youtubeError.error.errors.size() >= 1) {
				errorDetail = youtubeError.error.errors.get(0).reason;
			}

			switch (errorResponse.code()) {
				case 401:
					return ErrorType.INVALID_TOKEN;
				case 403:
					switch (errorDetail) {
						case DAILY_LIMIT_EXCEEDED_UNREG:
							return ErrorType.INVALID_TOKEN;
						case PLAYLIST_CONTAINS_MAXIMUM_NUMBER_OF_VIDEOS:
							return ErrorType.PLAYLIST_FULL;
					}
					return ErrorType.NEED_ACCESS;
				case 404:
					switch (errorDetail) {
						case VIDEO_NOT_FOUND:
							return ErrorType.VIDEO_NOT_FOUND;
					}
					return ErrorType.OTHER;
				case 409:
					switch (errorDetail) {
						case VIDEO_ALREADY_IN_PLAYLIST:
							return ErrorType.ALREADY_IN_PLAYLIST;
					}
					return ErrorType.OTHER;

				default:
					return ErrorType.OTHER;
			}
		}

		@Override
		public void onFailure(Call<T> call, Throwable t) {
			failure(ErrorType.NETWORK);
		}

		@Override
		public void onResponse(Call<T> call, Response<T> response) {
			if (response.isSuccessful()) {
				success(response.body());
			} else {
				failure(translateError(response));
			}
		}

		protected abstract void failure(ErrorType errorType);

		protected abstract void success(T result);
	}

}
