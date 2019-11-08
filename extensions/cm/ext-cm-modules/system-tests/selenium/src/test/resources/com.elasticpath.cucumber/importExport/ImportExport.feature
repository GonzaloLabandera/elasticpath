@importExport
  #TODO PB-7016: Imported product DB verification should be replaced with verification in CM after bug fix.
  #TODO PB-7017: ImportExport.runExport() should be updated after bug fix and DBConnector.updateSearchUrl() should be deleted.

Feature: Import Export tool

  @copyImportExportFolder @lockAndFinalize @deleteCopiedFolder
  Scenario: Export Import new product with stage1 and stage2
    When I sign in to the export environment CM as admin user
    And I go to Catalog Management
    When I create new product with following attributes
      | catalog        | category    | productName | productType | taxCode | brand  | storeVisible | availability     | attrShortTextMulti | attrShortTextMultiValue | attrInteger | attrIntegerValue | attrDecimal     | attrDecimalValue | shippableType | priceList               | listPrice |
      | Mobile Catalog | Accessories | Product     | Movies      | DIGITAL | Disney | true         | Always available | Languages          | English                 | Runtime     | 120              | Viewer's Rating | 5.5              | Digital Asset | Mobile Price List (CAD) | 111.00    |
    Then the newly created product is in the list
    When I run the export for newly created product
    Then the export Total Number Of Failures should be 0
    When I sign in to the import environment CM as admin user
    And I create a new change set Add_Prod
    And I run the import with stage1 flag
    Then the import Total Number Of Failures should be 0
    And I should see newly created product in the change set
    And the product does not exist in the data base
    When I run the import with stage2 flag
#    Then the newly created product is in the list
    Then the product exists in the data base

  @copyImportExportFolder @lockAndFinalize @deleteCopiedFolder
  Scenario: Export Import new product
    When I sign in to the export environment CM as admin user
    And I go to Catalog Management
    When I create new product with following attributes
      | catalog        | category    | productName | productType | taxCode | brand  | storeVisible | availability     | attrShortTextMulti | attrShortTextMultiValue | attrInteger | attrIntegerValue | attrDecimal     | attrDecimalValue | shippableType | priceList               | listPrice |
      | Mobile Catalog | Accessories | Product     | Movies      | DIGITAL | Disney | true         | Always available | Languages          | English                 | Runtime     | 120              | Viewer's Rating | 5.5              | Digital Asset | Mobile Price List (CAD) | 111.00    |
    Then the newly created product is in the list
    When I run the export for newly created product
    Then the export Total Number Of Failures should be 0
    When I sign in to the import environment CM as admin user
    And I create a new change set Add_Prod
    And I run the import with change set guid
    Then the import Total Number Of Failures should be 0
    And I should see newly created product in the change set
#    And the newly created product is in the list
    And the product exists in the data base

  @copyImportExportFolder @deleteCopiedFolder
  Scenario: Export Import all data - same server
    When I run the export
    Then the Total Number Of following should be 0
      | Failures |
      | Warnings |
      | Comments |
    And the number of exported objects should be more than follows
      | Total Number Of Objects | 1700 |
      | products                | 200  |
      | bundles                 | 45   |
      | categories              | 20   |
      | promotions              | 35   |
      | coupon_sets             | 5    |
      | condition_rules         | 35   |
    When I run the import in the same data base
    Then the import Total Number Of Failures should be 0
    And the number of exported and imported objects should be same
