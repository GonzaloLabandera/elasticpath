@cleanupJobProcessors
Feature: Clean up anonymous customer, failed orders, abandoned and inactive shopping cart

  Background:
    Given products
      | skuCode     | price  |
      | originalSku | 100.00 |

  Scenario: Clean up failed orders with cleanupFailedOrdersJob
    Given the FAILEDORDERCLEANUP maxHistory is 0 day
    And the order is in FAILED state
      | quantity | skuCode     |
      | 1        | originalSku |
    When the cleanupFailedOrdersJob processes
    Then the FAILED orders should be removed from TORDER
    And there is no PI saved in TORDERPAYMENTINSTRUMENT for failed orders

