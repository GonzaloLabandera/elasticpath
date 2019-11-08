@api @regression
Feature: Get all Brand projections tests

  Scenario Outline: Get all brand projections without pagination parameters when there are less projections than default page size
    Given I have brand projections for one store with the following parameters
      | languageLocale                | en                                                          |
      | displayNames                  | <displayNameA>,<displayNameB>,<displayNameC>,<displayNameD> |
      | versions                      | 1,1,1,1                                                     |
      | deleted                       | 0,0,0,0                                                     |
      | schemaVersions                | 1.0,1.0,1.0,1.0                                             |
      | projectionDateTimePastOffsets |                                                             |
    When I retrieve brand projections created in DB for created store
      | limit          |  |
      | startAfterCode |  |
    Then Response status code is 200
    And Multiple brands API response has the same values as all the generated brand projections
      | type           | brand                                                       |
      | deleted        | false                                                       |
      | languageLocale | en                                                          |
      | displayNames   | <displayNameA>,<displayNameB>,<displayNameC>,<displayNameD> |
    And Multiple brands API response has pagination block
      | limit          | 10    |
      | startAfter     |       |
      | hasMoreResults | false |
    And Multiple brands API response has non empty currentDateTime

    Examples:
      | displayNameA | displayNameB | displayNameC | displayNameD |
      | brandNameA   | brandNameB   | brandNameC   | brandNameD   |

  Scenario Outline: Get all brand projections with pagination parameters
    Given I have brand projections for one store with the following parameters
      | languageLocale                | en                                                                         |
      | displayNames                  | <displayNameA>,<displayNameB>,<displayNameC>,<displayNameD>,<displayNameE> |
      | versions                      | 1,1,1,1,1                                                                  |
      | deleted                       | 0,0,0,0,0                                                                  |
      | schemaVersions                | 1.0,1.0,1.0,1.0,1.0                                                        |
      | projectionDateTimePastOffsets |                                                                            |
    When I retrieve brand projections created in DB for created store
      | limit          | 2 |
      | startAfterCode |   |
    Then Response status code is 200
    And Multiple brands API response page with size 2 has correct brand projections and pagination block
      | type           | brand                                                                      |
      | deleted        | false                                                                      |
      | languageLocale | en                                                                         |
      | displayNames   | <displayNameA>,<displayNameB>,<displayNameC>,<displayNameD>,<displayNameE> |
      | hasMoreResults | true                                                                       |
    And Multiple brands API response has non empty currentDateTime
    When I retrieve brand projections created in DB for created store starting after previously received response page
      | limit | 3 |
    Then Response status code is 200
    And Multiple brands API response page with size 3 has correct brand projections and pagination block
      | type           | brand                                                                      |
      | deleted        | false                                                                      |
      | languageLocale | en                                                                         |
      | displayNames   | <displayNameA>,<displayNameB>,<displayNameC>,<displayNameD>,<displayNameE> |
      | hasMoreResults | false                                                                      |
    And Multiple brands API response does not have currentDateTime element

    Examples:
      | displayNameA | displayNameB | displayNameC | displayNameD | displayNameE |
      | brandAName   | brandBName   | brandCName   | brandDName   | brandEName   |

  Scenario Outline: Get all brand projections with pagination parameters when new brands are added between calls
    Given I have brand projections for one store with codes starting from specified parameter
      | codes                         | B,C,D,E                                                     |
      | languageLocale                | en                                                          |
      | displayNames                  | <displayNameB>,<displayNameC>,<displayNameD>,<displayNameE> |
      | versions                      | 1,1,1,1                                                     |
      | deleted                       | 0,0,0,0                                                     |
      | schemaVersions                | 1.0,1.0,1.0,1.0                                             |
      | projectionDateTimePastOffsets |                                                             |
    When I retrieve brand projections created in DB for created store
      | limit          | 2 |
      | startAfterCode |   |
    Then Response status code is 200
    And Multiple brands API response page with size 2 has correct brand projections and pagination block
      | type           | brand                                                       |
      | deleted        | false                                                       |
      | languageLocale | en                                                          |
      | displayNames   | <displayNameB>,<displayNameC>,<displayNameD>,<displayNameE> |
      | hasMoreResults | true                                                        |
    And Multiple brands API response has non empty currentDateTime
    When I add brand projections for previously generated store with codes starting from specified parameter
      | codes                         | A,F                           |
      | languageLocale                | en                            |
      | displayNames                  | <displayNameA>,<displayNameF> |
      | versions                      | 1,1                           |
      | deleted                       | 0,0                           |
      | schemaVersions                | 1.0,1.0                       |
      | projectionDateTimePastOffsets |                               |
    When I retrieve brand projections created in DB for created store starting after previously received response page
      | limit | 2 |
    Then Response status code is 200
    And Multiple brands API response page with size 2 has correct brand projections and pagination block
      | type           | brand                                                                                     |
      | deleted        | false                                                                                     |
      | languageLocale | en                                                                                        |
      | displayNames   | <displayNameB>,<displayNameC>,<displayNameD>,<displayNameE>,<displayNameA>,<displayNameF> |
      | hasMoreResults | true                                                                                      |
    And Multiple brands API response does not have currentDateTime element
    When I retrieve brand projections created in DB for created store starting after previously received response page
      | limit | 2 |
    Then Response status code is 200
    And Multiple brands API response page with size 2 has correct brand projections and pagination block
      | type           | brand                                                                                     |
      | deleted        | false                                                                                     |
      | languageLocale | en                                                                                        |
      | displayNames   | <displayNameB>,<displayNameC>,<displayNameD>,<displayNameE>,<displayNameA>,<displayNameF> |
      | hasMoreResults | false                                                                                     |
    And Multiple brands API response does not have currentDateTime element

    Examples:
      | displayNameA | displayNameB | displayNameC | displayNameD | displayNameE | displayNameF |
      | brandAName   | brandBName   | brandCName   | brandDName   | brandEName   | brandFName   |

  Scenario: Get all brand projections for store when there are no brand projections for such a store
    When I retrieve brand projections created in DB for store NON-EXISTING-STORE
      | limit          |  |
      | startAfterCode |  |
    Then Response status code is 200
    And Multiple brands API response has empty results
    And Multiple brands API response has pagination block
      | limit          | 10    |
      | startAfter     |       |
      | hasMoreResults | false |
    And Multiple brands API response has non empty currentDateTime

  Scenario Outline: Get all brand projections with non-existent value in pagination startAfter parameter
    Given I have brand projections for one store with codes starting from specified parameter
      | codes                         | B,C                           |
      | languageLocale                | en                            |
      | displayNames                  | <displayNameB>,<displayNameC> |
      | versions                      | 1,1                           |
      | deleted                       | 0,0                           |
      | schemaVersions                | 1.0,1.0                       |
      | projectionDateTimePastOffsets |                               |
    When I retrieve brand projections created in DB for created store
      | limit          | 2     |
      | startAfterCode | Acode |
    Then Response status code is 200
    And Multiple brands API response page with size 2 has correct brand projections and pagination block
      | type           | brand                         |
      | deleted        | false                         |
      | languageLocale | en                            |
      | displayNames   | <displayNameB>,<displayNameC> |
    And Multiple brands API response does not have currentDateTime element
    When I retrieve brand projections created in DB for created store
      | limit          | 2     |
      | startAfterCode | Dcode |
    Then Response status code is 200
    And Multiple brands API response has empty results
    And Multiple brands API response does not have currentDateTime element

    Examples:
      | displayNameB | displayNameC |
      | brandNameB   | brandNameC   |

  Scenario: Get all brand projections with invalid pagination parameters
    Given I have brand projections for one store with the following parameters
      | languageLocale                | en           |
      | displayNames                  | displayNameA |
      | versions                      | 1            |
      | deleted                       | 0            |
      | schemaVersions                | 1.0          |
      | projectionDateTimePastOffsets |              |
    When I retrieve brand projections created in DB for created store
      | limit          | -1 |
      | startAfterCode |    |
    Then Response status code is 400
    And Response does not have content
    When I retrieve brand projections created in DB for created store
      | limit          | 0 |
      | startAfterCode |   |
    Then Response status code is 400
    And Response does not have content
    When I retrieve brand projections created in DB for created store
      | limit          | randomString |
      | startAfterCode |              |
    Then Response status code is 400
    And Response does not have content

  Scenario Outline: Get all brand projections after specific date and time
    Given I have brand projections for one store with the following parameters
      | languageLocale                | en                                                                         |
      | displayNames                  | <displayNameA>,<displayNameB>,<displayNameC>,<displayNameD>,<displayNameE> |
      | versions                      | 1,1,1,1,1                                                                  |
      | deleted                       | 0,0,0,0,0                                                                  |
      | schemaVersions                | 1.0,1.0,1.0,1.0,1.0                                                        |
      | projectionDateTimePastOffsets | -70,-35,-20,-6,-5                                                          |
    When I retrieve brand projections created in DB for created store
      | limit               |     |
      | startAfterCode      |     |
      | modifiedSince       | -15 |
      | modifiedSinceOffset | 0   |
    Then Response status code is 200
    And Multiple brands API response has the same values as only provided brand projections
      | type           | brand                         |
      | deleted        | false                         |
      | languageLocale | en                            |
      | displayNames   | <displayNameD>,<displayNameE> |
    And Multiple brands API response has pagination block
      | limit          | 10    |
      | startAfter     |       |
      | hasMoreResults | false |
    And Multiple brands API response has non empty currentDateTime
    When I retrieve brand projections created in DB for created store
      | limit               |     |
      | startAfterCode      |     |
      | modifiedSince       | -15 |
      | modifiedSinceOffset | 15  |
    Then Response status code is 200
    And Multiple brands API response has the same values as only provided brand projections
      | type           | brand                                        |
      | deleted        | false                                        |
      | languageLocale | en                                           |
      | displayNames   | <displayNameC>,<displayNameD>,<displayNameE> |
    And Multiple brands API response has pagination block
      | limit          | 10    |
      | startAfter     |       |
      | hasMoreResults | false |
    And Multiple brands API response has non empty currentDateTime
    When I retrieve brand projections created in DB for created store
      | limit               |     |
      | startAfterCode      |     |
      | modifiedSince       | -15 |
      | modifiedSinceOffset |     |
    Then Response status code is 200
    And Multiple brands API response has the same values as only provided brand projections
      | type           | brand                                                       |
      | deleted        | false                                                       |
      | languageLocale | en                                                          |
      | displayNames   | <displayNameB>,<displayNameC>,<displayNameD>,<displayNameE> |
    And Multiple brands API response has pagination block
      | limit          | 10    |
      | startAfter     |       |
      | hasMoreResults | false |
    And Multiple brands API response has non empty currentDateTime

    Examples:
      | displayNameA | displayNameB | displayNameC | displayNameD | displayNameE |
      | brandNameA   | brandNameB   | brandNameC   | brandNameD   | brandEName   |

  Scenario Outline: Get all brands projections after specific date and time with invalid URL parameters
    Given I have brand projections for one store with the following parameters
      | languageLocale                | en                                                                         |
      | displayNames                  | <displayNameA>,<displayNameB>,<displayNameC>,<displayNameD>,<displayNameE> |
      | versions                      | 1,1,1,1,1                                                                  |
      | deleted                       | 0,0,0,0,0                                                                  |
      | schemaVersions                | 1.0,1.0,1.0,1.0,1.0                                                        |
      | projectionDateTimePastOffsets | 70,35,20,6,5                                                               |
    When I retrieve brand projections created in DB for created store
      | limit               |                  |
      | startAfterCode      |                  |
      | modifiedSince       | someRandomString |
      | modifiedSinceOffset |                  |
    Then Response status code is 400
    And Response does not have content
    When I retrieve brand projections created in DB for created store
      | limit               |    |
      | startAfterCode      |    |
      | modifiedSince       | 30 |
      | modifiedSinceOffset |    |
    Then Response status code is 400
    And Response does not have content
    When I retrieve brand projections created in DB for created store
      | limit               |                  |
      | startAfterCode      |                  |
      | modifiedSince       | -15              |
      | modifiedSinceOffset | someRandomString |
    Then Response status code is 400
    And Response does not have content
    When I retrieve brand projections created in DB for created store
      | limit               |     |
      | startAfterCode      |     |
      | modifiedSince       | -15 |
      | modifiedSinceOffset | -15 |
    Then Response status code is 400
    And Response does not have content

    Examples:
      | displayNameA | displayNameB | displayNameC | displayNameD | displayNameE |
      | brandNameA   | brandNameB   | brandNameC   | brandNameD   | brandEName   |