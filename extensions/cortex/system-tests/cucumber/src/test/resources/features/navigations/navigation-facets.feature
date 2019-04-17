@navigations
Feature: Retrieve offer node of navigation node

  Background:
    Given I am logged in as a public shopper

  Scenario: Category with no sub-categories does not have Category facets
    When I open the navigation category GiftCertificate
    And I follow links offers
    And I zoom the facets with zoom element
    Then there are the following facets
      | Brand       |
      | Price       |

  Scenario: Offers in navigation initially has no chosen Category facet values
    When I open the navigation category Games
    And I follow links offers
    And I follow links facets
    And I open the element with field display-name and value Category
    And I follow links facetselector
    Then there are no chosen links

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

  Scenario: No selected facets for non-category facet
    When I open the navigation category TV
    And I follow links offers
    And I follow links facets
    And I open the element with field display-name and value Languages
    And I follow links facetselector
    Then there is no chosen link found

  Scenario: Check the ability to select multiple facet values from the same facet
    When I open the navigation category Movies
    And I follow links offers
    And I follow links facets
    And I open the element with field display-name and value Price
    And I follow links facetselector
    When I select the choice $200 and Above
    When I follow links offersearchresult
    Then the offer search results list contains items with display-names
      | RentMovieLowTVCombo |
    And I follow links facets
    And I open the element with field display-name and value Price
    And I follow links facetselector
    When I select the choice $50 to $200
    And  I zoom the offersearchresult with zoom next
    Then the offer search results list contains items with display-names
      | Empty Bundle         |
      | RentMovieLowTVCombo  |
      | Rent Movies Bundle   |
      | New Movies           |
      | Movie Deal           |
      | Super Bundle         |
      | Gravity              |
      | Just Released Movies |
      | Extreme Movie Bundle |
    And I follow links facets
    And I open the element with field display-name and value Price
    And I follow links facetselector
    When I unselect the choice $200 and Above
    And  I zoom the offersearchresult with zoom next
    Then the offer search results list contains items with display-names
      | Empty Bundle         |
      | Rent Movies Bundle   |
      | New Movies           |
      | Movie Deal           |
      | Gravity              |
      | Super Bundle         |
      | Just Released Movies |
      | Extreme Movie Bundle |

  Scenario: Check the ability to select multiple facet values from the different facet
    When I open the navigation category Movies
    And I follow links offers
    And I follow links facets
    And I open the element with field display-name and value Price
    And I follow links facetselector
    When I select the choice $50 to $200
    When I follow links offersearchresult
    And I zoom the current url next
    Then the offer search results list contains items with display-names
      | Empty Bundle         |
      | Rent Movies Bundle   |
      | New Movies           |
      | Movie Deal           |
      | Gravity              |
      | Just Released Movies |
      | Super Bundle         |
      | Extreme Movie Bundle |
    And I follow links facets
    And I open the element with field display-name and value Languages
    And I follow links facetselector
    When I select the choice Italien
    And I follow links offersearchresult
    Then the offer search results list contains items with display-names
      | Super Bundle         |
      | Rent Movies Bundle   |
      | Extreme Movie Bundle |
    And I follow links facets
    And I open the element with field display-name and value Languages
    And I follow links facetselector
    When I unselect the choice Italien
    And  I zoom the offersearchresult with zoom next
    Then the offer search results list contains items with display-names
      | Empty Bundle         |
      | Rent Movies Bundle   |
      | New Movies           |
      | Movie Deal           |
      | Gravity              |
      | Just Released Movies |
      | Super Bundle         |
      | Extreme Movie Bundle |

  Scenario: Verify that the category facet only contains the sub-categories
    When I open the navigation category Games
    And I follow links offers
    And I follow links facets
    And I open the element with field display-name and value Category
    And I follow links facetselector
    Then the expected facet choice list matches the following list
      | value          | count |
      | Mobile Games   | 7     |
      | Android Games  | 1     |
      | IPhone Games   | 3     |