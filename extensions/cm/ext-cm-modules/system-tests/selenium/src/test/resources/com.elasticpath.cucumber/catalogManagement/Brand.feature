@smoketest @catalogManagement @catalog
Feature: Create Brand for existing catalog

  Background:
    Given I sign in to CM as admin user
    When I go to Catalog Management

  Scenario: Update, Delete new brand for an existing catalog
    When I select catalog Rock Jam in the list
    And I open the selected catalog
    And I add a brand My Test Brand with code TestBrand
    And I save my changes
    Then the brand My Test Brand is displayed in the brands table
    When I edit the brand name to newnamehere
    Then the brand newnamehere is displayed in the brands table
    When I delete brand newnamehere
    And I save my changes
    Then The brand newnamehere is deleted

  Scenario: Unable to add duplicate brand if it already exists
    When I select catalog Mobile Catalog in the list
    And I open the selected catalog
    And I add a brand Disney with code Disney
    Then an error message of A brand with the code you provided already exists. It cannot be added. is displayed in the add dialog

  Scenario: Unable to delete a brand already used by a product
    When I select catalog Mobile Catalog in the list
    And I open the selected catalog
    And I delete brand Samsung
    Then an error message of The following brand is currently in use. It cannot be removed. is displayed