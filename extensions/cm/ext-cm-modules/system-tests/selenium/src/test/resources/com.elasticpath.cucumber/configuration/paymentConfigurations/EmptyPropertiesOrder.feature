@regressionTest @configuration @paymentConfiguration
Feature: Order of empty properties

  Background:
    Given I sign in to CM as admin user
    And I go to Configuration
    And I go to Payment Configurations
    And I create a new Payment Configuration with following details
      | PROVIDER           | No Capabilities   |
      | METHOD             | CARD              |
      | CONFIGURATION_NAME | new_configuration |
    And I configure the payment configuration properties
      | B description | value B |
      | A key         |         |
      | C description |         |
    And I save the payment configuration

  Scenario: Empty Payment configuration properties are successfully saved
    When  I open the edit payment configuration dialog
    Then the payment configuration properties are in the following order
      | B description |
      | A key         |
      | C description |

  Scenario: Empty Payment configuration properties are successfully saved after editing
    When  I open the edit payment configuration dialog
    And I update the payment configuration properties
      | Property name | Old value         | New value   |
      | B description | value B           | value B new |
      | A key         | <Enter a value>   | value A new |
      | C description | <Enter a value>   |             |
    And I save the payment configuration
    And I open the edit payment configuration dialog
    Then the payment configuration properties are in the following order
      | B description |
      | A key         |
      | C description |