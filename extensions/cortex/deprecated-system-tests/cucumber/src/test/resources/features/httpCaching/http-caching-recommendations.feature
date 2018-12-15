@httpCaching
Feature: HTTP Caching - Recommendations

  Background:
    Given I login as a newly registered shopper

  Scenario Outline: Item recommendation groups list should have HTTP caching
    When I search for item name <ITEM_NAME>
    And I go to recommendations for an item
    Then I should see the Cache-Control header in the response with a valid max age and private directive
    And I should see an ETag header in the response

    Examples:
      | ITEM_NAME |
      | Twilight  |