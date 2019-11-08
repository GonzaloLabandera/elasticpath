@cm @regression
Feature: Syndication Sku Option flow tests

  Background:
    Given I have catalog Syndication Two Stores Catalog
    And I have stores SyndicationBilingualStore, SyndicationSingleLanguageStore connected to Syndication Two Stores Catalog catalog
    When I sign in to CM as admin user
    And I go to Catalog Management
    And I select SkuOptions tab in a catalog editor for catalog Syndication Two Stores Catalog

  Scenario Outline: Create and then Delete Sku Option, check ep.catalog JMS message, check created option projection via API
    And I am listening to <queueName> queue
    And I delete all messages from <queueName> queue
    And I add a new sku option with sku option code skuCode without saving with the following names
      | English | <skuOptionNameEn> |
      | French  | <skuOptionNameFr> |
    Then newly created sku option is in the list
    When I add a new sku option value with sku option value code <firstOptionValueCode> and the following names without saving
      | English | <firstOptionValueNameEn> |
      | French  | <firstOptionValueNameFr> |
    And I add a new sku option value with sku option value code <secondOptionValueCode> and the following names without saving
      | English | <secondOptionValueNameEn> |
      | French  | <secondOptionValueNameFr> |
    And I save changes made for new sku option
    Then newly created sku option is in the list
    When I go to Configuration
    And I go to Stores
    And I take all exist store codes
    Then There are sku projections for all exist stores
    When I read <queueName> message from queue
    Then Catalog event JMS message json for store <storeTwoLanguages> contains the following values
    # empty values will be replaced in the step with values from entities created in previous steps
      | key    | value               |
      | @class | CatalogEventType    |
      | name   | OPTIONS_UPDATED     |
      | guid   | AGGREGATE           |
      | type   | option              |
      | store  | <storeTwoLanguages> |
      | codes  |                     |
    And Catalog event JMS message json for store <storeOneLanguage> contains the following values
    # empty values will be replaced in the step with values from entities created in previous steps
      | key    | value              |
      | @class | CatalogEventType   |
      | name   | OPTIONS_UPDATED    |
      | guid   | AGGREGATE          |
      | type   | option             |
      | store  | <storeOneLanguage> |
      | codes  |                    |
    When I retrieve latest version of created in CM option projection for store <storeTwoLanguages> via API
    Then Response status code is 200
    And Single option API response contains complete information for projection with 2 sku option values and 2 languages
    # empty values will be replaced in the step with values from entities created in previous steps
      | type                            | option                    |
      | code                            |                           |
      | store                           | <storeTwoLanguages>       |
      | deleted                         | false                     |
      | firstLanguageLocale             | en                        |
      | secondLanguageLocale            | fr                        |
      | firstLanguage                   | English                   |
      | secondLanguage                  | French                    |
      | displayNameFirstLang            | <skuOptionNameEn>         |
      | displayNameSecondLang           | <skuOptionNameFr>         |
      | firstOptionValue                | <firstOptionValueCode>    |
      | firstOptionValueNameFirstLang   | <firstOptionValueNameEn>  |
      | firstOptionValueNameSecondLang  | <firstOptionValueNameFr>  |
      | secondOptionValue               | <secondOptionValueCode>   |
      | secondOptionValueNameFirstLang  | <secondOptionValueNameEn> |
      | secondOptionValueNameSecondLang | <secondOptionValueNameFr> |
    When I retrieve latest version of created in CM option projection for store <storeOneLanguage> via API
    Then Response status code is 200
    And Single option API response contains complete information for projection with 2 sku option values and 1 language
    # empty values will be replaced in the step with values from entities created in previous steps
      | type                   | option                    |
      | code                   |                           |
      | store                  | <storeOneLanguage>        |
      | deleted                | false                     |
      | languageLocale         | fr                        |
      | excludedLanguageLocale | en                        |
      | language               | French                    |
      | displayName            | <skuOptionNameFr>         |
      | firstOptionValue       | <firstOptionValueCode>    |
      | firstOptionValueName   | <firstOptionValueNameFr>  |
      | secondOptionValue      | <secondOptionValueCode>   |
      | secondOptionValueName  | <secondOptionValueNameFr> |
    And I go to Catalog Management
    And I delete all messages from <queueName> queue
    And I delete the newly created sku option
    And I read <queueName> message from queue
    Then Catalog event JMS message json for store <storeTwoLanguages> contains the following values
    # empty values will be replaced in the step with values from entities created in previous steps
      | key    | value               |
      | @class | CatalogEventType    |
      | name   | OPTIONS_UPDATED     |
      | guid   | AGGREGATE           |
      | type   | option              |
      | store  | <storeTwoLanguages> |
      | codes  |                     |
    And Catalog event JMS message json for store <storeOneLanguage> contains the following values
    # empty values will be replaced in the step with values from entities created in previous steps
      | key    | value              |
      | @class | CatalogEventType   |
      | name   | OPTIONS_UPDATED    |
      | guid   | AGGREGATE          |
      | type   | option             |
      | store  | <storeOneLanguage> |
      | codes  |                    |
    When I retrieve latest version of created in CM option projection for store <storeTwoLanguages> via API
    Then Response status code is 200
    And Single option API response contains complete information for projection of deleted sku option
    # empty values will be replaced in the step with values from entities created in previous steps
      | type    | option              |
      | code    |                     |
      | store   | <storeTwoLanguages> |
      | deleted | true                |
    When I retrieve latest version of created in CM option projection for store <storeOneLanguage> via API
    Then Response status code is 200
    And Single option API response contains complete information for projection of deleted sku option
    # empty values will be replaced in the step with values from entities created in previous steps
      | type    | option             |
      | code    |                    |
      | store   | <storeOneLanguage> |
      | deleted | true               |

    Examples:
      | queueName                             | skuOptionNameEn | skuOptionNameFr | storeTwoLanguages         | storeOneLanguage               | firstOptionValueCode | secondOptionValueCode | firstOptionValueNameEn | firstOptionValueNameFr | secondOptionValueNameEn | secondOptionValueNameFr |
      | Consumer.test.VirtualTopic.ep.catalog | skuOptionNameEn | skuOptionNameFr | SyndicationBilingualStore | SyndicationSingleLanguageStore | skuValueCode1        | skuValueCode2         | skuValue1NameEn        | skuValue1NameFr        | skuValue2NameEn         | skuValue2NameFr         |

  Scenario Outline: Update Sku Option, check ep.catalog JMS message, check created option projection via API
    When I have sku option with code optionTestOption1 and two sku option values with codes <firstOptionValueCode> and <secondOptionValueCode> and the following languages
      | <firstLanguage> | <secondLanguage> |
    When I am listening to <queueName> queue
    And I delete all messages from <queueName> queue
    And I edit last 5 characters of sku option names with random characters for the following languages without saving
      | <firstLanguage> | <secondLanguage> |
    And I save changes made for opened sku option
    And I read <queueName> message from queue
    Then Catalog event JMS message json for store <storeTwoLanguages> contains the following values
    # empty values will be replaced in the step with values from entities created in previous steps
      | key    | value               |
      | @class | CatalogEventType    |
      | name   | OPTIONS_UPDATED     |
      | guid   | AGGREGATE           |
      | type   | option              |
      | store  | <storeTwoLanguages> |
      | codes  |                     |
    And Catalog event JMS message json for store <storeOneLanguage> contains the following values
    # empty values will be replaced in the step with values from entities created in previous steps
      | key    | value              |
      | @class | CatalogEventType   |
      | name   | OPTIONS_UPDATED    |
      | guid   | AGGREGATE          |
      | type   | option             |
      | store  | <storeOneLanguage> |
      | codes  |                    |
    And I edit last 5 characters of sku option value <firstOptionValueCode> names without saving
      | <firstLanguage> | <secondLanguage> |
    And I save changes made for opened sku option
    And I delete all messages from <queueName> queue
    And I edit last 5 characters of sku option value <secondOptionValueCode> names without saving
      | <firstLanguage> | <secondLanguage> |
    And I save changes made for opened sku option
    And I read <queueName> message from queue
    Then Catalog event JMS message json for store <storeTwoLanguages> contains the following values
    # empty values will be replaced in the step with values from entities created in previous steps
      | key    | value               |
      | @class | CatalogEventType    |
      | name   | OPTIONS_UPDATED     |
      | guid   | AGGREGATE           |
      | type   | option              |
      | store  | <storeTwoLanguages> |
      | codes  |                     |
    And Catalog event JMS message json for store <storeOneLanguage> contains the following values
    # empty values will be replaced in the step with values from entities created in previous steps
      | key    | value              |
      | @class | CatalogEventType   |
      | name   | OPTIONS_UPDATED    |
      | guid   | AGGREGATE          |
      | type   | option             |
      | store  | <storeOneLanguage> |
      | codes  |                    |
    When I retrieve latest version of created in CM option projection for store <storeTwoLanguages> via API
    Then Response status code is 200
    And Single option API response contains complete information for projection with 2 sku option values and 2 languages
    # empty values will be replaced in the step with values from entities created in previous steps
      | type                            | option                  |
      | code                            |                         |
      | store                           | <storeTwoLanguages>     |
      | deleted                         | false                   |
      | firstLanguageLocale             | en                      |
      | secondLanguageLocale            | fr                      |
      | firstLanguage                   | <firstLanguage>         |
      | secondLanguage                  | <secondLanguage>        |
      | displayNameFirstLang            |                         |
      | displayNameSecondLang           |                         |
      | firstOptionValue                | <firstOptionValueCode>  |
      | firstOptionValueNameFirstLang   |                         |
      | firstOptionValueNameSecondLang  |                         |
      | secondOptionValue               | <secondOptionValueCode> |
      | secondOptionValueNameFirstLang  |                         |
      | secondOptionValueNameSecondLang |                         |
    When I retrieve latest version of created in CM option projection for store <storeOneLanguage> via API
    Then Response status code is 200
    And Single option API response contains complete information for projection with 2 sku option values and 1 language
    # empty values will be replaced in the step with values from entities created in previous steps
      | type                   | option                  |
      | code                   |                         |
      | store                  | <storeOneLanguage>      |
      | deleted                | false                   |
      | languageLocale         | fr                      |
      | excludedLanguageLocale | en                      |
      | language               | <secondLanguage>        |
      | displayName            |                         |
      | firstOptionValue       | <firstOptionValueCode>  |
      | firstOptionValueName   |                         |
      | secondOptionValue      | <secondOptionValueCode> |
      | secondOptionValueName  |                         |

    Examples:
      | queueName                             | storeTwoLanguages         | storeOneLanguage               | firstOptionValueCode    | secondOptionValueCode   | firstLanguage | secondLanguage |
      | Consumer.test.VirtualTopic.ep.catalog | SyndicationBilingualStore | SyndicationSingleLanguageStore | optionTestOption1value1 | optionTestOption1value2 | English       | French         |