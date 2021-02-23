# language: en
@exportStoreConfiguration
Feature: Export store
  In order to archive stores for backup or data migration,
  As Operations,
  I want to export stores to the file system.

  Scenario: Export Store
    Given the existing payment provider configurations of
      | paymentProviderConfigGuid | paymentProviderPluginBeanName | configName        | status | configData            |
      | 23456                     | cybersourceVisaProvider       | Cyper Source Visa | Active | key;value,key2;value2 |
      | 23457                     | paypalProvider                | Paypal Provider   | draft  | key3;value3           |
    And a store with following values
      | timezone                 | GMT -8:00 Pacific Standard Time |
      | store country            | United States                   |
      | store sub country        | California                      |
      | store name               | TestStore                       |
      | store code               | TestStoreCode                   |
      | currency                 | USD                             |
      | payment provider configs | 23456,23457                     |
	  | authenticated role       | BUYER                           |
	  | unauthenticated role     | SINGLE_SESSION_BUYER            |
	When exporting store with store code TestStoreCode the importexport tool
    And the exported store data is parsed
    Then the exported store records should equal
      | timezone                 | GMT -8:00 Pacific Standard Time |
      | store country            | United States                   |
      | store sub country        | California                      |
      | store name               | TestStore                       |
      | store code               | TestStoreCode                   |
      | currency                 | USD                             |
      | payment provider configs | 23456,23457                     |
	  | authenticated role       | BUYER                           |
	  | unauthenticated role     | SINGLE_SESSION_BUYER            |
	  And the exported manifest file should have an entry for stores

