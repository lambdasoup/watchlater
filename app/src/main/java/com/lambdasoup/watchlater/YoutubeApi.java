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

import java.util.List;

import retrofit.Callback;
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

	@GET("/channels?part=contentDetails&maxResults=50&mine=true")
	public void listMyChannels(Callback<Channels> cb);

	@POST("/playlistItems?part=snippet")
	public void insertPlaylistItem(@Body PlaylistItem playlistItem, Callback<PlaylistItem> cb);

	public static class Channels {

		public final List<Channel> items;

		public Channels(List<Channel> items) {
			this.items = items;
		}
	}

	public static class PlaylistItem {

		public final Snippet snippet;

		public PlaylistItem(Snippet snippet) {
			this.snippet = snippet;
		}
	}

	public static class Snippet {
		public final String playlistId;
		public final ResourceId resourceId;

		public Snippet(String playlistId, ResourceId resourceId) {
			this.playlistId = playlistId;
			this.resourceId = resourceId;
		}
	}

	public static class ResourceId {
		public final String kind;
		public final String videoId;

		public ResourceId(String kind, String videoId) {
			this.kind = kind;
			this.videoId = videoId;
		}
	}

	public static class Channel {

		public final ContentDetails contentDetails;

		public Channel(ContentDetails contentDetails) {
			this.contentDetails = contentDetails;
		}
	}

	public static class ContentDetails {

		public final RelatedPlaylists relatedPlaylists;

		public ContentDetails(RelatedPlaylists relatedPlaylists) {
			this.relatedPlaylists = relatedPlaylists;
		}
	}

	public static class RelatedPlaylists {

		public final String watchLater;

		public RelatedPlaylists(String watchLater) {
			this.watchLater = watchLater;
		}
	}
}
