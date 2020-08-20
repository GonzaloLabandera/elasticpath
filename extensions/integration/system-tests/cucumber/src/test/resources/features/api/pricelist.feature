Feature: Import/Export API: Price List

  Scenario: Export price lists
    Given I export PriceListDescriptor records from the API
    Then response has http status 200
    And response has at least 10 price_list elements

  Scenario: Export price lists with PriceListName filter
    Given I export PriceListDescriptor records with query "FIND PriceList WHERE PriceListName='Mobee Scope Price List'" from the API
    Then response has http status 200
    And response has exactly 1 price_list elements

  Scenario: Export base amounts
    Given I export BaseAmount records with parent PriceListDescriptor and query "FIND PriceList WHERE PriceListName='Mobee Scope Price List'" from the API
    Then response has http status 200
    And response has at least 1 base_amount elements
