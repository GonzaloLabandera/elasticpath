@paymentMethods
Feature: Payment instrument billing address
  As a shopper
  I want to create my payment instruments
  So that I could use them for checkout

  Background:
    Given I have authenticated as a newly registered shopper

  Scenario: Shopper can see billing address on payment instructions form
    Given I get the list of payment methods from my profile
    When I open Address Required Happy Path Config payment method
    And I follow the link requestinstructionsform
    Then the payments form contains the following fields
      | billing-address         |
      | PIC Instruction Field A |
      | PIC Instruction Field B |

  Scenario: Shopper can see billing address on payment instrument creation form
    Given I get the list of payment methods from my profile
    When I open Address Required Happy Path Config payment method
    And I follow the link paymentinstrumentform
    Then the payments form contains the following fields
      | billing-address                        |
      | payment-instrument-identification-form |
      | default-on-profile                     |

  Scenario: Shopper cannot see billing address on payment instructions form when billing address not required
    Given I get the list of payment methods from my profile
    When I open Smart Path Config payment method
    And I follow the link requestinstructionsform
    And the instrument creation form does not contain the following fields
      | billing-address |

  Scenario: Shopper cannot see billing address on payment instrument creation form when billing address not required
    Given I get the list of payment methods from my profile
    When I open Smart Path Config payment method
    And I follow the link paymentinstrumentform
    And the instrument creation form does not contain the following fields
      | billing-address |

  Scenario: Shopper successfully submits instructions form with billing address when address required
    Given I get the list of payment methods from my profile
    When I open Address Required Happy Path Config payment method
    And I open payment instructions form
    And I request payments instructions with a valid address and data
    Then I should see payment instruction created

  Scenario: Shopper cannot submit instructions form without billing address when address required
    Given I get the list of payment methods from my profile
    When I open Address Required Happy Path Config payment method
    And I request invalid payment instructions supplying following fields:
      | PIC Instruction Field A | abc |
      | PIC Instruction Field B | xyz |
    Then the HTTP status is conflict
    And I should see validation error message with message type, message id, and debug message
      | error | payment.instrument.creation.failed | Payment instrument creation failed. |

  Scenario: Shopper cannot submit instructions form with blank billing address when address required
    Given I get the list of payment methods from my profile
    When I open Address Required Happy Path Config payment method
    And I open payment instructions form
    And I request payments instructions with a blank billing address and data
    Then the HTTP status is bad request
    And Structured error message contains:
      | given-name must not be blank              |
      | locality must not be blank                |
      | street-address must not be blank          |
      | family-name must not be blank             |
      | postal-code must not be blank             |
      | country-name must not be blank            |
      | country-name size must be between 2 and 2 |

  Scenario: Shopper successfully creates a payment instrument with a billing address when address required
    Given I get the list of payment methods from my profile
    When I open Address Required Happy Path Config payment method
    And I open payment instrument form
    And I create a payment instrument with a valid address and data
    Then I should see the created payment instrument

  Scenario: Shopper cannot create instrument with blank billing address when address required
    Given I get the list of payment methods from my profile
    When I open Address Required Happy Path Config payment method
    And I open payment instrument form
    And I create a payment instrument with a blank address and data
    Then the HTTP status is bad request
    And Structured error message contains:
      | given-name must not be blank              |
      | locality must not be blank                |
      | street-address must not be blank          |
      | family-name must not be blank             |
      | postal-code must not be blank             |
      | country-name must not be blank            |
      | country-name size must be between 2 and 2 |

  Scenario: Shopper cannot create instrument without billing address when address required
    Given I get the list of payment methods from my profile
    When I open Address Required Happy Path Config payment method
    And I create payment instrument supplying following fields:
      | PIC Field A  | abc  |
      | PIC Field B  | xyz  |
      | display-name | Name |
    Then the HTTP status is conflict
    And I should see validation error message with message type, message id, and debug message
      | error | payment.instrument.creation.failed | Payment instrument creation failed. |

  Scenario: Shopper submits instructions form with billing address when no address required
    Given I get the list of payment methods from my profile
    When I open Happy Path Config payment method
    And I open payment instructions form
    And I request payments instructions with a valid address and data
    Then I should see payment instruction created

  Scenario: Shopper successfully creates a payment instrument with a billing address when no address required
    Given I get the list of payment methods from my profile
    When I open Happy Path Config payment method
    And I open payment instrument form
    And I create a payment instrument with a valid address and data
    Then I should see the created payment instrument

  Scenario: Billing address used for payment instrument is saved to profile
    Given I view my profile
    And I follow links addresses
    And there are 0 links of rel element
    When I get the list of payment methods from my profile
    And I open Address Required Happy Path Config payment method
    And I open payment instrument form
    And I create a payment instrument with a valid address and data
    And I view my profile
    And I follow links addresses
    Then there are 1 links of rel element

  Scenario: Same billing address from profile used for payment instrument does not save to profile again
    Given I view my profile
    And I get address form
    And I create address with Country CA, Extended-Address extended address, Locality Vancouver, Organization organization corp, Phone-Number 800-267-8888, Postal-Code V7V7V7, Region BC, Street-Address 123 Broadway, Family-Name family-name and Given-Name given-name
    And I view my profile
    And I follow links addresses
    And there are 1 links of rel element
    When I get the list of payment methods from my profile
    And I open Address Required Happy Path Config payment method
    And I open payment instrument form
    And I create a payment instrument with a valid address and data
    And I view my profile
    And I follow links addresses
    Then there are 1 links of rel element
