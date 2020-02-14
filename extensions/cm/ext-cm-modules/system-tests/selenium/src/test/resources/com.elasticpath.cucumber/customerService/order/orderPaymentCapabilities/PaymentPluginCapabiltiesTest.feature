@regressionTest @customerService @order @orderPaymentCapabilities
Feature: Payment Plugins Capabilities Test

  Background:
    Given I sign in to CM as admin user
    And I have authenticated as a newly registered shopper

  Scenario Outline: Reserve transaction skipped with no-capabilities-plugin
    Given I create an unsaved <PAYMENT_METHOD> payment instrument from order supplying following fields:
      | PIC Field A  | abc                       |
      | PIC Field B  | xyz                       |
      | display-name | <PAYMENT_INSTRUMENT_NAME> |
    And I make a purchase with newly created payment instrument for scope mobee with following skus
      | skuCode     | quantity |
      | digital_sku | 1        |
    When I search and open order editor for the latest order
    And I click on Show Skipped Payment Events
    Then I should see following order payment transaction in the Payment History with skipped events
      | Method                      | <PAYMENT_METHOD>          |
      | Type                        | Reserve                   |
      | Details                     | <PAYMENT_INSTRUMENT_NAME> |
      | Status                      | Skipped                   |
      | Amount                      | <ORDER_TOTAL>             |
      | Original payment instrument | Yes                       |
    And I should see following order payment transaction in the Payment History with skipped events
      | Method                      | <PAYMENT_METHOD>          |
      | Type                        | Charge                    |
      | Details                     | <PAYMENT_INSTRUMENT_NAME> |
      | Status                      | Approved                  |
      | Amount                      | <ORDER_TOTAL>             |
      | Original payment instrument | Yes                       |
    Examples:
      | PAYMENT_METHOD         | PAYMENT_INSTRUMENT_NAME | ORDER_TOTAL |
      | No Capabilities Config | my visa                 | $21.30      |

  Scenario Outline: Charge Failed should have Charge Failed event
    Given I go to Payment Configurations
    And I create a new Payment Configuration with following details
      | PROVIDER           | Smart Path   |
      | METHOD             | CARD         |
      | CONFIGURATION_NAME | Charge FAILS |
      | DISPLAY_NAME       | Charge FAILS |
    And I configure the payment configuration properties
      | CHARGE | FAILS |
    And I save the payment configuration
    And I activate the newly created payment configuration
    And I select and save the newly created payment configuration for store MOBEE
    And I have created order payment instrument with the newly created payment configuration
      | display-name | <PAYMENT_INSTRUMENT_NAME> |
    And I make a purchase with newly created payment instrument for scope mobee with following skus
      | skuCode      | quantity |
      | physical_sku | 1        |
    And I search and open order editor for the latest order
    When I cannot complete the order shipment with error message insufficient inventory or payment gateway failure
    Then I should see following order payment transaction in the Payment History
      | Method  | <PAYMENT_CONFIGURATION>   |
      | Type    | Cancel Reserve            |
      | Details | <PAYMENT_INSTRUMENT_NAME> |
      | Status  | Approved                  |
      | Amount  | $<ORDER_TOTAL>            |
    And I should see following order payment transaction in the Payment History
      | Method  | <PAYMENT_CONFIGURATION>   |
      | Type    | Reserve                   |
      | Details | <PAYMENT_INSTRUMENT_NAME> |
      | Status  | Approved                  |
      | Amount  | $<ORDER_TOTAL>            |
    And I should see following order payment transaction in the Payment History
      | Method  | <PAYMENT_CONFIGURATION>   |
      | Type    | Charge                    |
      | Details | <PAYMENT_INSTRUMENT_NAME> |
      | Status  | Failed                    |
      | Amount  | $<ORDER_TOTAL>            |

    Examples:
      | PAYMENT_CONFIGURATION | PAYMENT_INSTRUMENT_NAME | ORDER_TOTAL |
      | Charge FAILS          | my visa                 | 131.50      |

  Scenario Outline: Charge Failed and force completed order should not charge the payments
    Given I go to Payment Configurations
    And I create a new Payment Configuration with following details
      | PROVIDER           | Smart Path   |
      | METHOD             | CARD         |
      | CONFIGURATION_NAME | Charge FAILS |
      | DISPLAY_NAME       | Charge FAILS |
    And I configure the payment configuration properties
      | CHARGE | FAILS |
    And I save the payment configuration
    And I activate the newly created payment configuration
    And I select and save the newly created payment configuration for store MOBEE
    And I have created order payment instrument with the newly created payment configuration
      | display-name | <PAYMENT_INSTRUMENT_NAME> |
    And I make a purchase with newly created payment instrument for scope mobee with following skus
      | skuCode      | quantity |
      | physical_sku | 1        |
    And I search and open order editor for the latest order
    When I force complete shipment for shipment ID 1
    Then I should NOT see following order payment transaction in the Payment History
      | Method  | <PAYMENT_CONFIGURATION>   |
      | Type    | Charge                    |
      | Details | <PAYMENT_INSTRUMENT_NAME> |
      | Status  | Approved                  |
      | Amount  | $<ORDER_TOTAL>            |
    And Payment Summary should have the following totals
      | Ordered     | $<ORDER_TOTAL> |
      | Paid        | $0.00          |
      | Balance Due | $<ORDER_TOTAL> |

    Examples:
      | PAYMENT_CONFIGURATION | PAYMENT_INSTRUMENT_NAME | ORDER_TOTAL |
      | Charge FAILS          | my visa                 | 131.50      |

  Scenario Outline: Retry Charge on available reserved payment instrument
    Given I go to Payment Configurations
    And I create a new Payment Configuration with following details
      | PROVIDER           | Smart Path           |
      | METHOD             | CARD                 |
      | CONFIGURATION_NAME | <CONFIGURATION_NAME> |
      | DISPLAY_NAME       | <CONFIGURATION_NAME> |
    And I configure the payment configuration properties
      | CHARGE | FAILS |
    And I save the payment configuration
    And I activate the newly created payment configuration
    And I select and save the newly created payment configuration for store MOBEE
    And I have created payment instrument with the newly created payment configuration on my profile:
      | display-name | <PAYMENT_INSTRUMENT_NAME> |
    And I make a purchase with newly created payment instrument for scope mobee with following skus
      | skuCode      | quantity |
      | physical_sku | 1        |
    And I search and open order editor for the latest order
    And I cannot complete the order shipment with error message insufficient inventory or payment gateway failure
    When I update the saved payment configuration properties
      | CHARGE |  |
    Then I can release the order shipment
    And I should see following order payment transaction in the Payment History
      | Method                      | <CONFIGURATION_NAME>      |
      | Type                        | Charge                    |
      | Details                     | <PAYMENT_INSTRUMENT_NAME> |
      | Status                      | Approved                  |
      | Amount                      | $131.50                   |
      | Original payment instrument | Yes                       |

    Examples:
      | CONFIGURATION_NAME | PAYMENT_INSTRUMENT_NAME |
      | Charge FAILS       | my visa                 |