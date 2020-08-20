Feature: Import/Export API: Category

  Scenario: Export categories
    Given I export Category records from the API
    Then response has http status 200
    And response has at least 10 category elements

  @bug
  Scenario: Export categories with CategoryCode filter
    Given I export Category records with query "FIND Category WHERE CategoryCode='MobileGames'" from the API
    Then response has http status 200
    And response has exactly 1 category elements

  @bug
  Scenario: Export categories with CategoryName filter
    Given I export Category records with query "FIND Category WHERE CategoryName[en] = 'Mobile Games'" from the API
    Then response has http status 200
    And response has exactly 1 category elements

  Scenario: Export categories with CatalogCode filter
    Given I export Category records with query "FIND Category WHERE CatalogCode='MobileCatalog'" from the API
    Then response has http status 200
    And response has at least 1 category elements
