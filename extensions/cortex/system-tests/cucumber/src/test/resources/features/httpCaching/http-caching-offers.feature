@httpCaching
Feature: HTTP Caching - Offers

  Background:
    Given I login as a new public shopper

  Scenario Outline: Offer should have HTTP caching
    When I search and open the offer for offer name <OFFER_NAME>
    Then I should see the Cache-Control header in the response with a valid max age and private directive
    And I should see an ETag header in the response
    When I go to offer code
    Then I should see the Cache-Control header in the response with a valid max age and private directive
    And I should see an ETag header in the response
    When I search and open the offer for offer name <OFFER_NAME>
    And I go to offer definition
    Then I should see the Cache-Control header in the response with a valid max age and private directive
    And I should see an ETag header in the response

    Examples:
      | OFFER_NAME |
      | Twilight  |