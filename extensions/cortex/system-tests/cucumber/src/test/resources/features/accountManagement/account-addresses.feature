@addresses @accounts
Feature: Account Addresses

  Background:
    Given I authenticate with BUYER_ADMIN username testbuyeradmin@elasticpath.com and password password and role REGISTERED in scope mobee

  Scenario: Account with no account addresses has no element links
    Given I navigate links defaultprofile -> accounts
    And I get the account with the field account-business-name with value Some Other Business Account
    When I follow links addresses
    Then there are no element links

  Scenario: Can retrieve list of account addresses
    Given I navigate links defaultprofile -> accounts
    And I get the account with the field account-business-name with value Some Business Account
    When I follow links addresses
    Then there is an element with field address containing 1 Account St
    And there is an element with field address containing 2 Account St

  Scenario: Can retrieve list of account billing addresses
    Given I navigate links defaultprofile -> accounts
    And I get the account with the field account-business-name with value Some Business Account
    When I follow links addresses -> billingaddresses
    Then there is an element with field address containing 1 Account St
    And there is an element with field address containing 2 Account St

  Scenario: Can retrieve list of account shipping addresses
    Given I navigate links defaultprofile -> accounts
    And I get the account with the field account-business-name with value Some Business Account
    When I follow links addresses -> shippingaddresses
    Then there is an element with field address containing 1 Account St
    And there is an element with field address containing 2 Account St

  Scenario Outline: Address fields validations - all fields empty
    Given I navigate links defaultprofile -> accounts
    And I get the account with the field account-business-name with value Some Business Account
    When I follow links addresses -> addressform
    And I create account address with Country <COUNTRY>, Extended-Address <EXTENDED_ADDRESS>, Locality <LOCALITY>, Organization <ORGANIZATION>, Phone-Number <PHONE_NUMBER>, Postal-Code <POSTAL_CODE>, Region <REGION>, Street-Address <STREET_ADDRESS>, Family-Name <FAMILY_NAME> and Given-Name <GIVEN_NAME>
    Then the HTTP status is bad request
    And Structured error message contains:
      | locality must not be blank                    |
      | street-address must not be blank              |
      | country-name size must be between 2 and 2     |
      | postal-code size must be between 1 and 50     |
      | locality size must be between 1 and 200       |
      | postal-code must not be blank                 |
      | country-name must not be blank                |
      | street-address size must be between 1 and 200 |

    Examples:
      | COUNTRY | EXTENDED_ADDRESS | LOCALITY | ORGANIZATION | PHONE_NUMBER | POSTAL_CODE | REGION | STREET_ADDRESS | FAMILY_NAME | GIVEN_NAME |
      |         |                  |          |              |              |             |        |                |             |            |

  Scenario Outline: BUYER_ADMIN can create account address
    Given I navigate links defaultprofile -> accounts
    And I get the account with the field account-business-name with value Some Business Account
    When I follow links addresses -> addressform
    And I create account address with Country <COUNTRY>, Extended-Address <EXTENDED_ADDRESS>, Locality <LOCALITY>, Organization <ORGANIZATION>, Phone-Number <PHONE_NUMBER>, Postal-Code <POSTAL_CODE>, Region <REGION>, Street-Address <STREET_ADDRESS>, Family-Name <FAMILY_NAME> and Given-Name <GIVEN_NAME>
    And I follow links list
    Then the account address with postal code <POSTAL_CODE> should match the following address values
      | country-name     | <COUNTRY>          |
      | extended-address | <EXTENDED_ADDRESS> |
      | locality         | <LOCALITY>         |
      | postal-code      | <POSTAL_CODE>      |
      | region           | <REGION>           |
      | street-address   | <STREET_ADDRESS>   |

    Examples:
      | COUNTRY | EXTENDED_ADDRESS | LOCALITY  | ORGANIZATION | PHONE_NUMBER | POSTAL_CODE | REGION | STREET_ADDRESS | FAMILY_NAME | GIVEN_NAME    |
      | CA      | 1234             | Vancouver | Company Inc  | 555-555-5555 | A1B2C3     | BC     | 123 Broadway   | Customer    | Public Tester |

  Scenario Outline: PUT address with valid field values
    Given I navigate links defaultprofile -> accounts
    And I get the account with the field account-business-name with value Some Business Account
    And I follow links addresses
    Then navigate to the account address with postal code <POSTAL_CODE>
    When I modify existing account address with Country <COUNTRY>, Extended-Address <EXTENDED_ADDRESS>, Locality <LOCALITY>, Organization , Phone-Number , Postal-Code <POSTAL_CODE>, Region <REGION>, Street-Address <STREET_ADDRESS>, Family-Name <FAMILY_NAME> and Given-Name <GIVEN_NAME>
    Then the HTTP status is no content
    When I navigate links defaultprofile -> accounts
    And I get the account with the field account-business-name with value Some Business Account
    And I follow links addresses
    Then the account address with postal code <POSTAL_CODE> should match the following address values
          | country-name     | <COUNTRY>          |
          | extended-address | <EXTENDED_ADDRESS> |
          | locality         | <LOCALITY>         |
          | postal-code      | <POSTAL_CODE>      |
          | region           | <REGION>           |
          | street-address   | <STREET_ADDRESS>   |

    Examples:
      | COUNTRY | LOCALITY | EXTENDED_ADDRESS | POSTAL_CODE | REGION | STREET_ADDRESS | FAMILY_NAME | GIVEN_NAME |
      | CA      | Vancouver | 1234 | A1B2C3     | BC     | 123 street     | testFName   | testLName  |

  Scenario: Delete address
    Given I navigate links defaultprofile -> accounts
    And I get the account with the field account-business-name with value Some Business Account
    And I follow links addresses
    Then navigate to the account address with postal code A1B2C3
    When attempting a DELETE on the address
    Then the HTTP status is no content

  Scenario: Default account billing address selection
    Given I navigate links defaultprofile -> accounts
    And I get the account with the field account-business-name with value Some Business Account
    When I follow links addresses -> billingaddresses -> selector
    And I select the address with country CA and region QC
    Then the address with country CA and region QC is selected
    When I get the default account billing address
    Then the field address contains value 2 Account St

  Scenario: Default account shipping address selection
    Given I navigate links defaultprofile -> accounts
    And I get the account with the field account-business-name with value Some Business Account
    When I follow links addresses -> shippingaddresses -> selector
    And I select the address with country CA and region QC
    Then the address with country CA and region QC is selected
    When I get the default account shipping address
    Then the field address contains value 2 Account St

  Scenario: User billing address for profile
    Given I login using jwt authorization with the following details
      | scope                 | MOBEE                           |
      | expiration_in_seconds | 600                             |
      | account_shared_id     | BuyerRole@elasticpath.com       |
      | issuer                | punchout_shared_id              |
      | subject               | MOBEE:usertest3@elasticpath.com |
    When I view my profile
    And I follow links addresses -> billingaddresses
    Then there are 2 links of rel element
    And there is an element with field address containing 1 Punchout St
    When I get the default billing address
    Then the field address contains value 1 Punchout St
    When I follow links list -> billingaddresses -> selector
    And I select the address with country CA and region BC
    Then the address with country CA and region BC is selected

  Scenario: User shipping address for profile
    Given I login using jwt authorization with the following details
      | scope                 | MOBEE                           |
      | expiration_in_seconds | 600                             |
      | account_shared_id     | BuyerRole@elasticpath.com       |
      | issuer                | punchout_shared_id              |
      | subject               | MOBEE:usertest3@elasticpath.com |
    When I view my profile
    And I follow links addresses -> shippingaddresses
    Then there are 2 links of rel element
    And there is an element with field address containing 1 Punchout St
    When I get the default shipping address
    Then the field address contains value 1 Punchout St
    When I follow links list -> shippingaddresses -> selector
    And I select the address with country CA and region BC
    Then the address with country CA and region BC is selected

  Scenario: Account uses own addresses for billing address
    Given I login using jwt authorization with the following details
      | scope                 | MOBEE                           |
      | expiration_in_seconds | 600                             |
      | account_shared_id     | BuyerRole@elasticpath.com       |
      | issuer                | punchout_shared_id              |
      | subject               | MOBEE:usertest3@elasticpath.com |
    When I retrieve the order
    And I follow links billingaddressinfo -> selector
    And I select the address with country CA and region QC
    Then the address with country CA and region QC is selected

  Scenario: Account uses own addresses for shipping address
    Given I login using jwt authorization with the following details
      | scope                 | MOBEE                           |
      | expiration_in_seconds | 600                             |
      | account_shared_id     | BuyerRole@elasticpath.com       |
      | issuer                | punchout_shared_id              |
      | subject               | MOBEE:usertest3@elasticpath.com |
    When I add item with code physical_sku to my cart
    And I retrieve the order
    And I follow links deliveries -> element -> destinationinfo -> selector
    And I select the address with country CA and region QC
    Then the address with country CA and region QC is selected

  Scenario: BUYER cannot change account addresses
    Given I authenticate with BUYER username testbuyer@elasticpath.com and password password and role REGISTERED in scope mobee
    And I navigate links defaultprofile -> accounts
    And I get the account with the field account-business-name with value Some Business Account
    When I follow links addresses
    Then I should not see the following links
      | addressform |
    When I follow links billingaddresses
    Then I should not see the following links
      | selector |
    When I follow links account -> addresses -> shippingaddresses
    Then I should not see the following links
      | selector |