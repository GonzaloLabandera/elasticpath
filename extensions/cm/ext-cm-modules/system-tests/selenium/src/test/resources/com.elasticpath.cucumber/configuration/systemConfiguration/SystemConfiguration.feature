@regressionTest @configuration @systemConfiguration
  Feature: Edit System Configuration

    Scenario Outline: Enabling Change Sets
      Given I sign in to CM as admin user
      And I go to Configuration
      And I enter setting name <SYSTEM_SETTING> in filter
      And I should see 0 Defined Values records
      When I add new defined values record for system setting with following data
        | setting | <SYSTEM_SETTING> |
        | context | null             |
        | value   | true             |
      Then I should see 1 Defined Values records
      When I remove defined values record for system setting with following data
        | setting | <SYSTEM_SETTING> |
        | context | null             |
        | value   | true             |
      Then I should see 0 Defined Values records

      Examples:
        | SYSTEM_SETTING                    |
        | COMMERCE/SYSTEM/CHANGESETS/enable |

    Scenario: Rebuild Search Index
      Given I sign in to CM as admin user
      And I go to Configuration
      And I go to Search Indexes
      And Index CM Users should have status: Complete
      When I rebuild CM Users index
      Then Index CM Users should have status: Complete