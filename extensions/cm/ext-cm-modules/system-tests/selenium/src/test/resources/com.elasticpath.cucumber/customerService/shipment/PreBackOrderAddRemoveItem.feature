@regressionTest @customerService @shipment
Feature: Add remove pre/back order items
#tt0970179_sku - this is sku with AVAILABLE_FOR_PRE_ORDER availability criteria
#tt0926084_sku - this is sku with AVAILABLE_FOR_BACK_ORDER availability criteria

  Background:
    Given I have an order for scope mobee with following skus
      | skuCode                 | quantity |
      | handsfree_shippable_sku | 1        |
    And I sign in to CM as CSR user
    And I go to Customer Service
    And I search and open order editor for the latest order

  Scenario Outline: Add pre/back order item and check that Modify Reserve should present
    When I add sku <sku-code> to the shipment with following values
      | Price List Name | Mobile Price List |
    And I complete Payment Authorization with Original payment source payment source
    Then I should see following order payment transaction in the Payment History
      | Method | <PAYMENT_METHOD> |
      | Type   | Modify Reserve   |
      | Status | Approved         |
      | Amount | <ORDER_TOTAL>    |

    Examples:
      | PAYMENT_METHOD    | sku-code      | ORDER_TOTAL |
      | Smart Path Config | tt0970179_sku | $250.26     |
      | Smart Path Config | tt0926084_sku | $241.90     |

  Scenario Outline: Delete pre/back order items and check that Modify Reserve transactions should present
    When I add sku <sku-code-1> to the shipment with following values
      | Price List Name | Mobile Price List |
    And I complete Payment Authorization with Original payment source payment source
    And I add sku <sku-code-2> to the shipment with following values
      | Price List Name | Mobile Price List |
    And I complete Payment Authorization with Original payment source payment source
    Then I should see following order payment transaction in the Payment History
      | Method | <PAYMENT_METHOD> |
      | Type   | Modify Reserve   |
      | Status | Approved         |
      | Amount | $250.26          |
    And I should see following order payment transaction in the Payment History
      | Method | <PAYMENT_METHOD> |
      | Type   | Modify Reserve   |
      | Status | Approved         |
      | Amount | $279.16          |
    And I remove sku <sku-code-1> from the shipment
    And I remove sku <sku-code-2> from the shipment
    And I should see following order payment transaction in the Payment History
      | Method | <PAYMENT_METHOD> |
      | Type   | Modify Reserve   |
      | Status | Approved         |
      | Amount | $241.90          |
    And I should see following order payment transaction in the Payment History
      | Method | <PAYMENT_METHOD> |
      | Type   | Modify Reserve   |
      | Status | Approved         |
      | Amount | $213.00          |


    Examples:
      | PAYMENT_METHOD    | sku-code-1    | sku-code-2    |
      | Smart Path Config | tt0970179_sku | tt0926084_sku |