@facets
Feature: Offer Search facets

  Background:
    Given I am logged in as a public shopper

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

  Scenario: Facet search multiple choices within same facet
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
      | value    | count |
      | EN       | 3     |
      | Japanese | 7     |
      | Chinese  | 4     |
      | English  | 35    |
      | Spanish  | 6     |
    And there are no chosen links
    When I select the choice EN
    Then there is 1 link of rel chosen
    When I follow links offersearchresult
    Then the offer search results list contains items with display-names
      | Transformers Over 50          |
      | Transformers First Time Buyer |
      | Transformers Female           |
    And I follow links facets
    When I open the element with field display-name and value Languages
    And I follow the link facetselector
    When I select the choice Chinese
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

  Scenario: Facet search across multiple facets
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