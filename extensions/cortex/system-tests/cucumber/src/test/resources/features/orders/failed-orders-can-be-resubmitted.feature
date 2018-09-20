#This test is no long relevant as cortex now only support payment token. Need revisit this test when new payment is done.
@notready
Feature: Failed orders can be resubmitted
  As a client developer,
  I want to allow my shopper to resubmit their order
  So that when a shopper's credit cart declines, they can use a different credit
  and submit the order again

  Scenario: Submitting order fails with invalid credit card
    Given I have selected a credit card that would trigger a failed order
    When I submit the order
    Then my order fails with status 409

  Scenario: Re-submitting an order with valid payment method is successful
    Given I modify my payment method to another credit card
    When I submit the order
    Then my order succeeds