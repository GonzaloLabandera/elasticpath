@regressionTest @configuration @paymentConfiguration
Feature: Order of properties

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
      | A key         | value A |
      | C description | value C |
    And I save the payment configuration

  Scenario: Payment configuration properties remain in order
    When  I open the edit payment configuration dialog
    Then the payment configuration properties are in the following order
      | B description |
      | A key         |
      | C description |

  Scenario: Payment configuration properties remain in order after editing
    When  I open the edit payment configuration dialog
    And I update the payment configuration properties
      | Property name | Old value | New value   |
      | B description | value B   | value B new |
      | A key         | value A   | value A new |
      | C description | value C   | value C new |
    And I save the payment configuration
    And I open the edit payment configuration dialog
    Then the payment configuration properties are in the following order
      | B description |
      | A key         |
      | C description |