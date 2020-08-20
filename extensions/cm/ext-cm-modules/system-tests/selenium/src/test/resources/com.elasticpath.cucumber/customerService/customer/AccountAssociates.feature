@regressionTest @customerService @customer
Feature: Account Associates

  Background:
    Given I sign in to CM as admin user

  @smokeTest
  Scenario Outline: Add associate user to account
	Given I go to Customer Service
	And I search and open account editor for shared ID <accountSharedId>
	And I select Associates tab in the Customer Editor
	When I add associate user <associateUserEmail>
	Then user with email <associateUserEmail> is added to the Associates table

    Examples:
      | accountSharedId                | associateUserEmail                 |
      | SomeBusiness@abc.com           | harry.potter@elasticpath.com 	    |

  @smokeTest
  Scenario Outline: Add duplicate user to account
	Given I go to Customer Service
	And I search and open account editor for shared ID <accountSharedId>
	And I select Associates tab in the Customer Editor
	When I add invalid associate user <associateUserEmail>
	Then the following error message is displayed: <errorMessage>
	
    Examples:
      | accountSharedId                | associateUserEmail             | errorMessage |
      | SomeBusiness@abc.com           | harry.potter@elasticpath.com   | The selected user is already an associate of this account.|
 
  @smokeTest
  Scenario Outline: Delete associate user from account
	Given I go to Customer Service
	And I search and open account editor for shared ID <accountSharedId>
	And I select Associates tab in the Customer Editor
	And I delete associate user <associateUserEmail>
	Then user with email <associateUserEmail> is removed from the Associates table
	
	Examples:
      | accountSharedId                | associateUserEmail                 |
      | SomeBusiness@abc.com           | harry.potter@elasticpath.com 	    |

  @smokeTest
  Scenario Outline: Add nonexistant user to account
	Given I go to Customer Service
	And I search and open account editor for shared ID <accountSharedId>
	And I select Associates tab in the Customer Editor
	When I add invalid associate user <associateUserEmail>
	Then the following error message is displayed: <errorMessage>
	
    Examples:
      | accountSharedId                | associateUserEmail         | errorMessage |
      | SomeBusiness@abc.com           | doesnot@exist.com    	    | No registered user can be found with the specified email address. The user must register on the storefront before they can be associated with this account.|
