@importTagGroup
Feature: Import TagGroup

  Scenario: Import TagGroup
    Given the tag group import data has been emptied out
    When the following tags are imported using importexport
      | guid   | display_name   | code               | name               | description            | displayName       | language | fieldType | dictionaries                   |
      | GROUP1 | display name 1 | CUST_PRIORITY      | CUST_PRIORITY      | Customer Priority      | Cust Priority     | en_US    | text      | PLA_SHOPPER                    |
      | GROUP2 | display name 2 | CUST_REWARDS_LEVEL | CUST_REWARDS_LEVEL | Customer Rewards Level | Cust Reward Level | en_US    | text      | PLA_SHOPPER,PROMOTIONS_SHOPPER |
    Then all tags are persisted

