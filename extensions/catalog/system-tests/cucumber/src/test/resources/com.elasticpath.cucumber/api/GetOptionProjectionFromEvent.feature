@api @regression
Feature: Get Option projections by event message API tests

  Scenario: Get one option projection via API POST request with Catalog event message as a body
    Given I have option projection content with one language and one value
      | languageLocale  | en                 |
      | displayName     | skuOptionName      |
      | optionValue     | skuOptionValueCode |
      | optionValueName | skuOptionValueName |
    And I have option projection with generated content
      | version       | 1   |
      | deleted       | 0   |
      | schemaVersion | 1.0 |
    When I retrieve a created in DB option projection for created store via POST request
    Then Response status code is 200
    And Multiple options API response has same values as generated option projection
      | type               | option |
      | code               |        |
      | store              |        |
      | deleted            | false  |
      | languageLocale     | en     |
      | displayName        |        |
      | optionValue        |        |
      | optionDisplayValue |        |

  Scenario Outline: Get two option projections via API POST request with Catalog event message as a body which contains 2 sku options codes
    Given I have option projection content with one language and one value
      | languageLocale  | en                     |
      | displayName     | <firstDisplayName>     |
      | optionValue     | <firstOptionValue>     |
      | optionValueName | <firstOptionValueName> |
    And I have option projection with generated content
      | version       | 1   |
      | deleted       | 0   |
      | schemaVersion | 1.0 |
    And I have option projection content with one language and one value
      | languageLocale  | en                      |
      | displayName     | <secondDisplayName>     |
      | optionValue     | <secondOptionValue>     |
      | optionValueName | <secondOptionValueName> |
    And I have option projection for previously generated store with generated content
      | version       | 1   |
      | deleted       | 0   |
      | schemaVersion | 1.0 |
    When I retrieve 2 created in DB option projections for created store via POST request
    Then Response status code is 200
    And Multiple options API response has same values as the first generated option projection
      | type               | option                 |
      | code               |                        |
      | store              |                        |
      | deleted            | false                  |
      | languageLocale     | en                     |
      | displayName        | <firstDisplayName>     |
      | optionValue        | <firstOptionValue>     |
      | optionDisplayValue | <firstOptionValueName> |
    And Multiple options API response has same values as latest generated option projection
      | type               | option                  |
      | code               |                         |
      | store              |                         |
      | deleted            | false                   |
      | languageLocale     | en                      |
      | displayName        | <secondDisplayName>     |
      | optionValue        | <secondOptionValue>     |
      | optionDisplayValue | <secondOptionValueName> |
    Examples:
      | firstDisplayName | firstOptionValue   | firstOptionValueName | secondDisplayName   | secondOptionValue        | secondOptionValueName    |
      | skuOptionName    | skuOptionValueCode | skuOptionValueName   | skuOptionNameSecond | skuOptionValueCodeSecond | skuOptionValueNameSecond |

  Scenario Outline: Get partial content of option projections via API POST request with Catalog event message as a body which contains one malformed sku options code
    Given I have option projection content with one language and one value
      | languageLocale  | en                     |
      | displayName     | <firstDisplayName>     |
      | optionValue     | <firstOptionValue>     |
      | optionValueName | <firstOptionValueName> |
    And I have option projection with generated content
      | version       | 1   |
      | deleted       | 0   |
      | schemaVersion | 1.0 |
    And I have option projection content with one language and one value
      | languageLocale  | en                      |
      | displayName     | <secondDisplayName>     |
      | optionValue     | <secondOptionValue>     |
      | optionValueName | <secondOptionValueName> |
    And I have option projection for previously generated store with generated content
      | version       | 1   |
      | deleted       | 0   |
      | schemaVersion | 1.0 |
    When I retrieve a created in DB option projection for created store via POST request when codes list contains one non-existent sku code
    Then Response status code is 206
    And Multiple options API response has same values as latest generated option projection
      | type               | option                  |
      | code               |                         |
      | store              |                         |
      | deleted            | false                   |
      | languageLocale     | en                      |
      | displayName        | <secondDisplayName>     |
      | optionValue        | <secondOptionValue>     |
      | optionDisplayValue | <secondOptionValueName> |
    And Multiple options API response does not have the first generated option projection
    Examples:
      | firstDisplayName | firstOptionValue   | firstOptionValueName | secondDisplayName   | secondOptionValue        | secondOptionValueName    |
      | skuOptionName    | skuOptionValueCode | skuOptionValueName   | skuOptionNameSecond | skuOptionValueCodeSecond | skuOptionValueNameSecond |

  Scenario: Get option projection via API POST request with malformed Catalog event message as a body
    Given I have option projection content with one language and one value
      | languageLocale  | en                 |
      | displayName     | skuOptionName      |
      | optionValue     | skuOptionValueCode |
      | optionValueName | skuOptionValueName |
    And I have option projection with generated content
      | version       | 1   |
      | deleted       | 0   |
      | schemaVersion | 1.0 |
    When I retrieve a created in DB option projection for created store via POST request with malformed body
    Then Response status code is 400
    And Response does not have content

  Scenario: Get option projection via API POST request with Catalog event message as a body when store code in URL differs from the one in the body
    Given I have option projection content with one language and one value
      | languageLocale  | en                 |
      | displayName     | skuOptionName      |
      | optionValue     | skuOptionValueCode |
      | optionValueName | skuOptionValueName |
    And I have option projection with generated content
      | version       | 1   |
      | deleted       | 0   |
      | schemaVersion | 1.0 |
    When I retrieve a created in DB option projection for generated store via POST request when body contains store code NON-EXISTING-STORE
    Then Response status code is 400
    And Response does not have content