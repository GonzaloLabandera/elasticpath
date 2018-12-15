@carts
Feature: Adding a product with dependent items
  As a shopper
  I want to be able to add products with dependent items to my shopping cart
  So that the cart contains the items and their dependent items

  Background:
    Given I login as a registered shopper

  Scenario Outline: Apply an item to an order
    When I look up an item with code <ITEMCODE>
    And I go to add to cart form
    And I add the item to the cart with quantity <QTY> and configurable fields:
      | allFieldTypes.boolean  | <BOOLEAN>  |
    Then the cart total-quantity is <TOTALQTY>
    Examples:
      | ITEMCODE    | QTY | BOOLEAN  | TOTALQTY |
      | hama_bt_sku | 1   | true     | 2        |


