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

package com.lambdasoup.watchlater.test;

import android.os.SystemClock;
import android.util.Log;

import com.squareup.okhttp.mockwebserver.Dispatcher;
import com.squareup.okhttp.mockwebserver.MockResponse;
import com.squareup.okhttp.mockwebserver.RecordedRequest;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("WeakerAccess")
public class RestfulDispatcher extends Dispatcher {
    private static final String                                  STATUS_NOT_FOUND = "HTTP/1.1 404 Not Found";
    private static final String                                  TAG              = "RestfulDispatcher";
    private final        ConcurrentHashMap<String, MockResponse> responses        = new ConcurrentHashMap<>();
    private int delayDuration = 0;
    private TimeUnit delayTimeUnit = TimeUnit.SECONDS;

    /**
     * Registers a canned response for a particular full path. Request method and all other info
     * will be ignored for matching.
     */
    public void registerResponse(String path, MockResponse response) {
        MockResponse prevValue = responses.put(path, response);
        if (prevValue != null) {
            Log.w(TAG, "Overwrote previously set response " + prevValue + " for path " + path);
        }
    }

    @Override
    public MockResponse dispatch(RecordedRequest request) throws InterruptedException {
        // To permit interactive/browser testing, ignore requests for favicons.
        final String requestLine = request.getRequestLine();
        if (requestLine != null && requestLine.equals("GET /favicon.ico HTTP/1.1")) {
            System.out.println("served " + requestLine);
            return new MockResponse().setStatus(STATUS_NOT_FOUND);
        }

        MockResponse response = responses.get(request.getPath());
        if (response == null) {
            Log.d(TAG, "No canned response available for path " + request.getPath());
            response = getFailureResponse();
        }
        if (delayDuration != 0) {
            SystemClock.sleep(delayTimeUnit.toMillis(delayDuration));
        }
        return response;
    }

    private MockResponse getFailureResponse() {
        return new MockResponse().setStatus(STATUS_NOT_FOUND);
    }

    /**
     * Clears all prerecorded responses
     */
    public void clear() {
        responses.clear();
    }

    public void setDelay(int duration, TimeUnit timeUnit) {
        delayDuration = duration;
        delayTimeUnit = timeUnit;
    }
}
