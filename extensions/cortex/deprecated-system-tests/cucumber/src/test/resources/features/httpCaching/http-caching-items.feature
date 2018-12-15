@httpCaching
Feature: HTTP Caching - Items

  Background:
    Given I login as a newly registered shopper

  Scenario Outline: An item should have HTTP caching
    When I search for item name <ITEM_NAME>
    Then I should see the Cache-Control header in the response with a valid max age and private directive
    And I should see an ETag header in the response

    Examples:
      | ITEM_NAME |
      | Twilight  |