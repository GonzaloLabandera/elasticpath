@regressionTest @customerService @customer
Feature: Children Account Management

	Background:
		Given I sign in to CM as CSR user
		And I go to Customer Service

	Scenario Outline: Parent hierarchy for account with less than 5 children
		When I search and open account editor for shared ID <sharedId>
		Then I should see account with ordered parents:
			| <parent1> |
			| <parent2> |
			| <parent3> |
			| <parent4> |

		Examples:
			| sharedId              | parent1        | parent2        | parent3        | parent4        |
			| SomeBusiness5@abc.com | Some Business1 | Some Business2 | Some Business3 | Some Business4 |

	Scenario Outline: Parent hierarchy for account with more than 4 children
		When I search and open account editor for shared ID <sharedId>
		Then I should see account with ordered parents:
			| <parent1> |
			| <parent2> |
			| <parent3> |
			| <parent4> |

		Examples:
			| sharedId              | parent1        | parent2        | parent3        | parent4        |
			| SomeBusiness6@abc.com | Some Business1 | Some Business3 | Some Business4 | Some Business5 |

	Scenario Outline: Review first level children
		When I search and open account editor for shared ID <sharedId>
		And I select Child Accounts tab in the Customer Editor
		Then Child Accounts table contains first level <firstLevelChild> child
		And Child Accounts table does not contain <secondLevelChild> child

		Examples:
			| sharedId              | firstLevelChild | secondLevelChild |
			| SomeBusiness3@abc.com | Some Business4  | Some Business5   |

	Scenario Outline: Review child of account
		When I search and open account editor for shared ID <sharedId>
		And I select Child Accounts tab in the Customer Editor
		And I open <child> child
		Then I should see opened account with direct parent <businessName> name

		Examples:
			| sharedId              | child          | businessName   |
			| SomeBusiness3@abc.com | Some Business4 | Some Business3 |

	Scenario Outline: Review child of account
		When I search and open account editor for shared ID <sharedId>
		And I select Child Accounts tab in the Customer Editor
		And I add new child
		And I fill in the required fields
		And I save account
		And I close account search results tab
		And I close found customer
		And I search and open account editor for shared ID <sharedId>
		And I select Child Accounts tab in the Customer Editor
		Then Child Accounts table contains created child

		Examples:
			| sharedId              |
			| SomeBusiness5@abc.com |

	Scenario Outline: Delete child account
      When I search and open account editor for shared ID <sharedId>
      And I select Child Accounts tab in the Customer Editor
      And I delete <child> child
      Then Child Accounts table does not contain <child> child

      Examples:
        | sharedId               | child          |
        | account-delete@abc.com | account-delete |
