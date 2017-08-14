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

package com.lambdasoup.watchlater.test;

import android.support.test.espresso.IdlingResource;

import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import okhttp3.internal.Util;

class RetrofitHttpExecutorIdlingResource extends ThreadPoolExecutor implements IdlingResource {
    private final AtomicInteger currentTaskCount = new AtomicInteger(0);
    private volatile ResourceCallback idleTransitionCallback;


	RetrofitHttpExecutorIdlingResource() {
		// imitate the okhttp3 default Dispatcher executor properties
        super(0, Integer.MAX_VALUE, 60L, TimeUnit.SECONDS, new SynchronousQueue<>(), Util.threadFactory("Idiling resource OkHttp Dispatcher", false));
    }

    @Override
    public String getName() {
        return RetrofitHttpExecutorIdlingResource.class.getName();
    }

    @Override
    public boolean isIdleNow() {
        boolean idle = currentTaskCount.intValue() == 0;
        if (idle && idleTransitionCallback != null) {
            idleTransitionCallback.onTransitionToIdle();
        }
        return idle;
    }

    @Override
    public void registerIdleTransitionCallback(ResourceCallback resourceCallback) {
        this.idleTransitionCallback = resourceCallback;
    }

    @Override
    protected void afterExecute(Runnable r, Throwable t) {
        super.afterExecute(r, t);
        if (currentTaskCount.decrementAndGet() == 0 && idleTransitionCallback != null) {
            idleTransitionCallback.onTransitionToIdle();
        }
    }


    @Override
    public void execute(Runnable command) {
        currentTaskCount.incrementAndGet();
        super.execute(command);
    }
}
