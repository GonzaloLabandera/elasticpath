@purchases
Feature: Purchase Nested Bundles
  Purchasing nested bundles
  # TODO in automation story PB-4286
#  Background:
#    Given I have authenticated as a newly registered shopper
#
#  Scenario Outline: Bundle with nested bundle which contains physical and digital products has a correct order total and shipments
#    When Adding an item with item code <bundleWithNestedBundle> and quantity 1 to the cart
#    And I fill in email needinfo
#    And I fill in payment methods needinfo
#    And I fill in billing address needinfo
#    And I retrieve the order
#    And I follow links total
#    Then I see the cost field has amount: <amount>, currency: <currency> and display: <display>
#    When the order is submitted
#    Then the HTTP status is OK
#    And I view the components of a purchase line item <bundleWithNestedBundle>
#    Then all <bundleWithNestedBundleConstituents> constituents of a purchase line item <bundleWithNestedBundle> are displayed as components
#    When I view the components of a purchase line item's <bundleWithNestedBundle> component <nestedBundle>
#    Then all <nestedBundleConstituents> constituents of a purchase line item <nestedBundle> are displayed as components
#    When I view the purchase
#    And I view the shipment line items
#    Then I see shipment line items: <shipmentLineItemsAmount>
#    And I see the only following product names among shipment line items <shipmentLineItems>


#    Examples:
#      | bundleWithNestedBundle                             | nestedBundle                                  | amount | currency | display | bundleWithNestedBundleConstituents | nestedBundleConstituents | shipmentLineItemsAmount | shipmentLineItems                                                                                                              |
#      | bundleDependentLineItemsScheme1AssignedPrice_sku   | bundlePhysicalDigitalItemsAssignedPrice_sku   | 173.6  | CAD      | $173.60 | 2                                  | 2                        | 2                       | Physical Item From Bundle With Physical and Digital Components, Physical Item From Bundle With Physical and Digital Components |
#      | bundleDependentLineItemsScheme1CalculatedPrice_sku | bundlePhysicalDigitalItemsCalculatedPrice_sku | 69.44  | CAD      | $69.44  | 2                                  | 2                        | 2                       | Physical Item From Bundle With Physical and Digital Components, Physical Item From Bundle With Physical and Digital Components |
#      | bundleDependentLineItemsScheme2AssignedPrice_sku   | bundlePhysicalDigitalItemsAssignedPrice_sku   | 226.81 | CAD      | $226.81 | 3                                  | 2                        | 1                       | Physical Item From Bundle With Physical and Digital Components                                                                 |
#      | bundleDependentLineItemsScheme2CalculatedPrice_sku | bundlePhysicalDigitalItemsCalculatedPrice_sku | 90.72  | CAD      | $90.72  | 3                                  | 2                        | 1                       | Physical Item From Bundle With Physical and Digital Components                                                                 |