@accounts
Feature: Buyer needs to view all associated Accounts

  Background:
    Given I authenticate with BUYER username usertest@elasticpath.com and password password and role REGISTERED in scope mobee

  Scenario: User can view both associated accounts
    When I navigate links defaultprofile -> accounts
    Then there are 2 links of rel element
    And there is an account with the field account-business-name with value Some Business Account
    And there is an account with the field account-business-name with value Some Other Business Account

  Scenario: User can view the shared id of an account
    When I navigate links defaultprofile -> accounts
    And I get the account with the field account-business-name with value Some Business Account
    Then there is a identifier link with the field shared-id with value accounttest1@elasticpath.com

  Scenario: User can view the attributes of an account
    When I navigate links defaultprofile -> accounts
    And I get the account with the field account-business-name with value Some Business Account
    Then there is a attributes link with the field TEST_READONLY_ACCOUNT_ATTRIBUTE_SHORT_TEXT with value read only attribute

  Scenario: User cannot view an account they are not associated with
    Given I navigate links defaultprofile -> accounts
    And I get the account with the field account-business-name with value Some Business Account
    And save the purchase uri
    When I have authenticated as a newly registered shopper
    And attempt to access the other shoppers accounts uri
    Then the HTTP status is forbidden