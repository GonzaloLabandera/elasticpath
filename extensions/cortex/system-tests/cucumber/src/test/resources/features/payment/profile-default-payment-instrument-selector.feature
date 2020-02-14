@paymentMethods
Feature: Profile Payment Instrument Selection

  Background:
    Given I authenticate as a shopper with payment instruments X Y and Z and X as the default

  Scenario: Default Profile Payment Instrument Selection
    When I access the payment instruments on my profile
    When I select payment instrument Y on profile
    Then payment instruments X and Z are now choices as the payment instruments on the profile
    And payment instrument Y is chosen as the payment instrument for my purchase

  Scenario: Shopper can see the details for the chosen payment instrument on their profile
    When I get the payment instrument details for the chosen payment instrument X on profile
    Then the payment instrument details display the correct values

  Scenario: Shopper can see the payment instrument details for payment instrument choices on their profile
    When I get the payment instrument details for payment instrument choice Y or Z on profile
    Then the payment instrument details display the correct values for that choice

  Scenario: Shopper can go back from selector to payment instruments
    When I access default payment instruments selector on my profile
    Then I can go back to payment instruments on my profile

  Scenario: Shopper can go back from choice to choices
    When I access payment instruments selector choice on my profile
    Then I can go back to choices from choice in profile default payment instrument selector