Feature: Import/Export API: Customer

  Scenario: Export customers
    Given I export Customer records from the API
    Then response has http status 200
    And response has at least 10 customer elements

  Scenario: Export customer groups
    Given I export CustomerGroup records with parent Customer from the API
    Then response has http status 200
    And response has at least 1 customer_group elements

  Scenario: Export customer consent
    Given I export CustomerConsent records with parent Customer from the API
    Then response has http status 200
    And response has at least 1 customer_consent elements
