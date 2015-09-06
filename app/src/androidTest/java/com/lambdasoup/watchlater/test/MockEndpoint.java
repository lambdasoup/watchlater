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

package com.lambdasoup.watchlater.test;

import android.net.Uri;

import java.util.HashMap;
import java.util.Map;

import fi.iki.elonen.NanoHTTPD;

/**
 * Mock HTTP Endpoint for testing
 */
public class MockEndpoint extends NanoHTTPD {

	private final Map<String, String> handlers = new HashMap<>();

	public MockEndpoint(String hostname, int port) {
		super(hostname, port);
	}

	public void add(String path, String body) {
		handlers.put(path, body);
	}

	@Override
	public Response serve(IHTTPSession session) {
		Uri uri = Uri.parse(session.getUri());
		String path = uri.getEncodedPath();
		if (!handlers.containsKey(path)) {
			return super.serve(session);
		}

		return new Response(handlers.get(path));
	}
}
