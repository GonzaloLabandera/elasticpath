@regressionTest @dataPolicy
Feature: Report for B2B Buyer using system Data Policy Segments

  Background:
    Given I sign in to CM as admin user
    And I go to Data Policies
    And I create a new Data Policy with following values
      | policy-name          | TestOrderDataPolicy  |
      | policy-reference-key | test order           |
      | retention days       | 300                  |
      | state                | Active               |
      | data points          | Customer first name, Customer last name |
      | segment              | POLICY_EMAIL_SEGMENT |
    And I go to Configuration
    And I go to System Configuration
    And I add new defined values record for system setting with following data
      | setting | COMMERCE/STORE/dataPolicySegments |
      | context | MOBEE                             |
      | value   | POLICY_EMAIL_SEGMENT              |

  Scenario Outline: Ensure data is reported for the customer which is defined in system configuration
    When I login using jwt authorization with the following details
      | roles         | BUYER   |
      | user_id       | <USER>  |
    And I create a saved Smart Path Config payment instrument from order supplying the following fields:
      | display-name | saved one |
    # Purchase item using named cart
    And I create a new shopping cart with name family
    And I add alien_sku to cart family with quantity 1
    And I complete the purchase after providing all required order info for family cart
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
      | USER         | USER_SHARED_ID                 | CUSTOMER_FIRST_NAME | CUSTOMER_LAST_NAME |
      | usertestguid | MOBEE:usertest@elasticpath.com | test                | user               |

  Scenario Outline: View Disabled Data Policy specified in System settings
    When I Disabled newly created Data Policy
    And I go to Customer Service
    And I search and open customer editor for shared ID <USER_SHARED_ID>
    And I select Customer Data Policies tab in the Customer Editor
    And I click on Show Disabled Data Policies
    Then I should see newly Disabled Data Policy

    Examples:
      | USER_SHARED_ID                  |
      | MOBEE:ben.boxer@elasticpath.com |

  Scenario Outline: Delete Customer Data Points in Data Policies that are coming from System settings
    When I login using jwt authorization with the following details
      | roles         | BUYER  |
      | user_id       | <USER> |
    And I create a saved Smart Path Config payment instrument from order supplying the following fields:
      | display-name | saved one |
    # Purchase item using named cart
    And I create a new shopping cart with name family
    And I add alien_sku to cart family with quantity 1
    And I complete the purchase after providing all required order info for family cart
    # Reindex search to have new B2B user available for the search
    And I go to Configuration
    And I go to Search Indexes
    And I rebuild CM Users index
    Then Index CM Users should have status: Complete
    # Delete data points in customer service menu
    And I go to Customer Service
    And I search and open customer editor for shared ID <USER_SHARED_ID>
    And I select Customer Data Policies tab in the Customer Editor
    And I Delete Data Points for recent Data Policy
    And Data Point Values are empty for following Removable Data Points
      | Customer first name |
      | Customer last name  |

    Examples:
      | USER         | USER_SHARED_ID                 |
      | usertestguid | MOBEE:usertest@elasticpath.com |
