@accounts
Feature: Buyer Finance needs Accounts transactions listed in purchase history

  Background:
    Given I authenticate with BUYER username usertest@elasticpath.com and password password and role REGISTERED in scope mobee

  Scenario: Account orders show up in accounts and not in profile
    Given I add X-Ep-Account-Shared-Id header accounttest1@elasticpath.com
    And I have previously made a purchase with item code digital_sku
    When I navigate links defaultprofile -> accounts
    And I get the account with the field account-business-name with value Some Business Account
    And I follow links purchases
    Then there is an element for the newly create order
    When I navigate links defaultprofile -> purchases
    Then there is not an element for the newly create order

  Scenario: Non-account orders show up in profiles but not in accounts
    Given I have previously made a purchase with item code digital_sku
    When I navigate links defaultprofile -> purchases
    Then there is an element for the newly create order
    When I navigate links defaultprofile -> accounts
    And I get the account with the field account-business-name with value Some Business Account
    And I follow links purchases
    Then there is not an element for the newly create order

  Scenario: View associated account orders placed by other users
    Given I add X-Ep-Account-Shared-Id header accounttest1@elasticpath.com
    And I have previously made a purchase with item code digital_sku
    And I authenticate with BUYER username usertest2@elasticpath.com and password password and role REGISTERED in scope mobee
    When I navigate links defaultprofile -> accounts
    And I get the account with the field account-business-name with value Some Business Account
    And I follow links purchases
    Then there is an element for the newly create order