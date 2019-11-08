@exportTagGroup
Feature: Export TagGroup

  Scenario: Export TagGroup
    Given the following tag groups are saved in the database
      | guid   | display_name   | language |
      | GROUP1 | display name 1 | en       |
      | GROUP2 | display name 2 | fr       |
    And the following tag definitions are saved in the database
      | groupCode | code               | name               | description            | displayName       | language | fieldType | dictionaries                    |
      | GROUP1    | CUST_PRIORITY      | CUST_PRIORITY      | Customer Priority      | Cust Priority     | en_US    | text      | PLA_SHOPPER                     |
      | GROUP2    | CUST_REWARDS_LEVEL | CUST_REWARDS_LEVEL | Customer Rewards Level | Cust Reward Level | en_US    | text      | PLA_SHOPPER,PROMOTIONS_SHOPPER |
    When the tags in the database are exported using importexport
    And the exported tags are retrieved
    Then the exported tag records contain all tag groups and definitions
