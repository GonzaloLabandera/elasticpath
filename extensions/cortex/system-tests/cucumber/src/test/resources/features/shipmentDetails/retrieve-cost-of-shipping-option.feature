@shipping @headerAuth

Feature: Shipping Costs

  Background:
    Given I login as a registered shopper

  Scenario Outline: Shipping cost with fixed price calculation
    Given a shipping option <FIXED_PRICE_SHIPPING> has cost of <AMOUNT>
    And I have item with code <ITEM_SKU> in my cart with quantity 1
    When I select a shipping option <FIXED_PRICE_SHIPPING>
    Then the shipping cost has fields amount: <AMOUNT>, currency: <CURRENCY> and display: <DISPLAY_AMOUNT>

    Examples:
      | ITEM_SKU     | FIXED_PRICE_SHIPPING            | AMOUNT | CURRENCY | DISPLAY_AMOUNT |
      | physical_sku | FixedPriceNoPromoShippingOption | 100.00 | CAD      | $100.00        |

  Scenario Outline: Fixed shipping cost with shipping promotion
    Given a shipping promotion Free shipping on FixedPriceWith100PercentOffPromoShippingOption for the shipping option <FIXED_PRICE_SHIPPING>
    And I have item with code <ITEM_SKU> in my cart with quantity 1
    When I select a shipping option <FIXED_PRICE_SHIPPING>
    Then the shipping cost has fields amount: <AMOUNT>, currency: <CURRENCY> and display: <DISPLAY_AMOUNT>

    Examples:
      | ITEM_SKU     | FIXED_PRICE_SHIPPING                           | AMOUNT | CURRENCY | DISPLAY_AMOUNT |
      | physical_sku | FixedPriceWith100PercentOffPromoShippingOption | 0.00   | CAD      | $0.00          |

  Scenario Outline: Shipping cost calculation with percentage of total order
    Given an item with cost of <ITEM_PRICE> and shipping cost is 10 percent of order total
    And I have item with code <ITEM_SKU> in my cart with quantity 1
    When I select a shipping option <PERCENTAGE_SHIPPING_COST>
    Then the shipping cost has fields amount: <AMOUNT>, currency: <CURRENCY> and display: <DISPLAY_AMOUNT>

    Examples:
      | ITEM_SKU     | ITEM_PRICE | PERCENTAGE_SHIPPING_COST | AMOUNT | CURRENCY | DISPLAY_AMOUNT |
      | physical_sku | 25         | CanadaPostExpress        | 2.50   | CAD      | $2.50          |

  Scenario Outline: Shipping cost calculation with unit weight
    And sku <SKU_1> has weight of <WEIGHT_1>
    And sku <SKU_2> has weight of <WEIGHT_2>
    And shipping option <SHIPPING_OPTION> has <SHIPPING_OPTION_VALUE> for <CALCULATION_METHOD>
    When I add following items with quantity to the cart
      | <SKU_1> | <QTY_1> |
      | <SKU_2> | <QTY_2> |
    Then the order shipping option <SHIPPING_OPTION> has cost of <SHIPPING_COST>
    And I select shipping option <SHIPPING_OPTION>
    And I make a purchase
    Then purchase shipping option cost is <SHIPPING_COST>

    Examples:
      | SKU_1                   | QTY_1 | WEIGHT_1 | SKU_2        | QTY_2 | WEIGHT_2 | SHIPPING_OPTION                 | SHIPPING_OPTION_VALUE | CALCULATION_METHOD    | SHIPPING_COST |
      | iphone10                | 2     | 3 KG     | FocUSsku     | 1     | 2.5 KG   | CanadaPostWeightPrice           | $5.50                 | price per unit weight | $46.75        |
      | iphone10                | 1     | 3 KG     | physical_sku | 1     | 0 KG     | CanadaPostWeightPrice           | $5.50                 | price per unit weight | $16.50        |
      | handsfree_shippable_sku | 1     | 0 KG     | physical_sku | 1     | 0 KG     | CanadaPostWeightPrice           | $5.50                 | price per unit weight | $0.00         |
      | iphone10                | 2     | 3 KG     | FocUSsku     | 1     | 2.5 KG   | FixedPriceNoPromoShippingOption | $100.00               | fixed price           | $100.00       |
      | iphone10                | 1     | 3 KG     | physical_sku | 1     | 0 KG     | CanadaPostExpress               | 10%                   | order total           | $80.20        |

