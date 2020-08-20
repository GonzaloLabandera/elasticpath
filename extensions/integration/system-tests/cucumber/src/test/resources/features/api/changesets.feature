Feature: Import/Export API: Changeset Support

  Scenario: Import product with changeset specified
    When I import imports/testproduct.xml with change set guid 865223FE-4B81-6971-ED99-0BE65A82C8C2 to the API
    Then response has http status 200
    And summary object can be retrieved
    And summary contains object PRODUCT with count 1
    And summary contains no failures
    And summary contains no warnings
