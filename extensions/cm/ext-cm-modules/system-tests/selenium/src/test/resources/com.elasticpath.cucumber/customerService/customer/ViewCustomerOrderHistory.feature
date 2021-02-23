@regressionTest @customerService @customer
Feature: Customer Order History

  @smokeTest
  Scenario: View Order Details in Customer order history
    Given I have an order for scope mobee with following skus
      | skuCode                 | quantity |
      | handsfree_shippable_sku | 1        |
    And I sign in to CM as admin user
    And I go to Customer Service
    When I search and open order editor for the latest order
    And I open Customer Profile Orders tab
    Then I should see the latest order details

  Scenario: View Order Details in Customer order history for an account
    Given I create an account order for scope mobee and user usertest2@elasticpath.com and account accounttest1@elasticpath.com with following skus
      | skuCode                 | quantity |
      | handsfree_shippable_sku | 1        |
    And I sign in to CM as admin user
    When I search and open account editor for shared ID accounttest1@elasticpath.com
    And I select Orders tab in the Customer Editor
    Then I should see the latest order details in customer profile order tab
    And I refresh orders in customer profile order tab
    Then I should see the latest order details in customer profile order tab