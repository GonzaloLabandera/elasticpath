@cm @regression
Feature: Syndication flow for Attribute

  Background:
    Given I have catalog Syndication Two Stores Catalog
    And I have stores SyndicationBilingualStore, SyndicationSingleLanguageStore connected to Syndication Two Stores Catalog catalog
    When I sign in to CM as admin user
    And I go to Catalog Management
    And I select Attributes tab in a catalog editor for catalog Syndication Two Stores Catalog

  Scenario Outline: Create and then Delete Attribute, check ep.catalog JMS message, check created attribute projection via API
    And I am listening to <queueName> queue
    And I delete all messages from <queueName> queue
    And I add a new attribute with the following parameters
      | key        | <attributeKey>                      |
      | names      | <attributeNameEn>,<attributeNameFr> |
      | languages  | <languageEn>,<languageFr>           |
      | usage      | <usage>                             |
      | type       | <type>                              |
      | multiValue | <multivalue>                        |
      | required   | <required>                          |
    And I save my changes
    Then Newly created attribute is in the list
    When I go to Configuration
    And I go to Stores
    And I take all exist store codes
    When There are attribute projections for all exist stores
    When I read <queueName> message from queue
    Then Catalog event JMS message json for store <storeTwoLanguages> contains the following values
    # empty values will be replaced in the step with values from entities created in previous steps
      | key    | value               |
      | @class | CatalogEventType    |
      | name   | ATTRIBUTES_UPDATED  |
      | guid   | AGGREGATE           |
      | type   | attribute           |
      | store  | <storeTwoLanguages> |
      | codes  |                     |
    And Catalog event JMS message json for store <storeOneLanguage> contains the following values
    # empty values will be replaced in the step with values from entities created in previous steps
      | key    | value              |
      | @class | CatalogEventType   |
      | name   | ATTRIBUTES_UPDATED |
      | guid   | AGGREGATE          |
      | type   | attribute          |
      | store  | <storeOneLanguage> |
      | codes  |                    |
    When I retrieve latest version of created in CM attribute projection for store <storeTwoLanguages> via API
    Then Response status code is 200
    And Single attribute API response contains complete information for projection with 2 languages
    # empty values will be replaced in the step with values from entities created in previous steps
      | type                  | attribute           |
      | code                  |                     |
      | store                 | <storeTwoLanguages> |
      | deleted               | false               |
      | firstLanguageLocale   | en                  |
      | secondLanguageLocale  | fr                  |
      | firstLanguage         | <languageEn>        |
      | secondLanguage        | <languageFr>        |
      | displayNameFirstLang  | <attributeNameEn>   |
      | displayNameSecondLang | <attributeNameFr>   |
      | dataType              | ShortText           |
      | isMultiValue          | <multivalue>        |
    When I retrieve latest version of created in CM attribute projection for store <storeOneLanguage> via API
    Then Response status code is 200
    And Single attribute API response contains complete information for projection with 1 language
    # empty values will be replaced in the step with values from entities created in previous steps
      | type                   | attribute          |
      | code                   |                    |
      | store                  | <storeOneLanguage> |
      | deleted                | false              |
      | languageLocale         | fr                 |
      | excludedLanguageLocale | en                 |
      | language               | <languageFr>       |
      | displayName            | <attributeNameFr>  |
      | dataType               | ShortText          |
      | isMultiValue           | <multivalue>       |
    When I go to Catalog Management
    And I select Attributes tab in a catalog editor for catalog Syndication Two Stores Catalog
    And I delete all messages from <queueName> queue
    And I delete the newly created attribute by key
    And I save my changes
    And I read <queueName> message from queue
    Then Catalog event JMS message json for store <storeTwoLanguages> contains the following values
    # empty values will be replaced in the step with values from entities created in previous steps
      | key    | value               |
      | @class | CatalogEventType    |
      | name   | ATTRIBUTES_UPDATED  |
      | guid   | AGGREGATE           |
      | type   | attribute           |
      | store  | <storeTwoLanguages> |
      | codes  |                     |
    And Catalog event JMS message json for store <storeOneLanguage> contains the following values
    # empty values will be replaced in the step with values from entities created in previous steps
      | key    | value              |
      | @class | CatalogEventType   |
      | name   | ATTRIBUTES_UPDATED |
      | guid   | AGGREGATE          |
      | type   | attribute          |
      | store  | <storeOneLanguage> |
      | codes  |                    |
    When I retrieve latest version of created in CM attribute projection for store <storeTwoLanguages> via API
    Then Response status code is 200
    And Single attribute API response contains complete information for projection of deleted attribute
    # empty values will be replaced in the step with values from entities created in previous steps
      | type    | attribute           |
      | code    |                     |
      | store   | <storeTwoLanguages> |
      | deleted | true                |
    When I retrieve latest version of created in CM attribute projection for store <storeOneLanguage> via API
    Then Response status code is 200
    And Single attribute API response contains complete information for projection of deleted attribute
    # empty values will be replaced in the step with values from entities created in previous steps
      | type    | attribute          |
      | code    |                    |
      | store   | <storeOneLanguage> |
      | deleted | true               |

    Examples:
      | queueName                             | storeTwoLanguages         | storeOneLanguage               | usage   | type       | attributeNameEn | attributeNameFr | attributeKey | required | multivalue | languageEn | languageFr |
      | Consumer.test.VirtualTopic.ep.catalog | SyndicationBilingualStore | SyndicationSingleLanguageStore | Product | Short Text | attributeNameEn | attributeNameFr | attributeKey | true     | true       | English    | French     |

  Scenario Outline: Update Attribute, check ep.catalog JMS message, check updated attribute projection via API
    When I have attribute with key attributeTestAttribute1
    And I am listening to <queueName> queue
    And I delete all messages from <queueName> queue
    And I edit last 5 characters of attribute names with random characters for the following languages without saving
      | <languageEn> | <languageFr> |
    And I close Edit attribute dialog keeping changes
    And I save my changes
    When I read <queueName> message from queue
    Then Catalog event JMS message json for store <storeTwoLanguages> contains the following values
    # empty values will be replaced in the step with values from entities created in previous steps
      | key    | value               |
      | @class | CatalogEventType    |
      | name   | ATTRIBUTES_UPDATED  |
      | guid   | AGGREGATE           |
      | type   | attribute           |
      | store  | <storeTwoLanguages> |
      | codes  |                     |
    And Catalog event JMS message json for store <storeOneLanguage> contains the following values
    # empty values will be replaced in the step with values from entities created in previous steps
      | key    | value              |
      | @class | CatalogEventType   |
      | name   | ATTRIBUTES_UPDATED |
      | guid   | AGGREGATE          |
      | type   | attribute          |
      | store  | <storeOneLanguage> |
      | codes  |                    |
    When I retrieve latest version of created in CM attribute projection for store <storeTwoLanguages> via API
    Then Response status code is 200
    And Single attribute API response contains complete information for projection with 2 languages
    # empty values will be replaced in the step with values from entities created in previous steps
      | type                  | attribute           |
      | code                  |                     |
      | store                 | <storeTwoLanguages> |
      | deleted               | false               |
      | firstLanguageLocale   | en                  |
      | secondLanguageLocale  | fr                  |
      | firstLanguage         | <languageEn>        |
      | secondLanguage        | <languageFr>        |
      | displayNameFirstLang  |                     |
      | displayNameSecondLang |                     |
      | dataType              | <type>              |
      | isMultiValue          | <isMultiValue>      |
    When I retrieve latest version of created in CM attribute projection for store <storeOneLanguage> via API
    Then Response status code is 200
    And Single attribute API response contains complete information for projection with 1 language
    # empty values will be replaced in the step with values from entities created in previous steps
      | type                   | attribute          |
      | code                   |                    |
      | store                  | <storeOneLanguage> |
      | deleted                | false              |
      | languageLocale         | fr                 |
      | excludedLanguageLocale | en                 |
      | language               | <languageFr>       |
      | displayName            |                    |
      | dataType               | <type>             |
      | isMultiValue           | <isMultiValue>     |

    Examples:
      | queueName                             | storeTwoLanguages         | storeOneLanguage               | languageEn | languageFr | isMultiValue | type      |
      | Consumer.test.VirtualTopic.ep.catalog | SyndicationBilingualStore | SyndicationSingleLanguageStore | English    | French     | true         | ShortText |

