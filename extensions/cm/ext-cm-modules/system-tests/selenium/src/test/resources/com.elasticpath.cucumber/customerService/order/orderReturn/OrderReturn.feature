@regressionTest @customerService @order @orderReturn
Feature: Order Return

  Background:
    Given I sign in to CM as admin user

  Scenario Outline: Return digital item
    Given I have an order for scope mobee with following skus
      | skuCode    | quantity |
      | <sku-code> | 1        |
    When I create digital item return with quantity 1 for sku <sku-code>
      | Less shipment discount | 0.09 |
    Then I should see following order payment transaction in the Payment History
      | Type   | Credit            |
      | Status | Approved          |
      | Amount | -$<refund-amount> |
    And I should see following note in the Order Notes
      | DescriptionPart1 | Order return                                       |
      | DescriptionPart2 | is created. Return note: test return of <sku-code> |

    Examples:
      | sku-code        | refund-amount |
      | plantsVsZombies | 0.90          |

  Scenario Outline: Initiate return of physical item
    Given I have an order for scope <scope> with following skus
      | skuCode    | quantity |
      | <sku-code> | 1        |
    When I create physical item return with quantity 1 for sku <sku-code>
      | Less shipment discount | 0.00 |
    Then I should see following note in the Order Notes
      | DescriptionPart1 | Order return                                       |
      | DescriptionPart2 | is created. Return note: test return of <sku-code> |
    And I should NOT see following order payment transaction in the Payment History
      | Type   | Credit   |
      | Status | Approved |

    Examples:
      | scope | sku-code                |
      | mobee | handsfree_shippable_sku |

  Scenario Outline: Complete return of physical item
    Given I have an order for scope <scope> with following skus
      | skuCode    | quantity |
      | <sku-code> | 1        |
    When I create physical item return with quantity 1 for sku <sku-code>
      | Less shipment discount | 1.50 |
      | Less re-stocking fee   | 2.00 |
    And shipping receive return is processed for quantity 1 of sku <sku-code>
    And I complete the return refunding to original source
    Then the item received quantity shows as 1
    And I should see following order payment transaction in the Payment History
      | Type   | Credit            |
      | Status | Approved          |
      | Amount | -$<refund-amount> |
    And I should see following note in the Order Notes
      | DescriptionPart1 | Order return  |
      | DescriptionPart2 | is completed. |

    Examples:
      | scope | sku-code     | refund-amount |
      | mobee | physical_sku | 21.50         |

  Scenario Outline: Order shipment physical return with full shipping cost
    Given I have an order for scope <scope> with following skus
      | skuCode    | quantity |
      | <sku-code> | 1        |
    When I create physical item return with quantity 1 for sku <sku-code>
      | Shipping Cost | 100.00 |
    And shipping receive return is processed for quantity 1 of sku <sku-code>
    And I complete the return refunding to original source
    Then I should see following order payment transaction in the Payment History
      | Type   | Credit            |
      | Status | Approved          |
      | Amount | -$<refund-amount> |

    Examples:
      | scope | sku-code     | refund-amount |
      | mobee | physical_sku | 131.50        |

  Scenario: Shipment return cannot exceed original total
    Given I have an order for scope mobee with following skus
      | skuCode      | quantity |
      | physical_sku | 1        |
    When I create physical item return with quantity 1 for sku physical_sku
      | Shipping Cost | 101.00 |
    Then return dialog must have an error Shipping cost must not exceed original shipping cost in addition to any other returns.

  Scenario: Shipment return quantity must be positive
    Given I have an order for scope mobee with following skus
      | skuCode      | quantity |
      | physical_sku | 1        |
    When I create physical item return with quantity 0 for sku physical_sku
      | Shipping Cost | 0.00 |
    Then return dialog must have an error Return quantity must be a valid number in appropriate range.

  Scenario Outline: Return digital item with manual refund
    Given I have an order for scope mobee with following skus
      | skuCode    | quantity |
      | <sku-code> | 1        |
    When I create digital item return with quantity 1 for sku <sku-code>
      | Refund Option | Manual Refund |
    Then I should see following order payment transaction in the Payment History
      | Type   | Manual Credit     |
      | Status | Approved          |
      | Amount | -$<refund-amount> |

    Examples:
      | sku-code        | refund-amount |
      | plantsVsZombies | 0.99          |

  Scenario Outline: Complete return of physical item with manual refund
    Given I have an order for scope <scope> with following skus
      | skuCode    | quantity |
      | <sku-code> | 1        |
    When I create physical item return with quantity 1 for sku <sku-code>
      | Less shipment discount | 0.00 |
    And shipping receive return is processed for quantity 1 of sku <sku-code>
    And I complete the return refunding manually
    Then I should see following order payment transaction in the Payment History
      | Type   | Manual Credit     |
      | Status | Approved          |
      | Amount | -$<refund-amount> |

    Examples:
      | scope | sku-code     | refund-amount |
      | mobee | physical_sku | 25.00         |

  Scenario Outline: Express return of physical item
    Given I have an order for scope <scope> with following skus
      | skuCode    | quantity |
      | <sku-code> | 1        |
    When I create physical item return with quantity 1 for sku <sku-code>
      | Express Return | true |
    Then I should see following order payment transaction in the Payment History
      | Type   | Credit            |
      | Status | Approved          |
      | Amount | -$<refund-amount> |

    Examples:
      | scope | sku-code     | refund-amount |
      | mobee | physical_sku | 25.00         |

  Scenario Outline: Express return of physical item with manual refund
    Given I have an order for scope <scope> with following skus
      | skuCode    | quantity |
      | <sku-code> | 1        |
    When I create physical item return with quantity 1 for sku <sku-code>
      | Express Return | true          |
      | Refund Option  | Manual Refund |
    Then I should see following order payment transaction in the Payment History
      | Type   | Manual Credit     |
      | Status | Approved          |
      | Amount | -$<refund-amount> |

    Examples:
      | scope | sku-code     | refund-amount |
      | mobee | physical_sku | 25.00         |
