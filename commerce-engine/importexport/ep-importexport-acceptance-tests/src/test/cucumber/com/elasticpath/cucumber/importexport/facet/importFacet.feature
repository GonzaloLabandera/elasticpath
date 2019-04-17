@importFacet
Feature: Import Facet

Scenario: Import Facet
  Given the facets import data has been emptied out
  When the following facets are imported using importexport
    | facet_guid | business_object_id | facet_name | field_key_type | store_code | display_name                                                               | facet_type | searchable_option | range_facet_values                                                                                                                                                                                                                            | facet_group |
    | 123456     | A00093             | Brand      | 2              | STORE      | [{"language":"en", "value":"Brand"}, {"language":"fr", "value":"BrandFR"}] | 0          | true              | []                                                                                                                                                                                                                                            | 3           |
    | 1234567    | A00023             | Price      | 3              | STORE      | [{"language":"en", "value":"Price"}, {"language":"fr", "value":"PriceFR"}] | 1          | true              | [{"start":0,"end":5,"displayValues":[{"language":"en", "value":"Below $5"}, {"language":"fr", "value":"moins de $5"}]},{"start":5,"end":20,"displayValues":[{"language":"en", "value":"$5 to $20"}, {"language":"fr", "value":"$5 et $20"}]}] | 3           |
  Then the facet with guid 123456 is persisted
  And the facet with guid 1234567 is persisted