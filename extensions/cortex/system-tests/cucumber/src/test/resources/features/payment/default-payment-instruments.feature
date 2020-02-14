@paymentMethods
Feature: Default Payment instrument
  As a shopper
  I want to save my payment instruments as default
  So they would be used automatically on checkout

  Scenario: First payment instrument saved to profile becomes default for order
    Given I have authenticated as a newly registered shopper
    And I get the list of payment methods from my profile
    And I open Smart Path Config payment method
    And I create payment instrument supplying following fields:
      | display-name | test name |
    When I retrieve my order
    And I follow the link paymentinstrumentselector
    Then I should see a payment instrument default with the following fields:
      | name               | test name |
      | default-on-profile | true      |

  Scenario: Shopper can set default payment instrument from an order
    Given I have authenticated as a newly registered shopper
    And I create a default Smart Path Config payment instrument from order supplying following fields:
      | display-name | test name |
    When I access the payment instruments on my profile
    Then I should see a payment instrument default with the following fields:
      | name               | test name |
      | default-on-profile | true      |

  Scenario: Unsaved payment instrument in order displays as not default
    Given I have authenticated as a newly registered shopper
    And I create an unsaved Smart Path Config payment instrument from order supplying following fields:
      | display-name | test name |
    When I retrieve my order
    And I follow the link paymentinstrumentselector
    Then I should see a payment instrument option with the following fields:
      | name               | test name |
      | saved-on-profile   | false     |
      | default-on-profile | false     |

  Scenario: Non saveable payment instrument does not have default-on-profile option
    Given I have authenticated as a newly registered shopper
    And I get the list of payment methods from my order
    When I open Angry Path Config payment method
    Then The payment instrument form does not provide a default-on-profile field

  Scenario: Default payment instrument is replaced by the next one created
    Given I have authenticated as a newly registered shopper
    And I create a default Smart Path Config payment instrument from order supplying following fields:
      | display-name | default one |
    When I create a default Smart Path Config payment instrument from order supplying following fields:
      | display-name | default two |
    Then I should see 2 payment instruments on my order
    And I should see a payment instrument default with the following fields:
      | name               | default two |
      | default-on-profile | true        |

  Scenario: Default payment instrument stays default even not been used in order
    Given I have authenticated as a newly registered shopper
    And I create an unsaved Smart Path Config payment instrument from order supplying following fields:
      | display-name | unsaved one |
    When I create a default Smart Path Config payment instrument from profile supplying following fields:
      | display-name | default two |
    Then I should see 2 payment instruments on my order
    And I should see a payment instrument chosen with the following fields:
      | name               | unsaved one |
      | default-on-profile | false       |
    And I should see a payment instrument choice with the following fields:
      | name               | default two |
      | default-on-profile | true        |
    And I should see a payment instrument default with the following fields:
      | name               | default two |
      | default-on-profile | true        |

  Scenario: Default payment instrument stays default even not been selected in order
    Given I have authenticated as a newly registered shopper
    And I create a default Smart Path Config payment instrument from profile supplying following fields:
      | display-name | default one |
    And I have created Smart Path Config payment instrument on my profile with the following fields:
      | display-name | saved two |
    When I select saved two payment instrument
    Then I should see 2 payment instruments on my order
    And I should see a payment instrument chosen with the following fields:
      | name               | saved two |
      | default-on-profile | false     |
    And I should see a payment instrument choice with the following fields:
      | name               | default one |
      | default-on-profile | true        |
    And I should see a payment instrument default with the following fields:
      | name               | default one |
      | default-on-profile | true        |

  Scenario: No default-on-profile field when creating payment instrument from order as public shopper
    Given I login as a public shopper
    And I get the list of payment methods from my order
    When I open Happy Path Config payment method
    Then The payment instrument form does not provide a default-on-profile field

  Scenario: Default payment instrument becomes chosen when the other chosen one is deleted
    Given I have authenticated as a newly registered shopper
    And I create a saved Smart Path Config payment instrument from order supplying the following fields:
      | display-name | saved one |
    And I create a default Smart Path Config payment instrument from profile supplying following fields:
      | display-name | default two |
    When I delete saved one payment instrument from order
    Then I should see 1 payment instruments on my order
    And I should see a payment instrument chosen with the following fields:
      | name               | default two |
      | default-on-profile | true        |
    And I should see a payment instrument default with the following fields:
      | name               | default two |
      | default-on-profile | true        |

  Scenario: Default payment instrument deletion does not affect current choice of instruments
    Given I have authenticated as a newly registered shopper
    And I create an unsaved Smart Path Config payment instrument from order supplying following fields:
      | display-name | unsaved one |
    And I create a default Smart Path Config payment instrument from profile supplying following fields:
      | display-name | default two |
    When I delete default two payment instrument from order
    Then I should see 1 payment instrument on my order
    And I should see a payment instrument chosen with the following fields:
      | name               | unsaved one |
      | default-on-profile | false       |
    And I view the payment instruments available to be selected for my order
    And there are no default links
    And the default payment instrument is removed from current profile
