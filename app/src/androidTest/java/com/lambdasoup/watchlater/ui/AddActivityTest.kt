/*
 * Copyright (c) 2015 - 2021
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
package com.lambdasoup.watchlater.ui

import android.accounts.Account
import android.accounts.AccountManager
import android.app.Activity
import android.app.Instrumentation.ActivityResult
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.MutableLiveData
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents.*
import androidx.test.espresso.intent.matcher.IntentMatchers
import androidx.test.espresso.intent.matcher.IntentMatchers.hasData
import androidx.test.espresso.intent.matcher.IntentMatchers.toPackage
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.lambdasoup.watchlater.R
import com.lambdasoup.watchlater.data.YoutubeRepository
import com.lambdasoup.watchlater.data.YoutubeRepository.Videos
import com.lambdasoup.watchlater.data.YoutubeRepository.Videos.Item.ContentDetails
import com.lambdasoup.watchlater.data.YoutubeRepository.Videos.Item.Snippet.Thumbnails.Thumbnail
import com.lambdasoup.watchlater.viewmodel.AddViewModel
import com.lambdasoup.watchlater.viewmodel.AddViewModel.VideoAdd
import com.lambdasoup.watchlater.viewmodel.AddViewModel.VideoInfo
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.hamcrest.Matchers
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers
import org.mockito.MockitoAnnotations.initMocks

@RunWith(AndroidJUnit4::class)
@LargeTest
class AddActivityTest : WatchLaterActivityTest() {

    private val mockViewModel: AddViewModel = mock()
    private lateinit var accountLiveData: MutableLiveData<Account>
    private lateinit var videoInfoLiveData: MutableLiveData<VideoInfo>
    private lateinit var permissionNeededLiveData: MutableLiveData<Boolean>
    private lateinit var videoAddLiveData: MutableLiveData<VideoAdd>
    private lateinit var intent: Intent

    @Before
    fun setup() {
        initMocks(this)
        accountLiveData = MutableLiveData()
        videoInfoLiveData = MutableLiveData()
        permissionNeededLiveData = MutableLiveData()
        videoAddLiveData = MutableLiveData()
        whenever(mockViewModel.account).thenReturn(accountLiveData)
        whenever(mockViewModel.getPermissionNeeded()).thenReturn(permissionNeededLiveData)
        whenever(mockViewModel.getVideoAdd()).thenReturn(videoAddLiveData)
        whenever(mockViewModel.getVideoInfo()).thenReturn(videoInfoLiveData)
        setViewModel(mockViewModel)
        intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/watch?v=tntOCGkgt98"))
        init()
        val scenario = ActivityScenario.launch<LauncherActivity>(intent)
        scenario.moveToState(Lifecycle.State.RESUMED)
    }

    @After
    fun teardown() {
        release()
    }

    @Test
    fun should_set_uri() {
        verify(mockViewModel).setVideoUri(eq(Uri.parse("https://www.youtube.com/watch?v=tntOCGkgt98")))
    }

    @Test
    fun should_show_permission_needed_box() {
        permissionNeededLiveData.postValue(true)
        onView(withId(R.id.view_permissions_grant)).check(matches(isDisplayed()))
        onView(withId(R.id.permission_title)).check(matches(withText(R.string.add_permissions_description)))
        onView(withId(R.id.permission_information)).check(matches(withText(R.string.add_permissions_rationale)))
    }

    @Test
    fun should_not_show_permission_needed_box_after_clicking_grant_button() {
        permissionNeededLiveData.postValue(true)
        onView(withId(R.id.add_permissions)).perform(ViewActions.click())
        permissionNeededLiveData.postValue(false)
        onView(withId(R.id.add_permissions)).check(matches(Matchers.not(isDisplayed())))
    }

    @Test
    fun should_show_account_not_selected_box() {
        accountLiveData.postValue(null)
        onView(withId(R.id.view_account_set)).check(matches(isDisplayed()))
        onView(withId(R.id.view_account_label)).check(matches(withText(R.string.account_empty)))
    }

    @Test
    fun should_show_progress_circle_while_getting_video_information() {
        val videoInfo = VideoInfo.Progress
        videoInfoLiveData.postValue(videoInfo)
        onView(withId(R.id.video_progress)).check(matches(isDisplayed()))
    }

    @Test
    fun should_show_video_preview() {
        val givenThumbmailUrl = "http://sdfsdfsdfsdfsdfsdfsdfsdfdawad.de"
        val givenDuration = "PT00H00M59S"
        val expectedDescription = "ⓐ ⓑ ⓒ ⓓ ⓔ ⓕ ⓖ ⓗ ⓘ ⓙ ⓚ ⓛ ⓜ ⓝ ⓞ ⓟ ⓠ ⓡ ⓢ ⓣ ⓤ ⓥ ⓦ ⓧ ⓨ ⓩ ◕‿◕ ☮✖ ✗ ✘ ♬ ♪ ♩ ♫ ♪ ☼ ✄ ✂ ✆ ✉ ✦ ✧ ♱ ♰ ♂ ♀ ☿ ❤ ❥ ❦ ❧ ™ ® © ♡ ♦ ♢ ★ ☆ ✮ ✯ ☄ ☾ ☽ ☼ ☀ ☁ ☂ ☻ ☺ ☹ εїз ஐ ☎ ☏ ✌ ☢ ☣ ☠ ☮☯ ♠ ♤ ♣ ♧ ♥ ▲▼►◄ ʊ ϟ ღ ツ ™ ¿ ¡ ❝ ❞ ✿ ღ • * α в c ∂ ε f g н ι נ к ℓ м η σ ρ q я s т υ v ω x y z άв ς đ έ f ģ ħ ί ј ķ Ļ м ή ό ρ q ŕ ş ţ ù ν ώ x ч ž Å Á Ä Ã å á ä ã æ ß b c © Ð d é ê f ƒ g h ï î j k £ m Ñ ñ η ô õ ø Þ þ p q ® § š t ú û µ v w x ý ÿ z ž ❤ ♡ ❤ ღ ★ ☆ ✰ ¿ ؟ ﹖‼ ¡ ♩ ♪ ♫ ♬ © ® ™ ☮ ☯ ☢ ✔ ➝ ✚ ✞ ☨ ╬ ⁰ ¹ ² ³ ⁴ ⁵ ⁶ ⁷ ⁸ ⁹ ① ② ③ ④ ⑤ ⑥ ⑦ ⑧ ⑨ ⑩ ლ❛◡ (●*∩_∩*●)(*^ -^*) (-''-)(¬¬)(◡‿◡✿) (◕‿◕✿)(✖╭╮✖)(≧◡≦)(✿◠‿◠) (◑‿◐)(◕‿-)✖‿✖(╥╥)ಠಠ(╯╰) (╯3╰)(╯▽╰) 凸(¬‿¬)凸┌∩┐ (◣◢)┌∩┐(⊙▂⊙)(∪ ◡ ∪)(≧ω≦)o(≧o≦)o(⋋▂⋌) (•̪●)(॓॔) (╯ಊ╰)(─‿‿─)(⊙◎)d(--)bd|--|b‹(•¿•)›(╯5╰,)\u00AD(∩5∩)(69:)(a◕‿◕a)(^o▽)^o(~▽)~ (6?6)(6ω6)(◑O◐)(∩▂∩)(¬▂¬)(a❤‿❤a)(✿ ♥‿♥)(a♥‿♥a)(^o❤‿❤)^o٩(͡๏̯͡๏)۶ ٩(-̮̮̃•̃) ۶٩(̾●̮̮̃ ̾•̃̾)۶٩(-̮̮̃- ̃)۶ ¯\\(©¿©) /¯♥╣[--]╠♥◤(¬‿¬)◥"
        val expectedTitle = "WORLDS LONGEST YOUTUBE TITLE VIDEO WORLDS LONGEST YOUTUBE TITLE VIDEO WORLDS LONGEST YOUTUBE TITLE"
        val expectedDuration = "00h 00m 59s"
        val thumbnail = Thumbnail(givenThumbmailUrl)
        val thumbnails = Videos.Item.Snippet.Thumbnails(thumbnail)
        val snippet = Videos.Item.Snippet(expectedTitle, expectedDescription, thumbnails)
        val contentDetails = ContentDetails(givenDuration)
        val item = Videos.Item("123456789", snippet, contentDetails)
        val videoInfo = VideoInfo.Loaded(item)
        videoInfoLiveData.postValue(videoInfo)
        onView(withId(R.id.title)).check(matches(withText(expectedTitle)))
        onView(withId(R.id.description)).check(matches(withText(expectedDescription)))
        onView(withId(R.id.duration)).check(matches(withText(expectedDuration)))
    }

    @Test
    fun should_show_network_error() {
        val videoInfo = VideoInfo.Error(YoutubeRepository.ErrorType.Network)
        videoInfoLiveData.postValue(videoInfo)
        onView(withId(R.id.reason_title)).check(matches(withText(R.string.video_error_title)))
        onView(withId(R.id.reason)).check(matches(withText(R.string.error_network)))
    }

    @Test
    fun should_show_video_not_found_error() {
        val videoInfo = VideoInfo.Error(YoutubeRepository.ErrorType.VideoNotFound)
        videoInfoLiveData.postValue(videoInfo)
        onView(withId(R.id.reason_title)).check(matches(withText(R.string.video_error_title)))
        onView(withId(R.id.reason)).check(matches(withText(R.string.error_video_not_found)))
    }

    @Test
    fun should_show_default_error() {
        val videoInfo = VideoInfo.Error(YoutubeRepository.ErrorType.AlreadyInPlaylist)
        videoInfoLiveData.postValue(videoInfo)
        onView(withId(R.id.reason_title)).check(matches(withText(R.string.video_error_title)))
        val expectedText = getString(R.string.could_not_load, YoutubeRepository.ErrorType.AlreadyInPlaylist.toString())
        onView(withId(R.id.reason)).check(matches(withText(expectedText)))
    }

    @Test
    fun should_show_watch_buttons_on_default_error() {
        val videoInfo = VideoInfo.Error(YoutubeRepository.ErrorType.Other)
        videoInfoLiveData.postValue(videoInfo)
        onView(withId(R.id.action_watchnow)).check(matches(isDisplayed()))
        onView(withId(R.id.action_watchlater)).check(matches(isDisplayed()))
    }

    @Test
    fun should_add_video() {
        onView(withId(R.id.action_watchlater)).perform(ViewActions.click())
        verify(mockViewModel).watchLater()
    }

    @Test
    fun should_open_youtube_app() {
        val resultData = Intent()
        val result = ActivityResult(Activity.RESULT_OK, resultData)
        intending(toPackage("com.google.android.youtube")).respondWith(result)
        onView(withId(R.id.action_watchnow)).perform(ViewActions.click())
        intended(Matchers.allOf(
                hasData(intent.data),
                toPackage("com.google.android.youtube")))
    }

    @Test
    fun should_show_add_video_result_error_already_in_playlist() {
        val result = VideoAdd.Error(VideoAdd.ErrorType.YoutubeAlreadyInPlaylist)
        videoAddLiveData.postValue(result)
        val expectedText = getString(R.string.could_not_add, getString(R.string.error_already_in_playlist))
        onView(withId(R.id.add_result)).check(matches(withText(expectedText)))
    }

    @Test
    fun should_show_add_video_result_error_no_account() {
        val result = VideoAdd.Error(VideoAdd.ErrorType.NoAccount)
        videoAddLiveData.postValue(result)
        val expectedText = getString(R.string.could_not_add, getString(R.string.error_no_account))
        onView(withId(R.id.add_result)).check(matches(withText(expectedText)))
    }

    @Test
    fun should_show_add_video_result_error_no_permission() {
        val result = VideoAdd.Error(VideoAdd.ErrorType.NoPermission)
        videoAddLiveData.postValue(result)
        val expectedText = getString(R.string.could_not_add, getString(R.string.error_no_permission))
        onView(withId(R.id.add_result)).check(matches(withText(expectedText)))
    }

    @Test
    fun should_show_add_video_result_error_default() {
        val result = VideoAdd.Error(VideoAdd.ErrorType.Other)
        videoAddLiveData.postValue(result)
        val error = getString(R.string.error_general, VideoAdd.ErrorType.Other.toString())
        val expectedText = getString(R.string.could_not_add, error)
        onView(withId(R.id.add_result)).check(matches(withText(expectedText)))
    }

    @Test
    fun should_start_intent_when_add_result_intent() {
        val intent = Intent("android.content.pm.action.REQUEST_PERMISSIONS")
        intent.setPackage("com.google.android.packageinstaller")
        intending(IntentMatchers.hasAction(intent.action))
                .respondWith(ActivityResult(
                        Activity.RESULT_CANCELED,
                        null))
        val result = VideoAdd.HasIntent(intent)
        videoAddLiveData.postValue(result)
        val expectedText = getString(R.string.needs_youtube_permissions)
        onView(withId(R.id.add_result)).check(matches(withText(expectedText)))
        onView(withId(R.id.action_watchlater)).check(matches(isDisplayed()))
        intended(IntentMatchers.hasAction(intent.action))
    }

    @Test
    fun should_retry_video_add_after_youtube_permissions_ok() {
        val intent = Intent("android.content.pm.action.REQUEST_PERMISSIONS")
        intending(Matchers.equalTo(intent))
                .respondWith(ActivityResult(
                        Activity.RESULT_OK,
                        null))
        val result = VideoAdd.HasIntent(intent)
        videoAddLiveData.postValue(result)
        onView(withId(R.id.action_watchnow)).check(matches(isDisplayed()))
        verify(mockViewModel).watchLater()
    }

    @Test
    fun should_show_result_success() {
        val result = VideoAdd.Success
        videoAddLiveData.postValue(result)
        onView(withText(R.string.success_added_video)).check(matches(isDisplayed()))
    }

    @Test
    fun should_request_permissions() {
        permissionNeededLiveData.postValue(true)
        val resultData = Intent()
        val result = ActivityResult(Activity.RESULT_OK, resultData)
        intending(IntentMatchers.hasAction("android.content.pm.action.REQUEST_PERMISSIONS")).respondWith(result)
        onView(withText(R.string.add_permissions_grant)).perform(ViewActions.click())
        intended(IntentMatchers.hasAction("android.content.pm.action.REQUEST_PERMISSIONS"))
    }

    @Test
    fun should_choose_account() {
        val resultData = Intent()
        resultData.putExtra(AccountManager.KEY_ACCOUNT_NAME, "hans@example.com")
        resultData.putExtra(AccountManager.KEY_ACCOUNT_TYPE, "example_account")
        val result = ActivityResult(Activity.RESULT_OK, resultData)
        intending(toPackage("android")).respondWith(result)
        onView(withText(R.string.account_set)).perform(ViewActions.click())
        verify(mockViewModel).setAccount(ArgumentMatchers.eq(Account("hans@example.com", "example_account")))
    }
}
