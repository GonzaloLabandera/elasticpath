@paymentMethods
Feature: Payment methods on order
  As a shopper
  I want to manage my payment methods for my order
  so that checkout can be completed faster

  Scenario: A newly added payment instrument on a profile becomes chosen by default on order
    Given I authenticate as a registered shopper with a saved payment instrument on my profile
    When I view the payment instruments available to be selected for my order
    Then I see only the payment instrument available from my profile

  Scenario: Shopper's default payment instrument is chosen on their order payment instrument selector
    Given I authenticate as a shopper with payment instruments X Y and Z and X as the default
    When I view the payment instruments available to be selected for my order
    Then payment instrument X is chosen as the payment instrument for my purchase

  Scenario: A shopper is able to add a payment instrument to their order, which only exists for the life cycle
  of that order and hence should never appear on the shopper's profile.
    Given I login as a newly registered shopper
    When I create a payment instrument for my order
    Then the new payment instrument will be set for their order
    And the new payment instrument is not available from their profile

  Scenario: Order always has default payment instrument
    Given I login as a newly registered shopper
    And my order does not have a payment instrument applied
    When I create a payment instrument for my profile
    And I retrieve my order
    Then the default payment instrument is automatically applied to the order

  Scenario: Shopper can change the payment instrument for their order
    Given I authenticate as a shopper with saved payment instruments X Y and Z and payment instrument X is chosen on their order
    When I select payment instrument Y on the order
    Then payment instruments X and Z are now choices as the payment instruments on the order
    And payment instrument Y is now chosen as the payment instrument on the order

  Scenario: Shopper can see the details for the chosen payment instrument on their order
    Given I authenticate as a shopper with payment instrument X as the chosen payment instrument for their order
    When I get the payment instrument details for the chosen payment instrument X
    Then the payment instrument details display the correct values

  Scenario: Shopper can see the payment instrument details for payment instrument choices on their order
    Given I authenticate as a shopper with payment instruments X Y and Z and X as the default
    When I get the payment instrument details for payment instrument choice Y or Z
    Then the payment instrument details display the correct values for that choice

  Scenario: A public shopper cannot retrieve an order payment instrument of a registered shopper
    Given I login as a newly registered shopper
    And I create a payment instrument for my order
    And the new payment instrument will be set for their order
    And save the payment instrument uri
    When I am logged in as a public shopper
    And attempt to access the other shoppers payment instrument
    Then the HTTP status is forbidden

  Scenario: A registered shopper cannot retrieve an order payment instrument of a registered shopper
    Given I login as a newly registered shopper
    And I create a payment instrument for my order
    And the new payment instrument will be set for their order
    And save the payment instrument uri
    When I login as a newly registered shopper
    And attempt to access the other shoppers payment instrument
    Then the HTTP status is forbidden

  Scenario: A public shopper cannot retrieve an order payment instrument of another public shopper
    Given I login as a public shopper
    And I create a payment instrument for my order
    And the new payment instrument will be set for their order
    And save the payment instrument uri
    When I am logged in as a public shopper
    And attempt to access the other shoppers payment instrument
    Then the HTTP status is forbidden
