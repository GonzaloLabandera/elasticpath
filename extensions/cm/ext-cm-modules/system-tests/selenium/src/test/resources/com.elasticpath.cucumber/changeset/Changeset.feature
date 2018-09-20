@changeset @csproduct
Feature: General Change Set test

  Background:
    Given I sign in to CM as admin user

  Scenario: Update Change set name
    And I go to Change Set
    And I create a new change set NewTestChangeSet
    And I change the changeset name to EditedChangeSetName
    Then the changeset should have the edited name
	