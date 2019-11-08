@api @regression
Feature: Get Option projection API tests
  # Scenario with option projection of deleted sku option is covered in Sku Option end-to-end tests

  Background:
    Given I have option projection content with one language and one value
      | languageLocale  | en                 |
      | displayName     | skuOptionName      |
      | optionValue     | skuOptionValueCode |
      | optionValueName | skuOptionValueName |
    And I have option projection with generated content
      | version       | 1   |
      | deleted       | 0   |
      | schemaVersion | 1.0 |

  Scenario: Get changed option projection via API with ETag
    When I retrieve latest version of created in DB option projection for created store via API
    Then Response status code is 200
    And Single option API response has same values as generated option projection
      | type               | option |
      | code               |        |
      | store              |        |
      | deleted            | false  |
      | languageLocale     | en     |
      | displayName        |        |
      | optionValue        |        |
      | optionDisplayValue |        |
    When I edit generated option projection with the following parameters
      | version         | 1                     |
      | deleted         | 0                     |
      | languageLocale  | en                    |
      | displayName     | skuOptionNewName      |
      | optionValue     | skuOptionValueNewCode |
      | optionValueName | skuOptionValueNewName |
    And I retrieve created in DB option projection for created store using ETag from previous API call and see response status code 200
    And Single option API response has same values as updated option projection
      | type               | option |
      | code               |        |
      | store              |        |
      | deleted            | false  |
      | languageLocale     | en     |
      | displayName        |        |
      | optionValue        |        |
      | optionDisplayValue |        |
    And Response ETag header differs from previous API call's ETag header

  Scenario: Get unchanged option projection via API with ETag
    When I retrieve latest version of created in DB option projection for created store via API
    Then Response status code is 200
    And Single option API response has same values as generated option projection
      | type               | option |
      | code               |        |
      | store              |        |
      | deleted            | false  |
      | languageLocale     | en     |
      | displayName        |        |
      | optionValue        |        |
      | optionDisplayValue |        |
    And I retrieve created in DB option projection for created store using ETag from previous API call and see response status code 304
    And Response does not have content
    And Response ETag header is the same as in the previous API call's header

  Scenario: Get option projection via API with non-existent entities
    When I retrieve latest version of created in DB option projection for store NON-EXISTENT-STORE via API
    Then Response status code is 404
    And Response does not have content
    And I retrieve latest version of option projection NON-EXISTENT-SKU-CODE for store generated in DB via API
    Then Response status code is 404
    And Response does not have content