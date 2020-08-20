Feature: Import/Export API: Store

  Scenario: Export stores
    When I export Store records from the API
    Then response has http status 200
    And response has at least 1 store elements

  @bug
  Scenario: Export stores with StoreCode filter
    When I export Store records with query "FIND Store WHERE StoreCode='mobee'" from the API
    Then response has http status 200
    And response has exactly 1 store elements

  @bug
  Scenario: Export store associations
    When I export StoreAssociation records with parent Store from the API
    Then response has http status 200
    And response has at least 1 store_association elements

  Scenario: Import test store
    When I import imports/teststore.xml to the API
    Then response has http status 200
    And summary object can be retrieved
    And summary contains object STORE with count 1
    And summary contains no failures
    And summary contains no warnings

  Scenario: Check test store
    When I export Store records with query "FIND Store WHERE StoreCode='teststore'" from the API
    Then response has http status 200
    And response has exactly 1 store elements
