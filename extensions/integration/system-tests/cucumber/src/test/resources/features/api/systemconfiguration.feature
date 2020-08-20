Feature: Import/Export API: System Configuration

  Scenario: Export system configuration
    Given I export SystemConfiguration records from the API
    Then response has http status 200
    And response has at least 10 configuration_setting elements

  Scenario: Export system configuration with Namespace filter
    Given I export SystemConfiguration records with query "FIND Configuration WHERE Namespace='COMMERCE/SYSTEM/ASSETS/assetLocation'" from the API
    Then response has http status 200
    And response has exactly 1 configuration_setting elements

  Scenario: Export system configuration with Namespace and Context filters
    Given I export SystemConfiguration records with query "FIND Configuration WHERE Namespace='COMMERCE/STORE/listPagination' AND Context='MOBEE'" from the API
    Then response has http status 200
    And response has exactly 1 configuration_setting elements
