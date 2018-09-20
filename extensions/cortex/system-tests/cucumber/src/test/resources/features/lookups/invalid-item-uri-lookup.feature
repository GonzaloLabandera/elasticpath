@Lookups @Items
Feature: Invalid item uri returns item not found

  Background:
    Given I am logged in as a public shopper

  Scenario Outline: Invalid item uri should return item not found
    When I submit the invalid item uri <INVALID_URI>
    Then the HTTP status is not found

    Examples:
      | INVALID_URI                        |
      | /items/mobee/qgqvhkdon52c24tfmfwa= |