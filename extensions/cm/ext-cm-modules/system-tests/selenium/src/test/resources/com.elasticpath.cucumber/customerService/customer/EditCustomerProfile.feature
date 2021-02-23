@regressionTest @customerService @customer
Feature: Add address and update name

  Scenario: Suspended status should exist in status combo box for account customer
    Given I sign in to CM as admin user
    When I search and open account editor for shared ID SomeBusiness@abc.com
    Then I can select Suspended status for the customer

  Scenario: Suspended status should not exist in status combo box for registered user
    Given I sign in to CM as admin user
    When I search and open customer editor for shared ID MOBEE:harry.potter@elasticpath.com
    Then There are no Suspended status for the customer

  Scenario: Correct set of fields and tabs are displayed for account customer
    Given I sign in to CM as admin user
    When I search and open account editor for shared ID SomeBusiness@abc.com
    Then I should see following fields in the Basic Profile section:
      | Shared ID       |
      | Status          |
      | Business Name   |
      | Business Number |
      | Phone Number    |
      | Fax Number      |
    And I should see following fields in the Registration Information section:
      | Customer Type    |
      | Parent Hierarchy |
    And I should see following tabs:
      | Customer Profile  |
      | Addresses         |
      | Child Accounts    |
      | Associates        |
      | Orders            |
      | Customer Segments |

  Scenario: Correct set of fields and tabs are displayed for registered user
    Given I sign in to CM as admin user
    When I search and open customer editor for shared ID MOBEE:harry.potter@elasticpath.com
    Then I should see following fields in the Basic Profile section:
      | Shared ID     |
      | Status        |
      | Username      |
      | First Name    |
      | Last Name     |
      | Email Address |
      | Phone Number  |
      | Fax Number    |
      | Company       |
    And I should see following fields in the Registration Information section:
      | Customer Type      |
      | Date Registered    |
      | Store Registered   |
      | Preferred Language |
      | Preferred Currency |
      | Date of Birth      |
      | Receive HTML Email |
      | Receive Newsletter |
      | Business Number    |
      | Tax Exemption Id   |
    And I should see following tabs:
      | Customer Profile       |
      | Addresses              |
      | Orders                 |
      | Customer Segments      |
      | Customer Data Policies |

  Scenario Outline: Update customer name
    Given I have an order for scope <scope> with following skus
      | skuCode      | quantity |
      | <sku-code-1> | 1        |
    And I sign in to CM as CSR user
    And I go to Customer Service
    And I search and open order editor for the latest order
    And I open Customer Profile
    When I update first name <new-first-name> and last name <new-last-name> for the customer
    Then I should see the updated first name <new-first-name> and last name <new-last-name>

    Examples:
      | scope | sku-code-1              | new-first-name | new-last-name |
      | mobee | handsfree_shippable_sku | Jeff           | Vader         |

  Scenario Outline: Add new address
    Given I have an order for scope <scope> with following skus
      | skuCode      | quantity |
      | <sku-code-1> | 1        |
    And I sign in to CM as CSR user
    And I go to Customer Service
    And I search and open order editor for the latest order
    And I open Customer Profile Addresses tab
    And I click Add Address button
    When I add new address with the following values
      | first name     | Jeff          |
      | last name      | Vader         |
      | address line 1 | 751 Pike Rd   |
      | city           | Woodstock     |
      | state          | Alabama       |
      | zip            | 35188         |
      | country        | United States |
      | phone          | 616 2323231   |
    Then the new address should exist in the address list

    Examples:
      | scope | sku-code-1              |
      | mobee | handsfree_shippable_sku |
