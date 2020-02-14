@httpCaching @dataPolicies
Feature: HTTP Caching - Data Policies

  Background:
    Given I set X-EP-Data-Policy-Segments header EU_Data_Policy
    And I login as a new public shopper
    When I access the data policies
    And I select the data policy Marketing Contact Information
    Then I can access the data policy with the following fields:
      | data-policy-consent  | false                         |
      | policy-reference-key | DP12346                       |
      | policy-name          | Marketing Contact Information |

  Scenario: Data Policy consent form should have HTTP caching
    When I access the data policy form
    Then I should see the Cache-Control header in the response with a valid max age and private directive
    And I should see an ETag header in the response
