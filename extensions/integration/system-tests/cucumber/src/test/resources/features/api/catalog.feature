Feature: Import/Export API: Catalog

  Scenario: Export catalogs
    Given I export Catalog records from the API
    Then response has http status 200
    And response has at least 5 catalog elements

  Scenario: Export catalogs with CatalogCode filter
    Given I export Catalog records with query "FIND Catalog WHERE CatalogCode='Mobile'" from the API
    Then response has http status 200
    And response has exactly 1 catalog elements
