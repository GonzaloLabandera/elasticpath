@regressionTest @configuration @sortAttributes
Feature: Sort Attribute Population

  Scenario: Newly created attributes appear as potential sort attributes
    Given I sign in to CM as admin user
    When I go to Catalog Management
    And I select catalog Mobile Catalog in the list
    And I open the selected catalog
    And I select Attributes tab in the Catalog Editor for opened Catalog
    And I create a new catalog attribute with following details
      | attributeName | attributeUsage | attributeType | attributeRequired |
      | testAttribute | Product        | Decimal       | false             |
    And I go to Configuration
    And I go to Stores
    And I edit store MOBEE in editor
    And I select Sorting tab in the Store Editor
    Then I should see newly created catalog attribute