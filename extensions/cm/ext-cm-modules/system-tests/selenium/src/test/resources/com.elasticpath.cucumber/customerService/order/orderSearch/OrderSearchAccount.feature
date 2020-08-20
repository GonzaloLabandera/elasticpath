@regressionTest @customerService @order @orderSearch
Feature: Order Search with Account-Assigned Order

  Background:
    Given I sign in to CM as admin user
    And I go to Customer Service
    And I create an account order for scope mobee and user usertest@elasticpath.com and account accounttest1@elasticpath.com with following skus
      | skuCode                 | quantity |
      | alien_sku               | 1        |

  Scenario: Order search by Order Number
    When I search the latest order by number
    Then I should see the latest order in results pane
    Then I should see customer name test user in search results pane
    And I should see account name Some Business Account in search results pane

  Scenario: Order search by Business Name
    When I search for orders by Business Name Some Business Account
    Then I should see customer name test user in search results pane
    And I should see account name Some Business Account in search results pane
