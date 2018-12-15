@regressionTest @configuration @timeZone
Feature: CM User Time Zone

  Scenario Outline: Change CM user's time zone
    When I sign in to CM as admin user
    And I open Change Time Zone dialog
    Then I see that browser time zone is selected
    When I set <customTimeZone> time zone
    And I save changes in Change Time Zone dialog
    And I sign out
    And I sign in to CM as admin user
    And I open Change Time Zone dialog
    Then I see that custom time zone <customTimeZone> is selected
    When I choose browser time zone
    And I save changes in Change Time Zone dialog
    And I open Change Time Zone dialog
    Then I see that browser time zone is selected

    Examples:
      | customTimeZone                         |
      | (UTC+00:00) Coordinated Universal Time |