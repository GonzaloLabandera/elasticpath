@paymentMethods
Feature: Shopper can retrieve the payment method used on a purchase
  As a client developer
  I want to retrieve payment means used on a purchase
  so that I could display the information to the shopper

  Scenario: Retrieve token payment means on purchase
    Given a purchase was made with payment token
    When I view the purchase
    Then the token display-name matches the token used to create the purchase
    And the paymentmeans is a paymenttoken type

  Scenario: Payment means is empty when purchase was free
    Given a free product was purchased without payment
    When I view the purchase
    Then the paymentmeans is empty