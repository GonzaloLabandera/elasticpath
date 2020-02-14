@regressionTest @customerService @order @orderRefund
Feature: Order Refund with payment modification

  Background:
    Given I sign in to CM as admin user
    And I go to Payment Configurations

  Scenario Outline: Refund when payment provider is detached from store
    Given I create a new Payment Configuration with following details
      | PROVIDER           | Happy Path           |
      | METHOD             | CARD METHOD          |
      | CONFIGURATION_NAME | ATest payment config |
      | DISPLAY_NAME       | ATest payment config |
	And I configure the payment configuration properties
	  | Config A | value A |
	  | Config B | value B |
	And I save the payment configuration
	And I activate the newly created payment configuration
	And I select and save the newly created payment configuration for store MOBEE
    And I have authenticated as a newly registered shopper
    And I have created payment instrument with the newly created payment configuration on my profile:
      | display-name | <INSTRUMENT_NAME> |
      | PIC Field A  | Test PIC Value A  |
      | PIC Field B  | Test PIC Value B  |
    And I make a purchase with newly created payment instrument for scope mobee with following skus
      | skuCode     | quantity |
      | digital_sku | 1        |
    And I deselect and save the newly created payment configuration for store MOBEE
    When I search and open the latest order and create a refund with following values
      | Available Refund Amount | $21.30                  |
      | Currency Code           | CAD                     |
      | Refund Amount           | <REFUND_AMOUNT>         |
      | Refund Note             | discount                |
      | Payment Source          | Original payment source |
    Then I should see following order payment transaction in the Payment History
      | Type    | Credit            |
      | Details | <INSTRUMENT_NAME> |
      | Status  | Approved          |
      | Amount  | -$<REFUND_AMOUNT> |

    Examples:
      | INSTRUMENT_NAME | REFUND_AMOUNT |
      | my visa         | 5.00          |