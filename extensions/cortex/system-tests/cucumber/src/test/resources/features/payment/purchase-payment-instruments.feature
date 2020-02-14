@paymentMethods
Feature: Shopper can retrieve the payment instrument used on a purchase
  As a client developer
  I want to retrieve payment methods used on a purchase
  so that I can display the information to the shopper

  Scenario: Retrieve payment instruments on purchase
    Given a registered shopper purchase was made with payment instrument
    When I view the purchase
    Then the purchase payment instrument name matches the instrument used to create the purchase
    And the payment instrument is a paymentinstruments.purchase-payment-instrument type

  Scenario: Shopper's chosen payment instrument is used to complete the purchase
    Given I authenticate as a shopper with saved payment instruments X Y and Z and payment instrument X is chosen on their order
    When I complete the purchase for the order
    Then the chosen payment instrument is displayed correctly as the payment instrument for the purchase

  Scenario: Payment instruments is empty when purchase was free
    Given a free product was purchased without payment
    When I view the purchase
    Then the paymentinstruments link is empty

  Scenario: Shopper can go back from purchase-payment-instruments to purchase
    Given a registered shopper purchase was made with payment instrument
    When I view the purchase
    And I follow the link paymentinstruments
    And I follow the link purchase
    And the HTTP status code is 200

  Scenario: Unable to access another shoppers purchase-payment-instruments
    Given a registered shopper purchase was made with payment instrument
    When I view the purchase
    And I follow the link paymentinstruments
    And save the address uri
    When I have authenticated as a newly registered shopper
    And attempt to access the first shoppers paymentinstruments
    Then the HTTP status is forbidden

  Scenario: Anonymous shopper can access purchase instrument of the purchase
    Given an anonymous shopper purchase was made with payment instrument
    When I view the purchase
    Then the purchase payment instrument name matches the instrument used to create the purchase