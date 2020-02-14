@httpCaching
Feature: HTTP Caching - Purchases

  Background:
    Given I login as a newly registered shopper

  Scenario: Order purchase form should have HTTP caching
    When I add item with code FocUSsku to my cart
    And I retrieve the purchase form
    Then I should see the Cache-Control header in the response with a valid max age and private directive
    And I should see an ETag header in the response

  Scenario: Purchase lookup form should have HTTP caching
    When I retrieve the purchase lookup form
    Then I should see the Cache-Control header in the response with a valid max age and private directive
    And I should see an ETag header in the response

  Scenario: Purchases list should have HTTP caching
    When I view my profile
    And I follow links purchases
    Then I should see the Cache-Control header in the response with a valid max age and private directive
    And I should see an ETag header in the response

  Scenario: A single purchase should have HTTP caching
    When Adding an item with item code plantsVsZombies and quantity 1 to the cart
    And I fill in email needinfo
    And I fill in payment methods needinfo
    And I fill in billing address needinfo
    And the order is submitted
    Then the HTTP status is OK
    And I go to the purchases
    Then I should see the Cache-Control header in the response with a valid max age and private directive
    And I should see an ETag header in the response

  Scenario: Billing address on a purchase should have HTTP caching
    When I add item with code portable_tv_hdbuy_sku to my cart
    And I add item with code bundle_with_physical_and_multisku_items_bundle_sku to my cart
    And I fill in email needinfo
    And I fill in payment methods needinfo
    And I fill in billing address needinfo
    And I select shipping option CanadaPostExpress
    And I make a purchase
    When I go to the purchases
    And I navigate to the billing address
    Then I should see the Cache-Control header in the response with a valid max age and private directive
    And I should see an ETag header in the response

  Scenario: Purchase line items list should have HTTP caching
    When Adding an item with item code plantsVsZombies and quantity 1 to the cart
    And I fill in email needinfo
    And I fill in payment methods needinfo
    And I fill in billing address needinfo
    And the order is submitted
    Then the HTTP status is OK
    And I view the purchase line items
    Then I should see the Cache-Control header in the response with a valid max age and private directive
    And I should see an ETag header in the response

  Scenario Outline: A single purchase line item should have HTTP caching
    When Adding an item with item code <ITEMCODE> and quantity 1 to the cart
    And I fill in email needinfo
    And I fill in payment methods needinfo
    And I fill in billing address needinfo
    And the order is submitted
    Then the HTTP status is OK
    And I open purchase line item <ITEM_NAME>
    Then I should see the Cache-Control header in the response with a valid max age and private directive
    And I should see an ETag header in the response

    Examples:
      | ITEMCODE        | ITEM_NAME          |
      | plantsVsZombies | Plants vs. Zombies |

  Scenario Outline: Purchase line item components list should have HTTP caching
    When I add item with code <ITEM_CODE> to my cart
    And I fill in email needinfo
    And I fill in payment methods needinfo
    And I fill in billing address needinfo
    And I make a purchase
    And I open purchase line item <ITEM_NAME>
    And I follow links components
    Then I should see the Cache-Control header in the response with a valid max age and private directive
    And I should see an ETag header in the response

    Examples:
      | ITEM_CODE        | ITEM_NAME |
      | tbcp_0123456_sku | House     |

  Scenario Outline: A purchase line item component should have HTTP caching
    When I add item with code <ITEM_CODE> to my cart
    When I fill in email needinfo
    And I fill in payment methods needinfo
    And I fill in billing address needinfo
    And I make a purchase
    Then I open purchase line item <ITEM_NAME>
    And I follow links components
    And open the element with field name of <COMPONENT_1>
    Then I should see the Cache-Control header in the response with a valid max age and private directive
    And I should see an ETag header in the response

    Examples:
      | ITEM_CODE        | COMPONENT_1 | ITEM_NAME |
      | tbcp_0123456_sku | Avatar      | House     |

  Scenario Outline: Purchase line item component options list should have HTTP caching
    When I add item with code <ITEM_CODE> to my cart
    When I fill in email needinfo
    And I fill in payment methods needinfo
    And I fill in billing address needinfo
    And I make a purchase
    Then I open purchase line item <ITEM_NAME>
    And I follow links components
    And open the element with field name of <COMPONENT_1>
    And I follow links options
    Then I should see the Cache-Control header in the response with a valid max age and private directive
    And I should see an ETag header in the response

    Examples:
      | ITEM_CODE        | COMPONENT_1 | ITEM_NAME |
      | tbcp_0123456_sku | Avatar      | House     |


  Scenario Outline: A purchase line item component option should have HTTP caching
    When I add item with code <ITEM_CODE> to my cart
    When I fill in email needinfo
    And I fill in payment methods needinfo
    And I fill in billing address needinfo
    And I make a purchase
    Then I open purchase line item <ITEM_NAME>
    And I follow links components
    And open the element with field name of <COMPONENT_1>
    And I follow links options
    And open the element with field option-id of <COMPONENT_1_OPTION_1_ID>
    Then I should see the Cache-Control header in the response with a valid max age and private directive
    And I should see an ETag header in the response

    Examples:
      | ITEM_CODE        | COMPONENT_1 | COMPONENT_1_OPTION_1_ID | ITEM_NAME |
      | tbcp_0123456_sku | Avatar      | PurchaseType            | House     |

  Scenario Outline: A purchase line item component option value should have HTTP caching
    When I add item with code <ITEM_CODE> to my cart
    When I fill in email needinfo
    And I fill in payment methods needinfo
    And I fill in billing address needinfo
    And I make a purchase
    Then I open purchase line item <ITEM_NAME>
    And I follow links components
    And open the element with field name of <COMPONENT_1>
    And I follow links options
    And open the element with field option-id of <COMPONENT_1_OPTION_1_ID>
    And I follow links value
    Then I should see the Cache-Control header in the response with a valid max age and private directive
    And I should see an ETag header in the response

    Examples:
      | ITEM_CODE        | COMPONENT_1 | COMPONENT_1_OPTION_1_ID | ITEM_NAME |
      | tbcp_0123456_sku | Avatar      | PurchaseType            | House     |

  Scenario: Purchase payment instruments list should have HTTP caching
    When Adding an item with item code plantsVsZombies and quantity 1 to the cart
    And I fill in email needinfo
    And I fill in payment methods needinfo
    And I fill in billing address needinfo
    And the order is submitted
    Then the HTTP status is OK
    And I view the purchase payment instruments
    Then I should see the Cache-Control header in the response with a valid max age and private directive
    And I should see an ETag header in the response

  Scenario: A purchase payment instrument should have HTTP caching
    When Adding an item with item code plantsVsZombies and quantity 1 to the cart
    And I fill in email needinfo
    And I fill in payment methods needinfo
    And I fill in billing address needinfo
    And the order is submitted
    Then the HTTP status is OK
    And I view a single purchase payment instrument
    Then I should see the Cache-Control header in the response with a valid max age and private directive
    And I should see an ETag header in the response
