@references
Feature: References

  Background:
    Given I authenticate with BUYER_ADMIN username usertest3@elasticpath.com and password password and role REGISTERED in scope mobee

  Scenario: User can review account buyer roles
    Given I navigate links references -> buyerroles
	Then I get back the list of roles

  Scenario: User can review countries
    Given I navigate links references -> countries
    Then I get back all 39 supported countries


