Feature: Launcher
    Intro the User to blah

    @launcher @defaultapps
    Scenario Outline: Tell the user to change YouTube settings
        Given My device has YouTube as default
        When I open the launcher
        Then I see the text