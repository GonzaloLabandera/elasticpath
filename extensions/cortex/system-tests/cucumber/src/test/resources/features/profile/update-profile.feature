@profile
Feature: Update profile

  Scenario: Create email id as public shopper
    When I login as a public shopper
    And I create an email
    Then I can see the new email id in my profile

  Scenario: Update email id as registered shopper
    When I have authenticated as a newly registered shopper
    And I create an email
    Then I can see the new email id in my profile

  Scenario Outline: Update family-name and given-name as registered shopper
    When I have authenticated as a newly registered shopper
    And I update my profile family-name <FAMILY_NAME> and given-name <GIVEN_NAME>
    Then I should see my profile name as family-name <FAMILY_NAME> and given-name <GIVEN_NAME>

    Examples:
      | FAMILY_NAME    | GIVEN_NAME    |
      | testFamilyName | testGivenName |

  Scenario Outline: Accept single quotes, multi-line comments and backslash for strings.
    When I have authenticated as a newly registered shopper
    And I PUT to profile with json body <JSON>
    Then the HTTP status is no content

    Examples:
      | JSON                                                               |
      | {"given-name":'single-quoted', "family-name" : "test"}             |
      | {/*Im a comment */ "given-name" : 'test2', "family-name" : "test"} |
      | {"given-name" : "h\arry", "family-name" : "test"}                  |

  Scenario Outline: Accept UTF-8 encoded characters in comments.
    When I have authenticated as a newly registered shopper
    And I PUT to profile with json body <JSON>
    Then the HTTP status is no content

    Examples:
      | JSON                                                             |
      | {/*Copyright © */ "given-name" : "test", "family-name" : "test"} |

  Scenario Outline: Reject request because object is empty.
    When I have authenticated as a newly registered shopper
    And I PUT to profile with json body <JSON>
    Then the HTTP status is bad request

    Examples:
      | JSON                                      |
      | {//Commented line "given-name" : "test3"} |

  Scenario Outline: Cannot clear required fields in profile
    When I have authenticated as a newly registered shopper
    And I update my profile family-name <FAMILY_NAME> and given-name <GIVEN_NAME>
    Then the HTTP status is bad request

    Examples:
      | FAMILY_NAME | GIVEN_NAME |
      |             |            |

  Scenario Outline: Reject request because DOB is invalid.
    When I have authenticated as a newly registered shopper
    And I PUT to profile with json body <JSON>
    Then the HTTP status is bad request

    Examples:
      | JSON                                      |
      | { "date-of-birth" : "12345"} |

  Scenario Outline: Accept request because DOB is valid.
    When I have authenticated as a newly registered shopper
    And I PUT to profile with json body <JSON>
    Then the HTTP status is no content

    Examples:
      | JSON                                                                             |
      | { "given-name" : "test", "family-name" : "test", "date-of-birth" : "1994-01-01"} |

  Scenario: Cannot update another shopper's profile
    Given I login as a newly registered shopper
    And I view my profile
    And save the profile uri
    And I update my profile family-name originalFamily and given-name newGiven
    When I authenticate as another shopper and attempt to update the other shoppers profile family-name NEWFAMILY and given-name NEWGIVEN
    Then the HTTP status is forbidden
    And I re-authenticate on scope mobee with the original registered shopper
    And I view my profile
    And the field family-name contains value originalFamily
    And the field given-name contains value newGiven

  Scenario Outline: Able to remove field value
    When I have authenticated as a newly registered shopper
    And I PUT to profile with json body <JSON>
    And the HTTP status is no content
    And I view my profile
    And the field date-of-birth contains value 1994-01-01
    And I PUT to profile with json body <UPDATE_JSON>
    Then I should see a null value for profile field date-of-birth

  Examples:
    | JSON                                                                            | UPDATE_JSON                                                             |
    | {"given-name" : "test", "family-name" : "test", "date-of-birth" : "1994-01-01"} | {"given-name" : "test", "family-name" : "test", "date-of-birth" : null} |
    | {"given-name" : "test", "family-name" : "test", "date-of-birth" : "1994-01-01"} | {"given-name" : "test", "family-name" : "test"}                         |

  Scenario Outline: Missing required fields
    When I have authenticated as a newly registered shopper
    And I PUT to profile with json body <JSON>
    Then the HTTP status is bad request
    And the response message is Required profile fields {given-name,family-name} are missing.

    Examples:
      | JSON |
      | { }  |
