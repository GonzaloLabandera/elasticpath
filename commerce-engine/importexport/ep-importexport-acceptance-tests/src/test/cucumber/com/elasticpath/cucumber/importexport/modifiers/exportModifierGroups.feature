@exportModifiers
Feature: Export Modifiers

Scenario: Export Modifier
  Given the following modifier groups are saved in the database
    | code |
    | testOne |
    | testTwo|

  When the modifier groups in the database are exported using importexport
  And the exported modifier groups are retrieved
  Then the exported modifier group records contain a modifier group with code testOne
  And the exported modifier group records contain a modifier group with code testTwo



  Scenario: Export Modifier Group Filters
    Given the following modifier group filters are saved in the database
      | modifiercode | type | reference |
      | testOne | Catalog | 12345 |
      | testTwo |Cart | 4567      |

    When the modifier group Filters in the database are exported using importexport
    And the exported modifier group filters are retrieved
    Then the exported modifier group filter records contain a modifier group filter with modifier code testOne
    And the exported modifier group filter records contain a modifier group filter with modifier code testTwo