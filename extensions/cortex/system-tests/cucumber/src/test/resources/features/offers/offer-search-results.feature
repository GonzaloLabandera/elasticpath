@offer
Feature: Offer Search Results

  Background:
    Given I am logged in as a public shopper

  Scenario Outline: Offer Search returns a product if the keyword is specified in a product's name
    When I search for offer <PRODUCT_NAME>
    Then there is an item with display-name <PRODUCT_NAME>

    Examples:
      | PRODUCT_NAME  |
      | Sleepy Hallow |

  Scenario: Offer Search returns a bundle if the keyword is specified in a bundle component's product name
    When I search for offer htc
    Then the element list contains items with display-names
      | HTC Evo 4G         |
      | SmartPhones Bundle |

  Scenario Outline: Offer Search should not return products that have scope visible set to false
    When I search for offer <OUT_OF_SCOPE_PRODUCT>
    Then the field pagination contains value pages=1
    And the field pagination contains value results=0
    And the field pagination contains value results-on-page=0
    And there are 0 links of rel element

    Examples:
      | OUT_OF_SCOPE_PRODUCT      |
      | Motorola Wireless Headset |

  Scenario Outline: Offer Search should not return disabled products
    When I search for offer <DISABLED_PRODUCT>
    Then there are 0 links of rel element

    Examples:
      | DISABLED_PRODUCT |
      | Future Product   |

  Scenario Outline: Offer Search should not return products that are not sold separately
    When I search for offer <NOT_SOLD_SEPARATELY_PRODUCT>
    Then the element list does not contains items with display-names
      | <NOT_SOLD_SEPARATELY_PRODUCT> |

    Examples:
      | NOT_SOLD_SEPARATELY_PRODUCT |
      | Good Movies                 |

  Scenario Outline: Offer Search returns relevant result when searching for <SCENARIO>
    Given item <PRODUCT_CODE> has <FACET_NAME> with <VALUE> in language en
    When I search for the offer <VALUE> with page-size 100
    Then the list contains an element with the following code
      | <PRODUCT_CODE> |

    Examples:
      | SCENARIO          | FACET_NAME          | VALUE       | PRODUCT_CODE         |
      | Product Attribute | Product Description | Lorem ipsum | physicalProduct      |
      | Sku Options       | Billed              | per month   | phonePlan            |
      | Sku Attributes    | Screen Format       | widescreen  | tt258022dh           |
      | Others            | Brand               | WarnerBros  | GuardiansOfTheGalaxy |
