@regressionTest @customerService @customer
Feature: Customer Search

  Background:
    Given I sign in to CM as CSR user
    And I go to Customer Service

  @smokeTest
  Scenario Outline: Search for customer by email
    When I search for customer by email ID <emailId>
    Then I should see customer with email ID <emailId> in result list
    And I close customer search results tab
    When I search for customer by email ID <partialEmail>
    Then I should see customer with email ID <emailId> in result list

    Examples:
      | emailId                      | partialEmail         |
      | harry.potter@elasticpath.com | harry.potter@elastic |

  Scenario Outline: Search for customer by First name
    When I search for customer by first name <firstName>
    Then I should see customer with first name <firstName> in result list
    And I close customer search results tab
    When I search for customer by first name <partialFirstName>
    Then I should see customer with first name <firstName> in result list

    Examples:
      | firstName | partialFirstName |
      | Harry     | Har              |

  Scenario Outline: Search for customer by Last name
    When I search for customer by last name <lastName>
    Then I should see customer with last name <lastName> in result list
    And I close customer search results tab
    When I search for customer by last name <partialLastName>
    Then I should see customer with last name <lastName> in result list

    Examples:
      | lastName | partialLastName |
      | Potter   | Pott            |

  Scenario Outline: Search for customer by Zip Code
    When I search for customer by zip code <zipCode>
    Then I should see customer with zip code <zipCode> in result list
    And I close customer search results tab
    When I search for customer by zip code <partialZipCode>
    Then I should see customer with zip code <zipCode> in result list

    Examples:
      | zipCode | partialZipCode |
      | V6A 1N4 | V6A            |

  Scenario Outline: Search for customer by phone number
    When I search for customer by phone number <phoneNumber>
    Then I should see customer with phone number <phoneNumber> in result list
    And I close customer search results tab
    When I search for customer by phone number <partialPhoneNumber>
    Then I should see customer with phone number <phoneNumber> in result list

    Examples:
      | phoneNumber | partialPhoneNumber |
      | 6042154651  | 6042154            |

  Scenario Outline: Search for customer by email with Store filter
    When I search for customer by email <emailId> with store filter <customerStore>
    Then I should see customer with email ID <emailId> in result list
    And I close customer search results tab
    When I search for customer by email <emailId> with store filter <nonCustomerStore>
    Then I should see empty search results table

    Examples:
      | emailId                      | customerStore | nonCustomerStore |
      | harry.potter@elasticpath.com | Mobee         | Kobee            |

  Scenario Outline: Search for customer when there is more than one search result
    When I search for customer by email <email> with store filter <customerStore>
    Then I should see more than one row in result list
    And All entries in result list have <email> as a part of Email Address

    Examples:
      | email                     | customerStore |
      | male.user@elasticpath.com | Mobee         |