@lookups @offer
Feature: Lookup a batch of Offers

  Background:
    Given I am logged in as a public shopper

  Scenario: Lookup Offers from a list of valid product codes
    When I submit a batch of product codes ["alien", "gravity"]
    Then there are 2 links of rel element
    Then the element list contains items with display-names
      | Alien   |
      | Gravity |

  Scenario: Lookup Offers from a list of invalid product codes
    When I submit a batch of product codes ["invalid_code", "another_invalid_code"]
    Then there are no element links

  Scenario: Lookup Offers from a list of valid and invalid product codes
    When I submit a batch of product codes ["invalid_code", "alien", "gravity", "another_invalid_code"]
    Then there are 2 links of rel element
    And the element list contains items with display-names
      | Alien   |
      | Gravity |