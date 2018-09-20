#not-ready because by default the channel we send messages to is topic. 
# To run this test, first we need to change the channel to queue in system setting: commerce/system/messaging/*/channelUri
@notready
Feature: Validate messages from the Queue channels

  Scenario Outline: Read message from ep.customers and ep.orders queues
    Given I am listening to ep.customers queue
    And I delete all messages from ep.customers queue
    And I am listening to ep.orders queue
    And I delete all messages from ep.orders queue
    And I have an order for scope <scope> with following skus
      | skuCode      | quantity |
      | <sku-code-1> | 1        |
    When I read ep.customers message from queue
    Then json eventType should contain following values
      | key  | value               |
      | name | CUSTOMER_REGISTERED |
    When I read ep.orders message from queue
    Then json eventType should contain following values
      | key  | value          |
      | name | ORDER_RELEASED |
      | name | ORDER_CREATED  |

    Examples:
      | scope | sku-code-1 |
      | mobee | alien_sku  |
	