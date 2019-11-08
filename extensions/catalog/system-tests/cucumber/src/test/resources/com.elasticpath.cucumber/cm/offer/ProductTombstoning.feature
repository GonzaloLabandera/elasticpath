@cm @regression
Feature: Tombstone flow for Product

  Background:
    Given I have catalog Syndication Two Stores Catalog which has English and French languages
    And I have stores SyndicationBilingualStore, SyndicationSingleLanguageStore connected to Syndication Two Stores Catalog catalog
    When I sign in to CM as admin user
    And I go to Catalog Management

  Scenario Outline: Create tombstone product, check ep.catalog JMS message, check created product projection via API

    Given I select catalog Syndication Two Stores Catalog in the list
    And I open the selected catalog
    And I select CartItemModifierGroups tab in a catalog editor for catalog Syndication Two Stores Catalog
    And I add a new cart item modifier group with cart item modifier group code cartGroup1 without saving with the following names
      | English | cartGroupName_En |
      | French  | cartGroupName_Fr |
    And I save my changes
    And I select Brands tab in a catalog editor for catalog Syndication Two Stores Catalog
    And I add a new brand with brand code brandCode with the following exactly names
      | English | brand_en |
      | French  | brand_fr |
    And I select CategoryTypes tab in a catalog editor for catalog Syndication Two Stores Catalog
    And I create a new category type TestCat with following attributes
      | Category Description |
    And I create category of the category type for Syndication Two Stores Catalog
      | categoryName   | storeVisible | attrLongTextName     | attrLongTextValue   | attrDecimalName | attrDecimalValue | attrShortTextName | attrShortTextValue   |
      | ATest Category | true         | Category Description | long text attribute | Category Rating | 1.1              | Name              | short text attribute |
    And I select ProductTypes tab in a catalog editor for catalog Syndication Two Stores Catalog
    When I create a new product type TestProductType with newly created cart item modifier group and attribute
      | Features |
    And I create new product with following attributes for current catalog and created category
      | productName | taxCode | storeVisible | availability     | shippableType | catalog                        | brand    |
      | TestProduct | DIGITAL | true         | Always available | Digital Asset | Syndication Two Stores Catalog | brand_en |
    And I am listening to <queueName> queue
    And I delete all messages from <queueName> queue
    And I search and open an existing product with product name
    And I click store visible
    When I read Consumer.test.VirtualTopic.ep.catalog message from queue
    Then Catalog event JMS message json for store <storeOneLanguages> contains the following values
    # empty values will be replaced in the step with values from entities created in previous steps
      | key    | value               |
      | @class | CatalogEventType    |
      | name   | OFFERS_UPDATED      |
      | guid   | AGGREGATE           |
      | type   | offer               |
      | store  | <storeOneLanguages> |
      | codes  |                     |
    And Catalog event JMS message json for store <storeTwoLanguages> contains the following values
    # empty values will be replaced in the step with values from entities created in previous steps
      | key    | value               |
      | @class | CatalogEventType    |
      | name   | OFFERS_UPDATED      |
      | guid   | AGGREGATE           |
      | type   | offer               |
      | store  | <storeTwoLanguages> |
      | codes  |                     |
    When I retrieve latest version of created in CM offer projection for store <storeOneLanguages> via API
    Then Response status code is 200
    And Single offer API response contains complete information for tombstone projection
      | type              | offer               |
      | store             | <storeOneLanguages> |
      | deleted           | true                |
      | properties        | excluded            |
      | associations      | excluded            |
      | availabilityRules | excluded            |
      | selectionRules    | excluded            |
      | components        | excluded            |
      | extensions        | excluded            |
      | translations      | excluded            |
      | categories        | excluded            |
      | formFields        | excluded            |
    When I retrieve latest version of created in CM offer projection for store <storeTwoLanguages> via API
    Then Response status code is 200
    And Single offer API response contains complete information for tombstone projection
      | type              | offer               |
      | store             | <storeTwoLanguages> |
      | deleted           | true                |
      | properties        | excluded            |
      | associations      | excluded            |
      | availabilityRules | excluded            |
      | selectionRules    | excluded            |
      | components        | excluded            |
      | extensions        | excluded            |
      | translations      | excluded            |
      | categories        | excluded            |
      | formFields        | excluded            |
    And I delete all messages from <queueName> queue
    When I click store visible
    Then Catalog event JMS message json for store <storeOneLanguages> contains the following values
    # empty values will be replaced in the step with values from entities created in previous steps
      | key    | value               |
      | @class | CatalogEventType    |
      | name   | OFFERS_UPDATED      |
      | guid   | AGGREGATE           |
      | type   | offer               |
      | store  | <storeOneLanguages> |
      | codes  |                     |
    And Catalog event JMS message json for store <storeTwoLanguages> contains the following values
    # empty values will be replaced in the step with values from entities created in previous steps
      | key    | value               |
      | @class | CatalogEventType    |
      | name   | OFFERS_UPDATED      |
      | guid   | AGGREGATE           |
      | type   | offer               |
      | store  | <storeTwoLanguages> |
      | codes  |                     |
    When I retrieve latest version of created in CM offer projection for store <storeOneLanguages> via API
    Then Response status code is 200
    And Single offer API response contains complete information for tombstone projection
      | type              | offer               |
      | store             | <storeOneLanguages> |
      | deleted           | false               |
      | properties        | not excluded        |
      | associations      | not excluded        |
      | availabilityRules | not excluded        |
      | selectionRules    | not excluded        |
      | components        | not excluded        |
      | extensions        | not excluded        |
      | translations      | not excluded        |
      | categories        | not excluded        |
      | formFields        | not excluded        |
    When I retrieve latest version of created in CM offer projection for store <storeTwoLanguages> via API
    Then Response status code is 200
    And Single offer API response contains complete information for tombstone projection
      | type              | offer               |
      | store             | <storeTwoLanguages> |
      | deleted           | false               |
      | properties        | not excluded        |
      | associations      | not excluded        |
      | availabilityRules | not excluded        |
      | selectionRules    | not excluded        |
      | components        | not excluded        |
      | extensions        | not excluded        |
      | translations      | not excluded        |
      | categories        | not excluded        |
      | formFields        | not excluded        |
    When I delete all messages from <queueName> queue
    And I enter past date in Disable Date / Time field and save changes
    And I read Consumer.test.VirtualTopic.ep.catalog message from queue
    Then Catalog event JMS message json for store <storeOneLanguages> contains the following values
    # empty values will be replaced in the step with values from entities created in previous steps
      | key    | value               |
      | @class | CatalogEventType    |
      | name   | OFFERS_UPDATED      |
      | guid   | AGGREGATE           |
      | type   | offer               |
      | store  | <storeOneLanguages> |
      | codes  |                     |
    And Catalog event JMS message json for store <storeTwoLanguages> contains the following values
    # empty values will be replaced in the step with values from entities created in previous steps
      | key    | value               |
      | @class | CatalogEventType    |
      | name   | OFFERS_UPDATED      |
      | guid   | AGGREGATE           |
      | type   | offer               |
      | store  | <storeTwoLanguages> |
      | codes  |                     |
    When I retrieve latest version of created in CM offer projection for store <storeOneLanguages> via API
    Then Response status code is 200
    And Single offer API response contains complete information for tombstone projection
      | type              | offer               |
      | store             | <storeOneLanguages> |
      | deleted           | true                |
      | properties        | excluded            |
      | associations      | excluded            |
      | availabilityRules | excluded            |
      | selectionRules    | excluded            |
      | components        | excluded            |
      | extensions        | excluded            |
      | translations      | excluded            |
      | categories        | excluded            |
      | formFields        | excluded            |
    When I retrieve latest version of created in CM offer projection for store <storeTwoLanguages> via API
    Then Response status code is 200
    And Single offer API response contains complete information for tombstone projection
      | type              | offer               |
      | store             | <storeTwoLanguages> |
      | deleted           | true                |
      | properties        | excluded            |
      | associations      | excluded            |
      | availabilityRules | excluded            |
      | selectionRules    | excluded            |
      | components        | excluded            |
      | extensions        | excluded            |
      | translations      | excluded            |
      | categories        | excluded            |
      | formFields        | excluded            |
    When I delete all messages from <queueName> queue
    And I enter future date in Disable Date / Time field and save changes
    Then Catalog event JMS message json for store <storeOneLanguages> contains the following values
    # empty values will be replaced in the step with values from entities created in previous steps
      | key    | value               |
      | @class | CatalogEventType    |
      | name   | OFFERS_UPDATED      |
      | guid   | AGGREGATE           |
      | type   | offer               |
      | store  | <storeOneLanguages> |
      | codes  |                     |
    And Catalog event JMS message json for store <storeTwoLanguages> contains the following values
    # empty values will be replaced in the step with values from entities created in previous steps
      | key    | value               |
      | @class | CatalogEventType    |
      | name   | OFFERS_UPDATED      |
      | guid   | AGGREGATE           |
      | type   | offer               |
      | store  | <storeTwoLanguages> |
      | codes  |                     |
    When I retrieve latest version of created in CM offer projection for store <storeOneLanguages> via API
    And Response status code is 200
    And Single offer API response contains complete information for tombstone projection
      | type              | offer               |
      | store             | <storeOneLanguages> |
      | deleted           | false               |
      | properties        | not excluded        |
      | associations      | not excluded        |
      | availabilityRules | not excluded        |
      | selectionRules    | not excluded        |
      | components        | not excluded        |
      | extensions        | not excluded        |
      | translations      | not excluded        |
      | categories        | not excluded        |
      | formFields        | not excluded        |
    When I retrieve latest version of created in CM offer projection for store <storeTwoLanguages> via API
    Then Response status code is 200
    And Single offer API response contains complete information for tombstone projection
      | type              | offer               |
      | store             | <storeTwoLanguages> |
      | deleted           | false               |
      | properties        | not excluded        |
      | associations      | not excluded        |
      | availabilityRules | not excluded        |
      | selectionRules    | not excluded        |
      | components        | not excluded        |
      | extensions        | not excluded        |
      | translations      | not excluded        |
      | categories        | not excluded        |
      | formFields        | not excluded        |
    And I delete all messages from <queueName> queue
    When I delete the newly created product
    And I read Consumer.test.VirtualTopic.ep.catalog message from queue
    Then Catalog event JMS message json for store <storeOneLanguages> contains the following values
    # empty values will be replaced in the step with values from entities created in previous steps
      | key    | value               |
      | @class | CatalogEventType    |
      | name   | OFFERS_UPDATED      |
      | guid   | AGGREGATE           |
      | type   | offer               |
      | store  | <storeOneLanguages> |
      | codes  |                     |
    And Catalog event JMS message json for store <storeTwoLanguages> contains the following values
    # empty values will be replaced in the step with values from entities created in previous steps
      | key    | value               |
      | @class | CatalogEventType    |
      | name   | OFFERS_UPDATED      |
      | guid   | AGGREGATE           |
      | type   | offer               |
      | store  | <storeTwoLanguages> |
      | codes  |                     |
    When I retrieve latest version of created in CM offer projection for store <storeOneLanguages> via API
    Then Response status code is 200
    And Single offer API response contains complete information for tombstone projection
      | type              | offer               |
      | store             | <storeOneLanguages> |
      | deleted           | true                |
      | properties        | excluded            |
      | associations      | excluded            |
      | availabilityRules | excluded            |
      | selectionRules    | excluded            |
      | components        | excluded            |
      | extensions        | excluded            |
      | translations      | excluded            |
      | categories        | excluded            |
      | formFields        | excluded            |
    When I retrieve latest version of created in CM offer projection for store <storeTwoLanguages> via API
    Then Response status code is 200
    And Single offer API response contains complete information for tombstone projection
      | type              | offer               |
      | store             | <storeTwoLanguages> |
      | deleted           | true                |
      | properties        | excluded            |
      | associations      | excluded            |
      | availabilityRules | excluded            |
      | selectionRules    | excluded            |
      | components        | excluded            |
      | extensions        | excluded            |
      | translations      | excluded            |
      | categories        | excluded            |
      | formFields        | excluded            |
    Examples:
      | queueName                             | storeTwoLanguages         | storeOneLanguages              |
      | Consumer.test.VirtualTopic.ep.catalog | SyndicationBilingualStore | SyndicationSingleLanguageStore |
