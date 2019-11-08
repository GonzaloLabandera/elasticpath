@carts @multicarts
Feature: Registered shoppers can update created carts

  Background:
    Given I have authenticated as a newly registered shopper

  Scenario Outline: Update my cart name
    When I create a new shopping cart with name <INITIAL_NAME>
    And I update my cart with name <INITIAL_NAME> to <UPDATED_NAME>
    Then the cart has name <UPDATED_NAME>

    Examples:
      | INITIAL_NAME | UPDATED_NAME |
      | family       | friends      |
      | 大车         | 购物车       |

  Scenario: Can't update cart name to one that is being used
    When I create a new shopping cart with name family
    And I create a new shopping cart with name friends
    And I update my cart with name family to friends
    Then the HTTP status code is 400
    And I should see validation error message with message type, message id, and debug message
      | messageType | messageId                  | debugMessage               |
      | error       | cart.descriptor.not-unique | cart.descriptor.not-unique |

  Scenario: Update cart name to one that is being used by another shopper
    When I create a new shopping cart with name family
    And I have authenticated as a newly registered shopper
    And I create a new shopping cart with name friends
    And I update my cart with name friends to family
    Then the cart has name family
