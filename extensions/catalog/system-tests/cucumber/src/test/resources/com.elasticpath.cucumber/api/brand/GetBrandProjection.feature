@api @regression
Feature: Get Brand projection API tests
  # Scenario with brand projection of deleted brand is covered in Brand end-to-end tests

  Background:
    Given I have brand projections for one store with the following parameters
      | languageLocale | en        |
      | displayNames   | brandName |
      | versions       | 1         |
      | deleted        | 0         |
      | schemaVersions | 1.0       |

  Scenario Outline: Get changed brand projection via API with ETag
    When I retrieve created in DB brand projection for generated store via API
    Then Response status code is 200
    And Single brand projection API response has same values as generated projection
    # empty values will be replaced in the step with values from entities created in previous steps
      | type           | brand     |
      | code           |           |
      | store          |           |
      | deleted        | false     |
      | languageLocale | en        |
      | displayName    | brandName |
    When I edit generated brand projection with the following parameters
      | version        | 1              |
      | deleted        | 0              |
      | languageLocale | en             |
      | displayName    | <brandNewName> |
    And I retrieve created in DB brand projection for created store using ETag from previous API call and see response status code 200
    And Single brand projection API response has same values as generated projection
    # empty values will be replaced in the step with values from entities created in previous steps
      | type           | brand          |
      | code           |                |
      | store          |                |
      | deleted        | false          |
      | languageLocale | en             |
      | displayName    | <brandNewName> |
    And Response ETag header differs from previous API call's ETag header

    Examples:
      | brandNewName |
      | brandNewName |

  Scenario: Get unchanged brand projection via API with ETag
    When I retrieve created in DB brand projection for generated store via API
    Then Response status code is 200
    And Single brand projection API response has same values as generated projection
    # empty values will be replaced in the step with values from entities created in previous steps
      | type           | brand     |
      | code           |           |
      | store          |           |
      | deleted        | false     |
      | languageLocale | en        |
      | displayName    | brandName |
    And I retrieve created in DB brand projection for created store using ETag from previous API call and see response status code 304
    And Response does not have content
    And Response ETag header is the same as in the previous API call's header

  Scenario: Get brand projection via API with non-existent entities
    When I retrieve created in DB brand projection for store NON-EXISTENT-STORE via API
    Then Response status code is 404
    And Response does not have content
    And I retrieve brand projection NON-EXISTENT-SKU-CODE for store generated in DB via API
    Then Response status code is 404
    And Response does not have content