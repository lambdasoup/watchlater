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

/**
 * Created by jl on 14.07.16.
 */
public class PlaylistItem {
	public final Snippet snippet;

	public PlaylistItem(Snippet snippet) {
		this.snippet = snippet;
	}

	@SuppressWarnings("unused")
	public static class Snippet {
		public final String playlistId;
		public final ResourceId resourceId;
		public final String title;
		public final String description;

		@SuppressWarnings("SameParameterValue")
		public Snippet(String playlistId, ResourceId resourceId, String title, String description) {
			this.playlistId = playlistId;
			this.resourceId = resourceId;
			this.title = title;
			this.description = description;
		}

		@SuppressWarnings("unused")
		public static class ResourceId {
			@SuppressWarnings("unused")
			public final String kind = "youtube#video";
			public final String videoId;

			public ResourceId(String videoId) {
				this.videoId = videoId;
			}
		}
	}
}
