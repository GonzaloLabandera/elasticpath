@regressionTest @configuration @sortAttributes
Feature: Sort Attribute Configuration

  Background:
    Given I am logged in as a public shopper
    And I sign in to CM as admin user
    And I go to Configuration
    And I go to Stores
    And I edit store MOBEE in editor
    And I select Sorting tab in the Store Editor

  @cleanupAddedSortAttribute
  Scenario Outline: Add sort attribute
    When I add the following sort attribute
      | sortGroup    | attributeKey | sortOrder  | language | displayName    |
      | <SORT_GROUP> | <KEY>        | Descending | English  | <DISPLAY_NAME> |
    Then I should see newly added sort attribute in sorting table
    When I search for offer Movies
    And I follow links sortattributes
    Then I should see a choice with field display-name and value <DISPLAY_NAME>

    Examples:
      | SORT_GROUP | KEY         | DISPLAY_NAME      |
      | Attribute  | A00007      | Runtime Ascending |
      | Field      | Sales Count | Top Sellers       |

  @cleanupModifiedSortAttribute
  Scenario: Edit sort attribute
    And the following sort attribute exists
      | displayName | sortOrder |
      | name A-Z    | Ascending |
    When I edit sort attribute with display name name A-Z to have the following values
      | displayName     | sortOrder  |
      | Descending Name | Descending |
    And I search for offer Movies
    And I follow links sortattributes
    And I select the choice with field display-name and value Descending Name
    And I follow the link offersearchresult
    Then the element list contains items with display-names
      | Twilight                      |
      | Transformers Registered       |
      | Transformers Over 50          |
      | Transformers First Time Buyer |
      | Transformers Female           |

  @cleanupRatingSortAttribute
  Scenario: Remove sort attribute
    When I remove sort attribute with display name rating low to high
    Then I should not see recently removed sort attribute in sorting table
    When I search for offer Movies
    And I follow links sortattributes
    Then I should not see a choice with field display-name and value rating low to high

  Scenario: Sort attribute display name localization
    When I edit sort attribute with display name name A-Z to have the following values
      | language | displayName |
      | French   | name A-Z fr |
    When I am shopping in locale fr with currency CAD
    And I search for offer Movies
    And I follow links sortattributes
    Then I should see a choice with field display-name and value name A-Z fr

  @cleanupDefaultSortAttribute
  Scenario: Change sort attribute that is used by default
    Given the default sort attribute is Best Match
    When I make sort attribute with display name Price low to high the default
    And I search for offer Movies
    And I follow links sortattributes
    Then I should see a choice with field display-name and value Price low to high
