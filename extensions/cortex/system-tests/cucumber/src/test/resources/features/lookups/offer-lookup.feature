@lookups @offer
Feature: Lookup for an Offer

  Scenario: Lookup an offer with product code and verify code is correct
    Given I am logged in as a public shopper
    When I lookup an offer with code alien
    And follow the response
    Then There is a code link with code alien

  Scenario: Lookup an offer with product that does not exist should return Not Found
    Given I am logged in as a public shopper
    When I lookup an offer with code DOES_NOT_EXIST_CODE
    Then lookup fails with status not found

  Scenario: Lookup a disabled offer should return Not Found
    Given I am logged in as a public shopper
    When I lookup an offer with code futureProduct
    Then lookup fails with status not found