@httpCaching
Feature: HTTP Caching - Carts

  Background:
    Given I login as a newly registered shopper

  Scenario Outline: Add to cart form should have HTTP caching
    When I look up an item with code <ITEMCODE>
    And I go to add to cart form
    Then I should see the Cache-Control header in the response with a valid max age and private directive
    And I should see an ETag header in the response

    Examples:
      | ITEMCODE   |
      | alien_sku  |
      | berries_20 |

  Scenario Outline: Add multiple sku form should have HTTP caching
    When I look up an item with code <ITEM_2_CODE>
    And I go to add to bulk add cart form
    Then I should see the Cache-Control header in the response with a valid max age and private directive
    And I should see an ETag header in the response

    Examples:
      | ITEM_2_CODE |
      | sony_bt_sku |
