@promotions @catalogpromotions
Feature: Catalog Promotions
  As a shopper, when I trigger a catalog promotion, then I can see the details of the promotion, so I am more likely to complete a purchase.

  Scenario Outline: Retrieve promotions applied to catalog for registered shoppers
    Given I login as a registered shopper
    When I search for item name <ITEM_NAME>
    Then the list of applied catalog promotions contains promotion <PROMOTION>

    Examples:
      | ITEM_NAME               | PROMOTION                           |
      | Incredible Hulk         | RegisteredShopperCatalogPromotion45 |

  Scenario Outline: Unable to Retrieve promotions applied to catalog for registered shoppers when viewing as public shoppers
    Given I login as a public shopper
    When I search for item name <ITEM_NAME>
    Then the list of applied catalog promotions does not contain promotion <PROMOTION>

    Examples:
      | ITEM_NAME               | PROMOTION                           |
      | Incredible Hulk         | RegisteredShopperCatalogPromotion45 |

  Scenario Outline: Retrieve promotions applied to catalog for public shoppers
    Given I login as a public shopper
    When I search for item name <ITEM_NAME>
    Then the list of applied catalog promotions contains promotion <PROMOTION>

    Examples:
      | ITEM_NAME               | PROMOTION             |
      | Guardians of the Galaxy | halfoffselectedmovies |

  Scenario Outline: Purchasing price of item is replaced with the catalog promotion price after adding to cart
    Given I login as a registered shopper
    When I search for item name <ITEM_NAME>
    And I follow links addtocartform
    And I add to cart with quantity of 1
    Then I go to my cart
    And the cart total has amount: 10.99, currency: CAD and display: $10.99

    Examples:
      | ITEM_NAME       |
      | Incredible Hulk |

  Scenario Outline: Purchasing price of item is replaced with the public catalog promotion price after adding to cart
    Given I login as a public shopper
    When I search for item name <ITEM_NAME>
    And I follow links addtocartform
    And I add to cart with quantity of 1
    Then I go to my cart
    And the cart total has amount: 10.00, currency: CAD and display: $10.00

    Examples:
      | ITEM_NAME               |
      | Guardians of the Galaxy |

  Scenario Outline: Purchasing price of item is not replaced with the catalog promotion price after adding to cart when shopper conditions are not
  met
    Given I login as a public shopper
    When I search for item name <ITEM_NAME>
    And I follow links addtocartform
    And I add to cart with quantity of 1
    Then I go to my cart
    And the cart total has amount: 19.99, currency: CAD and display: $19.99

    Examples:
      | ITEM_NAME       |
      | Incredible Hulk |