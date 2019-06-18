/*
 * Copyright (c) 2015 - 2019
 *
 * Maximilian Hille <mh@lambdasoup.com>
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

package com.lambdasoup.watchlater.ui;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;

import com.lambdasoup.watchlater.BuildConfig;
import com.lambdasoup.watchlater.R;
import com.lambdasoup.watchlater.data.IntentResolverRepository;
import com.lambdasoup.watchlater.viewmodel.LauncherViewModel;

import static android.net.Uri.parse;

public class LauncherActivity extends WatchLaterActivity {

	private static final Uri    EXAMPLE_URI             = parse("https://www.youtube.com/watch?v=dGFSjKuJfrI");
	public static final  Intent EXAMPLE_INTENT          = new Intent(Intent.ACTION_VIEW, EXAMPLE_URI);
	private static final Intent INTENT_YOUTUBE_APP      = new Intent().setData(EXAMPLE_URI).setPackage("com.google.android.youtube");
	private static final Intent INTENT_YOUTUBE_SETTINGS = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:com.google.android.youtube"));

	private LauncherViewModel viewModel;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_launcher);

		Toolbar toolbar = findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		viewModel = getViewModel(LauncherViewModel.class);
		viewModel.getResolverState().observe(this, this::onResolverStateChanged);

		findViewById(R.id.launcher_youtube_button).setOnClickListener(v -> openYoutubeSettings());
		findViewById(R.id.launcher_example_button).setOnClickListener(v -> openExampleVideo());
	}

	private void onResolverStateChanged(IntentResolverRepository.ResolverState resolverState) {
		View view = findViewById(R.id.launcher_youtube_action);
		switch (resolverState) {
			case OK:
				view.setVisibility(View.GONE);
				break;
			case YOUTUBE_ONLY:
				view.setVisibility(View.VISIBLE);
				break;
		}
	}

	@Override
	protected void onResume() {
		super.onResume();

		viewModel.update();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu_add, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(@NonNull MenuItem item) {
		switch (item.getItemId()) {
			case R.id.menu_about:
				startActivity(new Intent(this, AboutActivity.class));
				return true;
			case R.id.menu_help:
				startActivity(new Intent(this, HelpActivity.class));
				return true;
			case R.id.menu_privacy:
				startActivity(new Intent(Intent.ACTION_VIEW, parse("https://lambdasoup.com/privacypolicy-watchlater/")));
				return true;
			case R.id.menu_store:
				startActivity(new Intent(Intent.ACTION_VIEW, parse("https://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID)));
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	private void openExampleVideo() {
		startActivity(EXAMPLE_INTENT);
	}

	private void openYoutubeSettings() {
		// check if youtube is installed
		if (getPackageManager().resolveActivity(INTENT_YOUTUBE_APP, PackageManager.MATCH_DEFAULT_ONLY) == null) {
			Toast.makeText(this, R.string.no_youtube_installed, Toast.LENGTH_SHORT).show();
			return;
		}
		if (getPackageManager().resolveActivity(INTENT_YOUTUBE_SETTINGS, PackageManager.MATCH_DEFAULT_ONLY) == null) {
			Toast.makeText(this, R.string.no_youtube_settings_activity, Toast.LENGTH_SHORT).show();
			return;
		}
		startActivity(INTENT_YOUTUBE_SETTINGS);
	}

}
