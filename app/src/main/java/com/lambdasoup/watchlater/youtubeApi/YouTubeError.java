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
public class YouTubeError {
	public final RootError error;

	public YouTubeError(RootError error) {
		this.error = error;
	}

	@SuppressWarnings("unused")
	public class RootError {
		public final int               code;
		public final String            message;
		public final List<ErrorDetail> errors;

		public RootError(int code, String message, List<ErrorDetail> errors) {
			this.code = code;
			this.message = message;
			this.errors = errors;
		}

		@SuppressWarnings("unused")
		public class ErrorDetail {
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
