@regressionTest @configuration @facets
Feature: Facet Population

  Background:
    Given I sign in to CM as admin user
    And I go to Catalog Management
    And I select catalog Mobile Catalog in the list
    And I open the selected catalog

  Scenario Outline: Newly created attributes appear as potential facets
    When I select Attributes tab in the Catalog Editor for opened Catalog
    And I create a new catalog attribute with following details
      | attributeName     | attributeUsage    | attributeType | attributeRequired |
      | testAttributeName | <ATTRIBUTE_USAGE> | Short Text    | false             |
    And I go to Configuration
    And I go to Stores
    And I edit store MOBEE in editor
    And I select Facets tab in the Store Editor
    Then I should see newly created attribute as a facet with the following details
      | Facet Group | <FACET_GROUP> |
      | Type        | String        |
      | Facetable   | No Facet      |

    Examples:
      | ATTRIBUTE_USAGE | FACET_GROUP       |
      | Product         | Product Attribute |
      | SKU             | SKU Attribute     |

  Scenario: Newly created SKU Option appears as potential facets
    When I select SkuOptions tab in the Catalog Editor of currently open Catalog
    And I create a new sku option with sku code testSkuOption and display name testSkuOption
    And I go to Configuration
    And I go to Stores
    And I edit store MOBEE in editor
    And I select Facets tab in the Store Editor
    Then I should see facet testSkuOption with the following details
      | Facet Group | SKU Option |