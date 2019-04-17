@regressionTest @catalogManagement @catalog
Feature: Manage Virtual Catalog

  Background:
    Given I sign in to CM as admin user

  Scenario: Create Edit and Delete new virtual catalog
    When I create a new virtual catalog with following details
      | catalogName           | language |
      | ATest Virtual Catalog | English  |
    Then newly created virtual catalog is in the list
    When I edit the virtual catalog name to a different name
    Then newly edited virtual catalog is in the list
    When I delete newly created virtual catalog
    Then newly created virtual catalog is deleted

  @smokeTest
  Scenario Outline: Add and Remove linked category in Virtual Catalog
    When I go to Catalog Management
    And I add new linked category to virtual catalog <virtual_catalog> with following data
      | <master catalog>  |
      | <linked category> |
    Then the linked category <linked category> should be added to catalog <virtual_catalog>
    When I remove linked category <linked category> from virtual catalog <virtual_catalog>
    Then <linked category> category is deleted

    Examples:
      | master catalog | linked category | virtual_catalog          |
      | Mobile Catalog | TV Series       | Rock Jam Virtual Catalog |

  @resetVirtualCatalogProduct
  Scenario: Exclude and Include product in Virtual Catalog
    Given there is an existing product 12 Angry Men in category Movies in virtual catalog Mobile Virtual Catalog
    When I exclude the product from the virtual catalog
    Then the product does not have Mobile Virtual Catalog in category assignment and merchandising associations
    When I include the product in the virtual catalog
    Then the product has Mobile Virtual Catalog in category assignment and merchandising associations

  Scenario Outline: I delete existing virtual catalog Rock Jam Virtual Catalog
    When I delete selected catalog Rock Jam Virtual Catalog in the list
    Then I should see the following error: <ERROR_MESSAGE>

    Examples:
      | ERROR_MESSAGE                     |
      | The catalog is in use by a store. |

  Scenario Outline: Newly created subcategories and products in Master Catalog are reflected in linked Virtual Catalog
    When I create new subcategory for category <category> in catalog <masterCatalog> with following data
      | categoryName | categoryType | storeVisible | attrLongTextName     | attrLongTextValue | attrDecimalName | attrDecimalValue | attrShortTextName | attrShortTextValue |
      | <category>   | Movies       | true         | Category Description | Long text value   | Category Rating | 1.1              | Name              | Short text value   |
    And I create new product for a newly created subcategory where parent category is <category> with following attributes
      | catalog         | productName | productType | taxCode | brand   | storeVisible | availability     | shippableType | priceList               | listPrice |
      | <masterCatalog> | Product     | Phones      | GOODS   | Samsung | true         | Always available | Shippable     | Mobile Price List (CAD) | 111.00    |
    Then newly created product is present in newly created subcategory under category <category> for <masterCatalog>
    And newly created subcategory is present in catalog <virtualCatalog> under category <category>
    Then newly created product is present in newly created subcategory under category <category> for <virtualCatalog>
    When I delete the newly created product
    Then the product is deleted
    And I open catalog management tab
    When I delete newly created subcategory
    Then newly created subcategory is deleted

    Examples:
      | masterCatalog  | category | virtualCatalog         |
      | Mobile Catalog | Movies   | Mobile Virtual Catalog |

  Scenario: Create Remove Category and add item to category in Virtual Catalog
    When I create new category for Mobile Virtual Catalog with following data
      | categoryName   | categoryType | storeVisible | attrLongTextName     | attrLongTextValue | attrDecimalName | attrDecimalValue | attrShortTextName | attrShortTextValue |
      | MTest Category | Movies       | true         | Category Description | Test Description  | Category Rating | 5.5              | Name              | Test Name          |
    And I expand Mobile Virtual Catalog catalog
    And I add existing product movie_222333 to newly created category
    Then the product with code movie_222333 has Mobile Virtual Catalog in category assignment and merchandising associations
    And I can see the Category Assignment tab of an existing product with product code movie_222333
    When I exclude category newly created in the Mobile Virtual Catalog catalog from category assignment list
    Then the product with code movie_222333 does not have Mobile Virtual Catalog in category assignment and merchandising associations
    When I delete newly created category
    Then newly created category is deleted