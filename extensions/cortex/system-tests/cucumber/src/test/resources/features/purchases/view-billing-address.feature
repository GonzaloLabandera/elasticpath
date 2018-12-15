@billings @headerAuth @purchases
Feature: View purchase billing address
  As a Shopper
  I want to see the address a shipment was dispatched to
  so that I Know where to collect my goods

  Background:
    Given I login as a registered shopper

  Scenario: View billing address
    When I add item with code portable_tv_hdbuy_sku to my cart
    And I add item with code bundle_with_physical_and_multisku_items_bundle_sku to my cart
    And I select shipping option CanadaPostExpress
    When I make a purchase
    And I navigate to the billing address
    Then I see billing address
    And I can follow a link back to the purchase

