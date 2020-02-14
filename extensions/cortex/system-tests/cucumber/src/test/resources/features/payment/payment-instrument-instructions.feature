@paymentMethods
Feature: Payment instrument creation instructions
  As a shopper
  I want to configure my payment instruments
  So that my private information was associated with the payment method I would use for checkout

  Scenario: Create profile payment instructions as a registered shopper
    Given I have authenticated as a newly registered shopper
    And I get the list of payment methods from my profile
    When I open Happy Path Config payment method
    And I request payment instruction supplying following fields:
      | PIC Instruction Field A | abc |
      | PIC Instruction Field B | xyz |
    Then I should see payment instruction created

  Scenario: Create order payment instructions as a registered shopper
    Given I have authenticated as a newly registered shopper
    And I get the list of payment methods from my order
    When I open Happy Path Config payment method
    And I request payment instruction supplying following fields:
      | PIC Instruction Field A | abc |
      | PIC Instruction Field B | xyz |
    Then I should see payment instruction created

  Scenario: Create order payment instructions as a anonymous shopper
    Given I am logged in as a public shopper
    And I get the list of payment methods from my order
    When I open Happy Path Config payment method
    And I request payment instruction supplying following fields:
      | PIC Instruction Field A | abc |
      | PIC Instruction Field B | xyz |
    Then I should see payment instruction created

  Scenario: Retrieve payment instructions with unsupported plugin capability is possible
    Given I have authenticated as a newly registered shopper
    And I get the list of payment methods from my profile
    When I open No Capabilities Config payment method
    And I request payment instruction without supplying any fields
    Then I should see payment instruction created

  Scenario: Retrieve payment instructions with empty fields is possible
    Given I have authenticated as a newly registered shopper
    And I get the list of payment methods from my profile
    When I open Smart Path Config payment method
    And I request payment instruction without supplying any fields
    Then I should see payment instruction created

  Scenario: Fail to retrieve payment instruction form with failing plugin capability
    Given I have authenticated as a newly registered shopper
    And I get the list of payment methods from my order
    When I open Angry Path Config payment method
    And I open payment instructions form
    Then the HTTP status is conflict
    And I should see validation error message with message type, message id, and debug message
      | error | payment.instrument.creation.failed | Payment instrument creation failed. |

  Scenario: Fail to submit payment instruction incomplete form
    Given I have authenticated as a newly registered shopper
    And I get the list of payment methods from my profile
    When I open Happy Path Config payment method
    And I request payment instruction without expected fields
    Then the HTTP status is conflict
    And I should see validation error message with message type, message id, and debug message
      | error | payment.instrument.creation.failed | Payment instrument creation failed. |

  Scenario: Fail to submit payment instruction invalid form
    Given I have authenticated as a newly registered shopper
    And I get the list of payment methods from my profile
    When I open Happy Path Config payment method
    And I request invalid payment instructions supplying following fields:
      | Invalid Field | 123 |
    Then the HTTP status is conflict
    And I should see validation error message with message type, message id, and debug message
      | error | payment.instrument.creation.failed | Payment instrument creation failed. |

  Scenario: Order payment instructions form should contain needinfo message when authenticated user doesn't have email
    Given I am logged into scope mobee as a public shopper
    And I get the list of payment methods from my order
    When I open Email Required Config payment method
    And I open payment instructions form
    Then the HTTP status is OK
    And there is needinfo with id payment.instrument.creation.failed and debug message Email is required.

  Scenario: Order payment instrument form should contain needinfo message when authenticated user doesn't have email
    Given I am logged into scope mobee as a public shopper
    And I get the list of payment methods from my order
    When I open Email Required Config payment method
    And I open payment instrument form
    Then the HTTP status is OK
    And there is needinfo with id payment.instrument.creation.failed and debug message Email is required.

  Scenario: Order payment instructions form should not contain needinfo message when authenticated user already has email
    Given I authenticate as a registered shopper harry.potter@elasticpath.com on scope mobee
    And I get the list of payment methods from my order
    When I open Email Required Config payment method
    And I open payment instructions form
    Then the HTTP status is OK
    And there are no needinfo messages

  Scenario: Order payment instrument form should not contain needinfo message when authenticated user already has email
    Given I authenticate as a registered shopper harry.potter@elasticpath.com on scope mobee
    And I get the list of payment methods from my order
    When I open Email Required Config payment method
    And I open payment instrument form
    Then the HTTP status is OK
    And there are no needinfo messages

  Scenario: Profile payment instructions form should contain needinfo message when authenticated user doesn't have email
    Given I am logged into scope mobee as a public shopper
    And I get the list of payment methods from my order
    When I open Email Required Config payment method
    And I open payment instructions form
    Then the HTTP status is OK
    And there is needinfo with id payment.instrument.creation.failed and debug message Email is required.

  Scenario: Profile payment instrument form should contain needinfo message when authenticated user doesn't have email
    Given I am logged into scope mobee as a public shopper
    And I get the list of payment methods from my order
    When I open Email Required Config payment method
    And I open payment instrument form
    Then the HTTP status is OK
    And there is needinfo with id payment.instrument.creation.failed and debug message Email is required.

  Scenario: Profile payment instructions form should not contain needinfo message when authenticated user already has email
    Given I authenticate as a registered shopper harry.potter@elasticpath.com on scope mobee
    And I get the list of payment methods from my profile
    When I open Email Required Config payment method
    And I open payment instructions form
    Then the HTTP status is OK
    And there are no needinfo messages

  Scenario: Profile payment instrument form should not contain needinfo message when authenticated user already has email
    Given I authenticate as a registered shopper harry.potter@elasticpath.com on scope mobee
    And I get the list of payment methods from my profile
    When I open Email Required Config payment method
    And I open payment instrument form
    Then the HTTP status is OK
    And there are no needinfo messages

  Scenario: A public shopper cannot retrieve order payment instructions of another public shopper
    Given I am logged into scope mobee as a public shopper
    And I get the list of payment methods from my order
    When I open Happy Path Config payment method
    And I open payment instrument form
    And save the payment instrument uri
    When I am logged in as a public shopper
    And attempt to access the other shoppers payment instructions
    Then the HTTP status is forbidden

  Scenario: A public shopper cannot retrieve order payment instructions of a registered hopper
    Given I have authenticated as a newly registered shopper
    And I get the list of payment methods from my order
    When I open Happy Path Config payment method
    And I open payment instrument form
    And save the payment instrument uri
    When I am logged in as a public shopper
    And attempt to access the other shoppers payment instructions
    Then the HTTP status is forbidden

  Scenario: A public shopper cannot retrieve profile payment instructions of a registered shopper
    Given I have authenticated as a newly registered shopper
    And I get the list of payment methods from my profile
    When I open Happy Path Config payment method
    And I open payment instrument form
    And save the payment instrument uri
    When I am logged in as a public shopper
    And attempt to access the other shoppers payment instructions
    Then the HTTP status is forbidden

  Scenario: A registered shopper cannot retrieve profile payment instructions of another registered shopper
    Given I have authenticated as a newly registered shopper
    And I get the list of payment methods from my profile
    When I open Happy Path Config payment method
    And I open payment instrument form
    And save the payment instrument uri
    Given I have authenticated as a newly registered shopper
    And attempt to access the other shoppers payment instructions
    Then the HTTP status is forbidden
