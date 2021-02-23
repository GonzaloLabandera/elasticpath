@totals @headerAuth

Feature: Retrieve Line Item Total
  As a client developer,
  I want to retrieve the pricing information of a lineitem
  so that the shopper could see how much they are paying for each lineitem

  Background:
    Given I login as a registered shopper

  Scenario Outline: Retrieve Line Item Total
    Given I add item <ITEM_NAME> to the cart with quantity <QTY>
    When I retrieve cart lineitem for <ITEM_NAME>
     And I follow the total link
    Then the cost fields has amount: <AMOUNT>, currency: <CURRENCY> and display: <DISPLAY_AMOUNT>

    Examples:
      | ITEM_NAME | QTY | AMOUNT | CURRENCY | DISPLAY_AMOUNT |
      | Twilight  | 2   | 94.94  | CAD      | $94.94         |

  Scenario Outline: Retrieve Line Item Total when Item in Cart has no Price
    Given I add item <ITEM_NAME> to the cart with quantity <QTY>
    And I append to the overwritten personalization header the key CURRENCY and value EUR
    When I retrieve cart lineitem for <ITEM_NAME>
    Then the line item total link is missing

    Examples:
      | ITEM_NAME | QTY |
      | Twilight  | 1   |