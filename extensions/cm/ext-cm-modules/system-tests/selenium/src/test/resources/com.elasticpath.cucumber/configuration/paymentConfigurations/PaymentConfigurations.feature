@regressionTest @configuration @paymentConfiguration
Feature: Payment Configuration

  Background:
    Given I sign in to CM as admin user
    And I go to Configuration
    And I go to Payment Configurations
    And I have authenticated as a newly registered shopper

  Scenario: Create new payment configuration
    When I create a new Payment Configuration with following details
      | PROVIDER           | Happy Path          |
      | METHOD             | CARD METHOD         |
      | CONFIGURATION_NAME | Test payment config |
      | DISPLAY_NAME       | Test payment config |
    And I configure the payment configuration properties
      | Config A | value A |
      | Config B | value B |
    And I save the payment configuration
    Then the newly created payment configuration status is Draft

  Scenario: Activate payment configuration
    Given I create a new Payment Configuration with following details
      | PROVIDER           | Happy Path          |
      | METHOD             | CARD METHOD         |
      | CONFIGURATION_NAME | Test payment config |
      | DISPLAY_NAME       | Test payment config |
    And I save the payment configuration
    When I activate the newly created payment configuration
    Then the newly created payment configuration status is Active

  Scenario: Disable payment configuration in Draft status
    Given I create a new Payment Configuration with following details
      | PROVIDER           | Happy Path          |
      | METHOD             | CARD METHOD         |
      | CONFIGURATION_NAME | Test payment config |
      | DISPLAY_NAME       | Test payment config |
    And I save the payment configuration
    When I disable the newly created payment configuration
    Then the newly created payment configuration status is Disabled

  Scenario: Disable payment configuration in Active status
    Given I create a new Payment Configuration with following details
      | PROVIDER           | Happy Path          |
      | METHOD             | CARD METHOD         |
      | CONFIGURATION_NAME | Test payment config |
      | DISPLAY_NAME       | Test payment config |
    And I save the payment configuration
    And I activate the newly created payment configuration
    And the newly created payment configuration status is Active
    When I disable the newly created payment configuration
    Then the newly created payment configuration status is Disabled

  Scenario: Unable to disable payment configuration if associated with store
    Given the payment configuration Angry Path Config is associated with store
    When I disable the payment configuration Angry Path Config
    Then the disable payment configuration action is denied
    And the payment configuration Angry Path Config status is Active

  Scenario: Payment configuration in Draft state should be editable
    Given I create a new Payment Configuration with following details
      | PROVIDER           | Happy Path          |
      | METHOD             | CARD METHOD         |
      | CONFIGURATION_NAME | Test payment config |
      | DISPLAY_NAME       | Test payment config |
    And I configure the payment configuration properties
      | Config A | value A |
      | Config B | value B |
    And I save the payment configuration
    When I open the edit payment configuration dialog
    And I configure the payment configuration name New name
    And I configure the payment configuration display name New name
    And I configure the localized display name New localized name for English locale
    And I update the payment configuration properties
      | Property name | Old value | New value   |
      | Config A      | value A   | value A new |
      | Config B      | value B   | value A new |
    And I save the payment configuration
    Then the newly created payment configuration status is Draft

  Scenario: Payment configuration values in Active state should not be interactive
    Given I create a new Payment Configuration with following details
      | PROVIDER           | Happy Path          |
      | METHOD             | CARD METHOD         |
      | CONFIGURATION_NAME | Test payment config |
      | DISPLAY_NAME       | Test payment config |
    And I configure the payment configuration properties
      | Config A | value A |
      | Config B | value B |
    And I save the payment configuration
    And I activate the newly created payment configuration
    When I open the edit payment configuration dialog
    And I configure the payment configuration name New name
    And I configure the payment configuration display name New name
    And I configure the localized display name New localized name for English locale
    And Payment configuration values should not be interactive
      | Config A | value A |
      | Config B | value B |
    And I save the payment configuration
    Then the newly created payment configuration status is Active

  Scenario Outline: Payment configuration cannot be saved when configuration name is not unique
    Given the payment configuration <PAYMENT_CONFIG_NAME> exists in the list
    And I create a new Payment Configuration with following details
      | PROVIDER           | Happy Path          |
      | METHOD             | CARD METHOD         |
      | CONFIGURATION_NAME | Test payment config |
      | DISPLAY_NAME       | Test payment config |
    And I save the payment configuration
    And the newly created payment configuration exists in the list
    When I open the edit payment configuration dialog
    And I update the payment configuration name to use an existing name <PAYMENT_CONFIG_NAME>
    Then Save payment configuration button is disabled with a validation message <VALIDATION_MESSAGE>

    Examples:
      | PAYMENT_CONFIG_NAME | VALIDATION_MESSAGE                                    |
      | Happy Path Config   | A payment configuration with this name already exists |

  Scenario: Create Payment Configuration with localization
    Given I create a new Payment Configuration with following details
      | PROVIDER           | Happy Path           |
      | METHOD             | CARD METHOD          |
      | CONFIGURATION_NAME | ATest payment config |
      | DISPLAY_NAME       | ATest payment config |
    And I configure the first localized display name Test Payment EN name for English locale
    And I click add localized properties
    And I configure the second localized display name Test Payment FR name for French locale
    And I save the payment configuration
    And I activate the newly created payment configuration
    And I select and save the newly created payment configuration for store MOBEE
    And I get the list of payment methods from my profile
    When I open newly created payment method for the English language
    Then the field display-name has value Test Payment EN name

  Scenario: Delete Payment Configuration localization
    Given I create a new Payment Configuration with following details
      | PROVIDER           | Happy Path           |
      | METHOD             | CARD METHOD          |
      | CONFIGURATION_NAME | ATest payment config |
      | DISPLAY_NAME       | ATest payment config |
    And I configure the first localized display name Test Payment EN name for English locale
    And I click add localized properties
    And I configure the second localized display name Test Payment FR name for French locale
    And I save the payment configuration
    And I activate the newly created payment configuration
    And I select and save the newly created payment configuration for store MOBEE
    And I go to Payment Configurations
    When I open the edit payment configuration dialog
    And I click delete first localization
    And I save the payment configuration
    And I get the list of payment methods from my profile
    And I open newly created payment method for the English language
    Then the field display-name has value ATest payment config

  Scenario Outline: Shopper cannot purchase with a order payment instrument when it is not associated with the store
    Given I add item with code <ITEM_CODE> to my cart
    And I create a new Payment Configuration with following details
      | PROVIDER           | Smart Path           |
      | METHOD             | CARD                 |
      | CONFIGURATION_NAME | ATest payment config |
      | DISPLAY_NAME       | ATest payment config |
    And I save the payment configuration
    And I activate the newly created payment configuration
    And I select and save the newly created payment configuration for store MOBEE
    And I get the list of payment methods from my order
    And I open newly created payment method for the English language
    And I create payment instrument supplying following fields:
      | display-name | <Name> |
    When I go to Stores
    And I deselect and save the newly created payment configuration for store MOBEE
    Then I retrieve the order
    And there is needinfo with id need.payment.method and debug message Payment method must be specified.
    And I retrieve the purchase form
    And there is needinfo with id need.payment.method and debug message Payment method must be specified.

    Examples:
      | Name                    | ITEM_CODE    |
      | Payment Instrument Name | physical_sku |

  Scenario Outline: Shopper cannot purchase with a profile payment instrument when it is not associated with the store
    Given I add item with code <ITEM_CODE> to my cart
    And I create a new Payment Configuration with following details
      | PROVIDER           | Smart Path           |
      | METHOD             | CARD                 |
      | CONFIGURATION_NAME | ATest payment config |
      | DISPLAY_NAME       | ATest payment config |
    And I save the payment configuration
    And I activate the newly created payment configuration
    And I select and save the newly created payment configuration for store MOBEE
    And I get the list of payment methods from my profile
    And I open newly created payment method for the English language
    And I create payment instrument supplying following fields:
      | display-name | <Name> |
    When I go to Stores
    And I deselect and save the newly created payment configuration for store MOBEE
    Then I retrieve the order
    And there is needinfo with id need.payment.method and debug message Payment method must be specified.
    And I retrieve the purchase form
    And there is needinfo with id need.payment.method and debug message Payment method must be specified.

    Examples:
      | Name                    | ITEM_CODE    |
      | Payment Instrument Name | physical_sku |

  Scenario Outline: Shopper can purchase with a profile payment instrument when it is associated with the store
    Given I add item with code <ITEM_CODE> to my cart
    And I create a new Payment Configuration with following details
      | PROVIDER           | Smart Path           |
      | METHOD             | CARD                 |
      | CONFIGURATION_NAME | ATest payment config |
      | DISPLAY_NAME       | ATest payment config |
    And I save the payment configuration
    And I activate the newly created payment configuration
    And I select and save the newly created payment configuration for store MOBEE
    And I get the list of payment methods from my profile
    And I open newly created payment method for the English language
    And I create payment instrument supplying following fields:
      | display-name | <Name> |
    And I retrieve the order
    And there is no needinfo with id need.payment.method
    And I retrieve the purchase form
    And there is no needinfo with id need.payment.method

    Examples:
      | Name                    | ITEM_CODE    |
      | Payment Instrument Name | physical_sku |

  Scenario Outline: Shopper can purchase with a order payment instrument when it is associated with the store
    Given I add item with code <ITEM_CODE> to my cart
    And I create a new Payment Configuration with following details
      | PROVIDER           | Smart Path           |
      | METHOD             | CARD                 |
      | CONFIGURATION_NAME | ATest payment config |
      | DISPLAY_NAME       | ATest payment config |
    And I save the payment configuration
    And I activate the newly created payment configuration
    And I select and save the newly created payment configuration for store MOBEE
    And I get the list of payment methods from my order
    And I open newly created payment method for the English language
    And I create payment instrument supplying following fields:
      | display-name | <Name> |
    And I retrieve the order
    And there is no needinfo with id need.payment.method
    And I retrieve the purchase form
    And there is no needinfo with id need.payment.method

    Examples:
      | Name                    | ITEM_CODE    |
      | Payment Instrument Name | physical_sku |

  Scenario: Payment configuration no longer exist in payment methods if it is disabled
    Given I create a new Payment Configuration with following details
      | PROVIDER           | Happy Path          |
      | METHOD             | CARD METHOD         |
      | CONFIGURATION_NAME | Test payment config |
      | DISPLAY_NAME       | Test payment config |
    And I save the payment configuration
    And I activate the newly created payment configuration
    And I select the newly created payment configuration for store MOBEE
    And I go to Payment Configurations
    When I disable the newly created payment configuration
    And I save the newly created payment configuration
    Then the following store warning message is displayed: Please, reload Payments tab.
    And the newly created payment method is not visible in my profile