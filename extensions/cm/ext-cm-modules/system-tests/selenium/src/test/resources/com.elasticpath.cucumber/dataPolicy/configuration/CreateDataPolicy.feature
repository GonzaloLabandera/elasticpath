@regressionTest @dataPolicy
Feature: Create Data Policy

  Background:
    Given I sign in to CM as admin user

  Scenario Outline: Create new Active Data Policy
    Given I create a new Data Policy with following values
      | policy-name          | Test                |
      | policy-reference-key | Test                |
      | retention days       | 1                   |
      | state                | Active              |
      | data points          | Customer first name |
      | segment              | <POLICY_SEGMENT>    |
    And the newly created Data Policy exists in the list
    When I set X-EP-Data-Policy-Segments header <POLICY_SEGMENT>
    And I am logged into scope mobee as a public shopper
    And I select newly created data policy
    Then I can see the newly created data policy in my profile with following details
      | data-policy-consent  | false |
      | policy-reference-key | Test  |
      | policy-name          |       |

    Examples:
      | POLICY_SEGMENT |
      | test_policy    |