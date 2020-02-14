@regressionTest @customerService @order @cancelOrder
Feature: Cancel Order

  Background:
    Given I sign in to CM as CSR user
    And I have authenticated as a newly registered shopper

  Scenario: Order status is cancelled when cancel order
    Given I create an unsaved Smart Path Config payment instrument from order supplying following fields:
      | display-name | unsaved instrument |
    And I make a purchase with newly created payment instrument for scope mobee with following skus
      | skuCode                 | quantity |
      | handsfree_shippable_sku | 1        |
    And I search and open order editor for the latest order
    When I cancel the order
    Then the order status should be Cancelled
    And the order balance due is $0.00

  Scenario Outline: Cancel Reserve approved when order cancelled
    Given I create an unsaved Happy Path Config payment instrument from order supplying following fields:
      | PIC Field A  | abc                       |
      | PIC Field B  | xyz                       |
      | display-name | <PAYMENT_INSTRUMENT_NAME> |
    And I make a purchase with newly created payment instrument for scope mobee with following skus
      | skuCode                 | quantity |
      | handsfree_shippable_sku | 1        |
    And I search and open order editor for the latest order
    When I cancel the order
    Then I should see following order payment transaction in the Payment History
      | Method  | <PAYMENT_METHOD>          |
      | Type    | Cancel Reserve            |
      | Details | <PAYMENT_INSTRUMENT_NAME> |
      | Status  | Approved                  |
      | Amount  | <ORDER_TOTAL>             |

    Examples:
      | PAYMENT_METHOD    | PAYMENT_INSTRUMENT_NAME | ORDER_TOTAL |
      | Happy Path Config | my visa                 | $213.00     |

  Scenario Outline: All shipment reservations are cancelled when order is cancelled
    Given I have an order for scope <scope> with following skus
      | skuCode    | quantity |
      | <sku-code> | 2        |
    And I go to Customer Service
    And I search and open order editor for the latest order
    And I create a new shipment for sku <sku-code> with following values
      | Address         | 555 Elliott Avenue W, Seattle, WA, 98119 |
      | Shipment Method | FedEx Express                            |
    And I complete Payment Authorization with Original payment source payment source
    When I cancel the order
    Then I should see following order payment transaction in the Payment History
      | Type   | Cancel Reserve |
      | Status | Approved       |
      | Amount | <order-total>  |

    Examples:
      | scope | sku-code     | order-total |
      | mobee | physical_sku | $263.00     |
