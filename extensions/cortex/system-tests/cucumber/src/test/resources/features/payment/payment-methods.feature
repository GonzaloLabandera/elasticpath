@paymentMethods
Feature: Payment methods on order
  As a shopper
  I want to manage my payment methods
  so that checkout can be completed faster

  Scenario: A shopper is able to access the PaymentMethods resource from Profile
    Given I login as a newly registered shopper
    When I view my profile
    Then there is a paymentmethods link

  Scenario: A shopper is able to access Profile from the PaymentMethods resource
    Given I login as a newly registered shopper
    And I view my profile
    When I follow the link paymentmethods
    Then there is a profile link

  Scenario: A shopper is able to access the PaymentMethods resource from Order
    Given I login as a newly registered shopper
    When I go to my cart
    And I follow the link order
    Then there is a paymentmethodinfo link

  Scenario: A shopper is able to access Order from the PaymentMethods resource
    Given I login as a newly registered shopper
    And I go to my cart
    And I follow the link order
    When I follow the link paymentmethodinfo
    Then there is an order link

  Scenario: An anonymous user is able to access Order from the PaymentMethods resource
    Given I login as a new public shopper
    And I go to my cart
    And I follow the link order
    When I follow the link paymentmethodinfo
    And I open Smart Path Config payment method
    Then I follow the link paymentmethods
    And there is an order link

  Scenario: Non Saveable Payment Method not visible in customer profile - new shopper
    Given I login as a newly registered shopper
    Then I should not see Angry Path Config payment method in my profile

  Scenario: Saveable Payment Methods visible in customer profile - new shopper
    Given I login as a newly registered shopper
    Then I should see the following payment methods in my profile
      | Happy Path Config                       |
      | Address Required Happy Path Config      |
      | No Capabilities Config                  |
      | Smart Path Config                       |
      | Cancel Unsupported Config               |
      | Modify Unsupported Config               |
      | Modify And Cancel Unsupported Config    |
      | Reserve Unsupported Config              |
      | Cancel Fails Config                     |
      | Email Required Config                   |
      | Big Amount Fields Happy Path Config     |
      | Credit Unsupported Config               |
      | Reverse Unsupported Config              |
      | Single Reserve Per PI Happy Path Config |
      | Reserve Fails                           |

  Scenario: All Payment Methods visible in customer order - new shopper
    Given I login as a newly registered shopper
    Then I should see the following payment methods in my order
      | Happy Path Config                       |
      | Address Required Happy Path Config      |
      | Angry Path Config                       |
      | No Capabilities Config                  |
      | Smart Path Config                       |
      | Cancel Unsupported Config               |
      | Modify Unsupported Config               |
      | Modify And Cancel Unsupported Config    |
      | Reserve Unsupported Config              |
      | Cancel Fails Config                     |
      | Email Required Config                   |
      | Big Amount Fields Happy Path Config     |
      | Credit Unsupported Config               |
      | Reverse Unsupported Config              |
      | Single Reserve Per PI Happy Path Config |
      | Reserve Fails                           |

  Scenario: Non Saveable Payment Method not visible in customer profile - existing shopper
    Given I authenticate as a registered shopper harry.potter@elasticpath.com on scope mobee
    Then I should not see Angry Path Config payment method in my profile

  Scenario: Saveable Payment Methods visible in customer profile - existing shopper
    Given I authenticate as a registered shopper harry.potter@elasticpath.com on scope mobee
    Then I should see the following payment methods in my profile
      | Happy Path Config                       |
      | Address Required Happy Path Config      |
      | No Capabilities Config                  |
      | Smart Path Config                       |
      | Cancel Unsupported Config               |
      | Modify Unsupported Config               |
      | Modify And Cancel Unsupported Config    |
      | Reserve Unsupported Config              |
      | Cancel Fails Config                     |
      | Email Required Config                   |
      | Big Amount Fields Happy Path Config     |
      | Credit Unsupported Config               |
      | Reverse Unsupported Config              |
      | Single Reserve Per PI Happy Path Config |
      | Reserve Fails                           |

  Scenario: All Payment Methods visible in customer order - existing shopper
    Given I authenticate as a registered shopper harry.potter@elasticpath.com on scope mobee
    Then I should see the following payment methods in my order
      | Happy Path Config                       |
      | Address Required Happy Path Config      |
      | Angry Path Config                       |
      | No Capabilities Config                  |
      | Smart Path Config                       |
      | Cancel Unsupported Config               |
      | Modify Unsupported Config               |
      | Modify And Cancel Unsupported Config    |
      | Reserve Unsupported Config              |
      | Cancel Fails Config                     |
      | Email Required Config                   |
      | Big Amount Fields Happy Path Config     |
      | Credit Unsupported Config               |
      | Reverse Unsupported Config              |
      | Single Reserve Per PI Happy Path Config |
      | Reserve Fails                           |

  Scenario: Shopper can access Payment Method from Order Payment Instrument
    Given I have authenticated as a newly registered shopper
    And I create a saved Happy Path Config payment instrument from order supplying the following fields:
      | display-name | test name |
      | PIC Field A  | abc       |
      | PIC Field B  | xyz       |
    When I retrieve my order
    And I follow the link paymentinstrumentselector
    And I access test name payment instrument from order
    And I follow the link paymentmethod
    Then I should arrive at the Happy Path Config payment method

  Scenario: Shopper can access Payment Method from Profile Payment Instrument
    Given I have authenticated as a newly registered shopper
    And I get the list of payment methods from my profile
    And I open Smart Path Config payment method
    And I create payment instrument supplying following fields:
      | display-name | test |
    When I access the payment instruments on my profile
    And I access test payment instrument from profile
    And I follow the link paymentmethod
    Then I should arrive at the Smart Path Config payment method

  Scenario: Registered shopper can access Payment Method from Purchase Payment Instrument
    Given a registered shopper purchase was made with payment instrument
    And I access payment instrument with default name from purchase
    When I follow the link paymentmethod
    Then the field display-name has value Smart Path Config

  Scenario: Anonymous shopper can access Payment Method from Purchase Payment Instrument
    Given an anonymous shopper purchase was made with payment instrument
    And I access payment instrument with default name from purchase
    When I follow the link paymentmethod
    Then the field display-name has value Smart Path Config

  Scenario Outline: Shopper can see localized display name values
    Given I have authenticated as a newly registered shopper
    And I get the list of payment methods from my profile
    And I open <PROVIDER> payment method in language <LANGUAGE>
    Then the field display-name has value <DISPLAY_NAME>

    Examples:
      | LANGUAGE | PROVIDER          | DISPLAY_NAME                    |
      | fr       | Happy Path Config | happy path display name - fr    |
      | fr-ca    | Happy Path Config | happy path display name - fr_CA |
      | pt       | Happy Path Config | default happy path display name |
      | en       | Happy Path Config | default happy path display name |

  Scenario: Anonymous user cannot access profile payment methods
    Given I login as a new public shopper
    When I view my profile
    Then there is no paymentmethods link found

  Scenario: Anonymous user can access order payment methods
    Given I login as a new public shopper
    When I retrieve my order
    Then there is a paymentmethodinfo link

  Scenario: Anonymous user forbidden from manually accessing profile payment methods
    Given I login as a new public shopper
    When I view my profile
    And I GET paymentmethods/mobee
    Then the HTTP status is forbidden
