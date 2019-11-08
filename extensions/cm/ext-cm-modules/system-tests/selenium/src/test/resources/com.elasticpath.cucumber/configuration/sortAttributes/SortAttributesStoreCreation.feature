@regressionTest @configuration @sortAttributes
Feature: Sort Attributes Store Creation

  Scenario: Cannot configure sort attributes during store creation
    Given I sign in to CM as admin user
    When I go to Configuration
    And I go to Stores
    And I click create store button
    And I select Sorting tab in the Store Editor
    Then I should see Cannot Configure Sort Attributes dialog