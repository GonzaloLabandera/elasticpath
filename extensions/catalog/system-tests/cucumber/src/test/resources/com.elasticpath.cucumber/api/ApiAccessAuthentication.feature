@api @regression
Feature: Api access authentication tests

  Scenario: Call syndication API using a user which has only "Web service access" role

    Given I have option projection content with one language and one value
      | languageLocale  | en                 |
      | displayName     | skuOptionName      |
      | optionValue     | skuOptionValueCode |
      | optionValueName | skuOptionValueName |
    And I have option projection with generated content
      | version       | 1   |
      | deleted       | 0   |
      | schemaVersion | 1.0 |
    When I retrieve latest version of created in DB option projection for created store via API
    Then Response status code is 200

  Scenario: Call syndication API using a user which has "Super User" role

    Given I have option projection content with one language and one value
      | languageLocale  | en                 |
      | displayName     | skuOptionName      |
      | optionValue     | skuOptionValueCode |
      | optionValueName | skuOptionValueName |
    And I have option projection with generated content
      | version       | 1   |
      | deleted       | 0   |
      | schemaVersion | 1.0 |
    And I use Super User for API calls
    When I retrieve latest version of created in DB option projection for created store via API
    Then Response status code is 200

  Scenario: Call syndication API using a user which has only "Cm login" role

    Given I have option projection content with one language and one value
      | languageLocale  | en                 |
      | displayName     | skuOptionName      |
      | optionValue     | skuOptionValueCode |
      | optionValueName | skuOptionValueName |
    And I have option projection with generated content
      | version       | 1   |
      | deleted       | 0   |
      | schemaVersion | 1.0 |
    And I use Cm Login User for API calls
    When I retrieve latest version of created in DB option projection for created store via API
    Then Response status code is 403
