@httpCaching
Feature: HTTP Caching - Navigations

  Background:
    Given I login as a newly registered shopper

  Scenario: Navigation should have HTTP caching
    When I open the navigation category Games
    Then I should see the Cache-Control header in the response with a valid max age and private directive
    And I should see an ETag header in the response

  Scenario: Navigations list should have HTTP caching
    When I open the root navigations
    Then I should see the Cache-Control header in the response with a valid max age and private directive
    And I should see an ETag header in the response

  Scenario: Navigation lookup form should have HTTP caching
    When I retrieve the navigation lookup form
    Then I should see the Cache-Control header in the response with a valid max age and private directive
    And I should see an ETag header in the response