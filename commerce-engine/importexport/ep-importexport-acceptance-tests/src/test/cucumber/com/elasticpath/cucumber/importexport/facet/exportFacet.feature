@exportFacet
Feature: Export Facet

Scenario: Export Facet
  Given the following facets are saved in the database
    | facet_guid | business_object_id | facet_name | field_key_type | store_code | display_name | facet_type | searchable_option | range_facet_values | facet_group |
    | 123456     | A00093             | Brand      | 2              | STORE      | {}           | 0          | true              | []                 | 3           |
    | 1234567    | A00023             | Category   | 3              | STORE      | {}           | 0          | true              | []                 | 3           |
  When the facets in the database are exported using importexport
  And the exported facets are retrieved
  Then the exported facet records contain a facet with guid 123456
  And the exported facet records contain a facet with guid 1234567