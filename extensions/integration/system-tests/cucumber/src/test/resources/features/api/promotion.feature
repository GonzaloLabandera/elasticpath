Feature: Import/Export API: Promotion

  Scenario: Export promotions
    Given I export Promotion records from the API
    Then response has http status 200
    And response has at least 10 promotion elements

  Scenario: Export promotions with CatalogCode filter
    Given I export Promotion records with query "FIND Promotion WHERE CatalogCode='Mobile'" from the API
    Then response has http status 200
    And response has at least 1 promotion elements

  Scenario: Export promotions with StoreCode filter
    Given I export Promotion records with query "FIND Promotion WHERE StoreCode='mobee'" from the API
    Then response has http status 200
    And response has at least 1 promotion elements

  Scenario: Export promotions with PromotionType filter
    Given I export Promotion records with query "FIND Promotion WHERE PromotionType='Catalog'" from the API
    Then response has http status 200
    And response has at least 2 promotion elements

  Scenario: Export promotions with PromotionName filter
    Given I export Promotion records with query "FIND Promotion WHERE PromotionName='1 Dollar Off itest_Segment'" from the API
    Then response has http status 200
    And response has exactly 1 promotion elements

  Scenario: Export coupons
    Given I export CouponSet records with parent Promotion from the API
    Then response has http status 200
    And response has at least 1 coupon_set elements

  Scenario: Export condition rules
    Given I export ConditionRule records with parent Promotion from the API
    Then response has http status 200
    And response has at least 1 rule elements
