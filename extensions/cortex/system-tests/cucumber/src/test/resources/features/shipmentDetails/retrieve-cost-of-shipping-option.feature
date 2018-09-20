@Shipping @HeaderAuth

Feature: Retrieve shipping costs
  As a client developer,
  I want to retrieve the cost of a shipping option
  so that I can show the shopper how much shipping will cost them

  Background:
    Given I login as a registered shopper

  Scenario Outline: Verify shipping cost without shipping promotion
    Given I add item <ITEM_NAME> to the cart
    When I select a shipping option <WITHOUT_PROMOTION>
    Then the shipping cost has fields amount: <AMOUNT>, currency: <CURRENCY> and display: <DISPLAY_AMOUNT>

    Examples:
      | ITEM_NAME              | WITHOUT_PROMOTION               | AMOUNT | CURRENCY | DISPLAY_AMOUNT |
      | Acon Bluetooth headset | FixedPriceNoPromoShippingOption | 100.0  | CAD      | $100.00        |

  Scenario Outline: Verify shipping cost with shipping promotion
    Given I add item <ITEM_NAME> to the cart
    When I select a shipping option <WITH_PROMOTION>
    Then the shipping cost has fields amount: <AMOUNT>, currency: <CURRENCY> and display: <DISPLAY_AMOUNT>

    Examples:
      | ITEM_NAME              | WITH_PROMOTION                                 | AMOUNT | CURRENCY | DISPLAY_AMOUNT |
      | Acon Bluetooth headset | FixedPriceWith100PercentOffPromoShippingOption | 0.0    | CAD      | $0.00          |