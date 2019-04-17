@httpCaching
Feature: HTTP Caching - Coupons

  Scenario Outline: Order coupon should have HTTP caching
    Given I login as a newly registered shopper
    When I apply a coupon code <COUPON> to the order
    And I retrieve the coupon <COUPON> details of my order
    Then I should see the Cache-Control header in the response with a valid max age and private directive
    And I should see an ETag header in the response

    Examples:
      | COUPON                                                   |
      | CouponWillApply10PercentOffTheProductWith10PercentCoupon |

  Scenario Outline: Purchase coupons list should have HTTP caching
    When I have the product <ACTIVE_PROMOTION_ITEM> with coupon <COUPON> applied to my purchase
    When Shopper retrieves the coupon info of their purchase
    Then I should see the Cache-Control header in the response with a valid max age and private directive
    And I should see an ETag header in the response

    Examples:
      | COUPON                                                   | ACTIVE_PROMOTION_ITEM              |
      | CouponWillApply10PercentOffTheProductWith10PercentCoupon | Product With 10 Percent Off Coupon |

  Scenario Outline: Purchase coupon should have HTTP caching
    When I have the product <ACTIVE_PROMOTION_ITEM> with coupon <COUPON> applied to my purchase
    And I retrieve the coupon <COUPON> details of my purchase
    Then I should see the Cache-Control header in the response with a valid max age and private directive
    And I should see an ETag header in the response

    Examples:
    | COUPON                                                   | ACTIVE_PROMOTION_ITEM              |
    | CouponWillApply10PercentOffTheProductWith10PercentCoupon | Product With 10 Percent Off Coupon |

  Scenario: Apply coupon to order form should have HTTP caching
    Given I login as a newly registered shopper
    When I navigate to apply coupon form
    Then I should see the Cache-Control header in the response with a valid max age and private directive
    And I should see an ETag header in the response
