@regressionTest @customerService @order @preOrder
Feature: Pre-order purchase

  Background:
    Given I sign in to CM as admin user
    And I have authenticated as a newly registered shopper

  Scenario Outline: Purchase pre-order physical item should reserve full amount
    Given I create an unsaved Smart Path Config payment instrument from order supplying following fields:
      | display-name | <PAYMENT_INSTRUMENT_NAME> |
    And I make a purchase with newly created payment instrument for scope mobee with following skus
      | skuCode                     | quantity |
      | physicalPreOrderProduct_sku | 1        |
    When I search and open order editor for the latest order
    Then I should see following order payment transaction in the Payment History
      | Type    | Reserve                   |
      | Status  | Approved                  |
      | Details | <PAYMENT_INSTRUMENT_NAME> |
      | Amount  | <AMOUNT>                  |

    Examples:
      | PAYMENT_INSTRUMENT_NAME | AMOUNT  |
      | unsaved instrument      | $133.13 |

  Scenario Outline: Cannot release shipment of pre-order before inventory restocking
    Given I create an unsaved Smart Path Config payment instrument from order supplying following fields:
      | display-name | <PAYMENT_INSTRUMENT_NAME> |
    And I make a purchase with newly created payment instrument for scope mobee with following skus
      | skuCode                     | quantity |
      | physicalPreOrderProduct_sku | 1        |
    When I search and open order editor for the latest order
    Then Release shipment button is disabled

    Examples:
      | PAYMENT_INSTRUMENT_NAME |
      | unsaved instrument      |

  Scenario Outline: Pre-order is charged when inventory is restocked
    Given I create an unsaved <PAYMENT_METHOD> payment instrument from order supplying following fields:
      | display-name | <PAYMENT_INSTRUMENT_NAME> |
    And I make a purchase with newly created payment instrument for scope mobee with following skus
      | skuCode       | quantity |
      | <PRODUCT_SKU> | 1        |
    And I make one unit of sku <PRODUCT_SKU> available in the stock
    And I search and open order editor for the latest order
    When I complete the order shipment
    Then I should see following order payment transaction in the Payment History
      | Method  | <PAYMENT_METHOD>          |
      | Type    | Charge                    |
      | Details | <PAYMENT_INSTRUMENT_NAME> |
      | Status  | Approved                  |
      | Amount  | <AMOUNT>                  |

    Examples:
      | PAYMENT_METHOD    | PAYMENT_INSTRUMENT_NAME | PRODUCT_SKU                 | AMOUNT  |
      | Smart Path Config | unsaved instrument      | physicalPreOrderProduct_sku | $133.13 |
