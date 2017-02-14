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

package com.lambdasoup.watchlater;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.IdRes;
import android.support.annotation.StringRes;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static android.net.Uri.parse;


public class LauncherActivity extends Activity {
    private static final Intent INTENT_ALL_APP_SETTINGS = new Intent(Settings.ACTION_APPLICATION_SETTINGS);
    private static final Intent INTENT_WATCHLATER_SETTINGS = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:com.lambdasoup.watchlater"));
    private static final Intent INTENT_YOUTUBE_SETTINGS = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:com.google.android.youtube"));
    private static final Uri EXAMPLE_URI = parse("https://www.youtube.com/watch?v=tntOCGkgt98");
    private static final Intent INTENT_YOUTUBE_APP = new Intent().setData(EXAMPLE_URI).setPackage("com.google.android.youtube");
    private static final String ACTIVITY_RESOLVER = "com.android.internal.app.ResolverActivity";
    private static final String ACTIVITY_YOUTUBE = "com.google.android.youtube.UrlActivity";
    private static final String ACTIVITY_WATCHLATER = "com.lambdasoup.watchlater.AddActivity";
    private static final Intent EXAMPLE_INTENT = new Intent(Intent.ACTION_VIEW, EXAMPLE_URI);
    private final String TAG = getClass().getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launcher);

        if (getFragmentManager().findFragmentByTag(MainActivityMenuFragment.TAG) == null) {
            getFragmentManager().beginTransaction().add(MainActivityMenuFragment.newInstance(), MainActivityMenuFragment.TAG).commit();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkAndSetDefaultsAdvice();
    }

    private void checkAndSetDefaultsAdvice() {
        boolean showYoutubeSettings = true;
        boolean showWatchlaterSettings = true;
        boolean showAllAppSettings = false;
        TextView advice = (TextView) findViewById(R.id.defaults_advice);
        ResolveInfo resolveInfo = getPackageManager().resolveActivity(EXAMPLE_INTENT, PackageManager.MATCH_DEFAULT_ONLY);
        if (resolveInfo == null) {
            // no matching activity found. User should make sure that youtube is installed, should make sure that both youtube and watchlater are allowed to open youtube links
            advice.setText(R.string.defaults_advice_adjust_both);
        } else {
            switch (resolveInfo.activityInfo.name) {
                case ACTIVITY_RESOLVER: {
                    // great, user gets chooser! Let's
                    List<ResolveInfo> activities = getPackageManager().queryIntentActivities(EXAMPLE_INTENT, PackageManager.MATCH_DEFAULT_ONLY);
                    // check that both youtube and watchlater occur
                    Set<String> activityNames = new HashSet<>();
                    for (ResolveInfo resInfo : activities) {
                        activityNames.add(resInfo.activityInfo.name);
                    }
                    if (activityNames.contains(ACTIVITY_WATCHLATER)) {
                        if (activityNames.contains(ACTIVITY_YOUTUBE)) {
                            advice.setText(R.string.defaults_advice_ok);
                            showWatchlaterSettings = false;
                            showYoutubeSettings = false;
                        } else {
                            advice.setText(R.string.defaults_advice_youtube_choice_missing);
                            showWatchlaterSettings = false;
                        }
                    } else {
                        if (activityNames.contains(ACTIVITY_YOUTUBE)) {
                            advice.setText(R.string.defaults_advice_watchlater_choice_missing);
                            showYoutubeSettings = false;
                        } else {
                            advice.setText(R.string.defaults_advice_both_choice_missing);
                        }
                    }
                    break;
                }
                case ACTIVITY_YOUTUBE: {
                    // youtube is set as default app to launch with, no chooser
                    advice.setText(R.string.defaults_advice_youtube_default);
                    showWatchlaterSettings = false;
                    break;
                }
                case ACTIVITY_WATCHLATER: {
                    // watchlater is set as default app to launch with, no chooser
                    advice.setText(R.string.defaults_advice_watchlater_default);
                    showYoutubeSettings = false;
                    break;
                }
                default: {
                    // some unknown app is set as the default app to launch with, without chooser.
                    advice.setText(getResources().getString(R.string.defaults_advice_other_app_default, resolveInfo.activityInfo.applicationInfo.name));
                    showAllAppSettings = true;
                }
            }
        }

        setVisibility(R.id.settings_watchlater_button, showWatchlaterSettings);
        setVisibility(R.id.settings_youtube_button, showYoutubeSettings);
        setVisibility(R.id.settings_all_apps_button, showAllAppSettings);
    }

    private void setVisibility(@IdRes int resId, boolean showWatchlaterSettings) {
        findViewById(resId).setVisibility(showWatchlaterSettings ? View.VISIBLE : View.GONE);
    }

    public void onClickExampleLink(@SuppressWarnings("UnusedParameters") View view) {
        startActivity(EXAMPLE_INTENT);
    }

    public void onClickSettingsYoutube(@SuppressWarnings("UnusedParameters") View view) {
        // check if youtube is installed
        if (getPackageManager().resolveActivity(INTENT_YOUTUBE_APP, PackageManager.MATCH_DEFAULT_ONLY) == null) {
            Toast.makeText(this, R.string.no_youtube_installed, Toast.LENGTH_SHORT).show();
            return;
        }
        startActivityOrToast(INTENT_YOUTUBE_SETTINGS, R.string.no_youtube_settings_activity);
    }


    public void onClickSettingsWatchlater(@SuppressWarnings("UnusedParameters") View view) {
        startActivityOrToast(INTENT_WATCHLATER_SETTINGS, R.string.no_watchlater_settings_activity);
    }

    public void onClickSettingsAllApps(@SuppressWarnings("UnusedParameters") View view) {
        startActivityOrToast(INTENT_ALL_APP_SETTINGS, R.string.no_app_settings_activity);
    }

    private void startActivityOrToast(Intent intent, @StringRes int errorResId) {
        if (getPackageManager().resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY) == null) {
            Toast.makeText(this, errorResId, Toast.LENGTH_SHORT).show();
            return;
        }
        startActivity(intent);
    }

}
