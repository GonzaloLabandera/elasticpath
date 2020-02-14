@regressionTest @customerService @shipment
Feature: Order Shipment

  Background:
    Given I sign in to CM as admin user
    And I go to Customer Service

  Scenario: Order Shipment Discount Value
    Given there is a 30 percent off cart subtotal coupon blackfriday and sku digital_sku has purchase price of price $20
    And I create an order for scope mobee with coupon blackfriday for following sku
      | skuCode     | quantity |
      | digital_sku | 1        |
    When I search and open order editor for the latest order
    Then I should see the following Shipment Summary
      | shipment-number | item-sub-total | shipment-discount | item-taxes | shipment-total |
      | E-shipment      | 20.00          | 6.00              | 0.91       | 14.91          |

  Scenario: Order Shipping Cost reduced when Shipping Promotion applied
    Given Shipping Method Canada Post 2 days cost is 5 percent of order item subtotal
    And Product 20off_shipping_sku cost is 10.00
    And Promotion 20% Off Shipping applied to the purchase
    And I authenticate as a registered user harry.potter@elasticpath.com for scope mobee to create an order with following sku
      | skuCode            | quantity |
      | 20off_shipping_sku | 1        |
    When I search and open order editor for the latest order
    Then I should see the following Shipment Summary
      | shipment-number | item-sub-total | shipping-cost | shipment-discount | total-before-tax | item-taxes | shipping-taxes | shipment-total |
      | 1               | 10.00          | 0.40          | 0.00              | 10.40            | 1.20       | 0.05           | 11.65          |
    And I should see the applied promotion of 20% Off Shipping in the order details

  Scenario: Shipping Tax applies to shipping cost
    Given Shipping Method as Canada Post 2 days with GST 0.05 and PST 0.07 for Shipping Tax
    And I authenticate as a registered user harry.potter@elasticpath.com for scope mobee to create an order with following sku
      | skuCode      | quantity |
      | physical_sku | 1        |
    When I search and open order editor for the latest order
    Then the Shipping Method should be Canada Post 2 days
    And I should see the following Shipment Summary
      | shipment-number | item-sub-total | shipping-cost | shipment-discount | total-before-tax | item-taxes | shipping-taxes | shipment-total |
      | 1               | 25.00          | 1.25          | 0.00              | 26.25            | 0.00       | 0.15           | 26.40          |

  Scenario Outline: Change in Shipping Method recalculates Shipping Cost
    Given I authenticate as a registered user harry.potter@elasticpath.com for scope mobee to create an order with following sku
      | skuCode      | quantity |
      | physical_sku | 1        |
    When I search and open order editor for the latest order
    And I change the Shipping Method to the following
      | Shipping Method | <SHIPPING_METHOD>       |
    And I complete Payment Authorization with Original payment source payment source
    Then the Shipping Method should be <SHIPPING_METHOD>
    And I should see the following Shipment Summary
      | shipment-number | item-sub-total | shipping-cost | shipment-discount | total-before-tax | item-taxes | shipping-taxes | shipment-total |
      | 1               | 25.00          | 2.50          | 0.00              | 27.50            | 0.00       | 0.31           | 27.81          |

    Examples:
      | SHIPPING_METHOD     |
      | Canada Post Express |

  Scenario Outline: Change in Shipping Address recalculates Shipping Cost
    Given I authenticate as a registered user harry.potter@elasticpath.com for scope mobee to create an order with following sku
      | skuCode      | quantity |
      | physical_sku | 1        |
    When I search and open order editor for the latest order
    And I change the Shipping Information to the following
      | Shipping Method | <SHIPPING_METHOD>                                                     |
      | Address         | James Potter, 4567 BumbleBee Dr, Unit 80, Corte Madera, CA, 94727, US |
    And I complete Payment Authorization with Original payment source payment source
    Then the Shipping Method should be <SHIPPING_METHOD>
    And I should see the following Shipment Summary
      | shipment-number | item-sub-total | shipping-cost | shipment-discount | total-before-tax | item-taxes | shipping-taxes | shipment-total |
      | 1               | 25.00          | 100.00        | 0.00              | 125.00           | 0.00       | 6.00           | 131.00         |

    Examples:
      | SHIPPING_METHOD |
      | FedEx Express   |

  Scenario Outline: Shipment Items price calculated for order with sku quantity greater than 1
    Given there is a 55 percent off line item total coupon 55PercentOff4PhysicalSku and the following products
      | sku-code   | purchase-price |
      | <sku-code> | 25.00          |
    And I create an order for scope mobee with coupon 55PercentOff4PhysicalSku for following sku
      | skuCode    | quantity |
      | <sku-code> | 4        |
    When I search and open order editor for the latest order
    Then I should see the applied promotion of 55_percent_off_4_items in the order details
    And I should see the following line items in the Shipment table
      | sku-code   | sale-price | quantity | discount | total-price |
      | <sku-code> | 25.00      | 4        | 55.00    | 45.00       |
    Examples:
      | sku-code     |
      | physical_sku |

  Scenario: Verify Order Shipment lineitem section fields - physical item
    When I authenticate as a registered user harry.potter@elasticpath.com for scope mobee to create an order with following sku
      | skuCode                 | quantity |
      | handsfree_shippable_sku | 1        |
    And I search and open order editor for the latest order
    Then I should see the following shipment with lineitem details
      | shipmentType | shipmentNumber | bundleName | inventory | skuCode                 | productName     | skuOptions | listPrice | salePrice | quantity | discount | totalPrice | paymentSchedule |
      | physical     | 1              |            | Allocated | handsfree_shippable_sku | Samsung Headset |            | 120.00    | 100.00    | 1        | 0.00     | 100.00     |                 |

  Scenario: Verify Order Shipment lineitem section fields - digital item
    When I authenticate as a registered user harry.potter@elasticpath.com for scope mobee to create an order with following sku
      | skuCode   | quantity |
      | tt0984938 | 1        |
    And I search and open order editor for the latest order
    Then I should see the following shipment with lineitem details
      | shipmentType | shipmentNumber | bundleName | inventory | skuCode   | productName                                 | skuOptions | listPrice | salePrice | quantity | discount | totalPrice | paymentSchedule |
      | e-shipment   |                |            |           | tt0984938 | Harry Potter and the Deathly Hallows Part 2 |            | 21.99     | 20.00     | 1        | 0.00     | 20.00      |                 |

  Scenario: Verify Order Shipment lineitem section fields - recurring item
    When I authenticate as a registered user harry.potter@elasticpath.com for scope mobee to create an order with following sku
      | skuCode            | quantity |
      | phone_plan_mon_sku | 1        |
    And I search and open order editor for the latest order
    Then I should see the following shipment with lineitem details
      | shipmentType | shipmentNumber | bundleName | inventory | skuCode            | productName | skuOptions | listPrice | salePrice | quantity | discount | totalPrice | paymentSchedule |
      | recurring    |                |            |           | phone_plan_mon_sku | Phone Plan  | per month  | 30.00     | 30.00     | 1        | 0.00     | 30.00      | per month       |

  Scenario: Verify Order Shipment lineitem section fields - bundle items
    When I authenticate as a registered user harry.potter@elasticpath.com for scope mobee to create an order with following sku
      | skuCode                                    | quantity |
      | bundleWithPhysicalAndDigitalComponents_sku | 1        |
    And I search and open order editor for the latest order
    Then I should see the following shipment with lineitem details
      | shipmentType | shipmentNumber | bundleName                                  | inventory | skuCode                                                    | productName                                                    | skuOptions | listPrice | salePrice | quantity | discount | totalPrice | paymentSchedule |
      | physical     | 1              | Bundle with Physical and Digital Components | Allocated | physicalItemFromBundleWithPhysicalAndDigitalComponents_sku | Physical Item From Bundle With Physical and Digital Components |            | 20.00     | 20.00     | 1        | 0.00     | 20.00      |                 |
      | e-shipment   |                | Bundle with Physical and Digital Components |           | digitalItemFromBundleWithPhysicalAndDigitalComponents_sku  | Digital Item From Bundle With Physical and Digital Components  |            | 20.00     | 20.00     | 1        | 0.00     | 20.00      |                 |

