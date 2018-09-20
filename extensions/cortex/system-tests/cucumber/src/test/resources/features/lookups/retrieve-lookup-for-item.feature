@Lookups @Items
Feature: Perform Lookup for an Item to verify the shown item name is correct

  Background:
    Given I am logged in as a public shopper

  Scenario Outline: Lookup for an item code to view item name is correct.
    When I look up an item with code <ITEMCODE>
    Then I should see item name is <ITEMNAME>

    Examples:
      | ITEMCODE                      | ITEMNAME                  |
      | product_with_no_discounts_sku | Product With No Discounts |

  Scenario Outline: Lookup for an item code to view item details display name and value are correct.
    When I look up an item with code <ITEMCODE>
    Then I should see item name is <ITEMNAME>
    And I should see item details shows: display name is <DISPLAY_NAME> and display value is <DISPLAY_VALUE>

    Examples:
      | ITEMCODE   | ITEMNAME         | DISPLAY_NAME        | DISPLAY_VALUE         |
      | berries_20 | Gift Certificate | Product Description | Gift certificate text |