@paymentMethods
Feature: Payment methods on profile
  As a shopper
  I want to manage my payment methods for my profile
  so that checkout can be completed faster

  Scenario: A registered shopper is able to retrieve their default payment instrument from their profile
    Given I authenticate as a registered shopper who has a test-instrument as their default payment method
    When I retrieve the default payment instrument on my profile
    Then I get the default instrument test-instrument

  Scenario: A registered shopper is able to retrieve the list of payment instruments from their profile
    Given I authenticate as a shopper with payment instruments X Y and Z and X as the default
    When I access the payment instruments on my profile
    Then the list contains payment instruments X Y and Z and X is displayed as the default

  Scenario: A registered shopper is able to add a payment instrument to their profile
    Given I login as a newly registered shopper
    When I create a payment instrument for my profile
    Then the payment instrument is available from their profile

  Scenario: A registered shopper is able to delete a payment instrument from their profile
    Given a registered shopper has payment instruments saved to his profile
    When a payment instrument is deleted from the profile
    Then it no longer shows up in his list of saved payment instruments on his profile

  Scenario: A registered shopper can not edit payment instrument after creation
    Given I login as a newly registered shopper
    When I create a payment instrument for my profile
    And the registered shopper attempts to edit payment instrument
    Then the HTTP status is method not allowed

  Scenario Outline: Shopper account shared in multiple stores should see all payment instruments with same payment configuration
    Given I authenticate as a registered shopper harry.potter@elasticpath.com on scope mobee
    And I get the list of payment methods from my profile
    And I open Happy Path Config payment method
    And I create payment instrument supplying following fields:
      | PIC Field A  | abc                      |
      | PIC Field B  | xyz                      |
      | display-name | <PAYMENTINSTRUMENT_NAME> |
    And I should see <PAYMENTINSTRUMENT_NAME> payment instrument created
    When I authenticate as a registered shopper harry.potter@elasticpath.com on scope kobee
    Then current profile has payment instrument with name <PAYMENTINSTRUMENT_NAME>

    Examples:
      | PAYMENTINSTRUMENT_NAME  |
      | test payment instrument |

  Scenario: A public shopper cannot retrieve a payment instrument of another registered shopper
    Given I login as a newly registered shopper
    And I create a payment instrument for my profile
    And I retrieve the default payment instrument on my profile
    And save the payment instrument uri
    When I am logged in as a public shopper
    And attempt to access the other shoppers payment instrument
    Then the HTTP status is forbidden


  Scenario: A registered shopper cannot retrieve a payment instrument of another registered shopper
    Given I login as a newly registered shopper
    And I create a payment instrument for my profile
    And I retrieve the default payment instrument on my profile
    And save the payment instrument uri
    Given I login as a newly registered shopper
    And attempt to access the other shoppers payment instrument
    Then the HTTP status is forbidden
