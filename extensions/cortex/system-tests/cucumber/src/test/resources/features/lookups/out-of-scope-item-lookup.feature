@Lookups @Items
Feature: Out of scope item look up returns item not found

  Scenario Outline: Item lookup outside of scope should not be found
    Given I am logged into scope <SCOPE> as a public shopper
    When I look up an out of scope item <SKU_CODE>
    Then the HTTP status is not found

    Examples:
      | SCOPE | SKU_CODE       |
      | mobee | BBEP128.Dove-L |