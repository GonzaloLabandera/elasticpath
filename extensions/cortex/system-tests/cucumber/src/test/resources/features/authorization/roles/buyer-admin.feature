@jwtAuthorization
Feature: Account Management - Buyer Admin permissions

  Buyer Admins do not have the permissions to access to any resources in the commerce domain.

  Scenario: Buyer Admin can not access any root links
    Given I login using jwt authorization with the following details
      | scope | MOBEE |
      | roles | buyer_admin |
    When I navigate to root
    Then I should see the field links is empty