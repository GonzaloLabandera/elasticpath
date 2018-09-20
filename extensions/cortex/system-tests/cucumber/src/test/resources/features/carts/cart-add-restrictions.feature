@Carts
Feature: Add To Cart is disabled for certain items
  As a Store Manager,
  In order to avoid poor customer experiences* resulting from invalid purchases,
  I need to prevent customers from adding to their cart items that I cannot fulfil or that do not have a valid price.

  Background:
    Given I am logged in as a public shopper

  Scenario Outline: Add To Cart is disabled for items without a price
    Given item <ITEM> does not have a price
    When I view <ITEM> in the catalog
    Then I am not able to add the item to my cart

    Examples:
      | ITEM                   |
      | bundle_nopriceitem_sku |
      | noPrice_sku            |

  Scenario Outline: Structured error message appears when trying to add bundle item to cart with a bundle constituent that has no price
    Given I am logged into scope mobee as a public shopper
    When I look up an item with code <ITEM>
    And I go to add to cart form
    Then there is an advisor message with data field <ITEM> and the following fields:
      | messageType | messageId               | debugMessage                                 | dataField | blocks                 |
      | error       | cart.item.not.available | Item '<ITEM>' is not available for purchase. | <ITEM>    | addtodefaultcartaction |

    Examples:
      | ITEM                   |
      | bundle_nopriceitem_sku |

 #TODO add tests for no inventory and marked as unavailable