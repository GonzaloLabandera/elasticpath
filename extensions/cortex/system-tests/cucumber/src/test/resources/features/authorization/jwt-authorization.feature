@jwtAuthorization
Feature: JWT Authorization login tests

  Scenario Outline: Verify JWT authorization with all values and different issuers, and corresponding private keys allows
  for basic item lookup
    Given I login using jwt authorization with the following details
      | scope                 | MOBEE                        |
      | expiration_in_seconds | 600                          |
      | account_shared_id     | <account_shared_id>          |
      | user_id               | <USER_ID>                    |
      | issuer                | <ISSUER>                     |
	  | subject               | <subject>                    |
	When I look up an item with code product_with_no_discounts_sku
    Then I should see item name is Product With No Discounts
    When I view my profile
    Then the HTTP status is OK
    Examples:
      | ISSUER             | USER_ID                        | subject                        | account_shared_id           |
      | am                 | usertestguid                   |                                |accounttest1@elasticpath.com |
      | punchout_attrval   | usertest                       |                                |accounttest1@elasticpath.com |
      | punchout_shared_id |  								| MOBEE:usertest@elasticpath.com |accounttest1@elasticpath.com |
	  | punchout_attrval   | testML001				        |                                |SomeBusiness3@abc.com        |


  Scenario: Verify that the JWT authorization with invalid issuer fails with 401 error
    Given I login using jwt authorization with the following details
      | scope                 | MOBEE                        |
      | expiration_in_seconds | 600                          |
      | account_shared_id     | accounttest1@elasticpath.com |
      | user_id               | invalidCustomer              |
      | issuer                | invalid                      |
    When I GET /profiles/mobee/default
    Then the HTTP status is unauthorized

  Scenario Outline: Verify that the JWT authorization with only roles entered allows for basic item lookup
    Given I login using jwt authorization with the following details
      | scope | mobee |
    When I look up an item with code <ITEM_CODE>
    Then I should see item name is <ITEM_NAME>
    When I view my profile
    Then the HTTP status is OK
    Examples:
      | ITEM_CODE                     | ITEM_NAME                 |
      | product_with_no_discounts_sku | Product With No Discounts |

  Scenario: Verify that the JWT authorization with empty user-id and metadata user-id fails with 400 error
    Given I login using jwt authorization with metadata and the following details inside
      | scope                 | MOBEE                        |
      | expiration_in_seconds | 600                          |
      | account_shared_id     | accounttest1@elasticpath.com |
    When I GET /profiles/mobee/default
    Then the HTTP status is bad request

  Scenario: Verify that the JWT authorization with empty user-id header and with populated metadata user-id is successful
    Given I login using jwt authorization with metadata and the following details inside
      | scope                 | MOBEE                        |
      | expiration_in_seconds | 600                          |
      | account_shared_id     | accounttest1@elasticpath.com |
      | metadata_user_id      | usertest                     |
    When I GET /profiles/mobee/default
    Then the HTTP status is OK

  Scenario: Verify that the JWT authorization with empty user-id header and with populated metadata is successful
    and creates new single session user
    Given I login using jwt authorization with metadata and the following details inside
      | scope                 | MOBEE                        |
      | expiration_in_seconds | 600                          |
      | account_shared_id     | accounttest1@elasticpath.com |
      | metadata_user_id      | someUserId                   |
      | metadata_first_name   | someFirstName                |
      | metadata_last_name    | someLastName                 |
      | metadata_user_company | someUserCompany              |
    When I GET /profiles/mobee/default
    Then the HTTP status is OK
    And the field family-name contains value someLastName
    And the field given-name contains value someFirstName
    And the field company contains value someUserCompany