@coupons
Feature: Coupons auto apply

  Scenario Outline: Auto apply saved coupon on login as registered shopper
    Given shopper <AUTO_APPLIED_COUPON_CUSTOMER_ID> has an auto applied coupon <COUPON>
    When I login to the registered shopper with email ID <AUTO_APPLIED_COUPON_CUSTOMER_ID>
    And I retrieve the coupon info for the order
    Then there is exactly 1 coupon applied to the order
    And the coupon <COUPON> is applied to the order

    Examples:
      | AUTO_APPLIED_COUPON_CUSTOMER_ID            | COUPON       |
      | itest.auto.applied.coupons@elasticpath.com | AUTO_APPLIED |


  Scenario Outline: Coupon with usage type other than LimitPerSpecifiedUser should not be automatically added to new orders
    Given shopper <AUTO_APPLIED_COUPON_CUSTOMER_ID> has an auto applied coupon <AUTO_APPLIED_COUPON>
    And I login to the registered shopper with email ID <AUTO_APPLIED_COUPON_CUSTOMER_ID>
    And I applied a coupon code <COUPON> to the order
    When I retrieve the coupon info for the order
    And there is exactly 2 coupons applied to the order
    Then I add item with code physical_sku to my cart
    And I fill in payment methods needinfo
    And I submit the order
    And I retrieve the coupon info for the order
    And there is exactly 1 coupons applied to the order
    And the coupon <AUTO_APPLIED_COUPON> is applied to the order

    Examples:
      | AUTO_APPLIED_COUPON_CUSTOMER_ID            | AUTO_APPLIED_COUPON | COUPON                                                   |
      | itest.auto.applied.coupons@elasticpath.com | AUTO_APPLIED        | CouponWillApply10PercentOffTheProductWith10PercentCoupon |