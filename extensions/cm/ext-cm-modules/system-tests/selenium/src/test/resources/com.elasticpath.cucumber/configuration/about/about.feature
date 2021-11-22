@regressionTest @configuration @about
Feature: CM User About

  Scenario: Verify About dialog
    When I sign in to CM as admin user
    And I open About dialog
    Then the About dialog is displayed
    And I can close the About dialog
