@prices
Feature: Prices - Retrieve Item Pricing By Scope
  As a client developer,
  I want to retrieve price data that corresponds to my scope,
  so I can display pricing information that matches the locale configured for my store

  Scenario Outline: <scope> pricing is returned in <currency> for the item
    Given I am logged into scope <scope> as a public shopper
    And I look up an item with code tt0970179_sku
    When I view the item price
    Then the list-price has fields amount: <amount>, currency: <currency> and display: <display>
    And the purchase-price has fields amount: <amount>, currency: <currency> and display: <display>
    Examples:
      | scope   | amount | currency | display |
      | Mobee   | 34.99  | CAD      | $34.99  |
      | Toastie | 19.99  | EUR      | €19.99  |