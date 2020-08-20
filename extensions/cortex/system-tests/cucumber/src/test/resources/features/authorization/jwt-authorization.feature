@jwtAuthorization
Feature: JWT Authorization login tests

  Scenario Outline: Verify JWT authorization with all values and different issuers, and corresponding private keys allows
  for basic item lookup
    Given I login using jwt authorization with the following details
      | scope                 | MOBEE                                |
      | roles                 | REGISTERED                           |
      | expiration_in_seconds | 600                                  |
      | account_shared_id     | accounttest1@elasticpath.com         |
      | user_id               | <USER_ID>                            |
      | issuer                | <ISSUER>                             |
    When I look up an item with code product_with_no_discounts_sku
    Then I should see item name is Product With No Discounts
    When I view my profile
    Then the HTTP status is OK
    Examples:
    | ISSUER             |           USER_ID                    |
    | am                 | usertestguid                         |
    | punchout_attrval   | usertest                             |
    | punchout_shared_id | MOBEE:usertest@elasticpath.com       |

  Scenario: Verify that the JWT authorization with invalid issuer fails with 401 error
    Given I login using jwt authorization with the following details
      | scope                 | MOBEE                                |
      | roles                 | REGISTERED                           |
      | expiration_in_seconds | 600                                  |
      | account_shared_id     | accounttest1@elasticpath.com         |
      | user_id               | invalidCustomer                      |
      | issuer                | invalid                              |
    When I GET /profiles/mobee/default
    Then the HTTP status is unauthorized

  Scenario Outline: Verify that the JWT authorization with only roles entered allows for basic item lookup
    Given I login using jwt authorization with the following details
      | roles | catalog_browser |
    When I look up an item with code <ITEM_CODE>
    Then I should see item name is <ITEM_NAME>
    When I view my profile
    Then the HTTP status is OK
    Examples:
      | ITEM_CODE                     | ITEM_NAME                 |
      | product_with_no_discounts_sku | Product With No Discounts |
