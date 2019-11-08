# language: en
@importStoreCustomerAttribute
Feature: Import Store Customer Attributes
  As Operations, I want to import store customer attributes from the file system

  Scenario: Import Store Customer Attributes
    Given the customer profile attribute testAttribute1 has been created
    And the customer profile attribute testAttribute2 has been created
    And the customer profile attribute testAttribute3 has been created
    And the store testStore has been created
    And the store customer attribute import data has been emptied out
    And the store customer attributes to import of
      | guid  | storeCode | attributeKey   | policyKey    |
      | 12345 | testStore | testAttribute1 | HIDDEN       |
      | 12346 | testStore | testAttribute2 | READ_ONLY    |
      | 12347 | testStore | testAttribute3 | DEFAULT      |
    When importing store customer attributes with the importexport tool
    Then the following store customer attributes exist
      | guid  | storeCode | attributeKey   | policyKey    |
      | 12345 | testStore | testAttribute1 | HIDDEN       |
      | 12346 | testStore | testAttribute2 | READ_ONLY    |
      | 12347 | testStore | testAttribute3 | DEFAULT      |