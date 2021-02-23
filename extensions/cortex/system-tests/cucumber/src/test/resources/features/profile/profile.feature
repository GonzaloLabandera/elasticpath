@profile
Feature: Profile

  Background:
    When I have authenticated as a newly registered shopper

  Scenario: Unable to access another shoppers profile
    When I view my profile
    And save the address uri
    When I have authenticated as a newly registered shopper
    And attempt to access the first shoppers profile
    Then the HTTP status is forbidden


  Scenario Outline: Able to see system profile attributes
    When I view my profile
    Then the HTTP status is OK
    And I should see profile field <ATTRIBUTE>

    Examples:
      | ATTRIBUTE             |
      | preferred-locale      |
      | preferred-currency    |
      | phone                 |
      | fax                   |
      | company               |
      | date-of-birth         |
      | html-email            |
      | notification          |
      | business-number       |
      | tax-exemption-id      |

  Scenario Outline: Unable to see hidden profile attributes
    When I view my profile
    Then the HTTP status is OK
    And I should not see profile field <ATTRIBUTE>

    Examples:
      | ATTRIBUTE      |
      | anonymous      |
      | email          |
