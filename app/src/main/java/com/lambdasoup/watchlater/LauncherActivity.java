package com.lambdasoup.watchlater;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static android.net.Uri.parse;


public class LauncherActivity extends Activity {
    private final String TAG = getClass().getSimpleName();
    private static final Uri EXAMPLE_URI = parse("https://www.youtube.com/watch?v=tntOCGkgt98");
    private static final String ACTIVITY_RESOLVER = "com.android.internal.app.ResolverActivity";
    private static final String ACTIVITY_YOUTUBE = "com.google.android.youtube.UrlActivity";
    private static final String ACTIVITY_WATCHLATER = "com.lambdasoup.watchlater.AddActivity";

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
        ResolveInfo resolveInfo = getPackageManager().resolveActivity(getExampleIntent(), PackageManager.MATCH_DEFAULT_ONLY);
        if (resolveInfo == null) {
            // no matching activity found. User should make sure that youtube is installed, should make sure that both youtube and watchlater are allowed to open youtube links
            advice.setText(R.string.defaults_advice_adjust_both);
        } else {
            switch (resolveInfo.activityInfo.name) {
                case ACTIVITY_RESOLVER: {
                    // great, user gets chooser! Let's
                    List<ResolveInfo> activities = getPackageManager().queryIntentActivities(getExampleIntent(), PackageManager.MATCH_DEFAULT_ONLY);
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
                    }  else {
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

        findViewById(R.id.settings_watchlater_button).setVisibility(showWatchlaterSettings ? View.VISIBLE : View.GONE);
        findViewById(R.id.settings_youtube_button).setVisibility(showYoutubeSettings ? View.VISIBLE : View.GONE);
        findViewById(R.id.settings_all_apps_button).setVisibility(showAllAppSettings ? View.VISIBLE : View.GONE);
    }

    public void onExampleLink(@SuppressWarnings("UnusedParameters") View view) {
        startActivity(getExampleIntent());
    }

    public void onSettingsYoutube(@SuppressWarnings("UnusedParameters") View view) {
        // check if youtube is installed
        if (null != getPackageManager().resolveActivity(new Intent().setData(EXAMPLE_URI).setPackage("com.google.android.youtube"), PackageManager.MATCH_DEFAULT_ONLY)) {
            try {
                startActivity(new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:com.google.android.youtube")));
            } catch (ActivityNotFoundException e) {
                Toast.makeText(this, R.string.no_youtube_settings_activity, Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, R.string.no_youtube_installed, Toast.LENGTH_SHORT).show();
        }
    }

    public void onSettingsWatchlater(@SuppressWarnings("UnusedParameters") View view) {
        try {
            startActivity(new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:com.lambdasoup.watchlater")));
        } catch (ActivityNotFoundException e) {
            // No settings for this application which is definitely installed? Nothing to do here.
            Toast.makeText(this, R.string.no_watchlater_settings_activity, Toast.LENGTH_SHORT).show();
        }
    }

    public void onSettingsAllApps(@SuppressWarnings("UnusedParameters") View view) {
        try {
            startActivity(new Intent(Settings.ACTION_APPLICATION_SETTINGS));
        } catch (ActivityNotFoundException e) {
            // No settings ? Nothing to do here.
            Toast.makeText(this, R.string.no_app_settings_activity, Toast.LENGTH_SHORT).show();
        }
    }

    private static Intent getExampleIntent() {
        return new Intent(Intent.ACTION_VIEW, EXAMPLE_URI);
    }
}
