@Coupons
Feature: Apply Coupon to Order

  Background:
    Given I have no coupons applied to an order

  Scenario Outline: Apply a new coupon to an order
    When I apply a coupon code <COUPON> to the order
    Then the coupon <COUPON> is applied to the order

    Examples:
      | COUPON                                                   |
      | CouponWillApply10PercentOffTheProductWith10PercentCoupon |


  Scenario Outline: Identical coupons are not duplicated in the order
    Given I applied a coupon code <COUPON> to the order
    When I re-apply the same coupon code <COUPON> to the same order
    Then the coupon <COUPON> is applied to the order
    And there is exactly 1 coupon applied to the order

    Examples:
      | COUPON                                                   |
      | CouponWillApply10PercentOffTheProductWith10PercentCoupon |

  Scenario Outline: Attempt to apply an invalid coupon to an order
    When I apply an invalid coupon <INVALID_COUPON> to the order
    Then the coupon is not accepted

    Examples:
      | INVALID_COUPON    |
      | invalidCouponCode |

  Scenario Outline: Coupon code is not case sensitive
    Given a coupon code <COUPON> exists
    When I apply a coupon code <COUPON_IN_OTHER_CASE> to the order
    Then the coupon <COUPON> is applied to the order
    And there is exactly 1 coupon applied to the order

    Examples:
      | COUPON                                                   | COUPON_IN_OTHER_CASE                                     |
      | CouponWillApply10PercentOffTheProductWith10PercentCoupon | COUPONWILLAPPLY10PERCENTOFFTHEPRODUCTWITH10PERCENTCOUPON |
      | CouponWillApply10PercentOffTheProductWith10PercentCoupon | couponwillapply10percentofftheproductwith10percentcoupon |

