@datapolicies
Feature: Access the data policies endpoint

  Background:
    Given I set X-EP-Data-Policy-Segments header EU_Data_Policy, Global_Data_Policy
    And I login as a new public shopper

  Scenario: Data policies displayed when segment header included
    When I access the data policies resource from root
    Then I should see a list of data policies with at least 2 data policies

  Scenario: No data policies provided without segment header
    Given I remove the X-EP-Data-Policy-Segments header
    When I access the data policies resource from root
    Then I should not see any data policies

  Scenario: No data policies provided when scope has not enabled data policies
    When I access the data policies resource from a scope with no data polices enabled
    Then I should not see any data policies

  Scenario: Access the data policies endpoint from profile as an anonymous shopper
    When I view my profile
    Then I can follow a link to data policies
    And I should see a list of data policies with at least 2 data policies

  Scenario: Access the data policies endpoint from profile as an authenticated shopper
    Given I have authenticated as a newly registered shopper
    When I view my profile
    Then I can follow a link to data policies
    And I should see a list of data policies with at least 2 data policies

  Scenario: Access individual data policy information from the data policies endpoint
    When I access the data policies resource from root
    And I select the data policy named Marketing Contact Information
    Then I can see a data policy with the following field:
      | data-policy-consent  | false                         |
      | policy-reference-key | DP12346                       |
      | policy-name          | Marketing Contact Information |

  Scenario: Accessing the data point endpoint without segments header returns not found
    Given I access the data policies resource from root
    And I select the data policy named Marketing Contact Information
    And I can access a data policy with the following field:
      | data-policy-consent  | false                         |
      | policy-reference-key | DP12346                       |
      | policy-name          | Marketing Contact Information |
    When I attempt to access the data policy after removing the X-EP-Data-Policy-Segments header
    Then the HTTP status code is 404