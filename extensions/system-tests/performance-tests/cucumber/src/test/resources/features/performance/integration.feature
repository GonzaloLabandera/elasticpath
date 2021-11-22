Feature: Integration performance tests
  This is a set of integration-related performance tests, comprising of various imports and exports.

  Background:
    Given I start measuring db queries on integration server

  Scenario: Export catalogs
    And I export Catalog records from the API
    And response has http status 200
    And response has at least 5 catalog elements
    Then I stop measuring db queries

  Scenario: Export categories
    And I export Category records from the API
    And response has http status 200
    And response has at least 10 category elements
    Then I stop measuring db queries

  Scenario: Import product with changeset specified
    And I import imports/testproduct.xml with change set guid 865223FE-4B81-6971-ED99-0BE65A82C8C2 to the API
    And response has http status 200
    And summary object can be retrieved
    And summary contains object PRODUCT with count 1
    And summary contains no failures
    And summary contains no warnings
    Then I stop measuring db queries

  Scenario: Export customers
    And I export Customer records from the API
    And response has http status 200
    And response has at least 10 customer elements
    Then I stop measuring db queries

  Scenario: Export price lists
    And I export PriceListDescriptor records from the API
    And response has http status 200
    And response has at least 10 price_list elements
    Then I stop measuring db queries

  Scenario: Export promotions
    And I export Promotion records from the API
    And response has http status 200
    And response has at least 10 promotion elements
    Then I stop measuring db queries

  Scenario: Export stores
    And I export Store records from the API
    And response has http status 200
    And response has at least 1 store elements
    Then I stop measuring db queries