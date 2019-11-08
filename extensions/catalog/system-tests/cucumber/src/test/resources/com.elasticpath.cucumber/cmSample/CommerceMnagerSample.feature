@cmsample
Feature: Cm sample test

  Background:
    Given I sign in to CM as admin user
    And I go to Catalog Management

  #This test contains examples of step definitions for Product, Category, CartItemModifierGroup and @cleanup* mechanism
  @cleanupCatalog @cleanupProduct @cleanUpCategory
  Scenario: Create a product of new cart item modifier group
    Given a new catalog with following details
      | catalogName   | language | brand     |
      | ATest Catalog | English  | TestBrand |
    And I select CartItemModifierGroups tab in the Catalog Editor
    And I create a new modifier group with group code groupCode and group name groupName
    And I add a new field to this group with following details
      | fieldCode | fieldName     | fieldType  | shortTextSize |
      | textField | textFieldName | Short Text | 15            |
    And newly created group is in the list
    And I select ProductTypes tab in the Catalog Editor
    When I create a new product type TestProductType with newly created cart item modifier group
    Then newly created product type is in the list
    And I select CategoryTypes tab in the Catalog Editor
    And I create a new category type TestCat Type with following attributes
      | Category Description |
      | Name                 |
    When I create new category of the new category type
      | categoryName   | storeVisible | attrLongTextName     | attrLongTextValue   | attrDecimalName | attrDecimalValue | attrShortTextName | attrShortTextValue   |
      | ATest Category | true         | Category Description | long text attribute | Category Rating | 1.1              | Name              | short text attribute |
    And I create new product with following attributes
      | productName | taxCode | brand  | storeVisible | availability     | shippableType |
      | TestProduct | DIGITAL | Disney | true         | Always available | Digital Asset |
    Then the newly created product is in the list

  #This test contains examples of step definitions for Catalog attribute
  @cleanupCatalog
  Scenario Outline: Add / Edit / Delete catalog attribute
    When I create a new catalog with following details
      | catalogName   | language |
      | ATest Catalog | English  |
    And I select Attributes tab in the Catalog Editor
    When I create a new catalog attribute with following details
      | attributeName     | attributeUsage | attributeType | attributeRequired |
      | testAttributeName | Product        | Short Text    | true              |
    Then newly created catalog attribute is in the list
    When I edit the catalog attribute name
    Then edited catalog attribute is in the list
    When I close the editor and try to delete the newly created catalog
    Then I should see following error messages
      | <error-message-1> |
      | <error-message-2> |
    When I open the newly created catalog editor
    And I delete the newly created catalog attribute
    Then the newly created catalog attribute is deleted

    Examples:
      | error-message-1                        | error-message-2       |
      | Unable to delete the following catalog | The catalog is in use |

  #This test contains examples of step definitions for Brand
  @cleanupCatalog
  Scenario: Update, Edit, Delete new brand for an existing catalog
    Given I create a new catalog with following details
      | catalogName   | language |
      | ATest Catalog | English  |
    And I go to Catalog Management
    And I open the newly created catalog editor
    When I add a brand testBrand
    Then the brand testBrand is displayed in the brands table
    When I edit brand name to newBrandName for the newly added brand
    Then the brand newBrandName is displayed in the brands table
    When I delete brand newBrandName
    Then The brand newBrandName is deleted

  #This test contains examples of step definitions for Sku Option, Sku Option Value
  @cleanupCatalog
  Scenario: Add / Edit / Delete Sku Options
    When I create a new catalog with following details
      | catalogName   | language |
      | ATest Catalog | English  |
    And I select SkuOptions tab in the Catalog Editor
    When I create a new sku option with sku code skuCode and display name testName
    Then newly created sku option is in the list
    When I create a new sku option value with sku code skuValueCode and display name valueTestName
    Then newly created sku option value is in the list
    When I edit the sku option name
    Then updated sku option name is in the list
    When I delete the newly created sku option
    Then the newly created sku option is deleted

  #This test contains examples of step definitions for Bundle
  Scenario: Create Edit Delete new bundle for existing category
    When I create a new bundle with following attributes
      | catalog        | category    | productName | bundlePricing | productType | brand  | storeVisible | availability     | attrShortTextMulti | attrShortTextMultiValue | attrInteger | attrIntegerValue | attrDecimal     | attrDecimalValue | bundleProductSKUList | priceList               | listPrice |
      | Mobile Catalog | Accessories | Bundle      | Assigned      | Movies      | Disney | true         | Always available | Languages          | English                 | Runtime     | 120              | Viewer's Rating | 5.5              | tt64464fn,tt0162661  | Mobile Price List (CAD) | 311.00    |
    Then the newly created bundle exists and contains the added items
    When I delete item with product code tt64464fn from the bundle
    Then the bundle item with product code tt64464fn is deleted
    When I add new Item with product code tt64464fn to the bundle
    Then the bundle item with product code tt64464fn exists in the bundle
    When I update bundle item with product code tt0162661 quantity to 3
    Then the bundle item with product code tt0162661 has quantity of 3
    When I delete the newly created bundle
    Then the bundle is deleted