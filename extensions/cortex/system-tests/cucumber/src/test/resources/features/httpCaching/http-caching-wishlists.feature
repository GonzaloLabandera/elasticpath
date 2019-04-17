@httpCaching
Feature: HTTP Caching - Wishlists

  Background:
    Given I have authenticated as a newly registered shopper

  Scenario Outline: Move to cart form should have HTTP caching
    Given I add item with code <ITEMCODE> to my default wishlist
    When I navigate to move to cart form for item with code <ITEMCODE>
    Then I should see the Cache-Control header in the response with a valid max age and private directive
    And I should see an ETag header in the response

    Examples:
      | ITEMCODE  |
      | alien_sku |

  Scenario Outline: Add item to wishlist form should have HTTP caching
    When I navigate to add to wishlist form for item with code <ITEMCODE>
    Then I should see the Cache-Control header in the response with a valid max age and private directive
    And I should see an ETag header in the response

    Examples:
    | ITEMCODE  |
    | alien_sku |

  Scenario Outline: Move to wishlist form should have HTTP caching
    Given item with code <ITEMCODE> already exists in my cart with quantity 1
    When I navigate to move to wishlist form for item with code <ITEMCODE>
    Then I should see the Cache-Control header in the response with a valid max age and private directive
    And I should see an ETag header in the response

    Examples:
      | ITEMCODE  |
      | alien_sku |
