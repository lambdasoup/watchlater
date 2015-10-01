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

import android.util.Log;

import java.util.List;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.http.Body;
import retrofit.http.GET;
import retrofit.http.POST;

/**
 * Youtube's Data Api
 * https://developers.google.com/youtube/v3/docs/
 * <p/>
 * Created by mh on 22.02.15.
 */
public interface YoutubeApi {
	 String TAG = "YoutubeApi";

	@GET("/channels?part=contentDetails,snippet&maxResults=50&mine=true")
	void listMyChannels(Callback<Channels> cb);

	@POST("/playlistItems?part=snippet")
	void insertPlaylistItem(@Body PlaylistItem playlistItem, Callback<PlaylistItem> cb);

	class Channels {
		public final List<Channel> items;

		public Channels(List<Channel> items) {
			this.items = items;
		}

		static class Channel {
			public final Snippet snippet;
			public final ContentDetails contentDetails;

			public Channel(Snippet snippet, ContentDetails contentDetails) {
				this.snippet = snippet;
				this.contentDetails = contentDetails;
			}

			static class ContentDetails {
				public final RelatedPlaylists relatedPlaylists;

				public ContentDetails(RelatedPlaylists relatedPlaylists) {
					this.relatedPlaylists = relatedPlaylists;
				}

				static class RelatedPlaylists {
					public final String watchLater;

					public RelatedPlaylists(String watchLater) {
						this.watchLater = watchLater;
					}
				}
			}

			static class Snippet {
				public final String title;

				public Snippet(String title) {
					this.title = title;
				}
			}
		}
	}

	class PlaylistItem {
		public final Snippet snippet;

		public PlaylistItem(Snippet snippet) {
			this.snippet = snippet;
		}

		static class Snippet {
			public final String playlistId;
			public final ResourceId resourceId;
			public final String title;
			public final String description;

			public Snippet(String playlistId, ResourceId resourceId, String title, String description) {
				this.playlistId = playlistId;
				this.resourceId = resourceId;
				this.title = title;
				this.description = description;
			}

			static class ResourceId {
				@SuppressWarnings("unused")
				public final String kind = "youtube#video";
				public final String videoId;

				public ResourceId(String videoId) {
					this.videoId = videoId;
				}
			}
		}
	}


	class YouTubeError {
		public final RootError error;

		public YouTubeError(RootError error) {
			this.error = error;
		}

		class RootError {
			public final int code;
			public final String message;
			public final List<ErrorDetail> errors;

			public RootError(int code, String message, List<ErrorDetail> errors) {
				this.code = code;
				this.message = message;
				this.errors = errors;
			}

			class ErrorDetail {
				public final String domain;
				public final String reason;
				public final String message;

				public ErrorDetail(String domain, String reason, String message) {
					this.domain = domain;
					this.reason = reason;
					this.message = message;
				}
			}
		}
	}

	enum ErrorType {
		NEED_ACCESS, NETWORK, OTHER, PLAYLIST_FULL, NOT_A_VIDEO, INVALID_TOKEN, VIDEO_NOT_FOUND, ALREADY_IN_PLAYLIST, PERMISSION_REQUIRED_ACCOUNTS;
	}

	abstract class ErrorTranslatingCallback<T> implements Callback<T> {

		public static final String DAILY_LIMIT_EXCEEDED_UNREG = "dailyLimitExceededUnreg";
		public static final String VIDEO_ALREADY_IN_PLAYLIST = "videoAlreadyInPlaylist";
		public static final String PLAYLIST_CONTAINS_MAXIMUM_NUMBER_OF_VIDEOS = "playlistContainsMaximumNumberOfVideos";
		public static final String VIDEO_NOT_FOUND = "videoNotFound";

		@Override
		final public void failure(RetrofitError error) {
			failure(translateError(error));
		}

		protected abstract void failure(ErrorType errorType);

		public static ErrorType translateError(RetrofitError error) {
			if (error.getResponse() == null) {
				return ErrorType.NETWORK;
			}

			YouTubeError youtubeError = (YouTubeError) error.getBodyAs(YouTubeError.class);
			if (youtubeError == null) {
				Log.d(TAG, "Expected a youtube api error response, got instead: " + error);
				return ErrorType.OTHER;
			}

			String errorDetail = "";
			if (youtubeError.error.errors != null
					&& youtubeError.error.errors.size() >= 1) {
				errorDetail = youtubeError.error.errors.get(0).reason;
			}

			switch (error.getResponse().getStatus()) {
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
	}



}
