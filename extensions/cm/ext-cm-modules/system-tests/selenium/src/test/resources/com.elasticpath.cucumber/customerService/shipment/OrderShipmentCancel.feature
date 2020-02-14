@regressionTest @customerService @shipment
Feature: Cancel Shipment

  Background:
    Given I have authenticated as a newly registered shopper
    And I sign in to CM as CSR user

  Scenario: Cancel single shipment cancels the order
    Given I create an unsaved Smart Path Config payment instrument from order supplying following fields:
      | display-name | unsaved instrument |
    And I make a purchase with newly created payment instrument for scope mobee with following skus
      | skuCode                 | quantity |
      | handsfree_shippable_sku | 1        |
    And I search and open order editor for the latest order
    When I cancel shipment by shipment number 1
    Then the shipment status should be Cancelled
    And the order status should be Cancelled
    And the order balance due is $0.00
    And Payment Summary should have the following totals
      | Ordered     | $213.00 |
      | Paid        | $0.00   |
      | Balance Due | $0.00   |

  Scenario Outline: Cancel Reserve approved when cancel shipment
    Given I create an unsaved Happy Path Config payment instrument from order supplying following fields:
      | PIC Field A  | abc                       |
      | PIC Field B  | xyz                       |
      | display-name | <PAYMENT_INSTRUMENT_NAME> |
    And I make a purchase with newly created payment instrument for scope mobee with following skus
      | skuCode                 | quantity |
      | handsfree_shippable_sku | 1        |
    And I search and open order editor for the latest order
    When I cancel shipment by shipment number 1
    Then I should see following order payment transaction in the Payment History
      | Method  | <PAYMENT_METHOD>          |
      | Type    | Cancel Reserve            |
      | Details | <PAYMENT_INSTRUMENT_NAME> |
      | Status  | Approved                  |
      | Amount  | <ORDER_TOTAL>             |
    And Payment Summary should have the following totals
      | Ordered     | <ORDER_TOTAL> |
      | Paid        | $0.00         |
      | Balance Due | $0.00         |

    Examples:
      | PAYMENT_METHOD    | PAYMENT_INSTRUMENT_NAME | ORDER_TOTAL |
      | Happy Path Config | my visa                 | $213.00     |

  Scenario Outline: Cancel Reserve failed for cancel shipment when using Cancel Fails Config plugin
    Given I create an unsaved Cancel Fails Config payment instrument from order supplying following fields:
      | display-name | <PAYMENT_INSTRUMENT_NAME> |
    And I make a purchase with newly created payment instrument for scope mobee with following skus
      | skuCode                 | quantity |
      | handsfree_shippable_sku | 1        |
    And I search and open order editor for the latest order
    When I cancel shipment by shipment number 1
    Then I should see following order payment transaction in the Payment History
      | Method  | <PAYMENT_METHOD>          |
      | Type    | Cancel Reserve            |
      | Details | <PAYMENT_INSTRUMENT_NAME> |
      | Status  | Failed                    |
      | Amount  | <ORDER_TOTAL>             |
    And Payment Summary should have the following totals
      | Ordered     | <ORDER_TOTAL> |
      | Paid        | $0.00         |
      | Balance Due | $0.00         |
    And the order balance due is $0.00

    Examples:
      | PAYMENT_METHOD      | PAYMENT_INSTRUMENT_NAME | ORDER_TOTAL |
      | Cancel Fails Config | my visa                 | $213.00     |

  Scenario Outline: Cancel Reserve skipped when using Cancel Unsupported Config plugin
    Given I create an unsaved Cancel Unsupported Config payment instrument from order supplying following fields:
      | display-name | <PAYMENT_INSTRUMENT_NAME> |
    And I make a purchase with newly created payment instrument for scope mobee with following skus
      | skuCode                 | quantity |
      | handsfree_shippable_sku | 1        |
    And I search and open order editor for the latest order
    When I cancel shipment by shipment number 1
    And I click on Show Skipped Payment Events
    Then I should see following order payment transaction in the Payment History with skipped events
      | Method  | <PAYMENT_METHOD>          |
      | Type    | Cancel Reserve            |
      | Details | <PAYMENT_INSTRUMENT_NAME> |
      | Status  | Skipped                   |
      | Amount  | <ORDER_TOTAL>             |
    And Payment Summary should have the following totals
      | Ordered     | <ORDER_TOTAL> |
      | Paid        | $0.00         |
      | Balance Due | $0.00         |
    And the order balance due is $0.00

    Examples:
      | PAYMENT_METHOD            | PAYMENT_INSTRUMENT_NAME | ORDER_TOTAL |
      | Cancel Unsupported Config | my visa                 | $213.00     |

  Scenario Outline: Cancel order shipment recalculates the Reserve amount
    Given I create an unsaved <PAYMENT_METHOD> payment instrument from order supplying following fields:
      | display-name | <PAYMENT_INSTRUMENT_NAME> |
    And I make a purchase with newly created payment instrument for scope mobee with following skus
      | skuCode      | quantity |
      | physical_sku | 4        |
    And I search and open order editor for the latest order
    And I split a shipment for sku physical_sku for shipment number 1 with following values
      | Address         | 555 Elliott Avenue W, Seattle, WA, 98119 |
      | Shipment Method | FedEx Express                            |
    And I complete Payment Authorization with Original payment source payment source
    And I split a shipment for sku physical_sku for shipment number 2 with following values
      | Address         | 555 Elliott Avenue W, Seattle, WA, 98119 |
      | Shipment Method | FedEx Express                            |
    And I complete Payment Authorization with Original payment source payment source
    And I complete the shipment for shipment ID 1
    When I cancel shipment by shipment number 2
    And I split a shipment for sku physical_sku for shipment number 3 with following values
      | Address         | 555 Elliott Avenue W, Seattle, WA, 98119 |
      | Shipment Method | FedEx Express                            |
    And I complete Payment Authorization with Original payment source payment source
    Then I should see following order payment transaction in the Payment History
      | Method  | <PAYMENT_METHOD>          |
      | Type    | Modify Reserve            |
      | Details | <PAYMENT_INSTRUMENT_NAME> |
      | Status  | Approved                  |
      | Amount  | $263.00                   |
    And Payment Summary should have the following totals
      | Ordered     | <ORDER_TOTAL> |
      | Paid        | $131.50       |
      | Balance Due | $263.00       |

    Examples:
      | PAYMENT_METHOD    | PAYMENT_INSTRUMENT_NAME | ORDER_TOTAL |
      | Smart Path Config | my visa                 | $526.00     |

  Scenario Outline: Cancel all shipments in order
    Given I create an unsaved Happy Path Config payment instrument from order supplying following fields:
      | PIC Field A  | abc                       |
      | PIC Field B  | xyz                       |
      | display-name | <PAYMENT_INSTRUMENT_NAME> |
    And I make a purchase with newly created payment instrument for scope mobee with following skus
      | skuCode      | quantity |
      | physical_sku | 2        |
    And I search and open order editor for the latest order
    And I split a shipment for sku physical_sku for shipment number 1 with following values
      | Address         | 555 Elliott Avenue W, Seattle, WA, 98119 |
      | Shipment Method | FedEx Express                            |
    And I complete Payment Authorization with Original payment source payment source
    When I cancel shipment by shipment number 1
    When I cancel shipment by shipment number 2
    Then I should see following order payment transaction in the Payment History
      | Method  | <PAYMENT_METHOD>          |
      | Type    | Cancel Reserve            |
      | Details | <PAYMENT_INSTRUMENT_NAME> |
      | Status  | Approved                  |
      | Amount  | <SHIPMENT_TOTAL>          |
    And Payment Summary should have the following totals
      | Ordered     | <ORDER_TOTAL> |
      | Paid        | $0.00         |
      | Balance Due | $0.00         |

    Examples:
      | PAYMENT_METHOD    | PAYMENT_INSTRUMENT_NAME | SHIPMENT_TOTAL | ORDER_TOTAL |
      | Happy Path Config | my visa                 | $131.50        | $263.00     |