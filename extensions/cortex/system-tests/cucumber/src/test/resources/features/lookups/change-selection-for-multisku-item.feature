@Lookups
Feature: Search for multi sku Portable TV item and change the item selection by option before purchase.Make a purchase with selected item and
  ensure purchased item shows selected item in purchase

  Scenario Outline: Change Multisku item selection by option and place an order.Ensure Purchase made with selected line item.

    Given I login as a public shopper
    When I look up an item with code <MULTI_SKU>
    And I change the multi sku selection by <OPTION> and select choice <VALUE>
    Then the item code is <ITEM_SKU_CODE>
    When I add selected multisku item to the cart
    And I fill in email needinfo
    And I fill in payment methods needinfo
    And I fill in billing address needinfo
    And I make a purchase
    Then I view purchase line item option <OPTION>
    And I should see item option value is <VALUE>

    Examples:
      | MULTI_SKU             | OPTION        | ITEM_SKU_CODE          | VALUE |
      | portable_tv_hdbuy_sku | Purchase Type | portable_tv_hdrent_sku | Rent  |

