@cm @regression
Feature: Syndication multiple Product flow tests

  Scenario Outline: Create multiple sku product, check ep.catalog JMS message, check created product projection via API
    Given I have catalog <catalogName>
    And I have stores <storeTwoLanguages>, <storeOneLanguage> connected to <catalogName> catalog
    When I have brand with code <brandCode>
      | English | <brandNameEn> |
      | French  | <brandNameFr> |
    And I have attribute with attribute key <attr1Key> and the following names
      | English | <attr1NameEn> |
      | French  | <attr1NameFr> |
    And I have attribute with attribute key <attr5Key> and the following names
      | English | <attr5NameEn> |
      | French  | <attr5NameFr> |
    And I have attribute with attribute key <attr3Key> and the following names
      | English | <attr3NameEn> |
      | French  | <attr3NameFr> |
    And I have attribute with attribute key <attr4Key> and the following names
      | English | <attr4NameEn> |
      | French  | <attr4NameFr> |
    And I have sku option <firstSkuCode> with the following names
      | English | <firstSkuNameEn> |
      | French  | <firstSkuNameFr> |
    And I have option value <firstSkuFirstValueCode> in sku option <firstSkuCode> with the following names
      | English | <firstSkuFirstValueNameEn> |
      | French  | <firstSkuFirstValueNameFr> |
    And I have option value <firstSkuSecondValueCode> in sku option <firstSkuCode> with the following names
      | English | <firstSkuSecondValueNameEn> |
      | French  | <firstSkuSecondValueNameFr> |
    And I have option value <firstSkuThirdValueCode> in sku option <firstSkuCode> with the following names
      | English | <firstSkuThirdValueNameEn> |
      | French  | <firstSkuThirdValueNameFr> |
    And I have sku option <secondSkuCode> with the following names
      | English | <secondSkuNameEn> |
      | French  | <secondSkuNameFr> |
    And I have option value <secondSkuFirstValueCode> in sku option <secondSkuCode> with the following names
      | English | <secondSkuFirstValueNameEn> |
      | French  | <secondSkuFirstValueNameFr> |
    And I have option value <secondSkuSecondValueCode> in sku option <secondSkuCode> with the following names
      | English | <secondSkuSecondValueNameEn> |
      | French  | <secondSkuSecondValueNameFr> |
    And I have option value <secondSkuThirdValueCode> in sku option <secondSkuCode> with the following names
      | English | <secondSkuThirdValueNameEn> |
      | French  | <secondSkuThirdValueNameFr> |
    And I have sku option <thirdSkuCode> with the following names
      | English | <thirdSkuNameEn> |
      | French  | <thirdSkuNameFr> |
    And I have option value <thirdSkuFirstValueCode> in sku option <thirdSkuCode> with the following names
      | English | <thirdSkuFirstValueNameEn> |
      | French  | <thirdSkuFirstValueNameFr> |
    And I have option value <thirdSkuSecondValueCode> in sku option <thirdSkuCode> with the following names
      | English | <thirdSkuSecondValueNameEn> |
      | French  | <thirdSkuSecondValueNameFr> |
    And I have option value <thirdSkuThirdValueCode> in sku option <thirdSkuCode> with the following names
      | English | <thirdSkuThirdValueNameEn> |
      | French  | <thirdSkuThirdValueNameFr> |
    And I have product type <prodType> with cart item modifier groups
      | productTestGroup1 | productTestGroup2 |
    And I have category type productTestCatType1 with the following attributes
      | <attr1NameEn> | <attr5NameEn> |
    And I have top level category productTestTop1
      | English | <categoryNameEn> |
    When I sign in to CM as admin user
    And I go to Catalog Management
    And I am listening to <queueName> queue
    And I delete all messages from <queueName> queue
    And I create new product with following values
      | catalog       | category         | productName | productType | taxCode | brand         | storeVisible | availability           | shippableType | productCode        | shippingWeight | shippingWidth | shippingLength | shippingHeight | minimumOrderQuantity | skuCode   | enableDateTimeDays | releaseDateTimeDays | attributesCodeList    |
      | <catalogName> | <categoryNameEn> | <prodName>  | <prodType>  | GOODS   | <brandNameEn> | true         | Available on pre order | Shippable     | prodTestMultiProd1 | 1.0            | 2.0           | 3.0            | 4.0            | 2                    | <skuCode> | -1                 | 7                   | <attr1Key>,<attr5Key> |
    And I add sku option in product with following values
      | prodSkuCode        | <firstProdSkuCode>        |
      | <firstSkuCode>     | <firstSkuFirstValueCode>  |
      | <secondSkuCode>    | <secondSkuFirstValueCode> |
      | <thirdSkuCode>     | <thirdSkuFirstValueCode>  |
      | taxCode            | N/A                       |
      | weight             | 1                         |
      | width              | 2                         |
      | length             | 3                         |
      | height             | 4                         |
      | shippableType      | Shippable                 |
      | enableDateTimeDays | -1                        |
      | attributesCodeList | <attr3Key>,<attr4Key>     |
    And I add sku option in product with following values
      | prodSkuCode         | <secondProdSkuCode>        |
      | <firstSkuCode>      | <firstSkuSecondValueCode>  |
      | <secondSkuCode>     | <secondSkuSecondValueCode> |
      | <thirdSkuCode>      | <thirdSkuSecondValueCode>  |
      | taxCode             | DIGITAL                    |
      | shippableType       | Digital Asset              |
      | disableDateTimeDays | 30                         |
    And I add sku option in product with following values
      | prodSkuCode         | <thirdProdSkuCode>        |
      | <firstSkuCode>      | <firstSkuThirdValueCode>  |
      | <secondSkuCode>     | <secondSkuThirdValueCode> |
      | <thirdSkuCode>      | <thirdSkuThirdValueCode>  |
      | taxCode             | DIGITAL                   |
      | shippableType       | Digital Asset             |
      | enableDateTimeDays  | -2                        |
      | disableDateTimeDays | -1                        |
    And  I finish creating sku product
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
    And Single offer API response contains complete information for projection multiple sku product with 2 languages
    # empty values will be replaced in the step with values from entities created in previous steps
      | type                   | offer               |
      | code                   |                     |
      | store                  | <storeTwoLanguages> |
      | deleted                | false               |
      | OFFER_TYPE             | <prodType>          |
      | NOT_SOLD_SEPARATELY    | false               |
      | MINIMUM_ORDER_QUANTITY | 2                   |
      | selectionType          | ITEM                |
      | quantity               | 1                   |
      | canDiscover            | HAS_STOCK,PRE_ORDER |
      | canView                | ALWAYS              |
      | canAddToCart           | HAS_STOCK,PRE_ORDER |
      | enableDateTime         |                     |
      | disableDateTime        |                     |
      | releaseDateTime        |                     |
      | componentsList         |                     |
      | formFields             |                     |
      | displayNameEn          | <prodName>          |
      | displayNameFr          | <prodName>          |
      | brandDisplayNameEn     | <brandNameEn>       |
      | brandDisplayNameFr     | <brandNameFr>       |
      | brandNameEn            | <brandCode>         |
      | brandNameFr            | <brandCode>         |
      | firstOptionsName       | <firstSkuCode>      |
      | secondOptionsName      | <secondSkuCode>     |
      | thirdOptionsName       | <thirdSkuCode>      |
      | firstDetailsName       | <attr1Key>          |
      | secondDetailsName      | <attr5Key>          |
      | firstProdSkuCode       | <firstProdSkuCode>  |
      | secondProdSkuCode      | <secondProdSkuCode> |
    And Single offer API response contains complete information for item with code <firstProdSkuCode> in product projection with 2 languages
    # empty values will be replaced in the step with values from entities created in previous steps
      | ITEM_TYPE                  | PHYSICAL                    |
      | TAX_CODE                   | GOODS                       |
      | enableDateTime             |                             |
      | disableDateTime            |                             |
      | weight                     | 1                           |
      | width                      | 2                           |
      | length                     | 3                           |
      | height                     | 4                           |
      | unitsWeight                | KG                          |
      | unitsLength                | CM                          |
      | firstSkuCode               | <firstSkuCode>              |
      | firstDisplayNameEn         | <firstSkuNameEn>            |
      | firstValue                 | <firstSkuFirstValueCode>    |
      | firstDisplayValueEn        | <firstSkuFirstValueNameEn>  |
      | secondSkuCode              | <secondSkuCode>             |
      | secondDisplayNameEn        | <secondSkuNameEn>           |
      | secondValue                | <secondSkuFirstValueCode>   |
      | secondDisplayValueEn       | <secondSkuFirstValueNameEn> |
      | thirdSkuCode               | <thirdSkuCode>              |
      | thirdDisplayNameEn         | <thirdSkuNameEn>            |
      | thirdValue                 | <thirdSkuFirstValueCode>    |
      | thirdDisplayValueEn        | <thirdSkuFirstValueNameEn>  |
      | firstDetailsName           | <attr3Key>                  |
      | firstDetailsDisplayNameEn  | <attr3NameEn>               |
      | secondDetailsName          | <attr4Key>                  |
      | secondDetailsDisplayNameEn | <attr4NameEn>               |
      | firstDisplayNameFr         | <firstSkuNameFr>            |
      | firstDisplayValueFr        | <firstSkuFirstValueNameFr>  |
      | secondDisplayNameFr        | <secondSkuNameFr>           |
      | secondDisplayValueFr       | <secondSkuFirstValueNameFr> |
      | thirdDisplayNameFr         | <thirdSkuNameFr>            |
      | thirdDisplayValueFr        | <thirdSkuFirstValueNameFr>  |
      | firstDetailsDisplayNameFr  | <attr3NameFr>               |
      | secondDetailsDisplayNameFr | <attr4NameFr>               |
    And Single offer API response contains complete information for item with code <secondProdSkuCode> in product projection with 2 languages
    # empty values will be replaced in the step with values from entities created in previous steps
      | ITEM_TYPE            | DIGITAL                      |
      | TAX_CODE             | DIGITAL                      |
      | enableDateTime       |                              |
      | disableDateTime      |                              |
      | weight               |                              |
      | width                |                              |
      | length               |                              |
      | height               |                              |
      | unitsWeight          |                              |
      | unitsLength          |                              |
      | firstSkuCode         | <firstSkuCode>               |
      | firstDisplayNameEn   | <firstSkuNameEn>             |
      | firstValue           | <firstSkuSecondValueCode>    |
      | firstDisplayValueEn  | <firstSkuSecondValueNameEn>  |
      | secondSkuCode        | <secondSkuCode>              |
      | secondDisplayNameEn  | <secondSkuNameEn>            |
      | secondValue          | <secondSkuSecondValueCode>   |
      | secondDisplayValueEn | <secondSkuSecondValueNameEn> |
      | thirdSkuCode         | <thirdSkuCode>               |
      | thirdDisplayNameEn   | <thirdSkuNameEn>             |
      | thirdValue           | <thirdSkuSecondValueCode>    |
      | thirdDisplayValueEn  | <thirdSkuSecondValueNameEn>  |
      | firstDisplayNameFr   | <firstSkuNameFr>             |
      | firstDisplayValueFr  | <firstSkuSecondValueNameFr>  |
      | secondDisplayNameFr  | <secondSkuNameFr>            |
      | secondDisplayValueFr | <secondSkuSecondValueNameFr> |
      | thirdDisplayNameFr   | <thirdSkuNameFr>             |
      | thirdDisplayValueFr  | <thirdSkuSecondValueNameFr>  |
    When I retrieve latest version of created in CM offer projection for store <storeOneLanguage> via API
    Then Response status code is 200
    And Single offer API response contains complete information for projection multiple sku product with 1 languages
    # empty values will be replaced in the step with values from entities created in previous steps
      | type                   | offer               |
      | code                   |                     |
      | store                  | <storeOneLanguage>  |
      | deleted                | false               |
      | OFFER_TYPE             | <prodType>          |
      | NOT_SOLD_SEPARATELY    | false               |
      | MINIMUM_ORDER_QUANTITY | 2                   |
      | selectionType          | ITEM                |
      | quantity               | 1                   |
      | canDiscover            | HAS_STOCK,PRE_ORDER |
      | canView                | ALWAYS              |
      | canAddToCart           | HAS_STOCK,PRE_ORDER |
      | enableDateTime         |                     |
      | disableDateTime        |                     |
      | releaseDateTime        |                     |
      | componentsList         |                     |
      | formFields             |                     |
      | displayNameFr          | <prodName>          |
      | brandDisplayNameFr     | <brandNameFr>       |
      | brandNameFr            | <brandCode>         |
      | firstOptionsName       | <firstSkuCode>      |
      | secondOptionsName      | <secondSkuCode>     |
      | thirdOptionsName       | <thirdSkuCode>      |
      | optionsDisplayNameFr   | <firstSkuNameFr>    |
      | optionsDisplayName1Fr  | <secondSkuNameFr>   |
      | optionsDisplayName2Fr  | <thirdSkuNameFr>    |
      | firstDetailsName       | <attr1Key>          |
      | secondDetailsName      | <attr5Key>          |
      | firstProdSkuCode       | <firstProdSkuCode>  |
      | secondProdSkuCode      | <secondProdSkuCode> |
    And Single offer API response contains complete information for item with code <firstProdSkuCode> in product projection with 1 languages
    # empty values will be replaced in the step with values from entities created in previous steps
      | ITEM_TYPE                  | PHYSICAL                    |
      | TAX_CODE                   | GOODS                       |
      | enableDateTime             |                             |
      | disableDateTime            |                             |
      | weight                     | 1                           |
      | width                      | 2                           |
      | length                     | 3                           |
      | height                     | 4                           |
      | unitsWeight                | KG                          |
      | unitsLength                | CM                          |
      | firstSkuCode               | <firstSkuCode>              |
      | firstDisplayNameFr         | <firstSkuNameFr>            |
      | firstValue                 | <firstSkuFirstValueCode>    |
      | firstDisplayValueFr        | <firstSkuFirstValueNameFr>  |
      | secondSkuCode              | <secondSkuCode>             |
      | secondDisplayNameFr        | <secondSkuNameFr>           |
      | secondValue                | <secondSkuFirstValueCode>   |
      | secondDisplayValueFr       | <secondSkuFirstValueNameFr> |
      | thirdSkuCode               | <thirdSkuCode>              |
      | thirdDisplayNameFr         | <thirdSkuNameFr>            |
      | thirdValue                 | <thirdSkuFirstValueCode>    |
      | thirdDisplayValueFr        | <thirdSkuFirstValueNameFr>  |
      | firstDetailsName           | <attr3Key>                  |
      | firstDetailsDisplayNameFr  | <attr3NameFr>               |
      | secondDetailsName          | <attr4Key>                  |
      | secondDetailsDisplayNameFr | <attr4NameFr>               |
    And Single offer API response contains complete information for item with code <secondProdSkuCode> in product projection with 1 languages
    # empty values will be replaced in the step with values from entities created in previous steps
      | ITEM_TYPE            | DIGITAL                      |
      | TAX_CODE             | DIGITAL                      |
      | enableDateTime       |                              |
      | disableDateTime      |                              |
      | weight               |                              |
      | width                |                              |
      | length               |                              |
      | height               |                              |
      | unitsWeight          |                              |
      | unitsLength          |                              |
      | firstSkuCode         | <firstSkuCode>               |
      | firstDisplayNameFr   | <firstSkuNameFr>             |
      | firstValue           | <firstSkuSecondValueCode>    |
      | firstDisplayValueFr  | <firstSkuSecondValueNameFr>  |
      | secondSkuCode        | <secondSkuCode>              |
      | secondDisplayNameFr  | <secondSkuNameFr>            |
      | secondValue          | <secondSkuSecondValueCode>   |
      | secondDisplayValueFr | <secondSkuSecondValueNameFr> |
      | thirdSkuCode         | <thirdSkuCode>               |
      | thirdDisplayNameFr   | <thirdSkuNameFr>             |
      | thirdValue           | <thirdSkuSecondValueCode>    |
      | thirdDisplayValueFr  | <thirdSkuSecondValueNameFr>  |
    When I update sku in product with following values
      | skuCode       | <firstProdSkuCode> |
      | enableDate    | -7                 |
      | taxCode       | SERVICE            |
      | shippableType | Digital Asset      |
      | attributeCode | <attr3Key>         |
    And I save my changes
    And I close opened previously sku editor for sku <firstProdSkuCode>
    And I update sku in product with following values
      | skuCode     | <secondProdSkuCode> |
      | enableDate  | -4                  |
      | disableDate | -3                  |
    And I save my changes
    And I close opened previously sku editor for sku <secondProdSkuCode>
    And I update sku in product with following values
      | skuCode     | <thirdProdSkuCode> |
      | enableDate  | -1                 |
      | disableDate | 30                 |
    And I save my changes
    And I close opened previously sku editor for sku <thirdProdSkuCode>
    When I retrieve latest version of created in CM offer projection for store <storeTwoLanguages> via API
    Then Response status code is 200
    And Single offer API response contains product sku codes for projection multiple sku product
      | firstProdSkuCode | <firstProdSkuCode> |
      | thirdProdSkuCode | <thirdProdSkuCode> |
    And Single offer API response contains complete information for item with code <firstProdSkuCode> with 2 languages after update sku options
    # empty values will be replaced in the step with values from entities created in previous steps
      | ITEM_TYPE                 | DIGITAL       |
      | TAX_CODE                  | SERVICE       |
      | enableDateTime            |               |
      | weight                    |               |
      | width                     |               |
      | length                    |               |
      | height                    |               |
      | unitsWeight               |               |
      | unitsLength               |               |
      | firstDetailsName          | <attr4Key>    |
      | firstDetailsDisplayNameEn | <attr4NameEn> |
      | firstDetailsDisplayNameFr | <attr4NameFr> |
    And Single offer API response contains complete information for item with code <thirdProdSkuCode> in product projection with 2 languages
    # empty values will be replaced in the step with values from entities created in previous steps
      | ITEM_TYPE            | DIGITAL                     |
      | TAX_CODE             | DIGITAL                     |
      | enableDateTime       |                             |
      | disableDateTime      |                             |
      | weight               |                             |
      | width                |                             |
      | length               |                             |
      | height               |                             |
      | unitsWeight          |                             |
      | unitsLength          |                             |
      | firstSkuCode         | <firstSkuCode>              |
      | firstDisplayNameEn   | <firstSkuNameEn>            |
      | firstValue           | <firstSkuThirdValueCode>    |
      | firstDisplayValueEn  | <firstSkuThirdValueNameEn>  |
      | secondSkuCode        | <secondSkuCode>             |
      | secondDisplayNameEn  | <secondSkuNameEn>           |
      | secondValue          | <secondSkuThirdValueCode>   |
      | secondDisplayValueEn | <secondSkuThirdValueNameEn> |
      | thirdSkuCode         | <thirdSkuCode>              |
      | thirdDisplayNameEn   | <thirdSkuNameEn>            |
      | thirdValue           | <thirdSkuThirdValueCode>    |
      | thirdDisplayValueEn  | <thirdSkuThirdValueNameEn>  |
      | firstDisplayNameFr   | <firstSkuNameFr>            |
      | firstDisplayValueFr  | <firstSkuThirdValueNameFr>  |
      | secondDisplayNameFr  | <secondSkuNameFr>           |
      | secondDisplayValueFr | <secondSkuThirdValueNameFr> |
      | thirdDisplayNameFr   | <thirdSkuNameFr>            |
      | thirdDisplayValueFr  | <thirdSkuThirdValueNameFr>  |
    When I retrieve latest version of created in CM offer projection for store <storeOneLanguage> via API
    Then Response status code is 200
    And Single offer API response contains product sku codes for projection multiple sku product
      | firstProdSkuCode | <firstProdSkuCode> |
      | thirdProdSkuCode | <thirdProdSkuCode> |
    And Single offer API response contains complete information for item with code <firstProdSkuCode> with 1 languages after update sku options
    # empty values will be replaced in the step with values from entities created in previous steps
      | ITEM_TYPE                 | DIGITAL                    |
      | TAX_CODE                  | SERVICE                    |
      | enableDateTime            |                            |
      | weight                    |                            |
      | width                     |                            |
      | length                    |                            |
      | height                    |                            |
      | unitsWeight               |                            |
      | unitsLength               |                            |
      | firstSkuCode              | <firstSkuCode>             |
      | firstDisplayNameFr        | <firstSkuNameFr>           |
      | firstValue                | <firstSkuFirstValueCode>   |
      | firstDisplayValueFr       | <firstSkuFirstValueNameFr> |
      | firstDetailsName          | <attr4Key>                 |
      | firstDetailsDisplayNameFr | <attr4NameFr>              |
    And Single offer API response contains complete information for item with code <thirdProdSkuCode> in product projection with 1 languages
    # empty values will be replaced in the step with values from entities created in previous steps
      | ITEM_TYPE            | DIGITAL                     |
      | TAX_CODE             | DIGITAL                     |
      | enableDateTime       |                             |
      | disableDateTime      |                             |
      | weight               |                             |
      | width                |                             |
      | length               |                             |
      | height               |                             |
      | unitsWeight          |                             |
      | unitsLength          |                             |
      | firstSkuCode         | <firstSkuCode>              |
      | firstDisplayNameFr   | <firstSkuNameFr>            |
      | firstValue           | <firstSkuThirdValueCode>    |
      | firstDisplayValueFr  | <firstSkuThirdValueNameFr>  |
      | secondSkuCode        | <secondSkuCode>             |
      | secondDisplayNameFr  | <secondSkuNameFr>           |
      | secondValue          | <secondSkuThirdValueCode>   |
      | secondDisplayValueFr | <secondSkuThirdValueNameFr> |
      | thirdSkuCode         | <thirdSkuCode>              |
      | thirdDisplayNameFr   | <thirdSkuNameFr>            |
      | thirdValue           | <thirdSkuThirdValueCode>    |
      | thirdDisplayValueFr  | <thirdSkuThirdValueNameFr>  |
    When I update sku in product with following values
      | skuCode     | <firstProdSkuCode> |
      | enableDate  | -8                 |
      | disableDate | -7                 |
    And I save my changes
    And I close opened previously sku editor for sku <firstProdSkuCode>
    When I update sku in product with following values
      | skuCode     | <thirdProdSkuCode> |
      | enableDate  | -3                 |
      | disableDate | -2                 |
    And I save my changes
    And I close opened previously sku editor for sku <thirdProdSkuCode>
    When I retrieve latest version of created in CM offer projection for store <storeTwoLanguages> via API
    Then Response status code is 200
    And Single offer API response contains empty items for multiple sku product projection without sku option
    # empty values will be replaced in the step with values from entities created in previous steps
      | items |  |
    When I retrieve latest version of created in CM offer projection for store <storeOneLanguage> via API
    Then Response status code is 200
    And Single offer API response contains empty items for multiple sku product projection without sku option
    # empty values will be replaced in the step with values from entities created in previous steps
      | items |  |

    Examples:
      | catalogName                    | prodType                        | categoryNameEn    | queueName                             | storeTwoLanguages         | storeOneLanguage               | skuCode                | prodName | attr1Key              | attr5Key              | attr3Key              | attr4Key              | attr1NameEn             | attr1NameFr             | attr5NameEn             | attr5NameFr             | attr3NameEn             | attr3NameFr             | attr4NameEn             | attr4NameFr             | brandNameEn         | brandNameFr         | brandCode         | firstSkuNameEn    | firstSkuNameFr    | storeTwoLanguages         | storeOneLanguage               | firstSkuFirstValueCode | firstSkuSecondValueCode | firstSkuThirdValueCode | secondSkuFirstValueCode | secondSkuSecondValueCode | secondSkuThirdValueCode | thirdSkuFirstValueCode | thirdSkuSecondValueCode | thirdSkuThirdValueCode | firstSkuFirstValueNameEn | firstSkuFirstValueNameFr | firstSkuSecondValueNameEn | firstSkuSecondValueNameFr | firstSkuThirdValueNameEn | firstSkuThirdValueNameFr | secondSkuFirstValueNameEn | secondSkuFirstValueNameFr | secondSkuSecondValueNameEn | secondSkuSecondValueNameFr | secondSkuThirdValueNameEn | secondSkuThirdValueNameFr | thirdSkuFirstValueNameEn | thirdSkuFirstValueNameFr | thirdSkuSecondValueNameEn | thirdSkuSecondValueNameFr | thirdSkuThirdValueNameEn | thirdSkuThirdValueNameFr | secondSkuNameEn   | secondSkuNameFr   | thirdSkuNameEn    | thirdSkuNameFr    | firstProdSkuCode        | secondProdSkuCode       | thirdProdSkuCode        | firstSkuCode    | secondSkuCode   | thirdSkuCode    |
      | Syndication Two Stores Catalog | productTestMultiSkuProductType1 | productTestTop1En | Consumer.test.VirtualTopic.ep.catalog | SyndicationBilingualStore | SyndicationSingleLanguageStore | prodTestMultiProd1_sku | prodName | productTestAttribute1 | productTestAttribute5 | productTestAttribute3 | productTestAttribute4 | productTestAttribute1En | productTestAttribute1Fr | productTestAttribute5En | productTestAttribute5Fr | productTestAttribute3En | productTestAttribute3Fr | productTestAttribute4En | productTestAttribute4Fr | productTestBrand1En | productTestBrand1Fr | productTestBrand1 | prodTestOption1En | prodTestOption1Fr | SyndicationBilingualStore | SyndicationSingleLanguageStore | prodTestOption1v1      | prodTestOption1v2       | prodTestOption1v3      | prodTestOption2v1       | prodTestOption2v2        | prodTestOption2v3       | prodTestOption3v1      | prodTestOption3v2       | prodTestOption3v3      | prodTestOption1v1En      | prodTestOption1v1Fr      | prodTestOption1v2En       | prodTestOption1v2Fr       | prodTestOption1v3En      | prodTestOption1v3Fr      | prodTestOption2v1En       | prodTestOption2v1Fr       | prodTestOption2v2En        | prodTestOption2v2Fr        | prodTestOption2v3En       | prodTestOption2v3Fr       | prodTestOption3v1En      | prodTestOption3v1Fr      | prodTestOption3v2En       | prodTestOption3v2Fr       | prodTestOption3v3En      | prodTestOption3v3Fr      | prodTestOption2En | prodTestOption2Fr | prodTestOption3En | prodTestOption3Fr | prodTestMultiProd1_sku1 | prodTestMultiProd1_sku2 | prodTestMultiProd1_sku3 | prodTestOption1 | prodTestOption2 | prodTestOption3 |
