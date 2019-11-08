# language: en
@importAttributePolicy
Feature: Import Attribute Policies
  As Operations, I want to import attribute policies from the file system

  Scenario: Import Attribute Policies
    Given the attribute policy import data has been emptied out
    And the attribute policies to import of
      | guid              | policyKey    | policyPermission |
      | hidden-none       | HIDDEN       | NONE             |
      | visible-emit      | READ_ONLY    | EMIT             |
      | default-emit      | DEFAULT      | EMIT             |
      | default-edit      | DEFAULT      | EDIT             |
    When importing attribute policies with the importexport tool
    Then the following attribute policies exist
      | guid              | policyKey    | policyPermission |
      | hidden-none       | HIDDEN       | NONE             |
      | visible-emit      | READ_ONLY    | EMIT             |
      | default-emit      | DEFAULT      | EMIT             |
      | default-edit      | DEFAULT      | EDIT             |