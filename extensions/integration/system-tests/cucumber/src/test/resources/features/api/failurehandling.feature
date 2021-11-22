Feature: Import/Export API: Failure Handling

  Scenario: Import customer with failure
    When I import imports/testcustomerwitherror.xml to the API
    Then response has http status 200
    And summary object can be retrieved
    And summary contains 1 failures

