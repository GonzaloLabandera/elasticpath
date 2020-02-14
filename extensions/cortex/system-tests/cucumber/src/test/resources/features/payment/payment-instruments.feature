@paymentMethods
Feature: Payment instrument
  As a shopper
  I want to create my payment instruments
  So that I could use them for checkout

  Scenario: A shopper is able to access the PaymentInstruments resource from Profile
    Given I login as a newly registered shopper
    When I view my profile
    Then there is a paymentinstruments link

  Scenario: A shopper is able to access Profile from the PaymentInstruments resource
    Given I login as a newly registered shopper
    And I view my profile
    When I follow the link paymentinstruments
    Then there is a profile link

  Scenario Outline: Anonymous shopper can create order payment instrument and it gets selected
    Given I am logged in as a public shopper
    And I get the list of payment methods from my order
    When I open Happy Path Config payment method
    And I create payment instrument supplying following fields:
      | PIC Field A  | abc    |
      | PIC Field B  | xyz    |
      | display-name | <Name> |
    Then I should see <Name> payment instrument created
    And payment instrument with name <Name> is selected for order

    Examples:
      | Name         |
      | Payment Name |

  Scenario Outline: Shopper can create profile payment instrument
    Given I have authenticated as a newly registered shopper
    And I get the list of payment methods from my profile
    When I open Happy Path Config payment method
    And I create payment instrument supplying following fields:
      | PIC Field A  | abc    |
      | PIC Field B  | xyz    |
      | display-name | <Name> |
    Then I should see <Name> payment instrument created

    Examples:
      | Name         |
      | Payment Name |

  Scenario Outline: Shopper can create order payment instrument and it gets selected
    Given I have authenticated as a newly registered shopper
    And I get the list of payment methods from my order
    When I open Happy Path Config payment method
    And I create payment instrument supplying following fields:
      | PIC Field A  | abc    |
      | PIC Field B  | xyz    |
      | display-name | <Name> |
    Then I should see <Name> payment instrument created
    And payment instrument with name <Name> is selected for order

    Examples:
      | Name         |
      | Payment Name |

  Scenario: Fail to create payment instrument with incomplete form
    Given I have authenticated as a newly registered shopper
    And I get the list of payment methods from my profile
    When I open Happy Path Config payment method
    And I create payment instrument without supplying any fields
    Then the HTTP status is conflict
    And I should see validation error message with message type, message id, and debug message
      | error | payment.instrument.creation.failed | Payment instrument creation failed. |

  Scenario: Fail to create payment instrument with invalid form
    Given I have authenticated as a newly registered shopper
    And I get the list of payment methods from my profile
    When I open Happy Path Config payment method
    And I create payment instrument supplying following fields:
      | Invalid Field | 123 |
    Then the HTTP status is conflict
    And I should see validation error message with message type, message id, and debug message
      | error | payment.instrument.creation.failed | Payment instrument creation failed. |

  Scenario Outline: Latest created order payment instrument should be selected
    Given I have authenticated as a newly registered shopper
    And I get the list of payment methods from my order
    When I open Happy Path Config payment method
    And I create payment instrument supplying following fields:
      | PIC Field A  | Test PIC Value A |
      | PIC Field B  | Test PIC Value B |
      | display-name | <Name_1>         |
    And payment instrument with name <Name_1> is selected for order
    And I get the list of payment methods from my order
    When I open Happy Path Config payment method
    And I create payment instrument supplying following fields:
      | PIC Field A  | Test PIC Value A |
      | PIC Field B  | Test PIC Value B |
      | display-name | <Name_2>         |
    And payment instrument with name <Name_2> is selected for order

    Examples:
      | Name_1         | Name_2         |
      | Payment Name 1 | Payment Name 2 |

  Scenario: Profile payment instrument creation requires display name
    Given I have authenticated as a newly registered shopper
    And I get the list of payment methods from my profile
    When I open Happy Path Config payment method
    And I create payment instrument supplying following fields:
      | PIC Field A | Test PIC Value A |
      | PIC Field B | Test PIC Value B |
    Then the HTTP status is conflict
    And I should see validation error message with message type, message id, and debug message
      | error | payment.instrument.name.required | Name must not be blank. |


  Scenario: Order payment instrument creation requires display name for registered shopper
    Given I have authenticated as a newly registered shopper
    And I get the list of payment methods from my order
    When I open Happy Path Config payment method
    And I create payment instrument supplying following fields:
      | PIC Field A | Test PIC Value A |
      | PIC Field B | Test PIC Value B |
    Then the HTTP status is conflict
    And I should see validation error message with message type, message id, and debug message
      | error | payment.instrument.name.required | Name must not be blank. |


  Scenario: Order payment instrument creation requires display name for public shopper
    Given I am logged in as a public shopper
    And I get the list of payment methods from my order
    When I open Happy Path Config payment method
    And I create payment instrument supplying following fields:
      | PIC Field A | Test PIC Value A |
      | PIC Field B | Test PIC Value B |
    Then the HTTP status is conflict
    And I should see validation error message with message type, message id, and debug message
      | error | payment.instrument.name.required | Name must not be blank. |
