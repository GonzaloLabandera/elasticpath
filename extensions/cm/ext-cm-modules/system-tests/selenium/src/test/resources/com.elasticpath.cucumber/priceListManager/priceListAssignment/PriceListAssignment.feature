@regressionTest @priceListManager @priceListAssignment
Feature: Price List Assignment

  @smokeTest @cleanupPriceListAssignment @cleanupPriceList
  Scenario: Create new Price List Assignment with newly created Price List
    Given I sign in to CM as admin user
    And I go to Price List Manager
    When I create a new price list with description test price list and currency CAD
    Then I should see the newly created price list
    When I create Price List Assignment with newly created price list for catalog ToastieCatalog
    Then I should see newly created Price List Assignment in search result

  @cleanupPriceListAssignment @cleanupPriceList
  Scenario: Delete Price List Assignment
    Given I sign in to CM as admin user
    And I go to Price List Manager
    And I have a Price List Assignment for catalog Mobile Catalog
    When I delete newly created price list assignment
    Then the deleted price list assignment no longer exists

  Scenario: Search Price List Assignment by Catalog
    Given I sign in to CM as admin user
    And I go to Price List Manager
    When I select Price List Assignments tab
    And I search Price List Assignments for catalog ToastieCatalog
    Then Search result should contain following Price List Assignments
      | Europe Pricing for Toastie  |
      | Default Pricing for Toastie |

  @cleanupPriceListAssignment @cleanupPriceList
  Scenario: Price List with a Price List Assignment can't be deleted
    Given I sign in to CM as admin user
    And I go to Price List Manager
    When I create a new price list with description test price list and currency CAD
    And I should see the newly created price list
    And I create Price List Assignment with newly created price list for catalog ToastieCatalog
    And I should see newly created Price List Assignment in search result
    When I select Price List tab
    And I delete the newly created price list
    Then I should see the following error: Delete a price list is prohibited

  @cleanupPriceListAssignment @cleanupPriceList
  Scenario: Edit Price List Assignment
    Given I sign in to CM as admin user
    And I go to Price List Manager
    When I create a new price list with description test price list and currency CAD
    And I create Price List Assignment with newly created price list for catalog ToastieCatalog
    When I edit the pricelist assignment description to "edit test"
    And I open the pricelist assignment
    Then the pricelist assignment description is "edit test"

  Scenario: Visible stores in Price List Assignment wizard with user that has access to all stores
    Given I sign in to CM as admin user
    And I go to Price List Manager
    And I have a Price List Assignment for catalog Tokenee Catalog
    When I open the pricelist assignment
    And I skip to "stores" selection
    Then Available Stores should contain all Stores

  Scenario: Visible stores in Price List Assignment wizard with user that has access to subset of stores
    Given I sign in to CM as tokenee_product_pricing_manager with password 111111
    And I go to Price List Manager
    And I have a Price List Assignment for catalog Tokenee Catalog
    When I open the pricelist assignment
    And I skip to "stores" selection
    Then Available Stores should contain the following Stores
      | Tokenee |

  @cleanupPriceListAssignment @cleanupPriceList
  Scenario: Price List Assignment assigned to product belonging to many stores only has assigned price visible to specific store
    Given the item price for sku mk34abef is $58.09 when customer jimmy.james@elasticpath.com retrieve the item price in store Tokenee
    And I sign in to CM as tokenee_product_pricing_manager with password 111111
    And I go to Price List Manager
    And I have a new Price List
    And I open the newly created price list editor
    And I add a list price 50.00 for sku code mk34abef
    When I create Price List Assignment with newly created price list using the following values
      | catalog  | Tokenee Catalog |
      | priority | 1               |
    Then the item price for sku mk34abef is $58.09 when customer harry.potter@elasticpath.com retrieve the item price in store Mobee
    Then the item price for sku mk34abef is $50.00 when customer jimmy.james@elasticpath.com retrieve the item price in store Tokenee

  @cleanupPriceListAssignment
  Scenario: Create Price List Assignment with Customer Conditions
    Given I sign in to CM as admin user
    And I go to Price List Manager
    And I create Price List Assignment with Customer conditions that has the following values
      | price list        | Kobee Price List                                                                  |
      | catalog           | Mobile Virtual Catalog                                                            |
      | priority          | 8                                                                                 |
      | shopper condition | Customer Profile -> are registered customers -> matching (case sensitive) -> True |
    And I skip to "stores" selection
    And I assign the price list assignment to the following stores
      | Kobee |
      | Mobee |
    When I click create price list assignment wizard save button
    Then I should see newly created Price List Assignment in search result