# language: en
@importStoreConfiguration
Feature: Import store
  In order to migrate stores from an archive,
  As Operations,
  I want to import stores from the file system.

  Scenario: Import Payment Configuration with store association
    Given the stores import data has been emptied out
    And the existing payment provider configuration of
      | guid  | paymentProviderPluginBeanName | name              | status | defaultDisplayName  | localizedNames                                                          |
      | 23456 | cybersourceVisaProvider       | Cyper Source Visa | ACTIVE | Visa display name   | en:Cyber Source Visa En;fr:Cyber Source Visa Fr;zh:Cyber Source Visa Zh |
      | 23457 | paypalProvider                | Paypal Provider   | DRAFT  | Paypal display name | en:Paypal Provider En;fr:Paypal Provider Fr                             |
    And the stores to import of
      | code  | globalization                    | url                                      | name  | state | type | catalog | display_out_of_stock | email_sender_name               | email_sender_address            | store_admin_email          | credit_card_cvv2_enabled | store_full_credit_cards | locales     | currencies | tax_codes   | credit_card_types     | payment_provider_configurations | authenticated role  | unauthenticated role |
      | MOBEE | CAD;en;UTF-8;BC;CA;Europe/London | https://mobee.elasticpath.com/storefront | Mobee | 200   | B2B  | Mobile  | true                 | CustomerService@elasticpath.com | CustomerService@elasticpath.com | StoreAdmin@elasticpath.com | true                     | false                   | fr;fr_ca;en | CAD;EUR    | 502;501;500 | American Express;Visa | 23456                           | BUYER               | SINGLE_SESSION_BUYER |
    When importing stores with the importexport tool
    Then there are no any warnings and failures in stores importing summary
    And database should contain store with code MOBEE wired with payment provider config with guid 23456
    And database should contain payment provider configuration with guid 23456 and status ACTIVE
    And database should contain payment provider configuration with guid 23457 and status DRAFT

  Scenario: Unable to import store if the payment configuration is not ACTIVE
    Given the stores import data has been emptied out
    And the existing payment provider configuration of
      | guid  | paymentProviderPluginBeanName | name              | status | defaultDisplayName  | localizedNames                                                          |
      | 23456 | cybersourceVisaProvider       | Cyper Source Visa | ACTIVE | Visa display name   | en:Cyber Source Visa En;fr:Cyber Source Visa Fr;zh:Cyber Source Visa Zh |
      | 23457 | paypalProvider                | Paypal Provider   | DRAFT  | Paypal display name | en:Paypal Provider En;fr:Paypal Provider Fr                             |
    And the stores to import of
      | code  | globalization                    | url                                      | name  | state | type | catalog | display_out_of_stock | email_sender_name               | email_sender_address            | store_admin_email          | credit_card_cvv2_enabled | store_full_credit_cards | locales     | currencies | tax_codes   | credit_card_types     | payment_provider_configurations | authenticated role  | unauthenticated role |
      | MOBEE | CAD;en;UTF-8;BC;CA;Europe/London | https://mobee.elasticpath.com/storefront | Mobee | 200   | B2B  | Mobile  | true                 | CustomerService@elasticpath.com | CustomerService@elasticpath.com | StoreAdmin@elasticpath.com | true                     | false                   | fr;fr_ca;en | CAD;EUR    | 502;501;500 | American Express;Visa | 23457                           | BUYER               | SINGLE_SESSION_BUYER |
    When importing stores with the importexport tool
    Then there is the unsupported store importing operation warning message in the summary
      | code     | details       |
      | IE-30402 | STORE;(MOBEE) |
      | IE-30407 | 1;STORE       |
    And database should not contain store with code MOBEE
    And database should not contain any stores wired with payment provider configuration with guid 23457
    And database should contain payment provider configuration with guid 23456 and status ACTIVE
    And database should contain payment provider configuration with guid 23457 and status DRAFT