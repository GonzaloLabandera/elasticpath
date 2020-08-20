Feature: Import/Export API: Tax Jurisdiction

  Scenario: Export tax jurisdictions
    Given I export TaxJurisdiction records from the API
    Then response has http status 200
    And response has at least 1 jurisdiction elements

  Scenario: Export tax jurisdictions with TaxJurisdictionCode filter
    Given I export TaxJurisdiction records with query "FIND TaxJurisdiction WHERE TaxJurisdictionCode='CATaxJurisdiction'" from the API
    Then response has http status 200
    And response has exactly 1 jurisdiction elements

  Scenario: Export tax jurisdictions with TaxJurisdictionRegion filter
    Given I export TaxJurisdiction records with query "FIND TaxJurisdiction WHERE TaxJurisdictionRegion='US'" from the API
    Then response has http status 200
    And response has at least 1 jurisdiction elements
