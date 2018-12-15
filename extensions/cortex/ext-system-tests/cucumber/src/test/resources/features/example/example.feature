@example
Feature: Purchase

  Scenario: Purchase has purchase number and same total as the order
    Given I login as a registered shopper
    And Adding an item with item code plantsVsZombies and quantity 1 to the cart
    And the order total has amount: 0.99, currency: CAD and display: $0.99
    When the order is submitted
    Then the HTTP status is OK
    When I view my profile
    And I follow links purchases -> element
    Then the field purchase-number matches \d\d\d\d\d
    And the field monetary-total contains value $0.99

  Scenario: Search product
    Given I login as a registered shopper
    When I search for an item name Alien
    Then the item code is alien_sku

  Scenario: Validate messages from VirtualTopic.ep.customers and VirtualTopic.ep.orders queue
    Given I am listening to Consumer.test.VirtualTopic.ep.orders queue
    And I login as a registered shopper
    And Adding an item with item code plantsVsZombies and quantity 1 to the cart
    And the order is submitted
    When I read Consumer.test.VirtualTopic.ep.orders message from queue
    Then json eventType should contain following values
      | key  | value          |
      | name | ORDER_CREATED  |
