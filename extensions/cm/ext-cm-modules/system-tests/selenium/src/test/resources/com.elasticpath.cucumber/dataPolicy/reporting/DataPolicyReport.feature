@regressionTest @dataPolicy
Feature: Data Policy Reporting for customer consent data policies

  Background:
    Given I sign in to CM as admin user
    And I go to Configuration
    And I go to System Configuration
    And I ensure table values for the following system setting COMMERCE/STORE/dataPolicySegments do not exist

  Scenario Outline: Report for Customer with Data Points
    When I select following report options
      | reportType    | store   |
      | <report-type> | <store> |
    And I run Customer Personal Data report for customer <shared-id>
    Then the Shared ID <shared-id> is in the Customer Personal Data report
    And the Report Content shows following Data Points
      | Data Point Value                    |
      | policy_user2                        |
      | automation_data_policy_user2@ep.com |
      | automation                          |
      | 4567 Broadway Street                |

    Examples:
      | report-type            | store | shared-id                             |
      | Customer Personal Data | Mobee | MOBEE:automation_data_policy_user2@ep.com |

  Scenario Outline: Report for Customer with No Data Points
    When I select following report options
      | reportType    | store   |
      | <report-type> | <store> |
    And I run Customer Personal Data report for customer <shared-id>
    Then the Shared ID <shared-id> is in the Customer Personal Data report
    And the Report is empty

    Examples:
      | report-type            | store | shared-id                      |
      | Customer Personal Data | Mobee | harry.potter@elasticpath.com |

  Scenario Outline: Customer ID validation for Customer Personal Data report
    When I select following report options
      | reportType    | store   |
      | <report-type> | <store> |
    And I enter empty value in Shared ID
    Then I can see validation error messages for Shared ID

    Examples:
      | report-type            | store |
      | Customer Personal Data | Mobee |
