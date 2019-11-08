@api @regression
Feature: Get all Option projections tests

  Scenario Outline: Get all option projections without pagination parameters when there are less projections than default page size
    Given I have option projections for one store with the following parameters
      | languageLocale                | en                                                                          |
      | displayNames                  | <displayNameA>,<displayNameB>,<displayNameC>,<displayNameD>                 |
      | optionValues                  | <optionValueA>,<optionValueB>,<optionValueC>,<optionValueD>                 |
      | optionValueNames              | <optionValueNameA>,<optionValueNameB>,<optionValueNameC>,<optionValueNameD> |
      | versions                      | 1,1,1,1                                                                     |
      | deleted                       | 0,0,0,0                                                                     |
      | schemaVersions                | 1.0,1.0,1.0,1.0                                                             |
      | projectionDateTimePastOffsets |                                                                             |
    When I retrieve option projections created in DB for created store
      | limit          |  |
      | startAfterCode |  |
    Then Response status code is 200
    And Multiple options API response has the same values as all the generated option projections
      | type                | option                                                                      |
      | deleted             | false                                                                       |
      | languageLocale      | en                                                                          |
      | displayNames        | <displayNameA>,<displayNameB>,<displayNameC>,<displayNameD>                 |
      | optionValues        | <optionValueA>,<optionValueB>,<optionValueC>,<optionValueD>                 |
      | optionDisplayValues | <optionValueNameA>,<optionValueNameB>,<optionValueNameC>,<optionValueNameD> |
    And Multiple options API response has pagination block
      | limit          | 10    |
      | startAfter     |       |
      | hasMoreResults | false |
    And Multiple options API response has non empty currentDateTime

    Examples:
      | displayNameA   | optionValueA        | optionValueNameA    | displayNameB   | optionValueB        | optionValueNameB    | displayNameC   | optionValueC        | optionValueNameC    | displayNameD   | optionValueD        | optionValueNameD    |
      | skuOptionNameA | skuOptionAValueCode | skuOptionAValueName | skuOptionNameB | skuOptionBValueCode | skuOptionBValueName | skuOptionNameC | skuOptionCValueCode | skuOptionCValueName | skuOptionNameD | skuOptionDValueCode | skuOptionDValueName |

  Scenario Outline: Get all option projections with pagination parameters
    Given I have option projections for one store with the following parameters
      | languageLocale                | en                                                                                             |
      | displayNames                  | <displayNameA>,<displayNameB>,<displayNameC>,<displayNameD>,<displayNameE>                     |
      | optionValues                  | <optionValueA>,<optionValueB>,<optionValueC>,<optionValueD>,<optionValueE>                     |
      | optionValueNames              | <optionValueNameA>,<optionValueNameB>,<optionValueNameC>,<optionValueNameD>,<optionValueNameE> |
      | versions                      | 1,1,1,1,1                                                                                      |
      | deleted                       | 0,0,0,0,0                                                                                      |
      | schemaVersions                | 1.0,1.0,1.0,1.0,1.0                                                                            |
      | projectionDateTimePastOffsets |                                                                                                |
    When I retrieve option projections created in DB for created store
      | limit          | 2 |
      | startAfterCode |   |
    Then Response status code is 200
    And Multiple options API response page with size 2 has correct option projections and pagination block
      | type                | option                                                                                         |
      | deleted             | false                                                                                          |
      | languageLocale      | en                                                                                             |
      | displayNames        | <displayNameA>,<displayNameB>,<displayNameC>,<displayNameD>,<displayNameE>                     |
      | optionValues        | <optionValueA>,<optionValueB>,<optionValueC>,<optionValueD>,<optionValueE>                     |
      | optionDisplayValues | <optionValueNameA>,<optionValueNameB>,<optionValueNameC>,<optionValueNameD>,<optionValueNameE> |
      | hasMoreResults      | true                                                                                           |
    And Multiple options API response has non empty currentDateTime
    When I retrieve option projections created in DB for created store starting after previously received response page
      | limit | 3 |
    Then Response status code is 200
    And Multiple options API response page with size 3 has correct option projections and pagination block
      | type                | option                                                                                         |
      | deleted             | false                                                                                          |
      | languageLocale      | en                                                                                             |
      | displayNames        | <displayNameA>,<displayNameB>,<displayNameC>,<displayNameD>,<displayNameE>                     |
      | optionValues        | <optionValueA>,<optionValueB>,<optionValueC>,<optionValueD>,<optionValueE>                     |
      | optionDisplayValues | <optionValueNameA>,<optionValueNameB>,<optionValueNameC>,<optionValueNameD>,<optionValueNameE> |
      | hasMoreResults      | false                                                                                          |
    And Multiple options API response does not have currentDateTime element

    Examples:
      | displayNameA   | optionValueA        | optionValueNameA    | displayNameB   | optionValueB        | optionValueNameB    | displayNameC   | optionValueC        | optionValueNameC    | displayNameD   | optionValueD        | optionValueNameD    | displayNameE   | optionValueE        | optionValueNameE    |
      | skuOptionAName | skuOptionAValueCode | skuOptionAValueName | skuOptionBName | skuOptionBValueCode | skuOptionBValueName | skuOptionCName | skuOptionCValueCode | skuOptionCValueName | skuOptionDName | skuOptionDValueCode | skuOptionDValueName | skuOptionEName | skuOptionEValueCode | skuOptionEValueName |

  Scenario Outline: Get all option projections with pagination parameters when new options are added between calls
    Given I have option projections for one store with codes starting from specified parameter
      | codes                         | B,C,D,E                                                                     |
      | languageLocale                | en                                                                          |
      | displayNames                  | <displayNameB>,<displayNameC>,<displayNameD>,<displayNameE>                 |
      | optionValues                  | <optionValueB>,<optionValueC>,<optionValueD>,<optionValueE>                 |
      | optionValueNames              | <optionValueNameB>,<optionValueNameC>,<optionValueNameD>,<optionValueNameE> |
      | versions                      | 1,1,1,1                                                                     |
      | deleted                       | 0,0,0,0                                                                     |
      | schemaVersions                | 1.0,1.0,1.0,1.0                                                             |
      | projectionDateTimePastOffsets |                                                                             |
    When I retrieve option projections created in DB for created store
      | limit          | 2 |
      | startAfterCode |   |
    Then Response status code is 200
    And Multiple options API response page with size 2 has correct option projections and pagination block
      | type                | option                                                                      |
      | deleted             | false                                                                       |
      | languageLocale      | en                                                                          |
      | displayNames        | <displayNameB>,<displayNameC>,<displayNameD>,<displayNameE>                 |
      | optionValues        | <optionValueB>,<optionValueC>,<optionValueD>,<optionValueE>                 |
      | optionDisplayValues | <optionValueNameB>,<optionValueNameC>,<optionValueNameD>,<optionValueNameE> |
      | hasMoreResults      | true                                                                        |
    And Multiple options API response has non empty currentDateTime
    When I add option projections for previously generated store with codes starting from specified parameter
      | codes                         | A,F                                   |
      | languageLocale                | en                                    |
      | displayNames                  | <displayNameA>,<displayNameF>         |
      | optionValues                  | <optionValueA>,<optionValueF>         |
      | optionValueNames              | <optionValueNameA>,<optionValueNameF> |
      | versions                      | 1,1                                   |
      | deleted                       | 0,0                                   |
      | schemaVersions                | 1.0,1.0                               |
      | projectionDateTimePastOffsets |                                       |
    When I retrieve option projections created in DB for created store starting after previously received response page
      | limit | 2 |
    Then Response status code is 200
    And Multiple options API response page with size 2 has correct option projections and pagination block
      | type                | option                                                                                                            |
      | deleted             | false                                                                                                             |
      | languageLocale      | en                                                                                                                |
      | displayNames        | <displayNameB>,<displayNameC>,<displayNameD>,<displayNameE>,<displayNameA>,<displayNameF>                         |
      | optionValues        | <optionValueB>,<optionValueC>,<optionValueD>,<optionValueE>,<optionValueA>,<optionValueF>                         |
      | optionDisplayValues | <optionValueNameB>,<optionValueNameC>,<optionValueNameD>,<optionValueNameE>,<optionValueNameA>,<optionValueNameF> |
      | hasMoreResults      | true                                                                                                              |
    And Multiple options API response does not have currentDateTime element
    When I retrieve option projections created in DB for created store starting after previously received response page
      | limit | 2 |
    Then Response status code is 200
    And Multiple options API response page with size 2 has correct option projections and pagination block
      | type                | option                                                                                                            |
      | deleted             | false                                                                                                             |
      | languageLocale      | en                                                                                                                |
      | displayNames        | <displayNameB>,<displayNameC>,<displayNameD>,<displayNameE>,<displayNameA>,<displayNameF>                         |
      | optionValues        | <optionValueB>,<optionValueC>,<optionValueD>,<optionValueE>,<optionValueA>,<optionValueF>                         |
      | optionDisplayValues | <optionValueNameB>,<optionValueNameC>,<optionValueNameD>,<optionValueNameE>,<optionValueNameA>,<optionValueNameF> |
      | hasMoreResults      | false                                                                                                             |
    And Multiple options API response does not have currentDateTime element

    Examples:
      | displayNameA   | optionValueA        | optionValueNameA    | displayNameB   | optionValueB        | optionValueNameB    | displayNameC   | optionValueC        | optionValueNameC    | displayNameD   | optionValueD        | optionValueNameD    | displayNameE   | optionValueE        | optionValueNameE    | displayNameF   | optionValueF        | optionValueNameF    |
      | skuOptionAName | skuOptionAValueCode | skuOptionAValueName | skuOptionBName | skuOptionBValueCode | skuOptionBValueName | skuOptionCName | skuOptionCValueCode | skuOptionCValueName | skuOptionDName | skuOptionDValueCode | skuOptionDValueName | skuOptionEName | skuOptionEValueCode | skuOptionEValueName | skuOptionFName | skuOptionFValueCode | skuOptionFValueName |

  Scenario: Get all option projections for store when there are no option projections for such a store
    When I retrieve option projections created in DB for store NON-EXISTING-STORE
      | limit          |  |
      | startAfterCode |  |
    Then Response status code is 200
    And Multiple options API response has empty results
    And Multiple options API response has pagination block
      | limit          | 10    |
      | startAfter     |       |
      | hasMoreResults | false |
    And Multiple options API response has non empty currentDateTime

  Scenario Outline: Get all option projections with non-existent value in pagination startAfter parameter
    Given I have option projections for one store with codes starting from specified parameter
      | codes                         | B,C                                   |
      | languageLocale                | en                                    |
      | displayNames                  | <displayNameB>,<displayNameC>         |
      | optionValues                  | <optionValueB>,<optionValueC>         |
      | optionValueNames              | <optionValueNameB>,<optionValueNameC> |
      | versions                      | 1,1                                   |
      | deleted                       | 0,0                                   |
      | schemaVersions                | 1.0,1.0                               |
      | projectionDateTimePastOffsets |                                       |
    When I retrieve option projections created in DB for created store
      | limit          | 2     |
      | startAfterCode | Acode |
    Then Response status code is 200
    And Multiple options API response page with size 2 has correct option projections and pagination block
      | type                | option                                |
      | deleted             | false                                 |
      | languageLocale      | en                                    |
      | displayNames        | <displayNameB>,<displayNameC>         |
      | optionValues        | <optionValueB>,<optionValueC>         |
      | optionDisplayValues | <optionValueNameB>,<optionValueNameC> |
    And Multiple options API response does not have currentDateTime element
    When I retrieve option projections created in DB for created store
      | limit          | 2     |
      | startAfterCode | Dcode |
    Then Response status code is 200
    And Multiple options API response has empty results
    And Multiple options API response does not have currentDateTime element

    Examples:
      | displayNameB   | optionValueB        | optionValueNameB    | displayNameC   | optionValueC        | optionValueNameC    |
      | skuOptionNameB | skuOptionBValueCode | skuOptionBValueName | skuOptionNameC | skuOptionCValueCode | skuOptionCValueName |

  Scenario: Get all option projections with invalid pagination parameters
    Given I have option projections for one store with the following parameters
      | languageLocale                | en               |
      | displayNames                  | displayNameA     |
      | optionValues                  | optionValueA     |
      | optionValueNames              | optionValueNameA |
      | versions                      | 1                |
      | deleted                       | 0                |
      | schemaVersions                | 1.0              |
      | projectionDateTimePastOffsets |                  |
    When I retrieve option projections created in DB for created store
      | limit          | -1 |
      | startAfterCode |    |
    Then Response status code is 400
    And Response does not have content
    When I retrieve option projections created in DB for created store
      | limit          | 0 |
      | startAfterCode |   |
    Then Response status code is 400
    And Response does not have content
    When I retrieve option projections created in DB for created store
      | limit          | randomString |
      | startAfterCode |              |
    Then Response status code is 400
    And Response does not have content

  Scenario Outline: Get all option projections after specific date and time
    Given I have option projections for one store with the following parameters
      | languageLocale                | en                                                                                             |
      | displayNames                  | <displayNameA>,<displayNameB>,<displayNameC>,<displayNameD>,<displayNameE>                     |
      | optionValues                  | <optionValueA>,<optionValueB>,<optionValueC>,<optionValueD>,<optionValueE>                     |
      | optionValueNames              | <optionValueNameA>,<optionValueNameB>,<optionValueNameC>,<optionValueNameD>,<optionValueNameE> |
      | versions                      | 1,1,1,1,1                                                                                      |
      | deleted                       | 0,0,0,0,0                                                                                      |
      | schemaVersions                | 1.0,1.0,1.0,1.0,1.0                                                                            |
      | projectionDateTimePastOffsets | -70,-35,-20,-6,-5                                                                                   |
    When I retrieve option projections created in DB for created store
      | limit               |     |
      | startAfterCode      |     |
      | modifiedSince       | -15 |
      | modifiedSinceOffset | 0   |
    Then Response status code is 200
    And Multiple options API response has the same values as only provided option projections
      | type                | option                                |
      | deleted             | false                                 |
      | languageLocale      | en                                    |
      | displayNames        | <displayNameD>,<displayNameE>         |
      | optionValues        | <optionValueD>,<optionValueE>         |
      | optionDisplayValues | <optionValueNameD>,<optionValueNameE> |
    And Multiple options API response has pagination block
      | limit          | 10    |
      | startAfter     |       |
      | hasMoreResults | false |
    And Multiple options API response has non empty currentDateTime
    When I retrieve option projections created in DB for created store
      | limit               |     |
      | startAfterCode      |     |
      | modifiedSince       | -15 |
      | modifiedSinceOffset | 15  |
    Then Response status code is 200
    And Multiple options API response has the same values as only provided option projections
      | type                | option                                                   |
      | deleted             | false                                                    |
      | languageLocale      | en                                                       |
      | displayNames        | <displayNameC>,<displayNameD>,<displayNameE>             |
      | optionValues        | <optionValueC>,<optionValueD>,<optionValueE>             |
      | optionDisplayValues | <optionValueNameC>,<optionValueNameD>,<optionValueNameE> |
    And Multiple options API response has pagination block
      | limit          | 10    |
      | startAfter     |       |
      | hasMoreResults | false |
    And Multiple options API response has non empty currentDateTime
    When I retrieve option projections created in DB for created store
      | limit               |     |
      | startAfterCode      |     |
      | modifiedSince       | -15 |
      | modifiedSinceOffset |     |
    Then Response status code is 200
    And Multiple options API response has the same values as only provided option projections
      | type                | option                                                                      |
      | deleted             | false                                                                       |
      | languageLocale      | en                                                                          |
      | displayNames        | <displayNameB>,<displayNameC>,<displayNameD>,<displayNameE>                 |
      | optionValues        | <optionValueB>,<optionValueC>,<optionValueD>,<optionValueE>                 |
      | optionDisplayValues | <optionValueNameB>,<optionValueNameC>,<optionValueNameD>,<optionValueNameE> |
    And Multiple options API response has pagination block
      | limit          | 10    |
      | startAfter     |       |
      | hasMoreResults | false |
    And Multiple options API response has non empty currentDateTime

    Examples:
      | displayNameA   | optionValueA        | optionValueNameA    | displayNameB   | optionValueB        | optionValueNameB    | displayNameC   | optionValueC        | optionValueNameC    | displayNameD   | optionValueD        | optionValueNameD    | displayNameE   | optionValueE        | optionValueNameE    |
      | skuOptionNameA | skuOptionAValueCode | skuOptionAValueName | skuOptionNameB | skuOptionBValueCode | skuOptionBValueName | skuOptionNameC | skuOptionCValueCode | skuOptionCValueName | skuOptionNameD | skuOptionDValueCode | skuOptionDValueName | skuOptionEName | skuOptionEValueCode | skuOptionEValueName |

  Scenario Outline: Get all option projections after specific date and time with invalid URL parameters
    Given I have option projections for one store with the following parameters
      | languageLocale                | en                                                                                             |
      | displayNames                  | <displayNameA>,<displayNameB>,<displayNameC>,<displayNameD>,<displayNameE>                     |
      | optionValues                  | <optionValueA>,<optionValueB>,<optionValueC>,<optionValueD>,<optionValueE>                     |
      | optionValueNames              | <optionValueNameA>,<optionValueNameB>,<optionValueNameC>,<optionValueNameD>,<optionValueNameE> |
      | versions                      | 1,1,1,1,1                                                                                      |
      | deleted                       | 0,0,0,0,0                                                                                      |
      | schemaVersions                | 1.0,1.0,1.0,1.0,1.0                                                                            |
      | projectionDateTimePastOffsets | 70,35,20,6,5                                                                                   |
    When I retrieve option projections created in DB for created store
      | limit               |                  |
      | startAfterCode      |                  |
      | modifiedSince       | someRandomString |
      | modifiedSinceOffset |                  |
    Then Response status code is 400
    And Response does not have content
    When I retrieve option projections created in DB for created store
      | limit               |    |
      | startAfterCode      |    |
      | modifiedSince       | 30 |
      | modifiedSinceOffset |    |
    Then Response status code is 400
    And Response does not have content
    When I retrieve option projections created in DB for created store
      | limit               |                  |
      | startAfterCode      |                  |
      | modifiedSince       | -15              |
      | modifiedSinceOffset | someRandomString |
    Then Response status code is 400
    And Response does not have content
    When I retrieve option projections created in DB for created store
      | limit               |     |
      | startAfterCode      |     |
      | modifiedSince       | -15 |
      | modifiedSinceOffset | -15 |
    Then Response status code is 400
    And Response does not have content

    Examples:
      | displayNameA   | optionValueA        | optionValueNameA    | displayNameB   | optionValueB        | optionValueNameB    | displayNameC   | optionValueC        | optionValueNameC    | displayNameD   | optionValueD        | optionValueNameD    | displayNameE   | optionValueE        | optionValueNameE    |
      | skuOptionNameA | skuOptionAValueCode | skuOptionAValueName | skuOptionNameB | skuOptionBValueCode | skuOptionBValueName | skuOptionNameC | skuOptionCValueCode | skuOptionCValueName | skuOptionNameD | skuOptionDValueCode | skuOptionDValueName | skuOptionEName | skuOptionEValueCode | skuOptionEValueName |