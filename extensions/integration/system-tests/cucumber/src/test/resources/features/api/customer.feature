Feature: Import/Export API: Customer

  Scenario: Export customers
    Given I export Customer records from the API
    Then response has http status 200
    And response has at least 10 customer elements

  Scenario: Export customers with CustomerType filter
    Given I export Customer records with query "FIND Customer WHERE SharedID = 'MOBEE:oliver.harris@elasticpath.com'" from the API
    Then response has http status 200
    And response has exactly 1 customer elements

  Scenario: Export customers with SharedID filter
    Given I export Customer records with query "FIND Customer WHERE CustomerType = ACCOUNT" from the API
    Then response has http status 200
    And response has at least 1 customer elements

  Scenario: Export customers with LastModifiedDate filter
    Given I export Customer records with query "FIND Customer WHERE LastModifiedDate > '2020-01-01T00:00:00-08:00'" from the API
    Then response has http status 200
    And response has at least 1 customer elements

  Scenario: Export customer groups
    Given I export CustomerGroup records with parent Customer from the API
    Then response has http status 200
    And response has at least 1 customer_group elements

  Scenario: Export customer consent
    Given I export CustomerConsent records with parent Customer from the API
    Then response has http status 200
    And response has at least 1 customer_consent elements
