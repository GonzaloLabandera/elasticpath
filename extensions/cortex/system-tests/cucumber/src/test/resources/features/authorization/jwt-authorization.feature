@jwtAuthorization
Feature: JWT Authorization login tests

  Scenario Outline: Verify that the JWT authorization with all values allows for basic item lookup and creates customer with specified name
    Given I login using jwt authorization with the following details
      | scope                 | MOBEE                                |
      | roles                 | buyer, catalog_browser               |
      | expiration_in_seconds | 600                                  |
      | shopper_guid          | 68eb4e51-cf65-4a0d-af87-40f31fa783d2 |
      | customer_guid         | 15e0d948-6d0f-4a59-a1f2-f6778bfee122 |
      | first_name            | <GIVEN-NAME>                         |
      | last_name             | <FAMILY-NAME>                        |
    When I look up an item with code <ITEM_CODE>
    Then I should see item name is <ITEM_NAME>
    When I view my profile
    Then the field family-name contains value <FAMILY-NAME>
    And the field given-name contains value <GIVEN-NAME>
    Examples:
      | ITEM_CODE                     | ITEM_NAME                 | FAMILY-NAME | GIVEN-NAME |
      | product_with_no_discounts_sku | Product With No Discounts | Smith       | John       |

  Scenario Outline: Verify that the JWT authorization with only roles entered allows for basic item lookup
    Given I login using jwt authorization with the following details
      | roles | catalog_browser |
    When I look up an item with code <ITEM_CODE>
    Then I should see item name is <ITEM_NAME>
    When I view my profile
    Then the HTTP status is OK
    Examples:
      | ITEM_CODE                     | ITEM_NAME                 |
      | product_with_no_discounts_sku | Product With No Discounts |
