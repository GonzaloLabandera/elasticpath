@customerZero
Feature: Customer zero smoke tests

  Background:
    Given I am logged into scope vestri as a public shopper

  Scenario Outline: Search by product name
    When I search for keyword "<PRODUCT_NAME>"
    Then there is an item with display-name <PRODUCT_NAME>

    Examples:
      | PRODUCT_NAME                 |
      | Stainless Steel Water Bottle |

  Scenario: Search by keyword
    When I search for the keyword "wheel" with page-size 5
    Then the field pagination contains value page-size=5
    And the field pagination contains value pages=4
    And the field pagination contains value results=19
    And the field pagination contains value results-on-page=5
    And there are 5 links of rel element

  Scenario Outline: Anonymous shopper purchase
    Given Adding an item with item code <ITEMCODE> and quantity <QUANTITY> to the cart
    And I fill in all the required purchase info with US address
    When the order is submitted
    Then the purchase status is IN_PROGRESS

    Examples:
      | ITEMCODE               | QUANTITY |
      | VESTRI_MODEL_X_HAT_GR  | 1        |
      | VESTRI_CHADEMO_ADAPTER | 2        |

  Scenario: Registered shopper purchase
    Given I have an order for scope vestri with following skus
      | skuCode                           | quantity |
      | VESTRI_MENS_ATHLETIC_TSHIRT_GR_MD | 1        |
      | VESTRI_NEMA_14-30                 | 2        |
    When I view the purchase
    Then the purchase status is IN_PROGRESS
