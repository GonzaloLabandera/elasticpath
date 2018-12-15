@profile
Feature: Purchase history

  Background:
    Given I login as a newly registered shopper

  Scenario: Retrieve purchase history from profile
    Given I login as a newly registered shopper
    And Adding an item with item code plantsVsZombies and quantity 1 to the cart
    And I fill in email needinfo
    And I fill in payment methods needinfo
    And I fill in billing address needinfo
    And the order is submitted
    And the HTTP status is OK
    And Adding an item with item code plantsVsZombies and quantity 1 to the cart
    And I fill in email needinfo
    And I fill in payment methods needinfo
    And I fill in billing address needinfo
    And the order is submitted
    And the HTTP status is OK
    When I view my profile
    And I follow links purchases
    Then there are 2 links of rel element
