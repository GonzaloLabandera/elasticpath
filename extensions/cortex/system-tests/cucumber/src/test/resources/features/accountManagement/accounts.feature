@accounts
Feature: Buyer needs to view all associated Accounts

  Background:
    Given I authenticate with BUYER username usertest@elasticpath.com and password password and role REGISTERED in scope mobee

  Scenario: User can view all associated accounts
    When I navigate links defaultprofile -> accounts
    Then there are 3 links of rel element
    And there is an account with the field account-business-name with value Some Business Account
    And there is an account with the field account-business-name with value Some Other Business Account
    And there is an account with the field account-business-name with value Suspended Account

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

  Scenario: User can view selected account
    Given I add X-Ep-Account-Shared-Id header accounttest1@elasticpath.com
    When I navigate links defaultprofile -> selectedaccount
    Then I should see account attribute account-business-name with value Some Business Account

  Scenario: User cannot see selected account link when account is not selected
    When I view my profile
    Then I should not see selectedaccount link

  Scenario: User cannot transact with account they are not associated to
    Given I add X-Ep-Account-Shared-Id header SomeBusiness3@abc.com
    When I navigate to root
    Then the HTTP status is unauthorized

  Scenario: Cannot submit an order when account has status Suspended
    Then I add X-Ep-Account-Shared-Id header SuspendedAccount@elasticpath.com
    Then I retrieve the order
    And there are advisor messages with the following fields:
      | messageType | messageId         | debugMessage                                                | linkedTo |
      | error       | account.suspended | The account you are transacting for is currently suspended. |          |

  Scenario: BUYER cannot edit account attributes
     Given I authenticate with BUYER username testbuyer@elasticpath.com and password password and role REGISTERED in scope mobee
     And I navigate links defaultprofile -> accounts
     And I get the account with the field account-business-name with value Some Business Account
     When I update attributes with following values
      | TEST_EDITABLE_ACCOUNT_ATTRIBUTE_SHORT_TEXT | updated |
      | account-business-name | Some Business Account |
      | account-business-number |  |
      | account-fax | |
      | account-phone | |
      | account-tax-exemption-id | |
     Then the HTTP status is forbidden

  Scenario: BUYER_ADMIN can edit account attributes
     Given I authenticate with BUYER_ADMIN username testbuyeradmin@elasticpath.com and password password and role REGISTERED in scope mobee
     And I navigate links defaultprofile -> accounts
     And I get the account with the field account-business-name with value Some Business Account
     When I update attributes with following values
       | TEST_EDITABLE_ACCOUNT_ATTRIBUTE_SHORT_TEXT | updated |
       | account-business-name | Some Business Account |
       | account-business-number |  |
       | account-fax | |
       | account-phone | |
       | account-tax-exemption-id | |
     Then the HTTP status is no content

Scenario: Account attribute updates are validated
     Given I authenticate with BUYER_ADMIN username testbuyeradmin@elasticpath.com and password password and role REGISTERED in scope mobee
     And I navigate links defaultprofile -> accounts
     And I get the account with the field account-business-name with value Some Business Account
     When I update attributes with following values
       | TEST_EDITABLE_ACCOUNT_ATTRIBUTE_SHORT_TEXT | updated |
     Then the HTTP status is bad request
     And the response message is Required account fields {account-business-name} are missing.