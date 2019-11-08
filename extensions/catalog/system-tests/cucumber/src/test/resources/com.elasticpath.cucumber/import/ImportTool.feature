@import @regression
Feature: Import tool tests

  Background:
    Given Import tool is successfully run as a part of data population mechanism

  Scenario: check brand projection of existing domain object after import tool is run for this domain object
    When I randomly select 5 brand domain objects
    Then corresponding projections were created in the system

  Scenario: check option projection of existing domain object after import tool is run for this domain object
    When I randomly select 5 option domain objects
    Then corresponding projections were created in the system

  Scenario: check fieldMetadata projection of existing domain object after import tool is run for this domain object
    When I randomly select 5 fieldMetadata domain objects
    Then corresponding projections were created in the system

  Scenario: check attribute projection of existing domain object after import tool is run for this domain object
    When I randomly select 5 attribute domain objects
    Then corresponding projections were created in the system