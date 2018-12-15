# language: en
@importProduct
Feature: Import product
  As Operations, I want to import product details from the file system

  Scenario: Import product
    Given I create a product testProduct
    And I create a change set test
    When I import product testProduct with visibility set to false into change set test
    Then the product testProduct visibility is set to false
    And the change set test has the following group member data
      | objectType | objectIdentifier |
      | Product    | testProduct      |

  Scenario: Import product bundle
    Given I create a product testProduct
    And I create a product bundle testProductBundle with constituent testProduct
    And I create a change set test
    When I import product bundle testProductBundle with visibility set to false into change set test
    Then the product testProductBundle visibility is set to false
    And the change set test has the following group member data
      | objectType     | objectIdentifier  |
      | Product Bundle | testProductBundle |