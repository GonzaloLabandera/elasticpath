@jwtAuthorization
Feature: Catalog Browser role tests for lookup

  Background:
    Given I login using jwt authorization with the following details
      | scope      | MOBEE           |
      | roles      | catalog_browser |
      | first_name | John            |
      | last_name  | Smith           |

  Scenario Outline: User can lookup product 
    When I look up an item with code <ITEM_CODE>
    Then I should see item name is <ITEM_NAME>
    Examples:
      | ITEM_CODE | ITEM_NAME |
      | alien_sku | Alien     |

  Scenario Outline: User can lookup products in batches 
    When I submit a batch of sku codes <SKU_CODES>
    Then the batch lookup returns the correct <SKU_CODES>
    Examples:
      | SKU_CODES                    |
      | ["alien_sku","physical_sku"] |

  Scenario: Item link permissions
    When I look up an item with code alien_sku
    Then I should see the following links
      | availability    |
      | definition      |
      | code            |
      | recommendations |
    But I should not see the following links
      | addtocartform       |
      | cartmemberships     |
      | price               |
      | appliedpromotions   |
      | addtowishlistform   |
      | wishlistmemberships |

  Scenario: Multi-sku product
    When I look up an item with code tt64464fn_hd
    And I change the multi sku selection by Video Quality and select choice Standard Definition
    Then the item code is tt64464fn_sd

  Scenario: Lookup an offer with product code and verify code is correct
    Given I am logged in as a public shopper
    When I lookup an offer with code alien
    And follow the response
    Then There is a code link with code alien

  Scenario: Lookup Offers from a list of valid product codes
    When I submit a batch of product codes ["alien", "gravity"]
    Then there are 2 links of rel element
    Then the element list contains items with display-names
      | Alien   |
      | Gravity |
