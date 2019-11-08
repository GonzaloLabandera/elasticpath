@carts @multicarts
Feature: Shoppers cannot access multicart resources when functionality disabled.

    Scenario: Cannot get createcartform using URI in store with multicart functionality disabled
    Given I create a new shopper profile in scope toastie
    When I authenticate with newly created shopper in scope toastie
    And I attempt to get the uri /carts/toastie/form
    Then the operation is identified as conflict

    Scenario: Cannot POST to createcartform using URI in store with multicart functionality disabled
    Given I create a new shopper profile in scope toastie
    When I authenticate with newly created shopper in scope toastie
    And I attempt to post to the uri /carts/toastie/form
    Then the operation is identified as conflict

    Scenario: Cannot get carts list in store with multicart functionality disabled
    Given I create a new shopper profile in scope toastie
    When I authenticate with newly created shopper in scope toastie
    When I navigate to root
    Then I should not see the following links
    | carts |