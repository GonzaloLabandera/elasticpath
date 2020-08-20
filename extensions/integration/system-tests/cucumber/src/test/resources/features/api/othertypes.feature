Feature: Import/Export API: Other Types

  Scenario: Export dynamic content
    Given I export DynamicContent records from the API
    Then response has http status 200
    And response has at least 1 dynamiccontent elements

  Scenario: Export dynamic content delivery
    Given I export DynamicContentDelivery records from the API
    Then response has http status 200
    And response has at least 1 dynamic_content_delivery elements

  Scenario: Export warehouses
    Given I export Warehouse records from the API
    Then response has http status 200
    And response has at least 1 warehouse elements

  Scenario: Export customer profile attributes
    Given I export CustomerProfileAttribute records from the API
    Then response has http status 200
    And response has at least 1 attribute elements

  Scenario: Export tax codes
    Given I export TaxCode records from the API
    Then response has http status 200
    And response has at least 1 code elements

  Scenario: Export user roles
    Given I export UserRole records from the API
    Then response has http status 200
    And response has at least 1 user_role elements

  Scenario: Export CM users
    Given I export CmUser records from the API
    Then response has http status 200
    And response has at least 1 cmuser elements

  Scenario: Export content spaces
    Given I export ContentSpace records from the API
    Then response has http status 200
    And response has at least 1 contentspace elements

  Scenario: Export shipping service levels
    Given I export ShippingServiceLevel records from the API
    Then response has http status 200
    And response has at least 1 shipping_service_level elements

  Scenario: Export shipping regions
    Given I export ShippingRegion records from the API
    Then response has http status 200
    And response has at least 1 shipping_region elements

  Scenario: Export saved conditions
    Given I export SavedCondition records from the API
    Then response has http status 200
    And response has at least 1 condition elements

  Scenario: Export CM import jobs
    Given I export CmImportJob records from the API
    Then response has http status 200
    And response has at least 1 cmimportjob elements

  Scenario: Export data policies
    Given I export DataPolicy records from the API
    Then response has http status 200
    And response has at least 1 data_policy elements

  Scenario: Export payment providers
    Given I export PaymentProvider records from the API
    Then response has http status 200
    And response has at least 1 payment_provider_configuration elements

  Scenario: Export facets
    Given I export Facet records from the API
    Then response has http status 200
    And response has at least 1 facet elements

  Scenario: Export sort attributes
    Given I export SortAttribute records from the API
    Then response has http status 200
    And response has at least 1 sort_attribute elements

  Scenario: Export attribute policies
    Given I export AttributePolicy records from the API
    Then response has http status 200
    And response has at least 1 attribute_policy elements

  Scenario: Export tag groups
    Given I export TagGroup records from the API
    Then response has http status 200
    And response has at least 1 tag_group elements

  Scenario: Export modifier groups
    Given I export ModifierGroup records from the API
    Then response has http status 200
    And response has at least 1 modifiergroup elements

  Scenario: Export modifier group filters
    Given I export ModifierGroupFilter records from the API
    Then response has http status 200
    And response has at least 1 modifiergroupfilter elements

  Scenario: Export user account associations
    Given I export UserAccountAssociation records from the API
    Then response has http status 200
    And response has at least 1 useraccountassociation elements
