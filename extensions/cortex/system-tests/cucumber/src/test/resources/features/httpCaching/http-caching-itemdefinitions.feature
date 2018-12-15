@httpCaching
Feature: HTTP Caching - Item Definitions

  Background:
    Given I am logged in as a public shopper

  Scenario Outline: Item definition should have HTTP caching
    When I view the item definition for item with code <SKU_CODE>
    Then I should see the Cache-Control header in the response with a valid max age and private directive
    And I should see an ETag header in the response

    Examples:
      | SKU_CODE  |
      | alien_sku |

  Scenario Outline: Item definition options list should have HTTP caching
    When I view the item definition for item with code <ITEM_WITH_OPTIONS>
    And I view the list of options
    Then I should see the Cache-Control header in the response with a valid max age and private directive
    And I should see an ETag header in the response

    Examples:
      | ITEM_WITH_OPTIONS |
      | tt64464fn_hd      |

  Scenario Outline: Item definition option should have HTTP caching
    When I view the item definition for item with code <ITEM_WITH_OPTIONS>
    And I view the option <OPTION_KEY>
    Then I should see the Cache-Control header in the response with a valid max age and private directive
    And I should see an ETag header in the response

    Examples:
      | ITEM_WITH_OPTIONS | OPTION_KEY    |
      | tt64464fn_hd      | Video Quality |

  Scenario Outline: Item definition option value should have HTTP caching
    When I view the item definition for item with code <ITEM_WITH_OPTIONS>
    And I view the value of option <OPTION_KEY>
    Then I should see the Cache-Control header in the response with a valid max age and private directive
    And I should see an ETag header in the response

    Examples:
      | ITEM_WITH_OPTIONS | OPTION_KEY    |
      | tt64464fn_hd      | Video Quality |

  Scenario Outline: Item definition components list should have HTTP caching
    When I view the item definition for item with code <BUNDLE_ITEM>
    And I view the list of bundle components
    Then I should see the Cache-Control header in the response with a valid max age and private directive
    And I should see an ETag header in the response

    Examples:
      | BUNDLE_ITEM |
      | mb_2893033  |

  Scenario Outline: Item definition component should have HTTP caching
    When I look up an item with code <BUNDLE_ITEM>
    And I examine the bundle component <BUNDLE_COMPONENT>
    Then I should see the Cache-Control header in the response with a valid max age and private directive
    And I should see an ETag header in the response

    Examples:
      | BUNDLE_ITEM | BUNDLE_COMPONENT |
      | mb_2893033  | New Movies       |

  Scenario Outline: Item definition nested components list should have HTTP caching
    When I look up an item with code <BUNDLE_ITEM>
    And I examine the bundle component <BUNDLE_COMPONENT>
    And I view the list of nested bundle components
    Then I should see the Cache-Control header in the response with a valid max age and private directive
    And I should see an ETag header in the response

    Examples:
      | BUNDLE_ITEM | BUNDLE_COMPONENT |
      | mb_2893033  | New Movies       |

  Scenario Outline: Item definition nested component should have HTTP caching
    When I look up an item with code <BUNDLE_ITEM>
    And I examine the bundle component <BUNDLE_COMPONENT>
    And I open the nested bundle component <NESTED_BUNDLE_COMPONENT>
    Then I should see the Cache-Control header in the response with a valid max age and private directive
    And I should see an ETag header in the response

    Examples:
      | BUNDLE_ITEM | BUNDLE_COMPONENT | NESTED_BUNDLE_COMPONENT |
      | mb_2893033  | New Movies       | The Social Network      |

  Scenario Outline: Item definition nested component options list should have HTTP caching
    When I look up an item with code <BUNDLE_ITEM>
    And I examine the bundle component <BUNDLE_COMPONENT>
    And I open the nested bundle component <NESTED_BUNDLE_COMPONENT>
    And I view the list of options
    Then I should see the Cache-Control header in the response with a valid max age and private directive
    And I should see an ETag header in the response

    Examples:
      | BUNDLE_ITEM | BUNDLE_COMPONENT | NESTED_BUNDLE_COMPONENT |
      | mb_2893033  | New Movies       | Superheroes             |

  Scenario Outline: Item definition nested component option should have HTTP caching
    When I look up an item with code <BUNDLE_ITEM>
    And I examine the bundle component <BUNDLE_COMPONENT>
    And I open the nested bundle component <NESTED_BUNDLE_COMPONENT>
    And I view the option <OPTION_KEY>
    Then I should see the Cache-Control header in the response with a valid max age and private directive
    And I should see an ETag header in the response

    Examples:
      | BUNDLE_ITEM | BUNDLE_COMPONENT | NESTED_BUNDLE_COMPONENT | OPTION_KEY |
      | mb_2893033  | New Movies       | Superheroes             | Resolution |

  Scenario Outline: Item definition nested component option value should have HTTP caching
    When I look up an item with code <BUNDLE_ITEM>
    And I examine the bundle component <BUNDLE_COMPONENT>
    And I open the nested bundle component <NESTED_BUNDLE_COMPONENT>
    And I view the value of option <OPTION_KEY>
    Then I should see the Cache-Control header in the response with a valid max age and private directive
    And I should see an ETag header in the response

    Examples:
      | BUNDLE_ITEM | BUNDLE_COMPONENT | NESTED_BUNDLE_COMPONENT | OPTION_KEY |
      | mb_2893033  | New Movies       | Superheroes             | Resolution |