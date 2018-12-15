@authentication
Feature: Authentication for customer shared between linked stores

  Scenario: Authenticate in a linked store
    When I create a new shopper profile in scope mobee
    And I authenticate with newly created shopper in scope toastie
    And I GET /profiles/toastie/default
    And the HTTP status is OK

  Scenario: Authenticate in a linked store multiple times
    When I create a new shopper profile in scope mobee
    And I authenticate with newly created shopper in scope toastie
    And I authenticate with newly created shopper in scope toastie
    And I GET /profiles/toastie/default
    And the HTTP status is OK

  Scenario: Authenticate in an unlinked store
    Given I create a new shopper profile in scope tokenee
    And the HTTP status is OK, created
    When I create a new shopper profile in scope mobee
    And I authenticate with newly created shopper in scope tokenee
    And the HTTP status is unauthorized
