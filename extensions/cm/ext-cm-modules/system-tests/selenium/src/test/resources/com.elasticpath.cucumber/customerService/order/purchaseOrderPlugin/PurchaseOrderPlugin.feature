@regressionTest @customerService @order @purchaseOrderPlugin
Feature: Purchase order plugin

  Background:
    Given I sign in to CM as admin user
    And I go to Configuration
    And I go to Payment Configurations
    And I have authenticated as a newly registered shopper

  Scenario Outline: Purchase Order instrument validates required input
    Given I create a new Payment Configuration with following details
      | PROVIDER           | ELASTICPATH             |
      | METHOD             | <PAYMENT_METHOD>        |
      | CONFIGURATION_NAME | <PAYMENT_CONFIGURATION> |
    And I save the payment configuration
    And I activate the newly created payment configuration
    And I select and save the newly created payment configuration for store MOBEE
    When I have created payment instrument with the newly created payment configuration on my profile:
      | display-name   | <INSTRUMENT_NAME>       |
      | purchase-order | <PURCHASE_ORDER_NUMBER> |
    Then I should see validation error message with message type, message id, and debug message
      | messageType | messageId                          | debugMessage                |
      | error       | payment.instrument.creation.failed | Purchase order is required. |

    Examples:
      | PAYMENT_METHOD | PAYMENT_CONFIGURATION | INSTRUMENT_NAME           | PURCHASE_ORDER_NUMBER |
      | PURCHASE_ORDER | EP purchase order     | purchase-order-instrument |                       |

  Scenario Outline: Purchase Order data in payment details
    Given I create a new Payment Configuration with following details
      | PROVIDER           | ELASTICPATH             |
      | METHOD             | <PAYMENT_METHOD>        |
      | CONFIGURATION_NAME | <PAYMENT_CONFIGURATION> |
    And I save the payment configuration
    And I activate the newly created payment configuration
    And I select and save the newly created payment configuration for store MOBEE
    And I have created payment instrument with the newly created payment configuration on my profile:
      | display-name   | <INSTRUMENT_NAME>       |
      | purchase-order | <PURCHASE_ORDER_NUMBER> |
    When I make a purchase with newly created payment instrument for scope mobee with following skus
      | skuCode     | quantity |
      | digital_sku | 1        |
    And I search and open order editor for the latest order
    Then I should see following order payment transaction in the Payment History
      | Method                      | <PAYMENT_METHOD>  |
      | Type                        | Reserve           |
      | Details                     | <INSTRUMENT_NAME> |
      | Status                      | Approved          |
      | Amount                      | <ORDER_TOTAL>     |
      | Original payment instrument | Yes               |
    And I should see following order payment data
      | PURCHASE_ORDER | <PURCHASE_ORDER_NUMBER> |
    And I should see following order payment transaction in the Payment History
      | Method                      | <PAYMENT_METHOD>  |
      | Type                        | Charge            |
      | Details                     | <INSTRUMENT_NAME> |
      | Status                      | Approved          |
      | Amount                      | <ORDER_TOTAL>     |
      | Original payment instrument | Yes               |
    And I should see following order payment data
      | PURCHASE_ORDER | <PURCHASE_ORDER_NUMBER> |

    Examples:
      | PAYMENT_METHOD | PAYMENT_CONFIGURATION | INSTRUMENT_NAME           | ORDER_TOTAL | PURCHASE_ORDER_NUMBER |
      | PURCHASE_ORDER | EP purchase order     | purchase-order-instrument | $21.30      | 12345                 |