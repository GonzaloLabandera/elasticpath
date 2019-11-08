@regressionTest @configuration @facets
Feature: Facet Store Creation

  Scenario: Cannot configure facets during store creation
    Given I sign in to CM as admin user
    When I go to Configuration
    And I go to Stores
    And I click create store button
    And I select Facets tab in the Store Editor
    Then I should see Cannot Configure Facets dialog