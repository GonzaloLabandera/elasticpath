@changeset @csproduct
Feature: Product/Bundle with change set

  Background:
    Given I sign in to CM as admin user

  @lockAndFinalize
  Scenario: Create new digital product for existing category with change set
    And I go to Change Set
    And I create a new change set ChangeSetCI_Add_Product
    And I go to Catalog Management
    And I select newly created change set
    When I create new product with following attributes
      | catalog        | category    | productName | productType | taxCode | brand  | storeVisible | availability     | attrShortTextMulti | attrShortTextMultiValue | attrInteger | attrIntegerValue | attrDecimal     | attrDecimalValue | shippableType | priceList               | listPrice |
      | Mobile Catalog | Accessories | Product     | Movies      | DIGITAL | Disney | true         | Always available | Languages          | English                 | Runtime     | 120              | Viewer's Rating | 5.5              | Digital Asset | Mobile Price List (CAD) | 111.00    |
    Then the newly created product is in the list
    And I should see newly created product in the change set
    When I lock and finalize latest change set
    And I create a new change set ChangeSetCI_Delete_Product
    And I go to Catalog Management
    And I select newly created change set
    When I delete the newly created product
    Then the product is deleted
    And I should see deleted product in the change set

  @lockAndFinalize
  Scenario: Create new bundle for existing category with change set
    And I go to Change Set
    And I create a new change set ChangeSetCI_Add_Bundle
    And I go to Catalog Management
    And I select newly created change set
    And I create new bundle with following attributes
      | catalog        | category    | productName | bundlePricing | productType | brand  | storeVisible | availability     | attrShortTextMulti | attrShortTextMultiValue | attrInteger | attrIntegerValue | attrDecimal     | attrDecimalValue | codesList           |
      | Mobile Catalog | Accessories | Bundle      | Assigned      | Movies      | Disney | true         | Always available | Languages          | English                 | Runtime     | 120              | Viewer's Rating | 5.5              | tt64464fn,tt0162661 |
    And the newly created bundle exists and contains the added items
    Then I should see newly created bundle in the change set
    When I lock and finalize latest change set
    And I create a new change set ChangeSetCI_Delete_Bundle
    And I go to Catalog Management
    And I select newly created change set
    When I delete the newly created bundle
    And the bundle is deleted
    Then I should see deleted bundle in the change set
	
	