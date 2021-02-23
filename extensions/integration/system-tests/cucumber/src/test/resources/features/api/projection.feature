Feature: Import/Export API: Projection

  Scenario: Import category
    When I import imports/category.xml to the API
    And response has http status 200
	And I replace name and import imports/category.xml to the API
    And response has http status 200
    And summary object can be retrieved
    And summary contains object CATEGORY with count 1
    And summary contains no failures
    And summary contains no warnings
    And I wait 3 seconds
	And I get category projection from the API with /Dobee/categories/Games path
	Then Projection was updated
