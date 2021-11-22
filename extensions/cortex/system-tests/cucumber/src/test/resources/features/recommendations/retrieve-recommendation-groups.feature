@recommendations
Feature: Retrieve recommendation groups for an item

  Background:
    Given I login as a public shopper

  Scenario Outline: Can retrieve recommendations for an item when recommendation groups exist.
    When I search for item name <ITEM_NAME>
    And I go to recommendations for an item
    Then I get all the recommendation groups that exist

    Examples:
      | ITEM_NAME |
      | Twilight  |

    Scenario Outline: Can retrieve recommendations for an offer when recommendation groups exist.
      When I search and open the offer for offer name <OFFER_NAME>
      And I go to recommendations for an offer
      Then I get all the recommendation groups that exist

      Examples:
        | OFFER_NAME |
        | Twilight  |