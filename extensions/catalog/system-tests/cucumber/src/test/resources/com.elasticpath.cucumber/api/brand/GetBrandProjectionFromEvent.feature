@api @regression
Feature: Get Brand projections by event message API tests

  Scenario Outline: Get one brand projection via API POST request with Catalog event message as a body
    Given I have brand projections for one store with the following parameters
      | languageLocale | en          |
      | displayNames   | <brandName> |
      | versions       | 1           |
      | deleted        | 0           |
      | schemaVersions | 1.0         |
    When I retrieve a created in DB brand projection for created store via POST request
    Then Response status code is 200
    And Multiple brands API response has same values as generated brand projection
      | type           | brand       |
      | code           |             |
      | store          |             |
      | deleted        | false       |
      | languageLocale | en          |
      | displayName    | <brandName> |

    Examples:
      | brandName |
      | brandName |

  Scenario Outline: Get two brand projections via API POST request with Catalog event message as a body which contains 2 brand codes
    Given I have brand projections for one store with the following parameters
      | languageLocale | en                                 |
      | displayNames   | <firstBrandName>,<secondBrandName> |
      | versions       | 1,1                                |
      | deleted        | 0,0                                |
      | schemaVersions | 1.0,1.0                            |
    When I retrieve 2 created in DB brand projections for created store via POST request
    Then Response status code is 200
    And Multiple brands API response has same values as the first generated brand projection
      | type           | brand            |
      | code           |                  |
      | store          |                  |
      | deleted        | false            |
      | languageLocale | en               |
      | displayName    | <firstBrandName> |
    And Multiple brands API response has same values as latest generated brand projection
      | type           | brand             |
      | code           |                   |
      | store          |                   |
      | deleted        | false             |
      | languageLocale | en                |
      | displayName    | <secondBrandName> |
    Examples:
      | firstBrandName | secondBrandName |
      | brandName      | brandNameSecond |

  Scenario Outline: Get partial content of brand projections via API POST request with Catalog event message as a body which contains one malformed brand code
    Given I have brand projections for one store with the following parameters
      | languageLocale | en                                 |
      | displayNames   | <firstBrandName>,<secondBrandName> |
      | versions       | 1,1                                |
      | deleted        | 0,0                                |
      | schemaVersions | 1.0,1.0                            |
    When I retrieve a created in DB brand projection for created store via POST request when codes list contains one non-existent brand code
    Then Response status code is 206
    And Multiple brands API response has same values as latest generated brand projection
      | type           | brand             |
      | code           |                   |
      | store          |                   |
      | deleted        | false             |
      | languageLocale | en                |
      | displayName    | <secondBrandName> |
    And Multiple options API response does not have the first generated option projection
    Examples:
      | firstBrandName | secondBrandName |
      | brandName      | brandNameSecond |

  Scenario: Get brand projection via API POST request with malformed Catalog event message as a body
    Given I have brand projections for one store with the following parameters
      | languageLocale | en          |
      | displayNames   | <brandName> |
      | versions       | 1           |
      | deleted        | 0           |
      | schemaVersions | 1.0         |
    When I retrieve a created in DB brand projection for created store via POST request with malformed body
    Then Response status code is 400
    And Response does not have content

  Scenario: Get brand projection via API POST request with Catalog event message as a body when store code in URL differs from the one in the body
    Given I have brand projections for one store with the following parameters
      | languageLocale | en          |
      | displayNames   | <brandName> |
      | versions       | 1           |
      | deleted        | 0           |
      | schemaVersions | 1.0         |
    When I retrieve a created in DB brand projection for generated store via POST request when body contains store code NON-EXISTING-STORE
    Then Response status code is 400
    And Response does not have content