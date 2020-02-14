@regressionTest @customerService @order @orderRefund
Feature: Order Refund

  Background:
    Given I sign in to CM as CSR user
    And I have authenticated as a newly registered shopper

  Scenario Outline: Refund order when refund amount is equal or less than captured order/shipment total
    Given I create an unsaved <PAYMENT_METHOD> payment instrument from order supplying following fields:
      | display-name | <INSTRUMENT_NAME> |
    And I make a purchase with newly created payment instrument for scope mobee with following skus
      | skuCode    | quantity |
      | <SKU_CODE> | 1        |
    And I search and open order editor for the latest order
    When I create a refund with following values
      | Available Refund Amount | <AVAILABLE_REFUND>      |
      | Currency Code           | CAD                     |
      | Refund Amount           | <REFUND_AMOUNT>         |
      | Refund Note             | discount                |
      | Payment Source          | Original payment source |
    Then I should see following order payment transaction in the Payment History
      | Method  | <PAYMENT_METHOD>  |
      | Type    | Credit            |
      | Details | <INSTRUMENT_NAME> |
      | Status  | Approved          |
      | Amount  | -$<REFUND_AMOUNT> |

    Examples:
      | PAYMENT_METHOD    | INSTRUMENT_NAME | SKU_CODE                                   | AVAILABLE_REFUND | REFUND_AMOUNT |
      | Smart Path Config | my visa         | digital_sku                                | $21.30           | 10.00         |
      | Smart Path Config | my visa         | digital_sku                                | $21.30           | 21.30         |
      | Smart Path Config | my visa         | bundleWithPhysicalAndDigitalComponents_sku | $21.30           | 10.00         |
      | Smart Path Config | my visa         | bundleWithPhysicalAndDigitalComponents_sku | $21.30           | 21.30         |

  Scenario Outline: Refund order fails when refund amount is greater than captured order total
    Given I create an unsaved <PAYMENT_METHOD> payment instrument from order supplying following fields:
      | display-name | <INSTRUMENT_NAME> |
    And I make a purchase with newly created payment instrument for scope mobee with following skus
      | skuCode    | quantity |
      | <SKU_CODE> | 1        |
    And I search and open order editor for the latest order
    When I fill order refund form with following values
      | Available Refund Amount | <AVAILABLE_REFUND>      |
      | Currency Code           | CAD                     |
      | Refund Amount           | <REFUND_AMOUNT>         |
      | Refund Note             | should not be possible  |
      | Payment Source          | Original payment source |
    Then Refund button should be disabled
    And Refund dialog should display an error message <ERROR_MESSAGE>

    Examples:
      | PAYMENT_METHOD    | INSTRUMENT_NAME | SKU_CODE                                   | AVAILABLE_REFUND | REFUND_AMOUNT | ERROR_MESSAGE                                     |
      | Smart Path Config | my visa         | digital_sku                                | $21.30           | 25.00         | Must be not more than available amount for refund |
      | Smart Path Config | my visa         | bundleWithPhysicalAndDigitalComponents_sku | $21.30           | 25.00         | Must be not more than available amount for refund |

  Scenario Outline: Refund order after shipment completion creates Order Notes
    Given I create an unsaved <PAYMENT_METHOD> payment instrument from order supplying following fields:
      | display-name | <INSTRUMENT_NAME> |
    And I make a purchase with newly created payment instrument for scope mobee with following skus
      | skuCode                 | quantity |
      | handsfree_shippable_sku | 1        |
    And I search and open order editor for the latest order
    And I complete the order shipment
    When I create a refund with following values
      | Available Refund Amount | $213.00                 |
      | Currency Code           | CAD                     |
      | Refund Amount           | <REFUND_AMOUNT>         |
      | Refund Note             | <REFUND_NOTE>           |
      | Payment Source          | Original payment source |
    Then I should see following note in the Order Notes
      | Originator  | csruser       |
      | Description | <REFUND_NOTE> |
    And I should see following note in the Order Notes
      | DescriptionPart1 | Refund                                    |
      | DescriptionPart2 | of $<REFUND_AMOUNT> to <INSTRUMENT_NAME>. |

    Examples:
      | PAYMENT_METHOD    | INSTRUMENT_NAME | REFUND_AMOUNT | REFUND_NOTE |
      | Smart Path Config | my visa         | 10.00         | discount    |

  Scenario Outline: Modify order with refund includes refunded amount into modification
    Given I create an unsaved <PAYMENT_METHOD> payment instrument from order supplying following fields:
      | display-name | <INSTRUMENT_NAME> |
    And I make a purchase with newly created payment instrument for scope mobee with following skus
      | skuCode                                    | quantity |
      | bundleWithPhysicalAndDigitalComponents_sku | 1        |
    And I search and open order editor for the latest order
    When I create a refund with following values
      | Available Refund Amount | $21.30                  |
      | Currency Code           | CAD                     |
      | Refund Amount           | <REFUND_AMOUNT>         |
      | Refund Note             | discount                |
      | Payment Source          | Original payment source |
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

  Scenario Outline: Refund one of the order shipments and complete another does not affect the charged amount
    Given I create an unsaved <PAYMENT_METHOD> payment instrument from order supplying following fields:
      | display-name | <INSTRUMENT_NAME> |
    And I make a purchase with newly created payment instrument for scope mobee with following skus
      | skuCode                                    | quantity |
      | bundleWithPhysicalAndDigitalComponents_sku | 1        |
    And I search and open order editor for the latest order
    When I create a refund with following values
      | Available Refund Amount | $21.30                  |
      | Currency Code           | CAD                     |
      | Refund Amount           | <REFUND_AMOUNT>         |
      | Refund Note             | discount                |
      | Payment Source          | Original payment source |
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

  Scenario Outline: Refund in split shipment
    Given I create an unsaved <PAYMENT_METHOD> payment instrument from order supplying following fields:
      | display-name | <INSTRUMENT_NAME> |
    When I make a purchase with newly created payment instrument for scope mobee with following skus
      | skuCode      | quantity |
      | physical_sku | 2        |
    And I search and open order editor for the latest order
    And I create a new shipment for sku physical_sku with following values
      | Address         | 555 Elliott Avenue W, Seattle, WA, 98119 |
      | Shipment Method | FedEx Express                            |
    And I complete Payment Authorization with Original payment source payment source
    And I complete the order shipment
    And I create a refund with following values
      | Available Refund Amount | $131.50                 |
      | Currency Code           | CAD                     |
      | Refund Amount           | <REFUND_AMOUNT>         |
      | Refund Note             | <REFUND_NOTE>           |
      | Payment Source          | Original payment source |
    Then I should see following order payment transaction in the Payment History
      | Method  | <PAYMENT_METHOD>  |
      | Type    | Credit            |
      | Details | <INSTRUMENT_NAME> |
      | Status  | Approved          |
      | Amount  | -$<REFUND_AMOUNT> |

    Examples:
      | PAYMENT_METHOD    | INSTRUMENT_NAME | REFUND_AMOUNT | REFUND_NOTE |
      | Smart Path Config | my visa         | 10.00         | discount    |

  Scenario Outline: Refund amount greater than one of charged amount will refund the remaining amount to the remaining charged transaction
    Given I create an unsaved <PAYMENT_METHOD> payment instrument from order supplying following fields:
      | display-name | <INSTRUMENT_NAME> |
    And I make a purchase with newly created payment instrument for scope mobee with following skus
      | skuCode      | quantity |
      | physical_sku | 2        |
    And I search and open order editor for the latest order
    And I create a new shipment for sku physical_sku with following values
      | Address         | 555 Elliott Avenue W, Seattle, WA, 98119 |
      | Shipment Method | FedEx Express                            |
    And I complete Payment Authorization with Original payment source payment source
    And I complete the shipment for shipment ID 1
    And I complete the shipment for shipment ID 2
    When I create a refund with following values
      | Available Refund Amount | $263.00                 |
      | Currency Code           | CAD                     |
      | Refund Amount           | <REFUND_AMOUNT>         |
      | Refund Note             | <REFUND_NOTE>           |
      | Payment Source          | Original payment source |
    Then I should see following order payment transaction in the Payment History
      | Method                      | <PAYMENT_METHOD>  |
      | Type                        | Credit            |
      | Details                     | <INSTRUMENT_NAME> |
      | Status                      | Approved          |
      | Amount                      | -$131.50          |
      | Original payment instrument | Yes               |
    And I should see following order payment transaction in the Payment History
      | Method                      | <PAYMENT_METHOD>  |
      | Type                        | Credit            |
      | Details                     | <INSTRUMENT_NAME> |
      | Status                      | Approved          |
      | Amount                      | -$0.50            |
      | Original payment instrument | Yes               |

    Examples:
      | PAYMENT_METHOD    | INSTRUMENT_NAME | REFUND_AMOUNT | REFUND_NOTE |
      | Smart Path Config | my visa         | 132.00        | refund      |

  Scenario Outline: Refund when Credit capability is unsupported
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
    Then Refund should respond with exact payment error <ERROR>

    Examples:
      | PAYMENT_METHOD            | INSTRUMENT_NAME | REFUND_AMOUNT | REFUND_NOTE | ERROR                                                                             |
      | Credit Unsupported Config | my visa         | 21.30         | refund      | Payment gateway or plugin issue: Capability is not supported by payment provider. |