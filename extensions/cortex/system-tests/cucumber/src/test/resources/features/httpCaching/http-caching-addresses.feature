@httpCaching
Feature: HTTP Caching - Addresses

  Background:
    Given I authenticate as a registered shopper harry.potter@elasticpath.com with the default scope

  Scenario: Address form should have HTTP caching
    When I get address form
    Then I should see the Cache-Control header in the response with a valid max age and private directive
    And I should see an ETag header in the response

  Scenario: Addresses list should have HTTP caching
    When I view my profile
    And I follow links addresses
    Then I should see the Cache-Control header in the response with a valid max age and private directive
    And I should see an ETag header in the response