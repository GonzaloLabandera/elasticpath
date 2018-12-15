@shipments @purchases @headerAuth
Feature: View purchase shipment address
  As a Shopper
  I want to see the address a shipment was dispatched to
  so that I Know where to collect my goods

  Background:
    Given I login as a registered shopper

  Scenario: View shipment address
    When I add item with code portable_tv_hdbuy_sku to my cart
    And I add item with code bundle_with_physical_and_multisku_items_bundle_sku to my cart
    And I select shipping option CanadaPostExpress
    When I make a purchase
    And I navigate to shipment
    And I follow the shipping address link
    Then I see shipping address
    And I can follow a link back to the shipment

