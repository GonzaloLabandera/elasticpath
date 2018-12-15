@offer
Feature: Offers

  Background:
    Given I am logged in as a public shopper

  Scenario: Offer resource links to required resources
    When I search and open the offer for offer name Extreme Movie Bundle
    Then there is a availability link
    And there is a definition link
    And there is a code link
    And there is a components link
    And there is a items link
    And there is a pricerange link

  Scenario Outline: Offer availability
    Given the catalog has item <OFFER_NAME> with condition <CONDITION>
    And I am logged in as a public shopper
    When I search and open the offer for offer name <OFFER_NAME>
    And I follow the link availability
    Then the field state matches <AVAILABILITY>

    Examples:
      | CONDITION                                       | OFFER_NAME                                 | AVAILABILITY  |
      | Item Available                                  | Sleepy Hallow                              | AVAILABLE     |
      | Item Not Available                              | physicalProductWithoutInventory            | NOT_AVAILABLE |
      | Item with both available and not available skus | Multi sku product with an out of stock sku | AVAILABLE     |

  Scenario: Offer definition
    When I search and open the offer for offer name The Portuguese Bun
    And I follow the link definition
    Then the field display-name contains value The Portuguese Bun

  Scenario: Offer shows bundle component display name
    When I search and open the offer for offer name Extreme Movie Bundle
    And I follow the link components
    And I go to offer component with name Super Bundle
    And I follow the link definition
    Then the field display-name contains value Super Bundle

  Scenario: Offer shows list of bundle components with mix of product code and sku code
    Given item name House has the following components
      | tt5656565         |
      | tt966001av_sd_buy |
    When I search and open the offer for offer name House
    And I follow the link components
    Then the list contains an element with the following code
      | tt5656565         |
      | tt966001av_sd_buy |

  Scenario: Offer does not return component for non bundle offer
    When I search and open the offer for offer name Sony Bluetooth Headset
    Then there are no components links

  Scenario: Offer item definition
    When I search and open the offer for offer name Best Series 2011
    And I follow the link items
    And I go to offer item with name Best Series 2011
    And I follow the link definition
    Then the field display-name contains value Best Series 2011

  Scenario: Offer code returns product code when offer search for a sku code
    Given item name Twilight with sku code tt888456tw exists in catalog mobee
    When I search and open the offer for sku code tt888456tw
    And I follow the link code
    Then the field code contains value tt888456tw

  Scenario: Offer returns list of sku codes for multi sku product
    Given item name Back To The Future has the following skus
      | tt777789bttf_hd_buy  |
      | tt777789bttf_hd_rent |
    When I search and open the offer for offer name Back To The Future
    And I follow the link items
    Then the list contains an element with the following code
      | tt777789bttf_hd_buy  |
      | tt777789bttf_hd_rent |

  Scenario: Offer definition displays expected item attributes
    When I search and open the offer for offer name Hugo
    And I follow the link definition
    Then the product attributes contain
      | name   | display name    | display value                                                |
      | A00001 | Plot Keyword    | Robot, Orphan, 1030s, Train Station, Mystery, إنسان آلي      |
      | A00002 | Release Date    | 2011                                                         |
      | A00003 | Storyline       | The "Invention of Hugo Cabret" concerns a 12-year-old orphan |
      | A00004 | Languages       | Anglais, Français, Italien, Allemand                         |
      | A00005 | Opening DayTime | 2011                                                         |
      | A00006 | Color           | True                                                         |
      | A00007 | Runtime         | 127                                                          |
      | A00009 | Viewer's Rating | 8.12                                                         |
      | A00013 | Format          | 35 mm, 1.85 : 1                                              |