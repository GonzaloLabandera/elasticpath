@regressionTest @customerService @order @manualRefund
Feature: Order Manual Refund

  Background:
    Given I sign in to CM as admin user
    And I have authenticated as a newly registered shopper

  Scenario Outline: Manually refund order when refund amount is equal or less than captured order/shipment total
    Given I create an unsaved <PAYMENT_METHOD> payment instrument from order supplying following fields:
      | display-name | <INSTRUMENT_NAME> |
    And I make a purchase with newly created payment instrument for scope mobee with following skus
      | skuCode    | quantity |
      | <SKU_CODE> | 1        |
    And I search and open order editor for the latest order
    When I create a refund with following values
      | Available Refund Amount | <AVAILABLE_REFUND> |
      | Currency Code           | CAD                |
      | Refund Amount           | <REFUND_AMOUNT>    |
      | Refund Note             | paid by cash       |
      | Payment Source          | Manual Refund      |
    Then I should see following order payment transaction in the Payment History
      | Method  |                   |
      | Type    | Manual Credit     |
      | Details |                   |
      | Status  | Approved          |
      | Amount  | -$<REFUND_AMOUNT> |

    Examples:
      | PAYMENT_METHOD    | INSTRUMENT_NAME | SKU_CODE                                   | REFUND_AMOUNT | AVAILABLE_REFUND |
      | Smart Path Config | my visa         | digital_sku                                | 10.00         | $21.30           |
      | Smart Path Config | my visa         | digital_sku                                | 21.30         | $21.30           |
      | Smart Path Config | my visa         | bundleWithPhysicalAndDigitalComponents_sku | 10.00         | $21.30           |
      | Smart Path Config | my visa         | bundleWithPhysicalAndDigitalComponents_sku | 21.30         | $21.30           |

  Scenario Outline: Manual refund order fails when refund amount is greater than captured order total
    Given I create an unsaved <PAYMENT_METHOD> payment instrument from order supplying following fields:
      | display-name | <INSTRUMENT_NAME> |
    And I make a purchase with newly created payment instrument for scope mobee with following skus
      | skuCode    | quantity |
      | <SKU_CODE> | 1        |
    And I search and open order editor for the latest order
    When I fill order refund form with following values
      | Available Refund Amount | <AVAILABLE_REFUND>     |
      | Currency Code           | CAD                    |
      | Refund Amount           | <REFUND_AMOUNT>        |
      | Refund Note             | should not be possible |
      | Payment Source          | Manual Refund          |
    Then Refund button should be disabled
    And Refund dialog should display an error message <ERROR_MESSAGE>

    Examples:
      | PAYMENT_METHOD    | INSTRUMENT_NAME | SKU_CODE                                   | AVAILABLE_REFUND | REFUND_AMOUNT | ERROR_MESSAGE                                     |
      | Smart Path Config | my visa         | digital_sku                                | $21.30           | 25.00         | Must be not more than available amount for refund |
      | Smart Path Config | my visa         | bundleWithPhysicalAndDigitalComponents_sku | $21.30           | 25.00         | Must be not more than available amount for refund |

  Scenario Outline: Manual refund order after shipment completion creates Order Notes
    Given I create an unsaved <PAYMENT_METHOD> payment instrument from order supplying following fields:
      | display-name | <INSTRUMENT_NAME> |
    And I make a purchase with newly created payment instrument for scope mobee with following skus
      | skuCode                 | quantity |
      | handsfree_shippable_sku | 1        |
    And I search and open order editor for the latest order
    And I complete the order shipment
    When I create a refund with following values
      | Available Refund Amount | $213.00         |
      | Currency Code           | CAD             |
      | Refund Amount           | <REFUND_AMOUNT> |
      | Refund Note             | <REFUND_NOTE>   |
      | Payment Source          | Manual Refund   |
    Then I should see following note in the Order Notes
      | Originator  | admin         |
      | Description | <REFUND_NOTE> |
    And I should see following note in the Order Notes
      | DescriptionPart1 | Manual refund        |
      | DescriptionPart2 | of $<REFUND_AMOUNT>. |

    Examples:
      | PAYMENT_METHOD    | INSTRUMENT_NAME | REFUND_AMOUNT | REFUND_NOTE  |
      | Smart Path Config | my visa         | 10.00         | paid by cash |

  Scenario Outline: Modify order with manual refund includes refunded amount into modification
    Given I create an unsaved <PAYMENT_METHOD> payment instrument from order supplying following fields:
      | display-name | <INSTRUMENT_NAME> |
    And I make a purchase with newly created payment instrument for scope mobee with following skus
      | skuCode                                    | quantity |
      | bundleWithPhysicalAndDigitalComponents_sku | 1        |
    And I search and open order editor for the latest order
    When I create a refund with following values
      | Available Refund Amount | $21.30          |
      | Currency Code           | CAD             |
      | Refund Amount           | <REFUND_AMOUNT> |
      | Refund Note             | paid by cash    |
      | Payment Source          | Manual Refund   |
    And I modify order shipment line item discount to 10
    Then I should see following order payment transaction in the Payment History
      | Method  | <PAYMENT_METHOD>  |
      | Type    | Modify Reserve    |
      | Details | <INSTRUMENT_NAME> |
      | Status  | Approved          |
      | Amount  | $<MODIFY_AMOUNT>  |

    Examples:
      | PAYMENT_METHOD    | INSTRUMENT_NAME | REFUND_AMOUNT | MODIFY_AMOUNT |
      | Smart Path Config | my visa         | 10.00         | 117.15        |

  Scenario Outline: Manual refund of one of the order shipments and complete another does not affect the charged amount
    Given I create an unsaved <PAYMENT_METHOD> payment instrument from order supplying following fields:
      | display-name | <INSTRUMENT_NAME> |
    And I make a purchase with newly created payment instrument for scope mobee with following skus
      | skuCode                                    | quantity |
      | bundleWithPhysicalAndDigitalComponents_sku | 1        |
    And I search and open order editor for the latest order
    When I create a refund with following values
      | Available Refund Amount | $21.30          |
      | Currency Code           | CAD             |
      | Refund Amount           | <REFUND_AMOUNT> |
      | Refund Note             | paid by cash    |
      | Payment Source          | Manual Refund   |
    And I complete the order shipment
    Then I should see following order payment transaction in the Payment History
      | Method  | <PAYMENT_METHOD>  |
      | Type    | Charge            |
      | Details | <INSTRUMENT_NAME> |
      | Status  | Approved          |
      | Amount  | $<CHARGE_AMOUNT>  |

    Examples:
      | PAYMENT_METHOD    | INSTRUMENT_NAME | REFUND_AMOUNT | CHARGE_AMOUNT |
      | Smart Path Config | my visa         | 10.00         | 127.80        |

  Scenario Outline: Manual Refund when Credit capability is unsupported
    Given I create an unsaved <PAYMENT_METHOD> payment instrument from order supplying following fields:
      | display-name | <INSTRUMENT_NAME> |
    And I make a purchase with newly created payment instrument for scope mobee with following skus
      | skuCode     | quantity |
      | digital_sku | 1        |
    And I search and open order editor for the latest order
    When I create a refund with following values
      | Available Refund Amount | $<REFUND_AMOUNT>        |
      | Currency Code           | CAD                     |
      | Refund Amount           | <REFUND_AMOUNT>         |
      | Refund Note             | <REFUND_NOTE>           |
      | Payment Source          | Original payment source |
    Then Refund should respond with payment error <ERROR>
    And I should NOT see Credit order payment transaction type in the Payment History
    When I create a refund with following values
      | Available Refund Amount | $<REFUND_AMOUNT> |
      | Currency Code           | CAD              |
      | Refund Amount           | <REFUND_AMOUNT>  |
      | Refund Note             | Cash Refund      |
      | Payment Source          | Manual Refund    |
    Then I should see following order payment transaction in the Payment History
      | Method  |                   |
      | Type    | Manual Credit     |
      | Details |                   |
      | Status  | Approved          |
      | Amount  | -$<REFUND_AMOUNT> |

    Examples:
      | PAYMENT_METHOD            | INSTRUMENT_NAME | REFUND_AMOUNT | REFUND_NOTE | ERROR                                            |
      | Credit Unsupported Config | my visa         | 21.30         | refund      | Capability is not supported by payment provider. |

  Scenario Outline: Manual Refund multiple times exceeding the original paid amount after failed credit should not be possible
    Given I go to Payment Configurations
    And I create a new Payment Configuration with following details
      | PROVIDER           | Smart Path              |
      | METHOD             | CARD                    |
      | CONFIGURATION_NAME | <PAYMENT_CONFIGURATION> |
      | DISPLAY_NAME       | <PAYMENT_CONFIGURATION> |
    And I configure the payment configuration properties
      | CREDIT | FAILS |
    And I save the payment configuration
    And I activate the newly created payment configuration
    And I select and save the newly created payment configuration for store MOBEE
    And I have created order payment instrument with the newly created payment configuration
      | display-name | <PAYMENT_INSTRUMENT_NAME> |
    And I make a purchase with newly created payment instrument for scope mobee with following skus
      | skuCode    | quantity |
      | <SKU_CODE> | 1        |
    And I search and open order editor for the latest order
    And I complete the order shipment
    And I fill order refund form with following values
      | Available Refund Amount | <AVAILABLE_REFUND>      |
      | Currency Code           | CAD                     |
      | Refund Amount           | <REFUND_AMOUNT>         |
      | Refund Note             | should fail             |
      | Payment Source          | Original payment source |
    And I cannot complete refund with error message Error occurred when processing payment.
    And I should see following order payment transaction in the Payment History
      | Method  | <PAYMENT_CONFIGURATION>   |
      | Type    | Credit                    |
      | Details | <PAYMENT_INSTRUMENT_NAME> |
      | Status  | Failed                    |
      | Amount  | -$<REFUND_AMOUNT>         |
    And I create a refund with following values
      | Available Refund Amount | <AVAILABLE_REFUND> |
      | Currency Code           | CAD                |
      | Refund Amount           | <REFUND_AMOUNT>    |
      | Refund Note             | paid by cash       |
      | Payment Source          | Manual Refund      |
    And I should see following order payment transaction in the Payment History
      | Type   | Manual Credit     |
      | Status | Approved          |
      | Amount | -$<REFUND_AMOUNT> |
    When I fill order refund form with following values
      | Available Refund Amount | $31.50                 |
      | Currency Code           | CAD                    |
      | Refund Amount           | <REFUND_AMOUNT>        |
      | Refund Note             | should not be possible |
      | Payment Source          | Manual Refund          |
    Then Refund button should be disabled
    And Refund dialog should display an error message <ERROR_MESSAGE>

    Examples:
      | PAYMENT_INSTRUMENT_NAME | PAYMENT_CONFIGURATION | SKU_CODE     | AVAILABLE_REFUND | REFUND_AMOUNT | ERROR_MESSAGE                                     |
      | my visa                 | Credit FAILS          | physical_sku | $131.50          | 100.00        | Must be not more than available amount for refund |