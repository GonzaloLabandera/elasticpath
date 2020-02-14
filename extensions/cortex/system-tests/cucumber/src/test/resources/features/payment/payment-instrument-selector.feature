@paymentMethods
Feature: Payment instrument selector
  As a shopper
  I want to select payment instruments from my profile
  So that I could use them for checkout

  Scenario Outline: Profile Payment Instrument can be selected on order
    Given I login as a newly registered shopper
    And I have created Happy Path Config payment instrument on my profile with the following fields:
      | display-name | Default PI       |
      | PIC Field A  | Test PIC Value A |
      | PIC Field B  | Test PIC Value B |
    And I have created Smart Path Config payment instrument on my profile with the following fields:
      | display-name | <Name> |
    And I add item with code tt0034583_sku to my cart
    And I create a default shipping address on the profile
    When I select <Name> payment instrument
    Then payment instrument with name <Name> is selected for order

    Examples:
      | Name                    |
      | Payment Instrument Name |

  Scenario: Payment instrument unsaved selection is deleted when toggled in the selector
    Given I have authenticated as a newly registered shopper
    And I create an unsaved Smart Path Config payment instrument from order supplying following fields:
      | display-name | unsaved instrument |
    When I deselect unsaved instrument payment instrument
    Then I should see 0 payment instruments on my order
    And my order does not have a payment instrument applied

  Scenario: Payment instrument saved selection is preserved as choice when toggled in the selector
    Given I have authenticated as a newly registered shopper
    And I have created Smart Path Config payment instrument on my profile with the following fields:
      | display-name | default instrument |
    And I create a saved Smart Path Config payment instrument from order supplying the following fields:
      | display-name | saved instrument |
    When I deselect saved instrument payment instrument
    Then I should see 2 payment instruments on my order
    And I should see a payment instrument choice with the following fields:
      | name | saved instrument |

  Scenario: Default payment instrument selection cannot be toggled in the selector
    Given I have authenticated as a newly registered shopper
    And I create a default Smart Path Config payment instrument from order supplying following fields:
      | display-name | default instrument |
    When I deselect default instrument payment instrument
    Then I should see 1 payment instruments on my order
    And I should see a payment instrument chosen with the following fields:
      | name | default instrument |
