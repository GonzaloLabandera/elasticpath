@regressionTest @customerService @order @orderSearch
Feature: Order Search

  Background:
    Given I sign in to CM as admin user
    And I go to Customer Service
    And I have an order for scope mobee with following skus
      | skuCode                 | quantity |
      | handsfree_shippable_sku | 1        |

  Scenario: Order searches by Order Number, Email and First Name
    When I search the latest order by number
    Then I should see the latest order in results pane
    When I search for the latest orders by email
    Then I should see customer name Test User in search results pane
    When I search for orders by First Name Test
    Then I should see customer name Test User in search results pane

  Scenario: Order searches by Last Name, Post Code, Store and Phone Number
    When I search for orders by Last Name User
    Then I should see customer name Test User in search results pane
    When I search for latest orders by Postal Code 98119
    Then I should see Order with Postal Code 98119 in search results pane
    When I search for latest order by Store Mobee
    Then I should see Order with Store Mobee in search results pane
    When I search and open order editor for the latest order
    And I update and save the following billing address for the order
      | phone | 500-500-500 |
    When I search for orders by Phone Number 500-500-500
    Then I should see Order with Phone Number 500-500-500 in search results pane

  Scenario: Order searches Date Range, Order Status, Order Shipment Status and SKU Code
    When I search for order by Dates between current date and next date
    Then I should see Orders search results are within Date Range
    When I search for latest order by Order Status In Progress
    Then I should see the latest order in results pane
    And I should see Order with Order Status - In Progress in search results pane
    When I search for latest order by Order Shipment Status Inventory Assigned
    Then I should see Order with Shipment Status Inventory Assigned in search results pane
    When I search for latest order by SKU Code handsfree_shippable_sku
    Then I should see Order with SKU Code handsfree_shippable_sku in search results pane
