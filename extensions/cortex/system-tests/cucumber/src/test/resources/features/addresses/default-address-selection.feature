@addresses

Feature: Default Address Selection

  Background:
    Given a registered shopper has payment instruments saved to his profile
    And I add an address with country CA and region BC
    And I add an address with country CA and region QC

  Scenario: Default Billing Address Selector
    When I view the default billing address selector
    Then the address with country CA and region BC is selected

  Scenario: Default Billing Address Selection
    When I view the default billing address selector
    And I select the address with country CA and region QC
    Then the address with country CA and region QC is selected

  Scenario: Default Shipping Address Selector
    When I view the default shipping address selector
    Then the address with country CA and region BC is selected

  Scenario: Default Shipping Address Selection
    When I view the default shipping address selector
    And I select the address with country CA and region QC
    Then the address with country CA and region QC is selected
