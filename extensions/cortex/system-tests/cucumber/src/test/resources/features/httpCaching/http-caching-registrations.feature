@httpCaching
Feature: HTTP Caching - Registrations

  Scenario: New account registration form should have HTTP caching
    When I login as a public shopper
    And I navigate to registration form
    Then I should see the Cache-Control header in the response with a valid max age and private directive
    And I should see an ETag header in the response
