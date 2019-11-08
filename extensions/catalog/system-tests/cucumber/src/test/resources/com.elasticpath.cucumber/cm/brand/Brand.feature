@cm @regression
Feature: Syndication flow for Brand

  Background:
    Given I have catalog Syndication Two Stores Catalog
    And I have stores SyndicationBilingualStore, SyndicationSingleLanguageStore connected to Syndication Two Stores Catalog catalog
    When I sign in to CM as admin user
    And I go to Catalog Management
    And I select Brands tab in a catalog editor for catalog Syndication Two Stores Catalog

  Scenario Outline: Create and then Delete Brand, check ep.catalog JMS message, check created brand projection via API
    And I am listening to <queueName> queue
    And I delete all messages from <queueName> queue
    And I add a new brand with brand code brandCode with the following exactly names
      | English | <brandNameEn> |
      | French  | <brandNameFr> |
    And I save my changes
    Then Newly created brand is in the list by code
    When I go to Configuration
    And I go to Stores
    And I take all exist store codes
    Then There are brand projections for all exist stores
    When I read <queueName> message from queue
    Then Catalog event JMS message json for store <storeTwoLanguages> contains the following values
    # empty values will be replaced in the step with values from entities created in previous steps
      | key    | value               |
      | @class | CatalogEventType    |
      | name   | BRANDS_UPDATED      |
      | guid   | AGGREGATE           |
      | type   | brand               |
      | store  | <storeTwoLanguages> |
      | codes  |                     |
    And Catalog event JMS message json for store <storeOneLanguage> contains the following values
    # empty values will be replaced in the step with values from entities created in previous steps
      | key    | value              |
      | @class | CatalogEventType   |
      | name   | BRANDS_UPDATED     |
      | guid   | AGGREGATE          |
      | type   | brand              |
      | store  | <storeOneLanguage> |
      | codes  |                    |
    When I retrieve latest version of created in CM brand projection for store <storeTwoLanguages> via API
    Then Response status code is 200
    And Single brand API response contains complete information for projection with 2 languages
      | type                  | brand               |
      | code                  |                     |
      | store                 | <storeTwoLanguages> |
      | deleted               | false               |
      | firstLanguageLocale   | en                  |
      | secondLanguageLocale  | fr                  |
      | firstLanguage         | English             |
      | secondLanguage        | French              |
      | displayNameFirstLang  | <brandNameEn>       |
      | displayNameSecondLang | <brandNameFr>       |
    When I retrieve latest version of created in CM brand projection for store <storeOneLanguage> via API
    Then Response status code is 200
    And Single brand API response contains complete information for projection with 1 language
    # empty values will be replaced in the step with values from entities created in previous steps
      | type                   | brand              |
      | code                   |                    |
      | store                  | <storeOneLanguage> |
      | deleted                | false              |
      | languageLocale         | fr                 |
      | excludedLanguageLocale | en                 |
      | language               | French             |
      | displayName            | <brandNameFr>      |
    And I go to Catalog Management
    And I select Brands tab in a catalog editor for catalog Syndication Two Stores Catalog
    And I delete all messages from <queueName> queue
    And I delete the created brand by code
    And I save my changes
    And I read <queueName> message from queue
    Then Catalog event JMS message json for store <storeTwoLanguages> contains the following values
    # empty values will be replaced in the step with values from entities created in previous steps
      | key    | value               |
      | @class | CatalogEventType    |
      | name   | BRANDS_UPDATED      |
      | guid   | AGGREGATE           |
      | type   | brand               |
      | store  | <storeTwoLanguages> |
      | codes  |                     |
    And Catalog event JMS message json for store <storeOneLanguage> contains the following values
    # empty values will be replaced in the step with values from entities created in previous steps
      | key    | value              |
      | @class | CatalogEventType   |
      | name   | BRANDS_UPDATED     |
      | guid   | AGGREGATE          |
      | type   | brand              |
      | store  | <storeOneLanguage> |
      | codes  |                    |
    When I retrieve latest version of created in CM brand projection for store <storeTwoLanguages> via API
    Then Response status code is 200
    And Single brand API response contains complete information for projection of deleted brand
    # empty values will be replaced in the step with values from entities created in previous steps
      | type    | brand               |
      | code    |                     |
      | store   | <storeTwoLanguages> |
      | deleted | true                |
    When I retrieve latest version of created in CM brand projection for store <storeOneLanguage> via API
    Then Response status code is 200
    And Single brand API response contains complete information for projection of deleted brand
    # empty values will be replaced in the step with values from entities created in previous steps
      | type    | brand              |
      | code    |                    |
      | store   | <storeOneLanguage> |
      | deleted | true               |

    Examples:
      | queueName                             | brandNameEn | brandNameFr | storeTwoLanguages         | storeOneLanguage               |
      | Consumer.test.VirtualTopic.ep.catalog | brandNameEn | brandNameFr | SyndicationBilingualStore | SyndicationSingleLanguageStore |

  Scenario Outline: Update Brand, check ep.catalog JMS message, check created brand projection via API
    When I have brand with code brandTestBrand1
      | English | brandTestBrand1En |
      | French  | brandTestBrand1Fr |
    And I am listening to <queueName> queue
    And I delete all messages from <queueName> queue
    And I edit last 5 characters of brand names with random characters for the following languages without saving
      | <firstLanguage> | <secondLanguage> |
    And I save my changes
    And I read <queueName> message from queue
    Then Catalog event JMS message json for store <storeTwoLanguages> contains the following values
    # empty values will be replaced in the step with values from entities created in previous steps
      | key    | value               |
      | @class | CatalogEventType    |
      | name   | BRANDS_UPDATED      |
      | guid   | AGGREGATE           |
      | type   | brand               |
      | store  | <storeTwoLanguages> |
      | codes  |                     |
    And Catalog event JMS message json for store <storeOneLanguage> contains the following values
    # empty values will be replaced in the step with values from entities created in previous steps
      | key    | value              |
      | @class | CatalogEventType   |
      | name   | BRANDS_UPDATED     |
      | guid   | AGGREGATE          |
      | type   | brand              |
      | store  | <storeOneLanguage> |
      | codes  |                    |
    When I retrieve latest version of created in CM brand projection for store <storeTwoLanguages> via API
    Then Response status code is 200
    And Single brand API response contains complete information for projection with 2 languages
      | type                  | brand               |
      | code                  |                     |
      | store                 | <storeTwoLanguages> |
      | deleted               | false               |
      | firstLanguageLocale   | en                  |
      | secondLanguageLocale  | fr                  |
      | firstLanguage         | <firstLanguage>     |
      | secondLanguage        | <secondLanguage>    |
      | displayNameFirstLang  |                     |
      | displayNameSecondLang |                     |
    When I retrieve latest version of created in CM brand projection for store <storeOneLanguage> via API
    Then Response status code is 200
    And Single brand API response contains complete information for projection with 1 language
    # empty values will be replaced in the step with values from entities created in previous steps
      | type                   | brand              |
      | code                   |                    |
      | store                  | <storeOneLanguage> |
      | deleted                | false              |
      | languageLocale         | fr                 |
      | excludedLanguageLocale | en                 |
      | language               | <secondLanguage>   |
      | displayName            |                    |

    Examples:
      | queueName                             | firstLanguage | secondLanguage | storeTwoLanguages         | storeOneLanguage               |
      | Consumer.test.VirtualTopic.ep.catalog | English       | French         | SyndicationBilingualStore | SyndicationSingleLanguageStore |
