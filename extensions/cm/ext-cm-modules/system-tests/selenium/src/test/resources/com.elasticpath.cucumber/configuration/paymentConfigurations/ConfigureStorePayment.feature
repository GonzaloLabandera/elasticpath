@regressionTest @configuration @paymentConfiguration
Feature: Store Payment Configurations

  Background:
    Given I sign in to CM as admin user
    And I go to Configuration

  Scenario: Payment instrument can be created using newly created payment configuration
    Given I go to Payment Configurations
    And I create a new Payment Configuration with following details
      | PROVIDER           | Happy Path           |
      | METHOD             | CARD METHOD          |
      | CONFIGURATION_NAME | ATest payment config |
      | DISPLAY_NAME       | ATest payment config |
    And I save the payment configuration
    And I activate the newly created payment configuration
    And I select and save the newly created payment configuration for store MOBEE
    And I have authenticated as a newly registered shopper
    When I have created payment instrument with the newly created payment configuration on my profile:
      | display-name | Test PI          |
      | PIC Field A  | Test PIC Value A |
      | PIC Field B  | Test PIC Value B |
    Then the payment instrument Test PI is available in profile

  Scenario: Inactive payment configuration is not visible for shopper
    Given I go to Payment Configurations
    And I create a new Payment Configuration with following details
      | PROVIDER           | Happy Path           |
      | METHOD             | CARD METHOD          |
      | CONFIGURATION_NAME | ATest payment config |
      | DISPLAY_NAME       | ATest payment config |
    And I save the payment configuration
    When I edit the existing store MOBEE
    Then the inactive payment configuration should not be available for store payment configuration
    And the unassociated with store payment configuration is not visible in store MOBEE

  Scenario: Active payment configuration not associated with store should not be visible to shopper
    Given I go to Payment Configurations
    And I create a new Payment Configuration with following details
      | PROVIDER           | Happy Path           |
      | METHOD             | CARD METHOD          |
      | CONFIGURATION_NAME | ATest payment config |
      | DISPLAY_NAME       | ATest payment config |
    And I save the payment configuration
    And I activate the newly created payment configuration
    Then the inactive payment configuration is not visible in store MOBEE

  Scenario: Store cannot be opened if no payment configuration
    Given I go to Stores
    And I create store with following values
      | timezone              | GMT -8:00 Pacific Standard Time |
      | store country         | United States                   |
      | store sub country     | California                      |
      | payment configuration |                                 |
      | store name            | TestStore-                      |
      | warehouse             | Generic Warehouse A             |
      | catalog               | Master Catalog A                |
      | language              | English (United States)         |
      | currency              | USD                             |
    And I edit the newly created store
    When I change the store state without confirmation to Open
    Then the following store warning message is displayed: At least one payment configuration must be selected for this store

  Scenario: Payment instrument is not available in profile if payment configuration isn't present in scope
    Given I go to Payment Configurations
    And I create a new Payment Configuration with following details
      | PROVIDER           | Happy Path           |
      | METHOD             | CARD METHOD          |
      | CONFIGURATION_NAME | ATest payment config |
      | DISPLAY_NAME       | ATest payment config |
    And I save the payment configuration
    And I activate the newly created payment configuration
    And I select and save the newly created payment configuration for store MOBEE
    And I authenticate as a registered shopper harry.potter@elasticpath.com on scope mobee
    When I have created payment instrument with the newly created payment configuration on my profile:
      | display-name | Test PI          |
      | PIC Field A  | Test PIC Value A |
      | PIC Field B  | Test PIC Value B |
    And I authenticate as a registered shopper harry.potter@elasticpath.com on scope kobee
    Then the payment instrument Test PI is not available in profile

  Scenario: Payment instrument is not available in profile if payment configuration is deactivated for store
    Given I go to Payment Configurations
    And I create a new Payment Configuration with following details
      | PROVIDER           | Happy Path           |
      | METHOD             | CARD METHOD          |
      | CONFIGURATION_NAME | ATest payment config |
      | DISPLAY_NAME       | ATest payment config |
    And I save the payment configuration
    And I activate the newly created payment configuration
    And I select and save the newly created payment configuration for store MOBEE
    And I have authenticated as a newly registered shopper
    When I have created payment instrument with the newly created payment configuration on my profile:
      | display-name | Unavailable test PI |
      | PIC Field A  | Test PIC Value A    |
      | PIC Field B  | Test PIC Value B    |
    And I deselect and save the newly created payment configuration for store MOBEE
    Then the payment instrument Unavailable test PI is not available in profile
    And the default payment instrument is not Unavailable test PI
