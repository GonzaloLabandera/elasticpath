@orders
Feature: Tests order resource

  Scenario: Order access restricted to owner
    Given I login as a registered shopper
    And I add item with code tt888456tw to my cart
    And I retrieve the order
    And save the order uri
    Given I have authenticated as a newly registered shopper
    And attempt to access the other shoppers order
    Then the HTTP status is forbidden

  Scenario: Can't access order through another store
    When I have authenticated on scope mobee as a newly registered shopper
    And I add item with code tt888456tw to my cart
    And I retrieve the order
    And save the order uri
    When I re-authenticate on scope toastie with the newly registered shopper
    And try to access the order from scope mobee on scope toastie
    Then the HTTP status is forbidden

  Scenario Outline: Free order
    Given I login as a newly registered shopper
    And I create a default shipping address on the profile
    When I add item with code <FREE_ITEM> to my cart
    And I retrieve the order
    Then there is no needinfo link to payment-method-info
    And I follow links tax
    And the tax total on the order is $0.00
    When I retrieve the order
    And I follow links total
    And I see the cost field has amount: 0.00, currency: CAD and display: $0.00
    When I submit the order and retrieve the HTTP status
    Then the HTTP status is OK, created

    Examples:
      | FREE_ITEM     |
      | tt0034583_sku |

  Scenario Outline: Verify that deliveries show up based on the type of products in your cart
    Given I have authenticated as a newly registered shopper
    When I add item with code <DIGITAL_ITEM> to my cart
    And I retrieve the order
    And I follow links deliveries
    Then there are 0 links of rel element
    And I add item with code <PHYSICAL_ITEM> to my cart
    And I retrieve the order
    And I follow links deliveries
    Then there are 1 links of rel element

    Examples:
      | PHYSICAL_ITEM           | DIGITAL_ITEM  |
      | handsfree_shippable_sku | tt0970179_sku |

  Scenario: Verify order is removed after purchase is created
    When I login as a registered shopper
    And I add item with code tt888456tw to my cart
    And I retrieve the order
    And save the order uri
    When I submit the order
    And the HTTP status is OK, created
    And attempt to access the completed order
    Then the HTTP status is forbidden
    And the number of cart lineitems is 0

  Scenario: Cannot submit an order when you have no cart content
    Given I login as a registered shopper
    When I retrieve the purchase form
    And there are no needinfo links
    Then there are no submitorderaction links
    And post to a created submitorderaction uri
    And the HTTP status is conflict

  Scenario Outline: Can submit order in French scope
    Given I have authenticated on scope <FRENCH SCOPE> as a newly registered shopper
    And I add item with code <FRENCH SCOPE SKU> to my cart
    And I select only the billing address
    And I fill in payment methods needinfo
    And I retrieve the order
    When I submit the order
    Then the HTTP status is OK, created

    Examples:
      | FRENCH SCOPE | FRENCH SCOPE SKU |
      | toastie      | tt0970179_sku    |

  Scenario: New shopping cart must be created after checkout
    Given I login as a registered shopper
    And I add item with code tt888456tw to my cart
    And capture the uri of the registered shopper's cart
    When I submit the order
    And the HTTP status is OK, created
    Then new cart is created
    
  Scenario: Cant submit the same order more than once
    Given I login as a public shopper
    And I fill in billing address needinfo
    And I fill in email needinfo
    And I fill in payment methods needinfo
    And I add item with code tt888456tw to my cart
    When I submit the order multiple times concurrently
    Then I view my profile
    And I follow links purchases
    And there are 1 links of rel element