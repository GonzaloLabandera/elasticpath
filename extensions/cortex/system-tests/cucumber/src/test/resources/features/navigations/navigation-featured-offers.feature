@navigations @offer
Feature: Display Featured Offers in Navigation

  Background:
    Given I am logged in as a public shopper

  Scenario: Category with featured offers displays all featured offers
    When I open the navigation category Movies
    And I follow the link featuredoffers
    Then the element list contains items with display-names
      | Die Hard      |
      | Casablanca    |
      | Avatar        |
      | Movie Deal    |
      | Sleepy Hallow |

  Scenario: Category with no featured offers does not show the featuredoffers link
    When I open the navigation category Games
    Then there is no featuredoffers link found