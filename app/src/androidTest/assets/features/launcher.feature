Feature: Launcher
    Intro the User to blah

    Scenario: Tell the user to change YouTube settings
        Given My device has YouTube as default
        When I open the launcher
        Then I see the text
