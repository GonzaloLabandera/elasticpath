@jwtAuthorization
Feature: Account Management - Catalog Browser permissions

  Catalog Browsers have limited permissions to resources in the commerce domain.
  They have the ability to lookup, search for and navigate products, this excludes any pricing related information.
  Access to other resources that are not related to the ability to browse the catalog is also off limits (eg. Account Registration).

  Background:
    Given I login using jwt authorization with the following details
      | scope | MOBEE           |
      | roles | catalog_browser |

  Scenario: Catalog Browser cannot access root carts resource using URI
    Given I login as a registered shopper
    When I go to my carts
    And save the carts uri
    And I login using jwt authorization with the following details
      | roles | CATALOG_BROWSER |
    And attempt to access the carts uri
    Then the operation is identified as forbidden

  Scenario: Catalog Browser can only perform lookup, navigate, search and access profile
    When I navigate to root
    Then I should see the following links
      | lookups        |
      | navigations    |
      | defaultprofile |
      | searches       |
    Then I should not see the following links
      | defaultcart     |
      | data-policies   |
      | countries       |
      | newaccountform  |
      | defaultwishlist |
      | carts           |

  Scenario: Catalog Browser can only see product details (excluding price) and recommendations for other products
    When I look up an item with code alien_sku
    Then I should see the following links
      | availability    |
      | definition      |
      | code            |
      | offer           |
      | recommendations |
    But I should not see the following links
      | addtocartform       |
      | cartmemberships     |
      | price               |
      | appliedpromotions   |
      | addtowishlistform   |
      | wishlistmemberships |
      | addtocartforms      |

  Scenario: Catalog Browser can not see pricing information for offers
    When I lookup an offer with code bundleWithPhysicalAndDigitalComponents
    And follow the response
    Then I should see the following links
      | availability |
      | definition   |
      | code         |
      | components   |
      | items        |
    But I should not see the following links
      | pricerange |

  Scenario: Catalog Browser can not lookup purchases
    When I retrieve the lookups link point
    Then I should see the following links
      | batchitemslookupform  |
      | itemlookupform        |
      | navigationlookupform  |
      | batchofferslookupform |
      | offerlookupform       |
    But I should not see the following links
      | purchaselookupform |

  Scenario: Catalog Browser can access facets and sortattributes
    When I open the navigation category TV
    And I follow links offers
    Then there is a facets link
    And there is a sortattributes link

  Scenario: Catalog Browser cannot access createcartform using URI
    Given I login as a registered shopper
    When I go to create cart form
    And save the createcartform uri
    And I login using jwt authorization with the following details
      | roles | CATALOG_BROWSER |
    And attempt to access the createcartform uri
    Then the operation is identified as forbidden

  Scenario: Catalog Browser cannot POST to createcartform using URI
    Given I login as a registered shopper
    When I go to create cart form
    And save the createcartform uri
    And I login using jwt authorization with the following details
      | roles | CATALOG_BROWSER |
    And post to the saved createcartform uri
    Then the operation is identified as forbidden
