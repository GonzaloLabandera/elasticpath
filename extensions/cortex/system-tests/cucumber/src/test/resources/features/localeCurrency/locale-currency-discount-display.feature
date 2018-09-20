@Locale
Feature: Apply coupon to cart to view discount details as per provided locale

  Scenario Outline: As a public shopper, I want to ensure correct discount amount and currency displayed with given locale
  and currency header when a 30% off cart total coupon code blackfriday is applied

    Given I am logged in as a public shopper
    And I add item with code tt0970179_sku to my cart
    When I apply a coupon code blackfriday to my order
    And I submit request header with the user traits <TRAITS_VALUE>
    Then the cart discount fields has amount: <DISCOUNT_VALUE>, currency: <CURRENCY_VALUE> and display: <DISCOUNT_DISPLAY>

    Examples:
      | TRAITS_VALUE              | DISCOUNT_VALUE | CURRENCY_VALUE | DISCOUNT_DISPLAY |
      | LOCALE=en,CURRENCY=CAD    | 10.5           | CAD            | $10.50           |
      | LOCALE=en,CURRENCY=EUR    | 0.6            | EUR            | €0.60            |
      | LOCALE=fr,CURRENCY=CAD    | 10.5           | CAD            | $10.50           |
      | LOCALE=fr,CURRENCY=EUR    | 0.6            | EUR            | €0.60            |
      | LOCALE=fr-CA,CURRENCY=EUR | 0.6            | EUR            | €0.60            |
      | LOCALE=fr-CA,CURRENCY=CAD | 10.5           | CAD            | 10,50$           |

