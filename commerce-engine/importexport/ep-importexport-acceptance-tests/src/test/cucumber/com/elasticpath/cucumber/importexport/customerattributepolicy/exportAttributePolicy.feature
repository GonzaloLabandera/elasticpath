# language: en
@exportAttributePolicy
Feature: Export Attribute Policies
  As Operations, I want to export attribute policies to the file system.

  Scenario: Export Attribute Policies
    When exporting attribute policies with the importexport tool
    And the exported attribute policy data is parsed
    Then the exported attribute policy records should include
      | guid              | policyKey    | policyPermission |
      | hidden-none       | HIDDEN       | NONE             |
      | visible-emit      | READ_ONLY    | EMIT             |
      | default-emit      | DEFAULT      | EMIT             |
      | default-edit      | DEFAULT      | EDIT             |
    And the exported manifest file should have an entry for attribute policies