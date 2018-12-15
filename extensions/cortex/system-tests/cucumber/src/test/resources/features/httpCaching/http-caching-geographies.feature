@httpCaching
Feature: HTTP Caching - Geographies

  Background:
    Given I login as a newly registered shopper

  Scenario: Countries list should have HTTP caching
    When I request the list of countries
    Then I should see the Cache-Control header in the response with a valid max age and private directive
    And I should see an ETag header in the response

  Scenario: A country should have HTTP caching
    When I request the list of countries
    And one of the countries is Canada
    Then I should see the Cache-Control header in the response with a valid max age and private directive
    And I should see an ETag header in the response

  Scenario: Regions list should have HTTP caching
    When I request the list of regions for Canada in scope mobee
    Then I should see the Cache-Control header in the response with a valid max age and private directive
    And I should see an ETag header in the response

  Scenario: A region should have HTTP caching
    When I request region BC
    Then I should see the Cache-Control header in the response with a valid max age and private directive
    And I should see an ETag header in the response