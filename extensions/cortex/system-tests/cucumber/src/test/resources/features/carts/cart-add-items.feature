@carts
Feature: Cart Add Items

  Background:
    Given I login as a newly registered shopper

  Scenario Outline: Add multiple valid items to cart.
    When I add the following SKU codes and their quantities to the cart
      | code          | quantity     |
      | <ITEM_1_CODE> | <ITEM_1_QTY> |
      | <ITEM_2_CODE> | <ITEM_2_QTY> |
    Then the cart total-quantity is <TOTAL_QTY>
    And the number of cart lineitems is <NUM_LINE_ITEMS>
    And the cart lineitem for item code <ITEM_1_CODE> has quantity of <ITEM_1_QTY>
    And the cart lineitem for item code <ITEM_2_CODE> has quantity of <ITEM_2_QTY>

    Examples:
      | ITEM_1_CODE | ITEM_2_CODE | ITEM_1_QTY | ITEM_2_QTY | TOTAL_QTY | NUM_LINE_ITEMS |
      | alien_sku   | sony_bt_sku | 2          | 3          | 5         | 2              |

  Scenario Outline: Item code specified twice returns error.
    When I add the following SKU codes and their quantities to the cart
      | code        | quantity     |
      | <ITEM_CODE> | <ITEM_QTY> |
      | <ITEM_CODE> | <ITEM_QTY> |
    Then the HTTP status code is 400
    And Structured error message contains:
      | Item '<ITEM_CODE>' has multiple entries. Please combine duplicate items into one entry. |
    And the cart total-quantity is <TOTAL_QTY>

    Examples:
      | ITEM_CODE | ITEM_QTY | TOTAL_QTY |
      | alien_sku | 1        | 0         |

  Scenario Outline: Two separate item codes specified twice returns two errors.
    When I add the following SKU codes and their quantities to the cart
      | code          | quantity     |
      | <ITEM_1_CODE> | <ITEM_QTY>   |
      | <ITEM_1_CODE> | <ITEM_QTY>   |
      | <ITEM_2_CODE> | <ITEM_QTY>   |
      | <ITEM_2_CODE> | <ITEM_QTY>   |
    Then the HTTP status code is 400
    And Structured error message contains:
      | Item '<ITEM_1_CODE>' has multiple entries. Please combine duplicate items into one entry. |
      | Item '<ITEM_2_CODE>' has multiple entries. Please combine duplicate items into one entry. |
    And the cart total-quantity is <TOTAL_QTY>

    Examples:
      | ITEM_1_CODE | ITEM_2_CODE | ITEM_QTY | TOTAL_QTY |
      | alien_sku   | sony_bt_sku | 2        | 0         |

  Scenario Outline: Configurable item code returns error.
    When I add the following SKU codes and their quantities to the cart
      | code               | quantity     |
      | <ITEM_1_CODE>      | <ITEM_QTY>   |
    Then the HTTP status code is 400
    And Structured error message contains:
      | Item '<ITEM_1_CODE>' is a configurable product. Please add it individually using 'additemtocart' form.    |
    And the cart total-quantity is <TOTAL_QTY>

    Examples:
      | ITEM_1_CODE | ITEM_QTY | TOTAL_QTY |
      | sscpwaft    | 1        | 0         |

  Scenario Outline: Invalid item code returns error.
    When I add the following SKU codes and their quantities to the cart
      | code               | quantity     |
      | <ITEM_1_CODE>      | <ITEM_QTY>   |
    Then the HTTP status code is 400
    And Structured error message contains:
      | Item with code '<ITEM_1_CODE>' does not exist. |
    And the cart total-quantity is <TOTAL_QTY>

    Examples:
      | ITEM_1_CODE    | ITEM_QTY | TOTAL_QTY |
      | invalid_sku    | 1        | 0         |

  Scenario Outline: Dependent item code that has separate item code can be added.
    When I add the following SKU codes and their quantities to the cart
      | code               | quantity     |
      | <ITEM_1_CODE>      | <ITEM_QTY>   |
    Then the HTTP status code is 201
    And the cart total-quantity is <TOTAL_QTY>

    Examples:
      | ITEM_1_CODE    | ITEM_QTY | TOTAL_QTY |
      | eco_h_sku      | 1        | 1         |

  Scenario Outline: Dependent item code that does not have separate item code cannot be added.
    When I add the following SKU codes and their quantities to the cart
      | code               | quantity     |
      | <ITEM_1_CODE>      | <ITEM_QTY>   |
    Then the HTTP status code is 400
    And Structured error message contains:
      | Item '<ITEM_1_CODE>' is not sold separately. |
    And the cart total-quantity is <TOTAL_QTY>

    Examples:
      | ITEM_1_CODE        | ITEM_QTY | TOTAL_QTY |
      | tt1568346_sku      | 1        | 0         |

  Scenario Outline: When a valid and invalid quantity is added an error is returned and nothing is added to cart.
    When I add the following SKU codes and their quantities to the cart
      | code        | quantity       |
      | <ITEM_1_CODE> | <ITEM_1_QTY> |
      | <ITEM_2_CODE> | <ITEM_2_QTY> |
    Then the HTTP status code is 400
    And Structured error message contains:
      | 'quantity' value must be greater than or equal to '1'.    |
    And the cart total-quantity is <TOTAL_QTY>

    Examples:
      | ITEM_1_CODE   | ITEM_2_CODE | ITEM_1_QTY | ITEM_2_QTY | TOTAL_QTY |
      | alien_sku     | sony_bt_sku | 0          | 1          | 0         |


  Scenario Outline: When item is already in cart I see an advisory message when navigating to bulk order form.
    When I add the following SKU codes and their quantities to the cart
      | code          | quantity       |
      | <ITEM_1_CODE> | <ITEM_1_QTY>   |
    And I go to add to bulk add cart form
    Then there are advisor messages with the following fields:
      | messageType | messageId            | debugMessage                                                                                                                                                                                                             |
      | warning     | cart.is.not.empty    | Shopping cart already contains '<ITEM_1_QTY>' item(s). Please consider clearing your shopping cart before continuing or you may continue and additional item(s) will be added or updated with the existing cart item(s). |
    Then the HTTP status code is 200
    And the cart total-quantity is <TOTAL_QTY>

    Examples:
      | ITEM_1_CODE   | ITEM_1_QTY | TOTAL_QTY |
      | alien_sku     | 1          | 1         |

  Scenario Outline: Duplicate item codes may be bulk added multiple times when done on different forms.
    When I add the following SKU codes and their quantities to the cart
      | code        | quantity         |
      | <ITEM_1_CODE> | <ITEM_1_QTY_1> |
      | <ITEM_2_CODE> | <ITEM_2_QTY>   |
    And I add the following SKU codes and their quantities to the cart
      | code        | quantity         |
      | <ITEM_1_CODE> | <ITEM_1_QTY_2> |
    Then the HTTP status code is 201
    And the cart total-quantity is <TOTAL_QTY>
    And the number of cart lineitems is <NUM_LINE_ITEMS>
    And the cart lineitem for item code <ITEM_1_CODE> has quantity of <ITEM_1_QTY>
    And the cart lineitem for item code <ITEM_2_CODE> has quantity of <ITEM_2_QTY>

    Examples:
      | ITEM_1_CODE | ITEM_2_CODE | ITEM_1_QTY_1 | ITEM_2_QTY | ITEM_1_QTY_2 | ITEM_1_QTY | TOTAL_QTY | NUM_LINE_ITEMS |
      | alien_sku   | sony_bt_sku | 1            | 1          |3             | 4          | 5         | 2              |

  Scenario Outline: Add item with insufficient quantity returns error
    When I add the following SKU codes and their quantities to the cart
      | code          | quantity     |
      | <ITEM_1_CODE> | <ITEM_1_QTY> |
      | <ITEM_2_CODE> | <ITEM_2_QTY> |
    Then the HTTP status code is 400
    And Structured error message contains:
      | Item '<ITEM_1_CODE>' only has <ITEM_1_INVENTORY> available but <ITEM_1_QTY> were requested.    |
    And the cart total-quantity is <TOTAL_QTY>
    And the number of cart lineitems is <NUM_LINE_ITEMS>

    Examples:
      | ITEM_1_CODE                                 | ITEM_2_CODE | ITEM_1_QTY  | ITEM_2_QTY | TOTAL_QTY | NUM_LINE_ITEMS | ITEM_1_INVENTORY |
      | physicalNoInventory_sku                     | sony_bt_sku | 25          | 3          | 0         | 0              | 0                |

  Scenario Outline: Single SKU products use product enable date and not the SKU enable date.
    When I add the following SKU codes and their quantities to the cart
      | code               | quantity     |
      | <ITEM_1_CODE>      | <ITEM_QTY>   |
    Then the HTTP status code is 201
    And the cart total-quantity is <TOTAL_QTY>

    Examples:
      | ITEM_1_CODE              | ITEM_QTY | TOTAL_QTY |
      | skuFutureEnableDate      | 1        | 1         |

  Scenario Outline: Large number of skus are rejected.
    When I send 4000 duplicate items to be added to the cart
      | code        | quantity   |
      | <ITEM_CODE> | <ITEM_QTY> |
    Then the HTTP status code is 413
    And Structured error message contains:
      | The request is too large. The maximum number of items that can be added to a cart in a single request is 2000. To add more items, create another request. |
    And the cart total-quantity is <TOTAL_QTY>

    Examples:
      | ITEM_CODE | ITEM_QTY | TOTAL_QTY |
      | alien_sku | 1        | 0         |