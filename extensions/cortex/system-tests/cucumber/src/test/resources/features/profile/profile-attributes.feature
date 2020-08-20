@profile
Feature: Profile

  Background:
    Given I login as a newly registered shopper

  Scenario: Able to access attributes sub-resource
    Given I view my profile attributes
    Then the HTTP status is OK
    And I should not see profile attribute given-name
    And I should not see profile attribute email