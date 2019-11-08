@carts
Feature: Deletion of Named Carts

  Background:
    Given I have authenticated as a newly registered shopper

  Scenario: Empty shopping cart can be deleted
    When I create a new shopping cart with name family
    And I have carts with the following names:
      | family |
    When I delete a shopping cart with name family
    Then the HTTP status code is 204
    And I do not have carts with the following names:
      | family |

  Scenario: Non-empty shopping cart can be deleted
    When I create a new shopping cart with name family
    And I add alien_sku to cart family with quantity 1
    And I delete a shopping cart with name family
    Then the HTTP status code is 204
    And I do not have carts with the following names:
      | family |
