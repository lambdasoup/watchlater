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

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.Instrumentation;
import android.arch.lifecycle.MutableLiveData;
import android.content.Intent;
import android.net.Uri;
import android.support.test.espresso.intent.rule.IntentsTestRule;
import android.support.test.filters.LargeTest;
import android.support.test.runner.AndroidJUnit4;

import com.lambdasoup.watchlater.R;
import com.lambdasoup.watchlater.data.YoutubeRepository;
import com.lambdasoup.watchlater.viewmodel.AddViewModel;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.intent.Intents.intended;
import static android.support.test.espresso.intent.Intents.intending;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasAction;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasData;
import static android.support.test.espresso.intent.matcher.IntentMatchers.toPackage;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class AddActivityTest extends WatchLaterActivityTest {

	@Rule
	public final IntentsTestRule<AddActivity> rule = new IntentsTestRule<>(AddActivity.class, false, false);

	private AddViewModel mockViewModel;
	private MutableLiveData<Account> accountLiveData;
	private MutableLiveData<AddViewModel.VideoInfo> videoInfoLiveData;
	private MutableLiveData<Boolean> permissionNeededLiveData;
	private MutableLiveData<AddViewModel.VideoAdd> videoAddLiveData;
	private Intent intent;

	@Before
	public void setup() {
		accountLiveData = new MutableLiveData<>();
		videoInfoLiveData = new MutableLiveData<>();
		permissionNeededLiveData = new MutableLiveData<>();
		videoAddLiveData = new MutableLiveData<>();

		mockViewModel = mock(AddViewModel.class);
		when(mockViewModel.getAccount()).thenReturn(accountLiveData);
		when(mockViewModel.getPermissionNeeded()).thenReturn(permissionNeededLiveData);
		when(mockViewModel.getVideoAdd()).thenReturn(videoAddLiveData);
		when(mockViewModel.getVideoInfo()).thenReturn(videoInfoLiveData);

		setViewModel(mockViewModel);

		intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/watch?v=tntOCGkgt98"));
		rule.launchActivity(intent);
	}

	@After
	public void teardown() {
		rule.finishActivity();
	}

	@Test
	public void should_set_uri() {
		verify(mockViewModel).setVideoUri(eq(Uri.parse("https://www.youtube.com/watch?v=tntOCGkgt98")));
	}

	@Test
	public void should_show_permission_needed_box() {
		permissionNeededLiveData.postValue(true);

		onView(withId(R.id.view_permissions_grant)).check(matches(isDisplayed()));
		onView(withId(R.id.permission_title)).check(matches(withText(rule.getActivity().getString(R.string.add_permissions_description))));
		onView(withId(R.id.permission_information)).check(matches(withText(rule.getActivity().getString(R.string.add_permissions_rationale))));
	}

	@Test
	public void should_not_show_permission_needed_box_after_clicking_grant_button() {
		permissionNeededLiveData.postValue(true);
		onView(withId(R.id.add_permissions)).perform(click());

		permissionNeededLiveData.postValue(false);
		onView(withId(R.id.add_permissions)).check(matches(not(isDisplayed())));
	}

	@Test
	public void should_show_account_not_selected_box() {
		accountLiveData.postValue(null);

		onView(withId(R.id.view_account_set)).check(matches(isDisplayed()));
		onView(withId(R.id.view_account_label)).check(matches(withText(rule.getActivity().getString(R.string.account_empty))));
	}

	@Test
	public void should_show_progress_circle_while_getting_video_information() {
		AddViewModel.VideoInfo videoInfo = new AddViewModel.VideoInfo(AddViewModel.VideoInfo.State.PROGRESS, null, null);
		videoInfoLiveData.postValue(videoInfo);

		onView(withId(R.id.video_progress)).check(matches(isDisplayed()));
	}

	@Test
	public void should_show_video_preview() {
		final String givenThumbmailUrl = "http://sdfsdfsdfsdfsdfsdfsdfsdfdawad.de";
		final String givenDuration = "PT00H00M59S";
		final String expectedDescription = "ⓐ ⓑ ⓒ ⓓ ⓔ ⓕ ⓖ ⓗ ⓘ ⓙ ⓚ ⓛ ⓜ ⓝ ⓞ ⓟ ⓠ ⓡ ⓢ ⓣ ⓤ ⓥ ⓦ ⓧ ⓨ ⓩ ◕‿◕ ☮✖ ✗ ✘ ♬ ♪ ♩ ♫ ♪ ☼ ✄ ✂ ✆ ✉ ✦ ✧ ♱ ♰ ♂ ♀ ☿ ❤ ❥ ❦ ❧ ™ ® © ♡ ♦ ♢ ★ ☆ ✮ ✯ ☄ ☾ ☽ ☼ ☀ ☁ ☂ ☻ ☺ ☹ εїз ஐ ☎ ☏ ✌ ☢ ☣ ☠ ☮☯ ♠ ♤ ♣ ♧ ♥ ▲▼►◄ ʊ ϟ ღ ツ ™ ¿ ¡ ❝ ❞ ✿ ღ • * α в c ∂ ε f g н ι נ к ℓ м η σ ρ q я s т υ v ω x y z άв ς đ έ f ģ ħ ί ј ķ Ļ м ή ό ρ q ŕ ş ţ ù ν ώ x ч ž Å Á Ä Ã å á ä ã æ ß b c © Ð d é ê f ƒ g h ï î j k £ m Ñ ñ η ô õ ø Þ þ p q ® § š t ú û µ v w x ý ÿ z ž ❤ ♡ ❤ ღ ★ ☆ ✰ ¿ ؟ ﹖‼ ¡ ♩ ♪ ♫ ♬ © ® ™ ☮ ☯ ☢ ✔ ➝ ✚ ✞ ☨ ╬ ⁰ ¹ ² ³ ⁴ ⁵ ⁶ ⁷ ⁸ ⁹ ① ② ③ ④ ⑤ ⑥ ⑦ ⑧ ⑨ ⑩ ლ❛◡ (●*∩_∩*●)(*^ -^*) (-''-)(¬¬)(◡‿◡✿) (◕‿◕✿)(✖╭╮✖)(≧◡≦)(✿◠‿◠) (◑‿◐)(◕‿-)✖‿✖(╥╥)ಠಠ(╯╰) (╯3╰)(╯▽╰) 凸(¬‿¬)凸┌∩┐ (◣◢)┌∩┐(⊙▂⊙)(∪ ◡ ∪)(≧ω≦)o(≧o≦)o(⋋▂⋌) (•̪●)(॓॔) (╯ಊ╰)(─‿‿─)(⊙◎)d(--)bd|--|b‹(•¿•)›(╯5╰,)\u00AD(∩5∩)(69:)(a◕‿◕a)(^o▽)^o(~▽)~ (6?6)(6ω6)(◑O◐)(∩▂∩)(¬▂¬)(a❤‿❤a)(✿ ♥‿♥)(a♥‿♥a)(^o❤‿❤)^o٩(͡๏̯͡๏)۶ ٩(-̮̮̃•̃) ۶٩(̾●̮̮̃ ̾•̃̾)۶٩(-̮̮̃- ̃)۶ ¯\\(©¿©) /¯♥╣[--]╠♥◤(¬‿¬)◥";
		final String expectedTitle = "WORLDS LONGEST YOUTUBE TITLE VIDEO WORLDS LONGEST YOUTUBE TITLE VIDEO WORLDS LONGEST YOUTUBE TITLE";
		final String expectedDuration = "00h 00m 59s";

		YoutubeRepository.Videos.Item.Snippet.Thumbnails.Thumbnail thumbnail = new YoutubeRepository.Videos.Item.Snippet.Thumbnails.Thumbnail(givenThumbmailUrl);
		YoutubeRepository.Videos.Item.Snippet.Thumbnails thumbnails = new YoutubeRepository.Videos.Item.Snippet.Thumbnails(thumbnail);
		YoutubeRepository.Videos.Item.Snippet snippet = new YoutubeRepository.Videos.Item.Snippet(expectedTitle, expectedDescription, thumbnails);
		YoutubeRepository.Videos.Item.ContentDetails contentDetails = new YoutubeRepository.Videos.Item.ContentDetails(givenDuration);

		YoutubeRepository.Videos.Item item = new YoutubeRepository.Videos.Item("123456789", snippet, contentDetails);
		AddViewModel.VideoInfo videoInfo = new AddViewModel.VideoInfo(AddViewModel.VideoInfo.State.LOADED, item, null);

		videoInfoLiveData.postValue(videoInfo);

		onView(withId(R.id.title)).check(matches(withText(expectedTitle)));
		onView(withId(R.id.description)).check(matches(withText(expectedDescription)));
		onView(withId(R.id.duration)).check(matches(withText(expectedDuration)));
	}

	@Test
	public void should_show_network_error() {
		AddViewModel.VideoInfo videoInfo = new AddViewModel.VideoInfo(AddViewModel.VideoInfo.State.ERROR, null, YoutubeRepository.ErrorType.NETWORK);
		videoInfoLiveData.postValue(videoInfo);

		onView(withId(R.id.reason_title)).check(matches(withText(rule.getActivity().getString(R.string.video_error_title))));
		onView(withId(R.id.reason)).check(matches(withText(rule.getActivity().getString(R.string.error_network))));
	}

	@Test
	public void should_show_video_not_found_error() {
		AddViewModel.VideoInfo videoInfo = new AddViewModel.VideoInfo(AddViewModel.VideoInfo.State.ERROR, null, YoutubeRepository.ErrorType.VIDEO_NOT_FOUND);
		videoInfoLiveData.postValue(videoInfo);

		onView(withId(R.id.reason_title)).check(matches(withText(rule.getActivity().getString(R.string.video_error_title))));
		onView(withId(R.id.reason)).check(matches(withText(rule.getActivity().getString(R.string.error_video_not_found))));
	}

	@Test
	public void should_show_default_error() {
		AddViewModel.VideoInfo videoInfo = new AddViewModel.VideoInfo(AddViewModel.VideoInfo.State.ERROR, null, YoutubeRepository.ErrorType.ALREADY_IN_PLAYLIST);
		videoInfoLiveData.postValue(videoInfo);

		onView(withId(R.id.reason_title)).check(matches(withText(rule.getActivity().getString(R.string.video_error_title))));
		String expectedText = rule.getActivity().getString(R.string.could_not_load, YoutubeRepository.ErrorType.ALREADY_IN_PLAYLIST.toString());
		onView(withId(R.id.reason)).check(matches(withText(expectedText)));
	}

	@Test
	public void should_show_watch_buttons_on_default_error() {
		AddViewModel.VideoInfo videoInfo = new AddViewModel.VideoInfo(AddViewModel.VideoInfo.State.ERROR, null, YoutubeRepository.ErrorType.OTHER);
		videoInfoLiveData.postValue(videoInfo);

		onView(withId(R.id.action_watchnow)).check(matches(isDisplayed()));
		onView(withId(R.id.action_watchlater)).check(matches(isDisplayed()));
	}

	@Test
	public void should_add_video() {
		onView(withId(R.id.action_watchlater)).perform(click());
		verify(mockViewModel).watchLater();
	}

	@Test
	public void should_open_youtube_app() {
		Intent resultData = new Intent();
		Instrumentation.ActivityResult result =
				new Instrumentation.ActivityResult(Activity.RESULT_OK, resultData);
		intending(toPackage("com.google.android.youtube")).respondWith(result);

		onView(withId(R.id.action_watchnow)).perform(click());

		intended(allOf(
				hasData(intent.getData()),
				toPackage("com.google.android.youtube")));
	}

	@Test
	public void should_show_add_video_result_error_already_in_playlist() {
		AddViewModel.VideoAdd result = AddViewModel.VideoAdd.ERROR(AddViewModel.VideoAdd.ErrorType.YOUTUBE_ALREADY_IN_PLAYLIST);
		videoAddLiveData.postValue(result);

		String expectedText = rule.getActivity().getString(R.string.could_not_add, rule.getActivity().getString(R.string.error_already_in_playlist));
		onView(withId(R.id.add_result)).check(matches(withText(expectedText)));
	}

	@Test
	public void should_show_add_video_result_error_no_account() {
		AddViewModel.VideoAdd result = AddViewModel.VideoAdd.ERROR(AddViewModel.VideoAdd.ErrorType.NO_ACCOUNT);
		videoAddLiveData.postValue(result);

		String expectedText = rule.getActivity().getString(R.string.could_not_add, rule.getActivity().getString(R.string.error_no_account));
		onView(withId(R.id.add_result)).check(matches(withText(expectedText)));
	}

	@Test
	public void should_show_add_video_result_error_no_permission() {
		AddViewModel.VideoAdd result = AddViewModel.VideoAdd.ERROR(AddViewModel.VideoAdd.ErrorType.NO_PERMISSION);
		videoAddLiveData.postValue(result);

		String expectedText = rule.getActivity().getString(R.string.could_not_add, rule.getActivity().getString(R.string.error_no_permission));
		onView(withId(R.id.add_result)).check(matches(withText(expectedText)));
	}

	@Test
	public void should_show_add_video_result_error_default() {
		AddViewModel.VideoAdd result = AddViewModel.VideoAdd.ERROR(AddViewModel.VideoAdd.ErrorType.OTHER);
		videoAddLiveData.postValue(result);

		String error = rule.getActivity().getString(R.string.error_general, AddViewModel.VideoAdd.ErrorType.OTHER.toString());
		String expectedText = rule.getActivity().getString(R.string.could_not_add, error);
		onView(withId(R.id.add_result)).check(matches(withText(expectedText)));
	}

	@Test
	public void should_show_result_success() {
		AddViewModel.VideoAdd result = AddViewModel.VideoAdd.SUCCESS();
		videoAddLiveData.postValue(result);

		onView(withText(R.string.success_added_video)).check(matches(isDisplayed()));
	}

	@Test
	public void should_request_permissions() {
		permissionNeededLiveData.postValue(true);

		Intent resultData = new Intent();
		Instrumentation.ActivityResult result =
				new Instrumentation.ActivityResult(Activity.RESULT_OK, resultData);
		intending(toPackage("com.android.packageinstaller")).respondWith(result);

		onView(withText(R.string.add_permissions_grant)).perform(click());

		intended(allOf(
				hasAction("android.content.pm.action.REQUEST_PERMISSIONS"),
				toPackage("com.android.packageinstaller")));
	}

	@Test
	public void should_choose_account() {
		Intent resultData = new Intent();
		resultData.putExtra(AccountManager.KEY_ACCOUNT_NAME, "hans@example.com");
		resultData.putExtra(AccountManager.KEY_ACCOUNT_TYPE, "example_account");
		Instrumentation.ActivityResult result =
				new Instrumentation.ActivityResult(Activity.RESULT_OK, resultData);
		intending(toPackage("android")).respondWith(result);

		onView(withText(R.string.account_set)).perform(click());

		verify(mockViewModel).setAccount(eq(new Account("hans@example.com", "example_account")));
	}
}
