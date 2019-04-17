@regressionTest @catalogManagement @product
Feature: Product Merchandising Association

  Scenario Outline: Add Edit and Remove Merchandising Associations from all tabs
    Given I sign in to CM as admin user
    And I go to Catalog Management
    And I am viewing the Merchandising Associations tab of an existing product with product code tt0050083
    When I add product code alien to merchandising association <MERCHANDISING_TAB>
    Then the product code alien exists under merchandising association <MERCHANDISING_TAB>
    When I edit product code alien to tt966001av
    Then the product code tt966001av exists under merchandising association <MERCHANDISING_TAB>
    When I delete product code tt966001av
    Then the product code tt966001av is no longer in merchandising association <MERCHANDISING_TAB>

    Examples:
      | MERCHANDISING_TAB |
      | Cross Sell        |
      | Up Sell           |
      | Warranty          |
      | Accessory         |
      | Replacement       |
      | Dependent Item    |