@jwtAuthorization
Feature: Catalog Browser role tests for search

  Background:
    Given I login using jwt authorization with the following details
      | scope      | MOBEE           |
      | roles      | catalog_browser |
      | first_name | John            |
      | last_name  | Smith           |

  Scenario: User can search for products and bundles
    When I search for keyword "htc"
    Then the element list contains items with display-names
      | HTC Evo 4G         |
      | SmartPhones Bundle |

  Scenario: Offer link permissions
    When I search for offer "SmartPhones"
    And I follow links element
    Then I should see the following links
      | availability |
      | definition   |
      | code         |
      | components   |
      | items        |
    But I should not see the following links
      | pricerange |

  Scenario: Facets link returns the correct facets
    When I search for the offer movie with page-size 5
    Then there is a facets link
    When I follow links facets
    Then the expected list of display-name matches the following list
      | Brand         |
      | Price         |
      | Category      |
      | Runtime       |
      | Rental Days   |
      | Screen Format |
      | Languages     |
      | Resolution    |

  Scenario: Filter search results by multiple choices within same facet
    When I search for the offer movie with page-size 20
    And I follow links facets
    Then the expected list of display-name matches the following list
      | Brand         |
      | Price         |
      | Category      |
      | Runtime       |
      | Rental Days   |
      | Screen Format |
      | Languages     |
      | Resolution    |
    When I open the element with field display-name and value Languages
    And I follow the link facetselector
    Then the expected facet choice list matches the following list
      #not doing all these to save time
      | value    | count |
      | EN       | 3     |
      | Japanese | 7     |
      | Chinese  | 4     |
    And there are no chosen links
    When I select the choice EN
    Then there is 1 link of rel chosen
    When I follow links offersearchresult
    Then the offer search results list contains items with display-names
      | Transformers Over 50          |
      | Transformers First Time Buyer |
      | Transformers Female           |
    When I follow links facets
    And I open the element with field display-name and value Languages
    And I follow the link facetselector
    And I select the choice Chinese
    Then there is 2 link of rel chosen
    When I follow links offersearchresult
    Then the offer search results list contains items with display-names
      | Extreme Movie Bundle          |
      | Rent Movies Bundle            |
      | RentMovieLowTVCombo           |
      | Back To The Future            |
      | Transformers Over 50          |
      | Transformers First Time Buyer |
      | Transformers Female           |

  Scenario: Filter search results across multiple facets
    When I search for the offer movie with page-size 20
    And I follow links facets
    Then the expected list of display-name matches the following list
      | Brand         |
      | Price         |
      | Category      |
      | Runtime       |
      | Rental Days   |
      | Screen Format |
      | Languages     |
      | Resolution    |
    When I open the element with field display-name and value Resolution
    And I follow the link facetselector
    Then the expected facet choice list matches the following list
      | value      | count |
      | 400 Pixels | 4     |
      | 720 Pixels | 4     |
    And there are no chosen links
    When I select the choice 400 Pixels
    Then there is 1 link of rel chosen
    When I follow links offersearchresult
    Then the offer search results list contains items with display-names
      | Movie Deal          |
      | New Movies          |
      | RentMovieLowTVCombo |
      | Best Series 2011    |
    When I follow links facets
    And I open the element with field display-name and value Languages
    And I follow the link facetselector
    Then there are no chosen links
    When I select the choice Chinese
    And I follow links offersearchresult
    Then the offer search results list contains items with display-names
      | RentMovieLowTVCombo |
