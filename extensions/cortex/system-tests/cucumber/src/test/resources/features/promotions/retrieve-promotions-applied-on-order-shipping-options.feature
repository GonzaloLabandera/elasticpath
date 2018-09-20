@Promotions
Feature: Shipping option promotions
  As a client developer
  I want to retrieve the shipping promotion applied on an order
  so that I can display shipping promotions to

  Background:
    Given I login as a public shopper

  Scenario Outline: Retrieve promotion applied to a selected shipping option
    And I fill in address for Canadian Shipping
    And I add item <ITEM_NAME> to the cart
    And I select shipping option <SHIPPING_OPTION>
    When I view a selected shipping option with promotions
    Then there is a list of applied promotions on the shipping option
    # the expected promotion
    And the list of applied promotions contains promotion <PROMOTION>

    Examples:
      | ITEM_NAME                       | SHIPPING_OPTION    | PROMOTION    |
      | triggerprodforfreeshippingpromo | Canada Post 2 days | freeshipping |

  Scenario Outline: Retrieve promotion applied to an unselected shipping option
    And I fill in address for Canadian Shipping
    And I add item <ITEM_NAME> to the cart
    And I select shipping option <SHIPPING_OPTION>
    When I view an unselected shipping option promotions <WITH_PROMOTION>
    Then there is a list of applied promotions on the shipping option
    # the unselected shipping option promotion
    And the list of applied promotions contains promotion <PROMOTION>

    Examples:
      | ITEM_NAME                       | SHIPPING_OPTION    | WITH_PROMOTION                                 | PROMOTION                                                       |
      | triggerprodforfreeshippingpromo | Canada Post 2 days | FixedPriceWith100PercentOffPromoShippingOption | Free shipping on FixedPriceWith100PercentOffPromoShippingOption |

  Scenario Outline: Retrieve shipping option with no promotions
    And I fill in address for Canadian Shipping
    And I add item <ITEM_NAME> to the cart
    And I select shipping option <SHIPPING_OPTION>
    When I view an unselected shipping option promotions <WITHOUT_PROMOTION>
    Then there is a list of applied promotions on the shipping option
    And the list of applied promotions is empty

    Examples:
      | ITEM_NAME                       | SHIPPING_OPTION    | WITHOUT_PROMOTION |
      | triggerprodforfreeshippingpromo | Canada Post 2 days | CanadaPostExpress |

  Scenario Outline: Retrieve promotion applied to a shipping option with personalization parameters
    And I fill in address for Canadian Shipping
    And I add item <ITEM_NAME> to the cart
    When I view an unselected shipping option <SHIPPING_OPTION> with personalised shipping promotions
    Then there is a list of applied promotions on the shipping option
    And the list of applied promotions contains promotion <PROMOTION>

    Examples:
      | ITEM_NAME                       | SHIPPING_OPTION     | PROMOTION                         |
      | triggerprodforfreeshippingpromo | Canada Post Express | PersonalisedShippingDiscountPromo |

  # TODO: We could show a variation here where there is no selection made at all on the selector
  # but I am tired of fighting selectors for now.

