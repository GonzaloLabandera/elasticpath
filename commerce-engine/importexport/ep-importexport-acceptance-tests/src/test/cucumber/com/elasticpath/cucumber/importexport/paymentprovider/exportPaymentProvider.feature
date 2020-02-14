# language: en
@exportPaymentProviderConfiguration
Feature: Export payment provider configurations
  In order to archive payment provider configurations for backup or data migration,
  As Operations,
  I want to export payment provider configurations to the file system.

  Scenario: Export Payment Provider Configurations
    Given the existing payment provider configurations of
      | paymentProviderConfigGuid | paymentProviderPluginBeanName | configName               | status   | configData            | defaultName | localizedNames                                                                               |
      | 23456                     | cybersourceCreditCardProvider | Cyper Source Credit Card | Active   | key;value,key2;value2 | Credit Card | en:Cyber Source Credit Card En;fr:Cyber Source Credit Card Fr;zh:Cyber Source Credit Card Zh |
      | 23457                     | paypalProvider                | Paypal Provider          | draft    | key3;value3           | Paypal      | en:Paypal Provider En;fr:Paypal Provider Fr                                                  |
      | 23458                     | yapstoneCreditCard            | Yapstone Credit Card     | DISABLED | key4;value4           | Credit Card | en:Yapstone Credit En;fr:Yapstone Credit Fr                                                  |
    When exporting payment provider configurations with the importexport tool
    And the exported payment provider configurations data is parsed
    Then the exported payment provider configurations records should equal
      | paymentProviderConfigGuid | paymentProviderPluginBeanName | configName               | status   | configData            | defaultName | localizedNames                                                                               |
      | 23456                     | cybersourceCreditCardProvider | Cyper Source Credit Card | Active   | key;value,key2;value2 | Credit Card | en:Cyber Source Credit Card En;fr:Cyber Source Credit Card Fr;zh:Cyber Source Credit Card Zh |
      | 23457                     | paypalProvider                | Paypal Provider          | draft    | key3;value3           | Paypal      | en:Paypal Provider En;fr:Paypal Provider Fr                                                  |
      | 23458                     | yapstoneCreditCard            | Yapstone Credit Card     | DISABLED | key4;value4           | Credit Card | en:Yapstone Credit En;fr:Yapstone Credit Fr                                                  |
    And the exported manifest file should have an entry for payment provider configurations

