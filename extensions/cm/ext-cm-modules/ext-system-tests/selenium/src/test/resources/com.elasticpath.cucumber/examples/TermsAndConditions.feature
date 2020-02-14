@termsAndConditions
Feature: Terms and conditions on order

  Scenario: Order editor should display terms and conditions property value
    Given I have an order with terms and conditions for scope mobee with following skus
      | skuCode                | quantity |
      | portable_tv_hdrent_sku | 1        |
    And I sign in to CM as admin user
    And I go to Customer Service
    When I search and open order editor for the latest order
    Then the Terms and Conditions value should be TRUE