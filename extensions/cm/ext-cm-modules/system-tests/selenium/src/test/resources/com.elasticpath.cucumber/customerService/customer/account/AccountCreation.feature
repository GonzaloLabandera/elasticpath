@regressionTest @customerService @customer
Feature: Seller Admin needs to create new Accounts

  Background:
    Given I sign in to CM as CSR user
    And I go to Customer Service

  Scenario: Create Account - successful save
    When I open create account wizard
    And  I fill in the required fields
	And I fill in the decimal attribute 10.00
    And I save account
    Then new Account is created