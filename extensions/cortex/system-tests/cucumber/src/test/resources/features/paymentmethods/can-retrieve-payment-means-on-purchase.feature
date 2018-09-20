@Paymentmethods
Feature: Shopper can retrieve the payment method used on a purchase
  As a client developer
  I want to retrieve payment means used on a purchase
  so that I could display the information to the shopper

  Scenario: Retrieve credit card payment means on purchase
    Given a purchase was made with credit card
    When I view the purchase
    Then the paymentmeans is a credit card type
    And the credit card information matches the credit card used to create the purchase

  Scenario: Retrieve token payment means on purchase
    Given a purchase was made with payment token
    When I view the purchase
    Then the paymentmeans is a paymenttoken type
    And the token display-name matches the token used to create the purchase

  Scenario: Payment means is empty when purchase was free
    Given a free product was purchased without payment
    When I view the purchase
    Then the paymentmeans is empty