@regressionTest @configuration @store
Feature: Store

  Scenario Outline: Profile Attribute Policy contains user and account attributes
    Given I sign in to CM as admin user
    When I go to Configuration
    And I go to Stores
    When I edit the existing store MOBEE
    And I view the store profile attribute policies list
    Then the store profile attribute list should have a policy for attribute <USER_ATTRIBUTE> with a policy of DEFAULT
    And the store profile attribute list should have a policy for attribute <ACCOUNT_ATTRIBUTE> with a policy of READ_ONLY

    Examples:
      | USER_ATTRIBUTE                  | ACCOUNT_ATTRIBUTE                    |
      | Preferred Locale (User Profile) | readonly attribute (Account Profile) |