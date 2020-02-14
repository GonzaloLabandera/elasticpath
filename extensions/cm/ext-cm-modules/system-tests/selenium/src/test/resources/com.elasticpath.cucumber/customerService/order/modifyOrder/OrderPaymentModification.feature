@regressionTest @customerService @order @modifyOrder
Feature: Order Payments Modification

  Background:
    Given I sign in to CM as admin user
    And I have authenticated as a newly registered shopper

  Scenario Outline: Modify Reserve transaction approved when order amount is decreased
    Given I create an unsaved <PAYMENT_METHOD> payment instrument from order supplying following fields:
      | display-name | <INSTRUMENT_NAME> |
    And I make a purchase with newly created payment instrument for scope mobee with following skus
      | skuCode      | quantity |
      | physical_sku | 1        |
    And I search and open order editor for the latest order
    When I modify order shipment line item discount to 10
    Then I should see following order payment transaction in the Payment History
      | Method  | <PAYMENT_METHOD>  |
      | Type    | Reserve           |
      | Details | <INSTRUMENT_NAME> |
      | Status  | <RESERVE_STATUS>  |
      | Amount  | <OLD_AMOUNT>      |
    And I should see following order payment transaction in the Payment History
      | Method  | <PAYMENT_METHOD>  |
      | Type    | Modify Reserve    |
      | Details | <INSTRUMENT_NAME> |
      | Status  | Approved          |
      | Amount  | <NEW_AMOUNT>      |
    And I should NOT see following order payment transaction in the Payment History
      | Type | Cancel Reserve |

    Examples:
      | PAYMENT_METHOD    | INSTRUMENT_NAME | OLD_AMOUNT | NEW_AMOUNT | RESERVE_STATUS |
      | Smart Path Config | instrument 1    | $131.50    | $121.50    | Approved       |

  Scenario Outline: Modify Reserve transaction approved when order amount is increased
    Given I create an unsaved <PAYMENT_METHOD> payment instrument from order supplying following fields:
      | display-name | <INSTRUMENT_NAME> |
    And I make a purchase with newly created payment instrument for scope mobee with following skus
      | skuCode      | quantity |
      | physical_sku | 1        |
    And I search and open order editor for the latest order
    When I modify order shipment line item quantity to 2
    And I click on Show Skipped Payment Events
    Then I should see following order payment transaction in the Payment History with skipped events
      | Method  | <PAYMENT_METHOD>  |
      | Type    | Reserve           |
      | Details | <INSTRUMENT_NAME> |
      | Status  | <RESERVE_STATUS>  |
      | Amount  | <OLD_AMOUNT>      |
    And I should see following order payment transaction in the Payment History with skipped events
      | Method  | <PAYMENT_METHOD>  |
      | Type    | Modify Reserve    |
      | Details | <INSTRUMENT_NAME> |
      | Status  | Approved          |
      | Amount  | <NEW_AMOUNT>      |
    And I should NOT see following order payment transaction in the Payment History
      | Type | Cancel Reserve |
    When I complete the shipment for shipment ID 1
    Then I should see following order payment transaction in the Payment History with skipped events
      | Method  | <PAYMENT_METHOD>  |
      | Type    | Charge            |
      | Details | <INSTRUMENT_NAME> |
      | Status  | Approved          |
      | Amount  | <NEW_AMOUNT>      |

    Examples:
      | PAYMENT_METHOD             | INSTRUMENT_NAME | OLD_AMOUNT | NEW_AMOUNT | RESERVE_STATUS |
      | Smart Path Config          | instrument 2a   | $131.50    | $156.50    | Approved       |
      | Reserve Unsupported Config | instrument 2b   | $131.50    | $156.50    | Skipped        |

  Scenario Outline: Decreasing order amount with Reserve capability unsupported system should keep original order total
    Given I create an unsaved <PAYMENT_METHOD> payment instrument from order supplying following fields:
      | display-name | <INSTRUMENT_NAME> |
    And I make a purchase with newly created payment instrument for scope mobee with following skus
      | skuCode      | quantity |
      | physical_sku | 1        |
    And I search and open order editor for the latest order
    When I modify order shipment line item discount to 10
    And I select Payments tab in the Order Editor
    And I click on Show Skipped Payment Events
    Then I should see following order payment transaction in the Payment History with skipped events
      | Method  | <PAYMENT_METHOD>  |
      | Type    | Reserve           |
      | Details | <INSTRUMENT_NAME> |
      | Status  | <RESERVE_STATUS>  |
      | Amount  | <OLD_AMOUNT>      |
    And I should see following order payment transaction in the Payment History with skipped events
      | Method  | <PAYMENT_METHOD>  |
      | Type    | Modify Reserve    |
      | Details | <INSTRUMENT_NAME> |
      | Status  | Approved          |
      | Amount  | <NEW_AMOUNT>      |
    And I should NOT see following order payment transaction in the Payment History with skipped events
      | Type | Cancel Reserve |
    And I should NOT see following order payment transaction in the Payment History with skipped events
      | Type   | Reserve      |
      | Amount | <NEW_AMOUNT> |

    Examples:
      | PAYMENT_METHOD             | INSTRUMENT_NAME | OLD_AMOUNT | NEW_AMOUNT | RESERVE_STATUS |
      | Reserve Unsupported Config | instrument 3    | $131.50    | $121.50    | Skipped        |

  Scenario Outline: Decreasing order amount with Modify capability unsupported system should use Cancel and Reserve capabilities to simulate modification
    Given I create an unsaved <PAYMENT_METHOD> payment instrument from order supplying following fields:
      | display-name | <INSTRUMENT_NAME> |
    And I make a purchase with newly created payment instrument for scope mobee with following skus
      | skuCode      | quantity |
      | physical_sku | 1        |
    And I search and open order editor for the latest order
    When I modify order shipment line item discount to 10
    And I complete the shipment for shipment ID 1
    Then I should see following order payment transaction in the Payment History
      | Method  | <PAYMENT_METHOD>  |
      | Type    | Reserve           |
      | Details | <INSTRUMENT_NAME> |
      | Status  | <RESERVE_STATUS>  |
      | Amount  | <NEW_AMOUNT>      |
    And I should see following order payment transaction in the Payment History
      | Method  | <PAYMENT_METHOD>  |
      | Type    | Cancel Reserve    |
      | Details | <INSTRUMENT_NAME> |
      | Status  | <CANCEL_STATUS>   |
      | Amount  | <OLD_AMOUNT>      |
    And I should NOT see following order payment transaction in the Payment History
      | Type | Modify Reserve |
    And I should see following order payment transaction in the Payment History
      | Method  | <PAYMENT_METHOD>  |
      | Type    | Charge            |
      | Details | <INSTRUMENT_NAME> |
      | Status  | <RESERVE_STATUS>  |
      | Amount  | <NEW_AMOUNT>      |

    Examples:
      | PAYMENT_METHOD            | INSTRUMENT_NAME | OLD_AMOUNT | NEW_AMOUNT | RESERVE_STATUS | CANCEL_STATUS |
      | Modify Unsupported Config | instrument 4    | $131.50    | $121.50    | Approved       | Approved      |

  Scenario Outline: Decreasing order amount with Modify and Cancel capabilities unsupported system should Reserve original order total
    Given I create an unsaved <PAYMENT_METHOD> payment instrument from order supplying following fields:
      | display-name | <INSTRUMENT_NAME> |
    And I make a purchase with newly created payment instrument for scope mobee with following skus
      | skuCode      | quantity |
      | physical_sku | 1        |
    And I search and open order editor for the latest order
    When I modify order shipment line item discount to 10
    Then I should see following order payment transaction in the Payment History
      | Method  | <PAYMENT_METHOD>  |
      | Type    | Reserve           |
      | Details | <INSTRUMENT_NAME> |
      | Status  | <RESERVE_STATUS>  |
      | Amount  | <OLD_AMOUNT>      |
    And I should NOT see following order payment transaction in the Payment History
      | Type | Modify Reserve |
    And I should NOT see following order payment transaction in the Payment History
      | Type   | Reserve      |
      | Amount | <NEW_AMOUNT> |
    When I complete the shipment for shipment ID 1
    Then I should see following order payment transaction in the Payment History
      | Method  | <PAYMENT_METHOD>  |
      | Type    | Charge            |
      | Details | <INSTRUMENT_NAME> |
      | Status  | <RESERVE_STATUS>  |
      | Amount  | <NEW_AMOUNT>      |

    Examples:
      | PAYMENT_METHOD                       | INSTRUMENT_NAME | OLD_AMOUNT | NEW_AMOUNT | RESERVE_STATUS |
      | Modify And Cancel Unsupported Config | instrument 5    | $131.50    | $121.50    | Approved       |

  Scenario Outline: Increasing order amount with Modify capability unsupported system should Reserve additional money
    Given I create an unsaved <PAYMENT_METHOD> payment instrument from order supplying following fields:
      | display-name | <INSTRUMENT_NAME> |
    And I make a purchase with newly created payment instrument for scope mobee with following skus
      | skuCode      | quantity |
      | physical_sku | 1        |
    And I search and open order editor for the latest order
    When I modify order shipment line item quantity to 2
    And I complete the shipment for shipment ID 1
    Then I should see following order payment transaction in the Payment History
      | Method  | <PAYMENT_METHOD>  |
      | Type    | Reserve           |
      | Details | <INSTRUMENT_NAME> |
      | Status  | <RESERVE_STATUS>  |
      | Amount  | <OLD_AMOUNT>      |
    And I should see following order payment transaction in the Payment History
      | Method  | <PAYMENT_METHOD>    |
      | Type    | Reserve             |
      | Details | <INSTRUMENT_NAME>   |
      | Status  | <RESERVE_STATUS>    |
      | Amount  | <ADDITIONAL_AMOUNT> |
    And I should NOT see following order payment transaction in the Payment History
      | Type | Modify Reserve |
    And I should NOT see following order payment transaction in the Payment History
      | Type | Cancel Reserve |
    And I should see following order payment transaction in the Payment History
      | Method  | <PAYMENT_METHOD>    |
      | Type    | Charge              |
      | Details | <INSTRUMENT_NAME>   |
      | Status  | <RESERVE_STATUS>    |
      | Amount  | <ADDITIONAL_AMOUNT> |
    And I should see following order payment transaction in the Payment History
      | Method  | <PAYMENT_METHOD>  |
      | Type    | Charge            |
      | Details | <INSTRUMENT_NAME> |
      | Status  | <RESERVE_STATUS>  |
      | Amount  | <OLD_AMOUNT>      |

    Examples:
      | PAYMENT_METHOD                       | INSTRUMENT_NAME | OLD_AMOUNT | ADDITIONAL_AMOUNT | RESERVE_STATUS |
      | Modify Unsupported Config            | instrument 6    | $131.50    | $25.00            | Approved       |
      | Modify And Cancel Unsupported Config | instrument 7    | $131.50    | $25.00            | Approved       |

  Scenario Outline: Decreasing order total with Modify Reserve unsupported and Cancel Reserve FAILS should see Cancel Reserve FAILED
    Given I go to Payment Configurations
    And I create a new Payment Configuration with following details
      | PROVIDER           | Smart Path Modify Unsupported |
      | METHOD             | CARD                          |
      | CONFIGURATION_NAME | <CONFIGURATION_NAME>          |
      | DISPLAY_NAME       | <CONFIGURATION_NAME>          |
    And I configure the payment configuration properties
      | CANCEL_RESERVE | FAILS |
    And I save the payment configuration
    And I activate the newly created payment configuration
    And I select and save the newly created payment configuration for store MOBEE
    And I have created payment instrument with the newly created payment configuration on my profile:
      | display-name | <PAYMENT_INSTRUMENT_NAME> |
    And I make a purchase with newly created payment instrument for scope mobee with following skus
      | skuCode      | quantity |
      | physical_sku | 1        |
    And I search and open order editor for the latest order
    When I modify order shipment line item discount to 10
    Then I should see following order payment transaction in the Payment History
      | Method                      | <CONFIGURATION_NAME>      |
      | Type                        | Reserve                   |
      | Details                     | <PAYMENT_INSTRUMENT_NAME> |
      | Status                      | Approved                  |
      | Amount                      | <NEW_TOTAL>               |
      | Original payment instrument | Yes                       |
    And I should see following order payment transaction in the Payment History
      | Method                      | <CONFIGURATION_NAME>      |
      | Type                        | Cancel Reserve            |
      | Details                     | <PAYMENT_INSTRUMENT_NAME> |
      | Status                      | Failed                    |
      | Amount                      | <ORIGINAL_TOTAL>          |
      | Original payment instrument | Yes                       |
    When I complete the shipment for shipment ID 1
    Then I should see following order payment transaction in the Payment History
      | Method                      | <CONFIGURATION_NAME>      |
      | Type                        | Charge                    |
      | Details                     | <PAYMENT_INSTRUMENT_NAME> |
      | Status                      | Approved                  |
      | Amount                      | <NEW_TOTAL>               |
      | Original payment instrument | Yes                       |


    Examples:
      | CONFIGURATION_NAME   | PAYMENT_INSTRUMENT_NAME | NEW_TOTAL | ORIGINAL_TOTAL |
      | Cancel Reserve FAILS | my visa                 | $121.50   | $131.50        |

  Scenario Outline: Modify Reserve Fails is not recorded when modify fails increasing order total
    Given I go to Configuration
    And I go to Payment Configurations
    And I create a new Payment Configuration with following details
      | PROVIDER           | <PAYMENT_METHOD>         |
      | METHOD             | CARD                     |
      | CONFIGURATION_NAME | ModifyReserveFailsConfig |
    And I configure the payment configuration properties
      | MODIFY_RESERVE | FAILS |
    And I save the payment configuration
    And I activate the newly created payment configuration
    And I select and save the newly created payment configuration for store MOBEE
    And I have created order payment instrument with the newly created payment configuration
      | display-name | <PAYMENT_INSTRUMENT_NAME> |
    And I make a purchase with newly created payment instrument for scope mobee with following skus
      | skuCode      | quantity |
      | physical_sku | 1        |
    And I search and open order editor for the latest order
    When I modify the order to increase order total by setting line item quantity to 2
    Then I should NOT see following order payment transaction in the Payment History
      | Method  | <PAYMENT_METHOD>          |
      | Type    | Modify Reserve            |
      | Details | <PAYMENT_INSTRUMENT_NAME> |
      | Status  | Approved                  |
      | Amount  | $156.50                   |

    Examples:
      | PAYMENT_METHOD | PAYMENT_INSTRUMENT_NAME |
      | Smart Path     | Modify Reserve Fails    |

  Scenario Outline: Modify Reserve transaction is Skipped when modify fails decreasing order total
    Given I go to Configuration
    And I go to Payment Configurations
    And I create a new Payment Configuration with following details
      | PROVIDER           | <PAYMENT_METHOD>         |
      | METHOD             | CARD                     |
      | CONFIGURATION_NAME | ModifyReserveFailsConfig |
    And I configure the payment configuration properties
      | MODIFY_RESERVE | FAILS |
    And I save the payment configuration
    And I activate the newly created payment configuration
    And I select and save the newly created payment configuration for store MOBEE
    And I have created order payment instrument with the newly created payment configuration
      | display-name | <PAYMENT_INSTRUMENT_NAME> |
    And I make a purchase with newly created payment instrument for scope mobee with following skus
      | skuCode      | quantity |
      | physical_sku | 1        |
    And I search and open order editor for the latest order
    When I modify order shipment line item discount to 10
    And I click on Show Skipped Payment Events
    Then I should see following order payment transaction in the Payment History with skipped events
      | Method  | <PAYMENT_METHOD>          |
      | Type    | Modify Reserve            |
      | Details | <PAYMENT_INSTRUMENT_NAME> |
      | Status  | Skipped                   |
      | Amount  | $121.50                   |
    Examples:
      | PAYMENT_METHOD | PAYMENT_INSTRUMENT_NAME |
      | Smart Path     | Modify Reserve Fails    |

  Scenario Outline: Payment skipped events are not shown by default
    Given I create an unsaved <PAYMENT_METHOD> payment instrument from order supplying following fields:
      | display-name | <INSTRUMENT_NAME> |
    And I make a purchase with newly created payment instrument for scope mobee with following skus
      | skuCode     | quantity |
      | digital_sku | 1        |
    When I search and open order editor for the latest order
    Then I should NOT see following order payment transaction in the Payment History with skipped events
      | Method  | <PAYMENT_METHOD>  |
      | Type    | Reserve           |
      | Details | <INSTRUMENT_NAME> |
      | Status  | Skipped           |
      | Amount  | <AMOUNT>          |
    And I should see following order payment transaction in the Payment History with skipped events
      | Method  | <PAYMENT_METHOD>  |
      | Type    | Charge            |
      | Details | <INSTRUMENT_NAME> |
      | Status  | Approved          |
      | Amount  | <AMOUNT>          |

    Examples:
      | PAYMENT_METHOD             | INSTRUMENT_NAME | AMOUNT |
      | Reserve Unsupported Config | instrument 8    | $21.30 |

  Scenario Outline: Payment skipped events can be displayed
    Given I create an unsaved <PAYMENT_METHOD> payment instrument from order supplying following fields:
      | display-name | <INSTRUMENT_NAME> |
    And I make a purchase with newly created payment instrument for scope mobee with following skus
      | skuCode      | quantity |
      | physical_sku | 1        |
    And I search and open order editor for the latest order
    When I click on Show Skipped Payment Events
    Then I should see following order payment transaction in the Payment History with skipped events
      | Method  | <PAYMENT_METHOD>  |
      | Type    | Reserve           |
      | Details | <INSTRUMENT_NAME> |
      | Status  | Skipped           |
      | Amount  | <AMOUNT>          |

    Examples:
      | PAYMENT_METHOD             | INSTRUMENT_NAME | AMOUNT  |
      | Reserve Unsupported Config | my visa         | $131.50 |