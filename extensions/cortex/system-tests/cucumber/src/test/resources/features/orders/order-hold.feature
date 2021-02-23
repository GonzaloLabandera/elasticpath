@orderHold
Feature: Verify orders are placed on hold

  Background:
    Given I login as a registered shopper

  @enableOrderHold
  Scenario Outline: Verify order is placed on hold - physical item
    When I add item <ITEM_NAME> to the cart with quantity 1
    And I select shipping option CanadaPostExpress
    And I make a purchase
    Then the purchase status is <PURCHASE_STATUS>

    Examples:
      | ITEM_NAME       | PURCHASE_STATUS |
      | physicalProduct | ON_HOLD         |

  @enableOrderHold
  Scenario Outline: Verify order is placed on hold - non shippable item
    When I add item <ITEM_NAME> to the cart with quantity 1
    And I make a purchase
    Then the purchase status is <PURCHASE_STATUS>

    Examples:
      | ITEM_NAME      | PURCHASE_STATUS |
      | digitalProduct | ON_HOLD         |

  @enableOrderHold
  Scenario Outline: Verify order is placed on hold - non shippable and physical items
    When I add item <ITEM_1> to the cart with quantity 1
    And I add item <ITEM_2> to the cart with quantity 1
    And I select shipping option CanadaPostExpress
    And I make a purchase
    Then the purchase status is <PURCHASE_STATUS>

    Examples:
      | ITEM_1          | ITEM_2         | PURCHASE_STATUS |
      | physicalProduct | digitalProduct | ON_HOLD         |
      | physicalProduct | Phone Plan     | ON_HOLD         |