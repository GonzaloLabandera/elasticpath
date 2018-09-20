@Lookups
Feature: Retrieve Lookup a batch of Items

  Scenario Outline: Can find a batch of Items from a list of codes.
    Given I am logged in as a public shopper
    And I retrieve the batch items lookup form
    When I submit a batch of sku codes <SKU_CODES>
    Then a batch of <NUMBER_OF_ITEMS> items is returned

    Examples:
      | SKU_CODES                                                                                                   | NUMBER_OF_ITEMS |
      | ["product_with_no_discounts_sku", "handsfree_shippable_sku", "phyItemNoStockSku", "portable_tv_hdrent_sku"] | 4               |

  Scenario Outline: Can find a batch of valid Items from a list of valid and invalid codes.
    Given I am logged in as a public shopper
    And I retrieve the batch items lookup form
    When I submit a batch of sku codes <SKU_CODES>
    Then a batch of <NUMBER_OF_ITEMS> items is returned

    Examples:
      | SKU_CODES                                                                    | NUMBER_OF_ITEMS |
      | ["product_with_no_discounts_sku", "handsfree_shippable_sku", "invalid_code"] | 2               |