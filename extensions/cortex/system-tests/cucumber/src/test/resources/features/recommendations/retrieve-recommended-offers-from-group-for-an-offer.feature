@recommendations
Feature: Retrieve recommended offers from groups for an offer

  Background:
    Given I login as a public shopper

  Scenario Outline: Search for an offer which has two recommendations for crosssell.
  Retrieve them and ensure they are displayed in correct order for an offer.
    When I search and open the offer for offer name <OFFER_NAME>
    And I retrieve the recommendation crosssell for this offer
    Then I get the 2 recommended offers
    And The ordering is correctly preserved <FIRST_ASSOCIATION_NAME>

    Examples:
      | OFFER_NAME                         | FIRST_ASSOCIATION_NAME |
      | multipleAssociationsSourceProduct | associatedProductTwo   |

  Scenario Outline: Search for an offer which has two recommendations for upsell.
  Retrieve them and ensure they are displayed in correct order for an offer.
    When I search and open the offer for offer name <OFFER_NAME>
    And I retrieve the recommendation upsell for this offer
    Then I get the 2 recommended offers
    And The ordering is correctly preserved <FIRST_ASSOCIATION_NAME>

    Examples:
      | OFFER_NAME                         | FIRST_ASSOCIATION_NAME |
      | multipleAssociationsSourceProduct | associatedProductTwo   |

  Scenario Outline: Search for an offer which has two recommendations for replacement.
  Retrieve them and ensure they are displayed in correct order for an offer.
    When I search and open the offer for offer name <OFFER_NAME>
    And I retrieve the recommendation replacement for this offer
    Then I get the 2 recommended offers
    And The ordering is correctly preserved <FIRST_ASSOCIATION_NAME>

    Examples:
      | OFFER_NAME                         | FIRST_ASSOCIATION_NAME |
      | multipleAssociationsSourceProduct | associatedProductTwo   |

  Scenario Outline: Search for an offer which two recommendations for warranty.
  Retrieve them and ensure they are displayed in correct order for an offer.
    When I search and open the offer for offer name <OFFER_NAME>
    And I retrieve the recommendation warranty for this offer
    Then I get the 2 recommended offers
    And The ordering is correctly preserved <FIRST_ASSOCIATION_NAME>

    Examples:
      | OFFER_NAME                         | FIRST_ASSOCIATION_NAME |
      | multipleAssociationsSourceProduct | associatedProductTwo   |

  Scenario Outline: Search for an offer two recommendations for accessory.
  Retrieve them and ensure they are displayed in correct order for an offer.
    When I search and open the offer for offer name <OFFER_NAME>
    And I retrieve the recommendation accessory for this offer
    Then I get the 2 recommended offers
    And The ordering is correctly preserved <FIRST_ASSOCIATION_NAME>

    Examples:
      | OFFER_NAME                         | FIRST_ASSOCIATION_NAME |
      | multipleAssociationsSourceProduct | associatedProductTwo   |

  Scenario Outline: Search for an offer which has two recommendations for crosssell.
  Retrieve them using zoom.Ensure they are displayed in correct order for an offer.
    When I search and open the offer for offer name <OFFER_NAME>
    And I go to recommendations for an offer
    When I zoom <ZOOMPARAM> into the cross-sells for this offer
    Then The zoom ordering is correctly preserved <FIRST_ASSOCIATION_NAME> and <SECOND_ASSOCIATION_NAME>

    Examples:
      | OFFER_NAME                         | ZOOMPARAM                          | FIRST_ASSOCIATION_NAME | SECOND_ASSOCIATION_NAME |
      | multipleAssociationsSourceProduct | ?zoom=crosssell:element:definition | associatedProductTwo   | associatedProductOne    |

  Scenario Outline: Cannot retrieve non-store-visible product through a recommendation
    When I search and open the offer for offer name <OFFER_NAME>
    And I retrieve the recommendation crosssell for this offer
    Then cross-sell offer is not present

    Examples:
      | OFFER_NAME                                                      |
      | SourceProductWithCrossSellsAssocToNonStoreVisibleTargetProduct |