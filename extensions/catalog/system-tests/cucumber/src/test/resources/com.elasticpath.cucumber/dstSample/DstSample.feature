@dstsample
Feature: Data Sync tool sample test

  Scenario: Sync after creating and deleting a price list
    Given I am listening to ep.changesets queue
    And I sign in to the author environment CM as admin user
    And I create and select the newly created change set DST_AddPL
    And I go to Price List Manager
    When I create a new price list with description Test Description and currency USD
    Then I should see newly created price list in the change set
    When I lock and publish latest change set
    And I read ep.changesets message from queue
    Then the json guid value should be same as change set guid
    And json eventType should contain following values
      | key  | value                        |
      | name | CHANGE_SET_READY_FOR_PUBLISH |
    When I run the data sync tool
    And I finalize the latest change set
    And I sign in to the publish environment CM as admin user
    And I go to the publish environment Price List Manager
    Then I should see the new price list in the publish environment
    When I switch to author environment
    And I create and select the newly created change set DST_DeletePL
    And I go to Price List Manager and select the newly created price list
    And I click add item to change set button
    And I delete the newly created price list
    Then I should see deleted price list in the change set
    When I lock and publish latest change set
    And I read ep.changesets message from queue
    Then the json guid value should be same as change set guid
    And json eventType should contain following values
      | key  | value                        |
      | name | CHANGE_SET_READY_FOR_PUBLISH |
    When I run the data sync tool
    And I finalize the latest change set
    And I switch to publish environment
    And I go to the publish environment Price List Manager
    Then the deleted price list no longer exists in publish environment