Feature: Import/Export API: Price List Assignment

  Scenario: Export price list assignments
    Given I export PriceListAssignment records from the API
    Then response has http status 200
    And response has at least 1 price_list_assignment elements

  Scenario: Export price list assignments with CatalogCode filter
    Given I export PriceListAssignment records with query "FIND PriceListAssignment WHERE CatalogCode='mobile'" from the API
    Then response has http status 200
    And response has at least 1 price_list_assignment elements
