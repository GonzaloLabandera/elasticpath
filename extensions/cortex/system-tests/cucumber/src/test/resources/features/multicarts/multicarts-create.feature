@carts @multicarts
Feature: Registered shoppers can create named carts

  Background:
    Given I have authenticated as a newly registered shopper

  Scenario Outline: Create new cart
    When I create a new shopping cart with name <NAME>
    Then the cart has name <NAME>

    Examples:
      | NAME    |
      | friends |
      | 大车    |

  Scenario: Can't create a cart without providing a name
    When I create a new shopping cart that has a name of length 0
    Then the HTTP status code is 400
    And I should see validation error message with message type, message id, debug message, and field
      | messageType | messageId      | debugMessage              | fieldName |
      | error       | field.required | 'name' value is required. | name      |

  Scenario: Can't create a cart with name longer than 255 characters
    When I create a new shopping cart that has a name of length 256
    Then the HTTP status code is 400
    And I should see validation error message with message type, message id, debug message, and field
      | messageType | messageId          | debugMessage                                            | fieldName |
      | error       | field.invalid.size | 'name' value must contain between 0 and 255 characters. | name      |

  Scenario: Can't create new cart with the same name
    When I create a new shopping cart with name personal
    And I create a new shopping cart with name personal
    Then the HTTP status code is 400
    And I should see validation error message with message type, message id, and debug message
      | messageType | messageId                  | debugMessage               |
      | error       | cart.descriptor.not-unique | cart.descriptor.not-unique |

  Scenario: Create cart with the same name being used by another shopper
    When I create a new shopping cart with name personal
    And I login as another registered shopper
    And I create a new shopping cart with name personal
    Then the cart has name personal

  Scenario: Zoom to retrieve cart descriptors
    When I create a new shopping cart with name friends
    And I create a new shopping cart with name family
    And I create a new shopping cart with name cousins
    Then I have carts with the following names
      | friends |
      | family  |
      | cousins |

  Scenario: Create new cart as shopper in linked stores
    When I create a new shopper profile in scope mobee
    And I authenticate with newly created shopper in scope kobee
    And I create a new shopping cart with name confidential
    Then the cart has name confidential
