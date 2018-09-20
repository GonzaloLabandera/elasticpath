# language: en
#  NOTE: once the current date passed the retention period, the tests will fail.
@dataPointValuesJobProcessors
Feature: Scheduled data point value jobs for data points shared by data policies

  Background:
    Given a customer with billing address of
      | lastName | firstName | street1       | street2       | city     | country | state    | zip | phone      | creationDate             | lastModifiedDate         |
      | Bird     | Big       | Sesame Street | Robson Street | New York | USA     | New York | zip | 5555555555 | 2018-01-01T10:00:00.000Z | 2018-01-01T10:00:00.000Z |

  Scenario: Expired data point value job should NOT remove billing address data point values if longest retention period is not expired
    Given the existing data policies of
      | guid  | dataPolicyName | description   | referenceKey | retentionPeriod | policyState | retentionType      | segments | dataPoints                                                         |
      | 12345 | Policy 1       | Description 1 | DP12345      | 5               | ACTIVE      | FROM_CREATION_DATE | US,CA,FR | 345;NAME1;CUSTOMER_BILLING_ADDRESS;FIRST_NAME;Cust first name;true |
      | 12346 | Policy 2       | Description 2 | DP12346      | 1000            | ACTIVE      | FROM_CREATION_DATE | US,CA,FR | 345;NAME1;CUSTOMER_BILLING_ADDRESS;FIRST_NAME;Cust first name;true |
      | 12347 | Policy 3       | Description 3 | DP12347      | 500             | ACTIVE      | FROM_CREATION_DATE | US,CA,FR | 345;NAME1;CUSTOMER_BILLING_ADDRESS;FIRST_NAME;Cust first name;true |
    And the existing customer consents of
      | guid  | dataPolicyGuid | consentDate              | action  |
      | 12422 | 12345          | 2018-01-01T10:10:00.000Z | GRANTED |
      | 12423 | 12346          | 2018-01-01T10:10:00.000Z | GRANTED |
      | 12424 | 12347          | 2018-01-01T10:10:00.000Z | GRANTED |
    When expired data point values job processor runs
    Then the data point value with data point guid [345] should have value [Big]

  Scenario: Expired data point values job should NOT remove data point values regardless of data policy states if longest retention period
  is not expired
    Given the existing data policies of
      | guid  | dataPolicyName | description   | referenceKey | retentionPeriod | policyState | retentionType      | segments | dataPoints                                                         |
      | 12345 | Policy 1       | Description 1 | DP12345      | 5               | DISABLED    | FROM_CREATION_DATE | US,CA,FR | 345;NAME1;CUSTOMER_BILLING_ADDRESS;FIRST_NAME;Cust first name;true |
      | 12346 | Policy 2       | Description 2 | DP12346      | 1000            | ACTIVE      | FROM_CREATION_DATE | US,CA,FR | 345;NAME1;CUSTOMER_BILLING_ADDRESS;FIRST_NAME;Cust first name;true |
    And the existing customer consents of
      | guid  | dataPolicyGuid | consentDate              | action  |
      | 12422 | 12345          | 2018-01-01T10:10:00.000Z | GRANTED |
      | 12423 | 12346          | 2018-01-01T10:10:00.000Z | GRANTED |
    When expired data point values job processor runs
    Then the data point value with data point guid [345] should have value [Big]

  Scenario: Revoked consent data point value job should remove data point values only for REVOKED consents regardless of data policy states
    Given the existing data policies of
      | guid  | dataPolicyName | description   | referenceKey | retentionPeriod | policyState | retentionType      | segments | dataPoints                                                         |
      | 12345 | Policy 1       | Description 1 | DP12345      | 5               | DISABLED    | FROM_CREATION_DATE | US,CA,FR | 345;NAME1;CUSTOMER_BILLING_ADDRESS;FIRST_NAME;Cust first name;true |
      | 12346 | Policy 2       | Description 1 | DP12346      | 5               | ACTIVE      | FROM_CREATION_DATE | US,CA,FR | 345;NAME1;CUSTOMER_BILLING_ADDRESS;FIRST_NAME;Cust first name;true |
    And the existing customer consents of
      | guid  | dataPolicyGuid | consentDate              | action  |
      | 12422 | 12345          | 2018-01-01T10:10:00.000Z | REVOKED |
      | 12423 | 12346          | 2018-01-01T10:10:00.000Z | REVOKED |
    When revoked consents data point values job processor runs
    Then the data point value with data point guid [345] should have value [‐]