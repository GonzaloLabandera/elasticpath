@cm @regression
Feature: Product category flow tests


  #@cleanupAllProducts @cleanUpAllCategories
  Scenario Outline: Create a product which is included in more than one category, check Category projection via API, check Offer projection via API
    Given I have catalog Syndication Two Stores Catalog
    And I have stores <storeTwoLanguages>, <storeOneLanguage> connected to <catalogName> catalog
    When I have brand with code <brandCode>
      | English | <brandNameEn> |
      | French  | <brandNameFr> |
    And I have product type <prodType> with cart item modifier groups
      | productTestGroup1 | productTestGroup2 |
    And I have category type productTestCatType1 with the following attributes
      | <attr1NameEn> | <attr2NameEn> |
    And I have top level category productTestTop1
      | English | <categoryTop1NameEn> |
    And I have top level category <categoryTop2>
      | English | <categoryTop2NameEn> |
    And I have subcategory <categorySub1Top2> with the following parameters
      | defaultLanguage | English                  |
      | defaultName     | <categorySub1Top2NameEn> |
      | parentName      | <categoryTop2NameEn>     |
    And I have subcategory <categorySub2Top2> with the following parameters
      | defaultLanguage | English                  |
      | defaultName     | <categorySub2Top2NameEn> |
      | parentName      | <categoryTop2NameEn>     |
    When I sign in to CM as admin user
    And I go to Catalog Management
    And I am listening to <queueName> queue
    And I delete all messages from <queueName> queue
    #And I select CategoryTypes tab in a catalog editor for catalog Syndication Two Stores Catalog
    #And I create a new category type <catType1> with following attributes
    #  | Category Description |
    #  | Name                 |
    #When I create new category of the category type for Syndication Two Stores Catalog
    #  | categoryName | categoryType | storeVisible | enableDateTime | disableDateTime | attrLongTextName     | attrLongTextValue | attrShortTextName | attrShortTextValue |
    #  | <cat1>       | <catType1>   | true         | -1             | 30              | Category Description | attr2             | Name              | attr1              |
    #And I create new category of the category type for Syndication Two Stores Catalog
    #  | categoryName | categoryType | storeVisible | enableDateTime | disableDateTime | attrLongTextName     | attrLongTextValue | attrShortTextName | attrShortTextValue |
    #  | <cat2>       | <catType1>   | true         | 5              | 10              | Category Description | attr2             | Name              | attr1              |
    #When I retrieve latest version of created in CM category projection for store SyndicationBilingualStore and category name <cat1> via API
    #Then Response status code is 200
    #And Single category API response contains correct availability rules for projection with 2 languages and category code <cat1>
    #  | availabilityRules.enableDateTime  | -1 |
    #  | availabilityRules.disableDateTime | 30 |
    #When I retrieve latest version of created in CM category projection for store SyndicationBilingualStore and category name <cat2> via API
    #Then Response status code is 200
    #And Single category API response contains correct availability rules for projection with 2 languages and category code <cat2>
    #  | availabilityRules.enableDateTime  | 5  |
    #  | availabilityRules.disableDateTime | 10 |
    #And I create subcategory for category <cat2> in catalog Syndication Two Stores Catalog with following data
    #  | categoryName | categoryType | storeVisible | enableDateTime | disableDateTime | attrLongTextName     | attrLongTextValue | attrShortTextName | attrShortTextValue |
    #  | <cat3>       | <catType1>   | true         | -1             | 30              | Category Description | attr2             | Name              | attr1              |
    #And I expand catalog with catalog name Syndication Two Stores Catalog
    #And I create subcategory for category <cat2> in catalog Syndication Two Stores Catalog with following data
    #  | categoryName | categoryType | storeVisible | enableDateTime | disableDateTime | attrLongTextName     | attrLongTextValue | attrShortTextName | attrShortTextValue |
    #  | <cat4>       | <catType1>   | true         | 1              | 15              | Category Description | attr2             | Name              | attr1              |
    #When I retrieve latest version of created in CM category projection for store SyndicationBilingualStore and category name <cat3> via API
    #Then Response status code is 200
    #And Single category API response contains correct availability rules for projection with 2 languages and category code <cat3>
    #  | availabilityRules.enableDateTime  | 5  |
    #  | availabilityRules.disableDateTime | 10 |
    #When I retrieve latest version of created in CM category projection for store SyndicationBilingualStore and category name <cat4> via API
    #Then Response status code is 200
    #And Single category API response contains correct availability rules for projection with 2 languages and category code <cat4>
    #  | availabilityRules.enableDateTime  | 5  |
    #  | availabilityRules.disableDateTime | 10 |
    #And I select Brands tab in a catalog editor for catalog Syndication Two Stores Catalog
    #And I add a new brand with brand code brandCode with the following exactly names
    #  | English | <brandNameEn> |
    #  | French  | <brandNameFr> |
    #And I expand catalog with catalog name Syndication Two Stores Catalog
    #And I select ProductTypes tab in the Catalog Editor for opened Catalog
    #When I create a new product type Prod Type with following attributes
    #  | Features |
    And I create new product for created subcategory <categorySub2Top2NameEn> where parent category is <categoryTop2NameEn> with following attributes
      | catalog       | productName | taxCode | brand         | storeVisible | availability     | shippableType |
      | <catalogName> | <prod1>     | DIGITAL | <brandNameEn> | true         | Always available | Digital Asset |
    And I retrieve latest version of created in CM offer projection for store <storeTwoLanguages> via API
    Then Response status code is 200
    And Single offer API response contains complete information about categories for projection with 2 languages
      | categories.code            | <categorySub2Top2>   |
      | categories.path            | <categoryTop2>       |
      | categories.enableDateTime  | Sep 5, 2037 9:26 AM  |
      | categories.disableDateTime | Sep 10, 2037 9:26 AM |
      | categories.default         | true                 |
    When I assign <categorySub1Top2NameEn> category to the newly created product
    And I define <categorySub1Top2NameEn> as Primary category for newly created product
    And I retrieve latest version of created in CM offer projection for store <storeTwoLanguages> via API
    Then Response status code is 200
    And Single offer API response contains complete information about categories for projection with 2 languages
      | categories.code            | <categorySub2Top2>;<categorySub1Top2>     |
      | categories.path            | <categoryTop2>;<categoryTop2>             |
      | categories.enableDateTime  | Sep 5, 2037 9:26 AM;Sep 5, 2037 9:26 AM   |
      | categories.disableDateTime | Sep 10, 2037 9:26 AM;Sep 10, 2037 9:26 AM |
      | categories.default         | false;true                                |
    When I open catalog management tab
    And I expand catalog with catalog name <catalogName>
    And I create new product for category <categoryTop1NameEn> with following attributes
      | catalog                        | productName | taxCode | brand         | storeVisible | availability     | shippableType |
      | Syndication Two Stores Catalog | <prod2>     | DIGITAL | <brandNameEn> | true         | Always available | Digital Asset |
    And I retrieve latest version of created in CM offer projection for store <storeTwoLanguages> via API
    Then Response status code is 200
    And Single offer API response contains complete information about categories for projection with 2 languages
      | categories.code            | <categoryTop1>       |
      | categories.path            |                      |
      | categories.enableDateTime  | Aug 1, 2037 9:45 AM  |
      | categories.disableDateTime | Aug 31, 2037 9:45 AM |
      | categories.default         | true                 |
    When I assign <categorySub1Top2NameEn> category to the newly created product
    And I define <categorySub1Top2NameEn> as Primary category for newly created product
    And I retrieve latest version of created in CM offer projection for store <storeTwoLanguages> via API
    Then Response status code is 200
    And Single offer API response contains complete information about categories for projection with 2 languages
      | categories.code            | <categoryTop1>;<categorySub1Top2>         |
      | categories.path            | <categoryTop2>                            |
      | categories.enableDateTime  | Aug 1, 2037 9:45 AM;Sep 5, 2037 9:26 AM   |
      | categories.disableDateTime | Aug 31, 2037 9:45 AM;Sep 10, 2037 9:26 AM |
      | categories.default         | true;false                                |
    When I open category in editor by partial name <categorySub1Top2NameEn>
    And I select editor's Featured Products tab
    And I remove all feature products for opened category
    And I add <prod1> as the featured product to opened category
    And I add <prod2> as the featured product to opened category
    And I retrieve latest version of created in CM offer projection for <prod1> product in <storeTwoLanguages> store via API
    Then Response status code is 200
    And Single offer API response contains complete information about categories for projection with 2 languages
      | categories.code            | <categorySub2Top2>;<categorySub1Top2>     |
      | categories.path            | <categoryTop2>;<categoryTop2>             |
      | categories.enableDateTime  | Sep 5, 2037 9:26 AM;Sep 5, 2037 9:26 AM   |
      | categories.disableDateTime | Sep 10, 2037 9:26 AM;Sep 10, 2037 9:26 AM |
      | categories.default         | false;true                                |
      | categories.featured        | 1                                         |
    When I retrieve latest version of created in CM offer projection for <prod2> product in <storeTwoLanguages> store via API
    Then Response status code is 200
    And Single offer API response contains complete information about categories for projection with 2 languages
      | categories.code            | <categoryTop1>;<categorySub1Top2>         |
      | categories.path            | <categoryTop2>                            |
      | categories.enableDateTime  | Aug 1, 2037 9:45 AM;Sep 5, 2037 9:26 AM   |
      | categories.disableDateTime | Aug 31, 2037 9:45 AM;Sep 10, 2037 9:26 AM |
      | categories.default         | true;false                                |
      | categories.featured        | 2                                         |
    When I move <prod2> featured product up 1 times in product list in opened category
    And I retrieve latest version of created in CM offer projection for <prod1> product in <storeTwoLanguages> store via API
    Then Response status code is 200
    And Single offer API response contains complete information about categories for projection with 2 languages
      | categories.code            | <categorySub2Top2>;<categorySub1Top2>     |
      | categories.path            | <categoryTop2>;<categoryTop2>             |
      | categories.enableDateTime  | Sep 5, 2037 9:26 AM;Sep 5, 2037 9:26 AM   |
      | categories.disableDateTime | Sep 10, 2037 9:26 AM;Sep 10, 2037 9:26 AM |
      | categories.default         | false;true                                |
      | categories.featured        | 2                                         |
    When I retrieve latest version of created in CM offer projection for <prod2> product in <storeTwoLanguages> store via API
    Then Response status code is 200
    And Single offer API response contains complete information about categories for projection with 2 languages
      | categories.code            | <categoryTop1>;<categorySub1Top2>         |
      | categories.path            | <categoryTop2>                            |
      | categories.enableDateTime  | Aug 1, 2037 9:45 AM;Sep 5, 2037 9:26 AM   |
      | categories.disableDateTime | Aug 31, 2037 9:45 AM;Sep 10, 2037 9:26 AM |
      | categories.default         | true;false                                |
      | categories.featured        | 1                                         |
    When I remove <prod2> from featured products list in opened category
    And I close category editor for <categorySub1Top2NameEn> category
    And I retrieve latest version of created in CM offer projection for <prod1> product in <storeTwoLanguages> store via API
    Then Response status code is 200
    And Single offer API response contains complete information about categories for projection with 2 languages
      | categories.code            | <categorySub2Top2>;<categorySub1Top2>     |
      | categories.path            | <categoryTop2>;<categoryTop2>             |
      | categories.enableDateTime  | Sep 5, 2037 9:26 AM;Sep 5, 2037 9:26 AM   |
      | categories.disableDateTime | Sep 10, 2037 9:26 AM;Sep 10, 2037 9:26 AM |
      | categories.default         | false;true                                |
      | categories.featured        | 1                                         |
    When I retrieve latest version of created in CM offer projection for <prod2> product in <storeTwoLanguages> store via API
    Then Response status code is 200
    And Single offer API response contains complete information about categories for projection with 2 languages
      | categories.code            | <categoryTop1>;<categorySub1Top2>         |
      | categories.path            | <categoryTop2>                            |
      | categories.enableDateTime  | Aug 1, 2037 9:45 AM;Sep 5, 2037 9:26 AM   |
      | categories.disableDateTime | Aug 31, 2037 9:45 AM;Sep 10, 2037 9:26 AM |
      | categories.default         | true;false                                |

    Examples:
      | catalogName                    | queueName                             | storeTwoLanguages         | storeOneLanguage               | prodType                         | categoryTop1    | categoryTop1NameEn | categoryTop2    | categoryTop2NameEn | categorySub1Top2    | categorySub1Top2NameEn | categorySub2Top2    | categorySub2Top2NameEn | catType1 | cat2 | cat3 | cat4 | brandCode         | brandNameEn         | brandNameFr         | prod1 | prod2 |
      | Syndication Two Stores Catalog | Consumer.test.VirtualTopic.ep.catalog | SyndicationBilingualStore | SyndicationSingleLanguageStore | productTestSingleSkuProductType1 | productTestTop1 | productTestTop1En  | productTestTop2 | productTestTop2En  | productTestSub1Top2 | productTestSub1Top2En  | productTestSub2Top2 | productTestSub2Top2En  | catType1 | cat2 | cat3 | cat4 | productTestBrand1 | productTestBrand1En | productTestBrand1Fr | prod1 | prod2 |
