@addresses
Feature: Addresses Form Fields
  This representation is a form that contains the input fields required to create an address.
  The complete form is posted to the createaddressaction to create an address.

  Background:
    Given I login as a public shopper

  Scenario: Get address form has the following fields
    Given I get address form
    Then form should have following values
      | key     | value            |
      | address | country-name     |
      | address | extended-address |
      | address | locality         |
      | address | postal-code      |
      | address | region           |
      | address | street-address   |
      | name    | family-name      |
      | name    | given-name       |
    And I see the following fields on the top-level address form:
      | organization |
      | phone-number |
