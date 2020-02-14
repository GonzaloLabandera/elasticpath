@regressionTest @dataPolicy
Feature: Report for B2b Buyer's personal data once consent has been given.

  Background:
    Given I sign in to CM as admin user
    And I go to Data Policies
    And I ensure Data points with following values exists
      | data point name | Email in B2B Order Data |
      | location key    | ORDER_DATA          |
      | data key        | user-email          |
      | description     | email of user       |

  Scenario Outline: Ensure data is reported for the B2B Buyer once consent has been given
    And I create a new Data Policy with following values
      | policy-name          | TestOrderDataPolicy  |
      | policy-reference-key | test order           |
      | retention days       | 300                  |
      | state                | Active               |
      | data points          | Email in B2B Order Data |
      | segment              | POLICY_EMAIL_SEGMENT |
    And I login using jwt authorization with the following details
      | roles         | BUYER   |
      | user-email    | <EMAIL> |
      | customer_guid | <USER>  |
    And I add X-EP-Data-Policy-Segments header POLICY_EMAIL_SEGMENT
    # Give consent
    And I access the data policies
    And I select newly created data policy
    And I access the data policy form
    And I post the following fields to the data policy form:
      | data-policy-consent | true |
    # Purchase item using named cart
    And I create a new shopping cart with name family
    And I add alien_sku to cart family with quantity 1
    And I complete the purchase after providing all required order info for family cart
    # Check customer service details
    And I go to Customer Service
    And I search and open order editor for the latest order
    And I select Details tab in the Order Editor
    Then I should see the following in the order data table
      | user-email | <EMAIL> |
    #Generate report and check it
    When I select following report options
      | reportType             | store |
      | Customer Personal Data | Mobee |
    And I run Customer Personal Data report for customer <USER>
    Then the User ID <USER> is in the Customer Personal Data report
    And the Report Content shows following Data Points
      | Email in B2B Order Data |
      | <EMAIL>             |
    Examples:
      | USER           | EMAIL                          |
      | policyTestUser | policyTestUser@elasticpath.com |