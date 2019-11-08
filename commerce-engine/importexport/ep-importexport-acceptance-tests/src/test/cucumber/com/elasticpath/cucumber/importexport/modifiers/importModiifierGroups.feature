@importModifiers
Feature: Import Modifiers

  Scenario: Import ModifierGroup
    Given the modifier groups import data has been emptied out
    When the following modifier groups are imported using importexport
      | code | displayName | fieldCode | fieldType | maxSize | required | fieldDisplayName|
      | 123456 | mod-group-name| f123456| ShortText| 25      | false    |field-display-name-1|
      | 123457 | mod-group-name-2|f-2   |ShortText |20       |false     |field-display-name-2|
    Then the modifier group with code 123456 is persisted
    And the modifier group with code 123457 is persisted


  Scenario: Import ModifierGroupFilter
    Given the modifier group filters import data has been emptied out
    When the following modifier group filters are imported using importexport
      | modifierCode | referenceGuid | type |
      | 123456 | 54321               | Catalog|
      | 11111 | 22222                |Cart    |
    Then the modifier group filter with modifier code 123456 and reference guid 54321 and type Catalog is persisted
    And the modifier group filter with modifier code 11111 and reference guid 22222 and type Cart is persisted