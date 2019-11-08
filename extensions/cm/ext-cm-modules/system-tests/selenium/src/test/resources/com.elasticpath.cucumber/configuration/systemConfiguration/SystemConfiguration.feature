@regressionTest @configuration @systemConfiguration
Feature: Edit System Configuration

  Background:
    Given I sign in to CM as admin user
    And I go to Configuration

  Scenario Outline: Add/Remove System Defined Value
    When I enter setting name <SYSTEM_SETTING> in filter
    And I should see 0 Defined Values records
    When I add new defined values record for system setting with following data
      | setting | <SYSTEM_SETTING> |
      | context | null             |
      | value   | <value>          |
    Then I should see 1 Defined Values records
    When I remove defined values record for system setting with following data
      | setting | <SYSTEM_SETTING> |
      | context | null             |
      | value   | <value>          |
    Then I should see 0 Defined Values records

    Examples:
      | SYSTEM_SETTING                            | value |
      | COMMERCE/SYSTEM/SESSIONCLEANUP/maxHistory | 61    |

  Scenario: Rebuild Search Index
    When I go to Search Indexes
    And Index CM Users should have status: Complete
    When I rebuild CM Users index
    Then Index CM Users should have status: Complete