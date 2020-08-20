@regressionTest @customerService @customer
Feature: Account Search

  Background:
    Given I sign in to CM as CSR user
    And I go to Customer Service

  @smokeTest
  Scenario: Search for accounts
    When I open account search tab
    Then I can click search button and non-empty search results table appears on the page

  @smokeTest
  Scenario Outline: Search for account by shared ID
    When I search for account by shared ID <sharedId>
    Then I should see account with shared ID <sharedId> in result list
    And I close account search results tab
    When I search for account by shared ID <partialSharedId>
    Then I should see empty account search results table

    Examples:
      | sharedId             | partialSharedId |
      | SomeBusiness@abc.com | SomeBus         |

  Scenario Outline: Search for account by business Name
    When I search for account by business name <businessName>
    Then I should see account with business name <businessName> in result list
    And I close account search results tab
    When I search for account by business name <partialBusinessName>
    Then I should see account with business name <businessName> in result list

    Examples:
      | businessName  | partialBusinessName |
      | Some Business | Some Bu             |

  Scenario Outline: Search for account by business number
    When I search for account by business number <businessNumber>
    Then I should see account with business number <businessNumber> in result list
    And I close account search results tab
    When I search for account by business number <partialBusinessNumber>
    Then I should see account with business number <businessNumber> in result list

    Examples:
      | businessNumber | partialBusinessNumber |
      | 9879879879     | 9879879               |

  Scenario Outline: Search for account by zip code
    When I search for account by zip code <zipCode>
    Then I should see account with zip code <zipCode> in result list
    And I close account search results tab
    When I search for account by zip code <partialZipCode>
    Then I should see account with zip code <zipCode> in result list

    Examples:
      | zipCode   | partialZipCode |
      | 45435-435 | 45435-         |
