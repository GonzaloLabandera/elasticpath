@paymentMethods @accounts
Feature: Account Payment instructions

  Background:
    Given I authenticate with BUYER_ADMIN username testbuyeradmin@elasticpath.com and password password and role REGISTERED in scope mobee

  Scenario: Create account payment instructions as a registered shopper
    Given I navigate links defaultprofile -> accounts
    And I get the account with the field account-business-name with value Some Other Business Account
    And I follow the link paymentmethods
    When I open Happy Path Config payment method
    And I request payment instruction supplying following fields:
      | PIC Instruction Field A | abc |
      | PIC Instruction Field B | xyz |
    Then I should see payment instruction created

  Scenario: Retrieve payment instructions with unsupported plugin capability is possible
    Given I navigate links defaultprofile -> accounts
    And I get the account with the field account-business-name with value Some Other Business Account
    And I follow the link paymentmethods
    When I open No Capabilities Config payment method
    And I request payment instruction without supplying any fields
    Then I should see payment instruction created

  Scenario: Retrieve payment instructions with empty fields is possible
    Given I navigate links defaultprofile -> accounts
    And I get the account with the field account-business-name with value Some Other Business Account
    And I follow the link paymentmethods
    When I open Smart Path Config payment method
    And I request payment instruction without supplying any fields
    Then I should see payment instruction created

  Scenario: Fail to submit payment instruction incomplete form
    Given I navigate links defaultprofile -> accounts
    And I get the account with the field account-business-name with value Some Other Business Account
    And I follow the link paymentmethods
    When I open Happy Path Config payment method
    And I request payment instruction without expected fields
    Then the HTTP status is conflict
    And I should see validation error message with message type, message id, and debug message
      | error | payment.instrument.creation.failed | Payment instrument creation failed. |

  Scenario: Fail to submit payment instruction invalid form
    Given I navigate links defaultprofile -> accounts
    And I get the account with the field account-business-name with value Some Other Business Account
    And I follow the link paymentmethods
    When I open Happy Path Config payment method
    And I request invalid payment instructions supplying following fields:
      | Invalid Field | 123 |
    Then the HTTP status is conflict
    And I should see validation error message with message type, message id, and debug message
      | error | payment.instrument.creation.failed | Payment instrument creation failed. |

  Scenario: Account payment instructions form should contain needinfo message when authenticated user doesn't have email
    Given I navigate links defaultprofile -> accounts
    And I get the account with the field account-business-name with value Some Other Business Account
    And I follow the link paymentmethods
    When I open Email Required Config payment method
    And I open payment instructions form
    Then the HTTP status is OK
    And there is needinfo with id payment.instrument.creation.failed and debug message Email is required.

  Scenario: Account payment instrument form should contain needinfo message when authenticated user doesn't have email
    Given I navigate links defaultprofile -> accounts
    And I get the account with the field account-business-name with value Some Other Business Account
    And I follow the link paymentmethods
    When I open Email Required Config payment method
    And I open payment instrument form
    Then the HTTP status is OK
    And there is needinfo with id payment.instrument.creation.failed and debug message Email is required.

  Scenario: A shopper cannot retrieve order payment instructions of an account they are not associated with
    Given I navigate links defaultprofile -> accounts
    And I get the account with the field account-business-name with value Some Other Business Account
    And I follow the link paymentmethods
    When I open Happy Path Config payment method
    And I open payment instrument form
    And save the payment instrument uri
    When I have authenticated as a newly registered shopper
    And attempt to access the other shoppers payment instructions
    Then the HTTP status is forbidden
