@regressionTest @dataPolicy
Feature: Report for B2b Buyer's personal data once consent has been given.

  Scenario Outline: Ensure data is reported for the B2B Buyer once consent has been given
    Given I sign in to CM as admin user
    And I go to Data Policies
    And I create a new Data Policy with following values
      | policy-name          | TestOrderDataPolicyTest                 |
      | policy-reference-key | test order key                          |
      | retention days       | 300                                     |
      | state                | Active                                  |
      | data points          | Customer first name, Customer last name |
      | segment              | POLICY_EMAIL_SEGMENT_TEST               |
    And I login using jwt authorization with the following details
      | subject      | <subject> |
	  | issuer       | <issuer>  |

	 And I create a saved Smart Path Config payment instrument from order supplying the following fields:
      | display-name | saved one |
    And I add X-EP-Data-Policy-Segments header POLICY_EMAIL_SEGMENT_TEST
    # Give consent
    And I access the data policies
    And I select newly created data policy
    And I access the data policy form
    And I post the following fields to the data policy form:
      | data-policy-consent | true |
    # Purchase item using named cart
    And I create a new shopping cart with name <family>
    And I add alien_sku to cart <family> with quantity 1
    And I complete the purchase after providing all required order info for <family> cart
    #Generate report and check it
    When I select following report options
      | reportType             | store |
      | Customer Personal Data | Mobee |
    And I run Customer Personal Data report for customer <USER_SHARED_ID>
    Then the Shared ID <USER_SHARED_ID> is in the Customer Personal Data report
    And the Report Content shows following Data Points
      | Customer first name   | Customer last name   |
      | <CUSTOMER_FIRST_NAME> | <CUSTOMER_LAST_NAME> |
    Examples:
      | subject                        | USER_SHARED_ID                 | CUSTOMER_FIRST_NAME | CUSTOMER_LAST_NAME | issuer             | family     |
      | MOBEE:usertest@elasticpath.com | MOBEE:usertest@elasticpath.com | test                | user               | punchout_shared_id | testFamily |
