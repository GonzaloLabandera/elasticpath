@regressionTest @configuration @tags
Feature: Tags System

  Background:
    Given I sign in to CM as admin user
    When I go to Configuration
    And I go to Tags
    And I add a tag group TEST_CONDITIONS

  @cleanupTags
  Scenario: Add New Tag Group
    Then verify the tag groups list contains newly added tag group

  @cleanupTags
  Scenario: Add New Tag Definition
    When I select the newly created tag group
    And I add tag definitions with the following data
      | code               | name               | description            | displayName       | language | fieldType | dictionaries                   |
      | CUST_PRIORITY      | CUST_PRIORITY      | Customer Priority      | Cust Priority     | en_US    | text      | PLA_SHOPPER                    |
      | CUST_REWARDS_LEVEL | CUST_REWARDS_LEVEL | Customer Rewards Level | Cust Reward Level | en_US    | text      | PLA_SHOPPER,PROMOTIONS_SHOPPER |
    Then verify the tag definition list contains the tag definitions

  @cleanupTags
  Scenario: Edit Tag Group
    When I open the newly created tag group
    And I edit the tag group name to TEST_CONDITION
    Then verify the edited tag group list contains the new name

  @cleanupTags
  Scenario: Edit Tag Definition
    When I select the newly created tag group
    And I add tag definitions with the following data
      | code               | name               | description            | displayName       | language | fieldType | dictionaries                   |
      | CUST_PRIORITY      | CUST_PRIORITY      | Customer Priority      | Cust Priority     | en_US    | text      | PLA_SHOPPER                    |
      | CUST_REWARDS_LEVEL | CUST_REWARDS_LEVEL | Customer Rewards Level | Cust Reward Level | en_US    | text      | PLA_SHOPPER,PROMOTIONS_SHOPPER |
    When I edit the tag definition CUST_PRIORITY with the following data
      | code          | name          | description       | displayName        | language | fieldType | dictionaries |
      | CUST_PRIORITY | CUST_PRIORITY | Customer Priority | priorite du client | fr       | text      | TIME         |
    Then verify the tag definition list contains the tag definitions

  Scenario: Remove Tag Group
    When I remove the recently created tag group
    Then the tag group should not exist in the list

  @cleanupTags
  Scenario: Remove Tag Definition
    When I select the newly created tag group
    And I add tag definitions with the following data
      | code          | name          | description       | displayName   | language | fieldType | dictionaries |
      | CUST_PRIORITY | CUST_PRIORITY | Customer Priority | Cust Priority | en_US    | text      | PLA_SHOPPER  |
    When I remove the tag definition CUST_PRIORITY
    Then the tag definition CUST_PRIORITY should not exist in the list