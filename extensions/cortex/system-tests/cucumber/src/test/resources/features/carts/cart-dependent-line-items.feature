@carts
Feature: Cart with Dependent Line items

  Background:
    Given I login as a registered shopper

  Scenario Outline: Purchase product with a dependent item
    When Adding an item with item code <ITEM_CODE> and quantity <QUANTITY> to the cart
    Then the cart total-quantity is <TOTAL_QUANTITY>
    And the number of cart lineitems is <QUANTITY>
    When I complete the purchase for the order
    Then the number of purchase lineitems is <TOTAL_QUANTITY>
    And the purchase line items should contain following skus
      | <ITEM_CODE> |
      | eco_h_sku   |

    Examples:
      | ITEM_CODE   | QUANTITY | TOTAL_QUANTITY |
      | hama_bt_sku | 1        | 2              |

  Scenario Outline: Bundle with nested bundle which contains two products has a corresponding dependent items structure and a correct total
    When I add <bundleWithNestedBundle> in the Cart with Dependent Line Items
    Then all <bundleWithNestedBundleConstituents> constituents of the <bundleWithNestedBundle> will be added to cart as DependentLineItems
    And all <nestedBundleConstituents> constituents of the dependent line item <nestedBundle> in line item <bundleWithNestedBundle> will be added to cart as its DependentLineItems
    When I retrieve the cart total
    Then the cost fields has amount: <amount>, currency: <currency> and display: <display>

    Examples:
      | bundleWithNestedBundle                             | nestedBundle                                  | bundleWithNestedBundleConstituents | nestedBundleConstituents | amount | currency | display |
      | bundleDependentLineItemsScheme1AssignedPrice_sku   | bundlePhysicalDigitalItemsAssignedPrice_sku   | 2                                  | 2                        | 150    | CAD      | $150.00 |
      | bundleDependentLineItemsScheme1CalculatedPrice_sku | bundlePhysicalDigitalItemsCalculatedPrice_sku | 2                                  | 2                        | 60     | CAD      | $60.00  |
      | bundleDependentLineItemsScheme2AssignedPrice_sku   | bundlePhysicalDigitalItemsAssignedPrice_sku   | 3                                  | 2                        | 200    | CAD      | $200.00 |
      | bundleDependentLineItemsScheme2CalculatedPrice_sku | bundlePhysicalDigitalItemsCalculatedPrice_sku | 3                                  | 2                        | 80     | CAD      | $80.00  |