@exportSortAttribute
Feature: Export SortAttribute

  Scenario: Export SortAttribute
    Given the following sortAttributes are saved in the database
      | sort_attribute_guid | business_object_id | sort_attribute_group | store_code | descending | display_value | sort_attribute_type | default_attribute |
      | 123456              | A00093             | ATTRIBUTE            | MOBEE      | true       | test          | STRING              | false             |
      | 1234567             | A00023             | ATTRIBUTE            | MOBEE      | false      | test          | STRING              | true              |
    When the sortAttributes in the database are exported using importexport
    And the exported sortAttributes are retrieved
    Then the exported sortAttribute records contain a sortAttribute with guid 123456
    And the exported sortAttribute records contain a sortAttribute with guid 1234567