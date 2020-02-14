@epCoreTool
Feature: EP core tool

  Scenario: Rebuild all search indexes
    Given I sign in to CM as admin user
    And I go to Configuration
    When I go to Search Indexes
    Then the status of all indexes should be Complete
    When I run the ep core tool to rebuild the indexes
    Then the status of all indexes should be Rebuild

  @resetPassword
  Scenario Outline: Update cm user password
    Given I sign in to CM as <cm_user> with password 111111
    When I sign out
    And I run the ep core tool to change the <cm_user> user password to <new_password>
    Then I should be able to sign in again to CM as <cm_user> with password <new_password>

    Examples:
      | cm_user  | new_password |
      | cs_brand | Password1    |

  @resetSettingValue
  Scenario Outline: Update System Configuration settings value
    Given I sign in to CM as admin user
    And I go to Configuration
    And I go to System Configuration
    When I search and select the system configuration <config_name>
    Then there should be 0 Defined value record
    When I run the ep core tool to set setting of <config_name> to true
    And I select the system configuration <config_name>
    Then there should be 1 Defined value record

    Examples:
      | config_name                                    |
      | COMMERCE/SYSTEM/EMAIL/emailTextTemplateEnabled |

  @resetProdName
  Scenario: Rebuild specific search index
    Given I sign in to CM as admin user
    And I go to Catalog Management
    And I search for product by name Gravity and verify it appears in a result list
    And I update the product name from Gravity to 1234
    When I search product by name 1234
    Then product 1234 is not in the result list
    When I run the ep core tool to rebuild the index of product
    And I search product by name 1234
    Then product 1234 is in the result list