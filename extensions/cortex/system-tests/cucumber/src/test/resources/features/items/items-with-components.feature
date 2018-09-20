#TODO: investigate way to test time fields
@Items

Feature: Items with components

  Background:
    Given I am logged in as a public shopper

  Scenario Outline: Item components represent bundle constituents
    Given a bundle item with code <PURCHASABLE_NESTED_BUNDLE> that has <NUMBER_OF_COMPONENTS> constituents
    When I look up an item with code <PURCHASABLE_NESTED_BUNDLE>
    Then the item has <NUMBER_OF_COMPONENTS> components

    Examples:
      | PURCHASABLE_NESTED_BUNDLE | NUMBER_OF_COMPONENTS |
      | mb_2893033                | 2                    |

  Scenario Outline: Bundle constituents can be bundles
    Given a nested bundle with code <PURCHASABLE_NESTED_BUNDLE> that has a bundle constituent with name <NESTED_BUNDLE>
    When I look up an item with code <PURCHASABLE_NESTED_BUNDLE>
    And I examine the nested bundle component <NESTED_BUNDLE>
    Then the nested bundle component has <NUMBER_OF_NESTED_COMPONENTS> components

    Examples:
      | PURCHASABLE_NESTED_BUNDLE | NESTED_BUNDLE | NUMBER_OF_NESTED_COMPONENTS |
      | mb_2893033                | New Movies    | 3                           |

  Scenario Outline: A bundle component that is not sold separately is restricted from being purchased from outside the bundle
    Given a nested bundle with code <PURCHASABLE_NESTED_BUNDLE> that has a bundle constituent with name <NESTED_BUNDLE>
    And nested bundle <NESTED_BUNDLE> has a component <NESTED_BUNDLE_COMPONENT> that is not sold separately
    And I look up an item with code <PURCHASABLE_NESTED_BUNDLE>
    When I attempt to add the nested bundle <NESTED_BUNDLE> component <NESTED_BUNDLE_COMPONENT> to cart
    Then I am prevented from adding the item to the cart

    Examples:
      | PURCHASABLE_NESTED_BUNDLE | NESTED_BUNDLE | NESTED_BUNDLE_COMPONENT |
      | mb_2893033                | New Movies    | The Social Network      |

  Scenario Outline: Bundle components refer to an underlying standalone item
    Given a bundle with code <PURCHASABLE_BUNDLE> that has a bundle constituent with name <STANDALONE_ITEM>
    When I look up an item with code <PURCHASABLE_BUNDLE>
    Then the component has a reference to the standalone item <STANDALONE_ITEM>

    Examples:
      | PURCHASABLE_BUNDLE | STANDALONE_ITEM    |
      | mb_6789012_sku      | The Social Network |
