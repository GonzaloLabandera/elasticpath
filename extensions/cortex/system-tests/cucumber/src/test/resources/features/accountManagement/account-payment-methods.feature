@paymentMethods @accounts
Feature: Payment methods on account

  Background:
    Given I authenticate with BUYER_ADMIN username testbuyeradmin@elasticpath.com and password password and role REGISTERED in scope mobee

  Scenario: A BUYER_ADMIN is able to access the Account PaymentMethods resource
    Given I navigate links defaultprofile -> accounts
    And I get the account with the field account-business-name with value Some Other Business Account
    Then there is a paymentmethods link

  Scenario: A BUYER_ADMIN is able to access Account from the PaymentMethods resource
    Given I navigate links defaultprofile -> accounts
    And I get the account with the field account-business-name with value Some Other Business Account
    When I follow the link paymentmethods
    Then there is a account link

  Scenario: Non Saveable Payment Method not visible in account
    Given I navigate links defaultprofile -> accounts
    And I get the account with the field account-business-name with value Some Other Business Account
    Then I should not see Angry Path Config payment method in the account

  Scenario: Saveable Payment Methods visible in account
    Given I navigate links defaultprofile -> accounts
    And I get the account with the field account-business-name with value Some Other Business Account
    Then I should see the following payment methods in the account
      | Happy Path Config                       |
      | Address Required Happy Path Config      |
      | No Capabilities Config                  |
      | Smart Path Config                       |
      | Cancel Unsupported Config               |
      | Modify Unsupported Config               |
      | Modify And Cancel Unsupported Config    |
      | Reserve Unsupported Config              |
      | Cancel Fails Config                     |
      | Email Required Config                   |
      | Big Amount Fields Happy Path Config     |
      | Credit Unsupported Config               |
      | Reverse Unsupported Config              |
      | Single Reserve Per PI Happy Path Config |
      | Reserve Fails                           |
      | Charge Fails                            |

  Scenario: BUYER_ADMIN can access Payment Method from Profile Payment Instrument
    Given I navigate links defaultprofile -> accounts
    And I get the account with the field account-business-name with value Some Other Business Account
    And I follow links paymentmethods
    And I open Smart Path Config payment method
    And I create payment instrument supplying following fields:
      | display-name | test |
    When I navigate links defaultprofile -> accounts
    And I get the account with the field account-business-name with value Some Other Business Account
    And I follow the link paymentinstruments
    And I access test payment instrument from account
    And I follow the link paymentmethod
    Then I should arrive at the Smart Path Config payment method

  Scenario: BUYER cannot access Profile Payment Instrument form or Payment Instructions form
    Given I authenticate with BUYER username testbuyer@elasticpath.com and password password and role REGISTERED in scope mobee
    And I navigate links defaultprofile -> accounts
    And I get the account with the field account-business-name with value Some Business Account
    And I follow links paymentmethods
    And I open Smart Path Config payment method
    Then I should not see the following links
    | requestinstructionsform |
    | paymentinstrumentform |

  Scenario: Buyer ability to use stored Account payment instruments
    And I navigate links defaultprofile -> accounts
    And I get the account with the field account-business-name with value Some Business Account
    And I follow links paymentmethods
    And I open No Capabilities Config payment method
    And I create payment instrument supplying following fields:
      | display-name | account-profile-under-user-test |
    And I navigate to root
    Given I navigate links defaultcart -> order -> paymentmethodinfo
    And I open No Capabilities Config payment method
    And I create payment instrument supplying following fields:
      | display-name | user-order-test |
    And I navigate links defaultprofile -> paymentmethods
    And I open No Capabilities Config payment method
    And I create payment instrument supplying following fields:
      | display-name | user-profile-test |
    And I add X-Ep-Account-Shared-Id header accounttest1@elasticpath.com
    And I view my profile
    And I navigate links defaultprofile -> paymentmethods
    And I open No Capabilities Config payment method
    And I create payment instrument supplying following fields:
      | display-name | user-under-account-test |
    And I navigate to root
    And I navigate links defaultcart -> order -> paymentmethodinfo
    And I open No Capabilities Config payment method
    And I create payment instrument supplying following fields:
      | display-name | account-order-test |
    When I navigate to root
    And I navigate links defaultcart -> order -> paymentinstrumentselector
    Then there are 1 links of rel choice
    And I follow links choice -> description
    And the field name has value account-profile-under-user-test
    When I navigate to root
    And I navigate links defaultcart -> order -> paymentinstrumentselector
    And there are 1 links of rel chosen
    And I follow links chosen -> description
    And the field name has value account-order-test
	When I navigate to root
	And I navigate links defaultcart -> order -> paymentinstrumentselector -> default
	And the field name has value account-profile-under-user-test