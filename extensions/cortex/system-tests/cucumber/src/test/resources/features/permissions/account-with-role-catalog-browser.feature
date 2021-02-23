@permissions
Feature: Catalog browser role for account

  Background:
    Given I authenticate with BUYER username usertest3@elasticpath.com and password password and role REGISTERED in scope mobee
    And I add X-Ep-Account-Shared-Id header CatalogBrowser@elasticpath.com

  Scenario: Account gets role CATALOG_BROWSER from associations and has no access to cart.
	When I navigate to root
	Then I should not see the following links
	  | carts              |
	When I go to my cart
	Then I should not see the following links
	  | additemstocartform |

  Scenario: Account gets role CATALOG_BROWSER from associations and has access to prices.
    Given an item Product With No Discounts exists in my catalog
    When I view the item price
    Then the list-price has fields amount: 10.00, currency: CAD and display: $10.00
    And the purchase-price has fields amount: 10.00, currency: CAD and display: $10.00

  Scenario: Account gets role CATALOG_BROWSER from associations from store and has access to discount.
    Then the cart discount amount is $0.00

  Scenario Outline: Account gets role CATALOG_BROWSER from associations and has access to total.
    When I retrieve the cart total
    Then the cost fields has amount: <AMOUNT>, currency: <CURRENCY> and display: <DISPLAY_AMOUNT>
    Examples:
       | AMOUNT | CURRENCY | DISPLAY_AMOUNT |
       |0.00  | CAD        | $0.00          |
