@cm @regression
Feature: Syndication Product flow tests

  Scenario Outline: Create Product, check ep.catalog JMS message, check created product projection via API
    Given I have catalog <catalogName>
    And I have stores <storeTwoLanguages>, <storeOneLanguage> connected to <catalogName> catalog
    When I have brand with code <brandCode>
      | English | <brandNameEn> |
      | French  | <brandNameFr> |
    And I have attribute with attribute key <attr1Key> and the following names
      | English | <attr1NameEn> |
      | French  | <attr1NameFr> |
    And I have attribute with attribute key productTestAttribute2 and the following names
      | English | <attr2NameEn>           |
      | French  | productTestAttribute2Fr |
    And I have product type <prodType> with cart item modifier groups
      | productTestGroup1 | productTestGroup2 |
    And I have category type productTestCatType1 with the following attributes
      | <attr1NameEn> | <attr2NameEn> |
    And I have top level category productTestTop1
      | English | <categoryNameEn> |
    And I have product with code <prod2> and the following parameters
      | catalog      | <catalogName>    |
      | category     | <categoryNameEn> |
      | productName  | prodTestProd2En  |
      | productType  | <prodType>       |
      | storeVisible | true             |
      | enableDate   |                  |
      | disableDate  |                  |
    And I have product with code <prod3> and the following parameters
      | catalog      | <catalogName>    |
      | category     | <categoryNameEn> |
      | productName  | prodTestProd3En  |
      | productType  | <prodType>       |
      | storeVisible | true             |
      | enableDate   |                  |
      | disableDate  |                  |
    And I have product with code <prod4> and the following parameters
      | catalog      | <catalogName>    |
      | category     | <categoryNameEn> |
      | productName  | prodTestProd4En  |
      | productType  | <prodType>       |
      | storeVisible | true             |
      | enableDate   |                  |
      | disableDate  |                  |
    And I have product with code <prod5> and the following parameters
      | catalog      | <catalogName>    |
      | category     | <categoryNameEn> |
      | productName  | prodTestProd5En  |
      | productType  | <prodType>       |
      | storeVisible | false            |
      | enableDate   |                  |
      | disableDate  |                  |
    And I have product with code <prod6> and the following parameters
      | catalog      | <catalogName>        |
      | category     | <categoryNameEn>     |
      | productName  | prodTestProd6En      |
      | productType  | <prodType>           |
      | storeVisible | true                 |
      | enableDate   | Aug 1, 2019 2:51 PM  |
      | disableDate  | Aug 11, 2019 2:51 PM |
    When I sign in to CM as admin user
    And I go to Catalog Management
    And I am listening to <queueName> queue
    And I delete all messages from <queueName> queue
    When I create new product with following values
      | catalog       | category         | productName | productType | taxCode | brand         | storeVisible | availability               | shippableType | productCode   | shippingWeight | shippingWidth | shippingLength | shippingHeight | minimumOrderQuantity | skuCode   | enableDateTimeDays |
      | <catalogName> | <categoryNameEn> | <prodName>  | <prodType>  | <tax>   | <brandNameEn> | true         | Available only if in stock | Shippable     | prodTestProd1 | 1.0            | 2.0           | 3.0            | 4.0            | 2                    | <skuCode> | -1                 |
    When I read <queueName> message from queue
    Then Catalog event JMS message json for store <storeTwoLanguages> contains the following values
    # empty values will be replaced in the step with values from entities created in previous steps
      | key    | value               |
      | @class | CatalogEventType    |
      | name   | OFFERS_UPDATED      |
      | guid   | AGGREGATE           |
      | type   | offer               |
      | store  | <storeTwoLanguages> |
      | codes  |                     |
    And Catalog event JMS message json for store <storeOneLanguage> contains the following values
    # empty values will be replaced in the step with values from entities created in previous steps
      | key    | value              |
      | @class | CatalogEventType   |
      | name   | OFFERS_UPDATED     |
      | guid   | AGGREGATE          |
      | type   | offer              |
      | store  | <storeOneLanguage> |
      | codes  |                    |
    When I retrieve latest version of created in CM offer projection for store <storeTwoLanguages> via API
    Then Response status code is 200
    And Single offer API response contains complete information for product projection with 2 languages
    # empty values will be replaced in the step with values from entities created in previous steps
      | type                    | offer                |
      | code                    |                      |
      | store                   | <storeTwoLanguages>  |
      | deleted                 | false                |
      | OFFER_TYPE              | <prodType>           |
      | NOT_SOLD_SEPARATELY     | false                |
      | MINIMUM_ORDER_QUANTITY  | 2                    |
      | selectionType           | NONE                 |
      | quantity                | 0                    |
      | canDiscover             | HAS_STOCK            |
      | canView                 | ALWAYS               |
      | canAddToCart            | HAS_STOCK            |
      | displayNameEn           | <prodName>           |
      | displayNameFr           | <prodName>           |
      | enableDateTime          |                      |
      | disableDateTime         |                      |
      | brandDisplayNameEn      | <brandNameEn>        |
      | brandDisplayNameFr      | <brandNameFr>        |
      | brandNameEn             | <brandCode>          |
      | brandNameFr             | <brandCode>          |
      | itemCode                | <skuCode>            |
      | ITEM_TYPE               | PHYSICAL             |
      | TAX_CODE                | <tax>                |
      | propertiesWeight        | 1.0                  |
      | propertiesWidth         | 2.0                  |
      | propertiesLength        | 3.0                  |
      | propertiesHeight        | 4.0                  |
      | propertiesUnitsWeight   | KG                   |
      | propertiesUnitsLength   | CM                   |
      | itemsEnableDateTime     |                      |
      | itemsDisableDateTime    |                      |
      | extensions              |                      |
      | componentsList          |                      |
      | categoryPath            |                      |
      | categoryCode            |                      |
      | categoryDefault         | true                 |
      | categoryFeatured        |                      |
      | categoryEnableDateTime  | Aug 1, 2037 9:45 AM  |
      | categoryDisableDateTime | Aug 31, 2037 9:45 AM |
      | formFields              |                      |
    When I retrieve latest version of created in CM offer projection for store <storeOneLanguage> via API
    Then Response status code is 200
    And Single offer API response contains complete information for projection with 1 languages
    # empty values will be replaced in the step with values from entities created in previous steps
      | type                    | offer                |
      | code                    |                      |
      | store                   | <storeOneLanguage>   |
      | deleted                 | false                |
      | OFFER_TYPE              | <prodType>           |
      | NOT_SOLD_SEPARATELY     | false                |
      | MINIMUM_ORDER_QUANTITY  | 2                    |
      | selectionType           | NONE                 |
      | quantity                | 0                    |
      | canDiscover             | HAS_STOCK            |
      | canView                 | ALWAYS               |
      | canAddToCart            | HAS_STOCK            |
      | displayNameFr           | <prodName>           |
      | enableDateTime          |                      |
      | disableDateTime         |                      |
      | brandDisplayNameFr      | <brandNameFr>        |
      | brandNameFr             | <brandCode>          |
      | itemCode                | <skuCode>            |
      | ITEM_TYPE               | PHYSICAL             |
      | TAX_CODE                | <tax>                |
      | propertiesWeight        | 1.0                  |
      | propertiesWidth         | 2.0                  |
      | propertiesLength        | 3.0                  |
      | propertiesHeight        | 4.0                  |
      | propertiesUnitsWeight   | KG                   |
      | propertiesUnitsLength   | CM                   |
      | itemsEnableDateTime     |                      |
      | itemsDisableDateTime    |                      |
      | itemsExtensions         |                      |
      | extensions              |                      |
      | componentsList          |                      |
      | categoryPath            |                      |
      | categoryCode            |                      |
      | categoryDefault         | true                 |
      | categoryFeatured        |                      |
      | categoryEnableDateTime  | Aug 1, 2037 9:45 AM  |
      | categoryDisableDateTime | Aug 31, 2037 9:45 AM |
      | formFields              |                      |
    And I update product with following values
      | productName | minimumOrderQuantity | taxCode | notSoldSeparately | shippableType | enableDateTimeDays | disableDateTimeDays |
      | <prodName>  | 3                    | DIGITAL | true              | Digital Asset | -2                 | 7                   |
    And I edit <attr1Key> attribute value for product
    And I select merchandising associations tab
    And I add code <prod2> to merchandising association <CrossSell>
    And I add code <prod3> to merchandising association <CrossSell>
    And I save my changes
    And I add code <prod5> to merchandising association <UpSell>
    And I save my changes
    And I add code <prod6> to merchandising association <Warranty>
    And I save my changes
    And I add code <prod4> to merchandising and with values
      | disableDate | -7          |
      | enableDate  | -9          |
      | association | <Accessory> |
    And I save my changes
    And I delete all messages from <queueName> queue
    And I add code <prod2> to merchandising association <Replacement>
    And I add code <prod3> to merchandising association <Replacement>
    And I save my changes
    And I read <queueName> message from queue
    Then Catalog event JMS message json for store <storeTwoLanguages> contains the following values
    # empty values will be replaced in the step with values from entities created in previous steps
      | key    | value               |
      | @class | CatalogEventType    |
      | name   | OFFERS_UPDATED      |
      | guid   | AGGREGATE           |
      | type   | offer               |
      | store  | <storeTwoLanguages> |
      | codes  |                     |
    And Catalog event JMS message json for store <storeOneLanguage> contains the following values
    # empty values will be replaced in the step with values from entities created in previous steps
      | key    | value              |
      | @class | CatalogEventType   |
      | name   | OFFERS_UPDATED     |
      | guid   | AGGREGATE          |
      | type   | offer              |
      | store  | <storeOneLanguage> |
      | codes  |                    |
    When I retrieve latest version of created in CM offer projection for store <storeTwoLanguages> via API
    Then Response status code is 200
    And Single offer API response contains complete information for projection with 2 languages after update
    # empty values will be replaced in the step with values from entities created in previous steps
      | NOT_SOLD_SEPARATELY           | true          |
      | MINIMUM_ORDER_QUANTITY        | 3             |
      | canDiscover                   |               |
      | canView                       | ALWAYS        |
      | canAddToCart                  |               |
      | enableDateTime                |               |
      | disableDateTime               |               |
      | detailsNameEn                 | <attr1Key>    |
      | detailsDisplayNameEn          | <attr1NameEn> |
      | displayNameFr                 | <prodName>    |
      | detailsNameFr                 | <attr1Key>    |
      | detailsDisplayNameFr          | <attr1NameFr> |
      | associationsCrossSellFirst    | <prod2>       |
      | associationsCrossSellSecond   | <prod3>       |
      | associationsUpSell            |               |
      | associationsWarranty          |               |
      | associationsAccessory         |               |
      | associationsReplacementFirst  | <prod2>       |
      | associationsReplacementSecond | <prod3>       |
      | ITEM_TYPE                     | DIGITAL       |
      | TAX_CODE                      | DIGITAL       |
      | itemsEnableDateTime           |               |
      | itemsDisableDateTime          |               |
      | propertiesWeight              |               |
      | propertiesWidth               |               |
      | propertiesLength              |               |
      | propertiesHeight              |               |
      | propertiesUnitsWeight         |               |
      | propertiesUnitsLength         |               |
    When I retrieve latest version of created in CM offer projection for store <storeOneLanguage> via API
    Then Response status code is 200
    And Single offer API response contains complete information for projection with 1 languages after update
    # empty values will be replaced in the step with values from entities created in previous steps
      | NOT_SOLD_SEPARATELY           | true          |
      | MINIMUM_ORDER_QUANTITY        | 3             |
      | canDiscover                   |               |
      | canView                       | ALWAYS        |
      | canAddToCart                  |               |
      | enableDateTime                |               |
      | disableDateTime               |               |
      | displayNameFr                 | <prodName>    |
      | detailsNameFr                 | <attr1Key>    |
      | detailsDisplayNameFr          | <attr1NameFr> |
      | associationsCrossSellFirst    | <prod2>       |
      | associationsCrossSellSecond   | <prod3>       |
      | associationsUpSell            |               |
      | associationsWarranty          |               |
      | associationsAccessory         |               |
      | associationsReplacementFirst  | <prod2>       |
      | associationsReplacementSecond | <prod3>       |
      | ITEM_TYPE                     | DIGITAL       |
      | TAX_CODE                      | DIGITAL       |
      | itemsEnableDateTime           |               |
      | itemsDisableDateTime          |               |
      | propertiesWeight              |               |
      | propertiesWidth               |               |
      | propertiesLength              |               |
      | propertiesHeight              |               |
      | propertiesUnitsWeight         |               |
      | propertiesUnitsLength         |               |
    When I move product <prod3> down to merchandising association <CrossSell>
    And I delete code <prod2> merchandising association <Replacement>
    And I update product <prod3> merchandising association <Replacement> with values
      | disableDate | -7 |
      | enableDate  | -9 |
    And I save my changes
    And I delete all messages from <queueName> queue
    And I add code <prod2> to merchandising association <DependentItem>
    And I save my changes
    When I retrieve latest version of created in CM offer projection for store <storeTwoLanguages> via API
    Then Response status code is 200
    And Single offer API response contains complete information for projection after update in merchandising association
      | associationsCrossSellFirst    | <prod3> |
      | associationsCrossSellSecond   | <prod2> |
      | associationsDependentItem     | <prod4> |
      | associationsReplacementFirst  | <prod2> |
      | associationsReplacementSecond | <prod3> |
    When I retrieve latest version of created in CM offer projection for store <storeOneLanguage> via API
    Then Response status code is 200
    And Single offer API response contains complete information for projection after update in merchandising association
      | associationsCrossSellFirst    | <prod3> |
      | associationsCrossSellSecond   | <prod2> |
      | associationsDependentItem     | <prod4> |
      | associationsReplacementFirst  | <prod2> |
      | associationsReplacementSecond | <prod3> |

    Examples:
      | catalogName                    | prodType                         | queueName                             | storeTwoLanguages         | storeOneLanguage               | skuCode           | prodName        | CrossSell  | UpSell  | Warranty | Accessory | Replacement | DependentItem  | attr1Key              | attr1NameEn             | attr1NameFr             | attr2NameEn             | prod2         | prod3         | prod4         | prod5         | prod6         | brandNameEn         | brandNameFr         | brandCode         | tax   | categoryNameEn    |
      | Syndication Two Stores Catalog | productTestSingleSkuProductType1 | Consumer.test.VirtualTopic.ep.catalog | SyndicationBilingualStore | SyndicationSingleLanguageStore | prodTestProd1_sku | prodTestProd1En | Cross Sell | Up Sell | Warranty | Accessory | Replacement | Dependent Item | productTestAttribute1 | productTestAttribute1En | productTestAttribute1Fr | productTestAttribute2En | prodTestProd2 | prodTestProd3 | prodTestProd4 | prodTestProd5 | prodTestProd6 | productTestBrand1En | productTestBrand1Fr | productTestBrand1 | GOODS | productTestTop1En |




