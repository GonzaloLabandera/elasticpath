@accounts
Feature: Account child accounts

  Background:
    Given I authenticate with BUYER_ADMIN username usertest3@elasticpath.com and password password and role REGISTERED in scope mobee

  Scenario: User can review child accounts of associated account
    Given I navigate links defaultprofile -> accounts
    And I get the account with the field account-business-name with value parent
    When I follow links childaccounts
    Then there are 5 links of rel element
    And there are 1 links of rel account
    And there is an element with field account-business-name containing Child1
    And there is an element with field account-business-name containing Child2
    And there is an element with field account-business-name containing Child3
    And there is an element with field account-business-name containing Child4
    And there is an element with field account-business-name containing Child5
    When I follow links next
    Then there are 1 links of rel element
    And there is an element with field account-business-name containing Child6
    And there are 1 links of rel account
    And there are 1 links of rel previous

  Scenario: Child accounts contain all account specific links
    Given I navigate links defaultprofile -> accounts
    And I get the account with the field account-business-name with value parent
    When I follow links childaccounts
    And I open the element with field account-business-name and value Child1
    Then there are 1 links of rel attributes
    And there are 1 links of rel status
    And there are 1 links of rel childaccounts
    And there are 1 links of rel identifier
    And there are 1 links of rel addresses
    And there are 1 links of rel paymentinstruments
    And there are 1 links of rel paymentmethods
    And there are 1 links of rel purchases
    When I follow links childaccounts
    And I open the element with field account-business-name and value Child12
    Then there are 1 links of rel attributes
    And there are 1 links of rel status
    And there are 1 links of rel childaccounts
    And there are 1 links of rel identifier
    And there are 1 links of rel addresses
    And there are 1 links of rel paymentinstruments
    And there are 1 links of rel paymentmethods
    And there are 1 links of rel purchases

  Scenario: Child account orders show up in accountpurchaselist link
    Given I authenticate with BUYER_ADMIN username usertest4@elasticpath.com and password password and role REGISTERED in scope mobee
    And I add X-Ep-Account-Shared-Id header Child1@abc.com
    And I have previously made a purchase with item code digital_sku
    And I remove the X-Ep-Account-Shared-Id header
    When I authenticate with BUYER_ADMIN username usertest3@elasticpath.com and password password and role REGISTERED in scope mobee
    And I navigate links defaultprofile -> accounts
    And I get the account with the field account-business-name with value parent
    And I follow links childaccounts
    And I open the element with field account-business-name and value Child1
    And I follow links purchases
    Then there is an element for the newly create order