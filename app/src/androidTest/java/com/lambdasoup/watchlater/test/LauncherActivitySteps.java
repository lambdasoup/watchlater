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

package com.lambdasoup.watchlater.test;

import android.test.ActivityInstrumentationTestCase2;

import com.lambdasoup.watchlater.ui.LauncherActivity;

import cucumber.api.CucumberOptions;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.When;

@CucumberOptions(
		features = "features",
		glue = "com.lambdasoup.watchlater.test"
)
public class LauncherActivitySteps extends ActivityInstrumentationTestCase2<LauncherActivity> {


	public LauncherActivitySteps() {
		super(LauncherActivity.class);
	}

	@Given("My devicse has YouTube as default")
	public void device_has_youtube_default() {
		fail();
	}

	@When("I open the launcher")
	public void open_the_launcher() {

	}

	@When("I see the text")
	public void text_displayed() {

	}

}
