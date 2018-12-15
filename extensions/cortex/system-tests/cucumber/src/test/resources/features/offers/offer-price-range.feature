@offer
Feature: Offer Price Range

  Background:
    Given I am logged in as a public shopper

  Scenario Outline: Offer price range - <SCENARIO>
    Given item with product code physicalProduct has the following prices
      | LIST_PRICE_RANGE_FROM     | <LIST_PRICE_RANGE_FROM>     |
      | LIST_PRICE_RANGE_TO       | <LIST_PRICE_RANGE_TO>       |
      | PURCHASE_PRICE_RANGE_FROM | <PURCHASE_PRICE_RANGE_FROM> |
      | PURCHASE_PRICE_RANGE_TO   | <PURCHASE_PRICE_RANGE_TO>   |
    When I search and open the offer for offer name <OFFER_NAME>
    And I follow the link pricerange
    Then the offer price range has the following result
      | LIST_PRICE_RANGE_FROM     | <LIST_PRICE_RANGE_FROM>     |
      | LIST_PRICE_RANGE_TO       | <LIST_PRICE_RANGE_TO>       |
      | PURCHASE_PRICE_RANGE_FROM | <PURCHASE_PRICE_RANGE_FROM> |
      | PURCHASE_PRICE_RANGE_TO   | <PURCHASE_PRICE_RANGE_TO>   |

    Examples:
      | SCENARIO                                       | OFFER_NAME         | LIST_PRICE_RANGE_FROM | LIST_PRICE_RANGE_TO | PURCHASE_PRICE_RANGE_FROM | PURCHASE_PRICE_RANGE_TO |
      | For single price                               | physicalProduct    | $25.00                | $25.00              | $25.00                    | $25.00                  |
      | Same List Price Range and Purchase Price Range | Alien              | $25.99                | $25.99              | $20.00                    | $20.00                  |
      | List Price and Purchase Price Range            | Gift Certificate   | $20.00                | $100.00             | $20.00                    | $100.00                 |
      | Different List Price and Purchase Price Range  | Back To The Future | $10.99                | $15.99              | $6.99                     | $10.99                  |

  Scenario: Offer price range does not appear if product has no price
    Given item sku noPrice_sku does not have a price
    When I search and open the offer for offer name productWithoutPrice
    Then there are no pricerange links

  Scenario Outline: Product with no sku price falls back to product prices
    Given item name house with sku code tbcp_0123456 has no sku price
    And item with name house has the following prices
      | LIST_PRICE_RANGE_FROM     | <LIST_PRICE_RANGE_FROM>     |
      | LIST_PRICE_RANGE_TO       | <LIST_PRICE_RANGE_TO>       |
      | PURCHASE_PRICE_RANGE_FROM | <PURCHASE_PRICE_RANGE_FROM> |
      | PURCHASE_PRICE_RANGE_TO   | <PURCHASE_PRICE_RANGE_TO>   |
    When I search and open the offer for offer name House
    And I follow the link pricerange
    Then the offer price range has the following result
      | LIST_PRICE_RANGE_FROM     | <LIST_PRICE_RANGE_FROM>     |
      | LIST_PRICE_RANGE_TO       | <LIST_PRICE_RANGE_TO>       |
      | PURCHASE_PRICE_RANGE_FROM | <PURCHASE_PRICE_RANGE_FROM> |
      | PURCHASE_PRICE_RANGE_TO   | <PURCHASE_PRICE_RANGE_TO>   |
    Examples:
      | LIST_PRICE_RANGE_FROM | LIST_PRICE_RANGE_TO | PURCHASE_PRICE_RANGE_FROM | PURCHASE_PRICE_RANGE_TO |
      | $50.50                | $50.50              | $30.00                    | $30.00                  |