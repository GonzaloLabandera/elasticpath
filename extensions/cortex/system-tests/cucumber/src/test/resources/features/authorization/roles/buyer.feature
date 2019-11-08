@jwtAuthorization
Feature: Account Management - Buyer permissions

  Buyers have permissions to access to all resources and operations available to REGISTERED shoppers in the commerce domain.

  Background:
    Given I login using jwt authorization with the following details
      | scope | MOBEE |
      | roles | buyer |

  Scenario: Buyer can access all root links
    When I navigate to root
    Then I should see the following links
      | defaultcart     |
      | data-policies   |
      | countries       |
      | lookups         |
      | navigations     |
      | defaultprofile  |
      | newaccountform  |
      | searches        |
      | defaultwishlist |
      | carts           |

  Scenario: Buyer can access all item links
    When I look up an item with code alien_sku
    Then I should see the following links
      | availability        |
      | addtocartform       |
      | addtocartforms       |
      | cartmemberships     |
      | definition          |
      | code                |
      | offer               |
      | price               |
      | appliedpromotions   |
      | recommendations     |
      | addtowishlistform   |
      | wishlistmemberships |

  Scenario: Buyer can access all offer links
    When I lookup an offer with code bundleWithPhysicalAndDigitalComponents
    And follow the response
    Then I should see the following links
      | availability |
      | definition   |
      | code         |
      | components   |
      | items        |
      | pricerange   |

  Scenario: Buyer can access all lookups
    When I retrieve the lookups link point
    Then I should see the following links
      | batchitemslookupform  |
      | itemlookupform        |
      | navigationlookupform  |
      | batchofferslookupform |
      | offerlookupform       |
      | purchaselookupform    |

  Scenario: Buyer can access facets and sortattributes
    When I open the navigation category TV
    And I follow links offers
    Then there is a facets link
    And there is a sortattributes link