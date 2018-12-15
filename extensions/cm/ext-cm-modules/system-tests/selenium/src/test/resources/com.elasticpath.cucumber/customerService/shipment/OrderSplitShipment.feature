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