@jwtAuthorization
Feature: Catalog Browser role tests

  Background:
    Given I login using jwt authorization with the following details
      | scope      | MOBEE           |
      | roles      | catalog_browser |
      | first_name | John            |
      | last_name  | Smith           |

  Scenario: Root link restrictions
    When I navigate to root
    Then I should see the following links
      | lookups        |
      | navigations    |
      | defaultprofile |
      | searches       |
    But I should not see the following links
      | defaultcart     |
      | data-policies   |
      | countries       |
      | newaccountform  |
      | defaultwishlist |

  Scenario: User can only view names on profile
    When I view my profile
    Then the field family-name contains value Smith
    And the field given-name contains value John
    And I should see the field links is empty

  Scenario: Navigate to items in node
    When I open the root navigations
    Then the expected navigation list exactly matches the following
      | Games               |
      | Accessories         |
      | Smartphones         |
      | Movies              |
      | TV                  |
      | GiftCertificate     |
      | phone_plan          |
      | AcceptanceTestItems |

  Scenario: User can retrieve navigation using navigationlookupform
    When I look up navigation item with category code Movies
    Then the category Movies that has no sub-category is at the expected category level and has items

  Scenario: Category with featured offers displays all featured offers
    When I open the navigation category Movies
    And I follow the link featuredoffers
    Then the element list contains items with display-names
      | Die Hard      |
      | Casablanca    |
      | Avatar        |
      | Movie Deal    |
      | Sleepy Hallow |

  Scenario: Facets link returns the correct facets
    When I open the navigation category TV
    And I follow links offers
    And I zoom the facets with zoom element
    Then there are the following facets
      | Brand       |
      | Price       |
      | Runtime     |
      | Rental Days |
      | Languages   |
      | Resolution  |

  Scenario: Facet values for non Category facets display values related to the offers in that Category
    When I open the navigation category TV
    And I follow links offers
    And I follow links facets
    And I open the element with field display-name and value Languages
    And I zoom the facetselector with zoom choice:description
    Then there are the following facet values
      | Italien  | 1 |
      | Anglais  | 1 |
      | Hindi    | 3 |
      | English  | 3 |
      | Allemand | 1 |
      | Russian  | 3 |
      | French   | 2 |
      | Français | 1 |