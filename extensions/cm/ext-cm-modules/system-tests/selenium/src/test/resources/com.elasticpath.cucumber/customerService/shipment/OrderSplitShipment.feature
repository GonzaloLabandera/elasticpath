@regressionTest @customerService @shipment
Feature: Split Shipment

  Scenario Outline: Split shipment bundle for tax exclusive store
    Given I sign in to CM as CSR user
    And I go to Customer Service
    And I have authenticated as a newly registered shopper
    And I create an unsaved <PAYMENT_METHOD> payment instrument from order supplying following fields:
      | display-name | <PAYMENT_INSTRUMENT_NAME> |
    And I make a purchase with newly created payment instrument for scope mobee with following skus
      | skuCode | quantity |
      | <SKU>   | 1        |
    And I search and open order editor for the latest order
    When I create a new shipment for sku physical_sku with following values
      | Address         | 555 Elliott Avenue W, Seattle, WA, 98119 |
      | Shipment Method | FedEx Express                            |
    And I complete Payment Authorization with Original payment source payment source

    Then I should see following order payment transaction in the Payment History
      | Method  | <PAYMENT_METHOD>          |
      | Type    | Modify Reserve            |
      | Details | <PAYMENT_INSTRUMENT_NAME> |
      | Status  | Approved                  |
      | Amount  | <ORDER_TOTAL>             |
    Then the order balance due is <ORDER_TOTAL>
    Examples:
      | SKU                                 | PAYMENT_METHOD    | PAYMENT_INSTRUMENT_NAME | ORDER_TOTAL |
      | bundleWithMultiplePhysicalItems_sku | Smart Path Config | instrument 1            | $318.19     |

  Scenario Outline: Split Shipment
    Given I have an order for scope <scope> with following skus
      | skuCode      | quantity |
      | <sku-code-1> | 1        |
      | <sku-code-2> | 1        |
    And I sign in to CM as CSR user
    And I search and open order editor for the latest order
    When I create a new shipment for sku <sku-code-2> with following values
      | Address         | 555 Elliott Avenue W, Seattle, WA, 98119 |
      | Shipment Method | FedEx Express                            |
    And I complete Payment Authorization with Original payment source payment source
    Then I should see 2 shipments

    Examples:
      | scope | sku-code-1              | sku-code-2 |
      | mobee | handsfree_shippable_sku | t384lkef   |

  Scenario Outline: Shipment Cost recalculates when split shipment
    Given I have authenticated as a newly registered shopper
    Given I create an unsaved <PAYMENT_METHOD> payment instrument from order supplying following fields:
      | display-name | <PAYMENT_INSTRUMENT_NAME> |
    And I make a purchase with newly created payment instrument for scope mobee with following skus
      | skuCode      | quantity |
      | <sku-code-1> | 1        |
      | <sku-code-2> | 1        |
    And I sign in to CM as CSR user
    And I search and open order editor for the latest order
    When I create a new shipment for sku <sku-code-2> with following values
      | Address         | 555 Elliott Avenue W, Seattle, WA, 98119 |
      | Shipment Method | FedEx Express                            |
    And I complete Payment Authorization with Original payment source payment source
    Then I should see 2 shipments
    And I should see the following Shipment Summary
      | shipment-number | item-sub-total | shipping-cost | shipment-discount | total-before-tax | item-taxes | shipping-taxes | shipment-total |
      | 1               | 25.00          | 100.00        | 0.00              | 125.00           | 0.00       | 6.50           | 131.50         |
      | 2               | 100.00         | 100.00        | 0.00              | 200.00           | 6.50       | 6.50           | 213.00         |
    Then I should see following order payment transaction in the Payment History
      | Method  | <PAYMENT_METHOD>          |
      | Type    | Reserve                   |
      | Details | <PAYMENT_INSTRUMENT_NAME> |
      | Status  | Approved                  |
      | Amount  | <ORDER_TOTAL>             |
    Then I should see following order payment transaction in the Payment History
      | Method  | <PAYMENT_METHOD>          |
      | Type    | Modify Reserve            |
      | Details | <PAYMENT_INSTRUMENT_NAME> |
      | Status  | Approved                  |
      | Amount  | <EDITED_ORDER_TOTAL>      |
    Then the order balance due is <EDITED_ORDER_TOTAL>
    And I can complete the order shipment
    Then I should see following order payment transaction in the Payment History
      | Method  | <PAYMENT_METHOD>          |
      | Type    | Charge                    |
      | Details | <PAYMENT_INSTRUMENT_NAME> |
      | Status  | Approved                  |
      | Amount  | <CHARGED_TOTAL>           |
    Then I should see following order payment transaction in the Payment History
      | Method  | <PAYMENT_METHOD>          |
      | Type    | Reserve                   |
      | Details | <PAYMENT_INSTRUMENT_NAME> |
      | Status  | Approved                  |
      | Amount  | <NEW_RESERVE>             |
    Then the order balance due is <NEW_RESERVE>
    Examples:
      | sku-code-1   | sku-code-2              | PAYMENT_METHOD    | PAYMENT_INSTRUMENT_NAME | ORDER_TOTAL | EDITED_ORDER_TOTAL | CHARGED_TOTAL | NEW_RESERVE |
      | physical_sku | handsfree_shippable_sku | Smart Path Config | instrument 1            | $238.00     | $344.50            | $131.50       | $213.00     |

  Scenario Outline: Split shipment for shipping method based on unit weight
    Given I login as a registered shopper
    And sku <sku-code-1> has weight of <weight-1>
    And sku <sku-code-2> has weight of <weight-2>
    And shipping option Canada Post Unit Weight Price has $5.50 for price per unit weight
    And I add following items with quantity to the cart
      | <sku-code-1> | <qty-1> |
      | <sku-code-2> | <qty-2> |
    And I select shipping option <shipping-option>
    And I make a purchase
    And I sign in to CM as CSR user
    And I search and open order editor for the latest order
    Then I should see the following Shipment Summary
      | shipment-number | item-sub-total | shipping-cost | shipment-discount | total-before-tax | item-taxes | shipping-taxes | shipment-total |
      | 1               | 877.00         | 30.25         | 0.00              | 907.25           | 12.00      | 3.63           | 922.88         |
    When I create a new shipment for sku <sku-code-2> with following values
      | Address         | 1111 EP Road, Vancouver, BC, v7v7v7 |
      | Shipment Method | Canada Post Unit Weight Price       |
    And I complete Payment Authorization with Original payment source payment source
    Then I should see the following Shipment Summary
      | shipment-number | item-sub-total | shipping-cost | shipment-discount | total-before-tax | item-taxes | shipping-taxes | shipment-total |
      | 1               | 777.00         | 16.50         | 0.00              | 793.50           | 0.00       | 1.99           | 795.49         |
      | 2               | 100.00         | 13.75         | 0.00              | 113.75           | 12.00      | 1.65           | 127.40         |

    Examples:
      | sku-code-1 | qty-1 | weight-1 | sku-code-2 | qty-2 | weight-2 | shipping-option       |
      | iphone10   | 1     | 3.0 KG   | FocUSsku   | 1     | 2.5 KG   | CanadaPostWeightPrice |

  Scenario Outline: Shipping cost calculation with unit weight, order total percentage and fixed price
    Given I login as a registered shopper
    And sku <sku-code-1> has weight of <weight-1>
    And sku <sku-code-2> has weight of <weight-2>
    And shipping option Fixed Price No Promo Shipping Option has $100.00 for fixed price
    And shipping option Canada Post Unit Weight Price has $5.50 for price per unit weight
    And shipping option Canada Post Express has 10% for order total
    And shipping option Canada Post 2 days has 5% for order total
    And I add following items with quantity to the cart
      | <sku-code-1> | <qty-1> |
      | <sku-code-2> | <qty-2> |
    And I select shipping option <shipping-option>
    When I make a purchase
    And I sign in to CM as CSR user
    And I search and open order editor for the latest order
    Then I should see the following Shipment Summary
      | shipment-number | item-sub-total | shipping-cost | shipment-discount | total-before-tax | item-taxes | shipping-taxes | shipment-total |
      | 1               | 802.00         | 100.00        | 0.00              | 902.00           | 0.00       | 12.00          | 914.00         |
    When I create a new shipment for sku <sku-code-2> with following values
      | Address         | 1111 EP Road, Vancouver, BC, v7v7v7 |
      | Shipment Method | Canada Post Unit Weight Price       |
    And I save my changes
    Then I should see the following Shipment Summary
      | shipment-number | item-sub-total | shipping-cost | shipment-discount | total-before-tax | item-taxes | shipping-taxes | shipment-total |
      | 1               | 777.00         | 100.00        | 0.00              | 877.00           | 0.00       | 12.00          | 889.00         |
      | 2               | 25.00          | 0.00          | 0.00              | 25.00            | 0.00       | 0.00           | 25.00          |
    When I change the shipment number 1 Shipping Method to the following
      | Shipping Method | Canada Post Express |
    And I complete Payment Authorization with Original payment source payment source
    Then I should see the following Shipment Summary
      | shipment-number | item-sub-total | shipping-cost | shipment-discount | total-before-tax | item-taxes | shipping-taxes | shipment-total |
      | 1               | 777.00         | 77.70         | 0.00              | 854.70           | 0.00       | 9.33           | 864.03         |
    When I change the shipment number 2 Shipping Method to the following
      | Shipping Method | Canada Post 2 days |
    And I complete Payment Authorization with Original payment source payment source
    Then I should see the following Shipment Summary
      | shipment-number | item-sub-total | shipping-cost | shipment-discount | total-before-tax | item-taxes | shipping-taxes | shipment-total |
      | 2               | 25.00          | 1.25          | 0.00              | 26.25            | 0.00       | 0.15           | 26.40          |
    When I change the shipment number 1 Shipping Method to the following
      | Shipping Method | Canada Post Unit Weight Price |
    And I complete Payment Authorization with Original payment source payment source
    Then I should see the following Shipment Summary
      | shipment-number | item-sub-total | shipping-cost | shipment-discount | total-before-tax | item-taxes | shipping-taxes | shipment-total |
      | 1               | 777.00         | 16.50         | 0.00              | 793.50           | 0.00       | 1.99           | 795.49         |

    Examples:
      | sku-code-1 | qty-1 | weight-1 | sku-code-2   | qty-2 | weight-2 | shipping-option                 |
      | iphone10   | 1     | 3.0 KG   | physical_sku | 1     | 0.0 KG   | FixedPriceNoPromoShippingOption |

  Scenario Outline: Modify order by split shipment with cart promotion for tax-inclusive store
    Given I have authenticated as a newly registered shopper
    When I am shopping in locale en with currency EUR
    And I create an order with GB address for scope mobee with following skus
      | skuCode     | quantity |
      | sony_bt_sku | 1        |
    And I sign in to CM as CSR user
    And I search and open order editor for the latest order
    And I add sku physical_sku to the shipment with following values
      | Price List Name | Mobee EUR Shopper Price List |
    When I create a new shipment for sku physical_sku with following values
      | Address         | 111 Main Street, London, 12345 |
      | Shipment Method | RoyalMailExpress               |
    And I complete Payment Authorization with Original payment source payment source
    Then I should see following order payment transaction in the Payment History
      | Method | <PAYMENT_METHOD> |
      | Type   | Modify Reserve   |
      | Status | Approved         |
      | Amount | <ORDER_TOTAL>    |

    Examples:
      | PAYMENT_METHOD    | ORDER_TOTAL |
      | Smart Path Config | €37.39      |

  Scenario Outline: Modify order by remove shipment with cart promotion for tax-inclusive store
    Given item <SKU> triggers the cart total promotion 10% off Cart Total
    And I have authenticated as a newly registered shopper
    And I am shopping in locale en with currency EUR
    And I create an order with GB address for scope mobee with following skus
      | skuCode | quantity |
      | <SKU>  | 1        |
    And I sign in to CM as CSR user
    And I search and open order editor for the latest order
    And I add sku physical_sku to the shipment with following values
      | Price List Name | Mobee EUR Shopper Price List |
    And I create a new shipment for sku physical_sku with following values
      | Address         | 111 Main Street, London, 12345 |
      | Shipment Method | RoyalMailExpress               |
    And I complete Payment Authorization with Original payment source payment source
    When I cancel shipment by shipment number 2
    Then I should see following order payment transaction in the Payment History
      | Method | <PAYMENT_METHOD> |
      | Type   | Modify Reserve   |
      | Status | Approved         |
      | Amount | <ORDER_TOTAL>    |

    Examples:
      | SKU         | PAYMENT_METHOD    | ORDER_TOTAL |
      | sony_bt_sku | Smart Path Config | €22.67      |

  Scenario Outline: Complete multiple split shipments without Modify Reserve capability
    Given I sign in to CM as admin user
    And I have authenticated as a newly registered shopper
    And I create an unsaved <PAYMENT_METHOD> payment instrument from order supplying following fields:
      | display-name | <PAYMENT_INSTRUMENT_NAME> |
    And I make a purchase with newly created payment instrument for scope mobee with following skus
      | skuCode | quantity |
      | <SKU>   | 2        |
    And I search and open order editor for the latest order
    When I create a new shipment for sku physical_sku with following values
      | Address         | 555 Elliott Avenue W, Seattle, WA, 98119 |
      | Shipment Method | FedEx Express                            |
    And I complete Payment Authorization with Original payment source payment source
    And I complete the shipment for shipment ID 1
    Then I should NOT see following order payment transaction in the Payment History
      | Type   | Charge |
      | Amount | $0.00  |
    And Payment Summary should have the following totals
      | Ordered     | $263.00 |
      | Paid        | $131.50 |
      | Balance Due | $131.50 |
    When I complete the shipment for shipment ID 2
    Then the order balance due is $0.00
    And Payment Summary should have the following totals
      | Ordered     | $263.00 |
      | Paid        | $263.00 |
      | Balance Due | $0.00   |

    Examples:
      | SKU          | PAYMENT_METHOD            | PAYMENT_INSTRUMENT_NAME |
      | physical_sku | Modify Unsupported Config | modify unsupported      |

  Scenario Outline: Split Shipment for free physical product
    Given I have an order for scope <scope> with following skus
      | skuCode      | quantity |
      | <sku-code-1> | 3        |
    And I sign in to CM as CSR user
    And I search and open order editor for the latest order
    And I split a shipment for sku <sku-code-1> for shipment number 1 with following values
      | Address         | 555 Elliott Avenue W, Seattle, WA, 98119 |
      | Shipment Method | FedEx Express                            |
    And I complete Payment Authorization with Original payment source payment source
    Then I should see 2 shipments

    Examples:
      | scope | sku-code-1              |
      | mobee | physicalFreeProduct_sku |