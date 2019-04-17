@navigations
Feature: Retrieve offer node of navigation node

  Scenario: Navigation has a link to offer resource
    Given I am logged in as a public shopper
    When I open the navigation category GiftCertificate
    Then there is an offers link

  Scenario: Navigation offer resource contains one offer
    Given I am logged in as a public shopper
    When I open the navigation category GiftCertificate
    And I follow links offers
    Then there are 1 links of rel element

  Scenario:  Navigation Search Results for Offer pagination
    Given I am logged in as a public shopper
    When I open the navigation category Games
    And I follow links offers
    Then there are 5 links of rel element
    And there is a next link
    And there is a facets link
    But there are no previous links
    And there are no applied-facets links
    When I follow the link next
    Then there is an element link
    And there is a previous link
    And there is a facets link
    But there are no applied-facets links

  Scenario: Offer resource links to required resources in navigation
    Given I am logged in as a public shopper
    When I open the navigation category TV
    And I follow links offers
    And I follow the 1 link with rel element
    Then there is a availability link
    And there is a definition link
    And there is a code link
    And there is a items link
    And there is a pricerange link

  Scenario: Navigation offers shows offers from child categories
    Given I am logged into scope tobee123 as a public shopper
    When I open the navigation subcategory Games -> MobileGames
    And I zoom the offers with zoom element:code
    Then there are the following offers
      | GI29389         |
      | GM38829         |
      | GI29939         |
      | GM10293         |
      | plantsVsZombies |
      | GA19920         |
      | GI18293         |

  Scenario: Navigation offers does NOT show offers from parent categories
    Given I am logged in as a public shopper
    When I open the navigation subcategory Games -> MobileGames -> IPhoneGames
    And I zoom the offers with zoom element:code
    Then the json path pagination.results equals 3
    And there are the following offers
      | GI29389 |
      | GI29939 |
      | GI18293 |
