@httpCaching
Feature: HTTP Caching - Searches

  Scenario: Keyword Search form should have HTTP caching
    When I login as a public shopper
    And I navigate to keyword search form
    Then I should see the Cache-Control header in the response with a valid max age and private directive
    And I should see an ETag header in the response

  Scenario: Offer Search form should have HTTP caching
    When I login as a public shopper
    And I navigate to offer search form
    Then I should see the Cache-Control header in the response with a valid max age and private directive
    And I should see an ETag header in the response
