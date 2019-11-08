@carts @multicarts
Feature: Registered shoppers can add items to named carts

  Background:
    Given I have authenticated as a newly registered shopper
    And I create a new shopping cart with name family

  Scenario: Add to specific cart does not affect default cart
    When I add alien_sku to cart family with quantity 1
    Then cart family total-quantity is 1
    When I go to my default cart
    Then the cart total-quantity is 0

  Scenario: Add to specific cart does not affect other created carts
    When I create a new shopping cart with name friends
    And I add alien_sku to cart friends with quantity 1
    Then cart friends total-quantity is 1
    And cart family total-quantity is 0

  Scenario: Add additional quantities of an item to a specific cart increments lineitem quantity
    When I add alien_sku to cart family with quantity 1
    Then cart family total-quantity is 1
    When I add alien_sku to cart family with quantity 1
    Then cart family total-quantity is 2

  Scenario Outline: Can't add item with no price to specific cart
    Given item sku <ITEM_SKU> does not have a price
    When I view <ITEM_SKU> in the catalog
    Then I am not able to add the item to cart family
    And cart family total-quantity is 0

    Examples:
      | ITEM_SKU               |
      | bundle_nopriceitem_sku |
      | noPrice_sku            |

  Scenario Outline: Can't add an item to a specific cart with an invalid quantity
    When I add alien_sku to cart family with quantity <QUANTITY>
    Then the operation is identified as bad request
    And cart family total-quantity is 0

    Examples:
      | QUANTITY      |
      | 0             |
      | -2            |
      | 0.1           |
      | invalidFormat |
      | 2147483648    |

  Scenario: Can't add item with insufficient quantity to specific cart
    When I add sony_bt_sku to cart family with quantity 11
    Then the operation is identified as bad request
    And Structured error message contains:
      | Item 'sony_bt_sku' only has 10 available but 11 were requested. |
    And cart family total-quantity is 0

  Scenario Outline: Add a configurable item to a specific cart
    When I add configurable item <ITEMCODE> to specific cart family with quantity <QTY> and configurable fields:
      | giftCertificate.message        | <MESSAGE>         |
      | giftCertificate.recipientEmail | <RECIPIENT_EMAIL> |
      | giftCertificate.recipientName  | <RECIPIENT_NAME>  |
      | giftCertificate.senderName     | <SENDER_NAME>     |
    Then in the cart family the cart lineitem with itemcode <ITEMCODE> has quantity <QTY> and configurable fields as:
      | giftCertificate.message        | <MESSAGE>         |
      | giftCertificate.recipientEmail | <RECIPIENT_EMAIL> |
      | giftCertificate.recipientName  | <RECIPIENT_NAME>  |
      | giftCertificate.senderName     | <SENDER_NAME>     |
    And cart family total-quantity is 1

    Examples:
      | ITEMCODE   | MESSAGE     | RECIPIENT_EMAIL              | RECIPIENT_NAME | SENDER_NAME  | QTY |
      | berries_20 | Hello World | harry.potter@elasticpath.com | Harry Potter   | MOBEE tester | 1   |

  Scenario: Add a dependent line item to a specific cart
    When I add hama_bt_sku to cart family with quantity 1
    Then cart family total-quantity is 2