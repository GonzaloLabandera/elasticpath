@cm @regression
Feature: Syndication flow for Cart Item Modifier Group

  Background:
    Given I have catalog Syndication Two Stores Catalog
    And I have stores SyndicationBilingualStore, SyndicationSingleLanguageStore connected to Syndication Two Stores Catalog catalog
    When I sign in to CM as admin user
    And I go to Catalog Management
    And I select CartItemModifierGroups tab in a catalog editor for catalog Syndication Two Stores Catalog

  Scenario Outline: Create and then Delete Cart Item Modifier Group, check ep.catalog JMS message, check created projection via API
    And I am listening to <queueName> queue
    And I delete all messages from <queueName> queue
    And I add a new cart item modifier group with cart item modifier group code cartGroup1 without saving with the following names
      | English | <cartGroupNameEn> |
      | French  | <cartGroupNameFr> |
    Then newly created group is in the list
    When I open selected Cart item modifier group
    And I add a new cart item modifier group field with cart item modifier group field code <cartGroup1FieldCode1> and the following names without saving
      | English  | <cartGroup1FieldCode1EnName> |
      | French   | <cartGroup1FieldCode1FrName> |
      | Type     | Short Text                   |
      | Required | true                         |
      | Max size | 10                           |
    When I add a new cart item modifier group field with cart item modifier group field code <cartGroup1FieldCode2> and the following names without saving
      | English  | <cartGroup1FieldCode2EnName> |
      | French   | <cartGroup1FieldCode2FrName> |
      | Type     | Decimal                      |
      | Required | false                        |
    When I add a new cart item modifier group field with cart item modifier group field code <cartGroup1FieldCode3> and the following names without saving
      | English  | <cartGroup1FieldCode3EnName> |
      | French   | <cartGroup1FieldCode3FrName> |
      | Type     | Multi Select Option          |
      | Required | true                         |
    And I add a new option for field with field code <cartGroup1FieldCode3> with cart item modifier group field option value <cartGroup1Field3Value1> and the following names without saving
      | English | <cartGroup1Field3Value1EnName> |
      | French  | <cartGroup1Field3Value1FrName> |
    And I add a new option for field with field code <cartGroup1FieldCode3> with cart item modifier group field option value <cartGroup1Field3Value2> and the following names without saving
      | English | <cartGroup1Field3Value2EnName> |
      | French  | <cartGroup1Field3Value2FrName> |
    And I save new Multi Select Option
    And I save my changes
    Then newly created group is in the list
    When I go to Configuration
    And I go to Stores
    And I take all exist store codes
    When There are group projections for all exist stores
    When I read <queueName> message from queue
    Then Catalog event JMS message json for store <store1> contains the following values
    # empty values will be replaced in the step with values from entities created in previous steps
      | key    | value                  |
      | @class | CatalogEventType       |
      | name   | FIELD_METADATA_UPDATED |
      | guid   | AGGREGATE              |
      | type   | fieldMetadata          |
      | store  | <store1>               |
      | codes  |                        |
    And Catalog event JMS message json for store <store2> contains the following values
    # empty values will be replaced in the step with values from entities created in previous steps
      | key    | value                  |
      | @class | CatalogEventType       |
      | name   | FIELD_METADATA_UPDATED |
      | guid   | AGGREGATE              |
      | type   | fieldMetadata          |
      | store  | <store2>               |
      | codes  |                        |
    When I retrieve latest version of created in CM group projection for store <store1> via API
    Then Response status code is 200
    And Single group API response contains complete information for projection with 3 group values and 2 languages
    # empty values will be replaced in the step with values from entities created in previous steps
      | type                                  | fieldMetadata                  |
      | code                                  |                                |
      | store                                 | <store1>                       |
      | deleted                               | false                          |
      | firstLanguageLocale                   | en                             |
      | secondLanguageLocale                  | fr                             |
      | firstLanguage                         | English                        |
      | secondLanguage                        | French                         |
      | displayNameFirstLang                  | <cartGroupNameEn>              |
      | displayNameSecondLang                 | <cartGroupNameFr>              |
      | FirstFieldName                        | <cartGroup1FieldCode1>         |
      | FirstFieldDisplayNameFirstLang        | <cartGroup1FieldCode1EnName>   |
      | FirstFieldDisplayNameSecondLang       | <cartGroup1FieldCode1FrName>   |
      | FirstFieldDataType                    | ShortText                      |
      | FirstFieldMaxSize                     | 10                             |
      | FirstFieldRequired                    | true                           |
      | FirstFieldFieldValues                 | empty array                    |
      | SecondFieldName                       | <cartGroup1FieldCode2>         |
      | SecondFieldDisplayNameFirstLang       | <cartGroup1FieldCode2EnName>   |
      | SecondFieldDisplayNameSecondLang      | <cartGroup1FieldCode2FrName>   |
      | SecondFieldDataType                   | Decimal                        |
      | SecondFieldRequired                   | false                          |
      | SecondFieldFieldValues                | empty array                    |
      | ThirdFieldName                        | <cartGroup1FieldCode3>         |
      | ThirdFieldDisplayNameFirstLang        | <cartGroup1FieldCode3EnName>   |
      | ThirdFieldDisplayNameSecondLang       | <cartGroup1FieldCode3FrName>   |
      | ThirdFieldDataType                    | PickMultiOption                |
      | ThirdFieldRequired                    | true                           |
      | ThirdFieldFirstValue                  | <cartGroup1Field3Value1>       |
      | ThirdFieldFirstDisplayNameFirstLang   | <cartGroup1Field3Value1EnName> |
      | ThirdFieldFirstDisplayNameSecondLang  | <cartGroup1Field3Value1FrName> |
      | ThirdFieldSecondValue                 | <cartGroup1Field3Value2>       |
      | ThirdFieldSecondDisplayNameFirstLang  | <cartGroup1Field3Value2EnName> |
      | ThirdFieldSecondDisplayNameSecondLang | <cartGroup1Field3Value2FrName> |
    When I retrieve latest version of created in CM group projection for store <store2> via API
    Then Response status code is 200
    And Single group API response contains complete information for projection with 3 group values and 1 language
    # empty values will be replaced in the step with values from entities created in previous steps
      | type                        | fieldMetadata                  |
      | code                        |                                |
      | store                       | <store2>                       |
      | deleted                     | false                          |
      | displayName                 | <cartGroupNameFr>              |
      | languageLocale              | fr                             |
      | excludedLanguageLocale      | en                             |
      | language                    | French                         |
      | FirstFieldName              | <cartGroup1FieldCode1>         |
      | FirstFieldDisplayName       | <cartGroup1FieldCode1FrName>   |
      | FirstFieldDataType          | ShortText                      |
      | FirstFieldMaxSize           | 10                             |
      | FirstFieldRequired          | true                           |
      | FirstFieldFieldValues       | empty array                    |
      | SecondFieldName             | <cartGroup1FieldCode2>         |
      | SecondFieldDisplayName      | <cartGroup1FieldCode2FrName>   |
      | SecondFieldDataType         | Decimal                        |
      | SecondFieldRequired         | false                          |
      | SecondFieldFieldValues      | empty array                    |
      | ThirdFieldName              | <cartGroup1FieldCode3>         |
      | ThirdFieldDisplayName       | <cartGroup1FieldCode3FrName>   |
      | ThirdFieldDataType          | PickMultiOption                |
      | ThirdFieldRequired          | true                           |
      | ThirdFieldFirstValue        | <cartGroup1Field3Value1>       |
      | ThirdFieldFirstDisplayName  | <cartGroup1Field3Value1FrName> |
      | ThirdFieldSecondValue       | <cartGroup1Field3Value2>       |
      | ThirdFieldSecondDisplayName | <cartGroup1Field3Value2FrName> |
    And I go to Catalog Management
    And I select CartItemModifierGroups tab in a catalog editor for catalog Syndication Two Stores Catalog
    And I delete all messages from <queueName> queue
    And I delete the newly created group
    And I save my changes
    And I read <queueName> message from queue
    Then Catalog event JMS message json for store <store1> contains the following values
    # empty values will be replaced in the step with values from entities created in previous steps
      | key    | value                  |
      | @class | CatalogEventType       |
      | name   | FIELD_METADATA_UPDATED |
      | guid   | AGGREGATE              |
      | type   | fieldMetadata          |
      | store  | <store1>               |
      | codes  |                        |
    And Catalog event JMS message json for store <store2> contains the following values
    # empty values will be replaced in the step with values from entities created in previous steps
      | key    | value                  |
      | @class | CatalogEventType       |
      | name   | FIELD_METADATA_UPDATED |
      | guid   | AGGREGATE              |
      | type   | fieldMetadata          |
      | store  | <store2>               |
      | codes  |                        |
    When I retrieve latest version of created in CM group projection for store <store2> via API
    Then Response status code is 200
    And Single group API response contains complete information for projection of deleted cartGroup
    # empty values will be replaced in the step with values from entities created in previous steps
      | type    | fieldMetadata |
      | code    |               |
      | store   | <store2>      |
      | deleted | true          |
    When I retrieve latest version of created in CM group projection for store <store1> via API
    Then Response status code is 200
    And Single group API response contains complete information for projection of deleted cartGroup
    # empty values will be replaced in the step with values from entities created in previous steps
      | type    | fieldMetadata |
      | code    |               |
      | store   | <store1>      |
      | deleted | true          |


    Examples:
      | queueName                             | store1                    | store2                         | cartGroupNameEn | cartGroupNameFr | cartGroup1FieldCode1 | cartGroup1FieldCode1FrName | cartGroup1FieldCode1EnName | cartGroup1FieldCode2 | cartGroup1FieldCode2EnName | cartGroup1FieldCode2FrName | cartGroup1FieldCode3 | cartGroup1FieldCode3EnName | cartGroup1FieldCode3FrName | cartGroup1Field3Value1 | cartGroup1Field3Value1EnName | cartGroup1Field3Value1FrName | cartGroup1Field3Value2 | cartGroup1Field3Value2EnName | cartGroup1Field3Value2FrName |
      | Consumer.test.VirtualTopic.ep.catalog | SyndicationBilingualStore | SyndicationSingleLanguageStore | cartGroupNameEn | cartGroupNameFr | cartGroup1FieldCode1 | cartGroup1FieldCode1FrName | cartGroup1FieldCode1EnName | cartGroup1FieldCode2 | cartGroup1FieldCode2EnName | cartGroup1FieldCode2FrName | cartGroup1FieldCode3 | cartGroup1FieldCode3EnName | cartGroup1FieldCode3FrName | cartGroup1Field3Value1 | cartGroup1Field3Value1EnName | cartGroup1Field3Value1FrName | cartGroup1Field3Value2 | cartGroup1Field3Value2EnName | cartGroup1Field3Value2FrName |

  Scenario Outline: Update Cart Item Modifier Group, check ep.catalog JMS message, check created projection via API
    When I have cart item modifier group with code fieldMetadataTest1 and the following parameters
      | field1Code         | <cartGroup1FieldCode1>    |
      | field2Code         | <cartGroup1FieldCode2>    |
      | field3Code         | <cartGroup1FieldCode3>    |
      | field3Option1Code  | <cartGroup1Field3Option1> |
      | field3Option1Lang1 | <firstLanguage>           |
      | field3Option1Lang2 | <secondLanguage>          |
      | field3Option2Code  | <cartGroup1Field3Option2> |
      | field3Option2Lang1 | <firstLanguage>           |
      | field3Option2Lang2 | <secondLanguage>          |
    And mentioned group is in the list
    And I am listening to <queueName> queue
    And I delete all messages from <queueName> queue
    And I open cart item modifier group field <cartGroup1FieldCode1> for selected group
    And I edit 5 last characters of cart item modifier group field <cartGroup1FieldCode1> names with random characters without saving
      | <firstLanguage> | <secondLanguage> |
    And I change field <cartGroup1FieldCode1> type as Short text -> Decimal or Decimal -> Short text with Max size 10
    And I close field and group dialogs keeping made changes
    And I open cart item modifier group field <cartGroup1FieldCode2> for selected group
    And I edit 5 last characters of cart item modifier group field <cartGroup1FieldCode2> names with random characters without saving
      | <firstLanguage> | <secondLanguage> |
    And I change field <cartGroup1FieldCode2> type as Short text -> Decimal or Decimal -> Short text with Max size 10
    And I close field and group dialogs keeping made changes
    And I open cart item modifier group field <cartGroup1FieldCode3> for selected group
    And I edit 5 last characters of cart item modifier group field <cartGroup1FieldCode3> names with random characters without saving
      | <firstLanguage> | <secondLanguage> |
    And I edit 5 last characters of cart item modifier group field <cartGroup1FieldCode3> option <cartGroup1Field3Option1> names with random characters the following languages without saving
      | <firstLanguage> | <secondLanguage> |
    And I edit 5 last characters of cart item modifier group field <cartGroup1FieldCode3> option <cartGroup1Field3Option2> names with random characters the following languages without saving
      | <firstLanguage> | <secondLanguage> |
    And I close field and group dialogs keeping made changes
    And I save my changes
    Then newly created group is in the list
    When I read <queueName> message from queue
    Then Catalog event JMS message json for store <store1> contains the following values
    # empty values will be replaced in the step with values from entities created in previous steps
      | key    | value                  |
      | @class | CatalogEventType       |
      | name   | FIELD_METADATA_UPDATED |
      | guid   | AGGREGATE              |
      | type   | fieldMetadata          |
      | store  | <store1>               |
      | codes  |                        |
    And Catalog event JMS message json for store <store2> contains the following values
    # empty values will be replaced in the step with values from entities created in previous steps
      | key    | value                  |
      | @class | CatalogEventType       |
      | name   | FIELD_METADATA_UPDATED |
      | guid   | AGGREGATE              |
      | type   | fieldMetadata          |
      | store  | <store2>               |
      | codes  |                        |
    When I retrieve latest version of created in CM group projection for store <store1> via API
    Then Response status code is 200
    And Single group API response contains complete fields information for group with 3 fields and 2 languages
    # empty values will be replaced in the step with values from entities created in previous steps
      | firstLanguageLocale                   | en                        |
      | secondLanguageLocale                  | fr                        |
      | firstLanguage                         | <firstLanguage>           |
      | secondLanguage                        | <secondLanguage>          |
      | FirstFieldName                        | <cartGroup1FieldCode1>    |
      | FirstFieldDisplayNameFirstLang        |                           |
      | FirstFieldDisplayNameSecondLang       |                           |
      | FirstFieldDataType                    |                           |
      | SecondFieldName                       | <cartGroup1FieldCode2>    |
      | SecondFieldDisplayNameFirstLang       |                           |
      | SecondFieldDisplayNameSecondLang      |                           |
      | SecondFieldDataType                   |                           |
      | SecondFieldMaxSize                    |                           |
      | ThirdFieldName                        | <cartGroup1FieldCode3>    |
      | ThirdFieldDisplayNameFirstLang        |                           |
      | ThirdFieldDisplayNameSecondLang       |                           |
      | ThirdFieldDataType                    | PickMultiOption           |
      | ThirdFieldFirstValue                  | <cartGroup1Field3Option1> |
      | ThirdFieldFirstDisplayNameFirstLang   |                           |
      | ThirdFieldFirstDisplayNameSecondLang  |                           |
      | ThirdFieldSecondValue                 | <cartGroup1Field3Option2> |
      | ThirdFieldSecondDisplayNameFirstLang  |                           |
      | ThirdFieldSecondDisplayNameSecondLang |                           |
    When I retrieve latest version of created in CM group projection for store <store2> via API
    Then Response status code is 200
    And Single group API response contains complete fields information for group with 3 fields and 1 language
    # empty values will be replaced in the step with values from entities created in previous steps
      | languageLocale              | fr                        |
      | excludedLanguageLocale      | en                        |
      | language                    | <secondLanguage>          |
      | FirstFieldName              | <cartGroup1FieldCode1>    |
      | FirstFieldDisplayName       |                           |
      | FirstFieldDataType          |                           |
      | SecondFieldName             | <cartGroup1FieldCode2>    |
      | SecondFieldDisplayName      |                           |
      | SecondFieldDataType         |                           |
      | SecondFieldMaxSize          |                           |
      | ThirdFieldName              | <cartGroup1FieldCode3>    |
      | ThirdFieldDisplayName       |                           |
      | ThirdFieldDataType          | PickMultiOption           |
      | ThirdFieldFirstValue        | <cartGroup1Field3Option1> |
      | ThirdFieldFirstDisplayName  |                           |
      | ThirdFieldSecondValue       | <cartGroup1Field3Option2> |
      | ThirdFieldSecondDisplayName |                           |

    Examples:
      | queueName                             | store1                    | store2                         | cartGroup1FieldCode1     | cartGroup1FieldCode2     | cartGroup1FieldCode3     | cartGroup1Field3Option1         | cartGroup1Field3Option2         | firstLanguage | secondLanguage |
      | Consumer.test.VirtualTopic.ep.catalog | SyndicationBilingualStore | SyndicationSingleLanguageStore | fieldMetadataTest1Field1 | fieldMetadataTest1Field2 | fieldMetadataTest1Field3 | fieldMetadataTest1Field3Option1 | fieldMetadataTest1Field3Option2 | English       | French         |
