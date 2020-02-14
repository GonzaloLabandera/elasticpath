# language: en
@importPaymentProviderConfiguration
Feature: Import Payment Provider Configurations
  In order to migrate payment provider configurations from an archive,
  As Operations,
  I want to import payment provider configurations from the file system.

  Scenario: Import Payment Provider Configuration
    Given the payment provider configuration import data has been emptied out
    And the existing payment provider configuration of
      | guid  | paymentProviderPluginBeanName | name              | status | defaultDisplayName  | localizedNames                                                          |
      | 23456 | cybersourceVisaProvider       | Cyper Source Visa | ACTIVE | Visa display name   | en:Cyber Source Visa En;fr:Cyber Source Visa Fr;zh:Cyber Source Visa Zh |
      | 23457 | paypalProvider                | Paypal Provider   | DRAFT  | Paypal display name | en:Paypal Provider En;fr:Paypal Provider Fr                             |
    And the payment provider configuration to import of
      | guid  | paymentProviderPluginBeanName | name                 | status   | defaultDisplayName    | localizedNames                                                                   |
      | 23458 | yapstoneMasterCard            | Yapstone Master Card | DISABLED | Yapstone display name | en:Yapstone Master Card En;fr:Yapstone Master Card Fr;zh:Yapstone Master Card Zh |
    When importing payment provider configuration with the importexport tool
    Then there are no any warning and failures in summary
    And database should contain payment provider configurations of
      | guid  | paymentProviderPluginBeanName | name                 | status   | defaultDisplayName    | localizedNames                                                                   |
      | 23456 | cybersourceVisaProvider       | Cyper Source Visa    | ACTIVE   | Visa display name     | en:Cyber Source Visa En;fr:Cyber Source Visa Fr;zh:Cyber Source Visa Zh          |
      | 23457 | paypalProvider                | Paypal Provider      | DRAFT    | Paypal display name   | en:Paypal Provider En;fr:Paypal Provider Fr                                      |
      | 23458 | yapstoneMasterCard            | Yapstone Master Card | DISABLED | Yapstone display name | en:Yapstone Master Card En;fr:Yapstone Master Card Fr;zh:Yapstone Master Card Zh |

  Scenario: Import Payment Provider Configuration with existing Payment Provider Configuration
    Given the payment provider configuration import data has been emptied out
    And the existing payment provider configuration of
      | guid  | paymentProviderPluginBeanName | name              | status | defaultDisplayName  | localizedNames                                                          |
      | 23456 | cybersourceVisaProvider       | Cyper Source Visa | ACTIVE | Visa display name   | en:Cyber Source Visa En;fr:Cyber Source Visa Fr;zh:Cyber Source Visa Zh |
      | 23457 | paypalProvider                | Paypal Provider   | DRAFT  | Paypal display name | en:Paypal Provider En;fr:Paypal Provider Fr                             |
    And the payment provider configuration to import of
      | guid  | paymentProviderPluginBeanName | name       | status   | defaultDisplayName           | localizedNames                                     |
      | 23456 | cybersourceVisa               | Cyper Visa | DISABLED | CyperSourceVisa display name | en:Cyper Visa En;fr:Cyper Visa Fr;zh:Cyper Visa Zh |
    When importing payment provider configuration with the importexport tool
    Then there is the unsupported payment provider configuration operation warning message in the summary
      | code     | details |
      | IE-31400 | 23456   |
    And database should contain payment provider configurations of
      | guid  | paymentProviderPluginBeanName | name              | status | defaultDisplayName  | localizedNames                                                          |
      | 23456 | cybersourceVisaProvider       | Cyper Source Visa | ACTIVE | Visa display name   | en:Cyber Source Visa En;fr:Cyber Source Visa Fr;zh:Cyber Source Visa Zh |
      | 23457 | paypalProvider                | Paypal Provider   | DRAFT  | Paypal display name | en:Paypal Provider En;fr:Paypal Provider Fr                             |
