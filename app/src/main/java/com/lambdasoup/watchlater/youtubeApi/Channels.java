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

import java.util.List;

/**
 * Created by jl on 14.07.16.
 */
public class Channels {
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
