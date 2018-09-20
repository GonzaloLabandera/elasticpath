@Locale
Feature: Purchase item as per provided locale and ensure purchase item shows correct amount and currency

  Scenario Outline: As a public shopper with given locale,I make a purchase.
  I ensure correct locale amount and currency are displayed for purchase items

    Given I am logged in as a public shopper
    And I submit request header with the user traits <TRAITS_VALUE>
    And I add item with code <ITEM_CODE> to my cart
    And I fill in email needinfo
    And I fill in payment methods needinfo
    And I fill in billing address needinfo
    And I make a purchase
    When I go to the purchases
    Then purchase item monetary total has fields amount: <AMOUNT>, currency: <CURRENCY> and display: <DISPLAY_AMOUNT>
    And purchase item tax total has fields amount: <TAX_AMOUNT>, currency: <CURRENCY> and display: <TAX_DISPLAY_AMOUNT>

#   To ensure the locale and currency value persist with original value when user submits request header with different locale and currency
    When I submit request header with the user traits <NEW_TRAITS_VALUE>
    Then purchase item monetary total has fields amount: <AMOUNT>, currency: <CURRENCY> and display: <DISPLAY_AMOUNT>
    And purchase item tax total has fields amount: <TAX_AMOUNT>, currency: <CURRENCY> and display: <TAX_DISPLAY_AMOUNT>

    Examples:
      | ITEM_CODE     | TRAITS_VALUE              | AMOUNT | CURRENCY | DISPLAY_AMOUNT | TAX_AMOUNT | TAX_DISPLAY_AMOUNT | NEW_TRAITS_VALUE          |
      | tt0970179_sku | LOCALE=en,CURRENCY=CAD    | 39.19  | CAD      | $39.19         | 4.2        | $4.20              | LOCALE=fr-CA,CURRENCY=EUR |
      | tt0970179_sku | LOCALE=fr-CA,CURRENCY=EUR | 2.23   | EUR      | €2.23          | 0.24       | €0.24              | LOCALE=en,CURRENCY=CAD    |