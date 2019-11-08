# language: en
@exportStoreCustomerAttribute
Feature: Export Store Customer Attributes
  As Operations, I want to export store customer attributes to the file system.

  Scenario: Export Store Customer Attributes
    Given the customer profile attribute testAttribute1 has been created
    And the customer profile attribute testAttribute2 has been created
    And the customer profile attribute testAttribute3 has been created
    And the store testStore has been created
    And the existing store customer attributes of
      | guid  | storeCode | attributeKey   | policyKey    |
      | 12345 | testStore | testAttribute1 | HIDDEN       |
      | 12346 | testStore | testAttribute2 | READ_ONLY    |
      | 12347 | testStore | testAttribute3 | DEFAULT      |
    When exporting store customer attributes with the importexport tool
    And the exported store customer attribute data is parsed
    Then the exported store customer attribute records should include
      | guid  | storeCode | attributeKey   | policyKey    |
      | 12345 | testStore | testAttribute1 | HIDDEN       |
      | 12346 | testStore | testAttribute2 | READ_ONLY    |
      | 12347 | testStore | testAttribute3 | DEFAULT      |
    And the exported manifest file should have an entry for store customer attributes