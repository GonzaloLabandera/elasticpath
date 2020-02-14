@regressionTest @configuration @store
Feature: Store

  Background:
    Given I sign in to CM as admin user
    When I go to Configuration
    And I go to Stores
    And I create store with following values
      | timezone              | GMT -8:00 Pacific Standard Time    |
      | store country         | United States                      |
      | store sub country     | California                         |
      | payment configuration | Address Required Happy Path Config |
      | store name            | TestStore-                         |
      | warehouse             | Generic Warehouse A                |
      | catalog               | Master Catalog A                   |
      | language              | English (United States)            |
      | currency              | USD                                |
    And the store should exist in the list

  # The following scenarios do not follow the best practice of having well-defined and isolated Given-When-Then conditions.  However, because
  # creating a store is very expensive it was deemed more efficient to keep the number of scenarios small.
  Scenario: Add and edit a store
    When I edit the store name to testStoreNameChanged
    Then the store should exist in the list
    When I add new language Arabic to the store
    Then I should see the new language in the selected languages list
    When I change the store state to Open
    Then the store list should show Open state
    When I change the store state to Restricted Access
    Then the store list should show Restricted Access state
    When I change the store state to Open
    Then the store list should show Open state

  @regressionTest @configuration @store
  Scenario: Profile Attribute Policy
    When I edit the newly created store
    And I view the store profile attribute policies list
    Then the store profile attribute list should not have a policy for Company
    When I view the store profile attribute policies list
    And I add the store profile attribute Company with a policy of DEFAULT
    Then the store profile attribute list should have a policy for attribute Company with a policy of DEFAULT
    When I edit the store profile attribute Company and change the policy to READ_ONLY
    Then the store profile attribute list should have a policy for attribute Company with a policy of READ_ONLY
    When I remove the store profile attribute Company
    Then the store profile attribute list should not have a policy for Company
