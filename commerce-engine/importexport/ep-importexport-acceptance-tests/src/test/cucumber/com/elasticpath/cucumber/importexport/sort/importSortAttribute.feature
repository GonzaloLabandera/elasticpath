@importSortAttribute
Feature: Import SortAttribute

  Scenario: Import SortAttribute
    Given the sort attribute import data has been emptied out
    When the following sortAttributes are imported using importexport
      | sort_attribute_guid | business_object_id | sort_attribute_group | store_code | descending | display_value | sort_attribute_type | default_attribute |
      | 123456              | A00093             | ATTRIBUTE            | MOBEE      | true       | test          | STRING              | false             |
      | 1234567             | A00023             | ATTRIBUTE            | MOBEE      | false      | test          | STRING              | true              |
    Then the sortAttribute with guid 123456 is persisted
    And the sortAttribute with guid 1234567 is persisted