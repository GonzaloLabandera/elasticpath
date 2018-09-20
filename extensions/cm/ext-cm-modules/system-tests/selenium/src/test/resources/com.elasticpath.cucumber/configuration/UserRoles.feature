@smoketest @configuration @user
Feature: User Roles

  Scenario: Verify user role exists
    Given I sign in to CM as admin user
    And I go to Configuration
    And I go to User Roles
    Then User role Super User exists
