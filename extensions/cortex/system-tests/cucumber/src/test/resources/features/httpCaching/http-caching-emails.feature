@httpCaching
Feature: HTTP Caching - Emails

  Scenario: Add email form should have HTTP caching
    When I login as a public shopper
    And I navigate to add email form
    Then I should see the Cache-Control header in the response with a valid max age and private directive
    And I should see an ETag header in the response
