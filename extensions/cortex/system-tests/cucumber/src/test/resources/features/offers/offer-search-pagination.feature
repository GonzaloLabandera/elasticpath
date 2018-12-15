@offer
Feature: Offer Search pagination

  Scenario: Offer search result pagination and navigation
    Given I am logged into scope searchbee as a public shopper
    And I search for offer movie
    And there are 5 links of rel element
    And there are no previous links
    And there is a next link
    And I follow links next
    When there are 5 links of rel element
    And there is a previous link
    And there is a next link
    And I follow links next
    Then there are 2 links of rel element
    And there is a previous link
    And there are no next links

  Scenario Outline: Number of offer search results per page matches the custom page size result
    Given I am logged into scope searchbee as a public shopper
    When I search for the offer movie with page-size <SEARCH_PAGE_SIZE>
    Then the field pagination contains value <PAGE_SIZE>
    And the field pagination contains value <PAGES>
    And the field pagination contains value <RESULTS>
    And the field pagination contains value <RESULTS_ON_PAGE>
    And there are <NUMBER_OF_LINKS> links of rel element

    Examples:
      | SEARCH_PAGE_SIZE | PAGE_SIZE    | PAGES   | RESULTS    | RESULTS_ON_PAGE    | NUMBER_OF_LINKS |
      | 2                | page-size=2  | pages=6 | results=12 | results-on-page=2  | 2               |
      | 20               | page-size=20 | pages=1 | results=12 | results-on-page=12 | 12              |

  Scenario: Offer Search returns results for empty space keyword
    Given I am logged into scope searchbee as a public shopper
    When I search for the offer ' ' with page-size 999
    Then the field pagination contains value page-size=999
    And the field pagination contains value pages=1
    And the field pagination contains value results=13
    And the field pagination contains value results-on-page=13
    And there are 13 links of rel element

  Scenario: Offer Search form - Bad Request when POST invalid values in optional field in request body
    Given I am logged into scope mobee as a public shopper
    When I POST to the offer search form the keyword movie with page-size 0
    Then the HTTP status is bad request
    When I POST to the offer search form the keyword movie with page-size -1
    Then the HTTP status is bad request
    When I POST to the offer search form with a 5000 char keyword
    Then the HTTP status is bad request

  Scenario: Offer Search form return 500 error when listPagination setting is set to zero
    Given I am logged into scope kobee as a public shopper
    When I follow the root searches link
    And I follow links offersearchform
    Then the HTTP status is server failure
