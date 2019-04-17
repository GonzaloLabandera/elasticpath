@regressionTest @customerService @shipment
Feature: Split Shipment

  Scenario Outline: Split Shipment
    Given I have an order for scope <scope> with following skus
      | skuCode      | quantity |
      | <sku-code-1> | 1        |
      | <sku-code-2> | 1        |
    And I sign in to CM as CSR user
    And I go to Customer Service
    And I search and open order editor for the latest order
    And I select Details tab in the Order Editor
    When I create a new shipment for sku <sku-code-2> with following values
      | Address         | 555 Elliott Avenue W, Seattle, WA, 98119 |
      | Shipment Method | FedEx Express                            |
      | Payment Source  | testTokenDisplayName                     |
    Then I should see 2 shipments

    Examples:
      | scope | sku-code-1              | sku-code-2 |
      | mobee | handsfree_shippable_sku | t384lkef   |

  Scenario Outline: Shipment Cost recalculates when split shipment
    Given I authenticate as a registered user harry.potter@elasticpath.com for scope mobee to create an order with following sku
      | skuCode      | quantity |
      | <sku-code-1> | 1        |
      | <sku-code-2> | 1        |
    And I sign in to CM as CSR user
    And I go to Customer Service
    And I search and open order editor for the latest order
    And I select Details tab in the Order Editor
    When I create a new shipment for sku <sku-code-2> with following values
      | Address         | 4567 BumbleBee Dr Unit 80, Corte Madera, CA, 94727 |
      | Shipment Method | FedEx Express                                      |
      | Payment Source  | test-token                                         |
    Then I should see 2 shipments
    And I should see the following Shipment Summary
      | shipment-number | item-sub-total | shipping-cost | shipment-discount | total-before-tax | item-taxes | shipping-taxes | shipment-total |
      | 1               | 25.00          | 1.25          | 0.00              | 26.25            | 0.00       | 0.15           | 26.40          |
      | 2               | 100.00         | 100.00        | 0.00              | 200.00           | 6.00       | 6.00           | 212.00         |

    Examples:
      | sku-code-1   | sku-code-2              |
      | physical_sku | handsfree_shippable_sku |

  Scenario Outline: Split Shipment cart discount shared evenly
    Given there is a 30 percent off cart subtotal coupon <coupon> and the following products
      | sku-code     | purchase-price |
      | <sku-code-1> | 25.00          |
      | <sku-code-2> | 150.00         |
    And I create an order for scope MOBEE with coupon <coupon> for following sku
      | skuCode      | quantity |
      | <sku-code-1> | 1        |
      | <sku-code-2> | 1        |
    And I sign in to CM as CSR user
    And I go to Customer Service
    And I search and open order editor for the latest order
    And I select Details tab in the Order Editor
    When I create a new shipment for sku <sku-code-2> with following values
      | Address         | 555 Elliott Avenue W, Seattle, WA, 98119 |
      | Shipment Method | FedEx Express                            |
      | Payment Source  | testTokenDisplayName                     |
    Then I should see the following Shipment Summary
      | shipment-number | item-sub-total | shipping-cost | shipment-discount | total-before-tax | item-taxes | shipping-taxes | shipment-total |
      | 1               | 25.00          | 100.00        | 7.50              | 117.50           | 0.00       | 6.50           | 124.00         |
      | 2               | 100.00         | 100.00        | 30.00             | 170.00           | 4.55       | 6.50           | 181.05         |

    Examples:
      | coupon      | sku-code-1   | sku-code-2 |
      | blackfriday | physical_sku | FocUSsku   |

  Scenario Outline: Split shipment catalog discount only applies to specific item
    Given there is a 50 percent off selected catalog item coupon <coupon> and the following products
      | sku-code     | purchase-price |
      | <sku-code-1> | 25.00          |
      | <sku-code-2> | 19.99          |
    Given I have an order for scope MOBEE with following sku
      | skuCode      | quantity |
      | <sku-code-1> | 1        |
      | <sku-code-2> | 1        |
    And I sign in to CM as CSR user
    And I go to Customer Service
    And I search and open order editor for the latest order
    And I select Details tab in the Order Editor
    When I create a new shipment for sku <sku-code-2> with following values
      | Address         | 555 Elliott Avenue W, Seattle, WA, 98119 |
      | Shipment Method | FedEx Express                            |
      | Payment Source  | testTokenDisplayName                     |
    Then I should see the following Shipment Summary
      | shipment-number | item-sub-total | shipping-cost | shipment-discount | total-before-tax | item-taxes | shipping-taxes | shipment-total |
      | 1               | 25.00          | 100.00        | 0.00              | 125.00           | 0.00       | 6.50           | 131.50         |
      | 2               | 10.00          | 100.00        | 0.00              | 110.00           | 0.65       | 6.50           | 117.15         |

    Examples:
      | coupon               | sku-code-1   | sku-code-2    |
      | halfofselectedmovies | physical_sku | guardians_sku |

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
    And I go to Customer Service
    And I search and open order editor for the latest order
    When I select Details tab in the Order Editor
    Then I should see the following Shipment Summary
      | shipment-number | item-sub-total | shipping-cost | shipment-discount | total-before-tax | item-taxes | shipping-taxes | shipment-total |
      | 1               | 877.00         | 30.25         | 0.00              | 907.25           | 12.00      | 3.63           | 922.88         |
    When I create a new shipment for sku <sku-code-2> with following values
      | Address         | 1111 EP Road, Vancouver, BC, v7v7v7 |
      | Shipment Method | Canada Post Unit Weight Price       |
      | Payment Source  | test-token                          |
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
    And I make a purchase
    And I sign in to CM as CSR user
    And I go to Customer Service
    And I search and open order editor for the latest order
    When I select Details tab in the Order Editor
    Then I should see the following Shipment Summary
      | shipment-number | item-sub-total | shipping-cost | shipment-discount | total-before-tax | item-taxes | shipping-taxes | shipment-total |
      | 1               | 802.00         | 100.00        | 0.00              | 902.00           | 0.00       | 12.00          | 914.00         |
    When I create a new shipment for sku <sku-code-2> with following values
      | Address         | 1111 EP Road, Vancouver, BC, v7v7v7 |
      | Shipment Method | Canada Post Unit Weight Price       |
      | Payment Source  | test-token                          |
    Then I should see the following Shipment Summary
      | shipment-number | item-sub-total | shipping-cost | shipment-discount | total-before-tax | item-taxes | shipping-taxes | shipment-total |
      | 1               | 777.00         | 100.00        | 0.00              | 877.00           | 0.00       | 12.00          | 889.00         |
      | 2               | 25.00          | 0.00          | 0.00              | 25.00            | 0.00       | 0.00           | 25.00          |
    When I change the shipment number 1 Shipping Method to Canada Post Express without authorizing payment
    Then I should see the following Shipment Summary
      | shipment-number | item-sub-total | shipping-cost | shipment-discount | total-before-tax | item-taxes | shipping-taxes | shipment-total |
      | 1               | 777.00         | 77.70         | 0.00              | 854.70           | 0.00       | 9.33           | 864.03         |
    When I change the shipment number 2 Shipping Method to the following
      | Shipping Method | Canada Post 2 days |
      | Payment Source  | test-token         |
    Then I should see the following Shipment Summary
      | shipment-number | item-sub-total | shipping-cost | shipment-discount | total-before-tax | item-taxes | shipping-taxes | shipment-total |
      | 2               | 25.00          | 1.25          | 0.00              | 26.25            | 0.00       | 0.15           | 26.40          |
    When I change the shipment number 1 Shipping Method to Canada Post Unit Weight Price without authorizing payment
    Then I should see the following Shipment Summary
      | shipment-number | item-sub-total | shipping-cost | shipment-discount | total-before-tax | item-taxes | shipping-taxes | shipment-total |
      | 1               | 777.00         | 16.50         | 0.00              | 793.50           | 0.00       | 1.99           | 795.49         |

    Examples:
      | sku-code-1 | qty-1 | weight-1 | sku-code-2   | qty-2 | weight-2 | shipping-option                 |
      | iphone10   | 1     | 3.0 KG   | physical_sku | 1     | 0.0 KG   | FixedPriceNoPromoShippingOption |
