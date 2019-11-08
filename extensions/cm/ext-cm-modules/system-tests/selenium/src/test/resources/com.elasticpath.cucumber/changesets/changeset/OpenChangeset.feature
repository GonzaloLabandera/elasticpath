@changeset @csCatalog
Feature: Open Object in Change set

  Background:
    Given I sign into CM as cs_changeset
    And I create a new catalog with following details
      | catalogName   | language |
      | ZTest Catalog | English  |

  @lockAndFinalize @cleanupCatalog
  Scenario Outline: Open and edit object in Change Set
    Given I create and select the newly created change set ChangeSetCI_Edit_BRAND
    And I go to Catalog Management
    And I open the newly created catalog editor
    And I add a brand <BRAND_NAME>
    And the brand <BRAND_NAME> is displayed in the brands table
    And I click add item to change set button
    When I open object in the changeset for <BRAND_NAME>
    And I change brand name to <NEW_BRAND_NAME>
    Then I should see edited brand <NEW_BRAND_NAME> in the change set

    Examples:
      | BRAND_NAME | NEW_BRAND_NAME |
      | NewBrand     | NewBrandA      |