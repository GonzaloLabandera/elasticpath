@carts @multicarts
Feature: Public shopper can't access named carts

  Scenario: Public shopper cannot see list of addtocartforms
    Given I login as a public shopper
    When I look up an item with code alien_sku
    Then I should not see the following links
      | addtocartforms |

  Scenario: Public shopper cannot access addtocartforms using URI
    Given I login as a registered shopper
    When I look up an item with code alien_sku
    And I follow the link addtocartforms
    And save the addtocartformslist uri
    And I relogin as a new public shopper
    And attempt to access the addtocartformslist uri
    Then the operation is identified as forbidden

  Scenario: Public shopper cannot access root carts link
    Given I login as a public shopper
    When I navigate to root
    Then I should not see the following links
      | carts |

  Scenario: Public shopper cannot access root carts resource using URI
    Given I login as a registered shopper
    When I go to my carts
    And save the carts uri
    And I relogin as a new public shopper
    And attempt to access the carts uri
    Then the operation is identified as forbidden

  Scenario: Public shopper cannot access createcartform using URI
    Given I login as a registered shopper
    When I go to create cart form
    And save the createcartform uri
    And I relogin as a new public shopper
    And attempt to access the createcartform uri
    Then the operation is identified as forbidden

  Scenario: Public shopper cannot POST to createcartform using URI
    Given I login as a registered shopper
    When I go to create cart form
    And save the createcartform uri
    And I relogin as a new public shopper
    And post to the saved createcartform uri
    Then the operation is identified as forbidden
