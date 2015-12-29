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

package com.lambdasoup.watchlater;

import android.app.ActionBar;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceGroup;
import android.util.Log;
import android.view.MenuItem;

/**
 * Created by jl on 14.12.15.
 */
public class SettingsActivity extends PreferenceActivity {
	public static final String PREF_KEY_DEFAULT_ACCOUNT_NAME = "pref_key_default_account_name";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		getFragmentManager().beginTransaction()
				.replace(android.R.id.content, new SettingsFragment())
				.commit();

		ActionBar actionBar = getActionBar();
		if (actionBar != null) {
			actionBar.setDisplayHomeAsUpEnabled(true);
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
				actionBar.setHomeAsUpIndicator(R.drawable.ic_close_white_24dp);
			} else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
				actionBar.setDisplayShowHomeEnabled(false);
				actionBar.setHomeAsUpIndicator(R.drawable.ic_close_white_24dp_padded);
			}
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				finish();
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	public static class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {
		private static final String TAG = SettingsFragment.class.getSimpleName();

		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);

			addPreferencesFromResource(R.xml.preferences);
			getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);

			findPreference(PREF_KEY_DEFAULT_ACCOUNT_NAME).setOnPreferenceClickListener(preference -> {
						getPreferenceManager().getSharedPreferences().edit().remove(PREF_KEY_DEFAULT_ACCOUNT_NAME).apply();
						return true;
					}
			);


		}

		@Override
		public void onResume() {
			super.onResume();

			updatePreference(getPreferenceManager().getSharedPreferences(), getPreferenceScreen());
		}

		@Override
		public void onDestroy() {
			super.onDestroy();
			getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
		}

		@Override
		public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
			updatePreference(sharedPreferences, findPreference(key));
		}

		private void updatePreference(SharedPreferences sharedPreferences, PreferenceGroup preferenceGroup) {
			for (int i = 0; i < preferenceGroup.getPreferenceCount(); i++) {
				updatePreference(sharedPreferences, preferenceGroup.getPreference(i));
			}
		}

		private void updatePreference(SharedPreferences sharedPreferences, Preference preference) {
			if (preference instanceof PreferenceGroup) {
				updatePreference(sharedPreferences, (PreferenceGroup) preference);
				return;
			}
			switch (preference.getKey()) {
				case PREF_KEY_DEFAULT_ACCOUNT_NAME:
					String defaultAccount = sharedPreferences.getString(PREF_KEY_DEFAULT_ACCOUNT_NAME, null);
					if (defaultAccount == null) {
						preference.setSummary(R.string.pref_default_account_name_no_default);
						preference.setEnabled(false);
					} else {
						preference.setSummary(getResources().getString(R.string.pref_default_account_name_summary, defaultAccount));
						preference.setEnabled(true);
					}
					break;
				default:
					Log.d(TAG, "No update action specified for preference with key " + preference.getKey());
			}
		}
	}
}
