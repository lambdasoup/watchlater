Feature: Add Video
  The Add Activity gives the user a preview to the video and lets them choose whether to add it to
  a playlist and/or watch it right away in the official YouTube app.
  Playlist and Google account selection is also done here.

  Scenario: I want to watch now
    Given the Google account is set
    And a playlist has been selected
    When I open Watch Later via a YouTube URL
    And the user clicks on 'Watch now'
    Then the YouTube app is opened with the video URL

  Scenario: I want to see the video info
    Given the Google account is set
    When I open Watch Later via a YouTube URL
    Then I see the video info
