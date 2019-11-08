@carts @multicarts
Feature: Changing line item quantity (same product) of named cart does not affect another cart

  Background:
    Given I have authenticated as a newly registered shopper
    And I create a new shopping cart with name family

  Scenario Outline: Can't update a named cart lineitem quantity with an invalid quantity
    When I add alien_sku to cart family with quantity 1
    And I attempt to change cart family lineitem quantity of alien_sku to <INVALID_QUANTITY>
    Then the operation is identified as bad request
    And cart family total-quantity is 1

    Examples:
      | INVALID_QUANTITY |
      | -1               |
      | 0.1              |
      | invalidFormat    |
      | 2147483648       |

  Scenario: Can't update lineitem of named cart with insufficient quantity
    When I add sony_bt_sku to cart family with quantity 1
    And I attempt to change cart family lineitem quantity of sony_bt_sku to 11
    Then the operation is identified as bad request
    And Structured error message contains:
      | Item 'sony_bt_sku' only has 10 available but 11 were requested. |
    Then specific cart family has lineitem code sony_bt_sku with quantity of 1

  Scenario: Changing named cart lineitem quantity to 0 deletes the item
    When I add alien_sku to cart family with quantity 1
    And I attempt to change cart family lineitem quantity of alien_sku to 0
    Then cart family total-quantity is 0

  Scenario: Remove lineitem from named cart
    When I add alien_sku to cart family with quantity 1
    And I delete lineitem code alien_sku from specific cart family
    Then cart family total-quantity is 0

  Scenario: Clear named cart
    When I add alien_sku to cart family with quantity 1
    And I add physical_sku to cart family with quantity 2
    And I clear specific cart family
    Then cart family total-quantity is 0

  Scenario: Update lineitem doesn't affect other carts
    When I add item with code alien_sku to my cart with quantity 1
    And I add alien_sku to cart family with quantity 1
    And I attempt to change cart family lineitem quantity of alien_sku to 2
    Then cart family total-quantity is 2
    And the cart total-quantity is 1

  Scenario Outline: Adding promotion doesn't affect other carts
    When I create a new shopping cart with name friends
    And I add <ITEM_CODE> to cart family with quantity 1
    And I add alien_sku to cart friends with quantity 1
    Then the cart family discount amount is 23.74
    And the cart friends discount amount is 0.00
    And the cart friends does not have promotions
    And the cart family has <PROMOTION> promotion

    Examples:
      | ITEM_CODE                                     | PROMOTION              |
      | triggerprodforfiftyoffentirepurchasepromo_sku | FiftyOffEntirePurchase |

  Scenario Outline: Removing promotion doesn't affect other carts
    When I create a new shopping cart with name friends
    And I add <ITEM_CODE> to cart family with quantity 1
    And I add <ITEM_CODE> to cart friends with quantity 1
    Then the cart family discount amount is 23.74
    And the cart friends discount amount is 23.74
    When I delete lineitem code <ITEM_CODE> from specific cart family
    Then the cart family discount amount is 0.00
    And the cart friends discount amount is 23.74

    Examples:
      | ITEM_CODE                                     |
      | triggerprodforfiftyoffentirepurchasepromo_sku |

  Scenario: Check total amount for carts
    When I create a new shopping cart with name friends
    And I add alien_sku to cart friends with quantity 1
    Then the cart friends total has amount: 20, currency: CAD and display: $20.00
    And the cart family total has amount: 0, currency: CAD and display: $0.00

  Scenario: Move item from named cart to wishlist
    When I add physical_sku to cart family with quantity 1
    And I move an item with code physical_sku from cart family to my default wishlist
    Then the HTTP status is OK, created
    And item with code physical_sku is in my default wishlist
    And item with code physical_sku is not found in cart family
