@carts @multicarts

Feature:  Multicart behavior for customers shared across different stores

Scenario:  A customer who belongs to two different stores will have different default carts
  Given I create a new shopper profile in scope Mobee
  And I add item with code digital_sku in my cart
  And I save the current cart uri
  When I authenticate with newly created shopper in scope Kobee
  And I go to my default cart
  Then the list of cart lineitems is empty
  And the cart guid is different than the saved cart guid

