@regressionTest @customerService @order @orderSearch
Feature: Order Search with User-Assigned Order

  Background:
    Given I sign in to CM as admin user
    And I go to Customer Service
    And I create an order for scope mobee with following skus
      | skuCode                 | quantity |
      | handsfree_shippable_sku | 1        |
    And I create a failed order for scope mobee with following skus
      | skuCode                 | quantity |
      | handsfree_shippable_sku | 1        |

  Scenario: Order searches by Order Number, Email and First Name
    When I search the latest successful order by number
    Then I select the row with the latest successful order in results page
    When I search for the latest orders by email
    Then I select the row with customer name testGivenName testFamilyName in search results pane
    When I search for orders by First Name testGivenName
    Then I select the row with customer name testGivenName testFamilyName in search results pane

  Scenario: Order searches by Last Name, Postal Code, Store and Phone Number
    When I search for orders by Last Name testFamilyName
    Then I select the row with customer name testGivenName testFamilyName in search results pane
    When I search for latest successful order by Postal Code 98119
    Then I should see Order with Postal Code 98119 in search results pane
    When I search for latest successful order by Store Mobee
    Then I select the row with store Mobee in search results pane
    When I search and open order editor for the latest order
    And I update and save the following billing address for the order
      | phone | 500-500-500 |
    When I search for orders by Phone Number 500-500-500
    Then I should see Order with Phone Number 500-500-500 in search results pane

  Scenario: Order searches Date Range, Order Status, Order Shipment Status and SKU Code
    When I search for order by Dates between current date and next date
    Then I should see Orders search results are within Date Range
    When I search for latest successful order by Order Status In Progress
     And I select the row with the latest successful order in results page
    Then The selected row has order status In Progress in search results pane
    When I search for latest successful order by Order Shipment Status Inventory Assigned
    Then I should see Order with Shipment Status Inventory Assigned in search results pane
    When I search for latest successful order by SKU Code handsfree_shippable_sku
    Then I should see Order with SKU Code handsfree_shippable_sku in search results pane

  Scenario: Order search for failed order
    When I search the order after the latest successful order by number
     And I select the row with the order after the latest successful order in results page
    Then The selected row has order status Failed in search results pane
