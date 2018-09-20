@Locale
Feature: Display item name, currency, list and purchase price as per provided locale

  Scenario Outline: As a public shopper,I Perform item search with given locale to ensure correct item name and price are displayed
    Given I am logged in as a public shopper
    When I search for item name <ITEM_NAME>
    And I submit request header with the user traits <TRAITS_VALUE>
    Then I should see item name is <DISPLAY_ITEMNAME>
    When I follow a link back to the item
    And I go to item price
    And the line-item has list amount: <LIST_AMOUNT>, currency: <CURRENCY> and display: <LIST_DISPLAY>
    And the line-item has purchase amount: <PURCHASE_AMOUNT>, currency: <CURRENCY> and display: <PURCHASE_DISPLAY>

    Examples:
      | ITEM_NAME | TRAITS_VALUE              | DISPLAY_ITEMNAME      | CURRENCY | PURCHASE_DISPLAY | PURCHASE_AMOUNT | LIST_DISPLAY | LIST_AMOUNT |
      | Hugo      | LOCALE=en,CURRENCY=CAD    | Hugo                  | CAD      | $34.99           | 34.99           | $34.99       | 34.99       |
      | Hugo      | LOCALE=en,CURRENCY=EUR    | Hugo                  | EUR      | €1.99            | 1.99            | €43.99       | 43.99       |
      | Hugo      | LOCALE=fr,CURRENCY=CAD    | Hugo French only [fr] | CAD      | $34.99           | 34.99           | $34.99       | 34.99       |
      | Hugo      | LOCALE=fr,CURRENCY=EUR    | Hugo French only [fr] | EUR      | €1.99            | 1.99            | €43.99       | 43.99       |
      | Hugo      | LOCALE=fr-CA,CURRENCY=EUR | les aventures de Hugo | EUR      | €1.99            | 1.99            | €43.99       | 43.99       |
      | Hugo      | LOCALE=fr-CA,CURRENCY=CAD | les aventures de Hugo | CAD      | 34,99$           | 34.99           | 34,99$       | 34.99       |


  Scenario Outline: As a Registered shopper, I Perform item search with given locale to ensure correct item name and price are displayed
    Given I login as a registered shopper
    When I search for item name <ITEM_NAME>
    And I submit request header with the user traits <TRAITS_VALUE>
    Then I should see item name is <DISPLAY_ITEMNAME>
    When I follow a link back to the item
    And I go to item price
    And the line-item has list amount: <LIST_AMOUNT>, currency: <CURRENCY> and display: <LIST_DISPLAY>
    And the line-item has purchase amount: <PURCHASE_AMOUNT>, currency: <CURRENCY> and display: <PURCHASE_DISPLAY>

    Examples:
      | ITEM_NAME | TRAITS_VALUE              | DISPLAY_ITEMNAME      | CURRENCY | PURCHASE_DISPLAY | PURCHASE_AMOUNT | LIST_DISPLAY | LIST_AMOUNT |
      | Hugo      | LOCALE=en,CURRENCY=CAD    | Hugo                  | CAD      | $34.99           | 34.99           | $34.99       | 34.99       |
      | Hugo      | LOCALE=en,CURRENCY=EUR    | Hugo                  | EUR      | €1.99            | 1.99            | €43.99       | 43.99       |
      | Hugo      | LOCALE=fr,CURRENCY=CAD    | Hugo French only [fr] | CAD      | $34.99           | 34.99           | $34.99       | 34.99       |
      | Hugo      | LOCALE=fr,CURRENCY=EUR    | Hugo French only [fr] | EUR      | €1.99            | 1.99            | €43.99       | 43.99       |
      | Hugo      | LOCALE=fr-CA,CURRENCY=EUR | les aventures de Hugo | EUR      | €1.99            | 1.99            | €43.99       | 43.99       |
      | Hugo      | LOCALE=fr-CA,CURRENCY=CAD | les aventures de Hugo | CAD      | 34,99$           | 34.99           | 34,99$       | 34.99       |
