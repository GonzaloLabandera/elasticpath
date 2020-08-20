Feature: Import/Export API: Product

  Scenario: Export products with ProductCode filter
    Given I export Product records with query "FIND Product WHERE ProductCode='tt0034583'" from the API
    Then response has http status 200
    And response has exactly 1 product elements

  @bug
  Scenario: Export products with ProductName filter
    Given I export Product records with query "FIND Product WHERE ProductName[en] = 'Samsung Galaxy Q'" from the API
    Then response has http status 200
    And response has exactly 1 product elements

  Scenario: Export products with SkuCode filter
    Given I export Product records with query "FIND Product WHERE SkuCode='tt0114369_hdb'" from the API
    Then response has http status 200
    And response has exactly 1 product elements

  Scenario: Export products with ProductStartDate filter
    Given I export Product records with query "FIND Product WHERE ProductStartDate < '2012-01-01T00:00:00-08:00'" from the API
    Then response has http status 200
    And response has at least 10 product elements

  Scenario: Export products with ProductEndDate filter
    Given I export Product records with query "FIND Product WHERE ProductEndDate > '2018-01-01T00:00:00-08:00'" from the API
    Then response has http status 200
    And response has at least 1 product elements

  Scenario: Export products with LastModifiedDate filter
    Given I export Product records with query "FIND Product WHERE LastModifiedDate > '2020-01-01T00:00:00-08:00'" from the API
    Then response has http status 200
    And response has at least 10 product elements

  Scenario: Export products with ProductActive filter
    Given I export Product records with query "FIND Product WHERE ProductActive = false" from the API
    Then response has http status 200
    And response has at least 1 product elements

  Scenario: Export products with BrandCode filter
    Given I export Product records with query "FIND Product WHERE BrandCode = 'Jawbone'" from the API
    Then response has http status 200
    And response has at least 1 product elements

  Scenario: Export products with BrandName filter
    Given I export Product records with query "FIND Product WHERE BrandName[fr_CA] = 'Jawbone_fr'" from the API
    Then response has http status 200
    And response has at least 1 product elements

  @bug
  Scenario: Export products with Price filter
    Given I export Product records with query "FIND Product WHERE Price = 10" from the API
    Then response has http status 200
    And response has at least 1 product elements

  Scenario: Export products with StoreCode filter
    Given I export Product records with query "FIND Product WHERE StoreCode = 'mobee'" from the API
    Then response has http status 200
    And response has at least 10 product elements

  Scenario: Export products with CategoryCode filter
    Given I export Product records with query "FIND Product WHERE CategoryCode = 'MobileGames'" from the API
    Then response has http status 200
    And response has at least 1 product elements

  Scenario: Export products with CategoryName filter
    Given I export Product records with query "FIND Product WHERE CategoryName[en] = 'Mobile Games'" from the API
    Then response has http status 200
    And response has at least 1 product elements

  @bug
  Scenario: Export products with AttributeName filter
    Given I export Product records with query "FIND Product WHERE AttributeName{00007} = '99'" from the API
    Then response has http status 200
    And response has at least 1 product elements

  @bug
  Scenario: Export products with SkuAttributeName filter
    Given I export Product records with query "FIND Product WHERE SkuAttributeName{A00015} = 'widescreen'" from the API
    Then response has http status 200
    And response has at least 1 product elements

  Scenario: Export product category associations
    Given I export ProductCategoryAssociation records with parent Product and query "FIND Product WHERE ProductCode='tt0034583'" from the API
    Then response has http status 200
    And response has exactly 3 category elements

  Scenario: Export product associations
    Given I export ProductAssociation records with parent Product and query "FIND Product WHERE ProductCode='singleAssociationSourceProduct'" from the API
    Then response has http status 200
    And response has exactly 2 productassociation elements

  Scenario: Export product inventory
    Given I export Inventory records with parent Product and query "FIND Product WHERE ProductCode='physicalProductWithLimitedInventory'" from the API
    Then response has http status 200
    And response has exactly 1 sku elements

  Scenario: Export product bundles
    Given I export ProductBundle records with parent Product and query "FIND Product WHERE ProductCode='bundlePhysicalDigitalItemsAssignedPrice'" from the API
    Then response has http status 200
    And response has exactly 1 bundle elements
