@associates @accounts
Feature: Account Associates

  Background:
    Given I authenticate with BUYER_ADMIN username testbuyeradmin@elasticpath.com and password password and role REGISTERED in scope mobee

  Scenario Outline: Can retrieve list of account associates
    Given I navigate links defaultprofile -> accounts
    And I get the account with the field account-business-name with value Some Business Account
    When I follow links associates
    Then the account associate with email <EMAIL> should have the role <ROLE>
    Examples:
      | EMAIL                           | ROLE        |
      | testbuyeradmin@elasticpath.com  | BUYER_ADMIN |

  Scenario Outline: Add associate form fields validations - all fields empty
    Given I navigate links defaultprofile -> accounts
    And I get the account with the field account-business-name with value Some Business Account
    When I follow links associates -> addassociateform
    And I create an account associate with email <EMAIL>, role <ROLE>
    Then the HTTP status is bad request
    And Structured error message contains:
      | email must not be blank |
      | role must not be blank  |

    Examples:
      | EMAIL | ROLE |
      |       |      |

  Scenario Outline: Add associate form fields validations - incorrect email
    Given I navigate links defaultprofile -> accounts
    And I get the account with the field account-business-name with value Some Business Account
    When I follow links associates -> addassociateform
    And I create an account associate with email <EMAIL>, role <ROLE>
    Then the HTTP status is bad request
    And Structured error message contains:
      | The user with the specified email does not exist. |

    Examples:
      | EMAIL                          | ROLE        |
      | james.bon@elasticpath.com      | BUYER_ADMIN |

  Scenario Outline: Add associate form fields validations - incorrect role
    Given I navigate links defaultprofile -> accounts
    And I get the account with the field account-business-name with value Some Business Account
    When I follow links associates -> addassociateform
    And I create an account associate with email <EMAIL>, role <ROLE>
    Then the HTTP status is bad request
    And Structured error message contains:
      | The specified role is not configured in the system |

    Examples:
      | EMAIL                     | ROLE           |
      | james.bond@elasticpath.com | INCORRECT_ROLE |

  Scenario Outline: BUYER_ADMIN can add account associate
    Given I navigate links defaultprofile -> accounts
    And I get the account with the field account-business-name with value Some Business Account
    When I follow links associates -> addassociateform
    And I create an account associate with email <EMAIL>, role <ROLE>
    And I get the associate with the field role with value <ROLE>
    And I follow links associatedetails
    Then I get the associatedetails with the field email with value <EMAIL>
    Examples:
      | EMAIL                      | ROLE        |
      | james.bond@elasticpath.com | BUYER_ADMIN |

  Scenario: User cannot update their own role
    Given I navigate links defaultprofile -> accounts
    And I get the account with the field account-business-name with value Some Business Account
    And I follow links associates
    Then I navigate to the account associate with email testbuyeradmin@elasticpath.com
    When I update role as BUYER
    Then the HTTP status is bad request
    And Structured error message contains:
      | User cannot update their own role |

  Scenario: Update account associate role validation
    Given I navigate links defaultprofile -> accounts
    And I get the account with the field account-business-name with value Some Business Account
    And I follow links associates
    Then I navigate to the account associate with email james.bond@elasticpath.com
    When I update role as BUYE
    Then the HTTP status is bad request
    And Structured error message contains:
      | The specified role is not configured in the system |

  Scenario: User can update other user role
    Given I navigate links defaultprofile -> accounts
    And I get the account with the field account-business-name with value Some Business Account
    And I follow links associates
    Then I navigate to the account associate with email james.bond@elasticpath.com
    When I update role as BUYER
    Then the HTTP status is no content

  Scenario: Delete associate
    Given I navigate links defaultprofile -> accounts
    And I get the account with the field account-business-name with value Some Business Account
    And I follow links associates
    Then I navigate to the account associate with email james.bond@elasticpath.com
    When attempting a DELETE on the associate
    Then the HTTP status is no content