@cleanupJobProcessors
Feature: Clean up anonymous customer, failed orders, abandoned and inactive shopping cart

  Background:
    Given products
      | skuCode     | price  |
      | originalSku | 100.00 |

  Scenario: Clean up anonymous customer
    Given the ANONYMOUSCUSTOMERCLEANUP maxHistory is 0 day
    And the anonymous customer has a modified date older than 0 day
    And the anonymous customer created a payment instrument in order
    When the cleanupAnonymousCustomerJob processes
    Then the anonymous customer should be removed from TCUSTOMER, TSHOPPER
    And the associated payment instrument should be removed from TCARTORDERPAYMENTINSTRUMENT


  Scenario: Clean up abandoned shopping cart
    Given the ABANDONEDCARTCLEANUP maxHistory is 0 day
    And the shopping cart has a modified date older than 0 day
    And the customer adds these items to the shopping cart
      | quantity | skuCode     |
      | 1        | originalSku |
    And the anonymous customer created a payment instrument in order
    When the cleanupAbandonedCartsJob processes
    Then the record from TSHOPPINGCART should be removed
    And the associated payment instrument should be removed from TCARTORDERPAYMENTINSTRUMENT


  Scenario: Clean up inactive shopping cart
    Given the ABANDONEDCARTCLEANUP maxHistory is 0 day
    And the shopping cart has a modified date older than 0 day
    And the customer adds these items to the shopping cart
      | quantity | skuCode     |
      | 1        | originalSku |
    And the anonymous customer created a payment instrument in order
    When the cleanupInactiveCartsJob processes
    Then the record from TSHOPPINGCART should be removed
    And the associated payment instrument should be removed from TCARTORDERPAYMENTINSTRUMENT

  Scenario: Clean up failed orders with cleanupFailedOrdersJob
    Given the FAILEDORDERCLEANUP maxHistory is 0 day
    And the order is in FAILED state
      | quantity | skuCode     |
      | 1        | originalSku |
    When the cleanupFailedOrdersJob processes
    Then the FAILED orders should be removed from TORDER
    And there is no PI saved in TORDERPAYMENTINSTRUMENT for failed orders

