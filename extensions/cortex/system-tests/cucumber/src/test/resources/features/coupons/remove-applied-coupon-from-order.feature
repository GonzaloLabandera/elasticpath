@coupons
Feature: Remove Coupon From Order

  Background:
    Given I have no coupons applied to an order

  Scenario Outline: Can remove applied coupon from order
    Given I applied a coupon code <COUPON> to the order
    When I remove the coupon <COUPON> from the order
    And I retrieve the coupon info for the order
    Then there is exactly 0 coupons applied to the order

    Examples:
      | COUPON                                                   |
      | CouponWillApply10PercentOffTheProductWith10PercentCoupon |


  Scenario Outline: Removed coupon will not be automatically added to new orders
    Given I login to the registered shopper with email ID <CUSTOMER_ID>
    And I apply coupon <COUPON> to the order
    When I remove the coupon <COUPON> from the order
    And I retrieve the coupon info for the order
    And there is exactly 0 coupons applied to the order
    Then I add item with code physical_sku to my cart
    And I fill in payment methods needinfo
    And I submit the order
    And I retrieve the coupon info for the new order
    And there is exactly 0 coupons applied to the order

    Examples:
      | CUSTOMER_ID                                 | COUPON                     |
      | itest.remove.applied.coupon@elasticpath.com | AUTO_APPLIED_TO_BE_REMOVED |
