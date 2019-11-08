@regressionTest @configuration @facets
Feature: Facet Configuration

  Background:
    Given I am logged in as a public shopper
    And I sign in to CM as admin user
    And I go to Configuration
    And I go to Stores
    And I edit store MOBEE in editor
    And I select Facets tab in the Store Editor

  Scenario: Configure facet searchability
    Given a facet Storyline that is configured to be searchable
    When I toggle Searchable property of facet Storyline
    And after cache expires I search for offer maggie
    Then there are 0 links of rel element
    When I toggle Searchable property of facet Storyline
    And after cache expires I search for offer maggie
    And I follow the link element
    And I follow the link definition
    Then the product attributes contain
      | name   | display name | display value |
      | A00003 | Storyline    | maggie        |

  @cleanupEnabledFacet
  Scenario Outline: Configure new facet using default values
    When I configure facet <FACET_NAME> with facetable option <FACET_OPTION>
    Then I should see facet <FACET_NAME> with the following details
      | Facetable | <FACET_OPTION> |
    When cache expires for facet <FACET_NAME>
    And I search for offer movies
    And I zoom the facets with zoom element
    Then there are the following facets
      | <FACET_NAME> |

    Examples:
      | FACET_OPTION | FACET_NAME    |
      | Facet        | Video Quality |
      | Range Facet  | Weight        |

  @cleanupEnabledFacet
  Scenario: Configure values of new Range Facet
    Given a facet Viewer's Rating that is configured with Facetable value No Facet
    When I configure facet Viewer's Rating with the following ranges
      | Lower Bound | Upper Bound | Label        |
      | 2           | 4           | 1 star & Up  |
      | 4           | 6           | 2 stars & Up |
      | 6           | 8           | 3 stars & Up |
      | 8           | 10          | 4 stars & Up |
    Then I should see facet Viewer's Rating with the following details
      | Facetable | Range Facet |
    When cache expires for facet Viewer's Rating
    And I search for offer movies
    And I follow links facets
    And I open the element with field display-name and value Viewer's Rating
    And I follow the link facetselector
    Then the expected facet choice list matches the following list
      | value        | count |
      | 4 stars & Up | 30    |
      | 3 stars & Up | 10    |
      | 2 stars & Up | 15    |
      | 1 star & Up  | 6     |

  @cleanupPriceFacet
  Scenario: Edit values of existing Range Facet
    Given a facet Price that is configured with the following ranges
      | Lower Bound | Upper Bound | Label          |
      | 0           | 5           | Below $5       |
      | 5           | 20          | $5 to $20      |
      | 20          | 50          | $20 to $50     |
      | 50          | 200         | $50 to $200    |
      | 200         | *           | $200 and Above |
    When I configure facet Price with the following ranges
      | Lower Bound | Upper Bound | Label         |
      | 0           | 10          | Below $10     |
      | 10          | 30          | $10 to $30    |
      | 30          | 60          | $30 to $60    |
      | 60          | *           | $60 and Above |
    And I search for offer movies
    And I follow links facets
    And I open the element with field display-name and value Price
    And I follow the link facetselector
    Then the expected facet choice list matches the following list
      | value         | count |
      | $30 to $60    | 8     |
      | Below $10     | 9     |
      | $60 and Above | 9     |
      | $10 to $30    | 19    |

  @cleanupDisabledFacet
  Scenario Outline: Disable preconfigured facets
    Given a facet <FACET_NAME> that is configured with Facetable value <INITIAL_FACET_OPTION>
    When I configure facet <FACET_NAME> with facetable option No Facet
    Then I should see facet <FACET_NAME> with the following details
      | Facetable | No Facet |
    When I search for offer movies
    And I zoom the facets with zoom element
    Then the list of facets does not contain the following
      | <FACET_NAME> |

    Examples:
      | FACET_NAME | INITIAL_FACET_OPTION |
      | Languages  | Facet                |
      | Runtime    | Range Facet          |

  Scenario: Facet localization
    When I configure facet Brand with the following display names
      | English | Brand    |
      | French  | Brand_fr |
    And I search for offer movies
    And I zoom the facets elements with language fr
    And cache expires for facet Brand_fr
    Then there are the following facets
      | Brand_fr |

  Scenario Outline: Filter Facet table by group
      When I filter facet table by group <FACET_GROUP>
      Then I should only see facets with group <FACET_GROUP>

    Examples:
      | FACET_GROUP       |
      | Product Attribute |
      | SKU Attribute     |
      | SKU Option        |
      | Field             |
