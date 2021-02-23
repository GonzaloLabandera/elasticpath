@permissions
Feature: Buyer role for account

  Background:
    Given I authenticate with BUYER username usertest3@elasticpath.com and password password and role REGISTERED in scope mobee
    And I add X-Ep-Account-Shared-Id header BuyerRole@elasticpath.com
    And I clear the cart

  Scenario Outline: Account gets role BUYER from associations and has access to cart.
    When I add the following SKU codes and their quantities to the cart
      | code          | quantity       |
      | <ITEM_1_CODE> | <ITEM_1_QTY_1> |
    Then the HTTP status code is 201
    And the cart total-quantity is <TOTAL_QTY>
    And the number of cart lineitems is <NUM_LINE_ITEMS>
    And the cart lineitem for item code <ITEM_1_CODE> has quantity of <ITEM_1_QTY>
    Examples:
      | ITEM_1_CODE |  ITEM_1_QTY_1 | ITEM_1_QTY | TOTAL_QTY | NUM_LINE_ITEMS |
      | alien_sku   | 1            |  1          | 1         | 1              |

  Scenario: Account gets role BUYER from associations and has access to prices.
    Given an item Product With No Discounts exists in my catalog
    When I view the item price
    Then the list-price has fields amount: 10.00, currency: CAD and display: $10.00
    And the purchase-price has fields amount: 10.00, currency: CAD and display: $10.00

  Scenario Outline: Account gets role BUYER from associations and has access to discount.
    Given item <ITEM_SKU> triggers the cart total discount promotion <PROMOTION_DISPLAY_NAME>
    And item <ITEM_SKU> has a purchase price of $47.47
    When I add item with code <ITEM_SKU> to my cart
    Then the cart discount amount is $23.74

    Examples:
      | ITEM_SKU                                      | PROMOTION_DISPLAY_NAME |
      | triggerprodforfiftyoffentirepurchasepromo_sku | FiftyOffEntirePurchase |

  Scenario Outline: Account gets role BUYER from associations and has access to total.
    Given I add item <ITEM_NAME> to the cart
    When I retrieve the cart total
    Then the cost fields has amount: <AMOUNT>, currency: <CURRENCY> and display: <DISPLAY_AMOUNT>

    Examples:
      | ITEM_NAME     | AMOUNT | CURRENCY | DISPLAY_AMOUNT |
      | Sleepy Hallow | 12.99  | CAD      | $12.99         |

  Scenario Outline: Account gets role BUYER from associations and can create and update custom cart.
    When I create a new shopping cart with name <INITIAL_NAME>
    And I update my cart with name <INITIAL_NAME> to <UPDATED_NAME>
    Then the cart has name <UPDATED_NAME>

    Examples:
      | INITIAL_NAME | UPDATED_NAME |
      | family       | friends      |
      | 大车         | 购物车       |