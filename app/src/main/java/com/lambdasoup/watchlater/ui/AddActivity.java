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

package com.lambdasoup.watchlater.ui;

import android.Manifest;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.lambdasoup.watchlater.R;
import com.lambdasoup.watchlater.viewmodel.AddViewModel;

import static android.net.Uri.parse;
import static android.widget.Toast.LENGTH_SHORT;


public class AddActivity extends WatchLaterActivity implements ActionView.ActionListener {

	private static final int                        PERMISSIONS_REQUEST_GET_ACCOUNTS = 100;
	private static final int                        REQUEST_ACCOUNT                  = 1;
	private static final String                     ACCOUNT_TYPE_GOOGLE              = "com.google";

	private ActionView actionView;
	private PermissionsView permissionsView;
	private AddViewModel viewModel;
	private VideoView    videoView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_add);
		Toolbar toolbar = findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		permissionsView = findViewById(R.id.add_permissions);
		permissionsView.setListener(this::tryAcquireAccountsPermission);

		actionView = findViewById(R.id.add_action);
		actionView.setListener(this);

		videoView = findViewById(R.id.add_video);

		viewModel = getViewModel(AddViewModel.class);
		viewModel.getVideoAdd().observe(this, this::onAddStatusChanged);

		ResultView resultView = findViewById(R.id.add_result);
		viewModel.getVideoAdd().observe(this, resultView);

		AccountView accountView = findViewById(R.id.add_account);
		viewModel.getAccount().observe(this, accountView);
		accountView.setListener(this::askForAccount);

		viewModel.setVideoUri(getIntent().getData());
		viewModel.getVideoInfo().observe(this, info -> videoView.setVideoInfo(info));

		viewModel.getPermissionNeeded().observe(this, this::onPermissionNeededChanged);
	}

	private void onPermissionNeededChanged(Boolean permissionNeeded) {
		if (permissionNeeded == null || !permissionNeeded) {
			permissionsView.setVisibility(View.GONE);
		} else {
			permissionsView.setVisibility(View.VISIBLE);
		}
	}

	private void onAddStatusChanged(AddViewModel.VideoAdd videoAdd) {
		if (videoAdd == null) {
			throw new IllegalArgumentException("add status should never be null");
		}

		actionView.setState(videoAdd.state);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
			case REQUEST_ACCOUNT:
				onRequestAccountResult(resultCode, data);
				return;
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	private void onRequestAccountResult(int resultCode, Intent data) {
		switch (resultCode) {
			case Activity.RESULT_OK:
				String name = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
				String type = data.getStringExtra(AccountManager.KEY_ACCOUNT_TYPE);
				viewModel.setAccount(new Account(name, type));
		}
	}

	@Override
	protected void onResume() {
		super.onResume();

		boolean needsPermission = needsPermission();
		viewModel.setPermissionNeeded(needsPermission);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu_add, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
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
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	private void askForAccount() {
		Intent intent = newChooseAccountIntent();
		startActivityForResult(intent, REQUEST_ACCOUNT);
	}

	@TargetApi(23)
	private void tryAcquireAccountsPermission() {
		requestPermissions(new String[]{Manifest.permission.GET_ACCOUNTS}, PERMISSIONS_REQUEST_GET_ACCOUNTS);
	}

	private boolean needsPermission() {
		// below 23 permissions are granted at install time
		if (Build.VERSION.SDK_INT < 23) {
			return false;
		}

		// below 26 we need GET_ACCOUNTS
		if (Build.VERSION.SDK_INT < 26) {
			boolean supportsRuntimePermissions = Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1;
			return supportsRuntimePermissions && !hasAccountsPermission();
		}

		// starting with O we don't need any permissions at runtime
		return false;
	}

	@TargetApi(23)
	private boolean hasAccountsPermission() {
		return getApplication().checkSelfPermission(Manifest.permission.GET_ACCOUNTS)
				== PackageManager.PERMISSION_GRANTED;
	}

	@SuppressWarnings("deprecation")
	private Intent newChooseAccountIntent() {
		String[] types = {ACCOUNT_TYPE_GOOGLE};
		String title = getString(R.string.choose_account);

		if (Build.VERSION.SDK_INT < 26) {
			return AccountManager.newChooseAccountIntent(null, null,
					types, false, title, null,
					null, null);
		}

		return AccountManager.newChooseAccountIntent(null, null,
				types, title, null, null,
				null);
	}

	private void openWithYoutube() {
		try {
			Intent intent = new Intent()
					.setData(getIntent().getData())
					.setPackage("com.google.android.youtube");
			startActivity(intent);
			finish();
		} catch (ActivityNotFoundException e) {
			Toast.makeText(this, R.string.error_youtube_player_missing, LENGTH_SHORT).show();
		}
	}

	@Override
	public void onWatchNowClicked() {
		openWithYoutube();
	}

	@Override
	public void onWatchLaterClicked() {
		viewModel.watchLater();
	}

}
