@regressionTest @customerService @singleReservePayment
Feature: Order Payments By Single Reserve Per Payment Instrument

  Background:
    Given I sign in to CM as admin user
    And I go to Payment Configurations
    And I create a new Payment Configuration with following details
      | PROVIDER           | Single Reserve Per PI Happy Path |
      | METHOD             | CARD METHOD                      |
      | CONFIGURATION_NAME | ATest payment config             |
      | DISPLAY_NAME       | ATest payment config             |
    And I configure the payment configuration properties
      | Config A | value A |
      | Config B | value B |
    And I save the payment configuration
    And I activate the newly created payment configuration
    And I select and save the newly created payment configuration for store MOBEE

  Scenario Outline: Single Reserve Per Payment Instrument is not charged when order has unshipped shipment
    Given I have authenticated as a newly registered shopper
    And I have created payment instrument with the newly created payment configuration on my profile:
      | display-name | <PAYMENT_INSTRUMENT_NAME> |
      | PIC Field A  | Test PIC Value A          |
      | PIC Field B  | Test PIC Value B          |
    When I make a purchase with newly created payment instrument for scope mobee with following skus
      | skuCode                                    | quantity |
      | bundleWithPhysicalAndDigitalComponents_sku | 1        |
    And I search and open order editor for the latest order
    Then I should see following order payment transaction in the Payment History
      | Type    | Reserve                   |
      | Details | <PAYMENT_INSTRUMENT_NAME> |
      | Status  | Approved                  |
      | Amount  | <ORDER_TOTAL>             |
    And I should NOT see Charge order payment transaction type in the Payment History

    Examples:
      | PAYMENT_INSTRUMENT_NAME | ORDER_TOTAL |
      | my PayPal               | $149.10     |

  Scenario Outline: Single Reserve Per Payment Instrument is charged when order has shipped all items
    Given I have authenticated as a newly registered shopper
    And I have created payment instrument with the newly created payment configuration on my profile:
      | display-name | <PAYMENT_INSTRUMENT_NAME> |
      | PIC Field A  | Test PIC Value A          |
      | PIC Field B  | Test PIC Value B          |
    And I make a purchase with newly created payment instrument for scope mobee with following skus
      | skuCode    | quantity |
      | <SKU_CODE> | 2        |
    And I search and open order editor for the latest order
    And I create a new shipment for sku <SKU_CODE> with following values
      | Address         | 555 Elliott Avenue W, Seattle, WA, 98119 |
      | Shipment Method | FedEx Express                            |
    And I reset order Shipping Cost value to 0
    When I complete the shipment for shipment ID 1
    And I complete the shipment for shipment ID 2
    Then I should see following order payment transaction in the Payment History
      | Type    | Charge                    |
      | Details | <PAYMENT_INSTRUMENT_NAME> |
      | Status  | Approved                  |
      | Amount  | <ORDER_TOTAL>             |

    Examples:
      | PAYMENT_INSTRUMENT_NAME | ORDER_TOTAL | SKU_CODE     |
      | my PayPal               | $156.50     | physical_sku |

  Scenario Outline: Single Reserve Per Payment Instrument can not be used for increasing order total
    Given I have authenticated as a newly registered shopper
    And I have created payment instrument with the newly created payment configuration on my profile:
      | display-name | <PAYMENT_INSTRUMENT_NAME> |
      | PIC Field A  | Test PIC Value A          |
      | PIC Field B  | Test PIC Value B          |
    And I make a purchase with newly created payment instrument for scope mobee with following skus
      | skuCode      | quantity |
      | physical_sku | 1        |
    When I search and open order editor for the latest order
    And I enter 2 for order shipment quantity
    Then Completion of Payment Authorization should be impossible with error message Payment was declined.
    And I should NOT see Modify Reserve order payment transaction type in the Payment History
    And Payment Summary should have the following totals
      | Ordered     | <order total> |
      | Paid        | $0.00         |
      | Balance Due | <order total> |
    Examples:
      | PAYMENT_INSTRUMENT_NAME | order total |
      | my PayPal               | $156.50     |

  Scenario Outline: Single Reserve Per Payment Instrument can be used for decreasing order total
    Given I have authenticated as a newly registered shopper
    And I have created payment instrument with the newly created payment configuration on my profile:
      | display-name | <PAYMENT_INSTRUMENT_NAME> |
      | PIC Field A  | Test PIC Value A          |
      | PIC Field B  | Test PIC Value B          |
    And I make a purchase with newly created payment instrument for scope mobee with following skus
      | skuCode      | quantity |
      | physical_sku | 1        |
    When I search and open order editor for the latest order
    And I modify order shipment line item discount to 10
    And I complete the order shipment
    Then I should see following order payment transaction in the Payment History
      | Type    | Charge                    |
      | Details | <PAYMENT_INSTRUMENT_NAME> |
      | Status  | Approved                  |
      | Amount  | <CHARGED_AMOUNT>          |

    Examples:
      | PAYMENT_INSTRUMENT_NAME | CHARGED_AMOUNT |
      | my PayPal               | $121.50        |

  Scenario Outline: Single Reserve Per Payment is charged for shipped item if all other shipments are cancelled
    Given I have authenticated as a newly registered shopper
    And I have created payment instrument with the newly created payment configuration on my profile:
      | display-name | <PAYMENT_INSTRUMENT_NAME> |
      | PIC Field A  | Test PIC Value A          |
      | PIC Field B  | Test PIC Value B          |
    And I make a purchase with newly created payment instrument for scope mobee with following skus
      | skuCode    | quantity |
      | <SKU_CODE> | 2        |
    And I search and open order editor for the latest order
    And I create a new shipment for sku <SKU_CODE> with following values
      | Address         | 555 Elliott Avenue W, Seattle, WA, 98119 |
      | Shipment Method | FedEx Express                            |
    And I reset order Shipping Cost value to 0
    When I complete the shipment for shipment ID 1
    Then I should NOT see Charge order payment transaction type in the Payment History
    And I cancel shipment by shipment number 2
    Then I should see following order payment transaction in the Payment History
      | Type    | Charge                    |
      | Details | <PAYMENT_INSTRUMENT_NAME> |
      | Status  | Approved                  |
      | Amount  | <ORDER_TOTAL>             |
    And I should NOT see Cancel order payment transaction type in the Payment History

    Examples:
      | PAYMENT_INSTRUMENT_NAME | ORDER_TOTAL | SKU_CODE     |
      | my PayPal               | $25.00      | physical_sku |

  Scenario Outline: Singe Use Payment Instrument cancel reserve on final cancelled shipment
    Given I have authenticated as a newly registered shopper
    And I have created payment instrument with the newly created payment configuration on my profile:
      | display-name | <PAYMENT_INSTRUMENT_NAME> |
      | PIC Field A  | Test PIC Value A          |
      | PIC Field B  | Test PIC Value B          |
    And I make a purchase with newly created payment instrument for scope mobee with following skus
      | skuCode    | quantity |
      | <SKU_CODE> | 2        |
    And I search and open order editor for the latest order
    And I create a new shipment for sku <SKU_CODE> with following values
      | Address         | 555 Elliott Avenue W, Seattle, WA, 98119 |
      | Shipment Method | FedEx Express                            |
    And I reset order Shipping Cost value to 0
    When I cancel shipment by shipment number 1
    Then I should NOT see Cancel Reserve order payment transaction type in the Payment History
    When I cancel shipment by shipment number 2
    Then I should see following order payment transaction in the Payment History
      | Type    | Cancel Reserve            |
      | Details | <PAYMENT_INSTRUMENT_NAME> |
      | Status  | Approved                  |
      | Amount  | <ORDER_TOTAL>             |
    And I should NOT see Charge order payment transaction type in the Payment History

    Examples:
      | PAYMENT_INSTRUMENT_NAME | ORDER_TOTAL | SKU_CODE     |
      | my PayPal               | $156.50     | physical_sku |

  Scenario Outline: Refund to Single Reserve per PI payment instrument
    Given I have authenticated as a newly registered shopper
    And I have created payment instrument with the newly created payment configuration on my profile:
      | display-name | <PAYMENT_INSTRUMENT_NAME> |
      | PIC Field A  | Test PIC Value A          |
      | PIC Field B  | Test PIC Value B          |
    And I make a purchase with newly created payment instrument for scope mobee with following skus
      | skuCode      | quantity |
      | physical_sku | 1        |
    When I search and open order editor for the latest order
    And I complete the order shipment
    And I create a refund with following values
      | Available Refund Amount | <ORDER_TOTAL>           |
      | Currency Code           | CAD                     |
      | Refund Amount           | <ORDER_TOTAL>           |
      | Refund Note             | refund                  |
      | Payment Source          | Original payment source |
    Then I should see following order payment transaction in the Payment History
      | Type   | Credit          |
      | Status | Approved        |
      | Amount | -$<ORDER_TOTAL> |

    Examples:
      | PAYMENT_INSTRUMENT_NAME | ORDER_TOTAL |
      | my PayPal               | 131.50      |

  Scenario Outline: Order Exchange can Refund to original payment source for Single Reserve payment instrument while Reserve with alternate payment source
    Given I have authenticated as a newly registered shopper
    And I create a default Smart Path Config payment instrument from order supplying following fields:
      | display-name | <ALTERNATE_PAYMENT> |
    And I have created payment instrument with the newly created payment configuration on my profile:
      | display-name | <PAYMENT_INSTRUMENT_NAME> |
      | PIC Field A  | Test PIC Value A          |
      | PIC Field B  | Test PIC Value B          |
    And I make a purchase with newly created payment instrument for scope mobee with following skus
      | skuCode | quantity |
      | <SKU>   | 1        |
    And I search and open order editor for the latest order
    And I complete the order shipment
    When I create a exchange with following values
      | Return Qty        | 1                         |
      | Return Sku Code   | <SKU>                     |
      | Exchange Sku Code | <SKU>                     |
      | Return Required   | False                     |
      | Price List Name   | Mobile Price List         |
      | Shipping Address  | Test User, 98119, Seattle |
      | Shipping Method   | FedEx Express             |
      | Payment Options   | Alternate payment source  |
      | Payment Source    | <ALTERNATE_PAYMENT>       |
    Then I should see following order payment transaction in the Payment History
      | Type    | Credit                    |
      | Details | <PAYMENT_INSTRUMENT_NAME> |
      | Status  | Approved                  |
      | Amount  | -$<REFUND_AMOUNT>         |
    When I open the exchange order editor
    Then I should see following order payment transaction in the Payment History
      | Type    | Reserve             |
      | Details | <ALTERNATE_PAYMENT> |
      | Status  | Approved            |
      | Amount  | $<ORDER_TOTAL>      |

    Examples:
      | PAYMENT_INSTRUMENT_NAME           | ORDER_TOTAL | REFUND_AMOUNT | ALTERNATE_PAYMENT | SKU          |
      | Single Reserve payment instrument | 131.50      | 25.00         | alternate payment | physical_sku |

  Scenario Outline: Order exchange can be completed to refund to Single Reserve per PI payment instrument.
    Given I have authenticated as a newly registered shopper
    And I create a default Smart Path Config payment instrument from order supplying following fields:
      | display-name | <ALTERNATE_PAYMENT> |
    And I have created payment instrument with the newly created payment configuration on my profile:
      | display-name | <PAYMENT_INSTRUMENT_NAME> |
      | PIC Field A  | Test PIC Value A          |
      | PIC Field B  | Test PIC Value B          |
    And I make a purchase with newly created payment instrument for scope mobee with following skus
      | skuCode | quantity |
      | <SKU>   | 1        |
    And I search and open order editor for the latest order
    And I complete the order shipment
    When I create a exchange with following values
      | Return Qty                                | 1                         |
      | Return Sku Code                           | <SKU>                     |
      | Exchange Sku Code                         | <SKU>                     |
      | Return Required                           | true                      |
      | Price List Name                           | Mobile Price List         |
      | Shipping Address                          | Test User, 98119, Seattle |
      | Shipping Method                           | FedEx Express             |
      | Payment Options                           | Alternate payment source  |
      | Payment Source                            | <ALTERNATE_PAYMENT>       |
    And shipping receive return is processed for quantity 1 of sku <SKU>
    And I complete the exchange refunding to original source
    Then I should see following order payment transaction in the Payment History
      | Type   | Credit            |
      | Status | Approved          |
      | Amount | -$<REFUND_AMOUNT> |

    Examples:
      | PAYMENT_INSTRUMENT_NAME           | REFUND_AMOUNT | ALTERNATE_PAYMENT | SKU          |
      | Single Reserve payment instrument | 25.00         | alternate payment | physical_sku |