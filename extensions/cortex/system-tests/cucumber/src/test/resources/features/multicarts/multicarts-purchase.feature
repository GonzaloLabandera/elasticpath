@carts @multicarts
Feature: Registered shoppers can complete purchase of named carts

  Background:
    Given I have authenticated as a newly registered shopper
    And I fill in email needinfo
    And I fill in billing address needinfo
    And I create a new shopping cart with name family
    And I fill in payment methods needinfo for cart family

  Scenario: Complete purchase with named cart
    When I add alien_sku to cart family with quantity 1
    And the order for cart family is submitted
    Then the HTTP status is OK
    And cart family total-quantity is 0

  Scenario: Purchase of named cart does not affect other named carts
    When I create a new shopping cart with name friends
    And I add alien_sku to cart friends with quantity 1
    And I add alien_sku to cart family with quantity 1
    And the order for cart family is submitted
    Then cart friends total-quantity is 1

  Scenario: Purchase of named cart does not affect default cart
    When I add item with code alien_sku to my cart with quantity 1
    And I add alien_sku to cart family with quantity 1
    And the order for cart family is submitted
    And I go to my default cart
    Then the cart total-quantity is 1

