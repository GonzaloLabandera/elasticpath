@jwtAuthorization @b2b
Feature: B2B Buyer functionality

  Scenario Outline: Buyers can create multiple carts
    Given I login using jwt authorization with the following details
      | roles | BUYER |
    When I create a new shopping cart with name <NAME>
    Then the cart has name <NAME>

    Examples:
      | NAME     |
      | friends  |
      | 大车      |

  Scenario: A Buyer's named carts is only persisted within the context of a chosen account
    Given I login using jwt authorization with the following details
      | account_shared_id  | accounttest1@elasticpath.com |
      | roles              | BUYER                        |
    When I create a new shopping cart with name retail
    And I login using jwt authorization with the following details
      | account_shared_id  | accounttest2@elasticpath.com |
      | roles              | BUYER                        |
    Then I do not have carts with the following names:
      | retail             |

  Scenario: A Buyer's default cart is only persisted within the context of a chosen account
    Given I login using jwt authorization with the following details
      | account_shared_id  | accounttest1@elasticpath.com |
      | roles              | BUYER                        |
    When Adding an item with item code plantsVsZombies and quantity 2 to the cart
    And I login using jwt authorization with the following details
      | account_shared_id  | accounttest2@elasticpath.com |
      | roles              | BUYER                        |
    Then the cart total-quantity remains 0

  Scenario: Different Buyers in the same account each have their own default carts
    Given I login using jwt authorization with the following details
      | account_shared_id  | accounttest1@elasticpath.com |
      | user_id            | usertestguid                 |
      | roles              | BUYER                        |
    When Adding an item with item code plantsVsZombies and quantity 1 to the cart
    And I login using jwt authorization with the following details
      | account_shared_id  | accounttest1@elasticpath.com |
      | user_id            | usertest2guid                |
      | roles              | BUYER                        |
    Then the cart total-quantity remains 0

  Scenario: Different Buyers in the same account each have their own set of named carts
    Given I login using jwt authorization with the following details
      | account_shared_id  | accounttest1@elasticpath.com |
      | user_id            | usertestguid                 |
      | roles              | BUYER                        |
    When I create a new shopping cart with name xmas gifts
    And I login using jwt authorization with the following details
      | account_shared_id  | accounttest1@elasticpath.com |
      | user_id            | usertest2guid                |
      | roles              | BUYER                        |
    Then I do not have carts with the following names
      | xmas gifts |

  Scenario: Buyers can order from default cart
    Given I login using jwt authorization with the following details
      | roles | BUYER |
    When Adding an item with item code plantsVsZombies and quantity 1 to the cart
    And I fill in email needinfo
    And I fill in payment methods needinfo
    And I fill in billing address needinfo
    And the order is submitted
    Then the HTTP status is OK

  Scenario: Buyers can order from named cart
    Given I login using jwt authorization with the following details
      | roles | BUYER |
    And I fill in email needinfo
    And I fill in billing address needinfo
    And I create a new shopping cart with name family
    And I fill in payment methods needinfo for cart family
    When I add plantsVsZombies to cart family with quantity 1
    And the order for cart family is submitted
    Then the HTTP status is OK
    And cart family total-quantity is 0
