Feature: Launcher
  The launcher activity helps the user setup system settings. Also, the user can try out the app
  functionality.

  Scenario: Try demo link
    Given Watch Later is set as default
    And Watch Later is not missing any hostname verifications
    When I open the Launcher
    And click on the demo button
    Then the video is opened

  Scenario: Needs default
    Given Watch Later is not set as default
    And Watch Later is not missing any hostname verifications
    When I open the Launcher
    Then I see the setup instructions

  Scenario: Needs verification
    Given Watch Later is missing some hostname verifications
    When I open the Launcher
    Then I see the setup instructions

  Scenario: Setup complete
    Given Watch Later is set as default
    And Watch Later is not missing any hostname verifications
    When I open the Launcher
    Then I do not see the setup instructions
