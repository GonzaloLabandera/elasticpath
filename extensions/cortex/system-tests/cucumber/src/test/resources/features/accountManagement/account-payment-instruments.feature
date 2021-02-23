@paymentMethods @accounts
Feature: Account Payment instrument

  Background:
    Given I authenticate with BUYER_ADMIN username testbuyeradmin@elasticpath.com and password password and role REGISTERED in scope mobee

  Scenario: A BUYER_ADMIN is able to access the PaymentInstruments resource from account
    Given I navigate links defaultprofile -> accounts
    And I get the account with the field account-business-name with value Some Other Business Account
    Then there is a paymentinstruments link

  Scenario: A BUYER_ADMIN is able to access account from the PaymentInstruments resource
    Given I navigate links defaultprofile -> accounts
    And I get the account with the field account-business-name with value Some Other Business Account
    When I follow the link paymentinstruments
    Then there is a account link

  Scenario Outline: BUYER_ADMIN can create account payment instrument
    Given I navigate links defaultprofile -> accounts
    And I get the account with the field account-business-name with value Some Other Business Account
    And I follow the link paymentmethods
    When I open Happy Path Config payment method
    And I create payment instrument supplying following fields:
      | PIC Field A  | abc    |
      | PIC Field B  | xyz    |
      | display-name | <Name> |
    Then I should see <Name> payment instrument created

    Examples:
      | Name         |
      | Payment Name |

  Scenario: Fail to create payment instrument with incomplete form
    Given I navigate links defaultprofile -> accounts
    And I get the account with the field account-business-name with value Some Other Business Account
    And I follow the link paymentmethods
    When I open Happy Path Config payment method
    And I create payment instrument without supplying any fields
    Then the HTTP status is conflict
    And I should see validation error message with message type, message id, and debug message
      | error | payment.instrument.creation.failed | Payment instrument creation failed. |

  Scenario: Fail to create payment instrument with invalid form
    Given I navigate links defaultprofile -> accounts
    And I get the account with the field account-business-name with value Some Other Business Account
    And I follow the link paymentmethods
    When I open Happy Path Config payment method
    And I create payment instrument supplying following fields:
      | Invalid Field | 123 |
    Then the HTTP status is conflict
    And I should see validation error message with message type, message id, and debug message
      | error | payment.instrument.creation.failed | Payment instrument creation failed. |

  Scenario: Account payment instrument creation requires display name
    Given I navigate links defaultprofile -> accounts
    And I get the account with the field account-business-name with value Some Other Business Account
    And I follow the link paymentmethods
    When I open Happy Path Config payment method
    And I create payment instrument supplying following fields:
      | PIC Field A | Test PIC Value A |
      | PIC Field B | Test PIC Value B |
    Then the HTTP status is conflict
    And I should see validation error message with message type, message id, and debug message
      | error | payment.instrument.name.required | Name must not be blank. |

