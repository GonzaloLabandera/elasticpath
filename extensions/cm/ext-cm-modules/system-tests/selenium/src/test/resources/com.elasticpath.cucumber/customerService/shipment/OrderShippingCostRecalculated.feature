@regressionTest @customerService @shipment
Feature: Shipping Cost Recalculated

  Background:
    Given I have an order for scope mobee with following skus
      | skuCode                 | quantity |
      | handsfree_shippable_sku | 1        |
    And I sign in to CM as admin user
    And I go to Customer Service
    And I search and open order editor for the latest order
    When I update the shipping address and set "Canada Post 2 days" shipping method for the order
      | Country         | Canada           |
      | State/Province  | British Columbia |
      | Zip/Postal Code | V7C5T8           |
      | City            | Vancouver        |
      | Address Line 1  | 751 Pike Rd      |
      | Phone           | 616 2323231      |
    Then I should see the following Shipment Summary
      | shipment-number | item-sub-total | shipping-cost | shipment-discount | total-before-tax | item-taxes | shipping-taxes | shipment-total |
      | 1               | 100.00         | 5.00          | 0.00              | 105.00           | 12.00      | 0.60           | 117.60         |

  Scenario: Percentage based shipping cost recalculation when updating shipment lineitems
    When I modify order shipment line item quantity to 2
    Then I should see the following Shipment Summary
      | shipment-number | item-sub-total | shipping-cost | shipment-discount | total-before-tax | item-taxes | shipping-taxes | shipment-total |
      | 1               | 200.00         | 10.00         | 0.00              | 210.00           | 24.00      | 1.20           | 235.20         |
    When I modify order shipment line item quantity to 1
    Then I should see the following Shipment Summary
      | shipment-number | item-sub-total | shipping-cost | shipment-discount | total-before-tax | item-taxes | shipping-taxes | shipment-total |
      | 1               | 100.00         | 5.00          | 0.00              | 105.00           | 12.00      | 0.60           | 117.60         |
    When I add sku portable_tv_hdbuy_sku to the shipment with following values
      | Price List Name | Mobile Price List       |
    And I complete Payment Authorization with Original payment source payment source
    Then I should see the following Shipment Summary
      | shipment-number | item-sub-total | shipping-cost | shipment-discount | total-before-tax | item-taxes | shipping-taxes | shipment-total |
      | 1               | 250.99         | 12.55         | 0.00              | 263.54           | 30.12      | 1.51           | 295.17         |
    When I remove sku handsfree_shippable_sku from the shipment
    Then I should see the following Shipment Summary
      | shipment-number | item-sub-total | shipping-cost | shipment-discount | total-before-tax | item-taxes | shipping-taxes | shipment-total |
      | 1               | 150.99         | 7.55          | 0.00              | 158.54           | 18.12      | 0.91           | 177.57         |
    When I modify order shipment line item discount to 10
    Then I should see the following Shipment Summary
      | shipment-number | item-sub-total | shipping-cost | shipment-discount | total-before-tax | item-taxes | shipping-taxes | shipment-total |
      | 1               | 140.99         | 7.05          | 0.00              | 148.04           | 16.92      | 0.84           | 165.80         |

  Scenario: Overriding Shipment Discount
    When I set order shipment discount to 1.50
    Then I should see the following Shipment Summary
      | shipment-number | item-sub-total | shipping-cost | shipment-discount | total-before-tax | item-taxes | shipping-taxes | shipment-total |
      | 1               | 100.00         | 4.93          | 1.50              | 103.43           | 11.83      | 0.60           | 115.86         |

  Scenario: Overriding Shipping Cost
    When I set order Shipping Cost value to 10
    Then I should see the following Shipment Summary
      | shipment-number | item-sub-total | shipping-cost | shipment-discount | total-before-tax | item-taxes | shipping-taxes | shipment-total |
      | 1               | 100.00         | 10.00         | 0.00              | 110.00           | 12.00      | 1.20           | 123.20         |