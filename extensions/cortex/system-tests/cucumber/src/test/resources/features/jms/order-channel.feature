@jms-tests
Feature: Validate messages in order channel

  Scenario Outline: Validate messages from ep.customers and ep.orders channel
    Given I am listening to ep.customers channel
    And I am listening to ep.orders channel
    And I have an order for scope <scope> with following skus
      | skuCode      | quantity |
      | <sku-code-1> | 1        |
    When I read ep.customers message from channel
    Then json eventType should contain following values
      | key  | value               |
      | name | CUSTOMER_REGISTERED |
    When I read ep.orders message from channel
    Then json eventType should contain following values
      | key  | value          |
      | name | ORDER_RELEASED |
      | name | ORDER_CREATED  |

    Examples:
      | scope | sku-code-1 |
      | mobee | alien_sku  |
    

