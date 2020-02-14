@regressionTest @customerService @order @orderPayments
Feature: Order Payments Charge Details

  Scenario Outline: Order payments transactions details
    Given I sign in to CM as CSR user
    And I have authenticated as a newly registered shopper
    And I create an unsaved <PAYMENT_METHOD> payment instrument from order supplying following fields:
      | PIC Field A  | abc                       |
      | PIC Field B  | xyz                       |
      | display-name | <PAYMENT_INSTRUMENT_NAME> |
    And I make a purchase with newly created payment instrument for scope mobee with following skus
      | skuCode     | quantity |
      | digital_sku | 1        |
    When I search and open order editor for the latest order
    Then I should see following order payment transaction in the Payment History
      | Method                      | <PAYMENT_METHOD>          |
      | Type                        | Reserve                   |
      | Details                     | <PAYMENT_INSTRUMENT_NAME> |
      | Status                      | Approved                  |
      | Amount                      | <ORDER_TOTAL>             |
      | Original payment instrument | Yes                       |
    And I should see following order payment transaction in the Payment History
      | Method                      | <PAYMENT_METHOD>          |
      | Type                        | Charge                    |
      | Details                     | <PAYMENT_INSTRUMENT_NAME> |
      | Status                      | Approved                  |
      | Amount                      | <ORDER_TOTAL>             |
      | Original payment instrument | Yes                       |
    Examples:
      | PAYMENT_METHOD    | PAYMENT_INSTRUMENT_NAME | ORDER_TOTAL |
      | Happy Path Config | my visa                 | $21.30      |

  Scenario Outline: Payment Instrument will be charged if already reserved even the associated payment method is disabled
    Given I sign in to CM as admin user
    And I go to Payment Configurations
    And I create a new Payment Configuration with following details
      | PROVIDER           | Happy Path           |
      | METHOD             | CARD METHOD          |
      | CONFIGURATION_NAME | ATest payment config |
      | DISPLAY_NAME       | ATest payment config |
    And I configure the payment configuration properties
      | Config A | value A |
      | Config B | value B |
    And I save the payment configuration
    And I activate the newly created payment configuration
    And I select and save the newly created payment configuration for store MOBEE
    And I have authenticated as a newly registered shopper
    And I have created payment instrument with the newly created payment configuration on my profile:
      | display-name | <PAYMENT_INSTRUMENT_NAME> |
      | PIC Field A  | Test PIC Value A          |
      | PIC Field B  | Test PIC Value B          |
    And I make a purchase with newly created payment instrument for scope mobee with following skus
      | skuCode                 | quantity |
      | handsfree_shippable_sku | 1        |
    And I deselect and save the newly created payment configuration for store MOBEE
    And I search and open order editor for the latest order
    When I complete the order shipment
    Then I should see following order payment transaction in the Payment History
      | Type    | Charge                    |
      | Details | <PAYMENT_INSTRUMENT_NAME> |
      | Status  | Approved                  |
      | Amount  | <ORDER_TOTAL>             |

    Examples:
      | PAYMENT_INSTRUMENT_NAME | ORDER_TOTAL |
      | my visa                 | $213.00     |

  Scenario: Payment Instruments for previous order are visible in payment history even the associated payment method is disabled
    Given I sign in to CM as admin user
    And I go to Payment Configurations
    And I create a new Payment Configuration with following details
      | PROVIDER           | Happy Path           |
      | METHOD             | CARD METHOD          |
      | CONFIGURATION_NAME | ATest payment config |
      | DISPLAY_NAME       | ATest payment config |
    And I configure the payment configuration properties
      | Config A | value A |
      | Config B | value B |
    And I save the payment configuration
    And I activate the newly created payment configuration
    And I select and save the newly created payment configuration for store MOBEE
    And I have authenticated as a newly registered shopper
    And I have created payment instrument with the newly created payment configuration on my profile:
      | display-name | my visa          |
      | PIC Field A  | Test PIC Value A |
      | PIC Field B  | Test PIC Value B |
    And I make a purchase with newly created payment instrument for scope mobee with following skus
      | skuCode     | quantity |
      | digital_sku | 1        |
    And I deselect and save the newly created payment configuration for store MOBEE
    When I view the purchase
    Then the purchase payment instrument name matches the instrument used to create the purchase

  Scenario: Free order should not have any payment events in payment history
    Given I sign in to CM as CSR user
    And I have authenticated as a newly registered shopper
    And I create an order with Canadian address for scope mobee with following skus
      | skuCode                 | quantity |
      | physicalFreeProduct_sku | 1        |
    And I search and open order editor for the latest order
    When I complete the order shipment
    Then I should NOT see order payment transactions in the Payment History