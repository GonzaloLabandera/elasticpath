@regressionTest @customerService @order @orderHold
Feature: Order Hold

  Background:
    Given I sign in to CM as admin user
    And I go to Configuration
    And I go to System Configuration
    And I add new defined values record for system setting with following data
      | setting | COMMERCE/SYSTEM/ONHOLD/holdAllOrdersForStore |
      | context | MOBEE                                        |
      | value   | true                                         |
    And I have an order that will be on hold for scope mobee with following skus
      | skuCode                 | quantity |
      | handsfree_shippable_sku | 1        |
    And I search and open order editor for the latest order
    And the order status should be On Hold
    And An order hold exists with the following
      | Hold Rule   | All orders are configured for hold processing |
      | Status      | ACTIVE                                        |
      | Resolved By |                                               |

  Scenario: No permission to resolve order hold
    Given I sign out
    When I sign in to CM as csrusernohold with password 111111
    And I search and open order editor for the latest order
    Then the order status should be On Hold
    And An order hold exists with the following
      | Hold Rule   | All orders are configured for hold processing |
      | Status      | ACTIVE                                        |
      | Resolved By |                                               |
    And I should be unable to resolve the hold

  Scenario: All order holds are resolved
    When I resolve all order holds
    Then The order hold status should be RESOLVED
    Then An order hold exists with the following
      | Hold Rule   | All orders are configured for hold processing |
      | Status      | RESOLVED                                      |
      | Resolved By | admin                                         |
    Then the order status should be In Progress

  @disableOrderHold
  Scenario: An order hold is marked as unresolvable
    When I mark the first possible order hold unresolvable
    Then The order hold status should be UNRESOLVABLE
    Then An order hold exists with the following
      | Hold Rule   | All orders are configured for hold processing |
      | Status      | UNRESOLVABLE                                  |
      | Resolved By | admin                                         |
    Then the order status should be Cancelled