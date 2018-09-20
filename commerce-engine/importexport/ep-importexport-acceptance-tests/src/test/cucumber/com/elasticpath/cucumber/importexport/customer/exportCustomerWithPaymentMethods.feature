# language: en
@exportCustomerWithPaymentMethods
Feature: Export a customer with payment methods
  In order to export existing CE customers with payment methods
  As a QA,
  I want to export a customer's payment methods for testing purposes

Scenario: Export a customer's payment methods
  Given a customer exists with payment methods A, B and C and C is the default
  When I export the customer
  Then the exported customer will have payment methods A, B and C and C is the default

Scenario: Don't allow export of real credit card data
  Given a customer exists with credit cards A and B
  When I export the customer with CARD_NUMBER_FILTER set to STATIC
  Then the exported customer will contain credit cards A and B without the real credit card numbers

Scenario: Export a customer with no payment methods
  Given a customer exists with no payment methods
  When I export the customer
  Then the exported customer will have no payment methods

Scenario: Export a customer with credit cards and CARD_NUMBER_FILTER set to EMPTY
  Given a customer exists with credit cards A and B
  When I export the customer with CARD_NUMBER_FILTER set to EMPTY
  Then the exported customer will have no payment methods
