@cm @regression
Feature: Syndication Сategory flow tests

  Background:
    Given I have catalog Syndication Two Stores Catalog
    And I have virtual catalog SyndicationVirtualCatalog
    And I have stores SyndicationBilingualStore, SyndicationSingleLanguageStore connected to Syndication Two Stores Catalog catalog
    And I have store SyndicationVirtualCatalogStore connected to SyndicationVirtualCatalog virtual catalog
    And I have attribute with attribute key categoryTestAttr1 and the following names
      | English | categoryTestAttr1En |
      | French  | categoryTestAttr1Fr |
    And I have attribute with attribute key categoryTestAttr2 and the following names
      | English | categoryTestAttr2En |
      | French  | categoryTestAttr2Fr |
    And I have category type categoryTestCatType with the following attributes
      | categoryTestAttr1 | categoryTestAttr2 |
    And I have top level category categoryTestTop1
      | English | categoryTestTop1En |
    And I have subcategory categoryTestSub1Top1 with the following parameters
      | defaultLanguage | English                |
      | defaultName     | categoryTestSub1Top1En |
      | parentName      | categoryTestTop1En     |
    And I have subcategory categoryTestSub2Sub1Top1 with the following parameters
      | defaultLanguage | English                    |
      | defaultName     | categoryTestSub2Sub1Top1En |
      | parentName      | categoryTestSub1Top1En     |
    And I have subcategory categoryTestSub2Top1 with the following parameters
      | defaultLanguage | English                |
      | defaultName     | categoryTestSub2Top1En |
      | parentName      | categoryTestTop1En     |
    And I have subcategory categoryTestSub1Sub2Top1 with the following parameters
      | defaultLanguage | English                    |
      | defaultName     | categoryTestSub1Sub2Top1En |
      | parentName      | categoryTestSub2Top1En     |
    And I have subcategory categoryTestSub2Sub2Top1 with the following parameters
      | defaultLanguage | English                    |
      | defaultName     | categoryTestSub2Sub2Top1En |
      | parentName      | categoryTestSub2Top1En     |
    And I have top level category categoryTestTop2
      | English | categoryTestTop2En |
    And I have subcategory categoryTestSub1Top2 with the following parameters
      | defaultLanguage | English                |
      | defaultName     | categoryTestSub1Top2En |
      | parentName      | categoryTestTop2En     |
    And I have subcategory categoryTestSub1Sub1Top2 with the following parameters
      | defaultLanguage | English                    |
      | defaultName     | categoryTestSub1Sub1Top2En |
      | parentName      | categoryTestSub1Top2En     |
    When I sign in to CM as admin user
    And I go to Catalog Management

  Scenario Outline: Create and then Delete Category, check ep.catalog JMS message, check created option projection via API
    And I am listening to <queueName> queue
    And I delete all messages from <queueName> queue
    And I create subcategory for category <sub1Top1category> in catalog <masterCatalog> with following values
      | categoryName           | storeVisible | enableDateTime              | disableDateTime              | attrLongTextName | attrLongTextValue | attrShortTextName | attrShortTextValue |
      | <sub1sub1Top1category> | true         | <newCategoryEnableDateTime> | <newCategoryDisableDateTime> | <attr2Key>       | attr2Value        | <attr1Key>        | attr1Value         |
    When I read <queueName> message from queue
    Then Catalog event JMS message json for category <sub1sub1Top1category> contains the following values
    # empty values will be replaced in the step with values from entities created in previous steps
      | key    | value              |
      | @class | CatalogEventType   |
      | name   | CATEGORIES_UPDATED |
      | guid   | AGGREGATE          |
      | type   | category           |
      | store  | <singleLangStore>  |
      | codes  |                    |
    And Catalog event JMS message json for category <sub1sub1Top1category> contains the following values
    # empty values will be replaced in the step with values from entities created in previous steps
      | key    | value              |
      | @class | CatalogEventType   |
      | name   | CATEGORIES_UPDATED |
      | guid   | AGGREGATE          |
      | type   | category           |
      | store  | <bilingualStore>   |
      | codes  |                    |
    When I retrieve latest version of created in CM category projection for store <bilingualStore> and category name <sub1sub1Top1category> via API
    Then Response status code is 200
    And Single category API response contains complete information for projection with 2 languages and category code <sub1sub1Top1category>
           # empty values will be replaced in the step with values from entities created in previous steps
      | type                                | category                  |
      | code                                |                           |
      | store                               | <bilingualStore>          |
      | deleted                             | false                     |
      | name                                | CATEGORY_TYPE             |
      | value                               |                           |
      | children                            |                           |
      | enableDateTime                      | <expectedEnableDateTime>  |
      | disableDateTime                     | <expectedDisableDateTime> |
      | parent                              |                           |
      | firstLanguageLocale                 | en                        |
      | secondLanguageLocale                | fr                        |
      | firstLanguage                       | English                   |
      | secondLanguage                      | French                    |
      | displayNameFirstLang                |                           |
      | displayNameSecondLang               |                           |
      | firstLangFirstDetailDisplayName     |                           |
      | firstLangFirstDetailName            |                           |
      | firstLangFirstDetailDisplayValues   |                           |
      | firstLangFirstDetailValues          |                           |
      | firstLangSecondDetailDisplayName    |                           |
      | firstLangSecondDetailName           |                           |
      | firstLangSecondDetailDisplayValues  |                           |
      | firstLangSecondDetailValues         |                           |
      | secondLangFirstDetailDisplayName    |                           |
      | secondLangFirstDetailName           |                           |
      | secondLangFirstDetailDisplayValues  |                           |
      | secondLangFirstDetailValues         |                           |
      | secondLangSecondDetailDisplayName   |                           |
      | secondLangSecondDetailName          |                           |
      | secondLangSecondDetailDisplayValues |                           |
      | secondLangSecondDetailValues        |                           |
    When I retrieve latest version of created in CM category projection for store <singleLangStore> and category name <sub1sub1Top1category> via API
    Then Response status code is 200
    And Single category API response contains complete information for projection with 1 language and category code <sub1sub1Top1category>
           # empty values will be replaced in the step with values from entities created in previous steps
      | type                      | category                  |
      | code                      |                           |
      | store                     | <singleLangStore>         |
      | modifiedDateTime          |                           |
      | deleted                   | false                     |
      | name                      | CATEGORY_TYPE             |
      | value                     |                           |
      | children                  |                           |
      | enableDateTime            | <expectedEnableDateTime>  |
      | disableDateTime           | <expectedDisableDateTime> |
      | parent                    |                           |
      | languageLocale            | fr                        |
      | excludedLanguageLocale    | en                        |
      | language                  | French                    |
      | displayName               |                           |
      | FirstDetailDisplayName    |                           |
      | FirstDetailName           |                           |
      | FirstDetailDisplayValues  |                           |
      | FirstDetailValues         |                           |
      | SecondDetailDisplayName   |                           |
      | SecondDetailName          |                           |
      | SecondDetailDisplayValues |                           |
      | SecondDetailValues        |                           |
    And I delete all messages from <queueName> queue
    When I add new linked category to existing virtual catalog <virtualCatalog> with following data
      | <masterCatalog>        |
      | <sub1sub1Top1category> |
    When I read <queueName> message from queue
    Then Catalog event JMS message json for category <sub1sub1Top1category> contains the following values
    # empty values will be replaced in the step with values from entities created in previous steps
      | key    | value              |
      | @class | CatalogEventType   |
      | name   | CATEGORIES_UPDATED |
      | guid   | AGGREGATE          |
      | type   | category           |
      | store  | <virtualCatStore>  |
      | codes  |                    |
    When I retrieve latest version of created in CM category projection for store <virtualCatStore> and category name <sub1sub1Top1category> via API
    Then Response status code is 200
    And Single category API response contains complete information for projection with 1 language and linked category code <sub1sub1Top1category>
           # empty values will be replaced in the step with values from entities created in previous steps
      | type                                | category                     |
      | code                                |                              |
      | store                               | <virtualCatStore>            |
      | deleted                             | false                        |
      | name                                | CATEGORY_TYPE                |
      | value                               |                              |
      | children                            |                              |
      | enableDateTime                      | <newCategoryEnableDateTime>  |
      | disableDateTime                     | <newCategoryDisableDateTime> |
      | parent                              |                              |
      | languageLocale                      | en                           |
      | excludedLanguageLocale              | fr                           |
      | language                            | English                      |
      | displayNameFirstLang                |                              |
      | displayNameSecondLang               |                              |
      | firstLangFirstDetailDisplayName     |                              |
      | firstLangFirstDetailName            |                              |
      | firstLangFirstDetailDisplayValues   |                              |
      | firstLangFirstDetailValues          |                              |
      | firstLangSecondDetailDisplayName    |                              |
      | firstLangSecondDetailName           |                              |
      | firstLangSecondDetailDisplayValues  |                              |
      | firstLangSecondDetailValues         |                              |
      | secondLangFirstDetailDisplayName    |                              |
      | secondLangFirstDetailName           |                              |
      | secondLangFirstDetailDisplayValues  |                              |
      | secondLangFirstDetailValues         |                              |
      | secondLangSecondDetailDisplayName   |                              |
      | secondLangSecondDetailName          |                              |
      | secondLangSecondDetailDisplayValues |                              |
      | secondLangSecondDetailValues        |                              |
    When I retrieve latest version of created in CM category projection for store <bilingualStore> and category name <sub1Top1category> via API
    Then Response status code is 200
    And Single category API response contains complete information for projection with 2 languages and category code <sub1Top1category>
           # empty values will be replaced in the step with values from entities created in previous steps
      | type                                | category                  |
      | code                                |                           |
      | store                               | <bilingualStore>          |
      | deleted                             | false                     |
      | name                                | CATEGORY_TYPE             |
      | value                               | categoryTestCatType       |
      | children                            |                           |
      | enableDateTime                      | <expectedEnableDateTime>  |
      | disableDateTime                     | <expectedDisableDateTime> |
      | parent                              |                           |
      | firstLanguageLocale                 | en                        |
      | secondLanguageLocale                | fr                        |
      | firstLanguage                       | English                   |
      | secondLanguage                      | French                    |
      | displayNameFirstLang                | <sub1Top1category>        |
      | displayNameSecondLang               | <sub1Top1category>        |
      | firstLangFirstDetailDisplayName     | categoryTestAttr1En       |
      | firstLangFirstDetailName            | <attr1Key>                |
      | firstLangFirstDetailDisplayValues   | attr1Value1               |
      | firstLangFirstDetailValues          | attr1Value1               |
      | firstLangSecondDetailDisplayName    | categoryTestAttr2En       |
      | firstLangSecondDetailName           | <attr2Key>                |
      | firstLangSecondDetailDisplayValues  | attr2Value2               |
      | firstLangSecondDetailValues         | attr2Value2               |
      | secondLangFirstDetailDisplayName    | categoryTestAttr1Fr       |
      | secondLangFirstDetailName           | <attr1Key>                |
      | secondLangFirstDetailDisplayValues  | attr1Value1               |
      | secondLangFirstDetailValues         | attr1Value1               |
      | secondLangSecondDetailDisplayName   | categoryTestAttr2Fr       |
      | secondLangSecondDetailName          | <attr2Key>                |
      | secondLangSecondDetailDisplayValues | attr2Value2               |
      | secondLangSecondDetailValues        | attr2Value2               |
    When I retrieve latest version of created in CM category projection for store <singleLangStore> and category name <sub1Top1category> via API
    Then Response status code is 200
    And Single category API response contains complete information for projection with 1 language and category code <sub1Top1category>
           # empty values will be replaced in the step with values from entities created in previous steps
      | type                      | category                  |
      | code                      |                           |
      | store                     | <singleLangStore>         |
      | modifiedDateTime          |                           |
      | deleted                   | false                     |
      | name                      | CATEGORY_TYPE             |
      | value                     | categoryTestCatType       |
      | children                  |                           |
      | enableDateTime            | <expectedEnableDateTime>  |
      | disableDateTime           | <expectedDisableDateTime> |
      | parent                    |                           |
      | languageLocale            | fr                        |
      | excludedLanguageLocale    | en                        |
      | language                  | French                    |
      | displayName               | categoryTestSub1Top1En    |
      | FirstDetailDisplayName    | categoryTestAttr1Fr       |
      | FirstDetailName           | <attr1Key>                |
      | FirstDetailDisplayValues  | attr1Value1               |
      | FirstDetailValues         | attr1Value1               |
      | SecondDetailDisplayName   | categoryTestAttr2Fr       |
      | SecondDetailName          | <attr2Key>                |
      | SecondDetailDisplayValues | attr2Value2               |
      | SecondDetailValues        | attr2Value2               |
    And I delete all messages from <queueName> queue
    And I remove linked category with name <sub1sub1Top1category> from catalog <virtualCatalog>
    When I read <queueName> message from queue
    Then Catalog event JMS message json for category <sub1sub1Top1category> contains the following values
    # empty values will be replaced in the step with values from entities created in previous steps
      | key    | value              |
      | @class | CatalogEventType   |
      | name   | CATEGORIES_UPDATED |
      | guid   | AGGREGATE          |
      | type   | category           |
      | store  | <virtualCatStore>  |
      | codes  |                    |
    When I retrieve latest version of created in CM category projection for store <virtualCatStore> and category name <sub1sub1Top1category> via API
    Then Response status code is 200
    And Single category API response contains complete information for tombstone projection with category name <sub1sub1Top1category>
      | type             | category          |
      | code             |                   |
      | store            | <virtualCatStore> |
      | modifiedDateTime |                   |
      | deleted          | true              |
    When I delete all messages from <queueName> queue
    And I open category <sub1sub1Top1category> in editor for catalog <masterCatalog>
    And I delete specified category <sub1sub1Top1category>
    When I read <queueName> message from queue
    Then Catalog event JMS message json for category <sub1sub1Top1category> contains the following values
    # empty values will be replaced in the step with values from entities created in previous steps
      | key    | value              |
      | @class | CatalogEventType   |
      | name   | CATEGORIES_UPDATED |
      | guid   | AGGREGATE          |
      | type   | category           |
      | store  | <singleLangStore>  |
      | codes  |                    |
    And Catalog event JMS message json for category <sub1sub1Top1category> contains the following values
    # empty values will be replaced in the step with values from entities created in previous steps
      | key    | value              |
      | @class | CatalogEventType   |
      | name   | CATEGORIES_UPDATED |
      | guid   | AGGREGATE          |
      | type   | category           |
      | store  | <bilingualStore>   |
      | codes  |                    |
    When I retrieve latest version of created in CM category projection for store <bilingualStore> and category name <sub1sub1Top1category> via API
    Then Response status code is 200
    And Single category API response contains complete information for tombstone projection with category name <sub1sub1Top1category>
      | type             | category         |
      | code             |                  |
      | store            | <bilingualStore> |
      | modifiedDateTime |                  |
      | deleted          | true             |
    When I retrieve latest version of created in CM category projection for store <singleLangStore> and category name <sub1sub1Top1category> via API
    Then Response status code is 200
    And Single category API response contains complete information for tombstone projection with category name <sub1sub1Top1category>
      | type             | category          |
      | code             |                   |
      | store            | <singleLangStore> |
      | modifiedDateTime |                   |
      | deleted          | true              |

    Examples:
      | queueName                             | newCategoryEnableDateTime | newCategoryDisableDateTime | expectedEnableDateTime | expectedDisableDateTime | sub1Top1category       | sub1sub1Top1category       | attr1Key          | attr2Key          | bilingualStore            | singleLangStore                | virtualCatStore                | masterCatalog                  | virtualCatalog              |
      | Consumer.test.VirtualTopic.ep.catalog | Aug 2, 2037 4:10 PM       | Aug 15, 2037 4:10 PM       | Aug 5, 2037 4:10 PM    | Aug 10, 2037 4:10 PM    | categoryTestSub1Top1En | categoryTestSub1Sub1Top1En | categoryTestAttr1 | categoryTestAttr2 | SyndicationBilingualStore | SyndicationSingleLanguageStore | SyndicationVirtualCatalogStore | Syndication Two Stores Catalog | Syndication Virtual Catalog |

  Scenario Outline: Update Category, check ep.catalog JMS message, check created option projection via API
    When I am listening to <queueName> queue
    And I delete all messages from <queueName> queue
    And I open category <sub1Sub2Top1Category> in editor for catalog Syndication Two Stores Catalog
    And I edit last 5 characters of category <sub1Sub2Top1Category> names with random characters for the following languages without saving
      | English | French |
    And I edit category <sub1Sub2Top1Category> enable and disable dates without saving
      | enableDateTime  | Aug 2, 2037 4:10 PM  |
      | disableDateTime | Aug 15, 2037 4:10 PM |
    And I save my changes
    And I read <queueName> message from queue
    Then Catalog event JMS message json for category <sub1Sub2Top1Category> contains the following values
    # empty values will be replaced in the step with values from entities created in previous steps
      | key    | value                          |
      | @class | CatalogEventType               |
      | name   | CATEGORIES_UPDATED             |
      | guid   | AGGREGATE                      |
      | type   | category                       |
      | store  | SyndicationSingleLanguageStore |
      | codes  |                                |
    And Catalog event JMS message json for category <sub1Sub2Top1Category> contains the following values
    # empty values will be replaced in the step with values from entities created in previous steps
      | key    | value                     |
      | @class | CatalogEventType          |
      | name   | CATEGORIES_UPDATED        |
      | guid   | AGGREGATE                 |
      | type   | category                  |
      | store  | SyndicationBilingualStore |
      | codes  |                           |
    When I retrieve latest version of created in CM category projection for store SyndicationBilingualStore and category name <sub1Sub2Top1Category> via API
    Then Response status code is 200
    And Single category API response contains complete information for projection with 2 languages and category code <sub1Sub2Top1Category>
           # empty values will be replaced in the step with values from entities created in previous steps
      | type                                | category                  |
      | code                                |                           |
      | store                               | SyndicationBilingualStore |
      | deleted                             | false                     |
      | name                                | CATEGORY_TYPE             |
      | value                               | <categoryType>            |
      | children                            |                           |
      | enableDateTime                      | Aug 5, 2037 10:10 AM      |
      | disableDateTime                     | Aug 10, 2037 10:10 AM     |
      | parent                              |                           |
      | firstLanguageLocale                 | en                        |
      | secondLanguageLocale                | fr                        |
      | firstLanguage                       | English                   |
      | secondLanguage                      | French                    |
      | displayNameFirstLang                |                           |
      | displayNameSecondLang               |                           |
      | firstLangFirstDetailDisplayName     | <flfdDisplayName>         |
      | firstLangFirstDetailName            | <flfdName>                |
      | firstLangFirstDetailDisplayValues   | <flfdDisplayValues>       |
      | firstLangFirstDetailValues          | <flfdValues>              |
      | firstLangSecondDetailDisplayName    | <flsdDisplayName>         |
      | firstLangSecondDetailName           | <flsdName>                |
      | firstLangSecondDetailDisplayValues  | <flsdDisplayValues>       |
      | firstLangSecondDetailValues         | <flsdValues>              |
      | secondLangFirstDetailDisplayName    | <slfdDisplayName>         |
      | secondLangFirstDetailName           | <slfdName>                |
      | secondLangFirstDetailDisplayValues  | <slfdDisplayValues>       |
      | secondLangFirstDetailValues         | <slfdValues>              |
      | secondLangSecondDetailDisplayName   | <slsdDisplayName>         |
      | secondLangSecondDetailName          | <slsdName>                |
      | secondLangSecondDetailDisplayValues | <slsdDisplayValues>       |
      | secondLangSecondDetailValues        | <slsdValues>              |
    When I retrieve latest version of created in CM category projection for store SyndicationSingleLanguageStore and category name <sub1Sub2Top1Category> via API
    Then Response status code is 200
    And Single category API response contains complete information for projection with 1 language and category code <sub1Sub2Top1Category>
           # empty values will be replaced in the step with values from entities created in previous steps
      | type                      | category                       |
      | code                      |                                |
      | store                     | SyndicationSingleLanguageStore |
      | modifiedDateTime          |                                |
      | deleted                   | false                          |
      | name                      | CATEGORY_TYPE                  |
      | value                     | <categoryType>                 |
      | children                  |                                |
      | enableDateTime            | Aug 5, 2037 10:10 AM           |
      | disableDateTime           | Aug 10, 2037 10:10 AM          |
      | languageLocale            | fr                             |
      | excludedLanguageLocale    | en                             |
      | language                  | French                         |
      | displayName               |                                |
      | FirstDetailDisplayName    | <slfdDisplayName>              |
      | FirstDetailName           | <slfdName>                     |
      | FirstDetailDisplayValues  | <slfdDisplayValues>            |
      | FirstDetailValues         | <slfdValues>                   |
      | SecondDetailDisplayName   | <slsdDisplayName>              |
      | SecondDetailName          | <slsdName>                     |
      | SecondDetailDisplayValues | <slsdDisplayValues>            |
      | SecondDetailValues        | <slsdValues>                   |
    And I am listening to <queueName> queue
    And I delete all messages from <queueName> queue
    And I expand Syndication Two Stores Catalog catalog
    And I open category <sub2Sub2Top1Category> in editor for catalog Syndication Two Stores Catalog
    And I edit last 5 characters of category <sub2Sub2Top1Category> names with random characters for the following languages without saving
      | English | French |
    And I edit category <sub2Sub2Top1Category> enable and disable dates without saving
      | enableDateTime  | Aug 6, 2037 4:10 PM |
      | disableDateTime | Aug 8, 2037 4:10 PM |
    And I save my changes
    When I read <queueName> message from queue
    Then Catalog event JMS message json for category <sub2Sub2Top1Category> contains the following values
    # empty values will be replaced in the step with values from entities created in previous steps
      | key    | value                          |
      | @class | CatalogEventType               |
      | name   | CATEGORIES_UPDATED             |
      | guid   | AGGREGATE                      |
      | type   | category                       |
      | store  | SyndicationSingleLanguageStore |
      | codes  |                                |
    And Catalog event JMS message json for category <sub2Sub2Top1Category> contains the following values
    # empty values will be replaced in the step with values from entities created in previous steps
      | key    | value                     |
      | @class | CatalogEventType          |
      | name   | CATEGORIES_UPDATED        |
      | guid   | AGGREGATE                 |
      | type   | category                  |
      | store  | SyndicationBilingualStore |
      | codes  |                           |
    When I retrieve latest version of created in CM category projection for store SyndicationBilingualStore and category name <sub2Sub2Top1Category> via API
    Then Response status code is 200
    And Single category API response contains complete information for projection with 2 languages and category code <sub2Sub2Top1Category>
           # empty values will be replaced in the step with values from entities created in previous steps
      | type                                | category                  |
      | code                                |                           |
      | store                               | SyndicationBilingualStore |
      | deleted                             | false                     |
      | name                                | CATEGORY_TYPE             |
      | value                               | <categoryType>            |
      | children                            |                           |
      | enableDateTime                      | Aug 6, 2037 4:10 PM       |
      | disableDateTime                     | Aug 8, 2037 4:10 PM       |
      | parent                              |                           |
      | firstLanguageLocale                 | en                        |
      | secondLanguageLocale                | fr                        |
      | firstLanguage                       | English                   |
      | secondLanguage                      | French                    |
      | displayNameFirstLang                |                           |
      | displayNameSecondLang               |                           |
      | firstLangFirstDetailDisplayName     | <flfdDisplayName>         |
      | firstLangFirstDetailName            | <flfdName>                |
      | firstLangFirstDetailDisplayValues   | <flfdDisplayValues>       |
      | firstLangFirstDetailValues          | <flfdValues>              |
      | firstLangSecondDetailDisplayName    | <flsdDisplayName>         |
      | firstLangSecondDetailName           | <flsdName>                |
      | firstLangSecondDetailDisplayValues  | <flsdDisplayValues>       |
      | firstLangSecondDetailValues         | <flsdValues>              |
      | secondLangFirstDetailDisplayName    | <slfdDisplayName>         |
      | secondLangFirstDetailName           | <slfdName>                |
      | secondLangFirstDetailDisplayValues  | <slfdDisplayValues>       |
      | secondLangFirstDetailValues         | <slfdValues>              |
      | secondLangSecondDetailDisplayName   | <slsdDisplayName>         |
      | secondLangSecondDetailName          | <slsdName>                |
      | secondLangSecondDetailDisplayValues | <slsdDisplayValues>       |
      | secondLangSecondDetailValues        | <slsdValues>              |
    When I retrieve latest version of created in CM category projection for store SyndicationSingleLanguageStore and category name <sub2Sub2Top1Category> via API
    Then Response status code is 200
    And Single category API response contains complete information for projection with 1 language and category code <sub2Sub2Top1Category>
           # empty values will be replaced in the step with values from entities created in previous steps
      | type                      | category                       |
      | code                      |                                |
      | store                     | SyndicationSingleLanguageStore |
      | modifiedDateTime          |                                |
      | deleted                   | false                          |
      | name                      | CATEGORY_TYPE                  |
      | value                     | <categoryType>                 |
      | children                  |                                |
      | enableDateTime            | Aug 6, 2037 4:10 PM            |
      | disableDateTime           | Aug 8, 2037 4:10 PM            |
      | languageLocale            | fr                             |
      | excludedLanguageLocale    | en                             |
      | language                  | French                         |
      | displayName               |                                |
      | FirstDetailDisplayName    | <slfdDisplayName>              |
      | FirstDetailName           | <slfdName>                     |
      | FirstDetailDisplayValues  | <slfdDisplayValues>            |
      | FirstDetailValues         | <slfdValues>                   |
      | SecondDetailDisplayName   | <slsdDisplayName>              |
      | SecondDetailName          | <slsdName>                     |
      | SecondDetailDisplayValues | <slsdDisplayValues>            |
      | SecondDetailValues        | <slsdValues>                   |
    When I am listening to <queueName> queue
    And I delete all messages from <queueName> queue
    And I expand Syndication Two Stores Catalog catalog
    And I open category <sub1Sub2Top1Category> in editor for catalog Syndication Two Stores Catalog
    And I save the ordered list of 2 children for expanded category <sub2Top1Category>
    And I move the last child of category <sub2Top1Category> one position up in catalog
    When I read <queueName> message from queue
    Then Catalog event JMS message json for category <sub2Top1Category> contains the following values
    # empty values will be replaced in the step with values from entities created in previous steps
      | key    | value                          |
      | @class | CatalogEventType               |
      | name   | CATEGORIES_UPDATED             |
      | guid   | AGGREGATE                      |
      | type   | category                       |
      | store  | SyndicationSingleLanguageStore |
      | codes  |                                |
    And Catalog event JMS message json for category <sub2Top1Category> contains the following values
    # empty values will be replaced in the step with values from entities created in previous steps
      | key    | value                     |
      | @class | CatalogEventType          |
      | name   | CATEGORIES_UPDATED        |
      | guid   | AGGREGATE                 |
      | type   | category                  |
      | store  | SyndicationBilingualStore |
      | codes  |                           |
    When I retrieve latest version of created in CM category projection for store SyndicationBilingualStore and category name <sub2Top1Category> via API
    Then Response status code is 200
    And Single category API response contains complete information for projection with 2 languages and category code <sub2Top1Category>
           # empty values will be replaced in the step with values from entities created in previous steps
      | type                                | category                  |
      | code                                |                           |
      | store                               | SyndicationBilingualStore |
      | deleted                             | false                     |
      | name                                | CATEGORY_TYPE             |
      | value                               | <categoryType>            |
      | children                            |                           |
      | enableDateTime                      | Aug 5, 2037 10:10 AM      |
      | disableDateTime                     | Aug 10, 2037 10:10 AM     |
      | parent                              |                           |
      | firstLanguageLocale                 | en                        |
      | secondLanguageLocale                | fr                        |
      | firstLanguage                       | English                   |
      | secondLanguage                      | French                    |
      | displayNameFirstLang                | categoryTestSub2Top1En    |
      | displayNameSecondLang               | categoryTestSub2Top1En    |
      | firstLangFirstDetailDisplayName     | <flfdDisplayName>         |
      | firstLangFirstDetailName            | <flfdName>                |
      | firstLangFirstDetailDisplayValues   | <flfdDisplayValues>       |
      | firstLangFirstDetailValues          | <flfdValues>              |
      | firstLangSecondDetailDisplayName    | <flsdDisplayName>         |
      | firstLangSecondDetailName           | <flsdName>                |
      | firstLangSecondDetailDisplayValues  | <flsdDisplayValues>       |
      | firstLangSecondDetailValues         | <flsdValues>              |
      | secondLangFirstDetailDisplayName    | <slfdDisplayName>         |
      | secondLangFirstDetailName           | <slfdName>                |
      | secondLangFirstDetailDisplayValues  | <slfdDisplayValues>       |
      | secondLangFirstDetailValues         | <slfdValues>              |
      | secondLangSecondDetailDisplayName   | <slsdDisplayName>         |
      | secondLangSecondDetailName          | <slsdName>                |
      | secondLangSecondDetailDisplayValues | <slsdDisplayValues>       |
      | secondLangSecondDetailValues        | <slsdValues>              |
    And Single category API response contains correct children information for category <sub2Top1Category>
    When I retrieve latest version of created in CM category projection for store SyndicationSingleLanguageStore and category name <sub2Top1Category> via API
    Then Response status code is 200
    And Single category API response contains complete information for projection with 1 language and category code <sub2Top1Category>
           # empty values will be replaced in the step with values from entities created in previous steps
      | type                      | category                       |
      | code                      |                                |
      | store                     | SyndicationSingleLanguageStore |
      | modifiedDateTime          |                                |
      | deleted                   | false                          |
      | name                      | CATEGORY_TYPE                  |
      | value                     | <categoryType>                 |
      | children                  |                                |
      | enableDateTime            | Aug 5, 2037 10:10 AM           |
      | disableDateTime           | Aug 10, 2037 10:10 AM          |
      | languageLocale            | fr                             |
      | excludedLanguageLocale    | en                             |
      | language                  | French                         |
      | displayName               | categoryTestSub2Top1En         |
      | FirstDetailDisplayName    | <slfdDisplayName>              |
      | FirstDetailName           | <slfdName>                     |
      | FirstDetailDisplayValues  | <slfdDisplayValues>            |
      | FirstDetailValues         | <slfdValues>                   |
      | SecondDetailDisplayName   | <slsdDisplayName>              |
      | SecondDetailName          | <slsdName>                     |
      | SecondDetailDisplayValues | <slsdDisplayValues>            |
      | SecondDetailValues        | <slsdValues>                   |

    Examples:
      | queueName                             | categoryType        | sub2Top1Category       | sub1Sub2Top1Category       | sub2Sub2Top1Category       | flfdDisplayName     | flfdName          | flfdDisplayValues | flfdValues  | flsdDisplayName     | flsdName          | flsdDisplayValues | flsdValues  | slfdDisplayName     | slfdName          | slfdDisplayValues | slfdValues  | slsdDisplayName     | slsdName          | slsdDisplayValues | slsdValues  |
      | Consumer.test.VirtualTopic.ep.catalog | categoryTestCatType | categoryTestSub2Top1En | categoryTestSub1Sub2Top1En | categoryTestSub2Sub2Top1En | categoryTestAttr1En | categoryTestAttr1 | attr1value1       | attr1value1 | categoryTestAttr2En | categoryTestAttr2 | attr2value1       | attr2value1 | categoryTestAttr1Fr | categoryTestAttr1 | attr1value1       | attr1value1 | categoryTestAttr2Fr | categoryTestAttr2 | attr2value1       | attr2value1 |

  Scenario Outline: Tombstone Category, check ep.catalog JMS message, check created option projection via API
    And I am listening to <queueName> queue
    And I delete all messages from <queueName> queue
    And I expand catalog with catalog name Syndication Two Stores Catalog
    And I open category <top2Category> in editor for catalog Syndication Two Stores Catalog
    And I make sure opened category <top2Category> is visible for its store
    And I make sure category <top2Category> has empty disable date
    And I edit the store visible to false for category <top2Category>
    When I read <queueName> message from queue
    Then Catalog event JMS message json for category <top2Category> contains the following values
    # empty values will be replaced in the step with values from entities created in previous steps
      | key    | value                          |
      | @class | CatalogEventType               |
      | name   | CATEGORIES_UPDATED             |
      | guid   | AGGREGATE                      |
      | type   | category                       |
      | store  | SyndicationSingleLanguageStore |
      | codes  |                                |
    And Catalog event JMS message json for category <top2Category> contains the following values
    # empty values will be replaced in the step with values from entities created in previous steps
      | key    | value                     |
      | @class | CatalogEventType          |
      | name   | CATEGORIES_UPDATED        |
      | guid   | AGGREGATE                 |
      | type   | category                  |
      | store  | SyndicationBilingualStore |
      | codes  |                           |
    When I retrieve latest version of created in CM category projection for store SyndicationBilingualStore and category name <top2Category> via API
    Then Response status code is 200
    And Single category API response contains complete information for tombstone projection with category name <top2Category>
      | type             | category                  |
      | code             |                           |
      | store            | SyndicationBilingualStore |
      | modifiedDateTime |                           |
      | deleted          | true                      |
    When I retrieve latest version of created in CM category projection for store SyndicationSingleLanguageStore and category name <top2Category> via API
    Then Response status code is 200
    And Single category API response contains complete information for tombstone projection with category name <top2Category>
      | type             | category                       |
      | code             |                                |
      | store            | SyndicationSingleLanguageStore |
      | modifiedDateTime |                                |
      | deleted          | true                           |
    When I retrieve latest version of created in CM category projection for store SyndicationBilingualStore and category name <sub1Top2Category> via API
    Then Response status code is 200
    And Single category API response contains complete information for tombstone projection with category name <sub1Top2Category>
      | type             | category                  |
      | code             |                           |
      | store            | SyndicationBilingualStore |
      | modifiedDateTime |                           |
      | deleted          | true                      |
    When I retrieve latest version of created in CM category projection for store SyndicationSingleLanguageStore and category name <sub1Top2Category> via API
    Then Response status code is 200
    And Single category API response contains complete information for tombstone projection with category name <sub1Top2Category>
      | type             | category                       |
      | code             |                                |
      | store            | SyndicationSingleLanguageStore |
      | modifiedDateTime |                                |
      | deleted          | true                           |
    When I retrieve latest version of created in CM category projection for store SyndicationBilingualStore and category name <sub1Sub1Top2category> via API
    Then Response status code is 200
    And Single category API response contains complete information for tombstone projection with category name <sub1Sub1Top2category>
      | type             | category                  |
      | code             |                           |
      | store            | SyndicationBilingualStore |
      | modifiedDateTime |                           |
      | deleted          | true                      |
    When I retrieve latest version of created in CM category projection for store SyndicationSingleLanguageStore and category name <sub1Sub1Top2category> via API
    Then Response status code is 200
    And Single category API response contains complete information for tombstone projection with category name <sub1Sub1Top2category>
      | type             | category                       |
      | code             |                                |
      | store            | SyndicationSingleLanguageStore |
      | modifiedDateTime |                                |
      | deleted          | true                           |
    And I am listening to <queueName> queue
    And I delete all messages from <queueName> queue
    And I open category <top2Category> in editor for catalog Syndication Two Stores Catalog
    And I edit the store visible to true for category <top2Category>
    When I read <queueName> message from queue
    Then Catalog event JMS message json for category <top2Category> contains the following values
    # empty values will be replaced in the step with values from entities created in previous steps
      | key    | value                          |
      | @class | CatalogEventType               |
      | name   | CATEGORIES_UPDATED             |
      | guid   | AGGREGATE                      |
      | type   | category                       |
      | store  | SyndicationSingleLanguageStore |
      | codes  |                                |
    And Catalog event JMS message json for category <top2Category> contains the following values
    # empty values will be replaced in the step with values from entities created in previous steps
      | key    | value                     |
      | @class | CatalogEventType          |
      | name   | CATEGORIES_UPDATED        |
      | guid   | AGGREGATE                 |
      | type   | category                  |
      | store  | SyndicationBilingualStore |
      | codes  |                           |
    When I retrieve latest version of created in CM category projection for store SyndicationBilingualStore and category name <top2Category> via API
    Then Response status code is 200
    And Single category API response contains complete information for projection with 2 languages and category code <top2Category>
        # empty values will be replaced in the step with values from entities created in previous steps
      | type                                | category                     |
      | code                                |                              |
      | store                               | SyndicationBilingualStore    |
      | deleted                             | false                        |
      | name                                | CATEGORY_TYPE                |
      | value                               | <categoryType>               |
      | children                            |                              |
      | enableDateTime                      | <expectedTop2EnableDateTime> |
      | disableDateTime                     | empty                        |
      | parent                              | empty                        |
      | firstLanguageLocale                 | en                           |
      | secondLanguageLocale                | fr                           |
      | firstLanguage                       | English                      |
      | secondLanguage                      | French                       |
      | displayNameFirstLang                | <top2Category>               |
      | displayNameSecondLang               | <top2Category>               |
      | firstLangFirstDetailDisplayName     | <flfdDisplayName>            |
      | firstLangFirstDetailName            | <flfdName>                   |
      | firstLangFirstDetailDisplayValues   | <flfdDisplayValues>          |
      | firstLangFirstDetailValues          | <flfdValues>                 |
      | firstLangSecondDetailDisplayName    | <flsdDisplayName>            |
      | firstLangSecondDetailName           | <flsdName>                   |
      | firstLangSecondDetailDisplayValues  | <flsdDisplayValues>          |
      | firstLangSecondDetailValues         | <flsdValues>                 |
      | secondLangFirstDetailDisplayName    | <slfdDisplayName>            |
      | secondLangFirstDetailName           | <slfdName>                   |
      | secondLangFirstDetailDisplayValues  | <slfdDisplayValues>          |
      | secondLangFirstDetailValues         | <slfdValues>                 |
      | secondLangSecondDetailDisplayName   | <slsdDisplayName>            |
      | secondLangSecondDetailName          | <slsdName>                   |
      | secondLangSecondDetailDisplayValues | <slsdDisplayValues>          |
      | secondLangSecondDetailValues        | <slsdValues>                 |
    When I retrieve latest version of created in CM category projection for store SyndicationSingleLanguageStore and category name <top2Category> via API
    Then Response status code is 200
    And Single category API response contains complete information for projection with 1 language and category code <top2Category>
           # empty values will be replaced in the step with values from entities created in previous steps
      | type                      | category                       |
      | code                      |                                |
      | store                     | SyndicationSingleLanguageStore |
      | modifiedDateTime          |                                |
      | deleted                   | false                          |
      | name                      | CATEGORY_TYPE                  |
      | value                     | <categoryType>                 |
      | children                  |                                |
      | parent                    | empty                          |
      | enableDateTime            | <expectedTop2EnableDateTime>   |
      | disableDateTime           | empty                          |
      | languageLocale            | fr                             |
      | excludedLanguageLocale    | en                             |
      | language                  | French                         |
      | displayName               | <top2Category>                 |
      | FirstDetailDisplayName    | <slfdDisplayName>              |
      | FirstDetailName           | <slfdName>                     |
      | FirstDetailDisplayValues  | <slfdDisplayValues>            |
      | FirstDetailValues         | <slfdValues>                   |
      | SecondDetailDisplayName   | <slsdDisplayName>              |
      | SecondDetailName          | <slsdName>                     |
      | SecondDetailDisplayValues | <slsdDisplayValues>            |
      | SecondDetailValues        | <slsdValues>                   |
    When I retrieve latest version of created in CM category projection for store SyndicationBilingualStore and category name <sub1Top2Category> via API
    Then Response status code is 200
    And Single category API response contains complete information for projection with 2 languages and category code <sub1Top2Category>
           # empty values will be replaced in the step with values from entities created in previous steps
      | type                                | category                         |
      | code                                |                                  |
      | store                               | SyndicationBilingualStore        |
      | deleted                             | false                            |
      | name                                | CATEGORY_TYPE                    |
      | value                               | <categoryType>                   |
      | children                            |                                  |
      | enableDateTime                      | <expectedSub1Top2EnableDateTime> |
      | disableDateTime                     | empty                            |
      | firstLanguageLocale                 | en                               |
      | secondLanguageLocale                | fr                               |
      | firstLanguage                       | English                          |
      | secondLanguage                      | French                           |
      | displayNameFirstLang                | <sub1Top2Category>               |
      | displayNameSecondLang               | <sub1Top2Category>               |
      | firstLangFirstDetailDisplayName     | <flfdDisplayName>                |
      | firstLangFirstDetailName            | <flfdName>                       |
      | firstLangFirstDetailDisplayValues   | <flfdDisplayValues>              |
      | firstLangFirstDetailValues          | <flfdValues>                     |
      | firstLangSecondDetailDisplayName    | <flsdDisplayName>                |
      | firstLangSecondDetailName           | <flsdName>                       |
      | firstLangSecondDetailDisplayValues  | <flsdDisplayValues>              |
      | firstLangSecondDetailValues         | <flsdValues>                     |
      | secondLangFirstDetailDisplayName    | <slfdDisplayName>                |
      | secondLangFirstDetailName           | <slfdName>                       |
      | secondLangFirstDetailDisplayValues  | <slfdDisplayValues>              |
      | secondLangFirstDetailValues         | <slfdValues>                     |
      | secondLangSecondDetailDisplayName   | <slsdDisplayName>                |
      | secondLangSecondDetailName          | <slsdName>                       |
      | secondLangSecondDetailDisplayValues | <slsdDisplayValues>              |
      | secondLangSecondDetailValues        | <slsdValues>                     |
    When I retrieve latest version of created in CM category projection for store SyndicationSingleLanguageStore and category name <sub1Top2Category> via API
    Then Response status code is 200
    And Single category API response contains complete information for projection with 1 language and category code <sub1Top2Category>
           # empty values will be replaced in the step with values from entities created in previous steps
      | type                      | category                         |
      | code                      |                                  |
      | store                     | SyndicationSingleLanguageStore   |
      | modifiedDateTime          |                                  |
      | deleted                   | false                            |
      | name                      | CATEGORY_TYPE                    |
      | value                     | <categoryType>                   |
      | children                  |                                  |
      | enableDateTime            | <expectedSub1Top2EnableDateTime> |
      | disableDateTime           | empty                            |
      | languageLocale            | fr                               |
      | excludedLanguageLocale    | en                               |
      | language                  | French                           |
      | displayName               | <sub1Top2Category>               |
      | FirstDetailDisplayName    | <slfdDisplayName>                |
      | FirstDetailName           | <slfdName>                       |
      | FirstDetailDisplayValues  | <slfdDisplayValues>              |
      | FirstDetailValues         | <slfdValues>                     |
      | SecondDetailDisplayName   | <slsdDisplayName>                |
      | SecondDetailName          | <slsdName>                       |
      | SecondDetailDisplayValues | <slsdDisplayValues>              |
      | SecondDetailValues        | <slsdValues>                     |
    When I retrieve latest version of created in CM category projection for store SyndicationBilingualStore and category name <sub1Sub1Top2category> via API
    Then Response status code is 200
    And Single category API response contains complete information for projection with 2 languages and category code <sub1Sub1Top2category>
           # empty values will be replaced in the step with values from entities created in previous steps
      | type                                | category                             |
      | code                                |                                      |
      | store                               | SyndicationBilingualStore            |
      | deleted                             | false                                |
      | name                                | CATEGORY_TYPE                        |
      | value                               | <categoryType>                       |
      | children                            |                                      |
      | enableDateTime                      | <expectedSub1Sub1Top2EnableDateTime> |
      | disableDateTime                     | empty                                |
      | parent                              |                                      |
      | firstLanguageLocale                 | en                                   |
      | secondLanguageLocale                | fr                                   |
      | firstLanguage                       | English                              |
      | secondLanguage                      | French                               |
      | displayNameFirstLang                | <sub1Sub1Top2category>               |
      | displayNameSecondLang               | <sub1Sub1Top2category>               |
      | firstLangFirstDetailDisplayName     | <flfdDisplayName>                    |
      | firstLangFirstDetailName            | <flfdName>                           |
      | firstLangFirstDetailDisplayValues   | <flfdDisplayValues>                  |
      | firstLangFirstDetailValues          | <flfdValues>                         |
      | firstLangSecondDetailDisplayName    | <flsdDisplayName>                    |
      | firstLangSecondDetailName           | <flsdName>                           |
      | firstLangSecondDetailDisplayValues  | <flsdDisplayValues>                  |
      | firstLangSecondDetailValues         | <flsdValues>                         |
      | secondLangFirstDetailDisplayName    | <slfdDisplayName>                    |
      | secondLangFirstDetailName           | <slfdName>                           |
      | secondLangFirstDetailDisplayValues  | <slfdDisplayValues>                  |
      | secondLangFirstDetailValues         | <slfdValues>                         |
      | secondLangSecondDetailDisplayName   | <slsdDisplayName>                    |
      | secondLangSecondDetailName          | <slsdName>                           |
      | secondLangSecondDetailDisplayValues | <slsdDisplayValues>                  |
      | secondLangSecondDetailValues        | <slsdValues>                         |
    When I retrieve latest version of created in CM category projection for store SyndicationSingleLanguageStore and category name <sub1Sub1Top2category> via API
    Then Response status code is 200
    And Single category API response contains complete information for projection with 1 language and category code <sub1Sub1Top2category>
           # empty values will be replaced in the step with values from entities created in previous steps
      | type                      | category                             |
      | code                      |                                      |
      | store                     | SyndicationSingleLanguageStore       |
      | modifiedDateTime          |                                      |
      | deleted                   | false                                |
      | name                      | CATEGORY_TYPE                        |
      | value                     | <categoryType>                       |
      | children                  |                                      |
      | enableDateTime            | <expectedSub1Sub1Top2EnableDateTime> |
      | disableDateTime           | empty                                |
      | languageLocale            | fr                                   |
      | excludedLanguageLocale    | en                                   |
      | language                  | French                               |
      | displayName               | <sub1Sub1Top2category>               |
      | FirstDetailDisplayName    | <slfdDisplayName>                    |
      | FirstDetailName           | <slfdName>                           |
      | FirstDetailDisplayValues  | <slfdDisplayValues>                  |
      | FirstDetailValues         | <slfdValues>                         |
      | SecondDetailDisplayName   | <slsdDisplayName>                    |
      | SecondDetailName          | <slsdName>                           |
      | SecondDetailDisplayValues | <slsdDisplayValues>                  |
      | SecondDetailValues        | <slsdValues>                         |
    And I am listening to <queueName> queue
    And I delete all messages from <queueName> queue
    And I open category <top2Category> in editor for catalog Syndication Two Stores Catalog
    And I edit the category <top2Category> disable date time to -1
    When I read <queueName> message from queue
    Then Catalog event JMS message json for category <top2Category> contains the following values
    # empty values will be replaced in the step with values from entities created in previous steps
      | key    | value                          |
      | @class | CatalogEventType               |
      | name   | CATEGORIES_UPDATED             |
      | guid   | AGGREGATE                      |
      | type   | category                       |
      | store  | SyndicationSingleLanguageStore |
      | codes  |                                |
    And Catalog event JMS message json for category <top2Category> contains the following values
    # empty values will be replaced in the step with values from entities created in previous steps
      | key    | value                     |
      | @class | CatalogEventType          |
      | name   | CATEGORIES_UPDATED        |
      | guid   | AGGREGATE                 |
      | type   | category                  |
      | store  | SyndicationBilingualStore |
      | codes  |                           |
    When I retrieve latest version of created in CM category projection for store SyndicationBilingualStore and category name <top2Category> via API
    Then Response status code is 200
    And Single category API response contains complete information for tombstone projection with category name <top2Category>
      | type             | category                  |
      | code             |                           |
      | store            | SyndicationBilingualStore |
      | modifiedDateTime |                           |
      | deleted          | true                      |
    When I retrieve latest version of created in CM category projection for store SyndicationSingleLanguageStore and category name <top2Category> via API
    Then Response status code is 200
    And Single category API response contains complete information for tombstone projection with category name <top2Category>
      | type             | category                       |
      | code             |                                |
      | store            | SyndicationSingleLanguageStore |
      | modifiedDateTime |                                |
      | deleted          | true                           |
    When I retrieve latest version of created in CM category projection for store SyndicationBilingualStore and category name <sub1Top2Category> via API
    Then Response status code is 200
    And Single category API response contains complete information for tombstone projection with category name <sub1Top2Category>
      | type             | category                  |
      | code             |                           |
      | store            | SyndicationBilingualStore |
      | modifiedDateTime |                           |
      | deleted          | true                      |
    When I retrieve latest version of created in CM category projection for store SyndicationSingleLanguageStore and category name <sub1Top2Category> via API
    Then Response status code is 200
    And Single category API response contains complete information for tombstone projection with category name <sub1Top2Category>
      | type             | category                       |
      | code             |                                |
      | store            | SyndicationSingleLanguageStore |
      | modifiedDateTime |                                |
      | deleted          | true                           |
    When I retrieve latest version of created in CM category projection for store SyndicationBilingualStore and category name <sub1Sub1Top2category> via API
    Then Response status code is 200
    And Single category API response contains complete information for tombstone projection with category name <sub1Sub1Top2category>
      | type             | category                  |
      | code             |                           |
      | store            | SyndicationBilingualStore |
      | modifiedDateTime |                           |
      | deleted          | true                      |
    When I retrieve latest version of created in CM category projection for store SyndicationSingleLanguageStore and category name <sub1Sub1Top2category> via API
    Then Response status code is 200
    And Single category API response contains complete information for tombstone projection with category name <sub1Sub1Top2category>
      | type             | category                       |
      | code             |                                |
      | store            | SyndicationSingleLanguageStore |
      | modifiedDateTime |                                |
      | deleted          | true                           |
    And I am listening to <queueName> queue
    And I delete all messages from <queueName> queue
    And I open category <top2Category> in editor for catalog Syndication Two Stores Catalog
    And I remove disable date time for category <top2Category>
    When I read <queueName> message from queue
    Then Catalog event JMS message json for category <top2Category> contains the following values
    # empty values will be replaced in the step with values from entities created in previous steps
      | key    | value                          |
      | @class | CatalogEventType               |
      | name   | CATEGORIES_UPDATED             |
      | guid   | AGGREGATE                      |
      | type   | category                       |
      | store  | SyndicationSingleLanguageStore |
      | codes  |                                |
    And Catalog event JMS message json for category <top2Category> contains the following values
    # empty values will be replaced in the step with values from entities created in previous steps
      | key    | value                     |
      | @class | CatalogEventType          |
      | name   | CATEGORIES_UPDATED        |
      | guid   | AGGREGATE                 |
      | type   | category                  |
      | store  | SyndicationBilingualStore |
      | codes  |                           |
    When I retrieve latest version of created in CM category projection for store SyndicationBilingualStore and category name <top2Category> via API
    Then Response status code is 200
    And Single category API response contains complete information for projection with 2 languages and category code <top2Category>
           # empty values will be replaced in the step with values from entities created in previous steps
      | type                                | category                     |
      | code                                |                              |
      | store                               | SyndicationBilingualStore    |
      | deleted                             | false                        |
      | name                                | CATEGORY_TYPE                |
      | value                               | <categoryType>               |
      | children                            |                              |
      | enableDateTime                      | <expectedTop2EnableDateTime> |
      | disableDateTime                     | empty                        |
      | parent                              | empty                        |
      | firstLanguageLocale                 | en                           |
      | secondLanguageLocale                | fr                           |
      | firstLanguage                       | English                      |
      | secondLanguage                      | French                       |
      | displayNameFirstLang                | <top2Category>               |
      | displayNameSecondLang               | <top2Category>               |
      | firstLangFirstDetailDisplayName     | <flfdDisplayName>            |
      | firstLangFirstDetailName            | <flfdName>                   |
      | firstLangFirstDetailDisplayValues   | <flfdDisplayValues>          |
      | firstLangFirstDetailValues          | <flfdValues>                 |
      | firstLangSecondDetailDisplayName    | <flsdDisplayName>            |
      | firstLangSecondDetailName           | <flsdName>                   |
      | firstLangSecondDetailDisplayValues  | <flsdDisplayValues>          |
      | firstLangSecondDetailValues         | <flsdValues>                 |
      | secondLangFirstDetailDisplayName    | <slfdDisplayName>            |
      | secondLangFirstDetailName           | <slfdName>                   |
      | secondLangFirstDetailDisplayValues  | <slfdDisplayValues>          |
      | secondLangFirstDetailValues         | <slfdValues>                 |
      | secondLangSecondDetailDisplayName   | <slsdDisplayName>            |
      | secondLangSecondDetailName          | <slsdName>                   |
      | secondLangSecondDetailDisplayValues | <slsdDisplayValues>          |
      | secondLangSecondDetailValues        | <slsdValues>                 |
    When I retrieve latest version of created in CM category projection for store SyndicationSingleLanguageStore and category name <top2Category> via API
    Then Response status code is 200
    And Single category API response contains complete information for projection with 1 language and category code <top2Category>
           # empty values will be replaced in the step with values from entities created in previous steps
      | type                      | category                       |
      | code                      |                                |
      | store                     | SyndicationSingleLanguageStore |
      | modifiedDateTime          |                                |
      | deleted                   | false                          |
      | name                      | CATEGORY_TYPE                  |
      | value                     | <categoryType>                 |
      | children                  |                                |
      | parent                    | empty                          |
      | enableDateTime            | <expectedTop2EnableDateTime>   |
      | disableDateTime           | empty                          |
      | languageLocale            | fr                             |
      | excludedLanguageLocale    | en                             |
      | language                  | French                         |
      | displayName               | <top2Category>                 |
      | FirstDetailDisplayName    | <slfdDisplayName>              |
      | FirstDetailName           | <slfdName>                     |
      | FirstDetailDisplayValues  | <slfdDisplayValues>            |
      | FirstDetailValues         | <slfdValues>                   |
      | SecondDetailDisplayName   | <slsdDisplayName>              |
      | SecondDetailName          | <slsdName>                     |
      | SecondDetailDisplayValues | <slsdDisplayValues>            |
      | SecondDetailValues        | <slsdValues>                   |
    When I retrieve latest version of created in CM category projection for store SyndicationBilingualStore and category name <sub1Top2Category> via API
    Then Response status code is 200
    And Single category API response contains complete information for projection with 2 languages and category code <sub1Top2Category>
           # empty values will be replaced in the step with values from entities created in previous steps
      | type                                | category                         |
      | code                                |                                  |
      | store                               | SyndicationBilingualStore        |
      | deleted                             | false                            |
      | name                                | CATEGORY_TYPE                    |
      | value                               | <categoryType>                   |
      | children                            |                                  |
      | enableDateTime                      | <expectedSub1Top2EnableDateTime> |
      | disableDateTime                     | empty                            |
      | parent                              |                                  |
      | firstLanguageLocale                 | en                               |
      | secondLanguageLocale                | fr                               |
      | firstLanguage                       | English                          |
      | secondLanguage                      | French                           |
      | displayNameFirstLang                | <sub1Top2Category>               |
      | displayNameSecondLang               | <sub1Top2Category>               |
      | firstLangFirstDetailDisplayName     | <flfdDisplayName>                |
      | firstLangFirstDetailName            | <flfdName>                       |
      | firstLangFirstDetailDisplayValues   | <flfdDisplayValues>              |
      | firstLangFirstDetailValues          | <flfdValues>                     |
      | firstLangSecondDetailDisplayName    | <flsdDisplayName>                |
      | firstLangSecondDetailName           | <flsdName>                       |
      | firstLangSecondDetailDisplayValues  | <flsdDisplayValues>              |
      | firstLangSecondDetailValues         | <flsdValues>                     |
      | secondLangFirstDetailDisplayName    | <slfdDisplayName>                |
      | secondLangFirstDetailName           | <slfdName>                       |
      | secondLangFirstDetailDisplayValues  | <slfdDisplayValues>              |
      | secondLangFirstDetailValues         | <slfdValues>                     |
      | secondLangSecondDetailDisplayName   | <slsdDisplayName>                |
      | secondLangSecondDetailName          | <slsdName>                       |
      | secondLangSecondDetailDisplayValues | <slsdDisplayValues>              |
      | secondLangSecondDetailValues        | <slsdValues>                     |
    When I retrieve latest version of created in CM category projection for store SyndicationSingleLanguageStore and category name <sub1Top2Category> via API
    Then Response status code is 200
    And Single category API response contains complete information for projection with 1 language and category code <sub1Top2Category>
           # empty values will be replaced in the step with values from entities created in previous steps
      | type                      | category                         |
      | code                      |                                  |
      | store                     | SyndicationSingleLanguageStore   |
      | modifiedDateTime          |                                  |
      | deleted                   | false                            |
      | name                      | CATEGORY_TYPE                    |
      | value                     | <categoryType>                   |
      | children                  |                                  |
      | enableDateTime            | <expectedSub1Top2EnableDateTime> |
      | disableDateTime           | empty                            |
      | languageLocale            | fr                               |
      | excludedLanguageLocale    | en                               |
      | language                  | French                           |
      | displayName               | <sub1Top2Category>               |
      | FirstDetailDisplayName    | <slfdDisplayName>                |
      | FirstDetailName           | <slfdName>                       |
      | FirstDetailDisplayValues  | <slfdDisplayValues>              |
      | FirstDetailValues         | <slfdValues>                     |
      | SecondDetailDisplayName   | <slsdDisplayName>                |
      | SecondDetailName          | <slsdName>                       |
      | SecondDetailDisplayValues | <slsdDisplayValues>              |
      | SecondDetailValues        | <slsdValues>                     |
    When I retrieve latest version of created in CM category projection for store SyndicationBilingualStore and category name <sub1Sub1Top2category> via API
    Then Response status code is 200
    And Single category API response contains complete information for projection with 2 languages and category code <sub1Sub1Top2category>
           # empty values will be replaced in the step with values from entities created in previous steps
      | type                                | category                             |
      | code                                |                                      |
      | store                               | SyndicationBilingualStore            |
      | deleted                             | false                                |
      | name                                | CATEGORY_TYPE                        |
      | value                               | <categoryType>                       |
      | children                            |                                      |
      | enableDateTime                      | <expectedSub1Sub1Top2EnableDateTime> |
      | disableDateTime                     | empty                                |
      | parent                              |                                      |
      | firstLanguageLocale                 | en                                   |
      | secondLanguageLocale                | fr                                   |
      | firstLanguage                       | English                              |
      | secondLanguage                      | French                               |
      | displayNameFirstLang                | <sub1Sub1Top2category>               |
      | displayNameSecondLang               | <sub1Sub1Top2category>               |
      | firstLangFirstDetailDisplayName     | <flfdDisplayName>                    |
      | firstLangFirstDetailName            | <flfdName>                           |
      | firstLangFirstDetailDisplayValues   | <flfdDisplayValues>                  |
      | firstLangFirstDetailValues          | <flfdValues>                         |
      | firstLangSecondDetailDisplayName    | <flsdDisplayName>                    |
      | firstLangSecondDetailName           | <flsdName>                           |
      | firstLangSecondDetailDisplayValues  | <flsdDisplayValues>                  |
      | firstLangSecondDetailValues         | <flsdValues>                         |
      | secondLangFirstDetailDisplayName    | <slfdDisplayName>                    |
      | secondLangFirstDetailName           | <slfdName>                           |
      | secondLangFirstDetailDisplayValues  | <slfdDisplayValues>                  |
      | secondLangFirstDetailValues         | <slfdValues>                         |
      | secondLangSecondDetailDisplayName   | <slsdDisplayName>                    |
      | secondLangSecondDetailName          | <slsdName>                           |
      | secondLangSecondDetailDisplayValues | <slsdDisplayValues>                  |
      | secondLangSecondDetailValues        | <slsdValues>                         |
    When I retrieve latest version of created in CM category projection for store SyndicationSingleLanguageStore and category name <sub1Sub1Top2category> via API
    Then Response status code is 200
    And Single category API response contains complete information for projection with 1 language and category code <sub1Sub1Top2category>
           # empty values will be replaced in the step with values from entities created in previous steps
      | type                      | category                             |
      | code                      |                                      |
      | store                     | SyndicationSingleLanguageStore       |
      | modifiedDateTime          |                                      |
      | deleted                   | false                                |
      | name                      | CATEGORY_TYPE                        |
      | value                     | <categoryType>                       |
      | children                  |                                      |
      | enableDateTime            | <expectedSub1Sub1Top2EnableDateTime> |
      | disableDateTime           | empty                                |
      | languageLocale            | fr                                   |
      | excludedLanguageLocale    | en                                   |
      | language                  | French                               |
      | displayName               | <sub1Sub1Top2category>               |
      | FirstDetailDisplayName    | <slfdDisplayName>                    |
      | FirstDetailName           | <slfdName>                           |
      | FirstDetailDisplayValues  | <slfdDisplayValues>                  |
      | FirstDetailValues         | <slfdValues>                         |
      | SecondDetailDisplayName   | <slsdDisplayName>                    |
      | SecondDetailName          | <slsdName>                           |
      | SecondDetailDisplayValues | <slsdDisplayValues>                  |
      | SecondDetailValues        | <slsdValues>                         |

    Examples:
      | queueName                             | top2Category       | categoryType        | sub1Top2Category       | sub1Sub1Top2category       | expectedTop2EnableDateTime | expectedSub1Top2EnableDateTime | expectedSub1Sub1Top2EnableDateTime | flfdDisplayName     | flfdName          | flfdDisplayValues | flfdValues  | flsdDisplayName     | flsdName          | flsdDisplayValues | flsdValues  | slfdDisplayName     | slfdName          | slfdDisplayValues | slfdValues  | slsdDisplayName     | slsdName          | slsdDisplayValues | slsdValues  |
      | Consumer.test.VirtualTopic.ep.catalog | categoryTestTop2En | categoryTestCatType | categoryTestSub1Top2En | categoryTestSub1Sub1Top2En | Jun 1, 2019 8:58 AM        | Jul 1, 2019 9:01 AM            | Aug 1, 2019 9:07 AM                | categoryTestAttr1En | categoryTestAttr1 | attr1value1       | attr1value1 | categoryTestAttr2En | categoryTestAttr2 | attr2value1       | attr2value1 | categoryTestAttr1Fr | categoryTestAttr1 | attr1value1       | attr1value1 | categoryTestAttr2Fr | categoryTestAttr2 | attr2value1       | attr2value1 |
