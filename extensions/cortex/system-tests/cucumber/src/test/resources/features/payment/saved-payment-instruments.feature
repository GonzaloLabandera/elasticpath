@paymentMethods
Feature: Saved Payment instrument
  As a shopper
  I want to save my payment instruments
  So that I could use them for checkout

  Scenario: Payment instruments created in profile are automatically saved
    Given I have authenticated as a newly registered shopper
    And I get the list of payment methods from my profile
    And I open Happy Path Config payment method
    And I create payment instrument supplying following fields:
      | PIC Field A  | abc       |
      | PIC Field B  | xyz       |
      | display-name | test name |
    When I retrieve my order
    And I follow the link paymentinstrumentselector
    Then I should see a payment instrument option with the following fields:
      | name               | test name |

  Scenario: Save payment instrument to profile from order
    Given I have authenticated as a newly registered shopper
    And I create a saved Happy Path Config payment instrument from order supplying the following fields:
      | PIC Field A  | abc       |
      | PIC Field B  | xyz       |
      | display-name | test name |
    When I access the payment instruments on my profile
    Then I should see a payment instrument with the following fields:
      | name               | test name |

  Scenario: Saved payment instrument in order displays as saved
    Given I have authenticated as a newly registered shopper
    And I create a saved Happy Path Config payment instrument from order supplying the following fields:
      | PIC Field A  | abc       |
      | PIC Field B  | xyz       |
      | display-name | test name |
    When I retrieve my order
    And I follow the link paymentinstrumentselector
    Then I should see a payment instrument option with the following fields:
      | name               | test name |
      | saved-on-profile   | true      |

  Scenario: Unsaved payment instrument in order displays as not saved
    Given I have authenticated as a newly registered shopper
    And I create an unsaved Happy Path Config payment instrument from order supplying following fields:
      | PIC Field A  | abc       |
      | PIC Field B  | xyz       |
      | display-name | test name |
    When I retrieve my order
    And I follow the link paymentinstrumentselector
    Then I should see a payment instrument option with the following fields:
      | name               | test name |
      | saved-on-profile   | false     |

  Scenario: Non saveable payment instrument does not have save-on-profile option
    Given I have authenticated as a newly registered shopper
    And I get the list of payment methods from my order
    When I open Angry Path Config payment method
    Then The payment instrument form does not provide a save-on-profile field

  Scenario: Unsaved payment instrument disappears after next one created
    Given I have authenticated as a newly registered shopper
    And I create an unsaved Happy Path Config payment instrument from order supplying following fields:
      | PIC Field A  | abc     |
      | PIC Field B  | xyz     |
      | display-name | unsaved |
    And I create a saved Happy Path Config payment instrument from order supplying the following fields:
      | PIC Field A  | abc   |
      | PIC Field B  | xyz   |
      | display-name | saved |
    Then I should see 1 payment instrument on my order
    And I should see a payment instrument chosen with the following fields:
      | name               | saved |
      | saved-on-profile   | true  |

  Scenario: Saved payment instrument persists after next one created
    Given I have authenticated as a newly registered shopper
    And I create a saved Happy Path Config payment instrument from order supplying the following fields:
      | PIC Field A  | abc       |
      | PIC Field B  | xyz       |
      | display-name | saved one |
    When I create a saved Happy Path Config payment instrument from order supplying the following fields:
      | PIC Field A  | abc       |
      | PIC Field B  | xyz       |
      | display-name | saved two |
    Then I should see 2 payment instruments on my order
    And I should see a payment instrument chosen with the following fields:
      | name               | saved two |
      | saved-on-profile   | true      |

  Scenario: Select saved payment instruments
    Given I have authenticated as a newly registered shopper
    And I create a saved Happy Path Config payment instrument from order supplying the following fields:
      | PIC Field A  | abc       |
      | PIC Field B  | xyz       |
      | display-name | saved one |
    When I create a saved Happy Path Config payment instrument from order supplying the following fields:
      | PIC Field A  | abc       |
      | PIC Field B  | xyz       |
      | display-name | saved two |
    Then I should see 2 payment instruments on my order
    And I select saved one payment instrument
    And I should see a payment instrument chosen with the following fields:
      | name               | saved one |
      | saved-on-profile   | true      |
    And I should see a payment instrument choice with the following fields:
      | name               | saved two |
    And I select saved two payment instrument
    And I should see a payment instrument chosen with the following fields:
      | name               | saved two |
      | saved-on-profile   | true      |
    And I should see a payment instrument choice with the following fields:
      | name               | saved one |

  Scenario: No savable field when creating payment instrument from profile
    Given I have authenticated as a newly registered shopper
    And I get the list of payment methods from my profile
    When I open Happy Path Config payment method
    Then The payment instrument form does not provide a save-on-profile field

  Scenario: No savable field when creating payment instrument from order as public shopper
    Given I login as a public shopper
    And I get the list of payment methods from my order
    When I open Happy Path Config payment method
    Then The payment instrument form does not provide a save-on-profile field

