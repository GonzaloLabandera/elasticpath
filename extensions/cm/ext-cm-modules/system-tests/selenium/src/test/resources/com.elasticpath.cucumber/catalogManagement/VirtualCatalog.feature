@smoketest @catalogManagement @catalog
Feature: Create Virtual Catalog

  Background:
    Given I sign in to CM as admin user

  Scenario: Create virtual catalog
    When I go to Catalog Management
    And I create a new virtual catalog with following details
      | catalogName           | language |
      | ATest Virtual Catalog | English  |
    Then newly created virtual catalog is in the list
    When I delete newly created virtual catalog
    Then newly created virtual catalog is deleted

  @resetVirtualCatalogProduct
  Scenario: Exclude and Include product in Virtual Catalog
    Given there is an existing product 12 Angry Men in category Movies in virtual catalog Mobile Virtual Catalog
    When I exclude the product from the virtual catalog
    Then the product does not have Mobile Virtual Catalog in category assignment and merchandising associations
    When I include the product in the virtual catalog
    Then the product has Mobile Virtual Catalog in category assignment and merchandising associations