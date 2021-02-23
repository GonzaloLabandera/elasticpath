@regressionTest @customerService @order @orderExchange
Feature: Order Exchange

  Background:
    Given I sign in to CM as CSR user

  Scenario Outline: Create Exchange with another product
    Given I have an order for scope <scope> with following skus
      | skuCode             | quantity |
      | <original-sku-code> | 1        |
    And I go to Customer Service
    And I search and open order editor for the latest order
    And I complete the order shipment
    When I create a exchange with following values
      | Return Qty        | 1                                                       |
      | Return Sku Code   | <original-sku-code>                                     |
      | Exchange Sku Code | <exchange-sku-code>                                     |
      | Price List Name   | Mobile Price List                                       |
      | Shipping Address  | Test User, 555 Elliott Avenue W, Seattle, WA, 98119, US |
      | Shipping Method   | FedEx Express                                           |
      | Payment Options   | Payment Source                                          |
      | Payment Source    | Original payment source                                 |
    Then I should see the returned sku code <original-sku-code>
    When I open the exchange order editor
    Then I should see the original order# as External Order# and exchange order# as Order#
    And I should see the following sku in item list
      | <exchange-sku-code> |

    Examples:
      | scope | original-sku-code       | exchange-sku-code |
      | mobee | handsfree_shippable_sku | t384lkef          |

  Scenario Outline: Create Exchange with physical return
    Given I have an order for scope <scope> with following skus
      | skuCode    | quantity |
      | <sku-code> | 1        |
    And I search and open order editor for the latest order
    And I complete the order shipment
    When I create a exchange with following values
      | Return Qty       | 1                                                       |
      | Return Sku Code  | <sku-code>                                              |
      | Shipping Address | Test User, 555 Elliott Avenue W, Seattle, WA, 98119, US |
      | Shipping Method  | FedEx Express                                           |
      | Payment Options  | Payment Source                                          |
      | Payment Source   | Original payment source                                 |
    Then I should NOT see following order payment transaction in the Payment History
      | Type   | Credit   |
      | Status | Approved |
    When I open the exchange order editor
    Then I should see following order payment transaction in the Payment History
      | Type   | Reserve          |
      | Status | Approved         |
      | Amount | <reserve-amount> |

    Examples:
      | scope | sku-code     | reserve-amount |
      | mobee | physical_sku | $131.50        |

  Scenario Outline: Complete Exchange with physical return
    Given I have an order for scope <scope> with following skus
      | skuCode    | quantity |
      | <sku-code> | 1        |
    And I go to Customer Service
    And I search and open order editor for the latest order
    And I complete the order shipment
    When I create a exchange with following values
      | Return Qty       | 1                                                       |
      | Return Sku Code  | <sku-code>                                              |
      | Shipping Address | Test User, 555 Elliott Avenue W, Seattle, WA, 98119, US |
      | Shipping Method  | FedEx Express                                           |
      | Payment Options  | Payment Source                                          |
      | Payment Source   | Original payment source                                 |
    And shipping receive return is processed for quantity 1 of sku <sku-code>
    And I complete the exchange refunding to original source
    Then I should see following order payment transaction in the Payment History
      | Type   | Credit            |
      | Status | Approved          |
      | Amount | -$<refund-amount> |

    Examples:
      | scope | sku-code     | refund-amount |
      | mobee | physical_sku | 25.00         |

  Scenario Outline: Complete Exchange refunding manually without return
    Given I have an order for scope <scope> with following skus
      | skuCode    | quantity |
      | <sku-code> | 1        |
    And I go to Customer Service
    And I search and open order editor for the latest order
    And I complete the order shipment
    When I create a exchange with following values
      | Return Qty       | 1                                                       |
      | Return Sku Code  | <sku-code>                                              |
      | Shipping Address | Test User, 555 Elliott Avenue W, Seattle, WA, 98119, US |
      | Shipping Method  | FedEx Express                                           |
      | Manual Refund    | true                                                    |
      | Payment Options  | Payment Source                                          |
      | Payment Source   | Original payment source                                 |
    Then I should see following order payment transaction in the Payment History
      | Type   | Manual Credit     |
      | Status | Approved          |
      | Amount | -$<refund-amount> |
    When I open the exchange order editor
    Then I should see following order payment transaction in the Payment History
      | Type   | Reserve          |
      | Status | Approved         |
      | Amount | <reserve-amount> |

    Examples:
      | scope | sku-code     | refund-amount | reserve-amount |
      | mobee | physical_sku | 25.00         | $131.50        |

  Scenario Outline: Create Exchange and ship to billing address used in Payment Instrument
    Given I have authenticated as a newly registered shopper
    And I get the list of payment methods from my profile
    And I open Address Required Happy Path Config payment method
    And I open payment instrument form
    And I create a payment instrument with a valid address and data
    And I make a purchase with newly created payment instrument for scope <scope> with following skus
      | skuCode    | quantity |
      | <sku-code> | 1        |
    And I search and open order editor for the latest order
    And I complete the order shipment
    When I create a exchange with following values
      | Return Qty       | 1                                                                                 |
      | Return Sku Code  | <sku-code>                                                                        |
      | Shipping Address | given-name family-name, 123 Broadway, extended address, Vancouver, BC, V7V7V7, CA |
      | Shipping Method  | Canada Post 2 days                                                                |
      | Payment Options  | Payment Source                                                                    |
      | Payment Source   | Original payment source                                                           |
    Then I should see the returned sku code <sku-code>

    Examples:
      | scope | sku-code     |
      | mobee | physical_sku |

  Scenario Outline: Exchange and Reserve with alternate payment source
    Given I login as a newly registered shopper
    And I have created Happy Path Config payment instrument on my profile with the following fields:
      | display-name | <ALTERNATE_PAYMENT> |
      | PIC Field A  | Test PIC Value A    |
      | PIC Field B  | Test PIC Value B    |
    And I create an unsaved Smart Path Config payment instrument from order supplying following fields:
      | display-name | <ORIGINAL_PAYMENT> |
    And I create an order with US address with following skus
      | skuCode | quantity |
      | <SKU>   | 1        |
    And I search and open order editor for the latest order
    And I complete the order shipment
    When I create a exchange with following values
      | Return Qty        | 1                                                       |
      | Return Sku Code   | <SKU>                                                   |
      | Exchange Sku Code | <SKU>                                                   |
      | Return Required   | False                                                   |
      | Price List Name   | Mobile Price List                                       |
      | Shipping Address  | Test User, 555 Elliott Avenue W, Seattle, WA, 98119, US |
      | Shipping Method   | FedEx Express                                           |
      | Payment Options   | Alternate payment source                                |
      | Payment Source    | <ALTERNATE_PAYMENT>                                     |
    Then I should see following order payment transaction in the Payment History
      | Type    | Credit             |
      | Details | <ORIGINAL_PAYMENT> |
      | Status  | Approved           |
      | Amount  | -$<REFUND_AMOUNT>  |
    When I open the exchange order editor
    Then I should see following order payment transaction in the Payment History
      | Type    | Reserve             |
      | Details | <ALTERNATE_PAYMENT> |
      | Status  | Approved            |
      | Amount  | $<RESERVE_AMOUNT>   |

    Examples:
      | ORIGINAL_PAYMENT | ALTERNATE_PAYMENT | SKU          | REFUND_AMOUNT | RESERVE_AMOUNT |
      | original pay     | alternate payment | physical_sku | 25.00         | 131.50         |

  Scenario Outline: Exchange free item should not have Credit and Reserve payment events
    Given I have an order with Canadian address for scope mobee with following skus
      | skuCode    | quantity |
      | <FREE_SKU> | 1        |
    And I go to Customer Service
    And I search and open order editor for the latest order
    And I complete the order shipment
    When I create a exchange with following values
      | Return Qty        | 1                                                     |
      | Return Sku Code   | <FREE_SKU>                                            |
      | Exchange Sku Code | <FREE_SKU>                                            |
      | Price List Name   | Mobile Price List                                     |
      | Shipping Address  | Test User, 123 Somestreet, Vancouver, BC, V1V 2K2, CA |
      | Shipping Method   | Canada Post 2 days                                    |
      | Payment Options   | Payment Source                                        |
      | Payment Source    | Original payment source                               |
    Then I should NOT see Credit order payment transaction type in the Payment History
    When I open the exchange order editor
    Then I should NOT see Reserve order payment transaction type in the Payment History

    Examples:
      | FREE_SKU                |
      | physicalFreeProduct_sku |

  Scenario Outline: Exchange free item and Reserve on alternate payment source for additional costs
    Given I login as a newly registered shopper
    And I have created Happy Path Config payment instrument on my profile with the following fields:
      | display-name | <ALTERNATE_PAYMENT> |
      | PIC Field A  | Test PIC Value A    |
      | PIC Field B  | Test PIC Value B    |
    And I create an unsaved Smart Path Config payment instrument from order supplying following fields:
      | display-name | <ORIGINAL_PAYMENT> |
    And I create an order with US address with following skus
      | skuCode    | quantity |
      | <FREE_SKU> | 1        |
    And I search and open order editor for the latest order
    And I complete the order shipment
    When I create a exchange with following values
      | Return Qty        | 1                                                       |
      | Return Sku Code   | <FREE_SKU>                                              |
      | Exchange Sku Code | <FREE_SKU>                                              |
      | Return Required   | False                                                   |
      | Price List Name   | Mobile Price List                                       |
      | Shipping Address  | Test User, 555 Elliott Avenue W, Seattle, WA, 98119, US |
      | Shipping Method   | FedEx Express                                           |
      | Payment Options   | Alternate payment source                                |
      | Payment Source    | <ALTERNATE_PAYMENT>                                     |
    Then I should NOT see Credit order payment transaction type in the Payment History
    When I open the exchange order editor
    Then I should see following order payment transaction in the Payment History
      | Type    | Reserve             |
      | Details | <ALTERNATE_PAYMENT> |
      | Status  | Approved            |
      | Amount  | $<RESERVE_AMOUNT>   |

    Examples:
      | ORIGINAL_PAYMENT | ALTERNATE_PAYMENT | FREE_SKU                | RESERVE_AMOUNT |
      | original pay     | alternate payment | physicalFreeProduct_sku | 106.50         |

  Scenario Outline: Public shopper can exchange free item without additional payment reservations
    Given I am logged into scope mobee as a public shopper
    And I create an unsaved Smart Path Config payment instrument from order supplying following fields:
      | display-name | test |
    And I create an order with CA address with following skus
      | skuCode    | quantity |
      | <FREE_SKU> | 1        |
    And I search and open order editor for the latest order
    And I complete the order shipment
    When I create exchange with adjusted shipping costs
      | Return Qty             | 1                                                     |
      | Return Sku Code        | <FREE_SKU>                                            |
      | Exchange Sku Code      | <FREE_SKU>                                            |
      | Return Required        | False                                                 |
      | Price List Name        | Mobile Price List                                     |
      | Shipping Address       | Test User, 123 Somestreet, Vancouver, BC, V1V 2K2, CA |
      | Shipping Method        | Canada Post 2 days                                    |
      | Adjusted Shipping Cost | 0                                                     |
      | Payment Options        | Payment Source                                        |
      | Payment Source         | Original payment source                               |
    Then I should NOT see Credit order payment transaction type in the Payment History
    When I open the exchange order editor
    Then I should NOT see Reserve order payment transaction type in the Payment History

    Examples:
      | FREE_SKU                |
      | physicalFreeProduct_sku |

  Scenario Outline: Error messages appear if there is no payment instrument
    Given I am logged into scope mobee as a public shopper
    And I create an order with CA address with following skus
      | skuCode    | quantity |
      | <FREE_SKU> | 1        |
    And I search and open order editor for the latest order
    And I complete the order shipment
    When I create a exchange with error
      | Return Qty               | 1                                                     |
      | Return Sku Code          | <FREE_SKU>                                            |
      | Exchange Sku Code        | <FREE_SKU>                                            |
      | Price List Name          | Mobile Price List                                     |
      | Shipping Address         | Test User, 123 Somestreet, Vancouver, BC, V1V 2K2, CA |
      | Shipping Method          | Fixed Price No Promo Shipping Option                  |
      | Payment Options          | Payment Source                                        |
      | Payment Source           | Original payment source                               |
      | Error processing payment | True                                                  |
    Then Error message No original payment instrument was found for the order. appears

    Examples:
      | FREE_SKU                |
      | physicalFreeProduct_sku |

  Scenario Outline: Cancel the exchange order if the refund was unsuccessful
    Given I have authenticated as a newly registered shopper
    And I create an unsaved <PAYMENT_METHOD> payment instrument from order supplying following fields:
      | display-name | <INSTRUMENT_NAME> |
    And I make a purchase with newly created payment instrument for scope mobee with following skus
      | skuCode    | quantity |
      | <sku-code> | 1        |
    And I search and open order editor for the latest order
    And I complete the order shipment
    When I create a exchange with error
      | Return Qty                       | 1                                                       |
      | Return Sku Code                  | <sku-code>                                              |
      | Shipping Address                 | Test User, 555 Elliott Avenue W, Seattle, WA, 98119, US |
      | Shipping Method                  | FedEx Express                                           |
      | Return Required                  | False                                                   |
      | Payment Options                  | Payment Source                                          |
      | Payment Source                   | Original payment source                                 |
      | Exchange order will be cancelled | True                                                    |
    And I search and open order editor for the latest exchange order
    Then the order status should be Cancelled
    Examples:
      | PAYMENT_METHOD            | sku-code     | INSTRUMENT_NAME |
      | Credit Unsupported Config | physical_sku | my visa         |

  Scenario Outline: Alternate Payment Source selection validation to ensure alternate payment source is selected
    Given I have authenticated as a newly registered shopper
    And I have created Happy Path Config payment instrument on my profile with the following fields:
      | display-name | <ALTERNATE_PAYMENT> |
      | PIC Field A  | Test PIC Value A    |
      | PIC Field B  | Test PIC Value B    |
    And I create an unsaved Smart Path Config payment instrument from order supplying following fields:
      | display-name | <ORIGINAL_PAYMENT> |
    And I make a purchase with newly created payment instrument for scope mobee with following skus
      | skuCode | quantity |
      | <SKU>   | 1        |
    And I search and open order editor for the latest order
    And I complete the order shipment
    When I create a exchange with following values
      | Return Qty                                | 1                                                       |
      | Return Sku Code                           | <SKU>                                                   |
      | Exchange Sku Code                         | <SKU>                                                   |
      | Return Required                           | False                                                   |
      | Price List Name                           | Mobile Price List                                       |
      | Shipping Address                          | Test User, 555 Elliott Avenue W, Seattle, WA, 98119, US |
      | Shipping Method                           | FedEx Express                                           |
      | Payment Options                           | Alternate payment source                                |
      | Not Selected Payment Source error message | true                                                    |
      | Payment Source                            | <ALTERNATE_PAYMENT>                                     |
    Then I should see following order payment transaction in the Payment History
      | Type    | Credit             |
      | Details | <ORIGINAL_PAYMENT> |
      | Status  | Approved           |
      | Amount  | -$<REFUND_AMOUNT>  |
    When I open the exchange order editor
    Then I should see following order payment transaction in the Payment History
      | Type    | Reserve             |
      | Details | <ALTERNATE_PAYMENT> |
      | Status  | Approved            |
      | Amount  | $<ORDER_TOTAL>      |

    Examples:
      | ORIGINAL_PAYMENT | ORDER_TOTAL | REFUND_AMOUNT | ALTERNATE_PAYMENT | SKU          |
      | Smart path       | 131.50      | 25.00         | alternate payment | physical_sku |

  Scenario Outline: Cancel Exchange should cancel RMA and Exchange Order
    Given I have an order for scope <scope> with following skus
      | skuCode    | quantity |
      | <sku-code> | 1        |
    And I search and open order editor for the latest order
    And I complete the order shipment
    And I create a exchange with following values
      | Return Qty       | 1                                                       |
      | Return Sku Code  | <sku-code>                                              |
      | Shipping Address | Test User, 555 Elliott Avenue W, Seattle, WA, 98119, US |
      | Shipping Method  | FedEx Express                                           |
      | Payment Options  | Payment Source                                          |
      | Payment Source   | Original payment source                                 |
    When I cancel the exchange under the original order
    Then The status of exchange order is Cancelled
    And The RMA status is Cancelled

    Examples:
      | scope | sku-code     |
      | mobee | physical_sku |

  Scenario Outline: Complete Exchange when the Exchange Order is cancelled
    Given I have an order for scope <scope> with following skus
      | skuCode    | quantity |
      | <sku-code> | 1        |
    And I go to Customer Service
    And I search and open order editor for the latest order
    And I complete the order shipment
    And I create a exchange with following values
      | Return Qty       | 1                                                       |
      | Return Sku Code  | <sku-code>                                              |
      | Shipping Address | Test User, 555 Elliott Avenue W, Seattle, WA, 98119, US |
      | Shipping Method  | FedEx Express                                           |
      | Payment Options  | Payment Source                                          |
      | Payment Source   | Original payment source                                 |
    And I open the exchange order editor
    And I cancel the order
    And shipping receive return is processed for quantity 1 of sku <sku-code>
    When I complete the exchange refunding to original source
    Then I should see following order payment transaction in the Payment History
      | Type   | Credit   |
      | Status | Approved |
      | Amount | -$25.00  |

    Examples:
      | scope | sku-code     |
      | mobee | physical_sku |

  Scenario Outline: Complete Exchange with return when credit capability unsupported
    Given I have authenticated as a newly registered shopper
    And I create an unsaved <PAYMENT_METHOD> payment instrument from order supplying following fields:
      | display-name | <INSTRUMENT_NAME> |
    And I make a purchase with newly created payment instrument for scope mobee with following skus
      | skuCode    | quantity |
      | <sku-code> | 1        |
    And I search and open order editor for the latest order
    And I complete the order shipment
    When I create a exchange with error
      | Return Qty                       | 1                                                       |
      | Return Sku Code                  | <sku-code>                                              |
      | Shipping Address                 | Test User, 555 Elliott Avenue W, Seattle, WA, 98119, US |
      | Shipping Method                  | FedEx Express                                           |
      | Return Required                  | True                                                    |
      | Payment Options                  | Payment Source                                          |
      | Payment Source                   | Original payment source                                 |
      | Exchange order will be cancelled | False                                                   |
    And shipping receive return is processed for quantity 1 of sku <sku-code>
    And I complete the exchange refunding to original source with error
    Then Error message Capability is not supported by payment provider. appears
    And I click cancel in exchange window

    Examples:
      | PAYMENT_METHOD            | INSTRUMENT_NAME | sku-code     |
      | Credit Unsupported Config | my visa         | physical_sku |

  Scenario Outline: Exchange dialog Authorize button should be inactive on step 1 and 2
    Given I have an order for scope <scope> with following skus
      | skuCode    | quantity |
      | <sku-code> | 1        |
    And I search and open order editor for the latest order
    And I complete the order shipment
    When I prepare an exchange with following values
      | Return Qty       | 1                                                       |
      | Return Sku Code  | <sku-code>                                              |
      | Shipping Address | Test User, 555 Elliott Avenue W, Seattle, WA, 98119, US |
      | Shipping Method  | FedEx Express                                           |
      | Payment Options  | Payment Source                                          |
      | Payment Source   | Original payment source                                 |
    Then Authorize Exchange button should be enabled
    And Authorize Exchange button should be disabled on previous page
    And Authorize Exchange button should be disabled on previous page

    Examples:
      | scope | sku-code     |
      | mobee | physical_sku |

  Scenario Outline: Exchange is cancelled if available refund amount is insufficient
    Given I have authenticated as a newly registered shopper
    And I create an unsaved <PAYMENT_METHOD> payment instrument from order supplying following fields:
      | display-name | <INSTRUMENT_NAME> |
    And I make a purchase with newly created payment instrument for scope mobee with following skus
      | skuCode    | quantity |
      | <sku-code> | 1        |
    And I search and open order editor for the latest order
    And I complete the order shipment
    And I create a refund with following values
      | Available Refund Amount | $131.50                 |
      | Currency Code           | CAD                     |
      | Refund Amount           | 120                     |
      | Refund Note             | Refund                  |
      | Payment Source          | Original payment source |
    When I create a exchange with error
      | Return Qty                       | 1                                                       |
      | Return Sku Code                  | <sku-code>                                              |
      | Shipping Address                 | Test User, 555 Elliott Avenue W, Seattle, WA, 98119, US |
      | Shipping Method                  | FedEx Express                                           |
      | Return Required                  | False                                                   |
      | Payment Options                  | Payment Source                                          |
      | Payment Source                   | Original payment source                                 |
      | Exchange order will be cancelled | True                                                    |
    Then I should NOT see following order payment transaction in the Payment History
      | Type    | Credit            |
      | Details | <INSTRUMENT_NAME> |
      | Status  | Approved          |
      | Amount  | -$25.00           |
    When I search and open order editor for the latest exchange order
    And the order status should be Cancelled

    Examples:
      | PAYMENT_METHOD    | sku-code     | INSTRUMENT_NAME |
      | Smart Path Config | physical_sku | my visa         |